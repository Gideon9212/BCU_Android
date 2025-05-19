package common.battle.entity;

import common.util.BattleObj;

public class EntCont extends BattleObj {

	public final Entity ent;
	public final DoorCont door;

	public double t;

	public EntCont(Entity e, int time) {
		ent = e;
		door = null;
		t = time;
	}

	public EntCont(DoorCont d, int time) {
		door = d;
		ent = d.ent;
		t = time;
	}

	public void update(float flow) {
		if (t > 0)
			t -= flow;
	}
}
