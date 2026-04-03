package common.util.anim;

import common.CommonStatic;
import common.system.P;
import common.system.fake.FakeGraphics;

public class EAnimD<T extends AnimI.AnimType<?, T>> extends EAnimI {

	public T type;

	protected MaAnim ma;

	public float f = -1f;

	public EAnimD(AnimI<?, T> ia, MaModel mm, MaAnim anim, T t) {
		super(ia, mm);
		ma = anim;
		type = t;
	}

	@SuppressWarnings("unchecked")
	public void changeAnim(T t, boolean skip) {
		f = -1;
		ma = ((AnimD<?, T>) anim()).getMaAnim(t);
		type = t;
		if (skip)
			setTime(0);
	}

	public boolean done() {
		return f >= ma.max;
	}

	@Override
	public void draw(FakeGraphics g, P ori, float siz) {
		if (f == -1) {
			f = 0;
			setup();
		}
		set(g);
		g.translate(ori.x, ori.y);
		for (EPart e : order) {
			P p = P.newP(siz, siz);
			e.drawPart(g, p);
			P.delete(p);
		}
	}

	/**
	 * Draw parts with specific opacity and size
	 * @param g Graphic
	 * @param ori Position
	 * @param siz Size
	 * @param opacity Opacity, range is 0 ~ 255
	 */
	public void drawBGEffect(FakeGraphics g, P ori, float siz, int opacity, float sizX, float sizY) {
		if(f == -1) {
			f = 0;
			setup();
		}
		set(g);
		g.translate(ori.x, ori.y);
		if(CommonStatic.getConfig().ref) {
			g.colRect(0, 0, 2, 2, 0, 255, 0, 255);
		}
		for (EPart ePart : order) {
			P p = P.newP(siz * sizX, siz * sizY);
			ePart.drawPartWithOpacity(g, p, opacity);
			P.delete(p);
		}
	}

	public float getBaseSizeX() {
		return mamodel.parts[0][8] * 1f / mamodel.ints[0];
	}

	public float getBaseSizeY() {
		return mamodel.parts[0][9] * 1f / mamodel.ints[0];
	}

	public void removeBasePivot() {
		mamodel.parts[0][6] = 0;
		mamodel.parts[0][7] = 0;
	}

	@Override
	public float ind() {
		return f;
	}

	@Override
	public int len() {
		return ma.max + 1;
	}

	public boolean unusable() {
		return ma == null || ma.n == 0;
	}

	@Override
	public void setTime(float value) {
		setTime(value, true);
	}

	public void setTime(float value, boolean rot) {
		setup();
		f = value;
		ma.update(f, this, rot);
	}

	public void setup() {
		ma.update(0, this, false);
	}

	@Override
	public void update(boolean rotate, float rate) {
		if (f == -1)
			rate = Math.max(rate, 1);
		else if (CommonStatic.getConfig().fps60)
			rate *= 0.5f;
		f += rate;

		ma.update(f, this, rotate);
	}

	@Override
	protected void performDeepCopy() {
		super.performDeepCopy();
		((EAnimD<?>) copy).setTime(f);
	}

	/**
	 * make this animation a component of another, used in warp and kb
	 */
	public void paraTo(EAnimD<?> base) {
		if (base == null)
			ent[0].setPara(null);
		else
			ent[0].setPara(base.ent[1]);
	}
}
