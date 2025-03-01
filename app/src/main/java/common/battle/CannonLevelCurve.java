package common.battle;

import common.util.Data;

import java.util.Map;

public class CannonLevelCurve extends Data {
    public enum PART {
        CANNON,
        BASE,
        DECORATION
    }

    private final PART part;

    private final Map<Integer, int[][]> curveMap;

    public CannonLevelCurve(Map<Integer, int[][]> curveMap, PART part) {
        this.curveMap = curveMap;
        this.part = part;
    }

    public int getMax() {
        int[][] map = curveMap.values().iterator().next();
        return map[map.length - 1][0];
    }

    public float applyFormula(int type, int level) {
        float v = applyFormulaRaw(type, level);
        switch (part) {
            case CANNON:
                switch (type) {
                    case BASE_RANGE:
                        return v / 4f;
                    case BASE_HEALTH_PERCENTAGE:
                        return v / 10f;
                    case BASE_HOLY_ATK_SURFACE:
                    case BASE_HOLY_ATK_UNDERGROUND:
                        return v / 1000f;
                    default:
                        return v;
                }
            case BASE:
            case DECORATION:
                return 1f - v / 10000f;
            default:
                return v;
        }
    }

    public int applyFormulaRaw(int type, int level) {
        if (!curveMap.containsKey(type)) {
            System.out.println("Warning : Invalid type " + type);
            return 0;
        }

        int[][] curve = curveMap.get(type);

        // clip level between 0 and max allowed
        level = Math.max(0, Math.min(level, curve[curve.length - 1][0]));

        int i = 0;
        int prevThreshold = 1;
        while(level > curve[i][0])
            prevThreshold = curve[i++][0];
        return curve[i][1] + (curve[i][2] - curve[i][1]) * (level - prevThreshold) / (curve[i][0] - prevThreshold);
    }
}
