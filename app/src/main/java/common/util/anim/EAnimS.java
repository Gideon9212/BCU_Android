package common.util.anim;

import common.CommonStatic;
import common.system.P;
import common.system.fake.FakeGraphics;

public class EAnimS extends EAnimI {

	public EAnimS(AnimI<?, ?> ia, MaModel mm) {
		super(ia, mm);
	}

	@Override
	public void draw(FakeGraphics g, P ori, float siz) {
		set(g);
		g.translate(ori.x, ori.y);
		if (CommonStatic.getConfig().ref && !CommonStatic.getConfig().battle)
			drawAxis(g, siz);
		visualizeRange(g, siz);
		for (EPart e : order)
			e.drawPart(g, new P(siz, siz));
		if (sele >= 0 && sele < ent.length) {
			g.setColor(FakeGraphics.RED);
			ent[sele].drawScale(g, new P(siz, siz));
		}
	}

	@Override
	public float ind() {
		return 0;
	}

	@Override
	public int len() {
		return 0;
	}

	@Override
	public void setTime(float value) {
	}

	@Override
	public void update(boolean rotate, float rate) {
	}

}
