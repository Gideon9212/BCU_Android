package common.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import common.CommonStatic;
import common.io.PackLoader.ZipDesc.FileDesc;
import common.io.assets.Admin.StaticPermitted;
import common.io.assets.AssetLoader.AssetHeader;
import common.io.assets.AssetLoader.AssetHeader.AssetEntry;
import common.io.assets.MultiStream;
import common.io.assets.MultiStream.ByteStream;
import common.io.json.JsonClass;
import common.io.json.JsonClass.JCConstructor;
import common.io.json.JsonClass.RType;
import common.io.json.JsonDecoder;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonEncoder;
import common.io.json.JsonField;
import common.io.json.JsonField.GenType;
import common.pack.Context;
import common.pack.Context.ErrType;
import common.pack.PackData;
import common.system.fake.FakeImage;
import common.system.files.FileData;
import common.system.files.VFileRoot;
import common.util.Data;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

@StaticPermitted
public class PackLoader {

	public interface PatchFile {

		File getFile(String path) throws Exception;

	}

	public interface Preload {

		boolean preload(FileDesc fd);

	}

	public interface Preloader {

		Preload getPreload(ZipDesc desc);

	}

	@JsonClass(read = RType.FILL)
	public static class ZipDesc {

		@JsonClass
		public static class FileDesc implements FileData {

			@JsonField
			public String path;

			@JsonField
			public int size;

			@JsonField
			private int offset;

			private File file; // writing only
			private ZipDesc pack; // reading only

			public FileDesc(FileSaver parent, String path, File f) {
				this.path = path;
				file = f;
				size = (int) f.length();
				offset = parent.len;
				parent.len += regulate(size);
			}

			@Deprecated
			@JCConstructor
			public FileDesc(ZipDesc desc) {
				pack = desc;
			}

			@Override
			public FakeImage getImg() {
				return FakeImage.read(this);
			}

			@Override
			public InputStream getStream() {
				return pack.readFile(path);
			}

			@Override
			public Queue<String> readLine() {
				return FileData.super.readLine();
			}

			@Override
			public int size() {
				return size;
			}

			@Override
			public String getPath() {
				return path;
			}

		}

		@JsonField
		public PackData.PackDesc desc;

		@JsonField(gen = GenType.GEN)
		public FileDesc[] files;

		public VFileRoot tree = new VFileRoot(".");

		private int offset;
		private FileLoader loader;

		public ZipDesc(PackData.PackDesc pd, FileDesc[] fs) {
			desc = pd;
			files = fs;
		}

		private ZipDesc(FileLoader fl, int off) {
			loader = fl;
			offset = off;
		}

		public void delete() {
			if (!loader.file.delete())
				System.out.println("Failed to delete file : " + loader.file.getAbsolutePath());
		}

		public File getZipFile() {
			return loader.file;
		}

		public boolean match(byte[] data) {
			return Arrays.equals(data, loader.key);
		}

		public boolean matchKey(String key) {
			return match(getMD5(key.getBytes(), 16));
		}

		@OnInjected
		public void onInjected() {
			for (FileDesc fd : files)
				tree.build(fd.path, fd);
		}

		public InputStream readFile(String path) {
			FileDesc fd = (FileDesc) tree.find(path).getData();
			return CommonStatic.ctx.noticeErr(() -> new FileLoader.FLStream(loader, offset + fd.offset, fd.size),
					ErrType.ERROR, "failed to read bcuzip at " + path);
		}

