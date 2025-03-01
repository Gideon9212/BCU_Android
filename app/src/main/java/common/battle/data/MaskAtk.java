package common.battle.data;

import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.BattleStatic;
import common.util.Data;
import common.util.Data.Proc;
import common.util.stage.Music;
import common.util.unit.Trait;

public interface MaskAtk extends BattleStatic {
	SortedPackSet<Trait> blank = new SortedPackSet<>(0);

	default int getAltAbi() {
		return 0;
	}

	int getAtk();

	int getPre();

	default boolean canProc() {
		return true;
	}

	default int getDire() {
		return 1;
	}

	int getLongPoint();

	default int getMove() {
		return 0;
	}

	default SortedPackSet<Trait> getATKTraits() {
		return blank;
	}

	Proc getProc();

	int getShortPoint();

	default int getTarget() {
		return Data.TCH_N;
	}

	boolean isLD();

	boolean isOmni();

	boolean isRange();

	default int loopCount() {
		return -1;
	}

	default Identifier<Music> getAudio(boolean sec) {
		return null;
	}

	default String getName() {
		return "";
	}
}
