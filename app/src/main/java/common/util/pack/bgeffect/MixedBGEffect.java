package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class MixedBGEffect extends BackgroundEffect {

    @JsonField
    public String name = "";
    @JsonField(generic = BackgroundEffect.class, alias = Identifier.class, defval = "isEmpty")
    public final ArrayList<BackgroundEffect> effects = new ArrayList<>();

    @JsonClass.JCConstructor
    public MixedBGEffect() {
        super(null);
    }

    public MixedBGEffect(Identifier<BackgroundEffect> id, BackgroundEffect... effects) {
        super(id);
        name = "BGEffect " + id;
        this.effects.addAll(Arrays.asList(effects));
    }

    public MixedBGEffect(Identifier<BackgroundEffect> id, List<BackgroundEffect> effects) {
        super(id);
        name = "BGEffect " + id;
        this.effects.addAll(effects);
    }

    @Override
    public void check() {
        for (BackgroundEffect effect : effects)
            effect.check();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {
        for (BackgroundEffect effect : effects)
            effect.preDraw(g, rect, siz, midH);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {
        for (BackgroundEffect effect : effects)
            effect.postDraw(g, rect, siz, midH);
    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        for (BackgroundEffect effect : effects)
            effect.draw(g, y, siz, midH);
    }

    @Override
    public void update(int w, float h, float midH, float timeFlow) {
        for (BackgroundEffect effect : effects)
            effect.update(w, h, midH, timeFlow);
    }

    @Override
    public void updateAnimation(int w, float h, float midH, float timeFlow) {
        for (BackgroundEffect effect : effects)
            effect.updateAnimation(w, h, midH, timeFlow);
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {
        for (BackgroundEffect effect : effects)
            effect.initialize(w, h, midH, bg);
    }

    @Override
    public void release() {
        for (BackgroundEffect effect : effects)
            effect.release();
    }

    @Override
    public String toString() {
        if (id.pack.equals(Identifier.DEF)) {
            if (id.id == Data.BG_EFFECT_SNOWSTAR)
                return CommonStatic.def.getUILang(0, "bgeff5");


            String temp = CommonStatic.def.getUILang(0, "bgjson" + id.id);
            if (temp.equals("bgjson" + id.id))
                temp = CommonStatic.def.getUILang(0, "bgeffdum").replace("_", "" + id.id);
            return temp;
        }
        if (getName().isEmpty())
            return id.toString();
        return Data.trio(id.id) + " - " + getName();
    }

    @Override
    public String getName() {
        return name;
    }
}
