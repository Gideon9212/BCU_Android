package common.util.unit;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.data.AtkDataModel;
import common.battle.data.CustomEnemy;
import common.battle.data.DataEnemy;
import common.battle.data.MaskEnemy;
import common.battle.entity.EEnemy;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.PackData;
import common.pack.UserProfile;
import common.system.files.VFile;
import common.util.Data;
import common.util.anim.AnimU;
import common.util.anim.AnimUD;
import common.util.anim.EAnimU;
import common.util.anim.MaModel;
import common.util.lang.MultiLangCont;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

import java.util.*;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class Enemy extends Character implements AbEnemy {

	public static void regType() { //Dictates if enemy is collab, special, or reocurring
		for (Enemy e : UserProfile.getBCData().enemies) {
			Map<MapColc.DefMapColc, Integer> lis = e.findMap();
			final int recurring = lis.getOrDefault(MapColc.DefMapColc.getMap("N"), 0) + lis.getOrDefault(MapColc.DefMapColc.getMap("A"), 0)
					+ lis.getOrDefault(MapColc.DefMapColc.getMap("Q"), 0) + lis.getOrDefault(MapColc.DefMapColc.getMap("ND"), 0);
			if (recurring == 0 && (lis.containsKey(MapColc.DefMapColc.getMap("C")) || lis.containsKey(MapColc.DefMapColc.getMap("R")) || lis.containsKey(MapColc.DefMapColc.getMap("CH"))
					|| lis.containsKey(MapColc.DefMapColc.getMap("CA"))))
				e.filter = 2;
			else if (recurring <= 3)
				e.filter = 0;
		}
	}

	@JsonClass.JCIdentifier
	@JsonField
	public final Identifier<AbEnemy> id;
	@JsonField
	public final MaskEnemy de;
	@JsonField(defval = "1")
	public byte filter = 1; //Filter Type, used solely for filter pages: 0 is non-reocurring, 1 is reocurring, 2 is collab, 3 is hidden

	@JsonClass.JCConstructor
	public Enemy() {
		id = null;
		de = null;
	}

	public Enemy(Identifier<AbEnemy> hash, AnimU<?> ac, CustomEnemy ce) {
		id = hash;
		de = ce;
		ce.pack = this;
		anim = ac;
	}

	public Enemy(VFile f, int[] ints) {
		id = new Identifier<>(Identifier.DEF, Enemy.class, CommonStatic.parseIntN(f.getName()));
		String str = "./org/enemy/" + Data.trio(id.id) + "/";
		de = new DataEnemy(this, ints);
		anim = new AnimUD(str, Data.trio(id.id) + "_e", "edi_" + Data.trio(id.id) + ".png", null);
		MaModel model = anim.loader.getMM();
		((DataEnemy) de).limit = CommonStatic.dataEnemyMinPos(model);
	}

	public List<Stage> findApp() {
		List<Stage> ans = new ArrayList<>();
		for (Stage st : MapColc.getAllStage()) {
			if (st != null && (st.getMC().getSave(false) == null || st.getMC().getSave(true).unlocked(st))
					&& st.contains(this))
				ans.add(st);
			}
		return ans;
	}

	public List<Stage> findApp(MapColc mc) {
		List<Stage> ans = new ArrayList<>();
		for (StageMap sm : mc.maps)
			for (Stage st : sm.list)
				if (st.contains(this))
					ans.add(st);
		return ans;
	}

	public Map<MapColc.DefMapColc, Integer> findMap() {
		Map<MapColc.DefMapColc, Integer> ans = new HashMap<>();
		for (MapColc mc : MapColc.values()) {
			if (!(mc instanceof MapColc.DefMapColc))
				continue;
			for (StageMap sm : mc.maps)
				for (Stage st : sm.list)
					if (st.contains(this)) {
						if (ans.containsKey(mc))
							ans.replace((MapColc.DefMapColc)mc, ans.get(mc) + 1);
						else
							ans.put((MapColc.DefMapColc)mc, 1);
					}
		}
		return ans;
	}

	@Override
	public EEnemy getEntity(StageBasis b, Object obj, float hpMagnif, float atkMagnif, int d0, int d1, int m) {
		hpMagnif *= de.multi(b.b);
		atkMagnif *= de.multi(b.b);
		EAnimU anim = getEntryAnim();
		return new EEnemy(b, de, anim, hpMagnif, atkMagnif, d0, d1, m);
	}

	public EAnimU getEntryAnim() {
		EAnimU anim = getEAnim(AnimU.TYPEDEF[AnimU.ENTRY]);
		if (anim.unusable())
			anim = getEAnim(AnimU.TYPEDEF[AnimU.WALK]);

		anim.setTime(0);
		return anim;
	}

	@Override
	public Identifier<AbEnemy> getID() {
		return id;
	}

	@Override
	public Set<Enemy> getPossible() {
		Set<Enemy> te = new TreeSet<>();
		te.add(this);
		return te;
	}

	public MaskEnemy getMask() {
		return de;
	}

	@OnInjected
	public void onInjected(JsonObject jobj) {
		CustomEnemy enemy = (CustomEnemy) de;
		enemy.pack = this;

		PackData.UserPack pack = (PackData.UserPack) getCont();
		//Updates stuff to match this fork without core version issues
		if (pack.desc.FORK_VERSION >= 12)
			return;
		inject(pack, jobj.getAsJsonObject("de"), enemy);
		if (pack.desc.FORK_VERSION >= 1)
			return;
		AtkDataModel[] atks = enemy.getAllAtkModels();
		for (AtkDataModel ma : atks)
			if (ma.getProc().SUMMON.prob > 0) {
				if (ma.getProc().SUMMON.id != null && !AbEnemy.class.isAssignableFrom(ma.getProc().SUMMON.id.cls))
					ma.getProc().SUMMON.fix_buff = true;
				if (ma.getProc().SUMMON.id == null || !AbEnemy.class.isAssignableFrom(ma.getProc().SUMMON.id.cls))
					ma.getProc().SUMMON.form = 1; //There for imports
			}
		if (!UserProfile.isOlderPack(pack, "0.6.4.0"))
			return;
		if (jobj.has("desc"))
			description.put(jobj.get("desc").getAsString().replace("<br>", "\n"));
		if (!UserProfile.isOlderPack(pack, "0.6.1.0"))
			return;
		Proc proc = enemy.getProc();
		proc.DMGCUT.traitIgnore = true;
		proc.DMGCAP.traitIgnore = true;
		if (UserProfile.isOlderPack(pack, "0.5.4.0"))
			enemy.limit = CommonStatic.customEnemyMinPos(anim.loader.getMM());
	}

	@JsonDecoder.PostLoad
	public void postLoad() {
		PackData.UserPack pack = (PackData.UserPack) getCont();
		if (pack.desc.FORK_VERSION < 11 && findApp(pack.mc).size() <= 3)
			filter = 0;
	}

	@Override
	public PackData getPack() {
		return getCont();
	}

	@Override
	public String toString() {
		String base = Data.trio(id.id);
		if (CommonStatic.getFaves().enemies.contains(this))
			base = "❤" + base;
		String desp = MultiLangCont.get(this);
		if (desp != null && !desp.isEmpty())
			return base + " - " + desp;

		String nam = names.toString();
		if (nam.isEmpty())
			return base;
		return base + " - " + nam;
	}

	public String getExplanation() {
		String[] desp = MultiLangCont.getDesc(this);
		if (desp != null && !desp[0].isEmpty())
			return String.join("\n", desp);
		return description.toString();
	}
}