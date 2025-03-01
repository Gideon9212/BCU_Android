package com.g2.bcu.androidutil;

import common.battle.data.MaskAtk;
import common.battle.data.MaskEntity;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.util.anim.AnimCI;
import common.util.anim.AnimU;
import common.util.anim.EAnimD;
import common.util.pack.DemonSoul;
import common.util.pack.EffAnim;
import common.util.pack.NyCastle;
import common.util.pack.Soul;
import common.util.unit.Enemy;
import common.util.unit.Form;
import common.util.unit.Unit;

public class StaticJava {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EAnimD<?> generateEAnimD(Object data, int form, int dataId) {
        try {
            if (data instanceof AnimCI) {
                AnimCI d = (AnimCI)data;
                return d.getEAnim(d.types()[dataId]);
            } else if(data instanceof EffAnim<?>) {
                ((EffAnim<?>) data).load();

                return new EAnimD((EffAnim<?>) data, ((EffAnim<?>) data).mamodel, ((EffAnim<?>) data).anims[dataId], ((EffAnim<?>) data).types[dataId]);
            } else if(data instanceof Soul) {
                ((Soul) data).anim.load();

                return new EAnimD(((Soul) data).anim, ((Soul) data).anim.mamodel, ((Soul) data).anim.anims[dataId], ((Soul) data).anim.types[dataId]);
            } else if(data instanceof NyCastle) {
                ((NyCastle) data).load();

                if(dataId == 0) {
                    return ((NyCastle) data).getEAnim(NyCastle.NyType.BASE);
                } else if(dataId == 1) {
                    return ((NyCastle) data).getEAnim(NyCastle.NyType.ATK);
                } else if(dataId == 2) {
                    return ((NyCastle) data).getEAnim(NyCastle.NyType.EXT);
                }
            } else if(data instanceof DemonSoul) {
                ((DemonSoul) data).anim.load();

                return new EAnimD(((DemonSoul) data).anim, ((DemonSoul) data).anim.mamodel, ((DemonSoul) data).anim.anims[dataId], ((DemonSoul) data).anim.types[dataId]);
            } else if(data instanceof Identifier<?>) {
                Object entity = ((Identifier<?>) data).get();

                if (entity != null) {
                    if (entity instanceof Unit) {
                        if (((Unit) entity).forms == null || ((Unit) entity).forms[form].anim == null) {
                            Form defaultForm = UserProfile.getBCData().units.get(0).forms[0];
                            return defaultForm.getEAnim(defaultForm.anim.types[0]);
                        }
                        return ((Unit) entity).forms[form].getEAnim(((Unit) entity).forms[form].anim.types()[dataId]);
                    } else if (entity instanceof Enemy) {
                        if (((Enemy) entity).anim == null) {
                            Enemy defaultEnemy = UserProfile.getBCData().enemies.get(0);
                            return defaultEnemy.getEAnim(defaultEnemy.anim.types[0]);
                        }
                        return ((Enemy) entity).getEAnim(((Enemy) entity).anim.types()[dataId]);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Try to generate EAnimD, but failed : \n\ndata = " + data + "\nform = " + form + "\ndataId = " + dataId);
        }
        return UserProfile.getBCData().units.get(0).forms[0].getEAnim(AnimU.TYPEDEF[AnimU.WALK]);
    }
    public static MaskAtk[] getAtkModel(MaskEntity ent, int index) {
        if (index < ent.getAtkTypeCount())
            return ent.getAtks(index);
        return ent.getSpAtks(true, index-ent.getAtkTypeCount());
    }
    public static int spAtkCount(MaskEntity ent) {
        MaskAtk[][] stks = ent.getSpAtks(true);
        for (int i = stks.length - 1; i >= 0; i--)
            if (stks[i].length > 0)
                return i+1;
        return 0;
    }
    public static int allAtk(MaskEntity ent, int index) {
        if (index < ent.getAtkTypeCount())
            return ent.allAtk(index);
        MaskAtk[] atks = ent.getSpAtks(true, index-ent.getAtkTypeCount());
        int dmg = 0;
        for (MaskAtk at : atks)
            if (at.getDire() == 1)
                dmg += at.getAtk();
        return dmg;
    }
}
