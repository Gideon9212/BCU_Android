package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.system.P;
import common.system.VImg;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeTransform;
import common.util.Data;
import common.util.anim.ImgCut;
import common.util.pack.Background;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@JsonClass.JCGeneric(Identifier.class)
public class RainBGEffect extends BackgroundEffect {
    private final VImg sprite;
    private final ImgCut imgCut;

    private FakeImage rain;
    private FakeImage splash;

    private final int rw;
    private final int rh;
    private final int sw;
    private final int sh;

    private final List<P> rainPosition = new LinkedList<>();
    private final List<P> splashPosition = new LinkedList<>();
    private final Random r = new Random();

    public RainBGEffect(Identifier<BackgroundEffect> i, VImg sprite, ImgCut imgCut) {
        super(i);
        this.sprite = sprite;
        this.imgCut = imgCut;

        FakeImage[] cutImages = imgCut.cut(sprite.getImg());
        this.rain = cutImages[29];
        this.splash = cutImages[28];

        rw = this.rain.getWidth();
        rh = this.rain.getHeight();

        sw = this.splash.getWidth();
        sh = this.splash.getHeight();
    }

    @Override
    public void check() {
        if (!rain.isValid() || !splash.isValid()) {
            FakeImage[] cutImages = imgCut.cut(sprite.getImg());

            rain = cutImages[29];
            splash = cutImages[28];
        }
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {
        g.setComposite(FakeGraphics.TRANS, 96, 0);

        for (P p : splashPosition)
            g.drawImage(splash, BackgroundEffect.convertP(p.x, siz) + (int) rect.x, (int) (p.y * siz - rect.y), sw * siz * 0.8f, sh * siz * 0.8f);

        g.setComposite(FakeGraphics.DEF, 255, 0);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {
        g.setComposite(FakeGraphics.TRANS, 127, 0);

        FakeTransform at = g.getTransform();

        for (P p : rainPosition) {
            //30 and 50 shifting is to draw image at center
            g.translate(BackgroundEffect.convertP(p.x, siz) + (int) rect.x - 30 * siz * 0.8f, (int) (p.y * siz - rect.y + midH * siz - 50 * siz * 0.8));
            g.rotate((float) (Math.PI / 3));

            g.drawImage(rain, 0, 0, rw * siz * 0.8f, rh * siz * 0.8f);

            g.setTransform(at);
        }

        g.setTransform(at);
        g.delete(at);

        g.setComposite(FakeGraphics.DEF, 255, 0);
    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        g.setComposite(FakeGraphics.TRANS, 96, 0);

        for (P p : splashPosition) {
            g.drawImage(splash, BackgroundEffect.convertP(p.x, siz), (int) (p.y * siz - y + midH), sw * siz * 0.8f, sh * siz * 0.8f);
        }
        g.setComposite(FakeGraphics.TRANS, 127, 0);

        FakeTransform at = g.getTransform();

        for (P p : rainPosition) {
            //30 and 50 shifting is to draw image at center
            g.translate(BackgroundEffect.convertP(p.x, siz) - 30 * siz * 0.8f, (int) (p.y * siz - y + midH * siz - 50 * siz * 0.8));
            g.rotate((float) (Math.PI / 3));

            g.drawImage(rain, 0, 0, rw * siz * 0.8f, rh * siz * 0.8f);

            g.setTransform(at);
        }

        g.setTransform(at);
        g.delete(at);

        g.setComposite(FakeGraphics.DEF, 255, 0);
    }

    @Override
    public void update(int w, float h, float midH, float timeFlow) {
        for (P p : splashPosition)
            P.delete(p);

        for (P p : rainPosition)
            P.delete(p);

        splashPosition.clear();
        rainPosition.clear();

        int rainNumber = w / 100;
        int splashNumber = 2 * w / 300;

        rainNumber += rainNumber / 6 - (r.nextInt(rainNumber) / 3);
        splashNumber += splashNumber / 6 - (r.nextInt(splashNumber) / 3);

        for(int i = 0; i < rainNumber; i++) {
            rainPosition.add(P.newP(r.nextInt(w + battleOffset) * timeFlow, r.nextInt(BGHeight) * 3 * timeFlow));
        }

        for(int i = 0; i < splashNumber; i++) {
            //Y : BGHeight * 3 - 100 - random(0 ~ 80)
            splashPosition.add(P.newP(r.nextInt(w + battleOffset) * timeFlow, timeFlow * BGHeight * 3 - Data.BG_EFFECT_SPLASH_MIN_HEIGHT - r.nextInt(Data.BG_EFFECT_SPLASH_RANGE)));
        }
    }

    @Override
    public void updateAnimation(int w, float h, float midH, float timeFlow) {
        //Do nothing
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {

    }

    @Override
    public String toString() {
        return CommonStatic.def.getUILang(0, "bgeff" + id.id);
    }
}