		public void unzip(PatchFile func, Consumer<Double> prog) throws Exception {
			InputStream fis = new FileInputStream(loader.file);
			long skippedBytes = fis.skip(offset);
			if (skippedBytes != offset)
				System.out.println("W/PackLoader::unzip - Failed to skip bytes : Skipped bytes = " + skippedBytes + " | Targeted skipped bytes = " + offset);

			int x = 0;
			for (FileDesc fd : files) {
				int n = regulate(fd.size) / PASSWORD;
				File dest = func.getFile(fd.path);
				Context.check(dest);
				OutputStream fos = new FileOutputStream(dest);
				byte[] bs = new byte[PASSWORD];
				Cipher cipher = decrypt(loader.key);
				for (int i = 0; i < n; i++) {
					fis.read(bs);
					byte[] ans = i == n - 1 ? cipher.doFinal(bs) : cipher.update(bs);

					if(i == n -1) {
						ans = unpad(ans);
					}

					fos.write(ans);
				}
				fos.flush();
				fos.close();
				prog.accept(1.0 * (x++) / files.length);
			}
			fis.close();
		}

		private byte[] unpad(byte[] data) {
			int pad = 0;

			for(int i = 0; i < data.length; i++) {
				if(data[data.length -1 - i] == 0)
					pad++;
				else
					break;
			}

			byte[] res = new byte[data.length - pad];

			System.arraycopy(data, 0, res, 0, res.length);
			return res;
		}

		private void load(Consumer<Double> con) throws Exception {
			int i = 0;
			for (FileDesc fd : files) {
				loader.fis.skip(regulate(fd.size));
				con.accept(1.0 * (i++) / files.length);
			}
		}

		private void save(FileSaver saver, @Nullable Consumer<Double> prog) throws Exception {
			int x = 0;
			for (FileDesc fd : files) {
				FileInputStream fis = new FileInputStream(fd.file);
				int rem = fd.size;
				byte[] data = null;
				Cipher cipher = encrypt(saver.key);
				while (rem > 0) {
					int size = Math.min(rem, CHUNK);
					if (data == null || data.length != size)
						data = new byte[size];
					fis.read(data);
					rem -= size;
					saver.save(cipher, data, rem == 0);
				}
				fis.close();
				if(prog != null)
					prog.accept(1.0 * (x++) / files.length);
			}
		}
	}

	private static class FileLoader {

		private static class FLStream extends InputStream {

			private final ByteStream fis;
			private final Cipher cipher;

			private int LEN, len, size;
			private final byte[] cache;
			private int index;

			private FLStream(FileLoader ld, int offset, int size) throws Exception {
				len = regulate(size);
				for (int i = 0; i <= 6; i++) {
					if (i == 6 || len < 1 << i + 13) {
						LEN = 1 << i + 10;
						break;
					}
				}
				cache = new byte[LEN];
				this.size = size;
				cipher = decrypt(ld.key);
				fis = MultiStream.getStream(ld.file, offset, ld.useRAF);
				index = LEN;
			}

			@Override
			public void close() throws IOException {
				fis.close();
			}

			@Override
			public int read() throws IOException {
				if (size == 0)
					return -1;
				if (index >= LEN)
					update();
				return (readByte()) & 0xff;
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				int rlen = Math.min(len, size);
				if (b == null) {
					throw new NullPointerException();
				} else if (off < 0 || rlen < 0 || rlen > b.length - off) {
					throw new IndexOutOfBoundsException();
				} else if (rlen == 0) {
					return rlen < len ? -1 : 0;
				}
				int i = rlen;
				while (i > 0) {
					if (index >= LEN)
						update();
					int avi = Math.min(i, LEN - index);
					System.arraycopy(cache, index, b, off, avi);
					i -= avi;
					index += avi;
					off += avi;
					size -= avi;
				}
				return rlen;

			}

			private byte readByte() {
				size--;
				return cache[index++];
			}

			private void update() throws IOException {
				int rlen = Math.min(len, LEN);
				fis.read(cache, 0, rlen);
				len -= rlen;
				index = 0;
				try {
					if (len == 0)
						cipher.doFinal(cache, 0, rlen, cache, 0);
					else
						cipher.update(cache, 0, rlen, cache, 0);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}

		}
		private final FileInputStream fis;
		private final ZipDesc pack;
		private final File file;
		private final byte[] key;
		private final boolean useRAF;

