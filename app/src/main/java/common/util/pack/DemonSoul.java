package common.util.pack;

import common.util.Animable;
import common.util.anim.AnimU;
import common.util.anim.EAnimI;
import org.jetbrains.annotations.NotNull;

public class DemonSoul extends Animable<AnimU<?>, AnimU.UType> implements Comparable<DemonSoul> {

    private final int id;
    boolean e;

    public DemonSoul(int id, AnimU<?> animS, boolean enemy) {
        anim = animS;
        this.id = id;
        e = enemy;

        if (!enemy) {
            anim.partial();
            anim.revert();
        }
    }

    @Override
    public String toString() {
        return (e ? "enemy " : "") + "demonsoul " + id;
    }

    @Override
    public int compareTo(@NotNull DemonSoul o) {
        return Integer.compare(id, o.id);
    }

    @Override
    public EAnimI getEAnim(AnimU.UType uType) {
        return anim.getEAnim(uType);
    }
}