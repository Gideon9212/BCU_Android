package common.util.stage.info;

import common.CommonStatic;
import common.pack.Identifier;
import common.util.stage.MapColc;
import common.util.stage.Music;
import common.util.stage.Stage;
import common.util.stage.StageMap;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DefStageInfo implements StageInfo {
    private static final DecimalFormat df;

    static {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        df = (DecimalFormat) nf;

        df.applyPattern("#.##");
    }

    public final Stage st;
    public final StageMap.StageMapInfo map;
    public final int energy, xp, once, rand;
    public final int[][] drop;
    public final int[][] time;
    public Stage[] exStages;
    public float[] exChances;
    public int diff = -1, exChance = -1, exMapID = -1, exStageIDMin = -1, exStageIDMax = -1;
    public boolean exConnection = false;
    public int maxMaterial = -1;

    public DefStageInfo(StageMap.StageMapInfo info, Stage s, int[] data) {
        map = info;
        st = s;

        energy = data[0];
        xp = data[1];
        s.mus0 = Identifier.parseInt(data[2], Music.class);
        if (data[3] != 0 && data[3] != 100) {
            s.mush = data[3];
            s.mus1 = Identifier.parseInt(data[4], Music.class);
        }

        once = data[data.length - 1];
        boolean isTime = data.length > 15;
        if (isTime)
            for (int i = 8; i < 15; i++)
                if (data[i] != -2) {
                    isTime = false;
                    break;
                }
        if (isTime) {
            time = new int[(data.length - 17) / 3][3];
            for (int i = 0; i < time.length; i++)
                for (int j = 0; j < 3; j++)
                    time[i][j] = data[16 + i * 3 + j];
        } else
            time = new int[0][3];
        boolean isMulti = !isTime && data.length > 9;
        if (data.length == 6) {
            drop = new int[0][];
            rand = 0;
        } else if (!isMulti) {
            drop = new int[1][];
            rand = 0;
        } else {
            drop = new int[(data.length - 7) / 3][3];
            rand = data[8];
            for (int i = 1; i < drop.length; i++)
                for (int j = 0; j < 3; j++)
                    drop[i][j] = data[6 + i * 3 + j];
        }
        if (drop.length > 0)
            drop[0] = new int[] { data[5], data[6], data[7] };
    }

    public void setData(String[] strs) {
        int chance = CommonStatic.parseIntN(strs[2]);

        exConnection = chance != 0;
        exChance = chance;

        exMapID = CommonStatic.parseIntN(strs[3]);

        exStageIDMin = CommonStatic.parseIntN(strs[4]);
        exStageIDMax = CommonStatic.parseIntN(strs[5]);
    }

    @Override
    public boolean exConnection() {
        return exConnection;
    }

    @Override
    public Stage[] getExStages() {
        if (!exConnection)
            return exStages;
        StageMap sm = MapColc.DefMapColc.getMap(exMapID + 4000);
        ArrayList<Stage> sts = new ArrayList<>(exStageIDMax - exStageIDMin + 1);

        if (sm != null)
            for (int i = exStageIDMin; i <= exStageIDMax; i++) {
                Stage st = sm.list.get(i);
                if (st != null)
                    sts.add(st);
            }
        return sts.toArray(new Stage[0]);
    }

    @Override
    public float[] getExChances() {
        if (exConnection)
            return new float[]{-1, exChance};
        return exChances;
    }

    public List<String> analyzeRewardChance() {
        ArrayList<String> res = new ArrayList<>();

        int sum = 0;

        for(int[] d : drop) {
            sum += d[0];
        }

        if(sum == 0)
            return null;

        if(sum == 1000) {
            for(int[] d : drop) {
                res.add(df.format(d[0]/10.0));
            }
        } else if((sum == drop.length && sum != 1) || rand == -3) {
            return res;
        } else if(sum == 100) {
            for(int[] d : drop) {
                res.add(String.valueOf(d[0]));
            }
        } else if(sum > 100 && (rand == 0 || rand == 1)) {
            double rest = 100.0;

            if(drop[0][0] == 100) {
                res.add(String.valueOf(100));

                for(int i = 1; i < drop.length; i++) {
                    double filter = rest * drop[i][0] / 100.0;

                    rest -= filter;

                    res.add(df.format(filter));
                }
            } else {
                for(int[] d : drop) {
                    double filter = rest * d[0] / 100.0;

                    rest -= filter;

                    res.add(df.format(filter));
                }
            }
        } else if(rand == -4) {
            int total = 0;

            for(int[] d : drop) {
                total += d[0];
            }

            if(total == 0)
                return null;

            for(int[] d : drop) {
                res.add(df.format(d[0] * 100.0 / total));
            }
        } else {
            for(int[] d : drop) {
                res.add(String.valueOf(d[0]));
            }
        }
        return res;
    }
}