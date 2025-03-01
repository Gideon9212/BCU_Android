package common.system.fake;

import common.io.assets.Admin.StaticPermitted;
import common.system.VImg;
import common.system.files.FileData;
import common.system.files.VFile;
import common.util.Data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public abstract class ImageBuilder<T> {

	@StaticPermitted(StaticPermitted.Type.ENV)
	public static ImageBuilder<?> builder;

	public static VImg toVImg(byte[] bs) {
		return new VImg(Data.err(() -> builder.build(bs)));
	}

	public static VImg toVImg(File f) {
		return new VImg(Data.err(() -> builder.build(f)));
	}

	public final FakeImage build(byte[] bs) throws IOException {
		return build((Supplier<InputStream>) () -> new ByteArrayInputStream(bs));
	}

	public abstract FakeImage build(File f) throws IOException;

	public final FakeImage build(FileData fd) throws IOException {
		return build((Supplier<InputStream>) fd::getStream);
	}

	public abstract FakeImage build(Supplier<InputStream> sup) throws IOException;

	public abstract FakeImage build(T o);

	public abstract FakeImage build(int w, int h);

	public final FakeImage build(VFile vf) throws IOException {
		return build(vf.getData());
	}

	public final VImg toVImg(T t) {
		return new VImg(Data.err(() -> build(t)));
	}

	public abstract boolean write(FakeImage o, String fmt, Object out) throws IOException;

}
