package common.util.unit;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.battle.data.*;
import common.io.json.JsonClass;
import common.io.json.JsonClass.JCConstructor;
import common.io.json.JsonClass.JCGeneric;
import common.io.json.JsonClass.RType;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.PackData;
import common.pack.UserProfile;
import common.system.BasedCopable;
import common.system.VImg;
import common.util.Data;
import common.util.anim.AnimU;
import common.util.anim.AnimUD;
import common.util.anim.MaModel;
import common.util.lang.MultiLangCont;

import javax.annotation.Nullable;
import java.util.Arrays;

@JCGeneric(AbForm.AbFormJson.class)
@JsonClass(read = RType.FILL)
public class Form extends Character implements BasedCopable<AbForm, AbUnit>, AbForm, Comparable<AbForm> {

	@JsonField
	public final MaskUnit du;
	public final Unit unit;
	public final Identifier<AbUnit> uid;
	@JsonField
	public int fid;
	public Orb orbs = null;

	@JCConstructor
	public Form(Unit u) {
		du = null;
		unit = u;
		uid = unit.id;
		orbs = new Orb(-1);
	}
	//Used solely to make placeholders
	public Form(Unit u, MaskUnit d) {
		du = d;
		unit = u;
		uid = unit.id;
		orbs = new Orb(0);
		anim = new AnimUD("./org/unit/000/f/", "000_f", "edi000_f.png", "uni000_f00.png");
		anim.getUni().setCut(CommonStatic.getBCAssets().unicut);
	}

	public Form(Unit u, int f, String str, AnimU<?> ac, CustomUnit cu) {
		unit = u;
		uid = u.id;
		fid = f;
		names.put(str);
		anim = ac;
		du = cu;
		cu.pack = this;
		orbs = new Orb(-1);
	}

	//Used for BC units
	protected Form(Unit u, int f, String str, String data) {
		unit = u;
		uid = u.id;
		fid = f;
		String nam = trio(uid.id) + "_" + SUFX[fid];
		anim = new AnimUD(str, nam, "edi" + nam + ".png", "uni" + nam + "00.png");
		anim.getUni().setCut(CommonStatic.getBCAssets().unicut);
		String[] strs = data.split("//")[0].trim().split(",");
		du = new DataUnit(this, strs);
		MaModel model = anim.loader.getMM();
		((DataUnit) du).limit = CommonStatic.dataFormMinPos(model);
	}
	//Used for BC eggs
	protected Form(Unit u, int f, int m, String str, String data) {
		unit = u;
		uid = u.id;
		fid = f;
		String nam = trio(m) + "_m";
		anim = new AnimUD(str, nam, "edi" + nam + duo(fid) + ".png", "uni" + nam + duo(fid) + ".png");
		anim.getUni().setCut(CommonStatic.getBCAssets().unicut);
		String[] strs = data.split("//")[0].trim().split(",");
		du = new DataUnit(this, strs);
		MaModel model = anim.loader.getMM();
		((DataUnit) du).limit = CommonStatic.dataFormMinPos(model);
	}

	@Override
	public Form copy(AbUnit b) {
		CustomUnit cu = new CustomUnit(anim);
		cu.importData(du);
		return new Form((Unit) b, fid, names.toString(), anim, cu);
	}

	@Override
	public Identifier<AbUnit> getID() {
		return uid;
	}

	@Override
	public Unit unit() {
		return unit;
	}

	@Override
	public int getFid() {
		return fid;
	}

	public MaskUnit maxu() {
		PCoin pc = du.getPCoin();
		if (pc != null)
			return pc.full;
		return du;
	}

	@Override
	public VImg getDeployIcon() {
		return anim.getUni();
	}

	public MaskUnit getMask() {
		return du;
	}

