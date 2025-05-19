package common.util.anim;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.pack.Source;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.system.fake.FakeImage.Marker;
import common.system.files.FileData;
import common.system.files.VFile;

@JsonClass.JCGeneric(Source.ResourceLocation.class)
public class AnimUD extends AnimU<AnimUD.DefImgLoader> {

	private final String name;

	@JsonClass.JCIdentifier
	public final Source.ResourceLocation id;

	static class DefImgLoader implements AnimU.ImageKeeper {
		private final String spath;
		private final VFile fnum, fedi, funi, fico;
		private FileData dnum;
		private FakeImage num;
		private VImg edi, uni, icon;

		private DefImgLoader(String path, String str, String sedi, String suni) {
			spath = path + str;
			fnum = VFile.get(spath + ".png");
			fedi = sedi == null ? null : VFile.get(path + sedi);
			funi = suni == null ? null : VFile.get(path + suni);
			fico = suni != null || sedi == null ? null : VFile.get(path + sedi.replace("di", "nemy_icon"));
		}

		@Override
		public VImg getEdi() {
			if (edi != null && edi.getImg() != null && edi.getImg().bimg() != null && edi.getImg().isValid())
				return edi;
			return fedi == null ? null : (edi = new VImg(fedi).mark(Marker.EDI));
		}

		@Override
		public VImg getPreviewIcon() {
			if (icon != null && icon.getImg() != null && icon.getImg().bimg() != null && icon.getImg().isValid())
				return icon;
			return fico == null ? null : (icon = new VImg(fico).mark(Marker.ICO));
		}

		@Override
		public ImgCut getIC() {
			return ImgCut.newIns(spath + ".imgcut");
		}

		@Override
		public MaAnim[] getMA() {
			MaAnim[] ma;
			if (VFile.get(spath + ".maanim") != null)
				ma = new MaAnim[]{MaAnim.newIns(spath + ".maanim")};
			else if (VFile.get(spath + "00.maanim") == null && VFile.get(spath + "02.maanim") != null)
				ma = new MaAnim[]{MaAnim.newIns(spath + "02.maanim")};
			else {
				if (VFile.get(spath + "_zombie00.maanim") != null)
					ma = new MaAnim[7];
				else if (VFile.get(spath + "_entry.maanim") != null)
					ma = new MaAnim[5];
				else
					ma = new MaAnim[4];
				for (int i = 0; i < 4; i++)
					ma[i] = MaAnim.newIns(spath + "0" + i + ".maanim");
				if (ma.length == 5)
					ma[4] = MaAnim.newIns(spath + "_entry.maanim");
				if (ma.length == 7)
					for (int i = 0; i < 3; i++)
						ma[i + 4] = MaAnim.newIns(spath + "_zombie0" + i + ".maanim");
			}
			ma = filterValidAnims(ma);
			return ma;
		}

		@Override
		public MaModel getMM() {
			return MaModel.newIns(spath + ".mamodel");
		}

		@Override
		public FakeImage getNum() {
			if (num != null && num.bimg() != null && num.isValid())
				return num;
			FileData fd = dnum == null ? (dnum = fnum.getData()) : dnum;
			num = fd.getImg();
			return num;
		}

		@Override
		public VImg getUni() {
			if (uni != null && uni.getImg() != null && uni.getImg().bimg() != null && uni.getImg().isValid())
				return uni;

			if (funi == null) {
				return CommonStatic.getBCAssets().slot[0];
			} else {
				uni = new VImg(funi).mark(Marker.UNI);
				uni.setCut(CommonStatic.getBCAssets().unicut);

				return uni;
			}
		}

		@Override
		public void unload() {
			dnum = null;

			if (num != null) {
				num.unload();
				num = null;
			}

			if (edi != null) {
				edi.unload();
				edi = null;
			}

			if (uni != null) {
				uni.unload();
				uni = null;
			}
		}

		private MaAnim[] filterValidAnims(MaAnim[] original) {
			int end = 0;
			for (int i = 0; i < original.length; i++)
				if (original[i] != null && original[i].n != 0)
					end = i;

			MaAnim[] fixed = new MaAnim[end + 1];
			System.arraycopy(original, 0, fixed, 0, end + 1);
			return fixed;
		}
	}

	public AnimUD(String path, String name, String edi, String uni) {
		super(path + name, new DefImgLoader(path, name, edi, uni));
		this.name = name;
		id = new Source.ResourceLocation(Identifier.DEF, name);
	}

	@Override
	public int getAtkLen(int atk) {
		partial(); //BC units only have 1 attack 100% of the time, so why bother using the param
		return anims[Math.min(anims.length - 1, 2)].len + 1; //The min is there due to spirits
	}

	@Override
	public void partial() {
		if (partial)
			return;
		super.partial();
		if (!partial)//There was an error loading if partial is still false
			return;

		if (types.length == 5)
			types[4] = TYPEDEF[ENTRY]; //Iron Wall
		
		for (MaAnim ma : anims)
			for (Part p : ma.parts)
				for (int j = 0; j < p.moves.length - 1; j++)
					if (p.moves[j][0] == p.moves[j+1][0]-1)
						p.moves[j][2] = 1;
	}

	@Override
	public String toString() {
		return name;
	}
}