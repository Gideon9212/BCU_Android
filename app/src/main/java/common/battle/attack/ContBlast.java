package common.battle.attack;

import common.CommonStatic;
import common.battle.entity.AbEntity;
import common.battle.entity.Entity;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.anim.EAnimD;
import common.util.pack.EffAnim.BlastEff;

import java.util.ArrayList;

public class ContBlast extends ContAb {
    private final AttackBlast blast;
    private final int maxl;
    protected final ArrayList<EAnimD<BlastEff>> anims;
    private int t = 0;

    protected ContBlast(AttackBlast atkBlast, float p, int lay) {
        super(atkBlast.model.b, p, lay);
        blast = atkBlast;
        maxl = blast.getProc().BLAST.lv;
        EAnimD<BlastEff> anim = (blast.dire == 1 ? effas().A_E_BLAST : effas().A_BLAST).getEAnim(BlastEff.START);
        anim.setTime(1);
        anims = new ArrayList<>(maxl * 2 - 1);
        anims.add(anim);
    }

    @Override
    public void draw(FakeGraphics gra, P p, float psiz) {
        FakeTransform at = gra.getTransform();
        for (int i = anims.size() - 1; i >= Math.max(0, anims.size() - 4); i--) {
            if (i > 0) {
                anims.get(i).ent[3].alter(53, 400 + (100 - (blast.lv * blast.reduction)) * 6);
                anims.get(i).ent[3].alter(4, 10 + 40 * ((i+1) / 2));
            }
            anims.get(i).draw(gra, p, psiz);
            gra.setTransform(at);
        }
        if (CommonStatic.getConfig().ref && blast.lv < maxl)
            drawAxis(gra, p, psiz * 1.25f);
    }

    public void drawAxis(FakeGraphics gra, P p, float siz) {
        float rat = CommonStatic.BattleConst.ratio;
        int h = (int) (640 * rat * siz);
        gra.setColor(FakeGraphics.MAGENTA);

        float d0 = Math.min(blast.sta, blast.end);
        float ra = Math.abs(blast.sta - blast.end);
        int x = (int) ((d0 - pos) * rat * siz + p.x);
        int y = (int) p.y;
        int w = (int) (ra * rat * siz);

        int ch = blast.lv == 0 ? 0 : (int)((25 + 100 * blast.lv) * rat * siz);
        if (blast.attacked) {
            gra.fillRect(x + ch, y, w, h);
            gra.fillRect(x - ch, y, w, h);
        } else {
            gra.drawRect(x + ch, y, w, h);
            gra.drawRect(x - ch, y, w, h);
        }
    }

    @Override
    public void update() { // FIXME: update on same frame as attack
        t++;
        blast.attacked = false;
        int rt = t - BLAST_PRE;
        if (rt >= 0 && blast.lv < maxl) {
            if (rt == 0)
                anims.get(0).changeAnim(maxl == 3 ? BlastEff.EXPLODE : BlastEff.SINGLE, true);
            int qrt = (t - 1) % BLAST_ITV;
            if (qrt == 0) {
                if (rt > 0)
                    blast.next();
                if (blast.lv < maxl) {
                    CommonStatic.setSE(EXPLOSION_SE + Math.max(0, 3 - (maxl - blast.lv)));
                    if (blast.lv > 0 && maxl != 3) {
                        EAnimD<BlastEff> a = (blast.dire == 1 ? effas().A_E_BLAST : effas().A_BLAST).getEAnim(BlastEff.SINGLE);
                        a.ent[3].b.revert();
                        anims.add((blast.dire == 1 ? effas().A_E_BLAST : effas().A_BLAST).getEAnim(BlastEff.SINGLE));
                        anims.add(a);
                    }
                }
            } else if (qrt == 3) {
                blast.capture();
                for (AbEntity e : blast.capt)
                    if (e instanceof Entity) {
                        float blo = e.getProc().IMUBLAST.block;
                        if (blo != 0) {
                            if (blo > 0)
                                ((Entity)e).anim.getEff(STPWAVE);
                            if (blo == 100) {
                                deactivate(e);
                                return;
                            } else
                                blast.raw = (int) (blast.raw * (100 - blo) / 100);
                        }
                    }
                blast.excuse();
            }
        }
        if (anims.get(anims.size() - 1).done())
            activate = false;
        updateAnimation();
    }

    /**
     * kill every related blast
     */
    protected void deactivate(AbEntity e) {
        if (e.getProc().IMUBLAST.mult < 0) {
            e.getProc().IMUBLAST.mult += 100;
            e.damaged(blast);
            e.getProc().IMUBLAST.mult -= 100;
        }
        activate = false;
    }

    @Override
    public void updateAnimation() {
        for (int i = anims.size() - 1; i >= Math.max(0, anims.size() - 4); i--)
            anims.get(i).update(false);
    }

    @Override
    public boolean IMUTime() {
        return false;
    }
}
