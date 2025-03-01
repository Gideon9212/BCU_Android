package common.util.pack.bgeffect;

import common.system.VImg;
import common.system.fake.FakeImage;
import common.util.anim.*;

public class BGEffectAnim extends AnimD<BGEffectAnim, BGEffectAnim.BGEffType> {

    private final String imgcutName, mamodelName, maanimName;
    private VImg img;

    /**
     * Animation class for BG effect
     * @param st Name of png
     * @param imgcut Name of imguct
     * @param mamodel Name of mamodel
     * @param maanim Name of maanim
     */
    public BGEffectAnim(String st, String imgcut, String mamodel, String maanim) {
        super(st);
        imgcutName = imgcut;
        mamodelName = mamodel;
        maanimName = maanim;

        img = new VImg(str);
    }

    public enum BGEffType implements AnimI.AnimType<BGEffectAnim, BGEffType> {
        DEF
    }

    @Override
    public FakeImage getNum() {
        if (img.getImg().bimg() == null || !img.getImg().isValid())
            img = new VImg(str);

        return img.getImg();
    }

    @Override
    public void load() {
        imgcut = ImgCut.newIns(imgcutName);
        mamodel = MaModel.newIns(mamodelName);
        anims = new MaAnim[] { MaAnim.newIns(maanimName) };
        types = BGEffType.values();
        img = new VImg(str);
        parts = imgcut.cut(img.getImg());

        loaded = true;
    }
}
