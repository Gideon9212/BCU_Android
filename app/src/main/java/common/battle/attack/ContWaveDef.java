package common.battle.attack;

import common.CommonStatic;
import common.battle.entity.AbEntity;
import common.battle.entity.Entity;
import common.util.pack.EffAnim.DefEff;

import java.util.HashSet;
import java.util.Set;

public class ContWaveDef extends ContWaveAb {

	private boolean hit = false, nw = false, secall = true;

	protected ContWaveDef(AttackWave a, float p, int layer, float delay) {
		this(a, p, layer, delay, new HashSet<>());
	}

	private ContWaveDef(AttackWave a, float p, int layer, float delay, Set<ContWaveAb> waves) {
		super(a, p, (a.dire == 1 ? a.waveType == WT_MEGA ? effas().A_E_MEGAWAVE : a.waveType == WT_MINI ? effas().A_E_MINIWAVE : effas().A_E_WAVE
				: a.waveType == WT_MEGA ? effas().A_MEGAWAVE : a.waveType == WT_MINI ? effas().A_MINIWAVE : effas().A_WAVE).getEAnim(DefEff.DEF), layer, delay);
		soundEffect = SE_WAVE;

		maxt -= 1;
		anim.setTime(1);
		this.waves = waves;
		this.waves.add(this);

		if (t > 0)
			update(false);
	}

	public void update(boolean nini) {
		tempAtk = false;
		boolean isMini = atk.waveType == WT_MINI, isMega = atk.waveType == WT_MEGA;
		// guessed attack point compared from BC
		int attack = (isMini || isMega ? 4 : 6);
		// guessed wave block time compared from BC
		if (t >= 0 && secall) {
			secall = false;
			CommonStatic.setSE(soundEffect);
		}
		if (t <= attack) {
			atk.capture();
			for (AbEntity e : atk.capt)
				if (e instanceof Entity) {
					float waves = e.getProc().IMUWAVE.block;
					if (waves != 0) {
						if (waves > 0)
							((Entity)e).anim.getEff(STPWAVE);
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
		int wtime = isMega ? W_MEGA_TIME : isMini ? W_MINI_TIME : W_TIME;
		if (!nw && t >= wtime) {
			if ((isMini || isMega) && atk.proc.MINIWAVE.lv > this.waves.size())
				nextWave(wtime);
			else if (!(isMini || isMega) && atk.proc.WAVE.lv > this.waves.size())
				nextWave(wtime);
			nw = true;
		}
		if (!hit && t >= attack) {
			sb.getAttack(atk);
			tempAtk = hit = true;
		}
		if (maxt <= t)
			activate = false;
		updateAnimation();
		if (nini)
			t += atk.attacker == null ? atk.model.b.timeFlow : atk.attacker.getTimeFreeze();
	}

	@Override
	public void updateAnimation() {
		if (t >= 0)
			anim.update(false, atk.attacker == null ? atk.model.b.timeFlow : atk.attacker.getTimeFreeze());
	}

	@Override
	protected void nextWave(float wtime) {
		int dire = atk.model.getDire();
		float np = pos + W_PROG * dire;
		if ((atk.waveType == WT_WAVE && atk.proc.WAVE.inverted) || ((atk.waveType == WT_MINI || atk.waveType == WT_MEGA) && atk.proc.MINIWAVE.inverted))
			np = pos - W_PROG * dire;

		int wid = dire == 1 ? W_E_WID : W_U_WID;
		new ContWaveDef(new AttackWave(atk.attacker, atk, np, wid), np, layer, t - wtime, waves);
	}

	@Override
	public boolean IMUTime() {
		return atk.attacker != null && (atk.attacker.getAbi() & AB_TIMEI) != 0;
	}
}
