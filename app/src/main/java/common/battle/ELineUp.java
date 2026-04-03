package common.battle;

import common.CommonStatic;
import common.battle.entity.DoorCont;
import common.battle.entity.ESpirit;
import common.battle.entity.EUnit;
import common.pack.SortedPackSet;
import common.util.BattleObj;
import common.util.stage.Limit;
import common.util.unit.AbForm;
import common.util.unit.Combo;
import common.util.unit.EForm;
import common.util.unit.Form;

public class ELineUp extends BattleObj {

	public final int[][] price = new int[2][5], maxC = new int[2][5];
	public final double[][] cool = new double[2][5], scd = new double[2][5];

	private final Proc.SPIRIT[][] spData = new Proc.SPIRIT[2][5];
	public final int[][] scount = new int[2][5], sGlow = new int[2][5];
	public final boolean[][] smnd = new boolean[2][5];

	private final LineUp.ComboBuff[] inc;

	protected ELineUp(LineUp lu, StageBasis sb, byte saveMode) {
		inc = new LineUp.ComboBuff[lu.incs.size()];
		int q = 0;
		for (LineUp.ComboBuff buff : lu.incs) {
			inc[q] = new LineUp.ComboBuff(buff.cg);
			for (byte i = 0; i < C_TOT; i++)
				if (!sb.isBanned(i))
					inc[q].inc[i] = buff.inc[i];
			q++;
		}
		Limit lim = sb.est.lim;
		SortedPackSet<Combo> coms = new SortedPackSet<>(lu.coms);
        for (byte i = 0; i < 2; i++)
			for (byte j = 0; j < 5; j++) {
				if (lu.fs[i][j] == null)
					price[i][j] = -1;
				else if (saveMode == 2 && !sb.st.getMC().getSave(true).getUnlockedsBeforeStage(sb.st, true).containsKey(lu.fs[i][j]) ||
					saveMode == 1 && sb.st.getMC().getSave(true).locked(lu.fs[i][j]))
					price[i][j] = -2;
				else if (lim != null && lu.efs[i][j] instanceof EForm && lim.unusable(((EForm)lu.efs[i][j]).du, sb.st.getCont().price, i))
					price[i][j] = -1;
				if (price[i][j] != 0) {
					if (price[i][j] == -2)
						for (int k = 0; k < coms.size(); k++) {
							LineUp.ComboBuff bf = inc[0];
							Combo c = coms.get(k); //1st check to not have negative due to banned combo
							if (coms.get(k).group != null)
								for (int l = 1; l < inc.length; l++)
									if (inc[l].cg == c.group) {
										bf = inc[l];
										break;
									}
							if (bf.inc[i] > 0 && coms.get(k).containsForm((Form) lu.fs[i][j])) {
								coms.remove(k--);
								bf.inc[c.type] -= CommonStatic.getBCAssets().values[c.type][c.lv];
							}
						}
					continue;
				}
				price[i][j] = sb.globalPrice() > 0 ? sb.globalPrice() : (int) (lu.efs[i][j].getPrice(sb.st.getCont().price) * (100 - getInc(C_DISCOUNT, lu.fs[i][j])));
				maxC[i][j] = sb.globalCdLimit() > 0 ? sb.b.t().getFinResGlobal(sb.globalCdLimit(), getInc(C_RESP, lu.fs[i][j])) : sb.b.t().getFinRes(lu.efs[i][j].getRespawn(), getInc(C_RESP, lu.fs[i][j]));
				if (lim != null && lim.stageLimit != null && lu.fs[i][j] instanceof Form) {
					int r = ((Form)lu.fs[i][j]).unit.rarity;
					price[i][j] = price[i][j] * lim.stageLimit.costMultiplier[r] / 100;
					maxC[i][j] = maxC[i][j] * lim.stageLimit.cooldownMultiplier[r] / 100;
				}
				spData[i][j] = lu.efs[i][j] instanceof EForm && ((EForm) lu.efs[i][j]).du.getProc().SPIRIT.id != null ? ((EForm)lu.efs[i][j]).du.getProc().SPIRIT : null;
				scount[i][j] = spData[i][j] == null ? -1 : 0;
			}
	}

	/**
	 * reset cooldown of a unit, as well as the values of a spirit
	 */
	protected void resetCD(int i, int j) {
		cool[i][j] = maxC[i][j];
		if (spData[i][j] != null) {
			scd[i][j] = spData[i][j].cd0;
			scount[i][j] = spData[i][j].amount;
		}
	}

	/**
	 * reset recharge time of a spirit and spawn it
	 */
	protected final void deploySpirit(int i, int j, StageBasis sb, EUnit spi) {
		boolean firstDeploy = true;
		for (EUnit u : sb.getAllOf(i, j)) {
			EUnit rit = firstDeploy ? spi : ((EForm)sb.b.lu.efs[i][j]).invokeSpirit(sb, spi.index);
			rit.added(-1, Math.min(Math.max(sb.ebase.pos + rit.data.getRange(), u.lastPosition + SPIRIT_SUMMON_RANGE), sb.ubase.pos));
			rit.group = -1;//for the getAllOf function
			if (!(rit instanceof ESpirit))
				rit.setSummon(spData[i][j].animType, null);
			if (spData[i][j].animType == Proc.SUMMON_ANIM.EVERYWHERE_DOOR)
				sb.doors.add(new DoorCont(sb, rit));
			else
				sb.le.add(rit);
			firstDeploy = false;
		}

		CommonStatic.setSE(SE_SPIRIT_SUMMON);
		sb.money -= spiritCost(i, j, sb.st.getCont().price);
		scount[i][j]--;
		scd[i][j] = spData[i][j].cd1;
		cool[i][j] = Math.min(Math.max(0, cool[i][j] + spData[i][j].summonerCd), maxC[i][j]);
	}

	public final boolean validSpirit(int i, int j) {
		return spData[i][j] != null && smnd[i][j];
	}

	public final boolean readySpirit(int i, int j) {
		return validSpirit(i,j) && scd[i][j] == 0 && scount[i][j] > 0;
	}

	public final int spiritCost(int i, int j, int sta) {
		return (int)(spData[i][j].moneyCost * (1 + sta * 0.5f) * 100);
	}

	/**
	 * count down the cooldown
	 */
	protected void update(float time, float flow) {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 5; j++) {
				if (cool[i][j] > 0 && (cool[i][j] -= flow) <= 0)
					CommonStatic.setSE(SE_SPEND_REF);

				if (validSpirit(i,j) && scount[i][j] > 0 && scd[i][j] > 0 && (scd[i][j] -= flow) <= 0)
					sGlow[i][j] = (int)time;
			}
		}
	}

	/**
	 * Takes combo bans into account
	 * @param id combo ID
	 * @return buff of the specificed combo (0 if banned)
	 */
	public int getInc(int id) {
		return inc[0].inc[id];
	}

	/**
	 * This is used for units specifically, takes restrictions and bans into account
	 */
	public int getInc(int id, AbForm f) {
		int def = getInc(id);
		if (!(f instanceof Form))
			return def;
		for (int i = 1; i < inc.length; i++)
			if (inc[i].cg.fset.contains(f))
				def += inc[i].inc[id];
		return def;
	}

	public int getInc(int id, EUnit u) {
		return getInc(id, (Form)u.data.getPack());
	}
}
