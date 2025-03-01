package common.battle.entity;

import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.attack.AtkModelUnit;
import common.battle.attack.AttackAb;
import common.battle.data.MaskAtk;
import common.battle.data.MaskEnemy;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.util.anim.AnimU;
import common.util.anim.EAnimU;
import common.util.pack.EffAnim;
import common.util.stage.Revival;
import common.util.unit.Enemy;
import common.util.unit.Trait;

public class EEnemy extends Entity {

	public final int mark;
	public final double mult, mula;

	public Revival rev;
	public float door;

	public EEnemy(StageBasis b, MaskEnemy de, EAnimU ea, float magnif, float atkMagnif, int d0, int d1, int m) {
		super(b, de, ea, atkMagnif, magnif);
		mult = magnif;
		mula = atkMagnif;
		mark = m;
		isBase = mark <= -1;
		layer = d0 == d1 ? d0 : d0 + (int) (b.r.nextFloat() * (d1 - d0 + 1));
		traits = new SortedPackSet<>(de.getTraits());

		skipSpawnBurrow = mark >= 1;
	}

	@Override
	public void kill(boolean glass) {
		super.kill(glass);

		if (!basis.st.trail && !glass && basis.maxBankLimit() <= 0) {
			float mul = basis.b.t().getDropMulti(basis.elu.getInc(C_MEAR)) * (1 + (status.money / 100f));
			basis.money = (int) (basis.money + mul * ((MaskEnemy) data).getDrop());
		}
		if (rev != null) {
			rev.triggerRevival(basis, basis.est.mul, layer, group, pos);
			if (!anim.deathSurge && rev.soul != null)
				anim.dead = rev.soul.get().getEAnim(AnimU.SOUL[0]).len();
		}
		if (mark >= 1 && basis.st.bossGuard) {
			basis.baseBarrier--;
			if (basis.baseBarrier == 0) {
				if (basis.ebase instanceof ECastle) {
					((ECastle) basis.ebase).guard = effas().A_E_GUARD.getEAnim(EffAnim.GuardEff.BREAK);
					CommonStatic.setSE(SE_BARRIER_ABI);
				} else
					((EEnemy)basis.ebase).anim.getEff(A_GUARD_BRK);
			}
		}
	}

	@Override
	public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
		float ans = super.calcDamageMult(dmg, e, matk);
		if (ans == 0)
			return 0;
		if (e instanceof EUnit) {
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (e.getAbi() & AB_WKILL) > 0)
				ans *= basis.b.t().getWKAtk(basis.elu.getInc(C_WKILL));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (e.getAbi() & AB_EKILL) > 0)
				ans *= basis.b.t().getEKAtk(basis.elu.getInc(C_EKILL));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (e.getAbi() & AB_BAKILL) > 0)
				ans *= 1.6;
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BEAST)) && matk.getProc().BSTHUNT.type.active)
				ans *= 2.5;
		}
		return ans;
	}

	@Override
	protected void sumDamage(int atk, boolean raw) {
		if (CommonStatic.getConfig().rawDamage == raw)
			basis.enemyStatistics.get((Enemy)data.getPack())[1] += atk;
	}

	@Override
	public void damaged(AttackAb atk) {
		if (isBase && dire == 1 && basis.baseBarrier > 0) {
			anim.getEff(A_GUARD);
			return;
		}
		super.damaged(atk);
	}

	@Override
	protected int getDamage(AttackAb atk, int ans) {
		ans = super.getDamage(atk, ans);
		if (atk.model instanceof AtkModelUnit) {
			SortedPackSet<Trait> sharedTraits = traits.inCommon(atk.trait);
			boolean isAntiTraited = targetTraited(atk.trait);
			sharedTraits.addIf(traits, t -> !t.BCTrait() && ((t.targetType && isAntiTraited) || t.others.contains(atk.attacker.data.getPack())));//Ignore the warning, atk.attacker will always be an unit

			if (!sharedTraits.isEmpty()) {
				if (atk.attacker.status.curse == 0 && atk.attacker.getProc().DMGINC.mult != 0)
					ans *= EUnit.OrbHandler.getOrb(atk.attacker.getProc().DMGINC.mult, atk, sharedTraits, basis.b.t());
				if (status.curse == 0 && getProc().DEFINC.mult != 0)
					ans /= getProc().DEFINC.mult/100.0;
			}
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (atk.abi & AB_WKILL) > 0)
				ans *= basis.b.t().getWKAtk(basis.elu.getInc(C_WKILL));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (atk.abi & AB_EKILL) > 0)
				ans *= basis.b.t().getEKAtk(basis.elu.getInc(C_EKILL));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (atk.abi & AB_BAKILL) > 0)
				ans *= 1.6;
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BEAST)) && atk.getProc().BSTHUNT.type.active)
				ans *= 2.5;
			if (traits.contains(BCTraits.get(TRAIT_SAGE)) && (atk.abi & AB_SKILL) > 0)
				ans = (int) (ans * SUPER_SAGE_HUNTER_ATTACK);
		}
		if (atk.canon == 16)
			if ((touchable() & TCH_UG) > 0)
				ans = (int) (maxH * basis.b.t().getCannonMagnification(5, BASE_HOLY_ATK_UNDERGROUND));
			else
				ans = (int) (maxH * basis.b.t().getCannonMagnification(5, BASE_HOLY_ATK_SURFACE));
		ans = critCalc((getAbi() & AB_METALIC) != 0 || data.getTraits().contains(UserProfile.getBCData().traits.get(TRAIT_METAL)), ans, atk);

		// Perform Orb
		ans += EUnit.OrbHandler.getOrbAtk(atk, this);

		return ans;
	}

	@Override
	protected float getLim() {
		float ans;
		float minPos = ((MaskEnemy) data).getLimit();

		if (mark >= 1)
			ans = pos - (minPos + basis.boss_spawn); // guessed value compared to BC
		else
			ans = pos - minPos;
		return Math.max(0, ans);
	}

	@Override
	public float getResistValue(AttackAb atk, boolean SageRes, double procResist) {
		float ans = (float) ((100f - procResist) / 100f);

		if (SageRes && (atk.abi & AB_SKILL) == 0 && traits.contains(BCTraits.get(TRAIT_SAGE)))
			ans *= SUPER_SAGE_RESIST;
		return ans;
	}

	@Override
	public void postUpdate() {
		if (skipSpawnBurrow && notAttacking())
			skipSpawnBurrow = status.burs[0] == 0;
		super.postUpdate();
	}

	@Override
	public double buff(int lv) {
		return lv * mult * mula;
	}
}
