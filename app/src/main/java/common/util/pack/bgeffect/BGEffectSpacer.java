package common.util.pack.bgeffect;

import common.pack.UserProfile;
import common.system.BattleRange;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.anim.EAnimD;
import common.util.pack.Background;

public class BGEffectSpacer {
    public final int pos0, pos1, value;
    public final BattleRange.SNAP snap;

    public BGEffectSpacer(int pos0, int pos1, int value, BattleRange.SNAP snap, int bgID) {
        this.pos0 = pos0;
        this.pos1 = pos1;
        this.snap = snap;

        if(snap == BattleRange.SNAP.DEFAULT) {
            this.value = value;
        } else {
            Background bg = UserProfile.getBCData().bgs.get(bgID);

            bg.load();

            this.value = bg.parts[Background.BG].getWidth() * value;
        }
    }

    public void drawWithSpacer(FakeGraphics g, P rect, float siz, BGEffectHandler handler, int index) {
        FakeTransform at = g.getTransform();

        EAnimD<BGEffectAnim.BGEffType> anim = handler.animation.get(index);

        for(int i = pos0; i <= pos1; i++) {
            if(snap == BattleRange.SNAP.DEFAULT) {
                g.translate(
                        convertP(handler.position[index].x + value * i * 4, siz) + rect.x,
                        convertP(handler.position[index].y, siz) - rect.y
                );
            } else {
                g.translate(
                        convertP(handler.position[index].x, siz) + value * i * siz + rect.x,
                        convertP(handler.position[index].y, siz) - rect.y
                );
            }

            g.rotate(handler.angle[index]);

            anim.drawBGEffect(g, BGEffectHandler.origin, siz * 0.8f, handler.opacity[index], handler.size[index].x, handler.size[index].y);

            g.setTransform(at);
        }

        g.delete(at);
    }

    /**
     * Convert battle unit to pixel unit
     * @param p Position in battle
     * @param siz Size of battle
     * @return Converted pixel
     */
    private int convertP(float p, float siz) {
        return (int) (p * BattleRange.battleRatio * siz);
    }
}
