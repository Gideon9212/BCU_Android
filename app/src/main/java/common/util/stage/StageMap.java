package common.util.stage;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonClass.JCConstructor;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.io.json.localDecoder;
import common.pack.FixIndexList.FixIndexMap;
import common.pack.Identifier;
import common.pack.IndexContainer;
import common.pack.PackData;
import common.pack.UserProfile;
import common.system.BasedCopable;
import common.system.files.FileData;
import common.system.files.VFile;
import common.util.Data;
import common.util.lang.MultiLangCont;
import common.util.lang.MultiLangData;
import common.util.stage.info.DefStageInfo;

import java.util.*;

@IndexContainer.IndexCont(MapColc.class)
@JsonClass
@JsonClass.JCGeneric(Identifier.class)
public class StageMap extends Data implements BasedCopable<StageMap, MapColc>,
		IndexContainer.Indexable<MapColc, StageMap>, IndexContainer.SingleIC<Stage> {

	public static class StageMapInfo {

		public final StageMap sm;

		private final Queue<String> qs;

		public int rand, time, lim;

		public int waitTime = -1, clearLimit = -1, resetMode = -1;

		public int[] materialDrop;
		public double[] multiplier;

		public boolean hiddenUponClear = false;
		public boolean unskippable = false;

		private StageMapInfo(StageMap map, FileData ad) {
			sm = map;
			qs = ad.readLine();
			int[] ints = CommonStatic.parseIntsN(qs.poll().split("//")[0]);
			if (ints.length > 3) {
				rand = ints[1];
				time = ints[2];
				lim = ints[3];
			}
			qs.poll();
		}

		protected void getData(Stage s) {
			String line = qs.poll();

			if(line == null)
				return;

			int[] ints = CommonStatic.parseIntsN(line.split("//")[0]);
			if (ints.length <= 4)
				return;
			s.info = new DefStageInfo(this, s, ints);
		}

		protected void injectMaterialDrop(String[] value) {
			int materialNumber = value.length - 13;

			materialDrop = new int[materialNumber];
			for(int i = 0; i < materialDrop.length; i++)
				materialDrop[i] = CommonStatic.safeParseInt(value[13 + i]);

			multiplier = new double[4];
			for(int i = 0; i < multiplier.length; i++)
				multiplier[i] = Double.parseDouble(value[1 + i]);

			for(int i = 0; i < sm.list.size(); i++) {
				Stage s = sm.list.get(i);
				if (s.info instanceof DefStageInfo)
					((DefStageInfo) s.info).maxMaterial = CommonStatic.safeParseInt(value[5 + i]);
			}
		}
	}

	@ContGetter
	public static StageMap get(String str) {
		String[] strs = str.split("/");
		if (strs[0].equals(Stage.CLIPMC.getSID()))
			return Stage.CLIPMC.maps.get(Integer.parseInt(strs[1]));
		return new Identifier<>(strs[0], StageMap.class, Integer.parseInt(strs[1])).get();
	}

	@JsonField
	@JsonClass.JCIdentifier
	public final Identifier<StageMap> id;
	@JsonField(generic = Limit.PackLimit.class, defval = "isEmpty")
	public final ArrayList<Limit> lim = new ArrayList<>();
	public StageMapInfo info;

	@JsonField(generic = Stage.class, defval = "isEmpty")
	public final FixIndexMap<Stage> list = new FixIndexMap<>(Stage.class);

	@JsonField(generic = MultiLangData.class, gen = JsonField.GenType.FILL, defval = "empty")
	public final MultiLangData names = new MultiLangData();
	@JsonField(generic = StageMap.class, alias = Identifier.class, decodeLast = true, backCompat = JsonField.CompatType.FORK, defval = "isEmpty")
	public final HashSet<StageMap> unlockReq = new HashSet<>();

	@JsonField(defval = "1")
	public int price = 1;
	@JsonField
	public int[] stars = new int[] { 100 };

	public int starMask = 0, cast = -1;



	@JCConstructor
	public StageMap() {
		this.id = null;
	}

	public StageMap(Identifier<StageMap> id) {
		this.id = id;
		names.put("new stage map");
	}

	protected StageMap(Identifier<StageMap> id, FileData m) {
		this.id = id;
		info = new StageMapInfo(this, m);
	}

	protected StageMap(Identifier<StageMap> id, String stn, int cas) {
		this(id, VFile.get(stn).getData());
		cast = cas;
	}

	public void add(Stage s) {
		if (s == null)
			return;
		list.add(s);
	}

	public LinkedList<StageMap> getUnlockableMaps() {
		LinkedList<StageMap> reqMaps = new LinkedList<>();
		for (StageMap sm : getCont().maps)
			if (sm.unlockReq.contains(this))
				reqMaps.add(sm);
		for (PackData.UserPack pac : UserProfile.getUserPacks())
			if (pac.desc.dependency.contains(id.pack))
				for (StageMap sm : pac.mc.maps)
					if (sm.unlockReq.contains(this))
						reqMaps.add(sm);
		return reqMaps;
	}

	@Override
	public StageMap copy(MapColc mc) {
		StageMap sm = new StageMap(mc.getNextID());
		sm.names.overwrite(names);

		sm.price = price;
		sm.stars = stars.clone();
		sm.price = price;

		for (Stage st : list)
			sm.add(st.copy(sm));

		return sm;
	}

	@Override
	public FixIndexMap<Stage> getFIM() {
		return list;
	}

	@Override
	public Identifier<StageMap> getID() {
		return id;
	}

	@Override
	public String getSID() {
		return id.pack + "/" + id.id;
	}

	@Override
	public String toString() {
		String desp = MultiLangCont.get(this);
		if (desp != null && !desp.isEmpty())
			return desp;
		String stName = names.toString();
		if (stName.isEmpty())
			return id + " (" + list.size() + ")";
		return stName;
	}

	@JsonDecoder.OnInjected
	public void onInjected(JsonObject jobj) {
		PostLoad();
		if (jobj.has("name"))
			names.put(jobj.get("name").getAsString());

		for (int i = 0; i < lim.size(); i++)
			if (lim.get(i).sid != -1) {
				Limit l = lim.get(i);
				Stage st = list.get(l.sid);
				if (st.lim.none())
					st.lim = l.clone();
				else
					st.lim.combine(l);
				l.sid = -1;
				lim.remove(i--);
			} else if (!(lim.get(i) instanceof Limit.PackLimit))
				lim.set(i, new Limit.PackLimit(lim.get(i)));
		if (jobj.has("stageLimit")) {
			StageLimit lim = new localDecoder(jobj.get("stageLimit"), StageLimit.class, this).decode();
			if (lim == null || lim.isBlank())
				return;
			Limit.PackLimit nlim = new Limit.PackLimit();
			nlim.stageLimit = lim;
			this.lim.add(nlim);
		}

		MapColc.PackMapColc mc = (MapColc.PackMapColc)getCont();
		if (mc.pack.desc.FORK_VERSION < 11) {
			if (UserProfile.isOlderPack(mc.pack, "0.7.8.2"))
				for (Limit l : lim) {
					if (l.stageLimit == null)
						continue;
					l.stageLimit.coolStart = l.stageLimit.globalCooldown > 0 || l.stageLimit.maxMoney > 0;
				}
			for (Limit l : lim)
				l.setStar(l.star); //Convert star limit to bitmask. There's only 4 stars anyway
		}
	}

	@JsonDecoder.PostLoad
	public void PostLoad() {
		unlockReq.removeIf(Objects::isNull);
	}
}
