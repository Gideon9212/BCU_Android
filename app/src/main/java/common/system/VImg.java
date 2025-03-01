package common.system;

import common.system.fake.FakeImage;
import common.system.fake.FakeImage.Marker;
import common.system.files.VFile;
import common.util.ImgCore;
import common.util.anim.ImgCut;

public class VImg extends ImgCore {

	private final VFile file;

	public String name = "";

	public FakeImage bimg = null;

	private boolean loaded = false;
	private ImgCut ic;
	private Marker marker;

	public VImg(FakeImage fi) {
		file = null;
		bimg = fi;
		loaded = true;
	}

	public VImg(String str) {
		this(VFile.get(str));
	}

	public VImg(VFile vf) {
		file = vf;
		loaded = false;
	}

	public synchronized void check() {
		if (bimg != null && !bimg.isValid())
			loaded = false;

		if (!loaded)
			load();
	}

	public FakeImage getImg() {
		check();
		return bimg;
	}

	public VImg mark(Marker string) {
		marker = string;
		if (bimg != null)
			bimg.mark(string);
		return this;
	}

	public void setCut(ImgCut cut) {
		ic = cut;
	}

	public void setImg(FakeImage img) {
		bimg = img;
		if (ic != null)
			bimg = ic.cut(bimg)[0];
		loaded = true;
	}

	@Override
	public String toString() {
		return file == null ? name.length() == 0 ? "img" : name : file.getName();
	}

	public void unload() {
		if (file == null)
			return;
		if (bimg != null) {
			bimg.unload();
			bimg = null;
		}
		loaded = false;
	}

	private void load() {
		loaded = true;
		if (file == null)
			return;
		bimg = file.getData().getImg();
		if (bimg == null)
			return;
		if (marker != null)
			bimg.mark(marker);
		if (ic != null)
			bimg = ic.cut(bimg)[0];
		try {
			bimg.getWidth();
		} catch (Exception e) {
			bimg = null;
		}
	}

}
