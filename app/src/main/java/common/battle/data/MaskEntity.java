package common.battle.data;

import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.BattleStatic;
import common.util.Data;
import common.util.Data.Proc;
import common.util.pack.Soul;
import common.util.unit.Character;
import common.util.unit.Trait;

public interface MaskEntity extends BattleStatic {

	int allAtk(int atk);

	int getAbi();

	Proc getAllProc();

	/**
	 * get the attack animation length
	 */
	default int getAnimLen(int atk) {
		return getPack().anim.getAtkLen(atk);
	}

	int getAtkCount(int atk);

	int getAtkLoop();

	default int firstAtk() {
		return 0;
	}

	default int realAtkCount() {
		return 1;
	}

	MaskAtk getAtkModel(int atk, int ind);

	MaskAtk[] getAtks(int atk);

	default int getAtkTypeCount() {
		return 1;
	}

	default MaskAtk[][] getAllAtks() {
		return new MaskAtk[][]{getAtks(0)};
	}

	default int getShare(int atk) {
		return 1;
	}

	default AtkDataModel[][] getSpAtks(boolean addCounter) {
		return new AtkDataModel[0][];
	}

	default AtkDataModel[] getSpAtks(boolean counter, int ind) {
		return new AtkDataModel[0];
	}

	Identifier<Soul> getDeathAnim();

	SortedPackSet<Trait> getTraits();

	int getHb();

	int getHp();

	/**
	 * get the attack period
	 */
	int getItv(int atk);

	/**
	 * get the Enemy/Form this data represents
	 */
	Character getPack();

	int getPost(boolean sp, int atk);

	Proc getProc();

	int getRange();

	MaskAtk getRepAtk();

	default AtkDataModel[] getRevenge() {
		return new AtkDataModel[0];
	}

	default AtkDataModel[] getResurrection() {
		return new AtkDataModel[0];
	}

	default AtkDataModel getCounter() {
		return null;
	}

	default AtkDataModel[] getGouge() {
		return new AtkDataModel[0];
	}

	default AtkDataModel[] getResurface() {
		return new AtkDataModel[0];
	}

	default AtkDataModel[] getRevive() {
		return new AtkDataModel[0];
	}

	default AtkDataModel[] getEntry() {
		return new AtkDataModel[0];
	}

	int getSpeed();

	int getWill();

	/**
	 * get waiting time
	 */
	int getTBA();

	default int getTouch() {
		return Data.TCH_N;
	}

	int getWidth();

	boolean isLD();

	boolean isOmni();

	boolean isRange(int atk);

	int touchBase();

	boolean isCommon();
}
