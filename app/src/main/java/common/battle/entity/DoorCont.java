package common.battle.entity;

import common.battle.StageBasis;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.anim.AnimU;
import common.util.pack.EffAnim;

public class DoorCont extends EAnimCont {

    private final StageBasis bas;
    private final Entity ent;
    private boolean entLeft;
    public boolean drawn;

    public DoorCont(StageBasis b, Entity e) {
        super(e.pos, e.layer, effas().A_DOOR.getEAnim(EffAnim.DefEff.DEF));
        bas = b;
        ent = e;

        ent.getAnim().ent[0].b.EWarp = true;
        ent.getAnim().paraTo(getAnim(), 24);
    }

    @Override
    public void draw(FakeGraphics gra, P p, float psiz) {
        FakeTransform at = gra.getTransform();
        super.draw(gra, p, psiz);
        gra.setTransform(at);
        if (!entLeft)
            ent.getAnim().draw(gra, p, psiz);
        gra.delete(at);
        drawn = true;
    }

    @Override
    public void update(float flow) {
        super.update(flow);
        if (getAnim().ind() > 9) {
            if (getAnim().ind() < 18) {
                if (ent.getAnim().type == AnimU.TYPEDEF[AnimU.ENTRY])
                    ent.getAnim().update(false, flow);
            } else if (!entLeft) {
                ent.getAnim().paraTo(null);
                bas.le.add(ent);
                entLeft = true;
            }
        }
    }

    public int getWill() {
        return entLeft ? 0 : ent.data.getWill() + 1;
    }

    public boolean ECheck(Entity e) {
        return !drawn && e.layer >= layer;
    }
}
