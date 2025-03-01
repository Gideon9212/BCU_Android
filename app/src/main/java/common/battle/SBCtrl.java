package common.battle;

import common.CommonStatic;
import common.CommonStatic.FakeKey;
import common.util.BattleObj;
import common.util.stage.EStage;
import common.util.stage.Replay;
import common.util.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SBCtrl extends BattleField {

	private final FakeKey keys;

	public final List<Integer> action = new ArrayList<>();

	public final Replay re;

	public SBCtrl(FakeKey kh, Stage stage, int star, BasisLU bas, int conf, long seed, byte saveMode) {
		super(new EStage(stage, star), bas, conf, seed, CommonStatic.getConfig().buttonDelay, saveMode);
		re = new Replay(bas, stage.id, star, conf, seed, CommonStatic.getConfig().buttonDelay, saveMode);
		keys = kh;
	}

	protected SBCtrl(FakeKey kh, StageBasis sb, Replay r) {
		super(sb);
		keys = kh;
		re = r.clone();
	}

	public Replay getData() {
		re.action = sb.rx.write();
		re.sniperCoords = new HashMap<>(sb.rx.sniperCoords);
		re.len = sb.time;
		return re;
	}

	@Override
	public double[] sniperCoords(boolean put) {
		double[] coords = new double[]{sb.pos, sb.siz};
		if (put)
			sb.rx.sniperCoords.put(sb.time, coords);
		return coords;
	}

	/**
	 * process the user action
	 */
	@Override
	protected void actions() {
		if (sb.ebase.health <= 0)
			return;
		if (sb.st.trail && sb.st.timeLimit != 0 && sb.st.timeLimit * 30 - sb.time < 0)
			return;
		int rec = 0;
		if ((keys.pressed(-1, 0) || action.contains(-1)) && act_mon()) {
			rec |= 1;
			keys.remove(-1, 0);
		}
		if ((keys.pressed(-1, 1) || action.contains(-2)) && act_can()) {
			rec |= 2;
			keys.remove(-1, 1);
		}
		if ((keys.pressed(-1, 2) || action.contains(-3)) && act_sniper()) {
			rec |= 4;
			keys.remove(-1, 2);
		}
		if (!CommonStatic.getConfig().twoRow && (keys.pressed(-3, 0) || action.contains(-4)) && act_change_up()) {
			rec |= 1 << 24;
			keys.remove(-3, 0);
		}
		if (!CommonStatic.getConfig().twoRow && (keys.pressed(-3, 0) || action.contains(-5)) && act_change_down()) {
			rec |= 1 << 25;
			keys.remove(-3, 0);
		}
		if ((keys.pressed(-1, 3) || action.contains(-6)) && act_continue()) {
			rec |= 1 << 26;
			keys.remove(-1, 3);
		}

		if(CommonStatic.getConfig().twoRow) {
			for(byte i = 0; i < 2; i++)
				for (byte j = 0; j < 5; j++) {
					boolean b0 = keys.pressed(i, j);
					boolean b1 = action.contains(i * 5 + j);
					if (keys.pressed(-2, 0) || action.contains(10))
						if (b0) {
							act_lock(i, j);
							rec |= 1 << (i * 5 + j + 3);
							keys.remove(i, j);
						} else if (b1) {
							act_lock(i, j);
							rec |= 1 << (i * 5 + j + 3);
							action.remove((Object) (i * 5 + j));
						}
					if ((b0 || b1 || sb.locks[i][j]))
						if (act_spawn(i, j, b0 || b1) && (b0 || b1))
							rec |= 1 << (i * 5 + j + 13);
				}
		} else
			for (byte j = 0; j < 5; j++) {
				boolean b0 = keys.pressed(sb.frontLineup, j);
				boolean b1 = action.contains(sb.frontLineup * 5 + j);
				if (keys.pressed(-2, 0) || action.contains(10))
					if (b0) {
						act_lock(sb.frontLineup, j);
						rec |= 1 << (sb.frontLineup * 5 + j + 3);
						keys.remove(sb.frontLineup, j);
					} else if (b1) {
						act_lock(sb.frontLineup, j);
						rec |= 1 << (sb.frontLineup * 5 + j + 3);
						action.remove((Object) (sb.frontLineup * 5 + j));
					}
				for (byte i = 0; i < 2; i++) {
					int row = (i + sb.frontLineup) % 2; // check front row first, then back row
					if ((b0 || b1 || sb.locks[i][j]))
						if (act_spawn(i, j, (b0 || b1) && row == sb.frontLineup) && (b0 || b1))
							rec |= 1 << (i * 5 + j + 13);
				}
			}

		action.clear();
		sb.rx.add(rec);
	}
}

class Recorder extends BattleObj {

	private final List<Integer> recd = new ArrayList<>();
	protected final HashMap<Integer, double[]> sniperCoords = new HashMap<>();

	private int num, rep;

	protected void add(int rec) {
		if (rec == num)
			rep++;
		else {
			if (rep > 0) {
				recd.add(num);
				recd.add(rep);
			}
			num = rec;
			rep = 1;
		}
	}

	protected int[] write() {
		if (rep > 0) {
			recd.add(num);
			recd.add(rep);
		}
		int[] ans = new int[recd.size()];
		for (int i = 0; i < recd.size(); i++)
			ans[i] = recd.get(i);
		return ans;
	}

}
