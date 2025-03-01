package common.util.stage.info;

import common.util.stage.Stage;

public interface StageInfo {

    boolean exConnection();

    Stage[] getExStages();

    float[] getExChances();
}