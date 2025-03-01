package common.util.unit;

import com.google.gson.JsonObject;
import common.battle.data.AtkDataModel;
import common.battle.data.CustomEntity;
import common.battle.data.MaskEntity;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.io.json.localDecoder;
import common.pack.Identifier;
import common.pack.PackData;
import common.pack.PackData.UserPack;
import common.pack.UserProfile;
import common.system.VImg;
import common.util.Animable;
import common.util.anim.AnimU;
import common.util.anim.EAnimU;
import common.util.lang.MultiLangData;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public abstract class Character extends Animable<AnimU<?>, AnimU.UType> {
    @JsonField(generic = MultiLangData.class, gen = JsonField.GenType.FILL, defval = "empty")
    public final MultiLangData names = new MultiLangData();
    @JsonField(generic = MultiLangData.class, gen = JsonField.GenType.FILL, defval = "empty")
    public final MultiLangData description = new MultiLangData();
    @JsonField(defval = "false")
    public boolean rev = false;

    @Override
    public EAnimU getEAnim(AnimU.UType t) {
        if (anim == null)
            return null;
        EAnimU a = anim.getEAnim(t);
        a.setDir(rev);
        return a;
    }

    public VImg getIcon() {
        if(anim == null)
            return null;
        return anim.getEdi();
    }
    public VImg getPreview() {
        if(anim == null)
            return null;
        return anim.getPreviewIcon();
    }

    public abstract MaskEntity getMask();

    public abstract Identifier<?> getID();

    /**
     * Handles most of the injected parameters of enemies and forms
     * @param pack The bcupack
     * @param jdu Json Object containing the data
     * @param ent The customEntity
     */
    protected void inject(UserPack pack, JsonObject jdu, CustomEntity ent) {
        if (pack.desc.FORK_VERSION < 11) {
            if (pack.desc.FORK_VERSION < 9) {
                AtkDataModel[] atks = null;
                if (pack.desc.FORK_VERSION < 6) {
                    if (pack.desc.FORK_VERSION < 4) {
                        if (pack.desc.FORK_VERSION < 3) {
                            if (pack.desc.FORK_VERSION < 2) {
                                AtkDataModel[] oldAtks = new localDecoder(jdu.getAsJsonObject("atks"), AtkDataModel[].class, ent).setGen(true).setPool(true).decode();
                                ent.hits.set(0, oldAtks);
                            } //Finish FORK_VERSION 2 checks
                            AtkDataModel oldSpAtk = new localDecoder(jdu.get("rev"), AtkDataModel.class, ent).setGen(true).decode();
                            if (oldSpAtk != null)
                                ent.revs = new AtkDataModel[]{oldSpAtk};
                            oldSpAtk = new localDecoder(jdu.get("res"), AtkDataModel.class, ent).setGen(true).decode();
                            if (oldSpAtk != null)
                                ent.ress = new AtkDataModel[]{oldSpAtk};
                            oldSpAtk = new localDecoder(jdu.get("bur"), AtkDataModel.class, ent).setGen(true).decode();
                            if (oldSpAtk != null)
                                ent.burs = new AtkDataModel[]{oldSpAtk};
                            oldSpAtk = new localDecoder(jdu.get("resu"), AtkDataModel.class, ent).setGen(true).decode();
                            if (oldSpAtk != null)
                                ent.resus = new AtkDataModel[]{oldSpAtk};
                            oldSpAtk = new localDecoder(jdu.get("revi"), AtkDataModel.class, ent).setGen(true).decode();
                            if (oldSpAtk != null)
                                ent.revis = new AtkDataModel[]{oldSpAtk};
                            oldSpAtk = new localDecoder(jdu.get("entr"), AtkDataModel.class, ent).setGen(true).decode();
                            if (oldSpAtk != null)
                                ent.entrs = new AtkDataModel[]{oldSpAtk};

                            atks = ent.getAllAtkModels();
                            Proc proc = ent.getProc();
                            //Updates stuff to match this fork without core version issues
                            if (pack.desc.FORK_VERSION < 1) {
                                if (UserProfile.isOlderPack(pack, "0.6.6.0")) {
                                    if (UserProfile.isOlderPack(pack, "0.6.5.0")) {
                                        if (UserProfile.isOlderPack(pack, "0.6.1.0")) {
                                            if (UserProfile.isOlderPack(pack, "0.6.0.0")) {
                                                int type = jdu.get("type").getAsInt();
                                                if (UserProfile.isOlderPack(pack, "0.5.2.0")) {
                                                    if (UserProfile.isOlderPack(pack, "0.5.1.0"))
                                                        type = reorderTrait(type);
                                                    //Finish 0.5.1.0 check
                                                    if (ent.tba != 0)
                                                        ent.tba += ent.getPost(false, 0) + 1;
                                                } //Finish 0.5.2.0 check
                                                proc.BARRIER.health = jdu.get("shield").getAsInt();
                                                ent.traits = Trait.convertType(type, false);
                                                if ((ent.abi & (1 << 18)) != 0) //Seal Immunity
                                                    proc.IMUSEAL.mult = 100;
                                                if ((ent.abi & (1 << 7)) != 0) //Moving atk Immunity
                                                    proc.IMUMOVING.mult = 100;
                                                if ((ent.abi & (1 << 12)) != 0) //Poison Immunity
                                                    proc.IMUPOI.mult = 100;
                                                ent.abi = reorderAbi(ent.abi, 0);
                                                for (AtkDataModel atk : atks)
                                                    atk.alt = reorderAbi(atk.alt, 0);
                                            } //Finish 0.6.0.0 check
                                            proc.DMGCUT.reduction = 100;
                                            for (AtkDataModel atk : atks)
                                                if (atk.getProc().POISON.prob > 0)
                                                    atk.getProc().POISON.type.ignoreMetal = true;
                                        } //Finish 0.6.1.0 check
                                        boolean bounty = (ent.abi & 16) > 0;
                                        boolean atkbase = (ent.abi & 32) > 0;
                                        for (AtkDataModel atk : atks) {
                                            atk.alt = reorderAbi(atk.alt, 1);
                                            if (bounty) //2x money
                                                atk.getProc().BOUNTY.mult = 100;
                                            if (atkbase) //base destroyer
                                                atk.getProc().ATKBASE.mult = 300;
                                        }
                                        ent.abi = reorderAbi(ent.abi, 1);
                                    } //Finish 0.6.5.0 check
                                    for (AtkDataModel atk : atks)
                                        if (atk.getProc().TIME.prob > 0)
                                            atk.getProc().TIME.intensity = atk.getProc().TIME.time;

                                    for (AtkDataModel atk : atks)
                                        if (atk.getProc().SUMMON.prob > 0) {
                                            atk.getProc().SUMMON.max_dis = atk.getProc().SUMMON.dis;
                                            atk.getProc().SUMMON.min_layer = -1;
                                            atk.getProc().SUMMON.max_layer = -1;
                                        }
                                } //Finish 0.6.6.0 check
                                if ((ent.abi & 32) > 0)
                                    proc.IMUWAVE.block = 100;
                                if ((ent.abi & 524288) > 0) {
                                    proc.DEMONVOLC.prob = 100;
                                    proc.DEMONVOLC.mult = 100;
                                }
                                ent.abi = reorderAbi(ent.abi, 2);
                                for (AtkDataModel atk : atks)
                                    atk.alt = reorderAbi(atk.alt, 2);
                            } //Finish FORK_VERSION 1 checks
                        } //Finish FORK_VERSION 3 checks
                        description.replace("<br>", "\n");
                    } //Finish FORK_VERSION 4 checks
                    for (AtkDataModel atk : ent.getAllAtkModels())
                        if (atk.pre == 0 && atk.str.toLowerCase().startsWith("combo"))
                            atk.str = "NC- " + atk.str;
                } //Finish FORK_VERSION 6 checks
                ent.getProc().DMGINC.mult = 100;
                ent.getProc().DEFINC.mult = 100;
                if ((ent.abi & 1) != 0) {
                    ent.getProc().DMGINC.mult *= 1.5;
                    ent.getProc().DEFINC.mult *= 2;
                }
                if ((ent.abi & 2) != 0)//res
                    ent.getProc().DEFINC.mult *= 4;
                if ((ent.abi & 4) != 0)//mas dmg
                    ent.getProc().DMGINC.mult *= 3;
                if ((ent.abi & 16384) != 0)//ins res
                    ent.getProc().DEFINC.mult *= 6;
                if ((ent.abi & 32768) != 0)//ins dmg
                    ent.getProc().DMGINC.mult *= 5;

                ent.abi = reorderAbi(ent.abi, 3);
                if (atks == null)
                    atks = ent.getAllAtkModels();
                for (AtkDataModel atk : atks)
                    atk.alt = reorderAbi(atk.alt, 3);

                if (ent.getProc().DMGINC.mult == 100)
                    ent.getProc().DMGINC.mult = 0;
                if (ent.getProc().DEFINC.mult == 100)
                    ent.getProc().DEFINC.mult = 0;
            } //Finish FORK_VERSION 9 checks
            for (AtkDataModel atk : ent.getAllAtkModels())
                if (atk.getProc().TIME.intensity != 0)
                    atk.getProc().TIME.intensity = (atk.getProc().TIME.intensity / atk.getProc().TIME.time) * 100;
        } //Finish FORK_VERSION 11 checks
    }

    public abstract String getExplanation();

    private static int reorderTrait(int oldTrait) {
        int newTrait = 0;

        for(int i = 0; i < TRAIT_TOT; i++) {
            if(((oldTrait >> i) & 1) > 0) {
                switch (i) {
                    case 0:
                        newTrait |= TB_WHITE;
                        break;
                    case 1:
                        newTrait |= TB_RED;
                        break;
                    case 2:
                        newTrait |= TB_FLOAT;
                        break;
                    case 3:
                        newTrait |= TB_BLACK;
                        break;
                    case 4:
                        newTrait |= TB_METAL;
                        break;
                    case 5:
                        newTrait |= TB_ANGEL;
                        break;
                    case 6:
                        newTrait |= TB_ALIEN;
                        break;
                    case 7:
                        newTrait |= TB_ZOMBIE;
                        break;
                    case 8:
                        newTrait |= TB_RELIC;
                        break;
                    default:
                        newTrait |= 1 << i;
                }
            }
        }

        return newTrait;
    }

    private static int reorderAbi(int ab, int ver) {
        int newAbi = 0, abiAdd = 0;
        switch (ver) {
            case 0: //Reformat moving attack, seal, and poison (not toxic) immunity
                for (int i = 0; i + abiAdd < ABI_TOT + 8; i++) {//+ 3
                    if (i == 7 || i == 12 || i == 18)
                        abiAdd++;
                    int i1 = i + abiAdd;
                    if (i1 == 12 || i1 == 18)
                        continue;
                    if (((ab >> i1) & 1) > 0)
                        newAbi |= 1 << i;
                }
                break;
            case 1: //Reformat Bounty and Base destroyer
                for (int i = 0; i + abiAdd < ABI_TOT + 6; i++) {//+ 1
                    if (i == 4)
                        abiAdd += 2;
                    int i1 = i + abiAdd;
                    if (((ab >> i1) & 1) > 0)
                        newAbi |= 1 << i;
                }
                break;
            case 2: //Reformat waveblock and (in the future) countersurge
                for (int i = 0; i + abiAdd < ABI_TOT + 5; i++) {//+ 0
                    if (i == 5 || i == 19)
                        abiAdd++;
                    int i1 = i + abiAdd;
                    if (i1 == 19)
                        continue;
                    if (((ab >> i1) & 1) > 0)
                        newAbi |= 1 << i;
                }
                break;
            case 3: //Reformat Strong Vs, Massive Dmg, Insane Dmg, Resistant, Insanely Resistant
                abiAdd = 3;
                for (int i = 0; i + abiAdd < ABI_TOT + 5; i++) {
                    if (i + abiAdd == 14)
                        abiAdd += 2;
                    int i1 = i + abiAdd;
                    if (((ab >> i1) & 1) > 0)
                        newAbi |= 1 << i;
                }
                break;
        }
        return newAbi;
    }

    public abstract PackData getPack();
}
