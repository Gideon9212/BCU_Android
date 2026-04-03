package common.io;

import java.util.function.BiFunction;
import common.pack.Context;

public strictfp abstract class DataIO {

	/**
	 * write a number n into a byte[] start from index
	 */

	public static void fromByte(byte[] b, int index, byte n) {
		b[index] = n;
	}

	public static void fromDouble(byte[] b, int index, double n) {
		long l = Double.doubleToLongBits(n);
		fromLong(b, index, l);
	}

	public static void fromFloat(byte[] b, int index, float n) {
		int l = Float.floatToIntBits(n);
		fromInt(b, index, l);
	}

	public static void fromInt(byte[] b, int index, int n) {
		for (int i = 0; i < 4; i++)
			b[index + i] = (byte) (n >> (i * 8) & 0xff);
	}

	public static void fromLong(byte[] b, int index, long n) {
		for (int i = 0; i < 8; i++)
			b[index + i] = (byte) (n >> (i * 8) & 0xff);
	}

	public static void fromShort(byte[] b, int index, short n) {
		for (int i = 0; i < 2; i++)
			b[index + i] = (byte) (n >> (i * 8) & 0xff);
	}

	public static byte[] getSignature(int n) {
		byte[] ans = new byte[4];
		fromInt(ans, 0, n);
		return ans;
	}

	public static <T> T readData(Context.SupExc<Integer> con, int n, BiFunction<int[], Integer, T> func)
			throws Exception {
		int[] data = new int[n];
		for (int i = 0; i < n; i++)
			data[i] = con.get();
		return func.apply(data, 0);
	}

	public static int readInt(Context.SupExc<Integer> con) throws Exception {
		int[] data = new int[4];
		for (int i = 0; i < 4; i++)
			data[i] = con.get();
		return toInt(data, 0);
	}

	/**
	 * read a number from a byte[] start from index
	 */

	public static int toByte(int[] datas, int index) {
		return datas[index];
	}

	public static double toDouble(int[] datas, int index) {
		return Double.longBitsToDouble(toLong(datas, index));
	}

	public static float toFloat(int[] datas, int index) {
		return Float.intBitsToFloat(toInt(datas, index));
	}

	public static int toInt(byte[] datas, int index) {
		int ans = 0;
		for (int i = 0; i < 4; i++)
			ans += (datas[index + i] & 0xff) << (i * 8);
		return ans;
	}

	public static int toInt(int[] datas, int index) {
		int ans = 0;
		for (int i = 0; i < 4; i++)
			ans += (datas[index + i]) << (i * 8);
		return ans;
	}

	public static long toLong(int[] datas, int index) {
		long l = 0;
		for (int i = 0; i < 8; i++) {
			long temp = datas[index + i];
			temp <<= i * 8;
			l += temp;
		}
		return l;
	}

	public static int toShort(int[] datas, int index) {
		short ans = 0;
		for (int i = 0; i < 2; i++)
			ans += (datas[index + i]) << (i * 8);
		return ans;
	}

	/**
	 * translate byte[] to int[] in order to make all the bytes unsigned
	 */

	public static int[] translate(byte[] datas) {
		int[] ans = new int[datas.length];
		for (int i = 0; i < ans.length; i++)
			ans[i] = (datas[i]) & 0xff;
		return ans;
	}

	public static byte[] translate(int[] datas) {
		byte[] ans = new byte[datas.length];
		for (int i = 0; i < ans.length; i++)
			ans[i] = (byte) datas[i];
		return ans;
	}

}
