package common.battle.attack;

import common.CommonStatic;
import common.CommonStatic.BattleConst;
import common.system.P;
import common.system.fake.FakeGraphics;

public class ContMove extends ContAb {

	private final int itv, move, ran, rep;
	private float t, rem, rept;
	private final AttackWave atk;
	private boolean tempAtk;

	/**
	 * conf: range, move, itrv, tot, rept,layer
	 */
	public ContMove(AttackSimple as, float p, int... conf) {
		super(as.model.b, p, conf[5]);
		move = conf[1];
		itv = conf[2];
		t = itv;
		rem = conf[3];
		ran = conf[0];
		rep = conf[4];
		rept = rep > 0 ? rep : -1;
		atk = new AttackWave(as.attacker, as, 0, 0, WT_MOVE);
	}

	@Override
	public void draw(FakeGraphics gra, P p, float siz) {
		if (!CommonStatic.getConfig().ref)
			return;

		// after this is the drawing of hit boxes
		siz *= 1.25;
		float rat = BattleConst.ratio;
		int h = (int) (640 * rat * siz);
		gra.setColor(FakeGraphics.MAGENTA);
		float d0 = -ran / 2f;
		int x = (int) (d0 * rat * siz + p.x);
		int y = (int) p.y;
		int w = (int) ((float) ran * rat * siz);
		if (tempAtk)
			gra.fillRect(x, y, w, h);
		else
			gra.drawRect(x, y, w, h);
	}

	@Override
	public void update() {
		tempAtk = false;
		float time = atk.attacker == null ? atk.model.b.timeFlow : atk.attacker.getTimeFreeze();
		t -= time;
		if (rept > 0)
			rept -= time;
		if (t <= 0) {
			rem -= time;
			if (rem > 0)
				t = itv;
			else
				activate = false;
			pos += move * atk.model.getDire() * time;
			if (rept <= 0) {
				atk.incl.clear();
				rept = rep;
			}
			sb.getAttack(new AttackWave(atk.attacker, atk, pos, ran));
			tempAtk = true;
		}
	}

	@Override
	public boolean IMUTime() {
		return atk.attacker != null && (atk.attacker.getAbi() & AB_TIMEI) != 0;
	}
}
