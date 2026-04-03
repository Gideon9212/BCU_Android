package common.battle.entity;

import common.CommonStatic;
import common.battle.BattleField;
import common.battle.StageBasis;
import common.battle.attack.AtkModelAb;
import common.battle.attack.AttackAb;
import common.battle.attack.AttackSimple;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.util.anim.EAnimD;
import common.util.pack.EffAnim.SniperEff;
import common.util.unit.Trait;

public class Sniper extends AtkModelAb {

	private final EAnimD<?> anim = effas().A_SNIPER.getEAnim(SniperEff.IDLE);
	private final EAnimD<?> atka = effas().A_SNIPER.getEAnim(SniperEff.ATK);
	private float coolTime = SNIPER_CD, preTime = 0, atkTime = 0;
	private Entity target;
	public boolean enabled = true;
	public double pos, layer, height, bulletX, targetAngle = 0, cannonAngle = 0, bulletAngle = 0;
	public final BattleField bf; //Used for replay pos/siz gathering

	public Sniper(StageBasis sb, BattleField bf) {
		super(sb);
		this.bf = bf;
	}

	/**
	 * base part of animation
	 */
	public void drawBase(FakeGraphics gra, P ori, float siz) {
		height = ori.y;

		anim.ent[1].alter(9, 1500);
		anim.ent[1].alter(10, 1500);
		atka.ent[1].alter(9, 1500);
		atka.ent[1].alter(10, 1500);

		if (atkTime == 0)
			anim.draw(gra, ori, siz);
		else {
			atka.draw(gra, ori, siz);
		}
	}

	@Override
	public int getAbi() {
		return 0;
	}

	@Override
	public int getDire() {
		return -1;
	}

	@Override
	public float getPos() {
		return b.ubase.pos + SNIPER_POS;
	}

	private void getAngle() {
		if (pos == -1) {
			if(preTime == 0 && bulletX == 0)
				bulletAngle = 0;

			targetAngle = 0;
			return;
		}

		int Cx = b.st.len * 4 - 3200;
		int Cy = 4400;

		int Ux = (int) (pos * 4);
		int Uy = 4480;

		double[] coords = bf.sniperCoords(preTime > 0);
		int scrollPos = (int) (-Math.round(coords[0] / CommonStatic.BattleConst.ratio * 4 / coords[1]));

		int sniperX = (Cx - scrollPos) / 10 + 203;
		int sniperY = (int) (Math.sin(Math.PI / 30 * b.time) * 10) + Cy / 10 - 369;

		int Uy10 = Uy / 10;
		int UyScroll = (Ux - scrollPos) / 10;
		double theta = Math.toDegrees(Math.atan2(sniperY - Uy10 - 4 * layer + 58, sniperX - UyScroll));

		if(preTime == 0 && bulletX == 0) {
			bulletAngle = theta;
		}

		targetAngle = theta;
	}

	public void update() {
		if (!enabled || b.ubase.health <= 0)
			return;
		if (coolTime > 0 && preTime == 0 && atkTime == 0)
			coolTime -= b.timeFlow;

		if (coolTime <= 0 && pos > 0) {
			if(Math.abs(targetAngle - cannonAngle) < 1) {
				coolTime = SNIPER_CD + 1;
				preTime = SNIPER_PRE;
				atkTime = atka.len();
				atka.setup();
				atka.update(false, b.timeFlow);
				anim.setup();
			} else {
				coolTime++;
			}
		}

		// find enemy pos
		if(preTime == 0) {
			pos = -1;
			for (Entity e : b.le)
				if (e.dire == 1 && e.pos > pos && !e.isBase && (e.touchable() & TCH_N) > 0) {
					target = e;
					pos = e.pos;
					layer = e.layer;
				}
		}
		if (enabled)
			getAngle();

		if (preTime > 0) {
			preTime -= b.timeFlow;
			if (preTime <= 0) {
				//fire bullet
				bulletX = b.ubase.pos + SNIPER_POS + 375 * Math.cos(Math.toRadians((int) bulletAngle));

				atka.ent[6].alter(12, 1000);
				anim.ent[6].alter(12, 1000);
			}
		}

		if (bulletX != 0 && bulletX > pos) {
			bulletX = (int) (bulletX * 4 * b.timeFlow - 1500 * Math.cos(Math.toRadians((int) bulletAngle))) / 4.0;

			atka.ent[6].alter(4, (int) ((bulletX - b.ubase.pos - SNIPER_POS) / Math.cos(Math.toRadians((int) bulletAngle)) * CommonStatic.BattleConst.ratio * 0.75));
			anim.ent[6].alter(4, (int) ((bulletX - b.ubase.pos - SNIPER_POS) / Math.cos(Math.toRadians((int) bulletAngle)) * CommonStatic.BattleConst.ratio * 0.75));

			if (bulletX <= pos) {
				int atk = b.b.t().getBaseHealth(target.basis.elu.getInc(C_BASE)) / 20;
				Proc proc = Proc.blank();
				proc.SNIPER.prob = 1;
				SortedPackSet<Trait> CTrait = new SortedPackSet<>();
				CTrait.add(UserProfile.getBCData().traits.get(TRAIT_TOT));
				AttackAb a = new AttackSimple(null, this, atk, CTrait, 0, proc, 0, getPos(), false, null, -1, true, 1);
				a.canon = -1;

				if(target != null && (target.touchable() & TCH_N) > 0) {
					target.damaged(a);
				}

				bulletX = 0;

				atka.ent[6].alter(12, 0);
				anim.ent[6].alter(12, 0);
			}
		}

		if (atkTime > 0) {
			atkTime -= b.timeFlow;
			atka.update(false, b.timeFlow);
		} else
			anim.update(true, b.timeFlow);

		if(bulletX > 0) {
			cannonAngle = targetAngle;
		} else if (cannonAngle != targetAngle) {
			if(cannonAngle < targetAngle) {
				cannonAngle += 1;

				if(cannonAngle > targetAngle)
					cannonAngle = targetAngle;
			} else {
				cannonAngle -= 1;

				if(cannonAngle < targetAngle)
					cannonAngle = targetAngle;
			}
		}
		updateAnim0();
	}

	public void updateAnimation() {
		if (atkTime > 0)
			atka.update(false, b.timeFlow);
		else
			anim.update(true, b.timeFlow);
		updateAnim0();
	}

	private void updateAnim0() {
		if(bulletX > 0) {
			anim.ent[6].alter(12, 1000);
			anim.ent[5].alter(11, (int) Math.round(bulletAngle * 10));
		} else
			anim.ent[5].alter(11, (int) Math.round(cannonAngle * 10));

		atka.ent[5].alter(11, (int) Math.round(bulletAngle * 10));

		anim.ent[1].alter(5, - (int) Math.round((989.5 / 1.5 - 25 * Math.sin(Math.PI * b.time / 30) - height) * CommonStatic.BattleConst.ratio));
		atka.ent[1].alter(5, - (int) Math.round((989.5 / 1.5 - 25 * Math.sin(Math.PI * b.time / 30) - height) * CommonStatic.BattleConst.ratio));

		atka.ent[6].alter(9, 900);
		anim.ent[6].alter(9, 900);
		atka.ent[6].alter(10, 900);
		anim.ent[6].alter(10, 900);
	}

	public void cancel() {
		atkTime = 0;
		preTime = 0;
		bulletX = 0;
	}
}