		private FileLoader(FileInputStream stream, int off, File file, boolean useRAF, Consumer<Double> con) throws Exception {
			this.file = file;
			this.fis = stream;
			this.useRAF = useRAF;
			byte[] head = new byte[HEADER];
			fis.read(head);
			if (!Arrays.equals(head, HEAD_DATA))
				throw new Exception("Corrupted File: header not match");
			key = new byte[PASSWORD];
			fis.read(key);
			byte[] len = new byte[4];
			fis.read(len);
			int size = DataIO.toInt(len, 0);
			String desc = new String(decode(size), StandardCharsets.UTF_8);
			JsonElement je = JsonParser.parseString(desc);
			int offset = off + HEADER + PASSWORD + 4 + regulate(size);
			pack = JsonDecoder.inject(je, ZipDesc.class, new ZipDesc(this, offset));
			pack.load(con);
		}

		private byte[] decode(int size) throws Exception {
			int len = regulate(size);
			byte[] bs = new byte[len];
			fis.read(bs);
			bs = decrypt(key).doFinal(bs);
			if (bs.length != size)
				return Arrays.copyOf(bs, size);
			return bs;
		}

	}

	private static class FileSaver {

		@StaticPermitted
		private static final String[] EXCLUDE = { ".DS_Store", ".desktop.ini", "__MACOSX" };

		private final FileOutputStream fos;
		private final byte[] key;

		private int len;

		private FileSaver(File dst, File folder, PackData.PackDesc pd, String password, Consumer<Double> prog)
				throws Exception {
			List<FileDesc> fs = new ArrayList<>();
			for (File fi : folder.listFiles())
				addFiles(fs, fi, "./");
			ZipDesc desc = new ZipDesc(pd, fs.toArray(new FileDesc[0]));
			byte[] bytedesc = JsonEncoder.encode(desc).toString().getBytes(StandardCharsets.UTF_8);
			fos = new FileOutputStream(dst);
			fos.write(HEAD_DATA);
			key = getMD5(password.getBytes(StandardCharsets.UTF_8), PASSWORD);
			fos.write(key);
			byte[] len = new byte[4];
			DataIO.fromInt(len, 0, bytedesc.length);
			fos.write(len);
			save(encrypt(key), bytedesc, true);
			desc.save(this, prog);
			fos.flush();
			fos.close();
		}

		private FileSaver(File dst, List<File> files, PackData.PackDesc pd, String password, @Nullable Consumer<Double> prog)
				throws Exception {
			files.removeIf(f -> f == null || !f.exists());

			if(files.size() == 0)
				throw new IllegalStateException("File size is 0 : "+files);

			if(!checkParents(files))
				throw new IllegalStateException("Files' parents aren't synced : "+files);

			List<FileDesc> fs = new ArrayList<>();
			for (File fi : files)
				addFiles(fs, fi, "./");
			ZipDesc desc = new ZipDesc(pd, fs.toArray(new FileDesc[0]));
			byte[] bytedesc = JsonEncoder.encode(desc).toString().getBytes(StandardCharsets.UTF_8);
			fos = new FileOutputStream(dst);
			fos.write(HEAD_DATA);
			key = getMD5(password.getBytes(StandardCharsets.UTF_8), PASSWORD);
			fos.write(key);
			byte[] len = new byte[4];
			DataIO.fromInt(len, 0, bytedesc.length);
			fos.write(len);
			save(encrypt(key), bytedesc, true);
			desc.save(this, prog);
			fos.flush();
			fos.close();
		}

		private void addFiles(List<FileDesc> fs, File f, String path) {
			for (String str : EXCLUDE)
				if (f.getName().equals(str))
					return;
			if (f.isDirectory())
				for (File fi : f.listFiles())
					addFiles(fs, fi, path + f.getName() + "/");
			else
				fs.add(new FileDesc(this, path + f.getName(), f));
		}

