package common.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public strictfp interface InStream {

	default byte[] nextBytesB() {
		int len = nextByte();
		byte[] ints = new byte[len];
		for (int i = 0; i < len; i++)
			ints[i] = (byte) nextByte();
		return ints;
	}

	default int[] nextIntsB() {
		int len = nextByte();
		int[] ints = new int[len];
		for (int i = 0; i < len; i++)
			ints[i] = nextInt();
		return ints;
	}

	default int[][] nextIntsBB() {
		int len = nextByte();
		int[][] ints = new int[len][];
		for (int i = 0; i < len; i++)
			ints[i] = nextIntsB();
		return ints;
	}

	default String nextString() {
		byte[] bts = nextBytesB();
		return new String(bts, StandardCharsets.UTF_8);
	}

	boolean end();

	int nextByte();

	double nextDouble();

	int nextInt();

	int nextShort();

	OutStream translate();

	default void close() throws IOException {

	}
}

strictfp class InStreamDef extends DataIO implements InStream {

	private final int[] bs;
	private final int off, max;
	private int index;

	InStreamDef(int[] data, int ofs, int m) {
		bs = data;
		off = ofs;
		max = m;
		index = off;
	}

	@Override
	public boolean end() {
		return index == max;
	}

	@Override
	public int nextByte() {
		check(1);
		int ans = toByte(bs, index);
		index++;
		return ans;
	}

	@Override
	public double nextDouble() {
		check(8);
		double ans = toDouble(bs, index);
		index += 8;
		return ans;
	}

	@Override
	public int nextInt() {
		check(4);
		int ans = toInt(bs, index);
		index += 4;
		return ans;
	}

	@Override
	public int nextShort() {
		check(2);
		int ans = toShort(bs, index);
		index += 2;
		return ans;
	}

	public int pos() {
		return index - off;
	}

	public int size() {
		return max - off;
	}

	@Override
	public String toString() {
		return "InStreamDef " + size();
	}

	@Override
	public OutStreamDef translate() {
		byte[] data = new byte[max - index];
		for (int i = 0; i < max - index; i++)
			data[i] = (byte) bs[index + i];
		return new OutStreamDef(data);
	}

	protected int[] getBytes() {
		return bs;
	}

	private void check(int i) {
		if (max - index < i) {
			String str = "out of bound: " + (index - off) + "/" + (max - off) + ", " + index + "/" + max + "/" + off
					+ "/" + bs.length;
			throw new BCUException(str);
		}
	}

}

strictfp class InStreamAnim extends DataIO implements InStream {

	private final int[] bs;
	private final int off, max;
	private int index;

	InStreamAnim(int[] data, int ofs, int m) {
		bs = data;
		off = ofs;
		max = m;
		index = off;
	}

	@Override
	public boolean end() {
		return index == max;
	}

	@Override
	public int nextByte() {
		check(1);
		int ans = toByte(bs, index);
		index++;
		return ans;
	}

	@Override
	public double nextDouble() {
		check(8);
		double ans = toDouble(bs, index);
		index += 8;
		return ans;
	}

	@Override
	public int nextInt() {
		check(4);
		int ans = toInt(bs, index);
		index += 4;
		return ans;
	}

	@Override
	public int nextShort() {
		check(2);
		int ans = toShort(bs, index);
		index += 2;
		return ans;
	}

	public int pos() {
		return index - off;
	}

	public int size() {
		return max - off;
	}

	@Override
	public String toString() {
		return "InStreamDef " + size();
	}

	@Override
	public OutStreamDef translate() {
		byte[] data = new byte[max - index];
		for (int i = 0; i < max - index; i++)
			data[i] = (byte) bs[index + i];
		return new OutStreamDef(data);
	}

	@Override
	public byte[] nextBytesB() {
		int len = nextInt();
		byte[] ints = new byte[len];
		for (int i = 0; i < len; i++)
			ints[i] = (byte) nextByte();
		return ints;
	}

	@Override
	public int[] nextIntsB() {
		int len = nextInt();
		int[] ints = new int[len];
		for (int i = 0; i < len; i++)
			ints[i] = nextInt();
		return ints;
	}

	@Override
	public int[][] nextIntsBB() {
		int len = nextInt();
		int[][] ints = new int[len][];
		for (int i = 0; i < len; i++)
			ints[i] = nextIntsB();
		return ints;
	}

	@Override
	public String nextString() {
		byte[] bts = nextBytesB();
		return new String(bts, StandardCharsets.UTF_8);
	}

	protected int[] getBytes() {
		return bs;
	}

	private void check(int i) {
		if (max - index < i) {
			String str = "out of bound: " + (index - off) + "/" + (max - off) + ", " + index + "/" + max + "/" + off
					+ "/" + bs.length;
			throw new BCUException(str);
		}
	}
}
