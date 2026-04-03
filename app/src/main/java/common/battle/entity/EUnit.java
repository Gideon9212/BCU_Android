package common.battle.entity;

import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.Treasure;
import common.battle.attack.AtkModelEnemy;
import common.battle.attack.AtkModelUnit;
import common.battle.attack.AttackAb;
import common.battle.data.MaskAtk;
import common.battle.data.MaskUnit;
import common.battle.data.OrbInfo;
import common.battle.data.PCoin;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.util.BattleObj;
import common.util.Data;
import common.util.anim.EAnimU;
import common.util.unit.Level;
import common.util.unit.Trait;

import java.util.List;
import java.util.Map;

public class EUnit extends Entity {

	public static class OrbHandler extends BattleObj {

		protected static float getOrb(double mult, AttackAb atk, SortedPackSet<Trait> traits, Treasure t) {
			if(atk.origin.model instanceof AtkModelUnit)
				return ((EUnit) ((AtkModelUnit) atk.origin.model).e).getOrb(mult, atk.trait, traits, t);
			return ((EUnit) ((AtkModelUnit)atk.model).e).getOrb(mult, atk.trait, traits, t);
		}

		protected static int getOrbAtk(AttackAb atk, EEnemy en) {
			if (atk.matk == null || !(atk.origin.model instanceof AtkModelUnit))
				return 0;
			// Warning : Eunit.e became public now
			EUnit unit = (EUnit)((AtkModelUnit) atk.origin.model).e;
			return unit.getOrb(en.traits, atk.matk.getAtk(), true);
		}
	}

	public final int lvl;
	public final int[] index;

	protected final Level level;
	/**
	 * Last position where entity moved without interruption
	 */
	public float lastPosition;

	private static float getD(String sid, float d0, Level lv) {
		if (lv.getOrbs() != null) {
			if (sid.equals("000000")) //SoL
				for (int[] orb : lv.getOrbs())
					if (orb.length == ORB_TOT && orb[ORB_TYPE] == ORB_SOLBUFF)
						return d0 * (100 + OrbInfo.get(ORB_SOLBUFF,(byte)orb[ORB_GRADE])[1]) / 100;
			if (sid.equals("000013")) //UL
				for (int[] orb : lv.getOrbs())
					if (orb.length == ORB_TOT && orb[ORB_TYPE] == ORB_ULBUFF)
						return d0 * (100 + OrbInfo.get(ORB_ULBUFF,(byte)orb[ORB_GRADE])[1]) / 100;
			//ZL is 000034
		}
		return d0;
	}

	public EUnit(StageBasis b, MaskUnit de, EAnimU ea, float d0, int layer0, int layer1, Level level, PCoin pc, int[] index, boolean isBase) {
		super(b, de, ea, getD(b.st.getMC().getSID(), d0, level), pc, level);
		layer = layer0 == layer1 ? layer0 : layer0 + (int) (b.r.nextFloat() * (layer1 - layer0 + 1));
		traits = new SortedPackSet<>(de.getTraits(false));
		lvl = level.getTotalLv();
		this.index = index;
		this.isBase = isBase;
		if (isBase) {
			maxH = health = maxH * b.b.t().getBaseHealth(b.elu.getInc(C_BASE,this)) / 1000;
			((AtkModelUnit)aam).d2 = b.b.t().getCanonAtk(b.elu.getInc(C_C_ATK,this)) / 100.0;
			if (b.est.lim.stageLimit != null) {
				((AtkModelUnit) aam).d2 *= b.est.lim.stageLimit.cannonMultiplier / 100.0;
				maxH = health = maxH * b.est.lim.stageLimit.cannonMultiplier / 100;
			}
		}
		this.level = level;
		setOrbProcs();
	}