	public boolean unused() {
		PackData.UserPack pack = (PackData.UserPack) unit.getCont();
		for (Combo c : pack.combos)
			for (Form f : c.forms) {
				if (f == null)
					break;
				if (f == this)
					return false;
			}
		for (Unit u : pack.units)
			for (Form f : u.forms) {
				if (f == this)
					continue;
				if (uid.equals(f.du.getProc().SUMMON.id) && fid == f.du.getProc().SUMMON.form - 1)
					return false;
				if (uid.equals(f.du.getProc().SPIRIT.id) && fid == f.du.getProc().SPIRIT.form - 1)
					return false;
				if (recursiveBlessUsed(f.du.getProc().BLESSING))
					return false;
			}
		for (Enemy e : pack.enemies) {//Just in case since enemies can summon units
			if (uid.equals(e.de.getProc().SUMMON.id) && fid == e.de.getProc().SUMMON.form - 1)
				return false;
			if (uid.equals(e.de.getProc().SPIRIT.id) && fid == e.de.getProc().SPIRIT.form - 1)
				return false;
			if (recursiveBlessUsed(e.de.getProc().BLESSING))
				return false;
		}
		for (UniRand ru : pack.randUnits)
			for (Form f : ru.getForms())
				if (f == this)
					return false;
		return true;
	}

	private boolean recursiveBlessUsed(Proc.BLESSING bless) {
		if (bless.procs == null)
			return false;
		if (uid.equals(bless.procs.SUMMON.id) && fid == bless.procs.SUMMON.form - 1)
			return true;
		if (uid.equals(bless.procs.SPIRIT.id) && fid == bless.procs.SPIRIT.form - 1)
			return true;
		return recursiveBlessUsed(bless.procs.BLESSING);
	}

	@OnInjected
	public void onInjected(JsonObject jobj) {
		CustomUnit form = (CustomUnit)du;
		form.pack = this;
		checkPackVersion(jobj, form);
		if (form.getPCoin() != null) {
			form.pcoin.verify();
			form.pcoin.update();
		}
	}
	private void checkPackVersion(JsonObject jobj, CustomUnit form) {
		if (unit == null && uid == null)
			return;
		Unit u = unit == null ? (Unit) uid.get() : unit;
		PackData.UserPack pack = (PackData.UserPack) u.getCont();
		if (pack.desc.FORK_VERSION >= 13)
			return;
		inject(pack, jobj.getAsJsonObject("du"), form);
		if (pack.desc.FORK_VERSION < 1) {
			AtkDataModel[] atks = form.getAllAtkModels();
			for (AtkDataModel atk : atks)
				if (atk.getProc().SUMMON.prob > 0) {
					if (atk.getProc().SUMMON.form <= 0) {
						atk.getProc().SUMMON.form = 1;
						atk.getProc().SUMMON.mult = 1;
						atk.getProc().SUMMON.fix_buff = true;
					} else if (atk.getProc().SUMMON.id != null && !Unit.class.isAssignableFrom(atk.getProc().SUMMON.id.cls))
						atk.getProc().SUMMON.fix_buff = true;
				}
			if (form.getPCoin() != null)
				for (int[] dat : form.pcoin.info)
					if (dat[dat.length - 1] == 1)
						dat[13] = 60;
			if (form.getProc().SPIRIT.id != null) {
				form.getProc().SPIRIT.animType = Proc.SUMMON_ANIM.ATTACK;
				form.getProc().SPIRIT.inv = true;
			}
		} //Finish FORK_VERSION 1 checks
		if (pack.desc.FORK_VERSION < 7)
			if (form.getPCoin() != null) {
				form.pcoin.info.replaceAll(data -> {
					int[] corres = Data.get_CORRES(data[0]);
					int[] trueArr;
					switch (corres[0]) {
						case Data.PC_P:
							trueArr = Arrays.copyOf(data, 3 + (form.getProc().getArr(corres[1]).getDeclaredFields().length - (corres.length >= 3 ? corres[2] : 0)) * 2);
							if (corres[1] == Data.P_BLAST) {
								trueArr[8] = trueArr[9] = 3;
								trueArr[10] = trueArr[11] = 30;
							}
							break;
						case Data.PC_BASE:
							trueArr = Arrays.copyOf(data, 5);
							break;
						default:
							trueArr = Arrays.copyOf(data, 3);
					}
					for (int i = 10; i < trueArr.length - 1; i++)
						trueArr[i] = 0;//Just in case so mainBCU talents don't get buggy
					if (data.length == 14)
						trueArr[trueArr.length - 1] = Math.max(0, data[13]); //super talent lv
					return trueArr;
				});
			}//Finish FORK_VERSION 7 checks
		//if (pack.desc.FORK_VERSION >= 13)
		//	return;//for future ver increases
		if (form.getPCoin() != null)
			for (int[] dat : form.pcoin.info) {
				if (dat[0] == 62) {//miniwave
					dat[6] -= 20;
					dat[7] -= 20;
				} else if (dat[0] == 65) {//minisurge
					dat[10] -= 20;
					dat[11] -= 20;
				}
			}
		if (!UserProfile.isOlderPack(pack, "0.6.4.0"))
			return;
		names.put(jobj.get("name").getAsString());
		if (jobj.has("explanation"))
			description.put(jobj.get("explanation").getAsString().replace("<br>", "\n"));
		if (UserProfile.isOlderPack(pack, "0.6.0.0"))
			form.limit = CommonStatic.customFormMinPos(anim.loader.getMM());
	}

