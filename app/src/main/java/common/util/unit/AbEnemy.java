package common.util.unit;

import common.battle.StageBasis;
import common.battle.entity.EEnemy;
import common.pack.Identifier;
import common.pack.IndexContainer.IndexCont;
import common.pack.IndexContainer.Indexable;
import common.pack.PackData;

import java.util.Set;

@IndexCont(PackData.class)
public interface AbEnemy extends Comparable<AbEnemy>, Indexable<PackData, AbEnemy>, AbCharacter {

	@Override
	default int compareTo(AbEnemy e) {
		return getID().compareTo(e.getID());
	}

	EEnemy getEntity(StageBasis sb, Object obj, float mul, float mul1, int d0, int d1, int m);

	@Override
	Identifier<AbEnemy> getID();

	Set<Enemy> getPossible();
}
