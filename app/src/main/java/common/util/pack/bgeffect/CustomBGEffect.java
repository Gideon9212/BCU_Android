package common.util.pack.bgeffect;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.Source;
import common.system.BattleRange;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.Data;
import common.util.anim.AnimCI;
import common.util.anim.AnimU;
import common.util.anim.EAnimU;
import common.util.pack.Background;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class CustomBGEffect extends BackgroundEffect {

    private static final P origin = new P(0, 0);
    private static int sw = 0;
    @JsonField
    public String name = "";
    @JsonField
    public int spacer = 0, fspacer = 0; //Redraw the background for each dist specified by this, unless it's -1;
    private boolean loaded = false;

    public final EAnimU[] ebg = new EAnimU[2];
    @JsonField(alias = Source.ResourceLocation.class)
    public AnimCI anim;

    @JsonClass.JCConstructor
    public CustomBGEffect() {
        super(null);
    }

    public CustomBGEffect(Identifier<BackgroundEffect> id, AnimCI abg) {
        super(id);
        name = "BGEffect " + id;
        anim = abg;
        ebg[0] = anim.getEAnim(AnimU.BGEFFECT[0]);
        ebg[1] = anim.getEAnim(AnimU.BGEFFECT[1]);
        loaded = true;
    }

    @Override
    public void check() {
        if (!loaded) {
            anim.anim.load();
            ebg[0] = anim.getEAnim(AnimU.BGEFFECT[0]);
            ebg[1] = anim.getEAnim(AnimU.BGEFFECT[1]);
            loaded = true;
        }
    }
    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {
        int spaced = 0;
        while (spaced <= sw) {
            FakeTransform at = g.getTransform();
            g.translate(convertP(1024 + spaced * 2, siz) + rect.x, convertP(4500 - midH, siz) - rect.y);
            ebg[0].drawBGEffect(g, origin, siz * 0.8f, 255, 1, 1);
            g.setTransform(at);
            g.delete(at);

            if (spacer == 0)
                break;
            spaced += spacer;
        }
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {
        int spaced = 0;
        while (spaced <= sw) {
            FakeTransform at = g.getTransform();
            g.translate(convertP(1024 + spaced * 2, siz) + rect.x, convertP(4500 - midH, siz) - rect.y);
            ebg[1].drawBGEffect(g, origin, siz * 0.8f, 255, 1, 1);
            g.setTransform(at);
            g.delete(at);

            if (fspacer == 0)
                break;
            spaced += fspacer;
        }
    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        P pee = P.newP(0, y);
        preDraw(g, pee, siz, midH);
        postDraw(g, pee, siz, midH);
        P.delete(pee);
    }

    @Override
    public void update(int w, float h, float midH, float timeFlow) {
        check();
        ebg[0].update(false, timeFlow);
        ebg[1].update(false, timeFlow);
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {
        check();
        sw = w;
        ebg[0].setTime(0);
        ebg[1].setTime(0);
    }

    /**
     * Convert battle unit to pixel unit
     * @param p Position in battle
     * @param siz Size of battle
     * @return Converted pixel
     */
    protected static int convertP(double p, double siz) {
        return (int) (p * BattleRange.battleRatio * siz);
    }

    @Override
    public String toString() {
        if (getName().length() == 0)
            return id.toString();
        return Data.trio(id.id) + " - " + getName();
    }

    @Override
    public String getName() {
        return name;
    }
}
