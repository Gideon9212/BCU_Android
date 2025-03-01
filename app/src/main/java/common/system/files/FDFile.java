package common.system.files;

import common.CommonStatic;
import common.pack.Context.ErrType;
import common.system.fake.FakeImage;

import java.io.*;

public class FDFile implements FileData {

	private final File file;

	public FDFile(File f) {
		file = f;
	}

	@Override
	public byte[] getBytes() {
		try {
			byte[] bs = new byte[(int) file.length()];
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
			buf.read(bs, 0, bs.length);
			buf.close();
			return bs;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public FakeImage getImg() {
		return FakeImage.read(file);
	}

	@Override
	public InputStream getStream() {
		return CommonStatic.ctx.noticeErr(() -> new FileInputStream(file), ErrType.ERROR,
				"failed to read bcuzip at " + file);
	}

	@Override
	public int size() {
		return (int) file.length();
	}

	@Override
	public String toString() {
		return file.getName();
	}

	@Override
	public String getPath() {
		return file.getPath();
	}

}
