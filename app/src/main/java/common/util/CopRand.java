package common.util;

import java.util.Random;

public class CopRand extends BattleObj {

	private static final Random ir = new Random();
	private long seed;

	public CopRand(long s) {
		seed = s;
		ir.setSeed(new Random().nextLong());
	}

	public float irFloat() {
		return ir.nextFloat();
	}

	public int irInt(int bound) {
		if (bound <= 0)
			return ir.nextInt();
		return ir.nextInt(bound);
	}

	public float nextFloat() {
		Random r = new Random(seed);
		seed = r.nextLong();
		return r.nextFloat();
	}

	public int nextInt(int bound) {
		Random r = new Random(seed);
		seed = r.nextLong();
		if (bound <= 0)
			return r.nextInt();
		return r.nextInt(bound);
	}
}
