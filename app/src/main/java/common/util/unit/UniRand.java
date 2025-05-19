package common.util.unit;

import common.CommonStatic;
import common.battle.StageBasis;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.Source;
import common.pack.UserProfile;
import common.system.VImg;
import common.util.Data;
import common.util.unit.rand.UREnt;

import java.util.*;

@JsonClass.JCGeneric({Identifier.class, AbForm.AbFormJson.class})
@JsonClass
public class UniRand extends Data implements AbUnit, AbForm {

    @JsonClass.JCIdentifier
    @JsonField
    public final Identifier<AbUnit> id;

    @JsonField
    public String name = "";
    public VImg icon = null, deployIcon = null;
    @JsonField(defval = "50")
    public int price = 50;
    @JsonField(defval = "60")
    public int cooldown = 60;

    public static final byte T_NL = 0, T_LL = 1;

    @JsonField(generic = UREnt.class, defval = "isEmpty")
    public final ArrayList<UREnt> list = new ArrayList<>();

    public final Map<StageBasis, ULock> map = new HashMap<>();

    @JsonField
    public byte type = 0;

    @JsonClass.JCConstructor
    public UniRand() {
        id = null;
    }

    public UniRand(Identifier<AbUnit> ID) {
        id = ID;
    }

    @Override
    public Form[] getForms() {
        TreeSet<Form> ents = new TreeSet<>();
        fillPossible(ents, new TreeSet<>());
        return ents.toArray(new Form[0]);
    }

    public void fillPossible(TreeSet<Form> se, Set<UniRand> sr) {
        list.removeIf(er -> er.ent == null);

        sr.add(this);
        for (UREnt e : list) {
            AbForm ae = e.ent;
            if (ae instanceof Form)
                se.add((Form) e.ent);
            if (ae instanceof UniRand) {
                UniRand er = (UniRand) ae;
                if (!sr.contains(er))
                    er.fillPossible(se, sr);
            }
        }
    }

    @Override
    public String toString() {
        return (CommonStatic.getFaves().units.contains(this) ? "❤" : "") + id.id + " - " + name + " (" + id.pack + ")";
    }

    @Override
    public Identifier<AbUnit> getID() {
        return id;
    }

    @Override
    public UniRand unit() {
        return this;
    }

    public Level getPrefLvs() {
        return new Level();
    }

    public void updateCopy(StageBasis sb, Object o) {
        if (o != null)
            map.put(sb, (ULock) o);
    }

    @Override
    public VImg getIcon() {
        if (icon != null)
            return icon;
        return CommonStatic.getBCAssets().ico[0][0];
    }

    @Override
    public VImg getDeployIcon() {
        if (deployIcon != null)
            return deployIcon;
        return CommonStatic.getBCAssets().slot[0];
    }

    public boolean contains(Form f, Form origin) {
        for(UREnt id : list) {
            AbForm i = id.ent;

            if(i == null)
                continue;

            if(origin.equals(i))
                continue;

            if (i.getID().pack.equals(f.getID().pack) && i.getID() == f.getID())
                return true;
        }

        return false;
    }

    @JsonDecoder.OnInjected
    public void onInjected() {
        icon = UserProfile.getUserPack(id.pack).source.readImage(Source.BasePath.RAND + "/unitDisplayIcons", id.id);
        deployIcon = UserProfile.getUserPack(id.pack).source.readImage(Source.BasePath.RAND + "/unitDeployIcons", id.id);
    }
}