package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.system.P;
import common.system.VImg;
import common.system.fake.FakeGraphics;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@JsonClass.JCGeneric(Identifier.class)
public class SnowBGEffect extends BackgroundEffect {
    private final float maxSlope = (float) Math.tan(Math.toRadians(75));
    private final VImg snow;

    private final int sw;
    private final int sh;

    private final List<P> snowPosition = new ArrayList<>();
    private final List<P> initPos = new ArrayList<>();
    private final List<Float> speed = new ArrayList<>();
    private final List<Float> slope = new ArrayList<>();
    private final Random r = new Random();

    private final List<Integer> capture = new LinkedList<>();

    public SnowBGEffect(Identifier<BackgroundEffect> i, VImg snow) {
        super(i);
        this.snow = snow;

        this.sw = (int) (snow.getImg().getWidth() * 1.8);
        this.sh = (int) (snow.getImg().getHeight() * 1.8);
    }

    @Override
    public void check() {
        snow.check();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {

    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {
        g.setComposite(FakeGraphics.TRANS, 127, 0);
        for (P p : snowPosition)
            g.drawImage(snow.getImg(), BackgroundEffect.convertP(p.x, siz) + (int) rect.x, (int) (p.y * siz - rect.y + midH * siz), sw * siz, sh * siz);
        g.setComposite(FakeGraphics.DEF, 255, 0);
    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        g.setComposite(FakeGraphics.TRANS, 127, 0);
        for (P p : snowPosition)
            g.drawImage(snow.getImg(), BackgroundEffect.convertP(p.x, siz), (int) (p.y * siz - y + midH * siz), sw * siz, sh * siz);
        g.setComposite(FakeGraphics.DEF, 255, 0);
    }

    @Override
    public void update(int w, float h, float midH, float timeFlow) {
        capture.clear();

        for(int i = 0; i < snowPosition.size(); i++) {
            if(snowPosition.get(i).y >= 1510 + sh || snowPosition.get(i).x < -sw || snowPosition.get(i).x >= w + battleOffset) {
                capture.add(i);
            } else {
                snowPosition.get(i).y += speed.get(i) * timeFlow;
                //slope(y - initY) + initX = x
                snowPosition.get(i).x = BackgroundEffect.revertP(slope.get(i) * (snowPosition.get(i).y - initPos.get(i).y) * timeFlow) + initPos.get(i).x;
            }
        }

        if(!capture.isEmpty()) {
            for (Integer capt : capture) {
                float x = r.nextInt(w + sw + battleOffset) * timeFlow;
                float y = -sh * timeFlow;

                snowPosition.get(capt).x = x;
                snowPosition.get(capt).y = y;
                initPos.get(capt).x = x;
                initPos.get(capt).y = y;

                //0 ~ 75
                float angle = (float) Math.toRadians(r.nextInt(75));

                //-0.5angle + 1 is stabilizer
                speed.set(capt, CommonStatic.fltFpsDiv((float) ((Data.BG_EFFECT_SNOW_SPEED - r.nextInt(Data.BG_EFFECT_SNOW_SPEED - 3)) * (-0.75 * angle / maxSlope + 1))));
                slope.set(capt,(float) Math.tan(-angle));
            }
        }
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {
        for (P p : snowPosition) P.delete(p);
        snowPosition.clear();

        int number = w / 200;

        for(int i = 0; i < number; i++) {
            float x = r.nextInt(w + sw + battleOffset);
            float y = r.nextInt(1510 + sh);
            snowPosition.add(P.newP(x, y));
            initPos.add(P.newP(x, y));

            //0~75
            float angle = (float) Math.toRadians(r.nextInt(75));

            //-0.5angle + 1 is stabilizer
            if (CommonStatic.getConfig().fps60)
                speed.add((float) ((Data.BG_EFFECT_SNOW_SPEED - r.nextInt(Data.BG_EFFECT_SNOW_SPEED - 3)) * (-0.75 * angle / maxSlope + 1)) / 2f);
            else
                speed.add((float) ((Data.BG_EFFECT_SNOW_SPEED - r.nextInt(Data.BG_EFFECT_SNOW_SPEED - 3)) * (-0.75 * angle / maxSlope + 1)));

            slope.add((float) Math.tan(-angle));
        }
    }

    @Override
    public String toString() {
        return CommonStatic.def.getUILang(0, "bgeff" + id.id);
    }
}