		private void save(Cipher cipher, byte[] bs, boolean end) throws Exception {
			if ((bs.length & 0xF) != 0)
				bs = Arrays.copyOf(bs, (bs.length | 0xF) + 1);
			bs = end ? cipher.doFinal(bs) : cipher.update(bs);
			fos.write(bs);
		}

		private boolean checkParents(List<File> files) {
			File parent = files.get(0).getParentFile();

			for(int i = 1; i < files.size(); i++) {
				File p = files.get(i).getParentFile();

				if(parent != null && p != null) {
					if(!parent.getName().equals(p.getName()))
						return false;
				} else if(parent != null || p != null)
					return false;
			}

			return true;
		}
	}

	private static final int HEADER = 16, PASSWORD = 16, CHUNK = 1 << 16;

	private static final String HEAD_STR = "battlecatsultimate";

	public static final byte[] HEAD_DATA = getMD5(HEAD_STR.getBytes(), HEADER);

	private static final byte[] INIT_VECTOR = getMD5("battlecatsultimate".getBytes(), 16);

	public static Cipher decrypt(byte[] key) throws Exception {
		IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR);
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
		return cipher;
	}

	public static Cipher encrypt(byte[] key) throws Exception {
		IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR);
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		return cipher;
	}

	public static byte[] getMD5(byte[] data, int len) {
		MessageDigest md5 = Data.err(() -> MessageDigest.getInstance("MD5"));
		byte[] ans = md5.digest(data);
		if (ans.length == len)
			return ans;
		return Arrays.copyOf(ans, len);
	}

	public static void read(FileInputStream fis, AssetHeader asset) throws Exception {
		byte[] head = new byte[HEADER];
		fis.read(head);
		if (!Arrays.equals(head, HEAD_DATA))
			throw new Exception("Corrupted File: header not match");
		byte[] len = new byte[4];
		fis.read(len);
		int size = DataIO.toInt(len, 0);
		byte[] data = new byte[size];
		fis.read(data);
		String desc = new String(data, StandardCharsets.UTF_8);
		JsonElement je = JsonParser.parseString(desc);
		JsonDecoder.inject(je, AssetHeader.class, asset);
		asset.offset = HEADER + 4 + size;
	}

	public static List<ZipDesc> readAssets(File f, Consumer<Double> prog) throws Exception {
		FileInputStream fis = new FileInputStream(f);
		AssetHeader header = new AssetHeader();
		read(fis, header);
		List<ZipDesc> ans = new ArrayList<>();
		int off = header.offset;
		int i = 0;
		for (AssetEntry ent : header.list) {
			int I = i++;
			prog.accept(1.0 * I / header.list.size());
			Consumer<Double> con = (d) -> prog.accept((I + d) / header.list.size());
			ZipDesc zip = new FileLoader(fis, off, f, true, con).pack;
			ans.add(zip);
			off += ent.size;
		}
		fis.close();
		return ans;
	}

	public static ZipDesc readPack(File f) throws Exception {
		FileInputStream fis = new FileInputStream(f);
		ZipDesc ans = new FileLoader(fis, 0, f, false, (d) -> {
		}).pack;
		fis.close();
		return ans;
	}

	public static void write(FileOutputStream fos, AssetHeader asset) throws Exception {
		fos.write(HEAD_DATA);
		byte[] data = JsonEncoder.encode(asset).toString().getBytes();
		byte[] len = new byte[4];
		DataIO.fromInt(len, 0, data.length);
		fos.write(len);
		fos.write(data);
	}

	public static void writePack(File dst, File folder, PackData.PackDesc pd, String password, Consumer<Double> prog)
			throws Exception {
		new FileSaver(dst, folder, pd, password, prog);
	}

	public static void writePackWithSpecificFiles(File dst, List<File> files, PackData.PackDesc pd, String password, Consumer<Double> prog) throws Exception {
		new FileSaver(dst, files, pd, password, prog);
	}

	private static int regulate(int size) {
		return (size & 0xF) == 0 ? size : (size | 0xF) + 1;
	}

}