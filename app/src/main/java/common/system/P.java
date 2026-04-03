package common.system;

import common.io.assets.Admin.StaticPermitted;
import common.util.BattleObj;

import java.util.ArrayDeque;
import java.util.Deque;

public strictfp class P extends BattleObj {

	@StaticPermitted(StaticPermitted.Type.TEMP)
	public static Deque<P> stack = new ArrayDeque<>();

	public static void delete(P p) {
		stack.add(p);
	}

	public static synchronized P newP(float x, float y) {
		if (!stack.isEmpty()) {
			P p = stack.pollFirst();
			if (p == null)
				return new P(x, y);

			return p.setTo(x, y);
		}

		return new P(x, y);
	}

	public static synchronized P newP(P p) {
		if (!stack.isEmpty()) {
			P p1 = stack.pollFirst();

			if (p1 == null)
				return new P(p.x, p.y);

			return p1.setTo(p.x, p.y);
		}

		return new P(p.x, p.y);
	}

	public static P polar(float r, float t) {
		return new P((float) (r * Math.cos(t)), (float) (r * Math.sin(t)));
	}

	public static float reg(float cx) {
		if (cx < 0)
			return 0;
		if (cx > 1)
			return 1;
		return cx;
	}

	public float x, y;

	public P(float X, float Y) {
		x = X;
		y = Y;
	}

	public float abs() {
		return dis(new P(0, 0));
	}

	public float atan2() {
		return (float) Math.atan2(y, x);
	}

	public float atan2(P p) {
		return sf(p).atan2();
	}

	public P copy() {
		return new P(x, y);
	}

	public float crossP(P p) {
		return x * p.y - y * p.x;
	}

	public float dis(P p) {
		return (float) Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2));
	}

	public P divide(P p) {
		x /= p.x;
		y /= p.y;
		return this;
	}

	public float dotP(P p) {
		return x * p.x + y * p.y;
	}

	@Override
	public boolean equals(Object obj) {
		if (P.class.isAssignableFrom(obj.getClass())) {
			P i = (P) obj;
			return Math.abs(i.x - x) + Math.abs(i.y - y) < 1e-10;
		}
		return false;
	}

	public boolean limit(P b2) {
		return limit(new P(0, 0), b2);
	}

	public boolean limit(P b1, P b2) {
		boolean ans = out(b1, b2, 0);
		if (x < b1.x)
			x = b1.x;
		if (x > b2.x)
			x = b2.x;
		if (y < b1.y)
			y = b1.y;
		if (y > b2.y)
			y = b2.y;
		return ans;
	}

	public P middle(P p, float per) {
		return copy().plus(sf(p), per);
	}

	public P middleC(P p, float per) {
		return copy().plus(sf(p), (float) ((1 - Math.cos(Math.PI * per)) / 2));
	}

	public boolean moveOut(P v, P b2, float r) {
		return moveOut(v, new P(0, 0), b2, r);
	}

	public boolean moveOut(P v, P b1, P b2, float r) {
		return x + r < b1.x && v.x <= 0 || y + r < b1.y && v.y <= 0 || x - r > b2.x && v.x >= 0
				|| y - r > b2.y && v.y >= 0;
	}

	public boolean out(int[] rect, float sca, float r) {
		P p0 = new P(rect[0], rect[1]);
		P p1 = new P(rect[2], rect[3]).plus(p0);
		p0.times(sca);
		p1.times(sca);
		return out(p0, p1, r);
	}

	public boolean out(P b2, float r) {
		return out(new P(0, 0), b2, r);
	}

	public boolean out(P b1, P b2, float r) {
		return x + r < b1.x || y + r < b1.y || x - r > b2.x || y - r > b2.y;
	}

	public P plus(float px, float py) {
		x += px;
		y += py;
		return this;
	}

	public P plus(P p) {
		x += p.x;
		y += p.y;
		return this;
	}

	public P plus(P p, float n) {
		x += p.x * n;
		y += p.y * n;
		return this;
	}

	public P positivize() {
		if (x < 0)
			x = -x;
		if (y < 0)
			y = -y;
		return this;
	}

	public P rotate(float t) {
		return setTo((float) (x * Math.cos(t) - y * Math.sin(t)), (float) (y * Math.cos(t) + x * Math.sin(t)));
	}

	public P setTo(float tx, float ty) {
		x = tx;
		y = ty;
		return this;
	}

	/* return this */
	public P setTo(P p) {
		x = p.x;
		y = p.y;
		return this;
	}

	public P sf(P p) {
		return substractFrom(p);
	}

	public P substractFrom(P p) {
		return new P(p.x - x, p.y - y);
	}

	public P times(float d) {
		x *= d;
		y *= d;
		return this;
	}

	public P times(float hf, float vf) {
		x *= hf;
		y *= vf;
		return this;
	}

	public P times(P p) {
		x *= p.x;
		y *= p.y;
		return this;
	}

	@Override
	public String toString() {
		return x + "," + y;
	}

}
