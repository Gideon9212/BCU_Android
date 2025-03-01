package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@JsonClass.JCGeneric(Identifier.class)
public class ShiningBGEffect extends BackgroundEffect {
    private final FakeImage shine;

    private final int sw;
    private final int sh;

    private final List<P> shinePosition = new ArrayList<>();
    private final List<Byte> time = new ArrayList<>();
    private final Random r = new Random();

    private final List<Integer> capture = new LinkedList<>();

    public ShiningBGEffect(Identifier<BackgroundEffect> id) {
        super(id);
        Background bg = UserProfile.getBCData().bgs.get(55);

        bg.load();

        shine = bg.parts[20];

        sw = shine.getWidth();
        sh = shine.getHeight();
    }

    @Override
    public void check() {
        shine.bimg();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {

    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {
        g.setComposite(FakeGraphics.BLEND, 255, 1);
        for(int i = 0; i < shinePosition.size(); i++) {
            float size = (float) Math.sin(Math.PI * time.get(i) / Data.BG_EFFECT_SHINING_TIME);
            g.drawImage(shine, BackgroundEffect.convertP(shinePosition.get(i).x, siz) + (int) (rect.x - sw * size * siz / 2), (int) (shinePosition.get(i).y * siz - rect.y - sh * size * siz / 2), sw * size * siz, sh * size * siz);
        }
        g.setComposite(FakeGraphics.DEF, 255, 0);
    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        g.setComposite(FakeGraphics.BLEND, 255, 1);
        for(int i = 0; i < shinePosition.size(); i++) {
            float size = (float) Math.sin(Math.PI * time.get(i) / Data.BG_EFFECT_SHINING_TIME);
            g.drawImage(shine, BackgroundEffect.convertP(shinePosition.get(i).x, siz) + (int) (-sw * size * siz / 2), (int) (shinePosition.get(i).y * siz - y - sh * size * siz / 2), sw * size * siz, sh * size * siz);
        }
        g.setComposite(FakeGraphics.DEF, 255, 0);
    }

    @Override
    public void update(int w, float h, float midH, float timeFlow) {
        capture.clear();

        for(int i = 0; i < shinePosition.size(); i++) {
            if(time.get(i) <= 0)
                capture.add(i);
            else
                time.set(i, (byte) (time.get(i) - timeFlow));
        }

        if(!capture.isEmpty())
            for (Integer capt : capture) {
                shinePosition.get(capt).x = r.nextInt(w + battleOffset) * timeFlow;
                shinePosition.get(capt).y = r.nextInt(BGHeight * 3 - BGHeight) * timeFlow;
                time.set(capt, Data.BG_EFFECT_SHINING_TIME);
            }
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {
        for (P p : shinePosition)
            P.delete(p);

        shinePosition.clear();
        time.clear();

        int number = w / 1600;

        for(int i = 0; i < number; i++) {
            shinePosition.add(P.newP(r.nextInt(w + battleOffset), r.nextInt(BGHeight * 3 - BGHeight)));
            time.add((byte) (r.nextInt(Data.BG_EFFECT_SHINING_TIME)));
        }
    }

    @Override
    public String toString() {
        return CommonStatic.def.getUILang(0, "bgeff" + id.id);
    }
}
