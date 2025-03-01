package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeTransform;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@JsonClass.JCGeneric(Identifier.class)
public class RockBGEffect extends BackgroundEffect {
    private FakeImage rock;
    private FakeImage segment;

    private final List<P> rockPosition = new ArrayList<>();
    private final List<Float> speed = new ArrayList<>();
    private final List<Float> size = new ArrayList<>();
    private final List<Boolean> isRock = new ArrayList<>();
    private final List<Float> angle = new ArrayList<>();
    //0 : Behind, 2 : Front
    private final List<Byte> layer = new ArrayList<>();
    private final List<Integer> opacity = new ArrayList<>();

    private final List<Integer> capture = new LinkedList<>();
    private final Random r = new Random();

    boolean vibrate = false;

    public RockBGEffect(Identifier<BackgroundEffect> id) {
        super(id);
    }

    @Override
    public void check() {
        if(rock != null)
            rock.bimg();

        if(segment != null)
            segment.bimg();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {
        FakeTransform at = g.getTransform();

        for(int i = 0; i < rockPosition.size(); i++) {
            if(layer.get(i) == 0) {
                g.setComposite(FakeGraphics.TRANS, opacity.get(i), 0);

                FakeImage img = isRock.get(i) ? rock : segment;
                float s = size.get(i);

                g.translate(BackgroundEffect.convertP(rockPosition.get(i).x, siz) + (int) rect.x, (int) (rockPosition.get(i).y * siz - rect.y));
                g.rotate(angle.get(i));
                g.drawImage(img, 0, 0, (int) (img.getWidth() * s * siz), (int) (img.getHeight() * s * siz));

                g.setTransform(at);
            }
        }

        g.setComposite(FakeGraphics.DEF, 255, 0);

        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {
        FakeTransform at = g.getTransform();

        for(int i = 0; i < rockPosition.size(); i++) {
            if(layer.get(i) == 1) {
                FakeImage img = isRock.get(i) ? rock : segment;
                float s = size.get(i);

                g.translate(BackgroundEffect.convertP(rockPosition.get(i).x + (vibrate ? 2 : -2), siz) + (int) rect.x, (int) (rockPosition.get(i).y * siz - rect.y + midH * siz));
                g.rotate(angle.get(i));
                g.drawImage(img, 0, 0, img.getWidth() * s * siz, img.getHeight() * s * siz);

                g.setTransform(at);
            }
        }

        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        FakeTransform at = g.getTransform();
        for(int i = 0; i < rockPosition.size(); i++) {
            if(layer.get(i) == 0) {
                g.setComposite(FakeGraphics.TRANS, opacity.get(i), 0);

                FakeImage img = isRock.get(i) ? rock : segment;
                float s = size.get(i);

                g.translate(BackgroundEffect.convertP(rockPosition.get(i).x, siz), (int) (rockPosition.get(i).y * siz - y));
                g.rotate(angle.get(i));
                g.drawImage(img, 0, 0, img.getWidth() * s * siz, img.getHeight() * s * siz);

                g.setTransform(at);
            }
        }
        g.setComposite(FakeGraphics.DEF, 255, 0);
        for(int i = 0; i < rockPosition.size(); i++) {
            if(layer.get(i) == 1) {
                FakeImage img = isRock.get(i) ? rock : segment;
                float s = size.get(i);

                g.translate(BackgroundEffect.convertP(rockPosition.get(i).x + (vibrate ? 2 : -2), siz), (int) (rockPosition.get(i).y * siz - y + midH * siz));
                g.rotate(angle.get(i));
                g.drawImage(img, 0, 0, img.getWidth() * s * siz, img.getHeight() * s * siz);

                g.setTransform(at);
            }
        }

        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void update(int w, float h, float midH, float timeFlow) {
        capture.clear();

        vibrate = !vibrate;

        for(int i = 0; i < rockPosition.size(); i++) {
            float s = size.get(i);
            int rh = isRock.get(i) ? rock.getHeight() : segment.getHeight();

            if(rockPosition.get(i).y < -rh * s) {
                capture.add(i);
            } else {
                rockPosition.get(i).y -= speed.get(i) * timeFlow;

                if(layer.get(i) != 1&& opacity.get(i) != 255) {
                    opacity.set(i, Math.min(255, opacity.get(i) + 5));
                }
            }
        }

        if(!capture.isEmpty())
            for (Integer capt : capture) {
                byte l = (byte) (r.nextFloat() >= 1 / 4.0 ? 0 : 1);
                boolean isR = l != 1 && r.nextFloat() >= 0.9;
                float siz = (Data.BG_EFFECT_ROCK_SIZE[l] - r.nextFloat() * (1 - 0.5f * l)) * (isR ? 0.8f : 1.0f);

                int rw = (int) ((isR ? rock.getWidth() : segment.getWidth()) * siz);

                rockPosition.get(capt).x = timeFlow * r.nextInt(w + battleOffset + 2 * rw) - rw;
                rockPosition.get(capt).y = l == 0 ? 1020 + Data.BG_EFFECT_ROCK_BEHIND_SPAWN_OFFSET : BGHeight * 3;
                isRock.set(capt, isR);
                angle.set(capt,(float) (r.nextFloat() * Math.PI) * timeFlow);
                layer.set(capt, l);
                size.set(capt, siz);
                speed.set(capt, CommonStatic.fltFpsDiv(Data.BG_EFFECT_ROCK_SPEED[l] - r.nextFloat() * 0.5f * timeFlow));
                opacity.set(capt, l == 0 ? 0 : 255);
            }
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {
        for (P p : rockPosition)
            P.delete(p);

        rockPosition.clear();
        speed.clear();
        size.clear();
        isRock.clear();
        angle.clear();
        layer.clear();

        rock = null;
        segment = null;

        Background background;

        if(!bg.id.pack.equals(Identifier.DEF) || (bg.id.id != 41 && bg.id.id != 75)) {
            background = UserProfile.getBCData().bgs.get(75);
        } else {
            background = bg;
        }

        background.load();

        if(background.id.id == 41) {
            rock = background.parts[21];
            segment = background.parts[20];
        } else {
            rock = background.parts[22];
            segment = background.parts[21];
        }

        int number = w / 100;

        for(int i = 0; i < number; i++) {
            byte l = (byte) (r.nextFloat() >= 0.25 ? 0 : 1);
            boolean isR = l != 1 && r.nextFloat() >= 0.9;
            float siz = (Data.BG_EFFECT_ROCK_SIZE[l] - r.nextFloat() * (1 - 0.5f * l)) * (isR ? 0.8f : 1f);

            int rw = (int) ((isR ? rock.getWidth() : segment.getWidth()) * siz);

            rockPosition.add(P.newP(r.nextInt(w + battleOffset + 2 * rw) - rw, r.nextInt(l == 0 ? 1020 + Data.BG_EFFECT_ROCK_BEHIND_SPAWN_OFFSET : BGHeight * 3)));
            speed.add(CommonStatic.fltFpsDiv(Data.BG_EFFECT_ROCK_SPEED[l] - r.nextFloat() * 0.5f));

            isRock.add(isR);
            angle.add((float) (r.nextFloat() * Math.PI));
            layer.add(l);
            size.add(siz);
            opacity.add(255);
        }
    }

    @Override
    public String toString() {
        return CommonStatic.def.getUILang(0, "bgeff" + id.id);
    }
}
