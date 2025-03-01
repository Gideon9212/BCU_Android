package common.system;

import common.util.unit.AbEnemy;
import common.util.unit.Enemy;

import java.util.List;

public class ENode extends Node<Enemy> {

    public ENode(Enemy v) {
        super(v);
        mul = 100;
        mula = 100;
    }

    public ENode(Enemy v, int[] muls) {
        super(v);
        mul = muls[0];
        mula = muls[1];
    }

    public static ENode getListE(List<AbEnemy> list, AbEnemy enemy) {
        ENode ans = null, ret = null;
        for (AbEnemy e : list) {
            if (!(e instanceof Enemy))
                continue;
            ENode temp = new ENode((Enemy) e);
            if (ans != null)
                ans.add(temp);
            if (e == enemy)
                ret = temp;
            ans = temp;
        }
        return ret;
    }

    public static ENode getListE(List<Enemy> list, AbEnemy enemy, List<int[]> muls) {
        ENode ans = null, ret = null;
        for (int i = 0; i < list.size(); i++) {
            ENode temp = new ENode(list.get(i), muls.get(i));
            if (ans != null)
                ans.add(temp);
            if (list.get(i) == enemy)
                ret = temp;
            ans = temp;
        }
        return ret;
    }

    public final int mul, mula;
}
