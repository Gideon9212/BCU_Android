package common.util.stage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.battle.LineUp;
import common.io.assets.Admin.StaticPermitted;
import common.io.json.JsonClass;
import common.io.json.JsonClass.JCIdentifier;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.io.json.localDecoder;
import common.pack.Identifier;
import common.pack.IndexContainer.IndexCont;
import common.pack.IndexContainer.Indexable;
import common.pack.PackData;
import common.pack.PackData.UserPack;
import common.util.Data;
import common.util.unit.AbForm;
import common.util.unit.Form;
import common.util.unit.Level;

import java.util.TreeMap;

@IndexCont(PackData.class)
@JsonClass(noTag = NoTag.LOAD)
@JsonClass.JCGeneric(Identifier.class)
public class LvRestrict extends Data implements Indexable<PackData, LvRestrict> {

	@StaticPermitted
	public static final Level MAX = new Level(50, 70, new int[0]);

	@JsonField(generic = { CharaGroup.class, Level.class }, alias = Identifier.class, backCompat = JsonField.CompatType.FORK, defval = "isEmpty")
	public final TreeMap<CharaGroup, Level> cgl = new TreeMap<>();
	@JsonField(backCompat = JsonField.CompatType.FORK)
	public Level[] rs = new Level[RARITY_TOT];
	@JsonField(backCompat = JsonField.CompatType.FORK)
	public Level def = new Level();

	@JCIdentifier
	public Identifier<LvRestrict> id;
	@JsonField(defval = "isEmpty")
	public String name = "";

	@JsonClass.JCConstructor
	public LvRestrict() {

	}

	public LvRestrict(Identifier<LvRestrict> ID) {
		def = MAX.clone();
		for (int i = 0; i < RARITY_TOT; i++)
			rs[i] = MAX.clone();
		id = ID;
	}

	public LvRestrict(Identifier<LvRestrict> ID, LvRestrict lvr) {
		id = ID;
		def = lvr.def.clone();
		for (int i = 0; i < RARITY_TOT; i++)
			rs[i] = lvr.rs[i].clone();
		for (CharaGroup cg : lvr.cgl.keySet())
			cgl.put(cg, lvr.cgl.get(cg).clone());
	}

	private LvRestrict(LvRestrict lvr) {
		for (CharaGroup cg : lvr.cgl.keySet())
			cgl.put(cg, lvr.cgl.get(cg).clone());
	}

	public LvRestrict combine(LvRestrict lvr) {
		LvRestrict ans = new LvRestrict(this);
		ans.def = combineLvs(lvr.def, def);
		for (int i = 0; i < RARITY_TOT; i++)
			ans.def = combineLvs(lvr.rs[i], rs[i]);

		for (CharaGroup cg : lvr.cgl.keySet())
			if (cgl.containsKey(cg)) {
				Level lv0 = cgl.get(cg);
				Level lv1 = lvr.cgl.get(cg);

				ans.cgl.put(cg, combineLvs(lv0, lv1));
			} else
				ans.cgl.put(cg, lvr.cgl.get(cg).clone());
		return ans;
	}

	public static Level combineLvs(Level src, Level dst) {
		int lv = Math.min(dst.getLv(), src.getLv());
		int plv = Math.min(dst.getPlusLv(), src.getPlusLv());

		int[] anp = dst.getTalents(), np = src.getTalents();
		int[] nps = new int[Math.max(anp.length, np.length)];
		for (int i = 0; i < nps.length; i++)
			if (i >= anp.length)
				nps[i] = np[i];
			else if (i >= np.length)
				nps[i] = anp[i];
			else
				nps[i] = Math.min(np[i], anp[i]);
		return new Level(lv, plv, nps);
	}

	@Override
	public Identifier<LvRestrict> getID() {
		return id;
	}

