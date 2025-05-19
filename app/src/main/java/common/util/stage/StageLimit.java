package common.util.stage;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.BattleStatic;
import common.util.Data;

import java.util.HashSet;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class StageLimit extends Data implements BattleStatic {

    public int maxMoney = 0, globalCooldown = 0, globalCost = 0, maxUnitSpawn = 0;
    @JsonField(defval = "this.defCD")
    public int[] cooldownMultiplier = { 100, 100, 100, 100, 100, 100 };
    @JsonField(defval = "this.defMoney")
    public int[] costMultiplier = { 100, 100, 100, 100, 100, 100 };
    @JsonField(defval = "this.defDeploy")
    public int[] rarityDeployLimit = { -1, -1, -1, -1, -1, -1 }; // -1 for none

    @JsonField(defval = "this.defDupe")
    public int[] deployDuplicationTimes = { 0, 0, 0, 0, 0, 0 }; // 0 for deactivated
    @JsonField(defval = "this.defDupe")
    public int[] deployDuplicationDelay = { 0, 0, 0, 0, 0, 0 }; // unit is frame

    public boolean coolStart = false;
    @JsonField(generic = Integer.class, defval = "isEmpty")
    public HashSet<Integer> bannedCatCombo = new HashSet<>();

    @JsonField(defval = "100")
    public int cannonMultiplier = 100; // percentage
    @JsonField(defval = "-1")
    public int unitSpeedLimit = -1, enemySpeedLimit = -1; // -1 for deactivated

    public boolean defCD() {
        for (int cd : cooldownMultiplier)
            if (cd != 100)
                return false;
        return true;
    }
    public boolean defMoney() {
        for (int cd : cooldownMultiplier)
            if (cd != 100)
                return false;
        return true;
    }
    public boolean defDeploy() {
        for (int d : rarityDeployLimit)
            if (d > 0)
                return false;
        return true;
    }
    public boolean defDupe() {
        for (int d : deployDuplicationTimes)
            if (d > 0)
                return false;
        return true;
    }

    public StageLimit() {
    }

    public StageLimit clone() {
        StageLimit sl = new StageLimit();

        sl.maxMoney = maxMoney;
        sl.maxUnitSpawn = maxUnitSpawn;
        sl.globalCooldown = globalCooldown;
        sl.cooldownMultiplier = cooldownMultiplier.clone();
        sl.costMultiplier = costMultiplier.clone();
        sl.globalCost = globalCost;
        sl.bannedCatCombo.addAll(bannedCatCombo);

        sl.cooldownMultiplier = cooldownMultiplier.clone();
        sl.costMultiplier = costMultiplier.clone();
        sl.rarityDeployLimit = rarityDeployLimit.clone();

        return sl;
    }

    public StageLimit combine(StageLimit second) {
        StageLimit combined = new StageLimit();
        combined.maxMoney = maxMoney == 0 ? second.maxMoney : second.maxMoney == 0 ? maxMoney : Math.min(maxMoney, second.maxMoney);
        combined.globalCooldown = globalCooldown == 0 ? second.globalCooldown : second.globalCooldown == 0 ? globalCooldown : Math.max(globalCooldown, second.globalCooldown);
        combined.globalCost = globalCost == -1 ? second.globalCost : second.globalCost == -1 ? globalCost : Math.max(globalCost, second.globalCost);
        combined.coolStart = coolStart || second.coolStart;
        combined.maxUnitSpawn = maxUnitSpawn == 0 ? second.maxUnitSpawn : second.maxUnitSpawn == 0 ? maxUnitSpawn : Math.min(maxUnitSpawn, second.maxUnitSpawn);
        for (int i = 0; i < costMultiplier.length; i++)
            combined.costMultiplier[i] = Math.max(costMultiplier[i], second.costMultiplier[i]);
        for (int i = 0; i < cooldownMultiplier.length; i++)
            combined.cooldownMultiplier[i] = Math.max(cooldownMultiplier[i], second.cooldownMultiplier[i]);
        combined.bannedCatCombo.addAll(bannedCatCombo);
        combined.bannedCatCombo.addAll(second.bannedCatCombo);
        return combined;
    }

    public boolean isBlank() {
        return !coolStart && maxMoney == 0 && globalCooldown == 0 && globalCost == 0 && maxUnitSpawn == 0 && unitSpeedLimit == -1
                && enemySpeedLimit == -1 && cannonMultiplier == 100 && defCD() && defMoney() && defDeploy() && bannedCatCombo.isEmpty() && defDupe();
    }
}
