package common.battle;

import common.CommonStatic;
import common.util.stage.EStage;

public abstract class BattleField {

	public StageBasis sb;
	public byte endFrames = 0;
	private boolean btlTick = true;

	protected BattleField(EStage stage, BasisLU bas, int conf, long seed, boolean buttonDelay, byte saveMode) {
		sb = new StageBasis(this, stage, bas, conf, seed, buttonDelay, saveMode);
	}

	protected BattleField(StageBasis bas) {
		sb = bas;
	}

	public void update() {
		if (!CommonStatic.getConfig().fps60 || btlTick) {
			sb.time++;
			if (sb.ebase.health <= 0 || sb.ubase.health <= 0)
				endFrames = (byte) Math.min(126, endFrames + 1);
			else
				endFrames = 0;

			actions();
			sb.update();
		} else
			sb.updateAnimation();
		btlTick = !btlTick;
	}

	protected boolean act_can() {
		return sb.act_can();
	}

	protected void act_lock(int i, int j) {
		sb.act_lock(i, j);
	}

	protected boolean act_mon() {
		return sb.act_mon();
	}

	protected boolean act_sniper() {
		return sb.act_sniper();
	}

	protected boolean act_continue() { return sb.act_continue(); }

	protected boolean act_change_up() {
		return sb.act_change_up();
	}

	protected boolean act_change_down() {
		return sb.act_change_down();
	}

	protected boolean act_spawn(int i, int j, boolean boo) {
		return sb.act_spawn(i, j, boo);
	}

	public abstract double[] sniperCoords(boolean put);

	protected abstract void actions();

}
