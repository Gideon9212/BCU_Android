package common.util.unit;

import common.battle.StageBasis;
import common.battle.entity.EUnit;
import common.util.BattleObj;
import common.util.Data;

import java.util.ArrayList;
import java.util.HashMap;

public class ERUnit extends Data implements IForm {

    private final UniRand unit;
    private final Level level;

    public ERUnit(UniRand unit, int lv) {
        this.unit = unit;
        level = new Level();
        level.setLevel(lv);
    }

    public ERUnit(UniRand unit, Level lvs) {
        this.unit = unit;
        level = lvs;
    }

    @Override
    public EUnit getEntity(StageBasis b, int[] index, boolean isBase) {
        IForm unit = getSelection(b, true);
        if (unit == null)
            return null;
        return unit.getEntity(b, index, isBase);
    }

    @Override
    public EUnit invokeEntity(StageBasis b, int lvl, int minlayer, int maxlayer) {
        IForm unit = getSelection(b, false);
        if (unit == null)
            return null;
        return unit.invokeEntity(b, lvl, minlayer, maxlayer);
    }

    private IForm getSelection(StageBasis sb, boolean deploy) {
        if (unit.type != UniRand.T_NL) {
            ULock l = unit.map.get(sb);
            if (l == null)
                unit.map.put(sb, l = unit.type == UniRand.T_LL ? new ULockLL() : new ULockGL());
            IForm ae = l.get(this);
            if (ae == null)
                l.put(this, ae = selector(sb, deploy));
            return ae;
        }
        return selector(sb, deploy);
    }

    private IForm selector(StageBasis sb, boolean deploy) {
        int tot = 0;
        ArrayList<DH> iList = new ArrayList<>();
        for (int i = 0; i < unit.list.size(); i++) {
            if (unit.list.get(i).ent instanceof UniRand) {
                Level eLv = modifyLv(unit.list.get(i).lv.clone());
                int t = unit.list.get(i).share;
                iList.add(new DH(new ERUnit((UniRand)unit.list.get(i).ent, eLv), t));
                tot += t;
            } else {
                Form f = (Form) unit.list.get(i).ent;
                if (sb.cantDeploy(f.unit.rarity, f.du.getWill()))
                    continue;
                Level eLv = modifyLv(unit.list.get(i).lv.clone());
                EForm ef = new EForm(f, f.regulateLv(null, eLv));
                if (deploy && sb.st.lim != null && !sb.st.lim.unusable(ef.du, sb.st.getCont().price, (byte)0))
                    continue;
                DH h = new DH(ef, unit.list.get(i).share);
                iList.add(h);
                tot += h.share;
            }
        }

        if (iList.size() == 1)
            return iList.get(0).iform;
        if (tot > 0) {
            int r = sb.r.nextInt(tot);
            for (DH ent : iList) {
                r -= ent.share;
                if (r < 0)
                    return ent.iform;
            }
        }
        return null;
    }

    private Level modifyLv(Level eLv) {
        eLv.setLevel(eLv.getLv() + level.getLv());
        eLv.setPlusLevel(eLv.getPlusLv() + level.getPlusLv());
        int[] eT = eLv.getTalents();
        for (int k = 0; k < Math.min(eT.length, level.getTalents().length); k++)
            eT[k] += level.getTalents()[k];
        eLv.setTalents(eT);
        return eLv;
    }

    @Override
    public int getWill() {
        int wp = Integer.MAX_VALUE;
        Form[] fs = unit.getForms();
        for (Form f : fs)
            wp = Math.min(wp, f.du.getWill());
        return wp;
    }

    @Override
    public float getPrice(int sta) {
        return unit.price * (1 + sta * 0.5f);
    }

    @Override
    public int getRespawn() {
        return unit.cooldown;
    }

    static class DH {
        final IForm iform;
        final int share;

        public DH(IForm ifs, int sh) {
            iform = ifs;
            share = sh;
        }
    }
}

interface ULock {

    IForm get(Object obj);

    IForm put(Object obj, IForm ae);

}

class ULockGL extends BattleObj implements ULock {

    private IForm ae;

    @Override
    public IForm get(Object obj) {
        return ae;
    }

    @Override
    public IForm put(Object obj, IForm e) {
        IForm pre = ae;
        ae = e;
        return pre;
    }

}

class ULockLL extends HashMap<Object, IForm> implements ULock {

    private static final long serialVersionUID = 1L;

}
