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
public class BalloonBGEffect extends BackgroundEffect {

    private FakeImage balloon;
    private FakeImage bigBalloon;

    private final List<P> balloonPosition = new ArrayList<>();
    private final List<Boolean> isBigBalloon = new ArrayList<>();
    private final List<Float> speed = new ArrayList<>();
    private final Random r = new Random();

    private final List<Integer> capture = new LinkedList<>();

    public BalloonBGEffect(Identifier<BackgroundEffect> id) {
        super(id);
    }

    @Override
    public void check() {
        if(balloon != null)
            balloon.bimg();

        if(bigBalloon != null)
            bigBalloon.bimg();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {

    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {
        for(int i = 0; i < balloonPosition.size(); i++) {
            FakeImage img = isBigBalloon.get(i) ? bigBalloon : balloon;

            g.drawImage(
                    img,
                    BackgroundEffect.convertP((float)(balloonPosition.get(i).x + Data.BG_EFFECT_BALLOON_FACTOR * Math.sin(balloonPosition.get(i).y / Data.BG_EFFECT_BALLOON_STABILIZER)), siz) + (int) rect.x,
                    (int) (balloonPosition.get(i).y * siz - rect.y + midH * siz),
                    img.getWidth() * siz,
                    img.getHeight() * siz
            );
        }
    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        for(int i = 0; i < balloonPosition.size(); i++) {
            FakeImage img = isBigBalloon.get(i) ? bigBalloon : balloon;
            g.drawImage(
                    img,
                    BackgroundEffect.convertP((float)(balloonPosition.get(i).x + Data.BG_EFFECT_BALLOON_FACTOR * Math.sin(balloonPosition.get(i).y / Data.BG_EFFECT_BALLOON_STABILIZER)), siz),
                    (int) (balloonPosition.get(i).y * siz - y + midH * siz),
                    img.getWidth() * siz,
                    img.getHeight() * siz
            );
        }
    }

    @Override
    public void update(int w, float h, float midH, float timeFlow) {
        capture.clear();

        for(int i = 0; i < balloonPosition.size(); i++) {
            int bh = isBigBalloon.get(i) ? bigBalloon.getHeight() : balloon.getHeight();

            if(balloonPosition.get(i).y < -bh)
                capture.add(i);
            else
                balloonPosition.get(i).y -= speed.get(i) * timeFlow;
        }

        if(!capture.isEmpty()) {
            for (Integer integer : capture) {
                boolean isBig = r.nextBoolean();

                int bw = isBig ? bigBalloon.getWidth() : balloon.getWidth();

                balloonPosition.get(integer).x = (r.nextInt(w + battleOffset + 2 * BackgroundEffect.revertP(bw)) - BackgroundEffect.revertP(bw)) * timeFlow;
                balloonPosition.get(integer).y = BGHeight * 3;
                isBigBalloon.set(integer, isBig);
            }
        }
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {
        for (P p : balloonPosition)
            P.delete(p);

        balloonPosition.clear();
        isBigBalloon.clear();

        balloon = null;
        bigBalloon = null;

        Background background;

        if(!bg.id.pack.equals(Identifier.DEF) || bg.bgEffect.id != Data.BG_EFFECT_BALLOON)
            background = UserProfile.getBCData().bgs.get(81);
        else
            background = bg;
        background.load();

        balloon = background.parts[20];
        bigBalloon = background.parts[21];

        int number = w / 400;

        for(int i = 0; i < number; i++) {
            boolean isBig = r.nextBoolean();

            int bw = isBig ? bigBalloon.getWidth() : balloon.getWidth();

            balloonPosition.add(P.newP(r.nextInt(w + battleOffset + 2 * BackgroundEffect.revertP(bw)) - BackgroundEffect.revertP(bw), r.nextInt(BGHeight) * 3));
            isBigBalloon.add(isBig);
            if (CommonStatic.getConfig().fps60)
                speed.add(Data.BG_EFFECT_BALLOON_SPEED / 2f);
            else
                speed.add(Data.BG_EFFECT_BALLOON_SPEED);
        }
    }

    @Override
    public String toString() {
        return CommonStatic.def.getUILang(0, "bgeff" + id.id);
    }
}
