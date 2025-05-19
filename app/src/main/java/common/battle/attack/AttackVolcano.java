package common.battle.attack;

import common.battle.entity.AbEntity;
import common.battle.entity.Entity;
import java.util.ArrayList;
import java.util.List;

public class AttackVolcano extends AttackAb {
	public ContVolcano handler;
	protected boolean attacked = false;
	public int raw;
	private float volcTime = VOLC_ITV;

	protected final List<Entity> vcapt = new ArrayList<>();
	public final Proc.ProcID pid;
	public boolean active = true;

	public AttackVolcano(Entity e, AttackSimple a, float sta, float end, int vt, Proc.ProcID id) {
		super(e, a, sta, end, false);
		this.waveType = vt;
		pid = id;
		raw = model instanceof AtkModelEntity ? ((AtkModelEntity)model).getDefAtk(matk) : atk;
		if(dire == 1 && model.b.canon.deco == DECO_BASE_WATER)
			raw = (int)(raw * model.b.b.t().getDecorationMagnification(model.b.canon.deco));
	}

	public void capture() {
		List<AbEntity> le = model.b.inRange(touch, attacker.status.rage > 0 ? 2 : dire, sta, end, excludeRightEdge);
		if (attacker.status.rage > 0 || attacker.status.hypno > 0)
			le.remove(attacker);
		capt.clear();

		for (AbEntity e : le)
			if (e instanceof Entity && !vcapt.contains((Entity) e) && ((abi & AB_ONLY) == 0 || e.isBase() || e.ctargetable(trait, attacker)))
				capt.add(e);
	}

	public void excuse() {
		if (volcTime <= 0) {
			volcTime = VOLC_ITV;
			vcapt.clear();
		} else
			volcTime -= attacker == null ? model.b.timeFlow : attacker.getTimeFreeze();

		if(attacker != null)
			atk = ((AtkModelEntity)model).getEffMult(raw);
		process();

		for (AbEntity e : capt) {
			e.damaged(this);
			vcapt.add((Entity) e);
		}
		attacked = !capt.isEmpty();
		r.clear();
	}
}