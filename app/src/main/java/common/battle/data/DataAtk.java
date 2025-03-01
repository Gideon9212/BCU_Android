package common.battle.data;

import common.util.Data.Proc;
import static common.util.Data.empty;

public class DataAtk implements MaskAtk {

	public final int index;
	public final DefaultData data;

	public DataAtk(DefaultData data, int index) {
		this.index = index;
		this.data = data;
	}

	@Override
	public int getAtk() {
		return data.atk[index];
	}

	@Override
	public int getPre() {
		if (index > 0)
			return data.pre[index] - data.pre[index - 1];
		return data.pre[0];
	}

	@Override
	public boolean canProc() {
		return data.abis[index];
	}

	@Override
	public boolean isLD() {
		if (index >= data.ldr.length)
			return data.ldr[0] > 0;
		return data.ldr[index] > 0;
	}

	@Override
	public boolean isOmni() {
		if (index >= data.ldr.length)
			return data.ldr[0] < 0;
		return data.ldr[index] < 0;
	}

	@Override
	public int getLongPoint() {
		if (index >= data.lds.length)
			return data.lds[0] + data.ldr[0];
		return data.lds[index] + data.ldr[index];
	}

	@Override
	public Proc getProc() {
		if (data.abis[index])
			return data.proc;
		return empty;
	}

	@Override
	public int getShortPoint() {
		if (index >= data.lds.length)
			return data.lds[0];
		return data.lds[index];
	}

	@Override
	public boolean isRange() {
		return data.isrange;
	}
}
