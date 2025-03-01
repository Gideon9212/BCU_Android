package common.util.anim;

import common.system.P;
import common.system.fake.FakeGraphics;
import common.util.BattleObj;

public abstract class EAnimI extends BattleObj {

	private static void sort(EPart[] arr, int low, int high) {
		if (low < high) {
			EPart pivot = arr[(low + high) / 2];
			int i = low - 1;
			int j = high + 1;
			while (i < j) {
				while (arr[++i].compareTo(pivot) < 0)
					;
				while (arr[--j].compareTo(pivot) > 0)
					;
				if (i >= j)
					break;

				EPart tmp = arr[i];
				arr[i] = arr[j];
				arr[j] = tmp;

			}
			sort(arr, low, j);
			sort(arr, j + 1, high);
		}
	}

	/**
	 * Outlines selected part. Only used in mamodel/maanim editor
	 */
	public int sele = -1;

	public EPart[] ent = null;
	protected final AnimI<?, ?> a;
	protected final MaModel mamodel;

	protected EPart[] order;

	public EAnimI(AnimI<?, ?> ia, MaModel mm) {
		a = ia;
		mamodel = mm;
		organize();
	}

	public AnimI<?, ?> anim() {
		return a;
	}

	public abstract void draw(FakeGraphics g, P ori, float siz);

	public abstract float ind();

	public abstract int len();

	public void organize() {
		ent = mamodel.arrange(this);
		order = new EPart[ent.length];
		for (int i = 0; i < ent.length; i++)
			order[i] = ent[i];
		sort();
	}

	public abstract void setTime(float value);

	public void update(boolean b) {
		update(b, 1);
	};

	public abstract void update(boolean rotate, float rate);

	@Override
	protected void performDeepCopy() {
		((EAnimI) copy).organize();
	}

	protected void sort() {
		sort(order, 0, order.length - 1);
	}

	@Override
	protected void terminate() {
		copy = null;
	}

	public EPart[] getOrder() {
		return order;
	}
}
