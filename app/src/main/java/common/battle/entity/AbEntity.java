package common.battle.entity;

import common.battle.attack.AttackAb;
import common.battle.data.MaskAtk;
import common.pack.SortedPackSet;
import common.util.BattleObj;
import common.util.unit.Trait;

public abstract class AbEntity extends BattleObj {

	/**
	 * health = Unit's current health.
	 * maxH = Unit's maximum HP. Used to limit healing and any effects that require % of Entity's HP.
	 */
	public long health, maxH;
	/**
	 * Direction/Faction of entity. -1 is Cat unit, 1 is Enemy Unit
	 */
	public int dire;
	/**
	 * Current Position of this Entity
	 */
	public float pos;

	/**
	 * Base shake
	 */
	public byte hit;

	protected AbEntity(int h) {
		if (h <= 0)
			h = 1;
		health = maxH = h;
	}

	/**
	 * Sets the dire and position of the new entity
	 * @param d dire
	 * @param p position
	 */
	public void added(int d, float p) {
		pos = p;
		dire = d;
	}

	public Proc getProc() {
		return empty;
	}

	public abstract float calcDamageMult(int dmg, Entity e, MaskAtk matk);

	public abstract void damaged(AttackAb atk);

	public abstract int getAbi();

	public abstract boolean isBase();

	public abstract void postUpdate();

	public abstract boolean targetable(Entity ent);

	public boolean ctargetable(SortedPackSet<Trait> t, Entity attacker) {
		return true;
	}

	public abstract int touchable();

	public abstract void preUpdate();

	public abstract void update();

	public abstract void updateAnimation();
}
