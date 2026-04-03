package common.util.stage;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.BattleStatic;
import common.util.Data;

import java.util.HashSet;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class StageLimit extends Data implements BattleStatic {

    public int maxMoney = 0, globalCooldown = 0, globalCost = -1, maxUnitSpawn = -1;
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
    public int unitSpeedOverride = -1, enemySpeedOverride = -1; // -1 for deactivated

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
            if (d >= 0)
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

    @Override
    public StageLimit clone() {
        StageLimit sl = new StageLimit();

        sl.maxMoney = maxMoney;
        sl.globalCooldown = globalCooldown;
        sl.cooldownMultiplier = cooldownMultiplier.clone();
        sl.costMultiplier = costMultiplier.clone();
        sl.globalCost = globalCost;
        sl.bannedCatCombo.addAll(bannedCatCombo);
        sl.maxUnitSpawn = maxUnitSpawn;

        sl.cooldownMultiplier = cooldownMultiplier.clone();
        sl.costMultiplier = costMultiplier.clone();
        sl.rarityDeployLimit = rarityDeployLimit.clone();

        sl.deployDuplicationTimes = deployDuplicationTimes.clone();
        sl.deployDuplicationDelay = deployDuplicationDelay.clone();

        sl.bannedCatCombo.addAll(bannedCatCombo);
        sl.coolStart = coolStart;
        sl.cannonMultiplier = cannonMultiplier;

        sl.unitSpeedOverride = unitSpeedOverride;
        sl.enemySpeedOverride = enemySpeedOverride;
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
        combined.unitSpeedOverride = unitSpeedOverride == 0 ? second.unitSpeedOverride : second.unitSpeedOverride == 0 ? unitSpeedOverride : Math.min(unitSpeedOverride, second.unitSpeedOverride);
        combined.enemySpeedOverride = enemySpeedOverride == 0 ? second.enemySpeedOverride : second.enemySpeedOverride == 0 ? enemySpeedOverride : Math.max(enemySpeedOverride, second.enemySpeedOverride);
        for (int i = 0; i < deployDuplicationTimes.length; i++)
            combined.deployDuplicationTimes[i] = Math.max(deployDuplicationTimes[i], second.deployDuplicationTimes[i]);
        for (int i = 0; i < deployDuplicationDelay.length; i++)
            combined.deployDuplicationDelay[i] = Math.max(deployDuplicationDelay[i], second.deployDuplicationDelay[i]);
        combined.cannonMultiplier = cannonMultiplier == 0 ? second.cannonMultiplier : second.cannonMultiplier == 0 ? cannonMultiplier : Math.min(cannonMultiplier, second.cannonMultiplier);
        for (int i = 0; i < rarityDeployLimit.length; i++)
            combined.rarityDeployLimit[i] = Math.max(rarityDeployLimit[i], second.rarityDeployLimit[i]);
        return combined;
    }

    public boolean isBlank() {
        return !coolStart && maxMoney == 0 && globalCooldown == 0 && globalCost == -1 && maxUnitSpawn == -1 && unitSpeedOverride == -1
                && enemySpeedOverride == -1 && cannonMultiplier == 100 && defCD() && defMoney() && defDeploy() && bannedCatCombo.isEmpty() && defDupe();
    }
}
