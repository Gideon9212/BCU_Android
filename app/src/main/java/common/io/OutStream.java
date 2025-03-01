package common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public strictfp interface OutStream {

	static OutStream getIns() {
		return new OutStreamDef();
	}

	static OutStream getAnimIns() {
		return new OutStreamAnim();
	}

	void accept(OutStream os);

	void flush(OutputStream fos) throws IOException;

	byte[] MD5();

	void terminate();

	InStream translate();

	void writeByte(byte n);

	void writeBytesB(byte[] s);

	void writeBytesI(byte[] s);

	void writeDouble(double n);

	void writeDoubles(double[] ints);

	void writeFloat(double n);

	void writeFloat(float n);

	void writeInt(int n);

	void writeIntB(int[] ints);

	void writeIntBB(int[][] ints);

	default void writeIntsN(int... ns) {
		for (int i : ns)
			writeInt(i);
	}

	void writeLong(long n);

	void writeShort(short n);

	void writeString(String str);

}

strictfp class OutStreamDef extends DataIO implements OutStream {

	private byte[] bs;
	private int index;

	public OutStreamDef() {
		bs = new byte[1024];
	}

	public OutStreamDef(int size) {
		bs = new byte[size];
		index = 0;
	}

	protected OutStreamDef(byte[] data) {
		bs = data;
		index = bs.length;
	}

	@Override
	public void accept(OutStream os) {
		if (!(os instanceof OutStreamDef))
			throw new BCUException("OutStream type mismatch");
		os.terminate();
		byte[] obs = ((OutStreamDef) os).bs;
		if (obs.length == 0)
			throw new BCUException("zero stream");
		writeInt(obs.length);
		check(obs.length);
		for (int i = 0; i < obs.length; i++)
			bs[index++] = obs[i];
	}

	public void concat(byte[] s) {
		check(s.length);
		for (byte b : s)
			fromByte(bs, index++, b);
	}

	@Override
	public void flush(OutputStream fos) throws IOException {
		terminate();
		fos.write(getSignature(bs.length));// signature
		fos.write(getBytes());
	}

	public byte[] getBytes() {
		return bs;
	}

	@Override
	public byte[] MD5() {
		try {
			MessageDigest mdi = MessageDigest.getInstance("MD5");
			mdi.update(getSignature(bs.length));
			return mdi.digest(bs);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int pos() {
		return index;
	}

	@Override
	public void terminate() {
		if (index == bs.length)
			return;
		bs = Arrays.copyOf(bs, index);
	}

	@Override
	public String toString() {
		return "OutStreamDef " + size();
	}

	@Override
	public InStreamDef translate() {
		return new InStreamDef(translate(bs), 0, bs.length);
	}

	@Override
	public void writeByte(byte n) {
		check(1);
		fromByte(bs, index, n);
		index++;
	}

	@Override
	public void writeBytesB(byte[] s) {
		check(s.length + 1);
		writeByte((byte) s.length);
		for (byte b : s)
			writeByte(b);
	}

	@Override
	public void writeBytesI(byte[] s) {
		writeInt(s.length);
		check(s.length);
		for (byte b : s)
			writeByte(b);
	}

	@Override
	public void writeDouble(double n) {
		check(8);
		fromDouble(bs, index, n);
		index += 8;
	}

	@Override
	public void writeDoubles(double[] ints) {
		if (ints == null) {
			writeByte((byte) 0);
			return;
		}
		writeByte((byte) ints.length);
		for (double i : ints)
			writeDouble(i);
	}

	@Override
	public void writeFloat(double n) {
		writeFloat((float) n);
	}

	@Override
	public void writeFloat(float n) {
		check(4);
		fromFloat(bs, index, n);
		index += 4;
	}

	@Override
	public void writeInt(int n) {
		check(4);
		fromInt(bs, index, n);
		index += 4;
	}

	@Override
	public void writeIntB(int[] ints) {
		if (ints == null) {
			writeByte((byte) 0);
			return;
		}
		writeByte((byte) ints.length);
		for (int i : ints)
			writeInt(i);
	}

	@Override
	public void writeIntBB(int[][] ints) {
		if (ints == null) {
			writeByte((byte) 0);
			return;
		}
		writeByte((byte) ints.length);
		for (int[] i : ints)
			writeIntB(i);
	}

	@Override
	public void writeLong(long n) {
		check(8);
		fromLong(bs, index, n);
		index += 8;
	}

	@Override
	public void writeShort(short n) {
		check(2);
		fromShort(bs, index, n);
		index += 2;
	}

	@Override
	public void writeString(String str) {
		byte[] bts;
		bts = str.getBytes(StandardCharsets.UTF_8);
		writeBytesB(bts);
	}

	protected int size() {
		return bs.length;
	}

	private void check(int i) {
		if (index + i > bs.length * 2)
			bs = Arrays.copyOf(bs, index + i);
		else if (index + i > bs.length)
			bs = Arrays.copyOf(bs, bs.length * 2);
	}

}

strictfp class OutStreamAnim extends DataIO implements OutStream {

	private byte[] bs;
	private int index;

	public OutStreamAnim() {
		bs = new byte[1024];
	}

	@Override
	public void accept(OutStream os) {
		if (!(os instanceof OutStreamAnim))
			throw new BCUException("OutStream type mismatch");
		os.terminate();
		byte[] obs = ((OutStreamAnim) os).bs;
		if (obs.length == 0)
			throw new BCUException("zero stream");
		writeInt(obs.length);
		check(obs.length);
		for (int i = 0; i < obs.length; i++)
			bs[index++] = obs[i];
	}

	public void concat(byte[] s) {
		check(s.length);
		for (byte b : s)
			fromByte(bs, index++, b);
	}

	@Override
	public void flush(OutputStream fos) throws IOException {
		terminate();
		fos.write(getSignature(bs.length));// signature
		fos.write(getBytes());
	}

	public byte[] getBytes() {
		return bs;
	}

	@Override
	public byte[] MD5() {
		try {
			MessageDigest mdi = MessageDigest.getInstance("MD5");
			mdi.update(getSignature(bs.length));
			return mdi.digest(bs);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int pos() {
		return index;
	}

	@Override
	public void terminate() {
		if (index == bs.length)
			return;
		bs = Arrays.copyOf(bs, index);
	}

	@Override
	public String toString() {
		return "OutStreamDef " + size();
	}

	@Override
	public InStreamAnim translate() {
		return new InStreamAnim(translate(bs), 0, bs.length);
	}

	@Override
	public void writeByte(byte n) {
		check(1);
		fromByte(bs, index, n);
		index++;
	}

	@Override
	public void writeBytesB(byte[] s) {
		check(s.length + 1);
		writeInt(s.length);
		for (byte b : s)
			writeByte(b);
	}

	@Override
	public void writeBytesI(byte[] s) {
		writeInt(s.length);
		check(s.length);
		for (byte b : s)
			writeByte(b);
	}

	@Override
	public void writeDouble(double n) {
		check(8);
		fromDouble(bs, index, n);
		index += 8;
	}

	@Override
	public void writeDoubles(double[] ints) {
		if (ints == null) {
			writeInt(0);
			return;
		}
		writeInt(ints.length);
		for (double i : ints)
			writeDouble(i);
	}

	@Override
	public void writeFloat(double n) {
		writeFloat((float) n);
	}

	@Override
	public void writeFloat(float n) {
		check(4);
		fromFloat(bs, index, n);
		index += 4;
	}

	@Override
	public void writeInt(int n) {
		check(4);
		fromInt(bs, index, n);
		index += 4;
	}

	@Override
	public void writeIntB(int[] ints) {
		if (ints == null) {
			writeInt(0);
			return;
		}
		writeInt(ints.length);
		for (int i : ints)
			writeInt(i);
	}

	@Override
	public void writeIntBB(int[][] ints) {
		if (ints == null) {
			writeInt( 0);
			return;
		}
		writeInt(ints.length);
		for (int[] i : ints)
			writeIntB(i);
	}

	@Override
	public void writeLong(long n) {
		check(8);
		fromLong(bs, index, n);
		index += 8;
	}

	@Override
	public void writeShort(short n) {
		check(2);
		fromShort(bs, index, n);
		index += 2;
	}

	@Override
	public void writeString(String str) {
		byte[] bts;
		bts = str.getBytes(StandardCharsets.UTF_8);
		writeBytesB(bts);
	}

	protected int size() {
		return bs.length;
	}

	private void check(int i) {
		if (index + i > bs.length * 2)
			bs = Arrays.copyOf(bs, index + i);
		else if (index + i > bs.length)
			bs = Arrays.copyOf(bs, bs.length * 2);
	}

}