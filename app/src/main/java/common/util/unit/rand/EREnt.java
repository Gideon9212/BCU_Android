package common.util.unit.rand;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.system.Copable;
import common.util.BattleStatic;
import common.util.unit.AbEnemy;
import org.jetbrains.annotations.Nullable;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class EREnt implements BattleStatic, Copable<EREnt> {

    @Nullable
    @JsonField(backCompat = JsonField.CompatType.FORK)
    public Identifier<AbEnemy> ent;
    public int multi = 100;
    public int mula = 100;
    public int share = 1;

    @Override
    public EREnt copy() {
        EREnt ans = new EREnt();
        ans.ent = ent;
        ans.multi = multi;
        ans.mula = mula;
        ans.share = share;
        return ans;
    }

    @JsonField(io = JsonField.IOType.W, tag = "ent", backCompat = JsonField.CompatType.UPST)
    public JsonElement legacyEnt() {
        if (ent == null)
            return null;
        JsonObject jo = new JsonObject();
        jo.addProperty("cls", ent.cls.getName());
        jo.addProperty("pack", ent.pack);
        jo.addProperty("id", ent.id);
        jo.addProperty("_class", Identifier.class.getName());
        return jo;
    }
}
