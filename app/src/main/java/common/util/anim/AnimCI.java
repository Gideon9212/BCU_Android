package common.util.anim;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Source;
import common.pack.Source.ResourceLocation;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.system.fake.FakeImage.Marker;

import java.util.ArrayList;

@JsonClass.JCGeneric(ResourceLocation.class)
public class AnimCI extends AnimU<AnimCI.AnimCIKeeper> {

	public static class AnimCIKeeper implements AnimU.ImageKeeper {

		public final Source.SourceAnimLoader loader;
		private FakeImage num;
		private boolean ediLoaded = false, prevLoaded = false;
		private VImg edi;
		private VImg uni;
		private VImg preview;

		private AnimCIKeeper(Source.SourceAnimLoader al) {
			loader = al;
		}

		@Override
		public VImg getPreviewIcon() {
			if (prevLoaded)
				return preview;
			prevLoaded = true;
			preview = loader.getIcon(Source.SourceAnimLoader.ICN);
			return preview;
		}
		@Override
		public VImg getEdi() {
			if (ediLoaded)
				return edi;
			ediLoaded = true;
			edi = loader.getIcon(Source.SourceAnimLoader.EDI);
			if (edi != null)
				edi.mark(Marker.EDI);
			return edi;
		}
		@Override
		public VImg getUni() {
			if (uni != null)
				return uni;
			uni = loader.getIcon(Source.SourceAnimLoader.UNI);
			if (uni != null)
				uni.mark(Marker.UNI);
			else
				uni = CommonStatic.getBCAssets().slot[0];
			return uni;
		}

		@Override
		public ImgCut getIC() {
			return loader.getIC();
		}

		@Override
		public MaAnim[] getMA() {
			MaAnim[] mas = loader.getMA();
			if (getName().base == Source.BasePath.ANIM && mas.length < AnimU.TYPEDEF.length) {
				MaAnim[] fixed = new MaAnim[AnimU.TYPEDEF.length];
				for (int i = 0; i < fixed.length; i++) {
					if (i < mas.length)
						fixed[i] = mas[i];
					else
						fixed[i] = new MaAnim();
				}
				return fixed;
			}
			return mas;
		}

		@Override
		public MaModel getMM() {
			return loader.getMM();
		}

		protected ResourceLocation getName() {
			return loader.getName();
		}

		@Override
		public FakeImage getNum() {
			if (num != null && num.bimg() != null && num.isValid())
				return num;
			return num = loader.getNum();
		}
		public void setNum(FakeImage fimg) {
			num = fimg;
		}

		public int getStatus() {
			return loader.getStatus();
		}

		public void setEdi(VImg vedi) {
			edi = vedi;
			if (vedi != null)
				vedi.mark(Marker.EDI);
			ediLoaded = true;
		}
		public void setUni(VImg vuni) {
			if (vuni != null) {
				uni = vuni;
				uni.mark(Marker.UNI);
			} else
				uni = CommonStatic.getBCAssets().slot[0];
		}
		public void setPreview(VImg vedi) {
			preview = vedi;
			prevLoaded = true;
		}

		@Override
		public void unload() {
			if (num != null) {
				num.unload();
				num = null;
			}
		}
	}

	@JsonClass.JCIdentifier
	public ResourceLocation id;

	public AnimCI(Source.SourceAnimLoader acl) {
		super(new AnimCIKeeper(acl));
		id = loader.getName();
	}

	@Override
	public final String[] names() {
		String[] names = rawNames();
		ArrayList<String> list = new ArrayList<>(types.length);
		boolean adAll = true;
		for (int i = 0; i < names.length; i++) {
			if (adAll || anims[i].n > 0)
				list.add(names[i]);
			if (adAll && types[i].toString().equals("kb"))
				adAll = false;
		}
		return list.toArray(new String[0]);
	}

	@Override
	public final UType[] types() {
		check();
		ArrayList<UType> list = new ArrayList<>(types.length);
		boolean adAll = true;
		for (int i = 0; i < types.length; i++) {
			if (adAll || anims[i].n > 0)
				list.add(types[i]);
			if (adAll && types[i].toString().equals("kb"))
				adAll = false;
		}
		return list.toArray(new UType[0]);
	}

	public String[] rawNames() {
		return super.names();
	}

	@Override
	public void load() {
		super.load();
		if (getEdi() != null)
			getEdi().check();
		if (getUni() != null)
			getUni().check();

		validate();
	}

	@Override
	public String toString() {
		return id.id;
	}
}