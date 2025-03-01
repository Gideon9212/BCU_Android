package common.util;

import java.util.Random;

public class CopRand extends BattleObj {

	private long seed;

	public CopRand(long s) {
		seed = s;
	}

	public double irDouble() {
		return Math.random();
	}

	public float irFloat() {
		return new Random().nextFloat();
	}

	public double nextDouble() {
		Random r = new Random(seed);
		seed = r.nextLong();
		return r.nextFloat();
	}

	public float nextFloat() {
		Random r = new Random(seed);
		seed = r.nextLong();
		return r.nextFloat();
	}

}
