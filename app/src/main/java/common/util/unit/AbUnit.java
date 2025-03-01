package common.util.unit;

import common.pack.IndexContainer;
import common.pack.PackData;

@IndexContainer.IndexCont(PackData.class)
public interface AbUnit extends Comparable<AbUnit>, IndexContainer.Indexable<PackData, AbUnit> {

    @Override
    default int compareTo(AbUnit u) {
        return getID().compareTo(u.getID());
    }

    Form[] getForms();

    default int getRarity() {
        return -1;
    }

    default int getMaxLv() {
        return 200;
    }

    default int getMaxPLv() {
        return 0;
    }

    default int getCap() {
        return getMaxLv() + getMaxPLv();
    }

    Level getPrefLvs();
}
