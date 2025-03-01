package common.battle.attack;

import common.battle.ELineUp;
import common.battle.data.MaskAtk;
import common.battle.data.PCoin;
import common.battle.entity.Entity;
import common.util.unit.Level;

public class AtkModelUnit extends AtkModelEntity {

	private final ELineUp elu;

	protected AtkModelUnit(Entity ent, float d0, float d1, PCoin pcoin, Level lv) {
		super(ent, d0, d1, pcoin, lv);
		elu = ent.basis.elu;
	}

	@Override
	public int getDefAtk(MaskAtk matk) {
		return (int)(Math.round(matk.getAtk() * d1) * d0);
	}
	@Override
	public int getEffMult(int dmg) {
		if (e.status.getWeaken() != 1)
			dmg = (int)(dmg * e.status.getWeaken());
		if (e.status.strengthen != 0)
			dmg += dmg * (e.status.strengthen + elu.getInc(C_STRONG)) / 100;
		dmg *= e.auras.getAtkAura();
		return dmg;
	}

	@Override
	protected int getAttack(MaskAtk matk, Proc proc) {
		int atk = getEffAtk(matk);

		if (matk.getProc() != empty) {
			setProc(matk, proc, 1);
			proc.KB.dis = proc.KB.dis * (100 + elu.getInc(C_KB)) / 100;
			proc.STOP.time = (proc.STOP.time * (100 + elu.getInc(C_STOP))) / 100;
			proc.SLOW.time = (proc.SLOW.time * (100 + elu.getInc(C_SLOW))) / 100;
			proc.WEAK.time = (proc.WEAK.time * (100 + elu.getInc(C_WEAK))) / 100;
			proc.getArr(P_BSTHUNT).set(e.getProc().getArr(P_BSTHUNT));
		} else {
			if (matk.getProc().MOVEWAVE.perform(b.r)) //Movewave procs regardless of seal state
				proc.MOVEWAVE.set(matk.getProc().MOVEWAVE);

			if (!matk.canProc()) {
				proc.getArr(P_BSTHUNT).set(e.getProc().getArr(P_BSTHUNT));
				for (int j : BCShareable) proc.getArr(j).set(e.getProc().getArr(j));
			}
		}
		extraAtk(matk);
		return atk;
	}

	@Override
	public Proc getProc(MaskAtk matk) {
		Proc p = super.getProc(matk);
		if (p.CRIT.prob == 0 || elu.getInc(C_CRIT) == 0)
			return p;
		Proc pp = p.clone();
		pp.CRIT.prob += elu.getInc(C_CRIT);
		return pp;
	}
}
