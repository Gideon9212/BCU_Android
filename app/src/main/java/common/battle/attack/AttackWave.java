package common.battle.attack;

import common.battle.entity.AbEntity;
import common.battle.entity.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttackWave extends AttackAb {

	protected final Set<Entity> incl;
	public int raw;

	public AttackWave(Entity e, AttackSimple a, float p0, float wid, int wt) {
		super(e, a, p0 - wid / 2, p0 + wid / 2, false);
		incl = new HashSet<>();
		waveType = wt;
		raw = model instanceof AtkModelEntity ? ((AtkModelEntity)model).getDefAtk(matk) : atk;
		if(wt != WT_MOVE && dire == 1 && model.b.canon.deco == DECO_BASE_WALL)
			raw *= model.b.b.t().getDecorationMagnification(model.b.canon.deco);
	}

	public AttackWave(Entity e, AttackWave a, float p0, float wid) {
		super(e, a, p0 - wid / 2, p0 + wid / 2, false);
		incl = a.incl;
		waveType = a.waveType;
		raw = a.raw;
	}

	public AttackWave(Entity e, AttackWave a, float pos, float start, float end) {
		super(e, a, pos - start, pos + end, false);
		incl = a.incl;
		waveType = a.waveType;
		raw = a.raw;
	}

	@Override
	public void capture() {
		List<AbEntity> le = model.b.inRange(touch, attacker != null && attacker.status.rage > 0 ? 2 : dire, sta, end, excludeRightEdge);
		if (waveType != WT_MOVE || !proc.MOVEWAVE.hit_base)
			le.remove(dire == 1 ? model.b.ubase : model.b.ebase);
		if (attacker != null && (attacker.status.rage > 0 || attacker.status.hypno > 0))
			le.remove(attacker);
		if (incl != null)
			le.removeAll(incl);
		capt.clear();
		if ((abi & AB_ONLY) == 0)
			capt.addAll(le);
		else
			for (AbEntity e : le)
				if (e.ctargetable(trait, attacker))
					capt.add(e);
	}

	@Override
	public void excuse() {
		if(attacker != null)
			atk = ((AtkModelEntity)model).getEffMult(raw);
		process();
		for (AbEntity e : capt) {
			if (e instanceof Entity) {
				e.damaged(this);
				incl.add((Entity) e);
			}
		}
		r.clear();
	}
}
