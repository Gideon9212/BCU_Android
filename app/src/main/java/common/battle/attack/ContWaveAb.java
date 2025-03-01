package common.battle.attack;

import common.CommonStatic;
import common.CommonStatic.BattleConst;
import common.battle.entity.AbEntity;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.anim.EAnimD;

import java.util.Set;

public abstract class ContWaveAb extends ContAb {

	protected final AttackWave atk;
	protected final EAnimD<?> anim;
	protected Set<ContWaveAb> waves;
	protected int soundEffect;
	protected float t;
	protected int maxt;
	protected boolean tempAtk;

	protected ContWaveAb(AttackWave a, float p, EAnimD<?> ead, int layer, float delay) {
		super(a.model.b, p, layer);
		atk = a;
		anim = ead;
		maxt = anim.len();
		t = delay;
	}

	@Override
	public void update() {
		update(true);
	}

	public abstract void update(boolean nonini);

	@Override
	public void draw(FakeGraphics gra, P p, float siz) {
		if (t < 0)
			return;
		FakeTransform at = gra.getTransform();
		anim.draw(gra, p, siz);
		gra.setTransform(at);
		drawAxis(gra, p, siz);
		gra.delete(at);
	}

	/**
	 * kill every related wave
	 */
	protected void deactivate(AbEntity e) {
		if (e != null && e.getProc().IMUWAVE.mult < 0) {
			e.getProc().IMUWAVE.mult += 100;
			e.damaged(atk);
			e.getProc().IMUWAVE.mult -= 100;
		}
		waves.forEach(w -> w.activate = false);
		activate = false;
	}

	protected void drawAxis(FakeGraphics gra, P p, float siz) {
		if (!CommonStatic.getConfig().ref)
			return;

		// after this is the drawing of hit boxes
		siz *= 1.25;
		float rat = BattleConst.ratio;
		int h = (int) (640 * rat * siz);
		gra.setColor(FakeGraphics.MAGENTA);
		float d0 = Math.min(atk.sta, atk.end);
		float ra = Math.abs(atk.sta - atk.end);
		int x = (int) ((d0 - pos) * rat * siz + p.x);
		int y = (int) p.y;
		int w = (int) (ra * rat * siz);
		if (tempAtk)
			gra.fillRect(x, y, w, h);
		else
			gra.drawRect(x, y, w, h);
	}

	/**
	 * generate the next wave container
	 */
	protected abstract void nextWave(float delay);

}
