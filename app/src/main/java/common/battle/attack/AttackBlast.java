package common.battle.attack;

import common.battle.entity.AbEntity;
import common.battle.entity.Entity;

import java.util.LinkedList;
import java.util.List;

public class AttackBlast extends AttackAb {

    public final float reduction;
    public int lv = 0;
    public int raw;
    public boolean attacked = false;
    private final LinkedList<AbEntity> ents = new LinkedList<>();

    protected AttackBlast(Entity attacker, AttackSimple src, float pos, int bt) {
        super(attacker, src, pos + 75, pos - 75, false);
        waveType = bt;
        reduction = src.getProc().BLAST.reduction;
        raw = ((AtkModelEntity)model).getDefAtk(matk);
    }

    @Override
    public void capture() {
        capt.clear();
        float rng = lv == 0 ? 0 : 25 + (100 * lv);

        List<AbEntity> le = model.b.inRange(touch, attacker != null && attacker.status.rage > 0 ? 2 : dire, sta + rng, end + rng, excludeRightEdge);
        if (lv > 0) {
            List<AbEntity> nle = model.b.inRange(touch, attacker != null && attacker.status.rage > 0 ? 2 : dire, sta - rng, end - rng, excludeRightEdge);
            nle.removeIf(le::contains);
            le.addAll(nle);
        }
        le.remove(dire == 1 ? model.b.ubase : model.b.ebase);
        capt.removeIf(ents::contains);
        if ((abi & AB_ONLY) == 0)
            capt.addAll(le);
        else
            for (AbEntity e : le)
                if (e.ctargetable(trait, attacker))
                    capt.add(e);
        ents.addAll(capt);
    }

    @Override
    public void excuse() {
        atk = ((AtkModelEntity)model).getEffMult(raw);
        atk -= (int)(lv * reduction / 100 * atk);

        for (AbEntity e : capt)
            e.damaged(this);
        attacked = true;
    }

    public void next() {
        if (++lv == 1) {
            sta -= 25;
            end += 25;
        }
    }
}
