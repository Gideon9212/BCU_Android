package common.battle.entity;

import common.CommonStatic;
import common.battle.attack.AttackVolcano;
import common.util.pack.EffAnim;

public class DemonCont extends EAnimCont {

    private final Entity ent;
    private final Proc.VOLC volc;
    private byte played = 0;

    public DemonCont(Entity e, AttackVolcano atk) {
        super(e.pos, e.layer, (e.dire == -1 ? effas().A_COUNTERSURGE : effas().A_E_COUNTERSURGE).getEAnim(EffAnim.DefEff.DEF));
        if ((atk.waveType & WT_VOLC) > 0)
            volc = atk.handler.ds ? atk.attacker.getProc().DEATHSURGE : atk.getProc().VOLC;
        else
            volc = atk.getProc().MINIVOLC;
        ent = e;
        e.basis.lea.add(this);
    }

    @Override
    public void update(float flow) {
        super.update(flow);
        if (played == 1 && getAnim().ind() >= COUNTER_SURGE_FORESWING) {
            ent.aam.getCounterSurge(pos, volc);
            played++;
        } else if (played == 0 && getAnim().ind() >= COUNTER_SURGE_SOUND) {
            CommonStatic.setSE(SE_COUNTER_SURGE);
            played++;
        }
    }
}
