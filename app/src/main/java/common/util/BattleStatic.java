package common.util;

public interface BattleStatic {

	/**
	 * designed to prevent a class from extending BattleObj and implementing
	 * BattleStatic
	 */
	default void conflict() {
	}

}