	public void setOrbProcs() {
		if(((MaskUnit)data).getOrb() != null && level.getOrbs() != null) {
			int[][] levelOrbs = level.getOrbs();
			for (int[] orb : levelOrbs)
				if (orb.length == ORB_TOT && orb[ORB_TYPE] >= ORB_MINIDEATHSURGE) {
					int eff = OrbInfo.get((byte)orb[ORB_TYPE],(byte)orb[ORB_GRADE])[0];
					switch (orb[ORB_TYPE]) {
						case ORB_RESKB:
							getProc().IMUKB.mult += eff;
							break;
						case ORB_RESWAVE:
							getProc().IMUWAVE.mult += eff;
							break;
						case ORB_REFUND:
							if (getProc().MONEYBACK.prob == 0) {
								getProc().MONEYBACK.prob = 100;
								getProc().MONEYBACK.count = 2;
							}
							getProc().MONEYBACK.mult += eff;
							break;
						case ORB_MINIDEATHSURGE:
							if (getProc().MINIDEATHSURGE.prob == 0) {
								getProc().MINIDEATHSURGE.prob = 100;
								getProc().MINIDEATHSURGE.dis_0 = 200;
								getProc().MINIDEATHSURGE.dis_1 = 500;
								getProc().MINIDEATHSURGE.time = getProc().MINIDEATHSURGE.maxtime = 20;
								getProc().MINIDEATHSURGE.spawns = 2;
							}
							getProc().MINIDEATHSURGE.mult += eff;
							break;
						case ORB_CANNON_CHARGE:
							if (getProc().CANONCHARGE.prob == 0) {
								getProc().CANONCHARGE.prob = 100;
								getProc().CANONCHARGE.count = 2;
							}
							getProc().CANONCHARGE.mult += eff;
							break;
						case ORB_RESTOXIC:
							getProc().IMUPOIATK.mult += eff;
							break;
						case ORB_DODGE:
							if (getProc().IMUATKANY.prob == 0)
								getProc().IMUATKANY.time = 30;
							getProc().IMUATKANY.prob += eff;
							break;
						case ORB_RESSLOW:
							getProc().IMUSLOW.mult += eff;
							break;
						case ORB_RESCURSE:
							getProc().IMUCURSE.mult += eff;
							break;
						case ORB_COUNTERSURGE:
							if (getProc().DEMONVOLC.prob == 0) {
								getProc().DEMONVOLC.max_times = 1;
								getProc().DEMONVOLC.count = 2;
								getProc().DEMONVOLC.mult = 100;
							}
							getProc().DEMONVOLC.prob += eff;
							break;
						case ORB_KILLSTRENGTHEN:
							getProc().KILLSTRENGTHEN.mult += eff;
							break;
						case ORB_LESSCD:
							if (getProc().COMBOCOOLDOWN.prob == 0) {
								getProc().COMBOCOOLDOWN.prob = 100;
								getProc().COMBOCOOLDOWN.count = 2;
							}
							getProc().COMBOCOOLDOWN.mult += eff;
							break;
						case ORB_RESFREEZE:
							getProc().IMUSTOP.mult += eff;
							break;
						case ORB_RESWEAKEN:
							getProc().IMUWEAK.mult += eff;
							break;
						case ORB_RESSURGE:
							getProc().IMUVOLC.mult += eff;
							break;
						case ORB_BOUNTY:
							getProc().BOUNTY.mult += eff;
					}
				}
		}
	}

	public EUnit(StageBasis b, MaskUnit de, EAnimU ea, float d0) {
		super(b, de, ea, d0, null, null);
		layer = de.getFront() + (int) (b.r.nextFloat() * (de.getBack() - de.getFront() + 1));
		traits = new SortedPackSet<>(de.getTraits(false));
		this.index = null;

		lvl = 1;
		health = maxH = (int) (health * b.b.t().getCannonMagnification(BASE_WALL, BASE_WALL_MAGNIFICATION) / 100.0);
		level = null;
	}

	@Override
	public void added(int d, float p) {
		super.added(d,p);
		lastPosition = p;

		int spwn = basis.spawns.get(data.getPack());
		if (spwn % getProc().COMBOCOOLDOWN.count == 0 && getProc().COMBOCOOLDOWN.perform(basis.r))
			basis.elu.cool[index[0]][index[1]] *= 1 - (getProc().COMBOCOOLDOWN.mult / 100);
		if (getProc().MONEYBACK.prob > 0 && spwn % getProc().MONEYBACK.count != 0)
			getProc().MONEYBACK.clear();
		if (getProc().CANONCHARGE.prob > 0 && spwn % getProc().CANONCHARGE.count != 0)
			getProc().CANONCHARGE.clear();
	}

	@Override
	public void kill(boolean glass) {
		super.kill(glass);
		if (!glass && status.money != 0)
			basis.money = (int)(basis.money-((status.money / 100) * (index != null ? basis.elu.price[index[0]][index[1]] : ((MaskUnit)data).getPrice() * basis.st.getCont().price * 100)));
		if (index != null)
			basis.elu.smnd[index[0]][index[1]] = !basis.getAllOf(index[0],index[1]).isEmpty();

		if (getProc().MONEYBACK.perform(basis.r))
			basis.money = (int)(basis.money+((getProc().MONEYBACK.mult / 100.0) * (index != null ? basis.elu.price[index[0]][index[1]] : ((MaskUnit)data).getPrice() * basis.st.getCont().price * 100)));
		if (getProc().CANONCHARGE.perform(basis.r))
			basis.cannon += (float)(basis.maxCannon * getProc().CANONCHARGE.mult / 100);
 	}