	/**
	 * Validate level values in {@code target} {@link common.util.unit.Level}
	 * @param src {@code Level} that will be put into {@code target} {@code Level}. Can be null
	 * @param target {@code Level} that will be validated
	 * @return Validated {@code target} {@code Level} will be returned
	 */
	@Override
	public Level regulateLv(@Nullable Level src, Level target) {
		if(src != null) {
			target.setLevel(Math.max(1, Math.min(src.getLv(), unit.max)));
			target.setPlusLevel(Math.max(0, Math.min(src.getPlusLv(), unit.maxp)));

			PCoin pc = du.getPCoin();
			if (pc != null) {
				int[] maxTalents = new int[pc.info.size()];

				for (int i = 0; i < pc.info.size(); i++)
					maxTalents[i] = Math.max(1, pc.info.get(i)[1]);

				int[] t = new int[maxTalents.length];
				for (int i = 0; i < Math.min(maxTalents.length, src.getTalents().length); i++)
					t[i] = Math.min(maxTalents[i], Math.max(0, src.getTalents()[i]));

				if (src.getTalents().length < target.getTalents().length)
					for (int i = src.getTalents().length; i < Math.min(maxTalents.length, target.getTalents().length); i++)
						t[i] = Math.min(maxTalents[i], Math.max(0, target.getTalents()[i]));
				target.setTalents(t);
			}
		} else {
			target.setLevel(Math.max(1, Math.min(unit.max, target.getLv())));
			target.setPlusLevel(Math.max(0, Math.min(unit.maxp, target.getPlusLv())));

			PCoin pc = du.getPCoin();

			if (pc != null) {
				int[] maxTalents = new int[pc.info.size()];
				int[] t = new int[pc.info.size()];

				for (int i = 0; i < pc.info.size(); i++)
					maxTalents[i] = Math.max(1, pc.info.get(i)[1]);

				for (int i = 0; i < Math.min(maxTalents.length, target.getTalents().length); i++) {
					t[i] = Math.min(maxTalents[i], Math.max(0, target.getTalents()[i]));
				}

				target.setTalents(t);
			}
		}
		return target;
	}

	@Override
	public PackData getPack() {
		return unit.getCont();
	}

	@Override
	public String toString() {
		String base = (uid == null ? "NULL" : uid.id) + "-" + fid + " ";
		if (CommonStatic.getFaves().units.contains(this))
			base = "❤" + base;
		String desp = MultiLangCont.get(this);
		if (desp != null && !desp.isEmpty())
			return base + desp;

		String nam = names.toString();
		if (!nam.isEmpty())
			return base + nam;
		return base;
	}

	public String getExplanation() {
		String[] desp = MultiLangCont.getDesc(this);
		if (desp != null && !desp[0].isEmpty())
			return String.join("\n", desp);
		return description.toString();
	}

	public int compareTo(AbForm u) {
		int i = getID().compareTo(u.getID());
		if (i == 0)
			return Integer.compare(fid, u.getFid());
		return i;
	}

	public boolean hasEvolveCost() {
		return unit.info.hasEvolveCost() && fid == 2;
	}
	public boolean hasZeroForm() {
		return unit.info.hasZeroForm() && fid == 3;
	}

	public int getEvoCost() {
		if (hasEvolveCost())
			return unit.info.xp;
		else if (hasZeroForm())
			return unit.info.zeroXp;
		return -1;
	}
	public int[][] getEvoMaterials() {
		if (hasEvolveCost())
			return unit.info.evo;
		else if (hasZeroForm())
			return unit.info.zeroEvo;
		return null;
	}
}