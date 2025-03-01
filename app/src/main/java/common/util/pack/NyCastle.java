package common.util.pack;

import common.CommonStatic;
import common.CommonStatic.BCAuxAssets;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.util.anim.*;

public class NyCastle extends AnimD<NyCastle, NyCastle.NyType> {

	public enum NyType implements AnimI.AnimType<NyCastle, NyType> {
		BASE, ATK, EXT
	}

	public static final int TOT = 8;

	public static void read() {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		String pre = "./org/castle/00";
		String mid = "/nyankoCastle_00";
		int[] type = new int[] { 0, 2, 3 };
		for (int t = 0; t < 3; t++)
			for (int i = 0; i < TOT; i++) {
				String str = pre + type[t] + mid + type[t] + "_0" + i;
				aux.main[t][i] = new VImg(str + ".png");
			}
		for (int i = 0; i < TOT; i++) {
			String str = pre + 1 + mid + 1 + "_0";
			aux.atks[i] = new NyCastle(str, i);
		}
	}

	private final int id;
	private final VImg sprite;
	private final MaModel atkm, extm;

	private NyCastle(String str, int t) {
		super(str);
		anim = this;
		id = t;
		sprite = new VImg(str + t + "_00.png");
		imgcut = ImgCut.newIns(str + t + "_00.imgcut");
		mamodel = MaModel.newIns(str + t + "_01.mamodel");
		anims = new MaAnim[]{MaAnim.newIns(str + t + "_01.maanim"),null,null};
		if (t != 1 && t != 2 && t != 7) {
			atkm = MaModel.newIns(str + t + "_00.mamodel");
			anims[1] = MaAnim.newIns(str + t + "_00.maanim");
		} else
			atkm = null;
		if (t == 6) {
			extm = MaModel.newIns(str + t + "_02.mamodel");
			anims[2] = MaAnim.newIns(str + t + "_02.maanim");
		} else
			extm = null;
	}

	@Override
	public EAnimD<NyType> getEAnim(NyType t) {
		check();
		if (t == NyType.BASE)
			return new EAnimD<>(this, mamodel, anims[0], t);
		if (t == NyType.ATK)
			return new EAnimD<>(this, atkm, anims[1], t);
		if (t == NyType.EXT)
			return new EAnimD<>(this, extm, anims[2], t);
		return null;
	}

	@Override
	public FakeImage getNum() {
		return sprite.getImg();
	}

	@Override
	public void load() {
		types = NyType.values();
		parts = imgcut.cut(sprite.getImg());
		loaded = true;
	}

	@Override
	public String[] names() {
		if (atkm == null)
			return new String[] { "castle" };
		if (extm == null)
			return new String[] { "castle", "atk" };
		return new String[] { "castle", "atk", "ext" };
	}

	@Override
	public String toString() {
		return "castle " + id;
	}
}
