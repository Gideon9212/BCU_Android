package common.battle.attack;

import common.battle.data.MaskAtk;
import common.battle.data.MaskEntity;
import common.battle.data.PCoin;
import common.battle.entity.*;
import common.pack.Identifier;
import common.util.Data;
import common.util.Data.Proc.SUMMON;
import common.util.unit.*;
import org.jcodec.common.tools.MathUtil;

import java.util.List;

public abstract class AtkModelEntity extends AtkModelAb {

	public static final String[] par = { "SUMMON", "KB", "STOP", "SLOW", "WEAK", "WARP", "CURSE", "SNIPER", "SEAL", "POISON", "BOSS", "RAGE", "HYPNO", "POIATK",
			"ARMOR", "SPEED", "LETHARGY", "DRAIN", "BLESSING",//post-cursedProcs
			"ATKBASE", "CRIT", "WAVE", "BREAK", "SATK", "VOLC", "MINIVOLC", "MINIWAVE", "MOVEWAVE", "SHIELDBREAK", "WORKERLV", "CDSETTER", "METALKILL", "BLAST"};
	/**
	 * Gets Attack Model for enemies
	 * @param e The entity
	 * @param mult Magnification
	 * @return returns AtkModelEntity with specified magnification values
	 */
	public static AtkModelEntity getEnemyAtk(Entity e, float mult) {
		if (e instanceof EEnemy) {
			EEnemy ee = (EEnemy) e;
			return new AtkModelEnemy(ee, mult);
		}
		return null;
	}

	/**
	 * Gets Attack Model for cat Units
	 * @param e The entity
	 * @param treasure Treasure Effects
	 * @param level Level stat multiplier
	 * @param pcoin Talents
	 * @param lv Misc level data
	 * @return returns AtkModelEntity with specified magnification values
	 */
	public static AtkModelEntity getUnitAtk(Entity e, float treasure, float level, PCoin pcoin, Level lv) {
		if(!(e instanceof EUnit))
			return null;

		return new AtkModelUnit(e, treasure, level, pcoin, lv);
	}

	/**
	 * Current index for multi-attacks
	 */
	public int atkType;
	protected final double d0, d1;
	/**
	 * Base stat data of the entity
	 */
	protected final MaskEntity data;
	public final Entity e;
	/**
	 * How many times can the given attack be done, per attack
	 */
	protected final int[][] act;

	protected AtkModelEntity(Entity ent, float d0, float d1) {
		super(ent.basis);
		e = ent;
		data = e.data;
		this.d0 = d0;
		this.d1 = d1;

		MaskAtk[][] matks = data.getAllAtks();
		MaskAtk[][] satks = data.getSpAtks(false);
		act = new int[matks.length + satks.length][];
		setAtks(matks, satks);
	}

	protected AtkModelEntity(Entity ent, float d0, float d1, PCoin pc, Level lv) {
		super(ent.basis);
		e = ent;
		data = e.data;
		if (pc != null && lv != null && lv.getTalents().length == pc.max.length)
			this.d0 = d0 * pc.getStatMultiplication(PC2_ATK, lv.getTalents());
		else
			this.d0 = d0;
		this.d1 = d1 * (1 + ent.basis.elu.getInc(Data.C_ATK) * 0.01);

		MaskAtk[][] matks = data.getAllAtks();
		MaskAtk[][] satks = data.getSpAtks(false);
		act = new int[matks.length + satks.length][];
		setAtks(matks, satks);
	}

	protected void setAtks(MaskAtk[][] matks, MaskAtk[][] satks) {
		atkType = data.firstAtk();
		for (int i = 0; i < matks.length; i++) {
			if (matks[i] == null)
				continue;
			act[i] = new int[matks[i].length];
			for (int j = 0; j < act[i].length; j++)
				act[i][j] = data.getAtkModel(i, j).loopCount();
		}
		for(int i = 0; i < satks.length; i++) {
			int ind = act.length - satks.length + i;
			act[ind] = new int[satks[i].length];
			for (int j = 0; j < act[ind].length; j++)
				act[ind][j] = satks[i][j].loopCount();
		}
	}

