package common.util.unit.rand;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.system.Copable;
import common.util.BattleStatic;
import common.util.unit.AbForm;
import common.util.unit.Level;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class UREnt implements BattleStatic, Copable<UREnt> {

    @JsonField(generic = AbForm.class, alias = AbForm.AbFormJson.class)
    public AbForm ent;
    public Level lv;
    public int share = 1;

    public UREnt() {
        ent = null;
        lv = null;
    }

    public UREnt(AbForm f) {
        ent = f;
        lv = f.unit().getPrefLvs();
    }

    @Override
    public UREnt copy() {
        UREnt ans = new UREnt(ent);
        ans.lv = lv.clone();
        ans.share = share;
        return ans;
    }
}
