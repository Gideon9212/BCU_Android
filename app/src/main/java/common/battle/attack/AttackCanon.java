package common.battle.attack;

import common.battle.entity.Cannon;
import common.pack.SortedPackSet;
import common.util.unit.Trait;

public class AttackCanon extends AttackSimple {

	public AttackCanon(Cannon c, int ATK, SortedPackSet<Trait> tr, int eab, Proc pro, float p0, float p1, int duration) {
		super(null, c, ATK, tr, eab, pro, p0, p1, true, null, 9, false, duration);
		canon = c.id > 2 ? 1 << (c.id - 1) : 1 << c.id;
		excludeRightEdge = c.id == 6;
		waveType |= WT_CANN;
		if (canon == 16)
			touch |= TCH_UG | TCH_CORPSE;
		if (canon == 32)
			touch |= TCH_CORPSE;
	}

}