	@Override
	public int getAbi() {
		return e.getAbi();
	}

	/**
	 * get the attack, for display only
	 */
	public int getAtk() {
		int ans = 0, temp = 0, c = 1;
		MaskAtk[] atks = data.getAtks(atkType);
		for (int i = 0; i < atks.length; i++)
			if (atks[i].getPre() > 0 || atks[i].getName().toLowerCase().startsWith("combo")) {
				ans += temp / c;
				temp = atks[i].getDire() > 0 ? getEffAtk(i) : 0;
				c = 1;
			} else {
				temp += atks[i].getDire() > 0 ? getEffAtk(i) : 0;
				c++;
			}
		ans += temp / c;
		return ans;
	}

	/**
	 * get damage from a specific attack, for AI only
	 */
	public int getAtk(int ind, int touch) {
		if (ind < data.getAtkCount(atkType) && getMAtk(ind).getDire() > 0 && (getMAtk(ind).getTarget() & touch) != 0)
			return getEffAtk(ind);
		return -1;
	}

	public int getEffAtk(int ind) {
		return getEffAtk(getMAtk(ind));
	}
	public int getEffAtk(MaskAtk matk) {
		return getEffMult(getDefAtk(matk));
	}
	public int getDefAtk(MaskAtk matk) {
		return (int)(Math.round(matk.getAtk() * d0) * d1);
	}
	public int getEffMult(int dmg) {
		if (e.status.getWeaken() != 1)
			dmg = (int) (dmg * e.status.getWeaken());
		if (e.status.strengthen != 0)
			dmg += dmg * e.status.strengthen / 100;
		dmg *= e.auras.getAtkAura();
		return dmg;
	}

	public int predictDamage(int ind) {
		int total = 0;
		MaskAtk[] atks = data.getAtks(ind);
		if (atks == null)
			return 0;
		for (int i = 0; i < atks.length; i++) {
			if (act[ind][i] == 0)
				continue;
			MaskAtk atk = atks[i];
			int dmg = getEffAtk(atk) * atk.getDire();
			float[] ranges = inRange(atk);
			List<AbEntity> ents = e.basis.inRange(atk.getTarget(), atk.getDire() * getDire(), ranges[0], ranges[1], false);
			for (AbEntity ent : ents)
				total = total + (int)(Math.min(ent.health * atk.getDire(), dmg * ent.calcDamageMult(dmg, e, atk)));
		}
		return total;
	}

	public boolean isUsable(int ind) {
		for (int act : act[ind])
			if (act != 0)
				return true;
		return false;
	}

	/**
	 * generate attack entity
	 */
	public final AttackAb getAttack(int ind) {
		if (act[atkType][ind] == 0)
			return null;
		act[atkType][ind]--;
		Proc proc = Proc.blank();
		MaskAtk matk = getMAtk(ind);
		int atk = getAttack(matk, proc);
		float[] ints = inRange(matk);
		return new AttackSimple(e, this, atk, e.traits, getAbi(), proc, ints[0], ints[1], matk, e.layer, matk.isLD() || matk.isOmni());
	}

	/**
	 * generate attack entity for a special attack
	 */
	public final AttackAb getSpAttack(int atkind, int ind) {
		int actind = data.getAtkTypeCount() + atkind;
		if (act[actind][ind] == 0)
			return null;
		act[actind][ind]--;
		Proc proc = Proc.blank();
		MaskAtk matk = data.getSpAtks(false, atkind)[ind];
		int atk = getAttack(matk, proc);
		float[] ints = inRange(matk);
		return new AttackSimple(e, this, atk, e.traits, getAbi(), proc, ints[0], ints[1], matk, e.layer, matk.isLD() || matk.isOmni());
	}

