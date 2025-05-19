package common.battle.data;

import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.io.json.JsonField.CompatType;
import common.io.json.JsonField.GenType;
import common.pack.PackData.UserPack;
import common.pack.SortedPackSet;
import common.util.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonClass(noTag = NoTag.LOAD)
public abstract class CustomEntity extends DataEntity {

	@JsonField(gen = GenType.GEN, defval = "proc isBlank")
	public AtkDataModel rep;//TODO - Replace this with a Proc variable, given that is the only thing that gets used about it
	@JsonField(gen = GenType.GEN)
	public AtkDataModel cntr;
	@JsonField(gen = GenType.GEN, usePool = true, backCompat = CompatType.FORK, defval = "isEmpty")
	public AtkDataModel[] revs = new AtkDataModel[0], ress = new AtkDataModel[0], burs = new AtkDataModel[0],
			resus = new AtkDataModel[0], revis = new AtkDataModel[0], entrs = new AtkDataModel[0];

	@JsonField(generic = AtkDataModel[].class, gen = GenType.GEN, backCompat = CompatType.FORK)
	public ArrayList<AtkDataModel[]> hits = new ArrayList<>();
	@JsonField(gen = GenType.GEN)
	public int[] share;

	public int base;
	@JsonField(defval = "1")
	public int touch = TCH_N;
	@JsonField(defval = "true")
	public boolean common = true, kbBounce = true, bossBounce = true;

	public CustomEntity() {
		rep = new AtkDataModel(this);
		hits.add(new AtkDataModel[1]);
		hits.get(0)[0] = new AtkDataModel(this);
		share = new int[]{1};
	}

	/**
	 * This field is used to filter all the procs of units if common is false,
	 * Also used for counter
	 */
	@JsonField(block = true)
	private Proc all;

	@Override
	public int allAtk(int atk) {
		int ans = 0, temp = 0, c = 1;
		if (share[atk] == 0)
			return 0;
		for (AtkDataModel adm : hits.get(atk))
			if (adm.pre > 0 || adm.str.toLowerCase().startsWith("combo")) {
				ans += temp / c;
				temp = adm.getDire() > 0 ? adm.atk : 0;
				c = 1;
			} else {
				temp += adm.getDire() > 0 ? adm.atk : 0;
				c++;
			}
		ans += temp / c;
		return ans;
	}

	/**
	 * Updates the procs in all and initializes if it is null
	 */
	public void updateAllProc() {
		all = Proc.blank();
		for (int i = 0; i < Data.PROC_TOT; i++) {
			if (Data.procSharable[i]) {
				all.getArr(i).set(getProc().getArr(i));
			} else
				for (AtkDataModel[] adms : hits) {
					if (adms == null)
						continue;
					for (AtkDataModel adm : adms)
						if (!all.getArr(i).exists())
							all.getArr(i).set(adm.proc.getArr(i));
				}
		}
	}

	/**
	 * Gets all procs for units without common proc
	 */
	@Override
	public Proc getAllProc() {
		if (common)
			return getProc();
		if (all == null)
			updateAllProc();
		return all;
	}

	@Override
	public int getAtkCount(int atk) {
		if (share[atk] == 0)
			return 0;
		return hits.get(atk).length;
	}

	@Override
	public int firstAtk() {
		int tot = 0;
		for (int j : share)
			if (j == 0)
				tot++;
			else
				break;
		return tot;
	}

	@Override
	public int realAtkCount() {
		int fir = firstAtk();
		for (int i = share.length - 1; i > fir; i--)
			if (share[i] > 0)
				return i - fir + 1;
		return 1;
	}

	@Override
	public MaskAtk getAtkModel(int atk, int ind) {
		if (atk >= hits.size() || ind >= hits.get(atk).length)
			return getSpAtks(false, atk - hits.size())[ind];
		return hits.get(atk)[ind];
	}

	@Override
	public MaskAtk[] getAtks(int atk) {
		return hits.get(atk);
	}

	@Override
	public int getAtkTypeCount() {
		return hits.size();
	}

	@Override
	public AtkDataModel[][] getAllAtks() {
		return hits.toArray(new AtkDataModel[0][0]);
	}

	public AtkDataModel[] getAllAtkModels() { //Used only on OnInjected for non-shareable procs
		if (common)//No point in doing this if common proc is active
			return new AtkDataModel[]{rep};
		AtkDataModel[] allAtks = new AtkDataModel[hits.get(0).length + 1];
		allAtks[0] = rep;
		System.arraycopy(hits.get(0), 0, allAtks, 1, hits.get(0).length);
		for (int i = 1; i < hits.size(); i++) {
			allAtks = Arrays.copyOf(allAtks, allAtks.length + hits.get(i).length);
			System.arraycopy(hits.get(i), 0, allAtks, allAtks.length - hits.get(i).length, hits.get(i).length);
		}
		AtkDataModel[][] sps = getSpAtks(true);
		for (AtkDataModel[] sp : sps) {
			if (sp.length == 0)
				continue;
			allAtks = Arrays.copyOf(allAtks, allAtks.length + sp.length);
			System.arraycopy(sp, 0, allAtks, allAtks.length - sp.length, sp.length);
		}
		return allAtks;
	}

