package common.battle;

import common.CommonStatic.FakeKey;
import common.util.stage.EStage;
import common.util.stage.Replay;

import java.util.HashMap;

public class SBRply extends Mirror {

	private final Replay r;
	private final MirrorSet mir;

	public SBRply(Replay re) {
		super(re);
		r = re;
		mir = new MirrorSet(r);
	}

	public void back(int t) {
		Mirror m = mir.getReal(sb.time - t);
		sb = m.sb;
		rl = m.rl;
	}

	public int prog() {
		if (r.len == 0)
			return 0;
		return Math.min(mir.size - 1, sb.time * mir.size / r.len);
	}

	public void restoreTo(int perc) {
		Mirror m = mir.getRaw(perc);
		if (m == null)
			return;
		sb = m.sb;
		rl = m.rl;
		while (prog() < perc)
			update();
		UISet();
	}

	public int size() {
		return mir.size - 1;
	}

	public SBCtrl transform(FakeKey kh) {
		return new SBCtrl(kh, (StageBasis) sb.clone(), r);
	}

	@Override
	public void update() {
		super.update();
		mir.add(this);
	}

	public void UISet() {
		if (sb.ebase.health > 0 && sb.ubase.health > 0)
			endFrames = 0;
	}
}

class Mirror extends BattleField {

	protected Release rl;

	protected Mirror(Mirror sr) {
		super((StageBasis) sr.sb.clone());
		rl = sr.rl.clone();
	}

	protected Mirror(Replay r) {
		super(new EStage(r.st.get(), r.star), r.lu, r.cfg, r.seed, r.buttonDelay, r.save);
		rl = new Release(r.action, r.sniperCoords);
	}

	@Override
	public double[] sniperCoords(boolean put) {
		double[] coords = rl.getSniperCoords(sb.time);
		if (coords == null)
			coords = new double[]{sb.pos, sb.siz};
		if (put)
			sb.rx.sniperCoords.put(sb.time, coords);
		return coords;
	}

	/**
	 * process the user action
	 */
	@Override
	protected void actions() {
		int rec = rl.get();
		if ((rec & 1) > 0)
			act_mon();
		if ((rec & 2) > 0)
			act_can();
		if ((rec & 4) > 0)
			act_sniper();
		if ((rec & (1 << 24)) > 0)
			act_change_up();
		if ((rec & (1 << 25)) > 0)
			act_change_down();
		if ((rec & (1 << 26)) > 0)
			act_continue();
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				if ((rec & (1 << (i * 5 + j + 3))) > 0)
					act_lock(i, j);
				act_spawn(i, j, (rec & (1 << (i * 5 + j + 13))) > 0);
			}
		sb.rx.add(rec);
	}

}

class MirrorSet {

	private final Mirror[] mis;
	private final int len;
	protected final int size;

	protected MirrorSet(Replay r) {
		len = r.len + 1;
		size = (int)Math.round(Math.sqrt(len));
		mis = new Mirror[size];
	}

	protected void add(SBRply sb) {
		int t = sb.sb.time;
		if (t * size / len >= size)
			return;
		if (mis[t * size / len] == null)
			mis[t * size / len] = new Mirror(sb);
	}

	protected Mirror getRaw(int t) {
		Mirror mr = mis[t];
		if (mr == null) {
			for (int i = t - 1; i >= 0; i--)
				if (mis[i] != null)
					return new Mirror(mis[i]);
			return null;
		}
		return new Mirror(mr);
	}

	protected Mirror getReal(int t) {
		Mirror m = getRaw(t * size / len);
		while (m.sb.time < t)
			m.update();
		return m;
	}

}

class Release {

	protected final int[] recd;
	private final HashMap<Integer, double[]> sniperCoords;
	private int ind, rec, rex;

	protected Release(int[] action, HashMap<Integer, double[]> sniperCoords) {
		recd = action;
		this.sniperCoords = sniperCoords;
	}

	private Release(Release r) {
		recd = r.recd;
		ind = r.ind;
		rec = r.rec;
		rex = r.rex;
		sniperCoords = r.sniperCoords;
	}

	@Override
	protected Release clone() {
		return new Release(this);
	}

	protected int get() {
		if (recd.length <= ind || rex != 0)
			rec = 0;
		else {
			rec = recd[ind++];
			rex = recd[ind++];
		}
		rex--;
		return rec;
	}

	protected double[] getSniperCoords(int t) {
		return sniperCoords.get(t);
	}

}