	/**
	 * Generate death surge when this entity is killed and the surge procs
	 */
	public void getDeathSurge(byte types) {
		Proc p = Proc.blank();
		for (byte i = 1; i < 4; i *= 2) {
			if ((i & types) == 0)
				continue;
			int atk = getAttack(data.getAtkModel(data.firstAtk(), 0), p);
			Proc.VOLC ds = i == 1 ? e.getProc().DEATHSURGE : e.getProc().MINIDEATHSURGE;
			if (ds instanceof Proc.MINIVOLC)
				atk = (int)(atk * ((Proc.MINIVOLC) ds).mult / 100.0);
			AttackSimple as = new AttackSimple(e, this, atk, e.traits, getAbi(), p, 0, 0, data.getAtkModel(data.firstAtk(), 0), 0, false);

			int addp = ds.dis_0 == ds.dis_1 ? ds.dis_0 : ds.dis_0 + (int) (b.r.nextFloat() * (ds.dis_1 - ds.dis_0));
			float p0 = getPos() + getDire() * addp;
			float sta = p0 + (getDire() == 1 ? W_VOLC_PIERCE : W_VOLC_INNER);
			float end = p0 - (getDire() == 1 ? W_VOLC_INNER : W_VOLC_PIERCE);

			e.summoned.add(new ContVolcano(new AttackVolcano(e, as, sta, end, i == 1 ? WT_VOLC : WT_MIVC, ds.pid), p0, e.layer, ds.time));
		}
	}

	/**
	 * Generate counter surge
	 */
	public void getCounterSurge(float pos, Proc.VOLC itm) {
		Proc p = Proc.blank();
		double mult = e.getProc().DEMONVOLC.mult;

		int atk = (int)(getAttack(data.getAtkModel(data.firstAtk(), 0), p) * (mult / 100.0));
		if (itm instanceof Proc.MINIVOLC) {
			mult *= ((Proc.MINIVOLC) itm).mult / 100.0;
			atk = (int)(atk * ((Proc.MINIVOLC) itm).mult / 100.0);
		}

		AttackSimple as = new AttackSimple(e, this, atk, e.traits, getAbi(), p, 0, 0, data.getAtkModel(data.firstAtk(), 0), 0, false);
		int addp = itm.dis_0 == itm.dis_1 ? itm.dis_0 : itm.dis_0 + (int) (b.r.nextFloat() * (itm.dis_1 - itm.dis_0));
		float p0 = pos + getDire() * addp;
		float sta = p0 + (getDire() == 1 ? W_VOLC_PIERCE : W_VOLC_INNER);
		float end = p0 - (getDire() == 1 ? W_VOLC_INNER : W_VOLC_PIERCE);

		e.summoned.add(new ContVolcano(new AttackVolcano(e, as, sta, end, mult >= 100 ? WT_VOLC : WT_MIVC, itm.pid), p0, e.layer, itm.time, true));
	}

	@Override
	public int getDire() {
		return e.getDire();
	}

	@Override
	public float getPos() {
		return e.pos;
	}

	/**
	 * get the attack box for maskAtk
	 */
	public float[] inRange(MaskAtk atk) {
		int dire = e.getDire();
		float d0, d1;
		d0 = d1 = dire == e.dire ? e.pos : e.pos + (data.getWidth() * dire);
		if (!atk.isLD() && !atk.isOmni()) {
			d0 += data.getRange() * dire;
			d1 -= data.getWidth() * dire;
		} else {
			d0 += atk.getShortPoint() * dire;
			d1 += atk.getLongPoint() * dire;
		}
		return new float[] { d0, d1 };
	}

	/**
	 * get the attack box for nth attack
	 */
	public float[] inRange(int ind) {
		return inRange(getMAtk(ind));
	}

	@Override
	public void invokeLater(AttackAb atk, Entity e) {
		SUMMON proc = atk.getProc().SUMMON;
		if (proc.prob > 0 && (proc.on_hit || (proc.on_kill && e.health <= 0))) {
			double rst = e.getProc().IMUSUMMON.mult;
			summon(proc, e, atk, rst);
		}
	}

	/**
	 * get the collide box bound
	 */
	public float[] touchRange() {
		int dire = e.getDire();
		float d0, d1;
		d0 = d1 = dire == e.dire ? e.pos : e.pos + (data.getWidth() * dire);
		d0 += data.getRange() * dire;
		if (data.isLD() && e.getProc().AI.calcblindspot)
			d1 += getBlindSpot() * dire;
		else
			d1 -= data.getWidth() * dire;
		return new float[] { d0, d1 };
	}