	@Override
	public AtkDataModel[][] getSpAtks(boolean addCounter) {
		if (addCounter && cntr != null)
			return new AtkDataModel[][]{revs, ress, new AtkDataModel[]{cntr}, burs, resus, revis, entrs};
		return new AtkDataModel[][]{revs, ress, burs, resus, revis, entrs};
	}

	@Override
	public AtkDataModel[] getSpAtks(boolean counter, int ind) {
		return getSpAtks(counter)[ind];
	}

	@Override
	public int getShare(int atk) {
		return share[atk];
	}

	@Override
	public int getItv(int atk) {
		if (share[atk] == 0)
			return getTBA() - 1;
		int longPre = 0;
		for (AtkDataModel adm : hits.get(atk))
			longPre += adm.pre;
		return longPre + Math.max(getTBA() - 1, getPost(false, atk));
	}

	@Override
	public int getPost(boolean sp, int atk) {
		if (sp) {
			AtkDataModel[] spa = getSpAtks(true, atk);
			return spa[spa.length - 1].pre;
		}
		int ans = getAnimLen(atk);
		for (AtkDataModel adm : hits.get(atk))
			ans -= adm.pre;
		return ans;
	}

	@Override
	public Proc getProc() {
		return rep.getProc();
	}

	@Override
	public MaskAtk getRepAtk() {
		return rep;
	}

	@Override
	public AtkDataModel[] getRevenge() {
		return revs;
	}

	@Override
	public AtkDataModel[] getResurrection() {
		return ress;
	}

	@Override
	public AtkDataModel getCounter() { return cntr; }

	@Override
	public AtkDataModel[] getGouge() {
		return burs;
	}

	@Override
	public AtkDataModel[] getResurface() {
		return resus;
	}

	@Override
	public AtkDataModel[] getRevive() {
		return revis;
	}

	@Override
	public AtkDataModel[] getEntry() {
		return entrs;
	}

	@Override
	public int getTBA() {
		return Math.abs(tba);
	}

	@Override
	public int getTouch() {
		return touch;
	}

	public void importData(MaskEntity de) {
		hp = de.getHp();
		hb = de.getHb();
		speed = de.getSpeed();
		range = de.getRange();
		abi = de.getAbi();
		loop = de.getAtkLoop();
		traits = new SortedPackSet<>();
		traits.addIf(de.getTraits(), t -> t.BCTrait() || t.id.pack.equals(getPack().getID().pack) || ((UserPack)getPack().getPack()).desc.dependency.contains(t.id.pack));
		width = de.getWidth();
		tba = de.getTBA();
		touch = de.getTouch();
		death = de.getDeathAnim();
		will = de.getWill();
		common = de.isCommon();
		cloneAttacks();
		if (de instanceof CustomEntity) {
			importData$1((CustomEntity) de);
			return;
		}

		base = de.touchBase();
		rep = new AtkDataModel(this);
		rep.proc = de.getProc().clone();
		int m = de.getAtkCount(0);
		hits.set(0, new AtkDataModel[m]);
		for (int i = 0; i < m; i++) {
			hits.get(0)[i] = new AtkDataModel(this, de, i);
			for (int j : BCShareable)
				hits.get(0)[i].proc.getArr(j).set(de.getProc().getArr(j));
		}
	}

	private void cloneAttacks() {
		ArrayList<AtkDataModel[]> hits2 = new ArrayList<>(hits.size());
		for (AtkDataModel[] hit : hits)
			hits2.add(hit.clone());
		hits = hits2;
	}

	@Override
	public boolean isLD() {
		boolean ans = false;
		for (AtkDataModel adm : hits.get(firstAtk()))
			ans |= adm.isLD();
		for (AtkDataModel[] adms : getSpAtks(true))
			for (AtkDataModel adm : adms)
				ans |= adm.isLD();

		return ans;
	}

	@Override
	public boolean isOmni() {
		boolean ans = false;
		for (AtkDataModel adm : hits.get(firstAtk()))
			ans |= adm.isOmni();
		for (AtkDataModel[] adms : getSpAtks(true))
			for (AtkDataModel adm : adms)
				ans |= adm.isOmni();

		return ans;
	}

	@Override
	public boolean isRange(int atk) {
		for (AtkDataModel adm : hits.get(atk))
			if (adm.range)
				return true;
		return false;
	}

	@Override
	public int touchBase() {
		return base == 0 ? range : base;
	}

	@Override
	public boolean isCommon() {
		return common;
	}

