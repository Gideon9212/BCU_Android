package common.util.stage;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.io.assets.Admin.StaticPermitted;
import common.io.json.Dependency;
import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.IndexContainer;
import common.pack.PackData.PackDesc;
import common.pack.PackData.UserPack;
import common.pack.Source.ResourceLocation;
import common.pack.UserProfile;
import common.system.BasedCopable;
import common.system.files.VFile;
import common.util.BattleStatic;
import common.util.Data;
import common.util.lang.MultiLangCont;
import common.util.lang.MultiLangData;
import common.util.pack.Background;
import common.util.stage.SCDef.Line;
import common.util.stage.info.CustomStageInfo;
import common.util.stage.info.DefStageInfo;
import common.util.stage.info.StageInfo;
import common.util.unit.AbEnemy;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@IndexContainer.IndexCont(StageMap.class)
@JsonClass.JCGeneric(Identifier.class)
@JsonClass(noTag = NoTag.LOAD)
public class Stage extends Data
		implements Comparable<Stage>, BasedCopable<Stage, StageMap>, BattleStatic, IndexContainer.Indexable<StageMap, Stage> {

	@StaticPermitted
	public static final MapColc CLIPMC = new MapColc.ClipMapColc();
	@StaticPermitted
	public static final StageMap CLIPSM = CLIPMC.maps.get(0);
	@StaticPermitted
	private static final int[] CH_CASTLES = { 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28,
			27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 46,
			47, 45, 47, 47, 45, 45 };

	@JsonField(block = true)
	public StageInfo info;
	@JsonField(block = true)
	public boolean isBCstage = false;

	@JsonClass.JCIdentifier
	public final Identifier<Stage> id;

	@JsonField(generic = MultiLangData.class, gen = JsonField.GenType.FILL, defval = "empty")
	public final MultiLangData names = new MultiLangData();

	public boolean non_con, trail, bossGuard;
	@JsonField(defval = "3000")
	public int len = 3000;
	@JsonField(defval = "60000")
	public int health = 60000;
	@JsonField(defval = "8")
	public int max = 8;
	public int mush, bgh;
	@JsonField(backCompat = JsonField.CompatType.FORK)
	public int timeLimit = 0;
	@JsonField(defval = "1")
	public int minUSpawn = 1, maxUSpawn = 1, minSpawn = 1, maxSpawn = 1;
	@JsonField
	public Identifier<CastleImg> castle;
	@JsonField
	public Identifier<Background> bg, bg1;
	@JsonField
	public Identifier<Music> mus0, mus1;
	@JsonField(defval = "empty")
	public SCDef data = new SCDef(0);
	@JsonField(defval = "none")
	public Limit lim = new Limit();
	@JsonField(block = true)
	public BattlePreset preset;
	@JsonField(generic = Replay.class, alias = ResourceLocation.class)
	public ArrayList<Replay> recd = new ArrayList<>();

	@JsonClass.JCConstructor
	public Stage() {
		id = null;
	}

	public Stage(Identifier<Stage> id) {
		this.id = id;
		names.put("stage " + getCont().list.size());
		lim.stageLimit = new StageLimit();
	}

	public Stage(StageMap sm) {
		this.id = sm.getNextID();
		names.put("stage " + sm.list.size());
	}

	protected Stage(Identifier<Stage> id, VFile f, int type) {
		this.id = id;
		isBCstage = true;
		StageMap sm = getCont();
		if (sm.info != null)
			sm.info.getData(this);
		Queue<String> qs = f.getData().readLine();
		names.put("" + id);
		String temp;
		boolean hasCastleData = false;
		if (type == 0) {
			temp = qs.poll();
			if(temp != null) {
				hasCastleData = true;
				String[] strs = temp.split(",");

				int cas = CommonStatic.parseIntN(strs[0]);
				if (cas == -1)
					cas = CH_CASTLES[id.id];
				if (sm.cast != -1)
					cas = sm.cast * 1000 + cas;
				castle = Identifier.parseInt(cas, CastleImg.class);
				non_con = strs[1].equals("1");

				if(info != null)
					((DefStageInfo)info).setData(strs);
			}
		} else {
			castle = Identifier.parseInt(sm.cast * 1000 + CH_CASTLES[id.id], CastleImg.class);
			non_con = false;
		}
		int intl = type == 2 ? 9 : 10;
		temp = qs.poll();

		if(temp != null) {
			String[] strs = temp.split(",");

			len = Integer.parseInt(strs[0]);
			health = Integer.parseInt(strs[1]);
			minSpawn = Integer.parseInt(strs[2]);
			maxSpawn = Integer.parseInt(strs[3]);
			bg = Identifier.rawParseInt(Integer.parseInt(strs[4]), Background.class);
			max = Math.min(50, Integer.parseInt(strs[5]));
			timeLimit = strs.length >= 8 ? Math.max(Integer.parseInt(strs[7]) * 60, 0) : 0;
			if(timeLimit != 0)
				health = Integer.MAX_VALUE;

			// Must be parsed only when it's for normal stages, not EoC/ItF/CotC
			if (hasCastleData)
				bossGuard = Integer.parseInt(strs[8]) == 1;
			trail = timeLimit != 0;

			int isBase = Integer.parseInt(strs[6]) - 2;

			List<int[]> ll = new ArrayList<>();

			while (!qs.isEmpty())
				if (!(temp = qs.poll()).isEmpty()) {
					if (!Character.isDigit(temp.charAt(0)))
						break;
					if (temp.startsWith("0,"))
						break;

					String[] ss = temp.split(",");

					for(int i = 0; i < ss.length; i++) {
						ss[i] = ss[i].trim();
					}

					int[] data = new int[SCDef.SIZE];

					for (int i = 0; i < intl; i++)
						if(i < ss.length)
							data[i] = Integer.parseInt(ss[i]);
						else if(i == SCDef.M) //Handle missing value manually
								data[i] = 100;

					data[SCDef.E] -= 2;
					data[SCDef.S0] *= 2;
					data[SCDef.R0] *= 2;
					data[SCDef.R1] *= 2;

					if (timeLimit == 0 && data[SCDef.C0] > 100) {
						if (intl > 9 && data[SCDef.M] == 100)
							data[SCDef.M] = data[SCDef.C0];
						data[SCDef.C0] = 100;
					}
					if (ss.length > 11 && CommonStatic.isInteger(ss[11])) {
						data[SCDef.M1] = Integer.parseInt(ss[11]);

						if(data[SCDef.M1] == 0)
							data[SCDef.M1] = data[SCDef.M];
					} else
						data[SCDef.M1] = data[SCDef.M];

					if(ss.length > 12 && CommonStatic.isInteger(ss[12]) && Integer.parseInt(ss[12]) == 1)
						data[SCDef.S0] *= -1;
					if(ss.length > 13 && CommonStatic.isInteger(ss[13]))
						data[SCDef.KC] = Integer.parseInt(ss[13]);

					if (data[0] == isBase)
						data[SCDef.C0] = 0;

					ll.add(data);
				}
			SCDef scd = new SCDef(ll.size());
			for (int i = 0; i < ll.size(); i++)
				scd.datas[i] = new Line(ll.get(scd.datas.length - i - 1));

			int ano = CommonStatic.parseIntN(strs[6]);
			if (ano == 317)
				scd.datas[ll.size() - 1].castle_0 = 0;

			data = scd;
		}

		validate();
	}

	public boolean contains(AbEnemy e) {
		return data.contains(e);
	}

	@Override
	public Stage copy(StageMap sm) {
		Stage ans = new Stage(sm);
		ans.len = len;
		ans.health = health;
		ans.max = max;
		if (bg != null)
			ans.bg = bg.clone();
		if (bg1 != null)
			ans.bg1 = bg1.clone();
		if (castle != null)
			ans.castle = castle.clone();
		ans.names.put(toString());
		ans.data = data.copy();
		ans.lim = lim != null ? lim.clone() : getLim(0);
		ans.non_con = non_con;
		if (mus0 != null)
			ans.mus0 = mus0.clone();
		if (mus1 != null)
			ans.mus1 = mus1.clone();
		ans.bgh = bgh;
		ans.mush = mush;
		if (info != null) {
			CustomStageInfo csi = new CustomStageInfo(ans);
			csi.stages.addAll(Arrays.asList(ans.info.getExStages()));
			if (!csi.stages.isEmpty()) {
				float[] chances = ans.info.getExChances();
				if (chances[0] == -1) {
					for (int i = 0; i < csi.stages.size(); i++)
						csi.chances.add(chances[1] / csi.stages.size());
					csi.checkChances();
				} else
					for (float chance : chances) {
						csi.chances.add(chance);
						csi.totalChance += (short)chance;
					}
			}
			if (info instanceof CustomStageInfo && ((CustomStageInfo)info).ubase != null) {
				csi.ubase = ((CustomStageInfo)info).ubase;
				csi.lv = ((CustomStageInfo)info).lv;
			}
			ans.info = csi;
		}
		ans.minSpawn = minSpawn;
		ans.maxSpawn = maxSpawn;
		ans.bossGuard = bossGuard;
		return ans;
	}

	@Override
	public Identifier<Stage> getID() {
		return id;
	}

	public Limit getLim(int star) {
		Limit tl = new Limit();
		if (lim != null && (lim.star == 0 || (lim.star & (1 << star)) != 0))
			tl.combine(lim);
		for (Limit l : getCont().lim)
			if ((l.star == 0 || (l.star & (1 << star)) != 0))
				tl.combine(l);
		return tl;
	}

	public int id() {
		return getCont().list.indexOf(this);
	}

	public Set<String> isSuitable(UserPack pack) {
		if(pack == null)
			return new TreeSet<>();
		Dependency dep = Dependency.collect(this);

		PackDesc desc = pack.desc;
		Set<String> set = dep.getPacks();
		set.remove(Identifier.DEF);
		set.remove(pack.getSID());
		for (String str : desc.dependency)
			set.remove(str);
		return set;
	}

	public void setNames(String str) {
		while (!checkName(str))
			str += "'";
		names.put(str);
	}

	@Override
	public String toString() {
		String desp = MultiLangCont.get(this);
		if (desp != null && !desp.isEmpty())
			return desp;
		String n = names.toString();
		if (!n.isEmpty())
			return n;
		return map + " - " + id();
	}

	public boolean isAkuStage() {
		if(data.datas.length == 0)
			return false;

		Line line = data.datas[data.datas.length - 1];

		if(line.enemy == null)
			return false;

		return line.enemy.id == 574 && line.enemy.pack.equals(Identifier.DEF) && line.castle_0 == 0;
	}

	protected void validate() {
		if(trail)
			return;

		if(getCont() == null || getMC() == null)
			return;

		if(getMC().getSID().equals("000006") || getMC().getSID().equals("000011"))
			trail = data.isTrail();
	}

	private boolean checkName(String str) {
		for (Stage st : getCont().list)
			if (st != this && st.names.toString().equals(str))
				return false;
		return true;
	}

	public MapColc getMC() {
		return getCont().getCont();
	}

	@Override
	public int compareTo(@NotNull Stage o) {
		return id.compareTo(o.id);
	}

	@JsonDecoder.OnInjected
	public void onInjected(JsonObject jobj) {
		if (jobj.has("name"))
			names.put(jobj.get("name").getAsString());
		recd.removeIf(Objects::isNull);
	}

	@JsonDecoder.PostLoad
	public void PostLoad() {
		MapColc.PackMapColc mc = (MapColc.PackMapColc)getMC();
		if (mc.pack.desc.FORK_VERSION < 12) {
			if (mc.pack.desc.FORK_VERSION < 11) {
				if (UserProfile.isOlderPack(mc.pack, "0.7.8.2") && lim.stageLimit != null)
					lim.stageLimit.coolStart = lim.stageLimit.globalCooldown > 0 || lim.stageLimit.maxMoney > 0;
				lim.setStar(lim.star); //All star will have to be 0 coz 1 << 0 is 1 though
				if (mc.pack.desc.FORK_VERSION < 5 && timeLimit > 0)
					timeLimit *= 60;
			}
			int basepos = 800;
			if (data.datas.length > 0 && data.getSimple(data.datas.length - 1).castle_0 == 0)
				basepos = data.getSimple(data.datas.length - 1).boss >= 1 ? (int)Math.ceil(Identifier.getOr(castle, CastleImg.class).boss_spawn) : 700;

			for (Line l : data.datas)
				if (l.doorchance > 0) {
					l.doordis_0 = (len - 500 - basepos) * l.doordis_0 / 100;
					l.doordis_1 = (len - 500 - basepos) * l.doordis_1 / 100;
				}
		}
	}

	@JsonField(tag = "timeLimit", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public int UTL() {
		if (timeLimit == 0)
			return 0;
		return Math.min(1, timeLimit / 60);
	}
}