	public boolean isValid(LineUp lu) {
		for (AbForm[] fs : lu.fs)
			for (AbForm f : fs)
				if (f != null) {
					Level mlv = valid(f);
					Level flv = lu.map.get(f.getID());

					if (mlv.getLv() < flv.getLv() || mlv.getPlusLv() < flv.getPlusLv())
						return false;

					int[] mt = mlv.getTalents();
					int[] ft = flv.getTalents();

					for (int i = 0; i < Math.min(mt.length, ft.length); i++)
						if (mt[i] < ft[i])
							return false;
				}
		return true;
	}

	@Override
	public String toString() {
		return id + "-" + name;
	}

	public boolean used() {
		PackData p = getCont();

		if(p instanceof UserPack) {
			for (StageMap sm : ((UserPack) p).mc.maps)
				for (Stage st : sm.list)
					if (st.lim != null && st.lim.lvr == this)
						return true;
		} else
			return p instanceof PackData.DefPack;

		return false;
	}

	public Level valid(AbForm f) {
		Level lv = MAX.clone();
		boolean mod = false;
		for (CharaGroup cg : cgl.keySet())
			if (f instanceof Form && cg.fset.contains(f)) {
				lv = combineLvs(cgl.get(cg), lv);
				mod = true;
			}
		if (mod)
			return f.regulateLv(null, lv);
		if (f instanceof Form)
			lv = combineLvs(rs[((Form)f).unit.rarity], lv);
		lv = combineLvs(def, lv);
		return f.regulateLv(null, lv);
	}

	public void validate(LineUp lu) {
		for (AbForm[] fs : lu.fs)
			for (AbForm f : fs)
				if (f != null)
					lu.map.put(f.getID(), valid(f));
		lu.renew();
	}

	@JsonField(tag = "all", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public int[] oldAll() {
		return toOldFormat(def);
	}

	@JsonField(tag = "rares", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public int[][] oldRares() {
		int[][] rr = new int[RARITY_TOT][6];
		for (int i = 0; i < rr.length; i++)
			rr[i] = toOldFormat(rs[i]);
		return rr;
	}

	@JsonField(tag = "res", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public TreeMap<Identifier<CharaGroup>, int[]> oldRes() {
		TreeMap<Identifier<CharaGroup>, int[]> ors = new TreeMap<>();
		for (CharaGroup cg : cgl.keySet())
			ors.put(cg.getID(), toOldFormat(cgl.get(cg)));
		return ors;
	}

	private static int[] toOldFormat(Level lv) {
		int[] arrs = new int[]{lv.getLv(), lv.getPlusLv(), 10, 10, 10, 10, 10};
		for (int i = 2; i < Math.min(arrs.length, lv.getTalents().length); i++)
			arrs[i] = lv.getTalents()[i];
		return arrs;
	}

	private static Level toNewFormat(int[] lvs) {
		Level lv = new Level(lvs[0], lvs[1], new int[0]);
		int[] nps = new int[lvs.length - 2];
		System.arraycopy(lvs, 2, nps, 0, nps.length);
		lv.setTalents(nps);
		return lv;
	}

	@JsonDecoder.OnInjected
	public void onInjected(JsonObject jobj) {
		if (!(getCont() instanceof UserPack))
			return;
		UserPack pack = (UserPack)getCont();
		if (pack.desc.FORK_VERSION < 10) {
			def = toNewFormat(JsonDecoder.decode(jobj.get("all"), int[].class));
			int[][] oldRares = JsonDecoder.decode(jobj.get("rares"), int[][].class);
			for (int i = 0; i < RARITY_TOT; i++)
				rs[i] = toNewFormat(oldRares[i]);

			JsonArray jarr = jobj.getAsJsonArray("res");
			int n = jarr.size();
			for (int i = 0; i < n; i++) {
				JsonObject job = jarr.get(i).getAsJsonObject();
				CharaGroup ch = new localDecoder(job.get("key"), CharaGroup.class, this).setAlias(Identifier.class).decode();
				cgl.put(ch, toNewFormat(JsonDecoder.decode(job.get("val"), int[].class)));
			}
		}
	}
}
