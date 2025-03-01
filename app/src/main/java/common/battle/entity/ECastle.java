package common.battle.entity;

import common.CommonStatic;
import common.battle.BasisLU;
import common.battle.StageBasis;
import common.battle.attack.AttackAb;
import common.battle.attack.AttackVolcano;
import common.battle.data.MaskAtk;
import common.util.anim.EAnimD;
import common.util.pack.EffAnim.DefEff;
import common.util.pack.EffAnim.GuardEff;
import common.util.unit.Enemy;

import java.util.Arrays;

public class ECastle extends AbEntity {

	private final StageBasis sb;

	public EAnimD<DefEff> smoke;
	public EAnimD<GuardEff> guard;
	public int smokeLayer = -1;
	public int smokeX = -1;

	public ECastle(StageBasis b) {
		super(b.st.trail ? Integer.MAX_VALUE
				: b.st.getMC().getSID().equals("000003") ? b.st.health * (b.est.star + 1) // might be bodged but EoC is the only sm with stars
				: b.st.health);
		sb = b;
	}

	public ECastle(StageBasis b, BasisLU lu) {
		super(lu.t().getBaseHealth(b.elu.getInc(C_BASE)));
		sb = b;
	}

	@Override
	public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
		float ans = (float)(1 + (matk.getProc().ATKBASE.mult / 100f));
		Proc.PM satk = matk.getProc().SATK;
		if (satk.mult > 0) {
			ans *= 1 + ((100 + satk.mult) * 0.01 / (satk.prob / 100f));
		}
		if (matk.getProc().CRIT.mult > 0) {
			ans *= 1 + (0.01 * matk.getProc().CRIT.mult / (matk.getProc().CRIT.prob / 100f));
		}
		return ans;
	}

	@Override
	public void damaged(AttackAb atk) {
		if (dire == 1 && sb.baseBarrier > 0) {
			guard = effas().A_E_GUARD.getEAnim(GuardEff.NONE);
			CommonStatic.setSE(SE_BARRIER_NON);
			return;
		}

		hit = 2;

		if(atk.isLongAtk || atk instanceof AttackVolcano)
			smoke = effas().A_WHITE_SMOKE.getEAnim(DefEff.DEF);
		else
			smoke = effas().A_ATK_SMOKE.getEAnim(DefEff.DEF);

		smokeLayer = (int) (atk.layer + 3 - sb.r.irFloat() * -6);
		smokeX = (int) (pos + 25 - sb.r.irFloat() * -25);

		int ans = atk.atk;
		ans *= 1 + atk.getProc().ATKBASE.mult / 100.0;

		double satk = atk.getProc().SATK.mult;
		if (satk > 0) {
			ans *= (100 + satk) * 0.01;
			sb.lea.add(new EAnimCont(pos, 9, effas().A_SATK.getEAnim(DefEff.DEF), -75f));
			CommonStatic.setSE(SE_SATK);
		}
		if (atk.getProc().CRIT.mult > 0) {
			ans *= 0.01 * atk.getProc().CRIT.mult;
			sb.lea.add(new EAnimCont(pos, 9, effas().A_CRIT.getEAnim(DefEff.DEF), -75f));
			CommonStatic.setSE(SE_CRIT);
		}
		CommonStatic.setSE(SE_HIT_BASE);

		if (atk.attacker != null) {
			atk.attacker.damageGiven += Math.min(ans, health);
			if(atk.attacker instanceof EUnit && ((EUnit)atk.attacker).index != null) {
				int[] index = ((EUnit)atk.attacker).index;
				sb.totalDamageGiven[index[0]][index[1]] += Math.min(ans, health);
			} else if (atk.attacker instanceof EEnemy)
				sb.enemyStatistics.get((Enemy)atk.attacker.data.getPack())[0] += Math.min(ans, health);
		}
		health -= ans;

		if (health > maxH)
			health = maxH;

		if (health <= 0)
			health = 0;

		if(dire == -1 && CommonStatic.getConfig().shake && sb.shakeCoolDown[0] == 0 && (sb.shake == null || !Arrays.equals(sb.shake, SHAKE_MODE_BOSS))) {
			sb.shake = SHAKE_MODE_HIT;
			sb.shakeDuration = SHAKE_MODE_HIT[SHAKE_DURATION];
			sb.shakeCoolDown[0] = SHAKE_MODE_HIT[SHAKE_COOL_DOWN];
		} else if (dire == 1) {
			sb.est.setBaseBarrier();
		}
	}

	@Override
	public int getAbi() {
		return 0;
	}

	@Override
	public boolean isBase() {
		return true;
	}

	@Override
	public void postUpdate() {
	}

	@Override
	public boolean targetable(Entity ent) { return true; }

	@Override
	public int touchable() {
		return TCH_N;
	}

	@Override
	public void update() {
		updateAnimation();

		if (hit > 0)
			hit--;
	}

	@Override
	public void updateAnimation() {
		//Do nothing
		if(smoke != null) {
			if(smoke.done()) {
				smoke = null;
				smokeLayer = -1;
				smokeX = -1;
			} else
				smoke.update(false, sb.timeFlow);
		}
		if (guard != null)
			if (guard.done())
				guard = null;
			else
				guard.update(false, sb.timeFlow);
	}

	@Override
	public void preUpdate() {
	}
}
