package common.util.unit;

import common.CommonStatic;
import common.system.VImg;

public interface AbCharacter {//For UI purposes, encompasses all Enemy, Unit, Form, RandomUnit, and RandomEnemy

    public VImg getIcon();

    default VImg getPreview() {
        return null;
    }

    default VImg getDeployIcon() {
        return CommonStatic.getBCAssets().slot[0];
    }
}
