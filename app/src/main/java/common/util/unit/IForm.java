package common.util.unit;

import common.battle.StageBasis;
import common.battle.entity.EUnit;

public interface IForm {

    static IForm newIns(AbForm form, int lv) {
        if (form instanceof Form)
            return new EForm((Form) form, lv);
        return new ERUnit((UniRand) form, lv);
    }

    static IForm newIns(AbForm form, Level lvs) {
        if (form instanceof Form)
            return new EForm((Form) form, lvs);
        return new ERUnit((UniRand) form, lvs);
    }

    EUnit getEntity(StageBasis b, int[] index, boolean isBase);

    EUnit invokeEntity(StageBasis b, int lvl, int minlayer, int maxlayer);

    int getWill();

    float getPrice(int sta);

    int getRespawn();
}
