package common.battle.data;

public abstract class DefaultData extends DataEntity {

	public Proc proc;
	protected int[] lds = new int[1], ldr = new int[1];
	protected int[] atk = new int[1], pre = new int[1];
	protected boolean[] abis = new boolean[]{true};
	protected DataAtk[] datks;

	public boolean isrange;

	@Override
	public int allAtk(int ignore) {
		int ans = 0;
		for (int a : atk)
			ans += a;
		return ans;
	}

	@Override
	public Proc getAllProc() {
		return proc;
	}

	@Override
	public int getAtkCount(int ignore) {
		return atk.length;
	}

	@Override
	public MaskAtk getAtkModel(int ignore, int ind) {
		if (ind >= getAtkCount(0) || datks == null || ind >= datks.length)
			return null;
		return datks[ind];
	}

	@Override
	public MaskAtk[] getAtks(int ignore) {
		return datks;
	}

	@Override
	public int getItv(int ignore) {
		return getLongPre() + Math.max(getTBA() - 1, getPost(false, 0));
	}

	@Override
	public int getPost(boolean ignore, int ignore2) {
		return getAnimLen(0) - getLongPre();
	}

	@Override
	public Proc getProc() {
		return proc;
	}

	@Override
	public MaskAtk getRepAtk() {
		return datks[0];
	}

	@Override
	public int getTBA() {
		return tba * 2;
	}

	@Override
	public boolean isLD() {
		for (int ld : ldr)
			if (ld <= 0)
				return false;
		return true;
	}

	@Override
	public boolean isOmni() {
		for (int ld : ldr)
			if (ld < 0)
				return true;
		return false;
	}

	@Override
	public boolean isRange(int ignore) {
		return isrange;
	}

	@Override
	public int touchBase() {
		return lds[0] > 0 ? lds[0] : range;
	}

	protected int getLongPre() {
		return pre[pre.length - 1];
	}

	@Override
	public boolean isCommon() {
		for (boolean abi : abis) {
			if (!abi)
				return false;
		}
		return true;
	}

}