	protected void extraAtk(MaskAtk matk) {
		if (matk.getMove() != 0)
			e.pos += matk.getMove() * e.getDire();
		if (matk.getAltAbi() != 0)
			e.altAbi(matk.getAltAbi());

		Proc p = getProc(matk);
		if (p.TIME.prob != 0 && (p.TIME.prob == 100 || b.r.nextFloat() * 100 < p.TIME.prob)) {
			b.tstop = Math.max(b.tstop, p.TIME.time);
			b.timeFlow = (100f - p.TIME.intensity) / 100;
		}
		Proc.THEME t = p.THEME;
		if (t.prob != 0 && (t.prob == 100 || b.r.nextFloat() * 100 < t.prob))
			b.changeTheme(t);
		Proc.WORKLV w = p.WORKERLV;
		if (w.prob != 0 && (w.prob == 100 || b.r.nextFloat() * 100 < w.prob))
			b.changeWorkerLv(w.mult);
	}

	protected abstract int getAttack(MaskAtk ind, Proc proc);

	public final MaskAtk getMAtk(int ind) {
		return data.getAtkModel(atkType, ind);
	}

	@Override
	protected int getLayer() {
		return e.layer;
	}

	public Proc getProc(MaskAtk matk) {
		if (e.status.seal > 0)
			return empty;
		if (e.status.blessings.isEmpty() || !matk.canProc())
			return matk.getProc();
		Proc p = matk.getProc().clone();
		for (common.util.Data.Proc.BLESSING b : e.status.blessings.keySet())
			if (b.procs != null)
				for (String str : par)
					p.get(str).add(b.procs.get(str));
		return p;
	}

	public Proc getProc(int ind) {
		return getProc(getMAtk(ind));
	}

	protected void setProc(MaskAtk matk, Proc proc, int startOff) {
		Proc p = getProc(matk);
		for (int i = startOff; i < par.length; i++)
			if (p.get(par[i]).perform(b.r))
				proc.get(par[i]).set(p.get(par[i]));
		for (int b : BCShareable) proc.getArr(b).set(p.getArr(b));
		if (p.SUMMON.perform(b.r)) {
			SUMMON sprc = p.SUMMON;
			if (!sprc.on_hit && !sprc.on_kill)
				summon(sprc, e, matk, 0);
			else
				proc.SUMMON.set(sprc);
		}
		Proc.CDSETTER c = p.CDSETTER;
		if (c.perform(b.r)) {
			if (c.slot == 10)
				proc.CDSETTER.set(c);
			else if (c.slot < 11)
				b.changeUnitCooldown(c.amount, c.slot, c.type);
			else
				b.changeUnitsCooldown(c.amount, c.type);
		}

		if (proc.CRIT.prob > 0 && proc.CRIT.mult == 0)
			proc.CRIT.mult = 200;
		if (proc.KB.prob > 0) {
			if (proc.KB.dis == 0)
				proc.KB.dis = KB_DIS[INT_KB];
			if (proc.KB.time == 0)
				proc.KB.time = KB_TIME[INT_KB];
		}
		if (proc.MINIWAVE.prob > 0 && proc.MINIWAVE.multi == 0)
			proc.MINIWAVE.multi = 20;
		if (proc.MINIVOLC.prob > 0 && proc.MINIVOLC.mult == 0)
			proc.MINIVOLC.mult = 20;
	}

	public double getBlindSpot() {
		double blindspot = data.getWidth() * e.getDire();
		if (data.isLD()) {
			blindspot = Integer.MAX_VALUE;
			for (int i = 0; i < data.getAtkCount(0); i++)
				blindspot = Math.min(getMAtk(i).getShortPoint(), blindspot);

			if (blindspot >= data.getRange())
				blindspot = data.getWidth() * e.getDire();
		}
		return blindspot;
	}

