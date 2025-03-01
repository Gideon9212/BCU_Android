package common.battle.entity;

import common.util.BattleObj;

public class EntCont extends BattleObj {

	public Entity ent;

	public double t;

	public EntCont(Entity e, int time) {
		ent = e;
		t = time;
	}

	public void update(float flow) {
		if (t > 0)
			t -= flow;
	}

}
