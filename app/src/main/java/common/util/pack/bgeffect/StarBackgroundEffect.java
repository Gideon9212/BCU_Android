package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@JsonClass.JCGeneric(Identifier.class)
public class StarBackgroundEffect extends BackgroundEffect {
    private static final int[][] starColors = {
            {233, 248, 255},
            {199, 249, 218},
            {222, 167, 197},
            {167, 169, 255}
    };

    private final List<Integer> opacities = new ArrayList<>();
    private final List<P> positions = new ArrayList<>();
    private final List<Byte> colors = new ArrayList<>();
    private final Random r = new Random();

    private final List<Integer> times = new ArrayList<>();

    private final List<Integer> capture = new ArrayList<>();

    private int range;
    private int number;

    public StarBackgroundEffect(Identifier<BackgroundEffect> id) {
        super(id);
    }

    @Override
    public void check() {

    }

    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {
        FakeTransform at = g.getTransform();

        g.setComposite(FakeGraphics.BLEND, 255, 1);

        for(int i = 0; i < number; i++) {
            int[] c = starColors[colors.get(i)];

            g.colRect(BackgroundEffect.convertP(positions.get(i).x, siz) + (int) rect.x, (int) (positions.get(i).y * siz - rect.y), (int) Math.max(1, siz * 4 * 0.8), (int) Math.max(1, siz * 4 * 0.8), c[0], c[1], c[2], opacities.get(i));
        }

        g.setComposite(FakeGraphics.DEF, 255, 0);

        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {

    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        FakeTransform at = g.getTransform();
        g.setComposite(FakeGraphics.BLEND, 255, 1);
        for(int i = 0; i < number; i++) {
            int[] c = starColors[colors.get(i)];
            g.colRect(BackgroundEffect.convertP(positions.get(i).x, siz), (int) (positions.get(i).y * siz - y), (int) Math.max(1, siz * 4 * 0.8), (int) Math.max(1, siz * 4 * 0.8), c[0], c[1], c[2], opacities.get(i));
        }
        g.setComposite(FakeGraphics.DEF, 255, 0);
        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void update(int w, float h, float midH, float timeFlow) {
        capture.clear();

        for(int i = 0; i < times.size(); i++) {
            times.set(i, (int)(times.get(i) - timeFlow));
            opacities.set(i, (int) (255 * Math.sin(Math.PI  * times.get(i) / 20) * timeFlow));

            if(times.get(i) <= 0)
                capture.add(i);
        }

        if(capture.size() > 0) {
            int rangeH = BGHeight * 2 + range;

            for (Integer capts : capture) {
                opacities.set(capts, 0);

                P.delete(positions.get(capts));

                positions.set(capts, P.newP(r.nextInt(w + battleOffset) * timeFlow, r.nextInt(rangeH) * timeFlow));
                colors.set(capts, (byte) (r.nextInt(starColors.length - 1)));
                times.set(capts, Data.BG_EFFECT_STAR_TIME);
            }
        }
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {
        opacities.clear();
        for (P position : positions)
            P.delete(position);

        positions.clear();
        times.clear();
        colors.clear();
        capture.clear();

        range = bg.id.id != 34 ? Data.BG_EFFECT_STAR_Y_RANGE : 0;

        int rangeH = BGHeight * 2 + range;

        number = w / 100;

        for(int i = 0; i < number; i++) {
            int time = (r.nextInt(Data.BG_EFFECT_STAR_TIME));

            opacities.add((int) (255 * Math.sin(Math.PI  * time / 20)));
            positions.add(P.newP(r.nextInt(w + battleOffset), r.nextInt(rangeH)));
            colors.add((byte) (r.nextInt(starColors.length - 1)));
            times.add(time);
        }
    }

    @Override
    public String toString() {
        return CommonStatic.def.getUILang(0, "bgeff" + id.id);
    }
}