	private void importData$1(CustomEntity ce) {
		base = ce.base;
		rep = new AtkDataModel(this, ce.rep);

		for (int j = 0; j < Math.min(hits.size(), ce.hits.size()); j++) {
			if (ce.share[j] == 0) {
				share[j] = 0;
				hits.set(j, null);
				continue;
			}
			int[] inds = new int[ce.hits.get(j).length];
			List<AtkDataModel> temp = new ArrayList<>(inds.length);
			List<AtkDataModel> tnew = new ArrayList<>(inds.length);
			for (int i = 0; i < inds.length; i++) {
				if (!temp.contains(ce.hits.get(j)[i])) {
					temp.add(ce.hits.get(j)[i]);
					tnew.add(new AtkDataModel(this, ce.hits.get(j)[i]));
				}
				inds[i] = temp.indexOf(ce.hits.get(j)[i]);
			}
			hits.set(j, new AtkDataModel[ce.hits.get(j).length]);
			for (int i = 0; i < hits.get(j).length; i++)
				hits.get(j)[i] = tnew.get(inds[i]);
		}

		revs = new AtkDataModel[ce.revs.length];
		for (int i = 0; i < revs.length; i++) {
			revs[i] = new AtkDataModel(this, ce.revs[i]);
			revs[i].str = "revenge" + (i > 0 ? i + 1 : "");
		}
		ress = new AtkDataModel[ce.ress.length];
		for (int i = 0; i < ress.length; i++) {
			ress[i] = new AtkDataModel(this, ce.ress[i]);
			ress[i].str = "resurrection" + (i > 0 ? i + 1 : "");
		}
		if (ce.cntr != null) {
			cntr = new AtkDataModel(this, ce.cntr);
			cntr.str = "counterattack";
		}
		burs = new AtkDataModel[ce.burs.length];
		for (int i = 0; i < burs.length; i++) {
			burs[i] = new AtkDataModel(this, ce.burs[i]);
			burs[i].str = "burrow" + (i > 0 ? i + 1 : "");
		}
		resus = new AtkDataModel[ce.resus.length];
		for (int i = 0; i < resus.length; i++) {
			resus[i] = new AtkDataModel(this, ce.resus[i]);
			resus[i].str = "resurface" + (i > 0 ? i + 1 : "");
		}
		revis = new AtkDataModel[ce.revis.length];
		for (int i = 0; i < revis.length; i++) {
			revis[i] = new AtkDataModel(this, ce.revis[i]);
			revis[i].str = "revive" + (i > 0 ? i + 1 : "");
		}
		entrs = new AtkDataModel[ce.entrs.length];
		for (int i = 0; i < entrs.length; i++) {
			entrs[i] = new AtkDataModel(this, ce.entrs[i]);
			entrs[i].str = "entrance" + (i > 0 ? i + 1 : "");
		}
	}

	public void animChanged(int del) {
		if (del == -1) {
			hits.add(new AtkDataModel[1]);
			hits.get(hits.size() - 1)[0] = new AtkDataModel(this);
			share = Arrays.copyOf(share, hits.size());
			share[hits.size() - 1] = 1;
		} else {
			hits.remove(del);
			int[] newShare = new int[hits.size()];
			for (int i = 0; i < newShare.length; i++) {
				newShare[i] = share[i < del ? i : i + 1];
			}
			share = newShare;
		}
	}

	@JsonDecoder.OnInjected
	public void onInjected() {
		for (int i = 0; i < traits.size(); i++)
			if (traits.get(i) == null) {
				traits.remove(i);
				i--;
			}
	}

	@JsonField(tag = "atks", io = JsonField.IOType.W, gen = GenType.GEN, usePool = true, backCompat = CompatType.UPST)
	public AtkDataModel[] getUAtk() {
		return hits.get(firstAtk());
	}

	@JsonField(tag = "rev", io = JsonField.IOType.W, gen = GenType.GEN, backCompat = CompatType.UPST)
	public AtkDataModel getURev() {
		return revs.length == 0 ? null : revs[0];
	}

	@JsonField(tag = "res", io = JsonField.IOType.W, gen = GenType.GEN, backCompat = CompatType.UPST)
	public AtkDataModel getURes() {
		return ress.length == 0 ? null : ress[0];
	}

	@JsonField(tag = "bur", io = JsonField.IOType.W, gen = GenType.GEN, backCompat = CompatType.UPST)
	public AtkDataModel getUBur() {
		return burs.length == 0 ? null : burs[0];
	}

	@JsonField(tag = "resu", io = JsonField.IOType.W, gen = GenType.GEN, backCompat = CompatType.UPST)
	public AtkDataModel getUResu() {
		return resus.length == 0 ? null : resus[0];
	}

	@JsonField(tag = "revi", io = JsonField.IOType.W, backCompat = CompatType.UPST)
	public AtkDataModel getURevi() {
		return revis.length == 0 ? null : revis[0];
	}
}
