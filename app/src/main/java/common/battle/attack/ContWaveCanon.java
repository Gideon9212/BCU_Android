package common.battle.attack;

import common.CommonStatic;
import common.battle.entity.AbEntity;
import common.battle.entity.Entity;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.util.pack.NyCastle.NyType;

import java.util.HashSet;
import java.util.Set;

public class ContWaveCanon extends ContWaveAb {

	private final int canid;

	// used only by normal and zombie cannon
	public ContWaveCanon(AttackWave a, float p, int id) {
		this(a, p, id, 3 + (W_TIME + 1) * a.proc.WAVE.lv + 4, new HashSet<>());
	} // maxTime = hitframe offset + (waves attack period) * (number of waves - 1) + ending linger

	private ContWaveCanon(AttackWave a, float p, int id, int maxTime, Set<ContWaveAb> waves) {
		super(a, p, CommonStatic.getBCAssets().atks[id].getEAnim(NyType.ATK), 9, 0);
		canid = id;
		soundEffect = SE_CANNON[canid][1];

		this.waves = waves;
		this.waves.add(this);
		maxt = maxTime;

		if (id != 0) {
			anim.setTime(1);
			maxt -= 1;
		}
		if (t > 0)
			update(false);
	}

	@Override
	public void draw(FakeGraphics gra, P p, float psiz) {
		if (t < 0)
			return;
		drawAxis(gra, p, psiz);
		if (canid == 0)
			psiz *= 1.25;
		else
			psiz *= 0.5 * 1.25;
		P pus = canid == 0 ? new P(9, 40) : new P(-72, 0);
		anim.draw(gra, p.plus(pus, -psiz), psiz * 2);
	}

	public float getSize() {
		return 2.5f;
	}

	@Override
	public void update(boolean nini) {
		tempAtk = false;
		// guessed attack point compared from BC
		int attack = 2;
		// guessed wave block time compared from BC
		if (t == 0)
			CommonStatic.setSE(soundEffect);
		if (t >= 1 && t <= attack) {
			atk.capture();
			for (AbEntity e : atk.capt)
				if (e instanceof Entity) {
					float waves = e.getProc().IMUWAVE.block;
					if (waves != 0) {
						if (waves > 0)
							((Entity) e).anim.getEff(STPWAVE);
						if (waves == 100) {
							deactivate(e);
							return;
						} else
							atk.raw = (int) (atk.raw * (100 - waves) / 100);
					}
				}
		}
		if (!activate)
			return;
		if (t == W_TIME && atk.getProc().WAVE.lv > 0)
			nextWave(t - W_TIME);
		if (t >= attack) {
			sb.getAttack(atk);
			tempAtk = true;
		}
		if (maxt == t)
			deactivate(null);
		updateAnimation();
		if (nini)
			t += atk.model.b.timeFlow;
	}

	@Override
	public void updateAnimation() {
		if (t >= 0 && !anim.done())
			anim.update(false, atk.model.b.timeFlow);
	}

	@Override
	protected void nextWave(float wTime) {
		float np = pos - NYRAN[canid];
		new ContWaveCanon(new AttackWave(atk.attacker, atk, np, NYRAN[canid]), np, canid, (int)(maxt - t + wTime), waves);
	}

	@Override
	public boolean IMUTime() {
		return false;
	}
}
