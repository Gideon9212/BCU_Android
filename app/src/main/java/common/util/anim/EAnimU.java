package common.util.anim;

import common.CommonStatic;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.anim.AnimU.UType;

public class EAnimU extends EAnimD<AnimU.UType> {

	private boolean flipped = false;
	protected EAnimU(AnimU<?> ani, UType i) {
		super(ani, ani.mamodel, ani.getMaAnim(i), i);
		type = i;
	}

	@Override
	public AnimU<?> anim() {
		return (AnimU<?>) a;
	}

	@Override
	public void changeAnim(AnimU.UType t, boolean skip) {
		boolean flip = flipped;
		flip(false);
		f = -1;
		ma = anim().getMaAnim(t);
		type = t;

		flip(flip);
		if (skip)
			setTime(0);
	}

	public void draw(FakeGraphics g, P ori, float siz, boolean flip) {
		flip(flip);
		draw(g, ori, siz);
	}

	public void setDir(boolean dir) {
		order[0].b.pvt = dir;
		flip(dir);
	}

	private void flip(boolean flip) {
		if (flip != flipped) {
			order[0].b.revert();
			flipped = flip;
		}
	}

	@Override
	public void draw(FakeGraphics g, P ori, float siz) {
		if (f == -1) {
			f = 0;
			setup();
		}
		set(g);
		FakeTransform at = g.getTransform();
		g.translate(ori.x, ori.y);
		if (CommonStatic.getConfig().ref && !CommonStatic.getConfig().battle) {
			P p0 = P.newP(-200, 0).times(siz);
			P p1 = P.newP(400, 100).times(siz);
			P p2 = P.newP(0, -300).times(siz);
			g.drawRect((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
			g.setColor(FakeGraphics.RED);
			g.drawLine(0, 0, (int) p2.x, (int) p2.y);

			P.delete(p0);
			P.delete(p1);
			P.delete(p2);
		}
		for (EPart e : order) {
			P p = P.newP(siz, siz);
			e.drawPart(g, p);
			P.delete(p);
		}
		if (sele >= 0 && sele < ent.length) {
			P p = P.newP(siz, siz);
			ent[sele].drawScale(g, p);
			P.delete(p);
		}

		g.setTransform(at);
		g.delete(at);
	}

	public void paraTo(EAnimD<?> base, boolean flip) {
		flip(flip);
		super.paraTo(base);
	}

	@Override
	protected void performDeepCopy() {
		super.performDeepCopy();
		((EAnimU)copy).flipped = false;
		((EAnimU)copy).setDir(order[0].b.pvt);
	}

	/**
	 * Similar to default paraTo, but allows to select the model part to connect to
	 * Used solely for Everywhere Door. Not null-safe, use regular one for that
	 */
	public void paraTo(EAnimD<?> base, int bas) {
		ent[0].setPara(base.ent[bas]);
	}
}