	@Override
	public void update() {
		super.update();
		if (status.curse > 0 || status.seal > 0)
			traits.clear();
		else if (traits.isEmpty())
			traits.addAll(data.getTraits(false));
		if (kbTime == 0)
			lastPosition = pos;
	}

	@Override
	public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
		float ans = super.calcDamageMult(dmg, e, matk);
		if (ans == 0)
			return 0;
		if (e instanceof EEnemy) {
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (e.getAbi() & AB_WKILL) > 0)
				ans *= basis.b.t().getWKDef(basis.elu.getInc(C_WKILL,this));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (e.getAbi() & AB_EKILL) > 0)
				ans *= basis.b.t().getEKDef(basis.elu.getInc(C_EKILL,this));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (e.getAbi() & AB_BAKILL) > 0)
				ans = (float)(ans * 0.7);
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BEAST)) && matk.getProc().BSTHUNT.active)
				ans = (float)(ans * 0.6);
			if (traits.contains(UserProfile.getBCData().traits.get(Data.TRAIT_SAGE)) && (e.getAbi() & AB_SKILL) > 0)
				ans *= SUPER_SAGE_HUNTER_HP;
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_VILLAIN))) {
				double re = basis.elu.getInc(C_VKILL,this) / 1000f;
				ans = re == 0 ? ans : (int)(ans / re);
			}
		}
		return ans;
	}

	@Override
	public void damaged(AttackAb atk) {
		if (atk.trait.contains(BCTraits.get(TRAIT_BEAST))) {
			Proc.BSTHUNT beastDodge = getProc().BSTHUNT;
			if (beastDodge.prob > 0 && (atk.dire != getDire())) {
				if (status.wild == 0 && (beastDodge.prob == 100 || basis.r.nextFloat() * 100 < beastDodge.prob)) {
					status.wild = beastDodge.time;
					anim.getEff(P_IMUATK);
				}
				if (status.wild > 0) {
					damageTaken += atk.atk;
					sumDamage(atk.atk, true);
					return;
				}
			}
		}
		super.damaged(atk);
	}

	@Override
	public void cont() {
		kbTime = 0;
		summoned.clear();
	}

	@Override
	protected void sumDamage(int atk, boolean raw) {
		if (index != null && CommonStatic.getConfig().rawDamage == raw)
			basis.dmgStatistics.get(data.getPack())[1] += atk;
	}

	@Override
	public float getResistValue(AttackAb atk, boolean SageRes, double procResist) {
		float ans = (float) ((100f - procResist) / 100f);

		if (SageRes && atk.trait.contains(BCTraits.get(TRAIT_SAGE)) && (getAbi() & AB_SKILL) != 0)
			ans *= SUPER_SAGE_HUNTER_RESIST;
		return ans;
	}

	@Override
	protected int getDamage(AttackAb atk, int ans) {
		ans = super.getDamage(atk, ans);
		if (atk.model instanceof AtkModelEnemy) {
			SortedPackSet<Trait> sharedTraits = traits.inCommon(atk.trait);
			if (!sharedTraits.isEmpty()) {
				if (status.curse == 0 && getProc().DEFINC.mult != 0)
					ans = (int)(ans * basis.b.t().getDEF(getProc().DEFINC.mult, atk.trait, sharedTraits, ((MaskUnit) data).getOrb(), level, basis.elu.getInc(getProc().DEFINC.mult < 400 ? C_GOOD : C_RESIST,this)));
				if (atk.attacker.status.curse == 0 && atk.attacker.getProc().DMGINC.mult != 0)
					ans = (int)(ans * atk.attacker.getProc().DMGINC.mult / 100.0);
			}
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (getAbi() & AB_WKILL) > 0)
				ans = (int)(ans * basis.b.t().getWKDef(basis.elu.getInc(C_WKILL, this)));
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (getAbi() & AB_EKILL) > 0)
				ans = (int)(ans * basis.b.t().getEKDef(basis.elu.getInc(C_EKILL, this)));
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_BARON))) {
				if ((getAbi() & AB_BAKILL) > 0)
					ans = (int) (ans * 0.7);
				if(((MaskUnit)data).getOrb() != null && level.getOrbs() != null) {
					int[][] levelOrbs = level.getOrbs();
					for (int[] orb : levelOrbs)
						if (orb.length == ORB_TOT && orb[ORB_TYPE] == ORB_BAKILL)
							ans = (int)(ans * OrbInfo.EFFECT.get(ORB_BAKILL).get((byte)orb[ORB_GRADE])[1] / 100.0);
				}
			}
			if (atk.trait.contains(UserProfile.getBCData().traits.get(Data.TRAIT_BEAST)) && getProc().BSTHUNT.active)
				ans = (int)(ans * 0.6); //Not sure
			if (atk.trait.contains(UserProfile.getBCData().traits.get(Data.TRAIT_SAGE)) && (getAbi() & AB_SKILL) > 0)
				ans = (int) (ans * SUPER_SAGE_HUNTER_HP);
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_VILLAIN))) {
				double re = basis.elu.getInc(C_VKILL,this) / 1000f;
				ans = re == 0 ? ans : (int)(ans / re);
			}
		}
		// Perform orb
		ans = getOrb(atk.trait, ans, false);

		if(basis.canon.base > 0)
			ans = (int) (ans * basis.b.t().getBaseMagnification(basis.canon.base, atk.trait));
		return critCalc((getAbi() & AB_METALIC) != 0, ans, atk);
	}

	@Override
	protected void processProcs0(AttackAb atk, int dmg) {
		Proc.CDSETTER cd = atk.getProc().CDSETTER;
		if (cd.prob > 0 && cd.slot == 10 && index != null && index[1] < 5)
			basis.changeUnitCooldown(cd.amount, index[0] * 5 + index[1], cd.type);
		super.processProcs0(atk, dmg);
	}

	@Override
	protected float getLim() {
		return Math.max(0, basis.st.len - pos - ((MaskUnit) data).getLimit());
	}

	@Override
	protected float getMov(float extmov) {
		if (status.slow == 0)
			extmov += (float)((basis.speedLimit(false) > -1 ? basis.speedLimit(false) : data.getSpeed()) * basis.elu.getInc(C_SPE,this) / 50) / 4f;
		return super.getMov(extmov);
	}

	private int getOrb(SortedPackSet<Trait> trait, int matk, boolean atk) {
		OrbInfo orb = ((MaskUnit) data).getOrb();
		if (orb == null || level.getOrbs() == null)
			return atk ? 0 : matk;
		int ans = atk ? 0 : matk;
		for (int[] line : level.getOrbs()) {
			int ORB = atk ? ORB_ATK : ORB_RES;
			if (line.length != ORB_TOT || line[ORB_TYPE] != ORB)
				continue;
			List<Trait> orbType = Trait.convertOrb(line[ORB_TRAIT]);
			boolean orbValid = false;
			for (Trait orbT : orbType)
				if (trait.contains(orbT)) {
					orbValid = true;
					break;
				}
			if (orbValid) {
				if (atk)
					ans += orb.getAtk(line[ORB_GRADE], matk);
				else
					ans = orb.getRes(line[ORB_GRADE], ans);
			}
		}
		return ans;
	}

	private float getOrb(double mult, SortedPackSet<Trait> eTraits, SortedPackSet<Trait> traits, Treasure t) {
		final byte ORB_LV = mult < 500 && mult > 100 ? mult < 300 ? ORB_STRONG : ORB_MASSIVE : -1;
		final Map<Byte,int[]> ORB_MULTIS = ORB_LV == -1 ? null : OrbInfo.EFFECT.get(ORB_LV);
		final float div = ORB_LV == ORB_STRONG ? 1000 : 300;
		float ini = 1;
		if (!traits.isEmpty())
			ini = (float) ((mult/100f) + (ORB_LV == ORB_STRONG ? 0.3f : mult > 100 ? 1f : 0f) / 3 * t.getFruit(traits));

		if(ORB_MULTIS != null && ((MaskUnit)data).getOrb() != null && level.getOrbs() != null) {
			int[][] levelOrbs = level.getOrbs();
			for (int[] lvOrb : levelOrbs)
				if (lvOrb.length == ORB_TOT && lvOrb[ORB_TYPE] == ORB_LV) {
					List<Trait> orbType = Trait.convertOrb(lvOrb[ORB_TRAIT]);
					for (Trait orbTr : orbType)
						if (eTraits.contains(orbTr)) {
							ini += ORB_MULTIS.get((byte)lvOrb[ORB_GRADE])[0] / div;
							break;
						}
				}
		}
		if (ini == 1 || ORB_LV == -1)
			return ini;
		float com = 1 + basis.elu.getInc(ORB_LV == ORB_STRONG ? C_GOOD : C_MASSIVE,this) * 0.01f;
		return ini * com;
	}

	@Override
	protected void onLastBreathe() {
		super.onLastBreathe();
		basis.notifyUnitDeath();
	}

	@Override
	public double buff(int lv) {
		return lv + lvl;
	}
}