	protected void summon(SUMMON proc, Entity ent, Object acs, double resist) {
		if (resist < 100) {
			if (proc.same_health && ent.health <= 0)
				return;
			int time = proc.time;
			int minlayer = proc.min_layer, maxlayer = proc.max_layer;
			if (proc.min_layer == proc.max_layer && proc.min_layer == -1)
				minlayer = maxlayer = e.layer;

			if ((proc.id == null && e instanceof EUnit) || (proc.id != null && AbUnit.class.isAssignableFrom(proc.id.cls))) {
				AbUnit u = Identifier.getOr(proc.id, AbUnit.class);
				if (proc.ignore_limit || !b.cantDeploy(u.getRarity(), u.getForms()[proc.form - 1].du.getWill())) {
					int lvl = proc.mult;
					if (!proc.fix_buff)
						lvl = (int) e.buff(lvl);
					lvl = (int) (lvl * (100.0 - resist) / 100);
					lvl = MathUtil.clip(lvl, 1, u.getCap());

					for (int i = 0; i < proc.amount; i++) {
						int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextFloat() * (proc.max_dis - proc.dis + 1));
						double up = ent.pos + getDire() * dis;
						Form f = u.getForms()[Math.max(proc.form - 1, 0)];
						IForm ef = IForm.newIns(u instanceof Unit ? f : (AbForm)u, lvl);
						EUnit eu = ef.invokeEntity(b, lvl, minlayer, maxlayer);
						if (proc.same_health)
							eu.health = e.health;

						eu.added(-1, (int) up);
						eu.setSummon(proc.anim_type, proc.bond_hp ? e : null);
						if (proc.anim_type == Proc.SUMMON_ANIM.EVERYWHERE_DOOR)
							b.tempe.add(new EntCont(new DoorCont(b, eu), time + (proc.interval * i)));
						else
							b.tempe.add(new EntCont(eu, time + (proc.interval * i)));
						if (proc.pass_proc % 2 == 1)
							eu.status.pass(e.status);
						if (e != ent && proc.pass_proc >= 2)
							eu.status.pass(ent.status);
					}
				}
			} else {
				AbEnemy ene = Identifier.getOr(proc.id, AbEnemy.class);
				int allow = b.st.data.allow(b, ene);
				if (allow >= 0 || proc.ignore_limit) {
					float mula = proc.mult * 0.01f;
					float mult = proc.mult * 0.01f;
					if (!proc.fix_buff) {
						if (e instanceof EUnit) {
							mula = (float) (mula + ((((EUnit) e).lvl - 1) * 0.2));
							mult = (float) (mult + ((((EUnit) e).lvl - 1) * 0.2));
						} else {
							mult = (float) (mult * ((EEnemy) e).mult);
							mula = (float) (mula * ((EEnemy) e).mula);
						}
					}

					mula = (float) (mula * (100.0 - resist) / 100);
					mult = (float) (mult * (100.0 - resist) / 100);
					for (int i = 0; i < proc.amount; i++) {
						int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextFloat() * (proc.max_dis - proc.dis + 1));
						float up = ent.pos + getDire() * dis;
						EEnemy ee = ene.getEntity(b, acs, mult, mula, minlayer, maxlayer, 0);

						ee.group = allow;
						if (up < ee.data.getWidth())
							up = ee.data.getWidth();
						if (up > b.st.len - 800)
							up = b.st.len - 800;

						ee.added(1, (int) up);
						if (proc.same_health)
							ee.health = e.health;
						ee.setSummon(proc.anim_type, proc.bond_hp ? e : null);
						if (proc.anim_type == Proc.SUMMON_ANIM.EVERYWHERE_DOOR)
							b.tempe.add(new EntCont(new DoorCont(b, ee), time + (proc.interval * i)));
						else
							b.tempe.add(new EntCont(ee, time + (proc.interval * i)));

						if (proc.pass_proc % 2 == 1)
							ee.status.pass(e.status);
						if (e != ent && proc.pass_proc >= 2)
							ee.status.pass(ent.status);
					}
				}
			}
		} else
			ent.anim.getEff(INV);
	}

}
