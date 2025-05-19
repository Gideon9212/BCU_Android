package common.battle.entity;

import common.CommonStatic;
import common.CommonStatic.BattleConst;
import common.battle.StageBasis;
import common.battle.attack.*;
import common.battle.data.*;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.BattleObj;
import common.util.Data;
import common.util.Data.Proc.POISON;
import common.util.Data.Proc.REVIVE;
import common.util.Data.Proc.SPEED;
import common.util.Data.Proc.COUNTER;
import common.util.anim.AnimU;
import common.util.anim.AnimU.UType;
import common.util.anim.EAnimD;
import common.util.anim.EAnimI;
import common.util.anim.EAnimU;
import common.util.pack.EffAnim;
import common.util.pack.EffAnim.*;
import common.util.pack.Soul;
import common.util.unit.Level;
import common.util.unit.Trait;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Entity class for units and enemies
 */
public abstract class Entity extends AbEntity implements Comparable<Entity> {

	/**
	 * Obtains BC's traits
	 */
	protected static final List<Trait> BCTraits = UserProfile.getBCData().traits.getList();
	private static final int REV = 0, RES = 1, BUR = 2, RESU = 3, REVI = 4, ENTR = 5; //Counter doesn't access SPATKS

	public static class AnimManager extends BattleObj {

		private final Entity e;

		/**
		 * dead FSM time <br>
		 * -1 means not dead<br>
		 * positive value means time remain for death anim to play
		 */
		public double dead = -1;

		/**
		 * KB anim, null means not being KBed, can have various value during battle
		 */
		private EAnimD<KBEff> back;

		/**
		 * entity anim
		 */
		private final EAnimU anim;

		/**
		 * corpse anim
		 */
		public EAnimD<ZombieEff> corpse;

		/**
		 * soul anim, null means not dead yet
		 */
		private EAnimI soul;

		/**
		 * smoke animation for each entity
		 */
		public EAnimD<DefEff> smoke;

		/**
		 * Layer for smoke animation
		 */
		public int smokeLayer;

		/**
		 * x-pos of smoke animation
		 */
		public int smokeX;

		/**
		 * responsive effect FSM time
		 */
		private double efft;

		/**
		 * responsive effect FSM type
		 */
		private byte eftp = A_EFF_INV;

		/**
		 * on-entity effect icons<br>
		 * index defined by Data.A_()
		 */
		private final EAnimD<?>[] effs = new EAnimD[A_TOT];

		/**
		 * Checks if speed is negative to flip
		 */
		private boolean negSpeed = false;

		private AnimManager(Entity ent, EAnimU ea) {
			e = ent;
			anim = ea;
		}

		/**
		 * draw this entity
		 */
		public void draw(FakeGraphics gra, P p, float siz) {
			if (dead > 0) {
				//100 is guessed value comparing from BC
				p.y -= 100 * siz;
				soul.draw(gra, p, siz);
				return;
			}
			FakeTransform at = gra.getTransform();
			if (corpse != null) {
				corpse.paraTo(back);
				corpse.draw(gra, p, siz);
			}
			if (corpse == null || e.status.revs[1] < REVIVE_SHOW_TIME) {
				if (corpse != null) {
					gra.setTransform(at);
					anim.changeAnim(AnimU.TYPEDEF[AnimU.IDLE], false);
				}
			} else {
				gra.delete(at);
				return;
			}

			boolean f = e.status.hypno > 0;
			if(e.data instanceof CustomEntity) {
				if((e.kb.kbType != INT_HB && e.kb.kbType != INT_SW) || (e.kb.kbType == INT_HB && ((CustomEntity) e.data).kbBounce) || (e.kb.kbType == INT_SW && ((CustomEntity) e.data).bossBounce))
					anim.paraTo(back, e.data.getPack().rev != f);
			} else
				anim.paraTo(back, e.data.getPack().rev != f);
			f |= (negSpeed && anim.type == AnimU.TYPEDEF[AnimU.WALK]);
			if (e.kbTime == 0 || e.kb.kbType != INT_WARP)
				anim.draw(gra, p, siz, e.data.getPack().rev != f);
			anim.paraTo(null);
			gra.setTransform(at);
			if (CommonStatic.getConfig().ref)
				e.drawAxis(gra, p, siz);
			gra.delete(at);
		}

		/**
		 * draw the effect icons
		 */
		public void drawEff(FakeGraphics g, P p, float siz) {
			if (dead != -1)
				return;
			if (e.status.warp[2] != 0)
				return;

			FakeTransform at = g.getTransform();
			int EWID = 36;
			float x = p.x;
			if (effs[eftp] != null) {
				effs[eftp].draw(g, p, siz * 0.75f);
			}

			for(int i = 0; i < effs.length; i++) {
				if(i == A_B || i == A_HEAL || i == A_DEMON_SHIELD || i == A_COUNTER || i == A_DMGCUT || i == A_DMGCAP || i == A_REMSHIELD || i == A_RANGESHIELD || i == A_DRAIN)
					continue;
				if ((i == A_SLOW && e.status.stop[0] != 0) || (i == A_UP && e.status.getWeaken() != 1) || (i == A_CURSE && e.status.seal != 0))
					continue;

				EAnimD<?> eae = effs[i];
				if (eae == null)
					continue;
				float offset = 0f;

				g.setTransform(at);
				eae.draw(g, new P(x, p.y+offset), siz * 0.75f);
				x -= EWID * e.dire * siz;
			}
			x = p.x;

			for(int i = A_B; i < effs.length; i++) {
				if(i == A_B || i == A_HEAL || i == A_DEMON_SHIELD || i == A_COUNTER || i == A_DMGCUT || i == A_DMGCAP || i == A_REMSHIELD || i == A_RANGESHIELD || i == A_DRAIN) {
					EAnimD<?> eae = effs[i];
					if(eae == null)
						continue;
					float offset = -25f * siz;

					g.setTransform(at);
					eae.draw(g, new P(x, p.y + offset), siz * 0.75f);
				}
			}

			g.delete(at);
		}

		/**
		 * get an effect icon
		 */
		@SuppressWarnings("unchecked")
		public void getEff(int t) {
			int dire = e.dire;
			switch (t) {
				case INV: {
					effs[eftp] = null;
					eftp = A_EFF_INV;
					effs[eftp] = effas().A_EFF_INV.getEAnim(DefEff.DEF);
					efft = effas().A_EFF_INV.len(DefEff.DEF);
					break;
				} case P_WAVE: {
					effs[A_WAVE_INVALID] = (dire == -1 ? effas().A_WAVE_INVALID : effas().A_E_WAVE_INVALID).getEAnim(DefEff.DEF);
					break;
				} case STPWAVE: {
					effs[eftp] = null;
					eftp = A_WAVE_STOP;
					EffAnim<DefEff> eff = dire == -1 ? effas().A_WAVE_STOP : effas().A_E_WAVE_STOP;
					effs[eftp] = eff.getEAnim(DefEff.DEF);
					efft = eff.len(DefEff.DEF);
					break;
				} case INVWARP: {
					effs[eftp] = null;
					eftp = A_FARATTACK;
					EffAnim<DefEff> eff = dire == -1 ? effas().A_FARATTACK : effas().A_E_FARATTACK;
					effs[eftp] = eff.getEAnim(DefEff.DEF);
					efft = eff.len(DefEff.DEF);
					break;
				} case P_STOP: {
					effs[A_STOP] = (dire == -1 ? effas().A_STOP : effas().A_E_STOP).getEAnim(DefEff.DEF);
					break;
				} case P_IMUATK: {
					effs[A_IMUATK] = effas().A_IMUATK.getEAnim(DefEff.DEF);
					break;
				} case P_SLOW: {
					effs[A_SLOW] = (dire == -1 ? effas().A_SLOW : effas().A_E_SLOW).getEAnim(DefEff.DEF);
					break;
				} case P_LETHARGY: {
					effs[A_LETHARGY] = (dire == -1 ? effas().A_LETHARGY : effas().A_E_LETHARGY).getEAnim(e.status.getLethargy() > 0 ? LethargyEff.DOWN : LethargyEff.UP);
					break;
				} case P_WEAK: {
					if (e.status.getWeaken() == 1)
						break;
					if (e.status.getWeaken() < 1)
						effs[A_DOWN] = (dire == -1 ? effas().A_DOWN : effas().A_E_DOWN).getEAnim(DefEff.DEF);
					else
						effs[A_DOWN] = (dire == -1 ? effas().A_WEAK_UP : effas().A_E_WEAK_UP).getEAnim(WeakUpEff.UP);
					break;
				} case P_CURSE: {
					effs[A_CURSE] = (dire == -1 ? effas().A_CURSE : effas().A_E_CURSE).getEAnim(DefEff.DEF);
					break;
				} case P_POISON: {
					int mask = e.status.poison;
					EffAnim<?>[] arr = {effas().A_POI0, e.dire == -1 ? effas().A_POI1 : effas().A_POI1_E, effas().A_POI2, effas().A_POI3, effas().A_POI4,
							effas().A_POI5, effas().A_POI6, effas().A_POI7};
					for (int i = 0; i < A_POIS.length; i++)
						if ((mask & (1 << i)) > 0) {
							int id = A_POIS[i];
							effs[id] = ((EffAnim<DefEff>) arr[i]).getEAnim(DefEff.DEF);
						}
					break;
				} case P_SEAL: {
					effs[A_SEAL] = (dire == -1 ? effas().A_SEAL : effas().A_E_SEAL).getEAnim(DefEff.DEF);
					break;
				} case P_STRONG: {
					effs[A_UP] = (dire == -1 ? effas().A_UP : effas().A_E_UP).getEAnim(DefEff.DEF);
					break;
				} case P_LETHAL: {
					EffAnim<DefEff> ea = dire == -1 ? effas().A_SHIELD : effas().A_E_SHIELD;
					effs[A_SHIELD] = ea.getEAnim(DefEff.DEF);
					CommonStatic.setSE(SE_LETHAL);
					break;
				} case P_WARP: {
					EffAnim<WarpEff> ea = effas().A_W;
					int ind = (int)e.status.warp[2];
					WarpEff pa = ind == 0 ? WarpEff.ENTER : WarpEff.EXIT;
					e.basis.lea.add(new WaprCont(e.pos, pa, e.layer, anim, e.dire, (e.getAbi() & AB_TIMEI) != 0));
					CommonStatic.setSE(ind == 0 ? SE_WARP_ENTER : SE_WARP_EXIT);
					e.status.warp[ind] = ea.len(pa);
					break;
				} case A_GUARD: {
					effs[A_B] = effas().A_E_GUARD.getEAnim(GuardEff.NONE);
					CommonStatic.setSE(SE_BARRIER_NON);
					break;
				} case A_GUARD_BRK: {
					effs[A_B] = effas().A_E_GUARD.getEAnim(GuardEff.BREAK);
					CommonStatic.setSE(SE_BARRIER_ABI);
					break;
				} case BREAK_ABI: {
					effs[A_B] = (dire == -1 ? effas().A_B : effas().A_E_B).getEAnim(BarrierEff.BREAK);
					CommonStatic.setSE(SE_BARRIER_ABI);
					break;
				} case BREAK_ATK: {
					effs[A_B] = (dire == -1 ? effas().A_B : effas().A_E_B).getEAnim(BarrierEff.DESTR);
					CommonStatic.setSE(SE_BARRIER_ATK);
					break;
				} case BREAK_NON: {
					effs[A_B] = (dire == -1 ? effas().A_B : effas().A_E_B).getEAnim(BarrierEff.NONE);
					CommonStatic.setSE(SE_BARRIER_NON);
					break;
				} case P_ARMOR: {
					ArmorEff index = e.status.getArmor() >= 0 ? ArmorEff.DEBUFF : ArmorEff.BUFF;
					effs[A_ARMOR] = (dire == -1 ? effas().A_ARMOR : effas().A_E_ARMOR).getEAnim(index);
					break;
				} case P_SPEED: {
					SpeedEff index = e.status.getSpeed(e.data.getSpeed()) >= e.data.getSpeed() ? SpeedEff.UP : SpeedEff.DOWN;
					effs[A_SPEED] = (dire == -1 ? effas().A_SPEED : effas().A_E_SPEED).getEAnim(index);
					break;
				} case HEAL: {
					effs[A_HEAL] = (dire == -1 ? effas().A_HEAL : effas().A_E_HEAL).getEAnim(DefEff.DEF);
					break;
				} case SHIELD_HIT: {
					EffAnim<ShieldEff> eff = dire == -1 ? effas().A_DEMON_SHIELD : effas().A_E_DEMON_SHIELD;
					boolean half = e.status.shield[0] * 1.0 / (e.getProc().DEMONSHIELD.hp * e.shieldMagnification) < 0.5;
					effs[A_DEMON_SHIELD] = eff.getEAnim(half ? ShieldEff.HALF : ShieldEff.FULL);
					CommonStatic.setSE(SE_SHIELD_HIT);
					break;
				} case SHIELD_BROKEN: {
					effs[A_DEMON_SHIELD] = (dire == -1 ? effas().A_DEMON_SHIELD : effas().A_E_DEMON_SHIELD).getEAnim(ShieldEff.BROKEN);
					CommonStatic.setSE(SE_SHIELD_BROKEN);
					break;
				} case SHIELD_REGEN: {
					effs[A_DEMON_SHIELD] = (dire == -1 ? effas().A_DEMON_SHIELD : effas().A_E_DEMON_SHIELD).getEAnim(ShieldEff.REGENERATION);
					CommonStatic.setSE(SE_SHIELD_REGEN);
					break;
				} case SHIELD_BREAKER: {
					effs[A_DEMON_SHIELD] = (dire == -1 ? effas().A_DEMON_SHIELD : effas().A_E_DEMON_SHIELD).getEAnim(ShieldEff.BREAKER);
					CommonStatic.setSE(SE_SHIELD_BREAKER);
					break;
				} case P_COUNTER: {
					effs[A_COUNTER] = (dire == -1 ? effas().A_COUNTER : effas().A_E_COUNTER).getEAnim(DefEff.DEF);
					break;
				} case P_DMGCUT: {
					effs[A_DMGCUT] = (dire == -1 ? effas().A_DMGCUT : effas().A_E_DMGCUT).getEAnim(DefEff.DEF);
					break;
				} case DMGCAP_FAIL: {
					effs[A_DMGCAP] = (dire == -1 ? effas().A_DMGCAP : effas().A_E_DMGCAP).getEAnim(DmgCap.FAIL);
					break;
				} case DMGCAP_SUCCESS: {
					effs[A_DMGCAP] = (dire == -1 ? effas().A_DMGCAP : effas().A_E_DMGCAP).getEAnim(DmgCap.SUCCESS);
					break;
				} case REMSHIELD_NEAR: {
					effs[A_REMSHIELD] = (dire == -1 ? effas().A_REMSHIELD : effas().A_E_REMSHIELD).getEAnim(RemShieldEff.NEAR);
					break;
				} case REMSHIELD_FAR: {
					effs[A_REMSHIELD] = (dire == -1 ? effas().A_REMSHIELD : effas().A_E_REMSHIELD).getEAnim(RemShieldEff.FAR);
					break;
				} case P_WEAKAURA: {
					effs[A_WEAKAURA] = (dire == -1 ? effas().A_AURA : effas().A_E_AURA).getEAnim(AuraEff.ENEDOWN);
					break;
				} case A_WEAKAURASTR: {
					effs[A_WEAKAURA] = (dire == -1 ? effas().A_AURA : effas().A_E_AURA).getEAnim(AuraEff.ENEUP);
					break;
				} case P_STRONGAURA: {
					effs[A_STRONGAURA] = (dire == -1 ? effas().A_AURA : effas().A_E_AURA).getEAnim(AuraEff.UNIUP);
					break;
				} case A_STRAURAWEAK: {
					effs[A_STRONGAURA] = (dire == -1 ? effas().A_AURA : effas().A_E_AURA).getEAnim(AuraEff.UNIDOWN);
					break;
				} case P_RAGE: {
					effs[A_RAGE] = effas().A_RAGE.getEAnim(DefEff.DEF);
					break;
				} case P_HYPNO: {
					effs[A_HYPNO] = effas().A_HYPNO.getEAnim(DefEff.DEF);
					break;
				} case P_RANGESHIELD: {
					effs[A_RANGESHIELD] = (dire == -1 ? effas().A_RANGESHIELD : effas().A_E_RANGESHIELD).getEAnim(RangeShieldEff.AREA);
					break;
				} case RANGESHIELD_SINGLE: {
					effs[A_RANGESHIELD] = (dire == -1 ? effas().A_RANGESHIELD : effas().A_E_RANGESHIELD).getEAnim(RangeShieldEff.SINGLE);
					break;
				} case P_DRAIN: {
					effs[A_DRAIN] = (dire == -1 ? effas().A_DRAIN : effas().A_E_DRAIN).getEAnim(DefEff.DEF);
					break;
				} case P_BLESS: {
					effs[A_BLESS] = effas().A_BLESS.getEAnim(DefEff.DEF); //(dire == -1 ? effas().A_BLESS : effas().A_E_BLESS).getEAnim(DefEff.DEF);
					break;
				} case P_SPEEDUP: {
					EffAnim<SpeedEff> eff = dire == -1 ? effas().A_SPEED : effas().A_E_SPEED;
					SpeedEff index;
					index = e.status.adrenaline >= 100 ? SpeedEff.UP : SpeedEff.DOWN;
					effs[A_DRENALINE] = eff.getEAnim(index);
				}
			}
		}

		/**
		 * update effect icons animation
		 */
		private void checkEff() {
			if (efft <= 0)
				effs[eftp] = null;
			if (e.status.stop[0] <= 0)
				effs[A_STOP] = null;
			if (e.status.slow <= 0)
				effs[A_SLOW] = null;
			if (e.status.weaks.isEmpty())
				effs[A_DOWN] = null;
			if (e.status.lethargies.isEmpty())
				effs[A_LETHARGY] = null;
			if (e.status.curse <= 0)
				effs[A_CURSE] = null;
			if (e.status.inv[0] == 0 && e.status.wild <= 0)
				effs[A_IMUATK] = null;
			for (int i = 0; i < A_POIS.length; i++)
				if ((e.status.poison & (1 << i)) == 0)
					effs[A_POIS[i]] = null;
			if (e.status.seal <= 0)
				effs[A_SEAL] = null;
			if (e.status.adrenaline == 100)
				effs[A_DRENALINE] = null;
			if (effs[A_SHIELD] != null && effs[A_SHIELD].done())
				effs[A_SHIELD] = null;
			if (effs[A_WAVE_INVALID] != null && effs[A_WAVE_INVALID].done())
				effs[A_WAVE_INVALID] = null;
			if (e.status.strengthen <= 0)
				effs[A_UP] = null;
			if (effs[A_B] != null && effs[A_B].done())
				effs[A_B] = null;
			if (e.status.armors.isEmpty())
				effs[A_ARMOR] = null;
			if (e.status.speeds.isEmpty())
				effs[A_SPEED] = null;
			if(effs[A_HEAL] != null && effs[A_HEAL].done())
				effs[A_HEAL] = null;
			if(effs[A_COUNTER] != null && effs[A_COUNTER].done())
				effs[A_COUNTER] = null;
			if(effs[A_DMGCUT] != null && effs[A_DMGCUT].done())
				effs[A_DMGCUT] = null;
			if(effs[A_DMGCAP] != null && effs[A_DMGCAP].done())
				effs[A_DMGCAP] = null;
			if (e.status.rage <= 0)
				effs[A_RAGE] = null;
			if (e.status.hypno <= 0)
				effs[A_HYPNO] = null;
			if (e.status.blessings.isEmpty())
				effs[A_BLESS] = null;
			if(effs[A_RANGESHIELD] != null && effs[A_RANGESHIELD].done())
				effs[A_RANGESHIELD] = null;
			if(effs[A_DRAIN] != null && effs[A_DRAIN].done())
				effs[A_DRAIN] = null;
			if(effs[A_REMSHIELD] != null && effs[A_REMSHIELD].done())
				effs[A_REMSHIELD] = null;
			if(effs[A_DEMON_SHIELD] != null && effs[A_DEMON_SHIELD].done())
				effs[A_DEMON_SHIELD] = null;
			efft -= e.getTimeFreeze();
		}

		/**
		 * process kb animation <br>
		 * called when kb is applied
		 */
		private void kbAnim() {
			int t = e.kb.kbType;
			if (t != INT_SW && t != INT_WARP)
				if(e.status.revs[1] >= REVIVE_SHOW_TIME) {
					e.anim.corpse = (e.getDire() == -1 ? effas().A_U_ZOMBIE : effas().A_ZOMBIE).getEAnim(ZombieEff.BACK);
				} else {
					if (e.anim.corpse != null) {
						if(e.anim.corpse.type == ZombieEff.REVIVE)
							for (int i = e.spInd; i < e.data.getRevive().length; i++)
								e.basis.getAttack(e.aam.getSpAttack(REVI, i));
						e.anim.corpse = null;
						e.status.revs[1] = 0;
					}
					setAnim(AnimU.TYPEDEF[AnimU.HB], true);
				}
			else
				setAnim(AnimU.TYPEDEF[AnimU.WALK], false);
			if (t == INT_WARP) {
				e.kbTime = e.status.warp[0];
				getEff(P_WARP);
				e.status.warp[2] = 1;
			}
			if (t == INT_KB)
				e.kbTime = e.status.kb;
			else if (t == INT_HB)
				back = effas().A_KB.getEAnim(KBEff.KB);
			else if (t == INT_SW)
				back = effas().A_KB.getEAnim(KBEff.SW);
			else if (t == INT_ASS)
				back = effas().A_KB.getEAnim(KBEff.ASS);
			if (t != INT_WARP)
				e.kbTime += 1;
			// Z-kill icon
			if (e.health <= 0 && e.zx.tempZK && e.traits.contains(BCTraits.get(TRAIT_ZOMBIE))) {
				EAnimD<DefEff> eae = effas().A_Z_STRONG.getEAnim(DefEff.DEF);
				e.basis.lea.add(new EAnimCont(e.pos, e.layer, eae));
				CommonStatic.setSE(SE_ZKILL);
			}
			e.status.burs[1] = 0;
		}

		protected byte deathSurge = 0;

		/**
		 * set kill anim
		 */
		private void kill() {
			if ((e.getAbi() & AB_GLASS) != 0) {
				e.dead = true;
				dead = 0;
				return;
			}
			if (e.getProc().DEATHSURGE.perform(e.basis.r))
				deathSurge |= 1;
			if (e.getProc().MINIDEATHSURGE.perform(e.basis.r))
				deathSurge |= 2;

			if (deathSurge != 0) {
				e.status.weaks.clear();
				soul = CommonStatic.getBCAssets().demonSouls.get((1 - e.getDire()) / 2).getEAnim(AnimU.SOUL[0]);
				dead = soul.len();
				CommonStatic.setSE(SE_DEATH_SURGE);
			} else {
				// converge souls layer: death on the same frame = same soul height
				// still not sure how this precisely work in BC, it seems to have exceptions
				Soul s = Identifier.get(e.data.getDeathAnim());
				dead = s == null ? 0 : (soul = s.getEAnim(AnimU.SOUL[0])).len();
			}
		}

		private void setAtk(int atk) {
			setAnim(anim.anim().types[2 + atk], true);
		}

		private int setAnim(UType t, boolean skip) {
			if (anim.type != t) {
				e.spInd = 0;
				anim.changeAnim(t, skip);
			}
			return anim.len();
		}

		private void cont() {
			if (anim.type.toString().contains("attack"))
				setAnim(AnimU.TYPEDEF[AnimU.WALK], false);
			else if (anim.type == AnimU.TYPEDEF[AnimU.HB]) {
				e.interrupt(0, 0f);
				setAnim(AnimU.TYPEDEF[AnimU.WALK], false);
			}
		}

		private void update() {
			checkEff();
			updateAnimation();
			if (dead > 0)
				dead = Math.max(0 ,dead - e.getTimeFreeze());

			if (anim.done() && anim.type == AnimU.TYPEDEF[AnimU.ENTRY])
				setAnim(AnimU.TYPEDEF[AnimU.IDLE], true);
			if (dead >= 0) {
				if (deathSurge != 0 && soul.len() - dead >= 21) {// 21 is guessed delay compared to BC
					e.aam.getDeathSurge(deathSurge);
					deathSurge = 0;
				}
				for (int i = e.spInd; i < e.data.getResurrection().length; i++) {
					AtkDataModel adm = e.data.getResurrection()[i];
					if ((soul == null && !e.dead) || (soul != null && soul.len() - dead >= adm.pre) || (soul != null && dead == 0 && !e.dead)) {
						e.spInd++;
						e.basis.getAttack(e.aam.getSpAttack(RES, i));
					}
				}
			}

			e.dead = dead == 0;
		}

		private void updateAnimation() {
			float t = e.getTimeFreeze();
			for (EAnimD<?> eff : effs)
				if (eff != null)
					eff.update(false, t);
			if (back != null || dead > 0)
				t = e.getTime();
			if ((e.status.stop[0] == 0 || e.status.stop[1] != 0) && (e.kbTime == 0 || (e.kb.kbType != INT_SW && e.kb.kbType != INT_WARP))) {
				float rate = t;
				if (e.status.slow == 0 && anim.type == AnimU.TYPEDEF[AnimU.WALK] || anim.type == AnimU.TYPEDEF[AnimU.RETREAT])
					rate *= Math.abs(e.getSpeed(e.data.getSpeed(), 0) / (e.data.getSpeed() * 0.5f));
				anim.update(false, rate);
			} if (back != null)
				back.update(false, t);
			if (dead > 0)
				soul.update(false, t);
			if (corpse != null)
				corpse.update(false, t);

			if(smoke != null) {
				if(smoke.done()) {
					smoke = null;
					smokeLayer = -1;
					smokeX = -1;
				} else
					smoke.update(false, t);
			}
		}
	}

	protected static class AtkManager extends BattleObj {

		/**
		 * atk FSM time
		 */
		protected double atkTime;

		/**
		 * attack times remain
		 */
		protected int attacksLeft;

		/**
		 * atk id primarily for display
		 */
		private int tempAtk = -1;

		private final Entity e;

		/**
		 * const field, attack count
		 */
		private int multi;

		/**
		 * atk loop FSM type
		 */
		private int preID;

		/**
		 * pre-atk time const field
		 */
		private int[] pres;

		/**
		 * atk loop FSM time
		 */
		private double preTime;

		private AtkManager(Entity ent) {
			e = ent;
			setAtk();
			attacksLeft = e.data.getAtkLoop();
		}

		private void setAtk() {
			MaskAtk[] atks = e.data.getAtks(e.aam.atkType);
			pres = new int[multi = atks.length];
			for (int i = 0; i < multi; i++)
				pres[i] = atks[i].getPre();
		}

		private void setUp() {
			if (e.data.realAtkCount() > 1) {
				int old = e.aam.atkType;
				if (e.getProc().AI.calcstrongest) {
					int[] total = new int[e.data.getAtkTypeCount()];
					for (int i = 0; i < total.length; i++)
						total[i] += e.aam.predictDamage(i);

					for (int i = e.data.firstAtk(); i < total.length; i++)
						if (total[i] > total[e.aam.atkType])
							e.aam.atkType = i;
					if (total[e.aam.atkType] <= 0)
						return;
				} else {
					int totShare = 0;
					for (int i = 0; i < e.data.getAtkTypeCount(); i++)
						if (e.aam.isUsable(i))
							totShare += e.data.getShare(i);
					int r = (int) (e.basis.r.nextFloat() * totShare);
					for (int i = 0; i < e.data.getAtkTypeCount(); i++) {
						if (!e.aam.isUsable(i))
							continue;
						r -= e.data.getShare(i);
						if (r <= 0) {
							e.aam.atkType = i;
							break;
						}
					}
				}
				if (old != e.aam.atkType)
					setAtk();
			}
			startAtk(true);
		}

		public void startAtk(boolean anim) {
			atkTime = e.data.getAnimLen(e.aam.atkType);
			preID = 0;
			preTime = pres[0];
			if (anim)
				e.anim.setAtk(e.aam.atkType);
		}

		private void stopAtk() {
			tempAtk = -1;
			if (atkTime > 0)
				atkTime = preTime = 0;
		}

		/**
		 * update attack state
		 */
		protected void updateAttack() {
			tempAtk = -1; //set tempAtk to -1, as axis display isn't needed anymore
			float t = e.getTimeFreeze();
			atkTime = Math.max(0, atkTime - t);
			if (preTime > 0) {
				preTime = Math.max(0, preTime - t);
				if (preTime == 0) {
					int atk0 = preID;
					while (++preID < multi && pres[preID] == 0)
						if (e.data.getAtkModel(e.aam.atkType, preID).getName().toLowerCase().startsWith("combo"))
							e.basis.getAttack(e.aam.getAttack(atk0++));

					tempAtk = preID - 1 > atk0 ? (int) (atk0 + e.basis.r.nextFloat() * (preID - atk0)) : atk0;
					e.basis.getAttack(e.aam.getAttack(tempAtk));
					if (preID < multi)
						preTime = pres[preID];
					else {
						attacksLeft--;
						e.waitTime = Math.max(e.data.getTBA(), 0);
					}
				}
			}
			if (atkTime == 0) {
				e.skipSpawnBurrow = false;
				e.anim.setAnim(AnimU.TYPEDEF[AnimU.IDLE], true);
			}
		}
	}

	private static class KBManager extends BattleObj {

		/**
		 * KB FSM type
		 */
		private int kbType;

		private final Entity e;

		/**
		 * remaining distance to KB
		 */
		private float kbDis;

		/**
		 * temp field to store wanted KB length
		 */
		private float tempKBdist;

		/**
		 * temp field to store wanted KB type
		 */
		private int tempKBtype = -1;

		private float initPos;
		private float kbDuration;
		private float time = 1;

		private KBManager(Entity ent) {
			e = ent;
		}

		/**
		 * process the interruption received
		 */
		private void doInterrupt() {
			int t = tempKBtype;
			if (t == -1)
				return;
			float d = tempKBdist;
			tempKBtype = -1;
			e.atkm.stopAtk();
			e.kbTime = KB_TIME[t];
			kbType = t;
			kbDis = d;
			initPos = e.pos;
			kbDuration = e.kbTime;
			time = 1;
			e.anim.kbAnim();
			e.anim.update();
		}

		private float easeOut(float time, float start, float end, float duration, float dire) {
			time /= duration;
			return -end * time * (time - 2) * dire + start;
		}

		private void interrupt(int t, float d) {
			if (t == INT_ASS && (e.getAbi() & AB_SNIPERI) > 0) {
				e.anim.getEff(INV);
				return;
			}
			if (t == INT_SW && (e.getAbi() & AB_IMUSW) > 0) {
				e.anim.getEff(INV);
				return;
			}
			int prev = tempKBtype;
			if (prev == -1 || KB_PRI[t] >= KB_PRI[prev]) {
				tempKBtype = t;
				tempKBdist = d;
			}
		}

		private void kbmove(float mov) {
			if (mov < 0)
				e.pos -= mov * e.dire;
			else
				e.pos -= Math.min(mov, e.getLim()) * e.dire;
		}

		/**
		 * update KB state <br>
		 * in KB state: deal with warp, KB go back, and anim change <br>
		 * end of KB: check whether it's killed, deal with revive
		 */
		private void updateKB() {
			e.kbTime = Math.max(0, e.kbTime - e.getTime());
			if (e.kbTime == 0) {
				if(e.isBase) {
					e.anim.setAnim(AnimU.TYPEDEF[AnimU.HB], false);
					return;
				}

				if ((e.getAbi() & AB_GLASS) > 0 && e.atkm.atkTime - 1 == 0 && e.atkm.attacksLeft == 0) {
					e.kill(true);
					return;
				}

				e.anim.back = null;
				if(e.status.revs[1] > 0)
					e.anim.corpse = (e.getDire() == -1 ? effas().A_U_ZOMBIE : effas().A_ZOMBIE).getEAnim(ZombieEff.DOWN);

				if(kbType == INT_HB)
					for (int i = e.spInd; i < e.data.getRevenge().length; i++)
						e.basis.getAttack(e.aam.getSpAttack(REV, i));
				e.anim.setAnim(AnimU.TYPEDEF[AnimU.WALK], true);

				kbDuration = 0;
				initPos = 0;
				time = 1;

				if(kbType == INT_HB && e.health > 0 && e.getProc().DEMONSHIELD.hp > 0) {
					e.status.shield[0] = (int) (e.getProc().DEMONSHIELD.hp * e.getProc().DEMONSHIELD.regen * e.shieldMagnification / 100.0);
					if (e.status.shield[0] > e.status.shield[1])
						e.status.shield[1] = e.status.shield[0];
					e.anim.getEff(SHIELD_REGEN);
				}

				if (e.health <= 0)
					e.preKill();
			} else {
				if (kbType != INT_WARP && kbType != INT_KB) {
					float mov = kbDis / e.kbTime;
					kbDis -= mov;
					kbmove(mov);
					if (kbType == INT_HB) {
						for (int i = e.spInd; i < e.data.getRevenge().length; i++)
							if (KB_TIME[INT_HB] - e.kbTime >= e.data.getRevenge()[i].pre) {
								e.basis.getAttack(e.aam.getSpAttack(REV, i));
								e.spInd++;
							}
					}
				} else if (kbType == INT_KB) {
					if (e.isBase && e.health <= 0)
						return;

					if (time == 1)
						kbDuration = e.kbTime;

					float mov = easeOut(time, initPos, kbDis, kbDuration, -e.dire) - e.pos;
					mov *= -e.dire;
					kbmove(mov);

					time += e.getTime();
				} else {
					e.anim.setAnim(AnimU.TYPEDEF[AnimU.IDLE], false);
					if (e.status.warp[0] > 0)
						e.status.warp[0] -= e.getTime();
					if (e.status.warp[1] > 0)
						e.status.warp[1] -= e.getTime();
					EffAnim<WarpEff> ea = effas().A_W;
					if (e.kbTime + 1 == ea.len(WarpEff.EXIT)) {
						kbmove(kbDis);
						kbDis = 0;
						e.anim.getEff(P_WARP);
						e.status.warp[2] = 0;
						e.kbTime -= 11;
					}
				}
			}
		}
	}

	private static class PoisonToken extends BattleObj {

		private final Entity e;

		private final HashMap<POISON, Float> list = new HashMap<>();

		private PoisonToken(Entity ent) {
			e = ent;
		}

		private void add(POISON ws) {
			if (ws.unstackable)
				list.keySet().removeIf(e -> e.unstackable && type(e) == type(ws));
			ws.prob = 0; // used as counter
			list.put(ws, (float)ws.time);
			getMax();
		}

		private void damage(int dmg, int type) {
			type &= 3;
			long mul = type == 0 ? 100 : type == 1 ? e.maxH : type == 2 ? e.health : (e.maxH - e.health);
			e.damage += mul * dmg / 100;
		}

		private void getMax() {
			int max = 0;
			for (POISON poison : list.keySet())
				max |= 1 << type(poison);
			e.status.poison = max;
		}

		private int type(POISON ws) {
			return ws.damage_type.ordinal() + (ws.damage < 0 ? 4 : 0);
		}

		private void update() {
			for (POISON ws : list.keySet())
				if (ws.time > 0) {
					list.replace(ws, list.get(ws) - e.getTime());
					ws.prob -= e.getTime();// used as counter for itv
					if (e.health > 0 && ws.prob <= 0) {
						if (!ws.ignoreMetal && (e instanceof EEnemy && e.data.getTraits().contains(UserProfile.getBCData().traits.get(TRAIT_METAL)) || (e instanceof EUnit && (e.getAbi() & AB_METALIC) != 0)))
							e.damage += 1;
						else
							damage(ws.damage, type(ws));
						ws.prob += ws.itv;
					}
				}
			list.keySet().removeIf(w -> list.get(w) <= 0);
			getMax();
		}

	}

	private static class Barrier extends BattleObj {
		private final Entity e;
		private int health;
		private double timer;

		private Barrier (Entity ent) { e = ent; }

		private void update() {
			if (health > 0) {
				if (timer > 0) {
					timer -= e.getTime();
					if (timer == 0)
						breakBarrier(false);
				}
			} else if (timer > 0) {
				timer -= e.getTime();
				if (timer == 0) {
					health = e.getProc().BARRIER.magnif ? (int) (e.shieldMagnification * e.getProc().BARRIER.health) : e.getProc().BARRIER.health;
					int timeout = e.getProc().BARRIER.timeout;
					if (timeout > 0)
						timer = timeout + effas().A_B.len(BarrierEff.NONE);
					e.anim.getEff(BREAK_NON);
				}
			}
		}

		private void breakBarrier(boolean abi) {
			health = 0;
			e.anim.getEff(abi ? BREAK_ABI : BREAK_ATK);

			int regen = e.getProc().BARRIER.regentime;
			if (regen > 0)
				timer = regen + e.anim.effs[A_B].len();
		}
	}

	private static class ZombX extends BattleObj {

		private final Entity e;

		private final Set<Entity> list = new HashSet<>();

		/**
		 * temp field: marker for zombie killer
		 */
		private boolean tempZK;

		private int extraRev = 0;

		private ZombX(Entity ent) {
			e = ent;
		}

		private byte canRevive() {
			if (e.status.revs[0] != 0)
				return 1;
			int tot = totExRev();
			if (tot == -1 || tot > extraRev)
				return 2;
			return 0;
		}

		private boolean canZK() {
			if (e.getProc().REVIVE.imu_zkill)
				return false;
			for (Entity zx : list)
				if (zx.getProc().REVIVE.imu_zkill)
					return false;
			return true;
		}

		private void damaged(AttackAb atk) {
			tempZK |= (atk.abi & AB_ZKILL) > 0 && canZK();
		}

		private void doRevive(int c) {
			int deadAnim = minRevTime();
			EffAnim<ZombieEff> ea = effas().A_ZOMBIE;
			deadAnim += ea.getEAnim(ZombieEff.REVIVE).len();
			e.status.revs[1] = deadAnim;
			int maxR = maxRevHealth();
			if (maxR > 100)
				e.maxH = Math.min(Integer.MAX_VALUE, e.maxH * maxR / 100);
			e.health = e.maxH * maxR / 100;

			if (c == 1)
				e.status.revs[0] -= e.getTimeFreeze();
			else if (c == 2)
				extraRev++;
		}

		private int maxRevHealth() {
			int max = e.proc.REVIVE.health;
			if (e.status.revs[0] == 0)
				max = 0;
			for (Entity zx : list) {
				int val = zx.proc.REVIVE.health;
				max = Math.max(max, val);
			}
			return max;
		}

		private int minRevTime() {
			int min = e.proc.REVIVE.time;
			if (e.status.revs[0] == 0)
				min = Integer.MAX_VALUE;
			for (Entity zx : list) {
				int val = zx.proc.REVIVE.time;
				min = Math.min(min, val);
			}
			return min;
		}

		private void postUpdate() {
			if (e.health > 0)
				tempZK = false;
		}

		private boolean prekill() {
			int c = canRevive();
			if (!tempZK && c > 0) {
				ProcManager status = e.status;
				doRevive(c);
				// clear state
				e.bdist = 0;
				status.removeActive(false);
				status.burs[1] = 0;
				status.strengthen = 0;
				status.lethal = false;
				status.poison = 0;
				return true;
			}
			return false;
		}

		private int totExRev() {
			int sum = 0;
			for (Entity zx : list) {
				int val = zx.proc.REVIVE.count;
				if (val == -1)
					return -1;
				sum += val;
			}
			return sum;
		}

		/**
		 * update revive status
		 */
		private void updateRevive() {
			float[] rev = e.status.revs;
			AnimManager anim = e.anim;

			list.removeIf(em -> {
				REVIVE.RANGE conf = em.getProc().REVIVE.range_type;
				if (conf == REVIVE.RANGE.FOREVER)
					return false;
				if (conf == REVIVE.RANGE.ALIVE || em.kbTime == -1)
					return em.kbTime == -1;
				return true;
			});
			List<AbEntity> lm = e.basis.inRange(TCH_ZOMBX, -e.getDire(), 0, e.basis.st.len, false);
			for (AbEntity abEntity : lm) {
				if (abEntity == e)
					continue;
				Entity em = ((Entity) abEntity);
				float d0 = em.pos + em.getProc().REVIVE.dis_0;
				float d1 = em.pos + em.getProc().REVIVE.dis_1;
				if ((d0 - e.pos) * (d1 - e.pos) > 0)
					continue;
				if (em.kb.kbType == INT_WARP)
					continue;
				if (!em.getProc().REVIVE.revive_non_zombie && e.traits.contains(BCTraits.get(TRAIT_ZOMBIE)))
					continue;
				REVIVE.RANGE type = em.getProc().REVIVE.range_type;
				if (type == REVIVE.RANGE.ACTIVE && (em.touchable() & (TCH_N | TCH_EX)) == 0)
					continue;
				list.add(em);
			}

			if (rev[1] > 0) {
				EffAnim<ZombieEff> ea = e.getDire() == -1 ? effas().A_U_ZOMBIE : effas().A_ZOMBIE;
				if (anim.corpse == null) {
					anim.corpse = ea.getEAnim(ZombieEff.DOWN);
					anim.corpse.setTime(0);
				}
				if (rev[1] == ea.getEAnim(ZombieEff.REVIVE).len() - 2) {
					anim.corpse = ea.getEAnim(ZombieEff.REVIVE);
					anim.corpse.setTime(0);
				}
				if(e.kbTime == 0) {
					rev[1] -= e.getTimeFreeze();
					if(anim.corpse != null && anim.corpse.type == ZombieEff.REVIVE)
						for (int i = e.spInd; i < e.data.getRevive().length; i++)
							if (anim.corpse.len() - rev[1] >= e.data.getRevive()[i].pre) {
								e.spInd++;
								e.basis.getAttack(e.aam.getSpAttack(REVI, i));
							}
				}
				if (rev[1] <= 0) {
					if(anim.corpse != null && e.anim.corpse.type == ZombieEff.REVIVE)
						for (int i = e.spInd; i < e.data.getRevive().length; i++)
							e.basis.getAttack(e.aam.getSpAttack(REVI, i));
					anim.corpse = null;
				}
			}
		}

	}

	public static class AuraManager extends BattleObj {
		AnimManager anim;
		int defTBA;
		float faura, daura, saura = 1, taura;
		float[] aff = new float[2];
		Stack<Float> atkAuras = new Stack<>();
		Stack<Float> defAuras = new Stack<>();
		Stack<Float> spdAuras = new Stack<>();
		Stack<Float> tbaAuras = new Stack<>();

		public AuraManager(AnimManager anim, int TBA) {
			this.anim = anim;
			defTBA = TBA;
			aff[0] = aff[1] = 1;
		}
		public void setAuras(Proc.AURA aura, boolean weak) {
			if (aura.amult != 0) {
				atkAuras.push((weak ? aura.amult : 100 + aura.amult) / 100f);
				aff[weak ? 0 : 1] *= atkAuras.peek();
			}
			if (aura.dmult != 0) {
				defAuras.push((weak ? 100 + aura.dmult : aura.dmult) / 100f);
				aff[weak ? 0 : 1] /= defAuras.peek();
			}
			if (aura.smult != 0) {
				spdAuras.push((weak ? aura.smult : 100 + aura.smult) / 100f);
				aff[weak ? 0 : 1] *= spdAuras.peek();
			}
			if (aura.tmult != 0) {
				tbaAuras.push((weak ? 100 + aura.tmult : aura.tmult) / 100f);
				aff[weak ? 0 : 1] /= tbaAuras.peek();
			}
		}
		public void updateAuras() {
			faura = daura = saura = taura = 1;
			while (!atkAuras.isEmpty())
				faura *= atkAuras.pop();
			while (!defAuras.isEmpty())
				daura *= defAuras.pop();
			while (!spdAuras.isEmpty())
				saura *= spdAuras.pop();
			while (!tbaAuras.isEmpty())
				taura *= tbaAuras.pop();
			taura--;

			if (aff[0] != 1 && anim.effs[A_WEAKAURA] == null)
				anim.getEff(aff[0] < 1 ? P_WEAKAURA : A_WEAKAURASTR);
			else if (aff[0] == 1)
				anim.effs[A_WEAKAURA] = null;

			if (aff[1] != 1 && anim.effs[A_STRONGAURA] == null)
				anim.getEff(aff[1] > 1 ? P_STRONGAURA : A_STRAURAWEAK);
			else if (aff[1] == 1)
				anim.effs[A_STRONGAURA] = null;
			aff[0] = aff[1] = 1;
		}
		public float getAtkAura() {
			return faura;
		}
		public float getDefAura() {
			return daura;
		}
		public float getSpdAura() {
			return saura;
		}
		public int getTbaAura() {
			return (int)(defTBA * taura);
		}
	}
	private static class SummonManager extends BattleObj {
		public LinkedList<Entity> children = new LinkedList<>();

		public void damaged(AttackAb atk, int dmg, boolean proc) {
			for (Entity child : children) {
				if (proc)
					child.processProcs0(atk, dmg);
				child.damage += dmg;
			}
		}
		public void update() {
			children.removeIf(e -> e.anim.dead == 0);
		}
	}

	public static class ProcManager extends BattleObj {

		public boolean lethal;
		public int kb, strengthen, adrenaline = 100, money, dcut, dcap, poison;
		public double slow, curse, seal, wild, rage, hypno;
		public final int[] shield = new int[2];
		public final double[] stop = new double[2], inv = new double[2];
		public final float[] warp = new float[3], burs = new float[2], revs = new float[2];
		public final LinkedList<double[]> weaks = new LinkedList<>(), armors = new LinkedList<>(), speeds = new LinkedList<>(), lethargies = new LinkedList<>();
		public final HashMap<Proc.BLESSING, Float> blessings = new HashMap<>();

		private final Entity e;

		public ProcManager(Entity ent) {
			e = ent;
		}

		/**Update proc timers*/
		public void update() {
			float time = e.getTime();
			if (stop[0] > 0)
				stop[0] -= time;
			if (slow > 0)
				slow -= time;
			weaks.removeIf(weak -> (weak[0] -= time) <= 0);
			armors.removeIf(armr -> (armr[0] -= time) <= 0);
			speeds.removeIf(sped -> (sped[0] -= time) <= 0);
			lethargies.removeIf(leth -> (leth[0] -= time) <= 0);
			if (curse > 0)
				curse -= time;
			if (seal > 0)
				seal -= time;
			if (inv[0] > 0)
				inv[0] = Math.max(inv[0]-time, 0);
			else if (inv[1] > 0)
				inv[1] -= time;
			if (wild > 0)
				wild -= time;
			if (rage > 0)
				rage -= time;
			if (hypno > 0)
				hypno -= time;

			if (blessings.keySet().removeIf(bless -> {
				float t = blessings.get(bless) - time;
				blessings.replace(bless, t);
				return t <= 0;
			})) {
				for (int i = 0; i < PROC_TOT; i++) {
					if (!procSharable[i] && !bcShareable(i))
						continue;
					e.proc.getArr(i).set(e.data.getProc().getArr(i));
					for (Proc.BLESSING b : blessings.keySet())
						if (b.procs != null)
							e.proc.getArr(i).add(b.procs.getArr(i));
				}
				if (e.dire == 1 || curse + seal == 0) {
					e.traits.clear();
					e.traits.addAll(e.data.getTraits());
					for (Proc.BLESSING b : blessings.keySet())
						e.traits.addAll(b.traits);
				}
			}
		}

		public float getWeaken() {
			float mag = 1f;
			for (double[] weak : weaks)
				mag *= (float)weak[1];
			return mag;
		}
		public float getArmor() {
			float mag = 0f;
			for (double[] armor : armors)
				mag += (float)armor[1];
			return mag;
		}
		public float getSpeed(float mov) {
			for (double[] speed : speeds) {
				if (speed[2] == 2)
					mov = (float)speed[1] * 0.5f;
				else if (speed[2] == 1)
					mov += mov * ((float)speed[1] / 100f);
				else
					mov += (float)speed[1] * 0.5f;
			}
			return mov;
		}
		public float getLethargy() {
			float tba = 0f;
			for (double[] lethargy : lethargies) {
				if (lethargy[2] == 1)
					tba += (float)(e.data.getTBA() * (lethargy[1] / 100.0));
				else if (lethargy[2] == 0)
					tba += (float)lethargy[1];
			}
			return tba;
		}

		/**
		 * Passes procs from one manager to another. Used for *pass procs* summon
		 * @param pm The "another" proc
		 */
		public void pass(ProcManager pm) {
			if (pm.stop[0] != 0) {
				stop[0] = Math.max(stop[0], pm.stop[0]);
				stop[1] = Math.min(stop[1], pm.stop[1]);
				e.anim.getEff(P_STOP);
			}
			slow = Math.max(slow, pm.slow);
			if (slow != 0)
				e.anim.getEff(P_SLOW);
			curse = Math.max(curse, pm.curse);
			if (curse != 0)
				e.anim.getEff(P_CURSE);
			seal = Math.max(seal, pm.seal);
			if (seal != 0)
				e.anim.getEff(P_SEAL);
			poison = Math.max(poison, pm.poison);
			if (poison != 0)
				e.anim.getEff(P_POISON);
			rage = Math.max(rage, pm.rage);
			if (rage != 0)
				e.anim.getEff(P_RAGE);
			hypno = Math.max(hypno, pm.hypno);
			if (hypno != 0)
				e.anim.getEff(P_HYPNO);

			armors.addAll(pm.armors);
			if (!armors.isEmpty())
				e.anim.getEff(P_ARMOR);
			lethargies.addAll(pm.lethargies);
			if (!lethargies.isEmpty())
				e.anim.getEff(P_LETHARGY);
			speeds.addAll(pm.speeds);
			if (!speeds.isEmpty())
				e.anim.getEff(P_SPEED);
			for (int i = 0; i < pm.weaks.size(); i++)
				if (i >= weaks.size())
					weaks.add(pm.weaks.get(i).clone());
				else {
					double[] ws = weaks.get(i), ps = pm.weaks.get(i);
					if (ps[0] != 0) {
						if ((ws[0] - 100) * (ps[0] - 100) == 1)
							ws[0] = Math.max(ws[0], ps[0]);
						else
							ws[0] = ps[0];

						if (ps[1] < 100)
							ws[1] = Math.min(ws[1], ps[1]);
						else
							ws[1] = Math.max(ws[1], ps[1]);
					}
				}
			if (!weaks.isEmpty())
				e.anim.getEff(P_WEAK);
		}

		/**
		 * Clears all procs
		 * @param one Sets procs to 1 instead of 0 if true
		 */
		public void removeActive(boolean one) {
			e.pois.list.clear();
			weaks.clear();
			armors.clear();
			speeds.clear();
			lethargies.clear();
			clearBlessings();
			stop[0] = slow = curse = seal = poison = one ? 1 : 0;
		}
		public void clearBlessings() {
			blessings.clear();
			for (int i = 0; i < PROC_TOT; i++)
				e.proc.getArr(i).set(e.data.getProc().getArr(i));

			e.traits.clear();
			if (e.dire == 1 || curse + seal == 0)
				e.traits.addAll(e.data.getTraits());
		}
		public int blessAbis() {
			int a = 0;
			for (Proc.BLESSING b : blessings.keySet())
				a |= b.abis;
			return a;
		}
	}

	/**Manages animations*/
	public final AnimManager anim;

	/**Manages attacks*/
	protected final AtkManager atkm;

	/**Manages revival*/
	private final ZombX zx = new ZombX(this);

	/**Manages entity auras*/
	public final AuraManager auras;

	/**Connects the summoner to the summoned if bonds are set*/
	private final SummonManager bondTree = new SummonManager();

	/**
	 * game engine, contains environment configuration
	 */
	public final StageBasis basis;

	/**
	 * entity data, read only
	 */
	public final MaskEntity data;

	/**
	 * group, used for search
	 */
	public int group;

	/**
	 * Summoned entity without using summon ability<br>
	 * This is for calculating specific entity's actual damage output during the battle<br>
	 * This entity must not be removed from entity list in battle (Indicator of atk, hp, etc.)<br>
	 * if this list isn't empty
	 */
	public final List<ContAb> summoned = new ArrayList<>();

	/**
	 * Confirmation that this entity is fully dead, not being able to be revived, etc.<br>
	 * This variable is for counting entity number when summoned variable isn't empty
	 */
	public boolean dead = false;

	/**
	 * Damage given to targets<br>
	 * If entity has area attack and attacked several targets, then formula will be dmg * number_of_targets<br>
	 * Formula for calculating damage done to each target is min(atk, target_hp)
	 */
	public long damageGiven = 0;

	/**
	 * Damage taken from opponents
	 */
	public long damageTaken = 0;

	/**
	 * The time that this entity has been alive
	 */
	public int livingTime = 0;

	private final KBManager kb = new KBManager(this);

	/**
	 * layer of display, constant field
	 */
	public int layer;

	/**
	 * proc status, contains ability-specific status data
	 */
	public final ProcManager status = new ProcManager(this);

	/**
	 * trait of enemy, also target trait of unit, uses list
	 */
	public SortedPackSet<Trait> traits;

	/**
	 * attack model
	 */
	protected final AtkModelEntity aam;

	/**
	 * temp field: damage accumulation
	 */
	private long damage;

	/**
	 * const field
	 */
	protected boolean isBase;

	/**
	 * KB/burrow state: <br>
	 * -1: dead (spirit) <br>
	 * positive: KB time count-down <br>
	 * negative: burrow type <br>
	 * 0: none of the above
	 */
	protected float kbTime;

	/**
	 * wait FSM time
	 */
	private double waitTime;

	/**
	 * remaining burrow distance
	 */
	private float bdist;

	/**
	 * poison proc processor
	 */
	private final PoisonToken pois = new PoisonToken(this);

	/**
	 * abilities that are activated after it's attacked
	 */
	private final List<AttackAb> tokens = new LinkedList<>();

	/**
	 * temp field within an update loop <br>
	 * used for moving determination
	 */
	private boolean touch;

	/**
	 * temp field: whether it can attack
	 */
	private boolean touchEnemy;

	/**
	 * Alternate abilities changed by attacks
	 */
	private int altAbi = 0;

	/**
	 * determines whether to skip burrowing at spawn point
	 * burrow will happen after the first step or the first attack
	 * only used by boss
	 */
	protected boolean skipSpawnBurrow = false;

	/**
	 * entity's barrier processor
	 */
	private final Barrier barrier = new Barrier(this);

	public boolean hasBarrier() {
		return barrier.health > 0;
	}

	/**
	 * Used for regenerating shield considering enemy's magnification
	 */
	private final float shieldMagnification;

	/**
	 * true if unit is dead and can't be revived
	 */
	private boolean killCounted = false;

	/**
	 * Index for Special Attack, reset when animation changes
	 */
	private int spInd = 0;

	/**
	 * Procs
	 */
	private final Proc proc;

	/**
	 * EEnemy Constructor
	 * @param b Stage Data
	 * @param de Enemy Data
	 * @param ea Animations
	 * @param atkMagnif Atk Buff
	 * @param hpMagnif Health Buff
	 */
	protected Entity(StageBasis b, MaskEntity de, EAnimU ea, float atkMagnif, float hpMagnif) {
		super(Math.round(de.getHp() * hpMagnif));
		basis = b;
		data = de;
		proc = data.getProc().clone();
		aam = AtkModelEntity.getEnemyAtk(this, atkMagnif);
		anim = new AnimManager(this, ea);
		atkm = new AtkManager(this);
		shieldMagnification = hpMagnif;
		auras = new AuraManager(anim, de.getTBA());
		ini(hpMagnif);
	}

	/**
	 * Entity constructor used by EUnit
	 * @param b Stage Data
	 * @param de Unit Data
	 * @param ea Animations
	 * @param lvMagnif Level
	 * @param pc PCoin Data
	 * @param lv Effective Entity level
	 */
	protected Entity(StageBasis b, MaskEntity de, EAnimU ea, float lvMagnif, PCoin pc, Level lv) {
		super((pc != null && lv != null && lv.getTalents().length == pc.max.length) ?
				(int) ((1 + b.elu.getInc(Data.C_DEF) * 0.01) * (int) ((int) (Math.round(de.getHp() * lvMagnif) * b.b.t().getDefMulti()) * pc.getStatMultiplication(Data.PC2_HP, lv.getTalents()))) :
				(int) ((1 + b.elu.getInc(Data.C_DEF) * 0.01) * (int) (Math.round(de.getHp() * lvMagnif) * b.b.t().getDefMulti()))
		);
		basis = b;
		data = de;
		proc = data.getProc().clone();
		aam = AtkModelEntity.getUnitAtk(this, b.b.t().getAtkMulti(), lvMagnif, pc, lv);
		anim = new AnimManager(this, ea);
		atkm = new AtkManager(this);
		shieldMagnification = lvMagnif;
		auras = new AuraManager(anim, de.getTBA());
		ini(lvMagnif);
	}

	/**
	 * Initializes all non-final variables found in both constructors
	 */
	private void ini(double hpMagnif) {
		barrier.health = getProc().BARRIER.magnif ? (int) (getProc().BARRIER.health * hpMagnif) : getProc().BARRIER.health;
		barrier.timer = getProc().BARRIER.timeout;
		status.burs[0] = proc.BURROW.count;
		status.revs[0] = proc.REVIVE.count;
		status.dcut = proc.DMGCUT.magnif ? (int) (hpMagnif * proc.DMGCUT.dmg) : proc.DMGCUT.dmg;
		status.dcap = proc.DMGCAP.magnif ? (int) (hpMagnif * proc.DMGCAP.dmg) : proc.DMGCAP.dmg;
		status.shield[0] = status.shield[1] = (int)(proc.DEMONSHIELD.hp * hpMagnif);
		if (((DataEntity)data).tba < 0)
			waitTime = Math.max(data.getTBA(), 0);
	}

	/**
	 * Switches out entity abilities as dictated by atk alt abis.
	 * @param alt the attack's alt abis
	 */
	public void altAbi(int alt) {
		altAbi ^= alt;
	}

	/**
	 * Receive attack. Also processes shields
	 */
	@Override
	public void damaged(AttackAb atk) {
		damageTaken += atk.atk;
		sumDamage(atk.atk, true);

		if (status.inv[0] == -1) {//Spirit
			anim.getEff(P_IMUATK);
			return;
		}

		Proc.IMUATK imuatk = getProc().IMUATK;
		if (imuatk.prob > 0 && (atk.dire == -1 || receive(-1) || ctargetable(atk.trait, atk.attacker))) {
			if (status.inv[0] + status.inv[1] == 0 && (imuatk.prob == 100 || basis.r.nextFloat() * 100 < imuatk.prob)) {
				status.inv[0] = (int) (imuatk.time * (1 + 0.2 / 3 * getFruit(atk.trait, atk.dire, -1)));
				status.inv[1] = imuatk.cd;
				anim.getEff(P_IMUATK);
			}
			if (status.inv[0] > 0)
				return;
		}

		int dmg = getDamage(atk, atk.atk);

		Proc.CANNI cRes = getProc().IMUCANNON;
		if (atk.canon > 0 && cRes.mult != 0)
			if ((atk.canon & cRes.type) > 0) {
				if (cRes.mult > 0)
					anim.getEff(P_WAVE);

				if (cRes.mult == 100)
					return;
				else {
					dmg = (int) (dmg * (100 - cRes.mult) / 100);
					switch (atk.canon) {
						case 2:
							atk.getProc().SLOW.time = (int) (atk.getProc().SLOW.time * (100 - cRes.mult) / 100);
							break;
						case 4:
						case 16:
							atk.getProc().STOP.time = (int) (atk.getProc().STOP.time * (100 - cRes.mult) / 100);
							break;
						case 32:
							if (cRes.mult > 0 && basis.r.nextFloat() * 100 < cRes.mult)
								atk.getProc().BREAK.clear();
							atk.getProc().KB.dis = (int) (atk.getProc().KB.dis * (100 - cRes.mult) / 100);
							break;
						case 64:
							atk.getProc().CURSE.time = (int) (atk.getProc().CURSE.time * (100 - cRes.mult) / 100);
					}
				}
			}
		// if immune to wave and the attack is wave, jump out
		if (atk.waveType != 5 && (((WT_WAVE | WT_MINI | WT_MEGA) & atk.waveType) > 0) && atk.canon != 16) {
			Proc.WAVE w = (WT_WAVE & atk.waveType) > 0 ? atk.getProc().WAVE : atk.getProc().MINIWAVE;
			if (getProc().IMUWAVE.pid.match(w.pid)) {
				if (getProc().IMUWAVE.mult > 0)
					anim.getEff(P_WAVE);
				if (getProc().IMUWAVE.mult == 100)
					return;
				else
					dmg = (int) (dmg * (100 - getProc().IMUWAVE.mult) / 100);
			}
		}
		if ((atk.waveType & WT_MOVE) > 0 && getProc().IMUMOVING.pid.match(atk.getProc().MOVEWAVE.pid)) {
			if (getProc().IMUMOVING.mult > 0)
				anim.getEff(P_WAVE);
			if (getProc().IMUMOVING.mult == 100)
				return;
			else
				dmg = (int) (dmg * (100 - getProc().IMUMOVING.mult) / 100);
		}
		if ((atk.waveType & WT_BLST) > 0 && getProc().IMUBLAST.pid.match(atk.getProc().BLAST.pid)) {
			if (getProc().IMUBLAST.mult > 0)
				anim.getEff(P_WAVE);
			if (getProc().IMUBLAST.mult == 100)
				return;
			else
				dmg = (int) (dmg * (100 - getProc().IMUBLAST.mult) / 100);
		}
		if ((atk.waveType & (WT_VOLC | WT_MIVC)) > 0 && getProc().IMUVOLC.pid.match(((AttackVolcano)atk).pid)) {
			if (getProc().IMUVOLC.mult > 0)
				anim.getEff(P_WAVE);
			if (getProc().IMUVOLC.mult == 100) {
				AttackVolcano volc = (AttackVolcano)atk;
				if (!hasBarrier() && status.shield[0] == 0 && volc.handler != null && !volc.handler.reflected && !volc.handler.surgeSummoned.contains(this) && getProc().DEMONVOLC.perform(basis.r))
					new DemonCont(this, volc);
				return;
			} else
				dmg = (int) (dmg * (100 - getProc().IMUVOLC.mult) / 100);
		}
		Proc.RANGESHIELD rngs = getProc().RANGESHIELD;
		if ((atk instanceof AttackSimple && ((AttackSimple)atk).range == rngs.range) && rngs.perform(basis.r)) {
			anim.getEff(rngs.range ? P_RANGESHIELD : RANGESHIELD_SINGLE);
			CommonStatic.setSE(SE_RANGESHIELD);
			if (rngs.mult == 100)
				return;
			else if (rngs.mult != 0)
				dmg = (int) (dmg * (100 - rngs.mult) / 100);
		}
		boolean proc = true;

		Proc.DMGCUT dmgcut = getProc().DMGCUT;
		if (dmgcut.prob > 0 && ((dmgcut.traitIgnore && status.curse == 0) || ctargetable(atk.trait, atk.attacker)) && dmg < status.dcut && dmg > 0 && (dmgcut.prob == 100 || basis.r.nextInt(100) < dmgcut.prob)) {
			anim.getEff(P_DMGCUT);
			if (dmgcut.procs)
				proc = false;

			if (dmgcut.reduction == 100) {
				if (!proc)
					return;
				dmg = 0;
			} else if (dmgcut.reduction != 0)
				dmg = dmg * (100 - dmgcut.reduction) / 100;
		}

		Proc.DMGCAP dmgcap = getProc().DMGCAP;
		if (dmgcap.prob > 0 && ((dmgcap.traitIgnore && status.curse == 0) || ctargetable(atk.trait, atk.attacker)) && dmg > status.dcap && (dmgcap.prob == 100 || basis.r.nextInt(100) < dmgcap.prob)) {
			anim.getEff(dmgcap.nullify ? DMGCAP_SUCCESS : DMGCAP_FAIL);
			if (dmgcap.procs)
				proc = false;

			if (dmgcap.nullify) {
				if (!proc)
					return;
				dmg = 0;
			} else
				dmg = status.dcap;
		}

		if (atk.attacker != null) {
			Proc.REMOTESHIELD remote = getProc().REMOTESHIELD;
			double stRange = Math.abs(atk.attacker.pos - pos);
			if (remote.prob > 0 && remote.reduction + remote.block != 0 && ((!remote.traitCon && status.curse == 0) || ctargetable(atk.trait, atk.attacker)) &&
					(remote.waves || atk instanceof AttackSimple) && stRange >= remote.minrange && stRange <= remote.maxrange && (remote.prob == 100 || basis.r.nextInt(100) < remote.prob)) {
				if (remote.procs)
					proc = false;

				anim.getEff(stRange <= data.getRange() ? REMSHIELD_NEAR : REMSHIELD_FAR);
				if (remote.block != 0) {
					atk.r.add(remote);
					if (remote.block > 0)
						anim.getEff(STPWAVE);
				}

				if (remote.reduction == 100) {
					if (!proc)
						return;
					dmg = 0;
				} else if (remote.reduction != 0)
					dmg = dmg * (100 - remote.reduction) / 100;
			}
			for (Proc.REMOTESHIELD r : atk.r) {
				if (((!r.traitCon && status.curse == 0) || ctargetable(atk.trait, atk.attacker))
						&& stRange >= r.minrange && stRange <= r.maxrange) {
					if (r.procs)
						proc = false;

					if (r.block == 100) {
						if (!proc)
							return;
						dmg = 0;
					} else if (r.block != 0)
						dmg = dmg * (100 - r.block) / 100;
				}
			}
		}

		boolean barrierContinue = !hasBarrier();
		boolean shieldContinue = status.shield[0] == 0;

		if (!barrierContinue) {
			if (atk.getProc().BREAK.prob > 0) {
				barrier.breakBarrier(true);
				barrierContinue = true;
			} else if (dmg >= barrier.health) {
				barrier.breakBarrier(false);
				status.removeActive(true);
			} else {
				anim.getEff(BREAK_NON);
				status.removeActive(true);
			}
		}

		boolean metalKillerActivate = atk.getProc().METALKILL.mult > 0;
		if (dire == 1) {
			metalKillerActivate &= data.getTraits().contains(UserProfile.getBCData().traits.get(TRAIT_METAL));
		} else if (dire == -1)
			metalKillerActivate &= (data.getAbi() & AB_METALIC) != 0;
		if (metalKillerActivate)
			dmg = dmg + (int) Math.max(health * atk.getProc().METALKILL.mult / 100f, 1f);

		if (!shieldContinue) {
			if (atk.getProc().SHIELDBREAK.prob > 0) {
				status.shield[0] = 0;
				anim.getEff(SHIELD_BREAKER);
				shieldContinue = true;
			} else if (dmg >= status.shield[0]) {
				status.shield[0] = 0;
				anim.getEff(SHIELD_BROKEN);
			} else {
				status.shield[0] -= dmg;
				if (status.shield[0] > status.shield[1])
					status.shield[0] = status.shield[1];

				anim.getEff(SHIELD_HIT);
			}
		}
		if (!barrierContinue)
			return;
		//75.0 is guessed value compared from BC
		if (atk.getProc().CRIT.mult > 0) {
			basis.lea.add(new EAnimCont(pos, layer, effas().A_CRIT.getEAnim(DefEff.DEF), -75f));
			CommonStatic.setSE(SE_CRIT);
		}
		//75.0 is guessed value compared from BC
		if (atk.getProc().SATK.mult > 0) {
			basis.lea.add(new EAnimCont(pos, layer, effas().A_SATK.getEAnim(DefEff.DEF), -75f));
			CommonStatic.setSE(SE_SATK);
		}

		if (metalKillerActivate)
			basis.lea.add(new EAnimCont(pos, layer, (dire == 1 ? effas().A_E_METAL_KILLER : effas().A_METAL_KILLER).getEAnim(DefEff.DEF), -75f));

		if (!shieldContinue)
			return;

		if ((atk.waveType & (WT_VOLC | WT_MIVC)) > 0) {
			AttackVolcano volc = (AttackVolcano)atk;
			if (volc.handler != null && !volc.handler.reflected && !volc.handler.surgeSummoned.contains(this) && getProc().DEMONVOLC.perform(basis.r))
				new DemonCont(this, volc);
		}

		tokens.add(atk);
		atk.playSound(isBase);
		hit = 2;
		damage += dmg;
		zx.damaged(atk);
		status.money = (int) atk.getProc().BOUNTY.mult;
		if (dmg < 0)
			anim.getEff(HEAL);

		if (atk.isLongAtk || atk instanceof AttackVolcano)
			anim.smoke = effas().A_WHITE_SMOKE.getEAnim(DefEff.DEF);
		else
			anim.smoke = effas().A_ATK_SMOKE.getEAnim(DefEff.DEF);
		anim.smokeLayer = (int) (layer + 3 - basis.r.irFloat() * -6);
		anim.smokeX = (int) (pos + 25 - basis.r.irFloat() * -50);

		bondTree.damaged(atk, dmg, proc);
		final int FDmg = dmg;
		atk.notifyEntity(e -> {
			COUNTER counter = getProc().COUNTER;
			if (!atk.isCounter && counter.prob > 0 && e.getDire() != getDire() && (e.touchable() & getTouch()) > 0 && (counter.prob == 100 || basis.r.nextFloat() * 100 < counter.prob)) {
				boolean isWave = ((WT_WAVE | WT_MINI | WT_MEGA | WT_MOVE | WT_VOLC | WT_MIVC | WT_BLST) & atk.waveType) > 0;
				if (!isWave || counter.counterWave != COUNTER.CWAVE.NONE) {
					float[] ds = counter.minRange != 0 || counter.maxRange != 0 ? new float[]{pos + counter.minRange, pos + counter.maxRange} : aam.touchRange();
					int reflectAtk = FDmg * counter.damage / 100;

					Proc reflectProc = Proc.blank();
					if ((counter.procType % 2) == 1)
						for (String s0 : AtkModelEntity.par)
							if (s0.equals("VOLC") || s0.equals("WAVE") || s0.equals("MINIWAVE") || s0.equals("MINIVOLC") || s0.equals("BLAST")) {
								if (isWave && counter.counterWave == COUNTER.CWAVE.REFLECT)
									reflectProc.get(s0).set(atk.getProc().get(s0));
							} else
								reflectProc.get(s0).set(atk.getProc().get(s0));

					if (data.getCounter() != null) {
						if (counter.useOwnDamage)
							reflectAtk = data.getCounter().atk;

						if (counter.procType >= 2) {
							Proc p = data.getCounter().getProc();
							for (String s0 : AtkModelEntity.par)
								if (p.get(s0).perform(basis.r))
									reflectProc.get(s0).set(p.get(s0));
						}
					} else {
						if (counter.useOwnDamage)
							reflectAtk = getAtk();

						if (counter.procType >= 2) {
							Proc p = data.getAllProc();
							for (String s0 : AtkModelEntity.par) {
								if ((s0.equals("VOLC") || s0.equals("WAVE") || s0.equals("MINIWAVE") || s0.equals("MINIVOLC")) && (!isWave || counter.counterWave != COUNTER.CWAVE.REFLECT))
									continue;
								if (p.get(s0).perform(e.basis.r))
									reflectProc.get(s0).set(p.get(s0));
							}
						}
					}
					if (counter.maxDamage != 0) {
						int lim = (int)(counter.maxDamage * ((float)(FDmg) / atk.atk));
						reflectAtk = Math.min(reflectAtk, counter.maxDamage < 0 ? Math.min(-lim, (int) health) : lim);
					}
                    if (status.getWeaken() != 1)
						reflectAtk = (int) (reflectAtk * status.getWeaken());
					if (status.strengthen != 0)
						reflectAtk += reflectAtk * status.strengthen / 100;
					reflectAtk *= auras.getAtkAura();
					if (!isBase)
						if (atk.getProc().ARMOR.prob > 0 && checkAIImmunity(atk.getProc().ARMOR.mult, getProc().IMUARMOR.focus, getProc().IMUARMOR.mult < 0) && getProc().IMUARMOR.mult < 100)
							reflectAtk *= (100 + atk.getProc().ARMOR.mult) / 100.0;
						else if (!status.armors.isEmpty())
							reflectAtk *= (100 + status.getArmor()) / 100.0;
					reflectAtk *= auras.getDefAura();

					AttackSimple as = new AttackSimple(this, aam, reflectAtk, traits, getAbi(), reflectProc, ds[0], ds[1],
							data.getCounter() != null ? data.getCounter() : data.getAtkModel(data.firstAtk(), 0), e.layer, false, counter.areaAttack);
					if (counter.areaAttack)
						as.capture();
					if (as.counterEntity(counter.outRange || (e.pos - ds[0]) * (e.pos - ds[1]) <= 0 ? e : null))
						anim.getEff(Data.P_COUNTER);
				}
			}

			int d = FDmg;
			if (!isBase)
				if (atk.getProc().ARMOR.prob > 0 && checkAIImmunity(atk.getProc().ARMOR.mult, getProc().IMUARMOR.focus, getProc().IMUARMOR.mult < 0) && getProc().IMUARMOR.mult < 100)
					d *= (100 + atk.getProc().ARMOR.mult) / 100.0;
				else if (!status.armors.isEmpty())
					d *= (100 + status.getArmor()) / 100.0;
			d *= auras.getDefAura();

			e.damageGiven += Math.max(health - maxH, Math.min(d, health));
			sumDamage(d, false);
			basis.dmgStatistics.get(e.data.getPack())[0] += Math.max(health - maxH, Math.min(d, health));
		});
		if (proc)
			processProcs0(atk, FDmg);
	}

	/**
	 * Sums damage to statistics
	 * @param atk Damage dealt
	 * @param raw true if damage isn't affected by procs and abilities
	 */
	protected abstract void sumDamage(int atk, boolean raw);

	/**
	 * Inflict the applying procs on this entity.
	 * @param atk Attack Data
	 * @param dmg Effective damage
	 */
	protected void processProcs0(AttackAb atk, int dmg) {
		if (atk.getProc().POIATK.mult > 0) {
			float rst = getResistValue(atk, false, getProc().IMUPOIATK.mult);
			if (rst == 0f)
				anim.getEff(INV);
			else {
				float poiDmg = (float) (atk.getProc().POIATK.mult * rst / 100f);
				damage += maxH * poiDmg;
				basis.lea.add(new EAnimCont(pos, layer, effas().A_POISON.getEAnim(DefEff.DEF)));
				CommonStatic.setSE(SE_POISON);

				sumDamage((int)(maxH * poiDmg), false);
				Entity e = atk.attacker;
				if (e != null) {
					long totDmg = Math.min((long)(maxH * poiDmg), Math.max(0, health - dmg));
					e.damageGiven += totDmg;
					basis.dmgStatistics.get(e.data.getPack())[0] += totDmg;
				}
			}
		}
		if (atk.attacker != null && atk.attacker.health > 0 && atk.getProc().DRAIN.mult > 0 && btargetable(atk)) {
			atk.attacker.health = Math.min(atk.attacker.health + (long) (dmg * atk.getProc().DRAIN.mult / 100), atk.attacker.maxH);
			atk.attacker.anim.getEff(P_DRAIN);
		}
		processProcs(atk);
	}

	/**
	 * Inflict the applying procs which don't need effective damage on this entity.
	 * @param atk Attack data
	 */
	private void processProcs(AttackAb atk) {
		// process proc part
		if (!btargetable(atk))
			return;

		float f = getFruit(atk.trait, atk.dire, 1);
		float time = atk.origin instanceof AttackCanon ? 1 : 1 + f * 0.2f / 3;
		if (atk.getProc().STOP.time != 0 || atk.getProc().STOP.prob > 0)
			freeze(atk, time);
		if (atk.getProc().SLOW.time != 0 || atk.getProc().SLOW.prob > 0)
			slow(atk, time);
		if (atk.getProc().WEAK.time > 0)
			weaken(atk, time);
		if (atk.getProc().LETHARGY.time > 0)
			lethargy(atk, time);
		if (atk.getProc().CURSE.time != 0 || atk.getProc().CURSE.prob > 0)
			curse(atk, time);
		if (atk.getProc().KB.dis != 0)
			knockback(atk, f);

		if (atk.getProc().SNIPER.prob > 0)
			interrupt(INT_ASS, KB_DIS[INT_ASS]);
		if (atk.getProc().BOSS.prob > 0)
			interrupt(INT_SW, KB_DIS[INT_SW]);

		if (atk.getProc().WARP.prob > 0)
			warp(atk);
		if (atk.getProc().SEAL.prob > 0)
			seal(atk, time);
		if (atk.getProc().POISON.time > 0)
			poison(atk);
		if (!isBase && atk.getProc().ARMOR.time > 0)
			breakArmor(atk, time);
		if (atk.getProc().SPEED.time > 0)
			hasten(atk, time);
		if (atk.getProc().RAGE.time > 0)
			enrage(atk, time);
		if (atk.getProc().HYPNO.time > 0)
			hypnotize(atk, time);
		if (atk.getProc().BLESSING.prob > 0) {
			if (status.blessings.isEmpty())
				anim.getEff(P_BLESS);
			else if (!atk.getProc().BLESSING.stackable)
				status.clearBlessings();
			Proc.BLESSING b = (Proc.BLESSING)atk.getProc().BLESSING.clone();
			status.blessings.put(b, (float)b.time);
			if (b.procs != null)
				for (int i = 0; i < PROC_TOT; i++)
					if (procSharable[i] || bcShareable(i))
						proc.getArr(i).add(b.procs.getArr(i));
			if (dire == 1 || (status.curse + status.seal <= 0))
				traits.addAll(b.traits);
		}
	}
	public void freeze(AttackAb atk, float time) {
		float rst = getResistValue(atk, true, getProc().IMUSTOP.mult + (getProc().IMUSTOP.block == 100 ? 100 : 0));

		if (rst > 0f) {
			int val = (int)((int)(atk.getProc().STOP.time * time) * rst);
			double tim = atk.getProc().STOP.mult != 0 ? atk.getProc().STOP.mult : 100;
			if (val < 0) {
				status.stop[0] = Math.max(status.stop[0], Math.abs(val));
				status.stop[1] = Math.max(status.stop[1], (100 - tim) / 100);
			} else {
				status.stop[0] = val;
				status.stop[1] = (100 - tim) / 100;
			}
			anim.getEff(P_STOP);
		} else
			anim.getEff(INV);
	}
	public void slow(AttackAb atk, float time) {
		float rst = getResistValue(atk, true, getProc().IMUSLOW.mult + (getProc().IMUSLOW.block == 100 ? 100 : 0));
		if (rst > 0f) {
			int val = (int)((int)(atk.getProc().SLOW.time * time) * rst);
			if (val < 0)
				status.slow = Math.max(status.slow, Math.abs(val));
			else
				status.slow = val;
			anim.getEff(P_SLOW);
		} else
			anim.getEff(INV);
	}
	public void weaken(AttackAb atk, float time) {
		double i = getProc().IMUWEAK.mult + (getProc().IMUWEAK.block == 100 ? 100 : 0);
		float rst = getResistValue(atk, true, checkAIImmunity(atk.getProc().WEAK.mult - 100, getProc().IMUWEAK.focus, i > 0) ? i : 0);
		if (rst > 0f) {
			double val = Math.floor((int)(atk.getProc().WEAK.time * time) * rst);
			if (status.weaks.isEmpty() || atk.getProc().WEAK.stackable)
				status.weaks.add(new double[]{Math.abs(val), atk.getProc().WEAK.mult / 100.0});
			else {
				double[] curw = new double[]{status.weaks.get(0)[0], status.getWeaken()};
				status.weaks.clear();
				if (val < 0)
					curw[0] = Math.max(curw[0], Math.abs(val));
				else
					curw[0] = val;

				if (atk.getProc().WEAK.mult > 100)
					curw[1] = Math.max(curw[1], atk.getProc().WEAK.mult / 100.0);
				else if (val < 0)
					curw[1] = Math.min(curw[1], atk.getProc().WEAK.mult / 100.0);
				else
					curw[1] = atk.getProc().WEAK.mult / 100.0;
				status.weaks.add(curw);
			}
			anim.getEff(P_WEAK);
		} else
			anim.getEff(INV);
	}
	public void lethargy(AttackAb atk, float time) {
		double i = getProc().IMULETHARGY.mult + (getProc().IMULETHARGY.block == 100 ? 100 : 0);
		float rst = getResistValue(atk, true, checkAIImmunity(atk.getProc().LETHARGY.mult, getProc().IMULETHARGY.focus, i > 0) ? i : 0);
		if (rst > 0f) {
			int val = (int)((int)(atk.getProc().LETHARGY.time * time) * rst);
			if (status.lethargies.isEmpty() || atk.getProc().LETHARGY.stackable)
				status.lethargies.add(new double[]{Math.abs(val), atk.getProc().LETHARGY.mult, atk.getProc().LETHARGY.percentage ? 1 : 0});
			else {
				double[] curw = new double[]{status.lethargies.get(0)[0], status.getLethargy(), atk.getProc().LETHARGY.percentage ? 1 : 0};
				status.lethargies.clear();
				if (val < 0)
					curw[0] = Math.max(curw[0], Math.abs(val));
				else
					curw[0] = val;

				if (atk.getProc().LETHARGY.mult > 0)
					curw[1] = (int) Math.max(curw[1], atk.getProc().LETHARGY.mult);
				else if (val < 0)
					curw[1] = (int) Math.min(curw[1], atk.getProc().LETHARGY.mult);
				else
					curw[1] = (int) atk.getProc().LETHARGY.mult;
				status.lethargies.add(curw);
			}
			anim.getEff(P_LETHARGY);
		} else
			anim.getEff(INV);
	}
	public void curse(AttackAb atk, float time) {
		float rst = getResistValue(atk, true, getProc().IMUCURSE.mult + (getProc().IMUCURSE.block == 100 ? 100 : 0));
		if (rst > 0f) {
			int val = (int) ((int)(atk.getProc().CURSE.time * time) * rst);
			if (val < 0)
				status.curse = Math.max(status.curse, Math.abs(val));
			else
				status.curse = val;
			anim.getEff(P_CURSE);
		} else
			anim.getEff(INV);
	}
	public void knockback(AttackAb atk, float f) {
		float rst = getResistValue(atk, true, getProc().IMUKB.mult + (getProc().IMUKB.block == 100 ? 100 : 0));
		if (rst > 0f) {
			status.kb = atk.getProc().KB.time;
			interrupt(atk.getProc().KB.time == KB_TIME[INT_HB] ? INT_HB : P_KB, atk.getProc().KB.dis * (1 + f * 0.1f) * rst);
		} else
			anim.getEff(INV);
	}
	public void warp(AttackAb atk) {
		float rst = getResistValue(atk, true, getProc().IMUWARP.mult + (getProc().IMUWARP.block == 100 ? 100 : 0));
		if (rst > 0f) {
			Data.Proc.WARP warp = atk.getProc().WARP;
			interrupt(INT_WARP, warp.dis == warp.dis_1 ? warp.dis : warp.dis + (int) (basis.r.nextFloat() * (warp.dis_1 - warp.dis)));
			EffAnim<WarpEff> e = effas().A_W;
			int len = e.len(WarpEff.ENTER) + e.len(WarpEff.EXIT);
			int val = (int)(atk.getProc().WARP.time * rst);
			status.warp[0] = val + len;
		} else
			anim.getEff(INVWARP);
	}
	public void seal(AttackAb atk, float time) {
		float rst = getResistValue(atk, true, proc.IMUSEAL.mult + (getProc().IMUSEAL.block == 100 ? 100 : 0));
		if (rst > 0f) {
			int val = (int) (atk.getProc().SEAL.time * time);
			val = (int) (val * rst);
			if (val < 0)
				status.seal = Math.max(status.seal, Math.abs(val));
			else
				status.seal = val;
			anim.getEff(P_SEAL);
		} else
			anim.getEff(INV);
	}
	public void poison(AttackAb atk) {
		double i = getProc().IMUPOI.mult + (getProc().IMUPOI.block == 100 ? 100 : 0);
		float res = getResistValue(atk, true, checkAIImmunity(atk.getProc().POISON.damage, getProc().IMUPOI.focus, i < 0) ? i : 0);
		if (res > 0f) {
			POISON ws = (POISON) atk.getProc().POISON.clone();
			ws.time = (int)(ws.time * res);
			if (atk.atk != 0 && ws.modifAffected)
				ws.damage = (int) (ws.damage * (float) getDamage(atk, atk.atk) / atk.atk);

			pois.add(ws);
			anim.getEff(P_POISON);
		} else
			anim.getEff(INV);
	}
	public void breakArmor(AttackAb atk, float time) {
		double i = getProc().IMUARMOR.mult + (getProc().IMUARMOR.block == 100 ? 100 : 0);
		float res = getResistValue(atk, true, checkAIImmunity(atk.getProc().ARMOR.mult, getProc().IMUARMOR.focus, i < 0) ? i : 0);
		if (res > 0f) {
			if (!atk.getProc().ARMOR.stackable)
				status.armors.clear();
			int val = (int) (atk.getProc().ARMOR.time * time * res);
			status.armors.add(new double[]{val, atk.getProc().ARMOR.mult});
			anim.getEff(P_ARMOR);
		} else
			anim.getEff(INV);
	}
	public void hasten(AttackAb atk, float time) {
		float res = getResistValue(atk, true, getProc().IMUSPEED.mult + (getProc().IMUSPEED.block == 100 ? 100 : 0));
		boolean b;
		if (atk.getProc().SPEED.type == SPEED.TYPE.SET)
			b = (data.getSpeed() > atk.getProc().SPEED.speed && res > 0) || (data.getSpeed() < atk.getProc().SPEED.speed && res < 0);
		else
			b = res < 0;
		if (!checkAIImmunity(atk.getProc().SPEED.speed, getProc().IMUSPEED.focus, b))
			res = 1;

		if (res > 0f) {
			int val = (int) (atk.getProc().SPEED.time * time * res);
			if (!atk.getProc().SPEED.stackable)
				status.speeds.clear();
			else if (atk.getProc().SPEED.type == SPEED.TYPE.SET)
				status.speeds.removeIf(spd -> spd[2] == 2);
			status.speeds.add(new double[]{val, atk.getProc().SPEED.speed, atk.getProc().SPEED.type.ordinal()});
			status.speeds.sort(Comparator.comparingDouble(s -> -s[2]));
			anim.getEff(P_SPEED);
		} else
			anim.getEff(INV);
	}
	public void enrage(AttackAb atk, float time) {
		float res = getResistValue(atk, true, getProc().IMURAGE.mult + (getProc().IMURAGE.block == 100 ? 100 : 0));
		if (res > 0f) {
			int t = (int) ((atk.getProc().RAGE.time * time) * res);
			if (t < 0)
				status.rage = Math.max(Math.abs(t), status.rage);
			else
				status.rage = t;
			anim.getEff(P_RAGE);
		} else
			anim.getEff(INV);
	}
	public void hypnotize(AttackAb atk, float time) {
		float res = getResistValue(atk, true, getProc().IMUHYPNO.mult + (getProc().IMUHYPNO.block == 100 ? 100 : 0));
		if (res > 0f) {
			int t = (int) ((atk.getProc().HYPNO.time * time) * res);
			if (t < 0)
				status.hypno = Math.max(Math.abs(t), status.hypno);
			else
				status.hypno = t;
			anim.getEff(P_HYPNO);
		} else
			anim.getEff(INV);
	}

	/**
	 * Gets the properly formatted resistance value to a certain proc, and adds SuperSage-related resistances if those apply.
	 * @param atk The Attack. Used to check for ability/trait compatibility atm
	 * @param SageRes Whether Sage Resistances are to be applied.
	 * @param procResist The raw amount of resistance
	 * @return formatted resistance value
	 */
	public abstract float getResistValue(AttackAb atk, boolean SageRes, double procResist);

	/**
	 * Used exclusively for smartImu, which rids of immunities for exclusively buffs/debuffs, depending on the side param.
	 * @param val The resistance value
	 * @param side The kind of proc targetted by the Immunity Ignorance. 0 is none, 1 is buff, -1 is debuff.
	 * @param invert Invert the result if condition passes
	 * @return true if immunity applies
	 */
	public static boolean checkAIImmunity(double val, Proc.IMUAD.FOCUS side, boolean invert) {
		if (side == Proc.IMUAD.FOCUS.ALL)
			return true;
		if (invert)
			return val * side.effect < 0;
		return val * side.effect > 0;
	}

	/**
	 * get the current ability bitmask
	 */
	@Override
	public int getAbi() {
		if (status.seal > 0)
			return ((data.getAbi() | status.blessAbis()) ^ altAbi) & (AB_ONLY | AB_METALIC | AB_GLASS);
		return (data.getAbi() | status.blessAbis()) ^ altAbi;
	}

	/**
	 * Get total damage, only used in display and counter
	 */
	public int getAtk() {
		return aam.getAtk();
	}

	/**
	 * get the current proc array
	 */
	@Override
	public Proc getProc() {
		if (status.seal > 0)
			return empty;
		return proc;
	}

	/**
	 * Interrupt the current animation and set up KB/Warp/BossWave.
	 */
	public void interrupt(int t, float d) {
		if(isBase && health <= 0)
			return;

		kb.interrupt(t, d);
	}

	@Override
	public boolean isBase() {
		return isBase;
	}

	/**
	 * save it dead, proceed death animation
	 *
	 * @param glass if this is true, it means it dies because of self-destruct,
	 * and entity will not drop money because of this
	 */
	public void kill(boolean glass) {
		if (kbTime == -1)
			return;
		kbTime = -1;
		atkm.stopAtk();

		anim.kill();
	}

	/**
	 * This function stops enemy attack when continue is used
	 */
	public void cont() {
		if (dire != 1)
			return;
		summoned.clear();
		pos = basis.ebase.pos;
		atkm.stopAtk();
		anim.cont();
	}

	/**
	 * update the entity after receiving attacks
	 */
	@Override
	public void postUpdate() {
		int hb = data.getHb();
		long ext = health * hb % maxH;
		if (ext == 0)
			ext = maxH;
		if (!status.armors.isEmpty())
			damage = (long) (damage * (100 + status.getArmor()) / 100.0);

		damage = (long)(damage * auras.getDefAura());
		if (!isBase && damage > 0 && kbTime <= 0 && kbTime != -1 && (ext <= damage * hb || health < damage))
			interrupt(INT_HB, KB_DIS[INT_HB]);
		health -= damage;
		if (health > maxH)
			health = maxH;

		// increase damage
		float strong = getProc().STRONG.health;
		if ((touchable() & TCH_CORPSE) == 0 && strong > 0 && damage != 0) {
			boolean wz = status.strengthen == 0;
			if (getProc().STRONG.incremental && health * 100 > maxH * strong) {
				status.strengthen = (int)(getProc().STRONG.mult * (maxH - health) / (maxH * (100 - strong) / 100.0));
			} else if (health * 100 <= maxH * strong)
				status.strengthen = getProc().STRONG.mult;

			if (wz && status.strengthen != 0)
				anim.getEff(P_STRONG);
		}
		// adrenaline
		float threshold = getProc().SPEEDUP.health;
		if ((touchable() & TCH_CORPSE) == 0 && threshold > 0 && damage != 0) {
			boolean wz = status.adrenaline == 100;
			if (getProc().SPEEDUP.incremental && health * 100 > maxH * threshold) {
				status.adrenaline = 100 + (int)((getProc().SPEEDUP.mult - 100) * (maxH - health) / (maxH * (100 - threshold) / 100.0));
			} else if (health * 100 <= maxH * threshold)
				status.adrenaline = getProc().SPEEDUP.mult;

			if (wz && status.adrenaline != 100)
				anim.getEff(P_SPEEDUP);
		}
		damage = 0;
		// lethal strike
		if (getProc().LETHAL.prob > 0 && health <= 0) {
			boolean b = getProc().LETHAL.prob == 100 || basis.r.nextFloat() * 100 < getProc().LETHAL.prob;
			if (!status.lethal && b) {
				health = 1;
				anim.getEff(P_LETHAL);
			}
			status.lethal = true;
		}

		for (AttackAb token : tokens)
			token.model.invokeLater(token, this);
		tokens.clear();

		if(isBase && health <= 0)
			kbTime = 1;

		kb.doInterrupt();

		if ((getAbi() & AB_GLASS) > 0 && atkm.atkTime - 1 <= 0 && kbTime == 0 && atkm.attacksLeft == 0)
			kill(true);

		// update ZKill
		zx.postUpdate();

		if (isBase && health < 0) {
			health = 0;
			atkm.stopAtk();
			anim.setAnim(AnimU.TYPEDEF[AnimU.HB], true);
		}

		if(!dead || !summoned.isEmpty())
			livingTime++;
		summoned.removeIf(s -> !s.activate);

		if(health <= 0 && zx.canRevive() == 0 && !killCounted)
			onLastBreathe();
		if (health > 0)
			status.money = 0;
	}

	/**
	 * Sets the animation that will be used for summon
	 * @param conf The type of animation used
	 */
	public void setSummon(Proc.SUMMON_ANIM conf, Entity bond) {
		if (conf == Proc.SUMMON_ANIM.WARP) {
			kb.kbType = INT_WARP; // conf 1 - Warp exit animation
			kbTime = effas().A_W.len(WarpEff.EXIT);
			status.warp[2] = 1;
		} else if (conf == Proc.SUMMON_ANIM.BURROW && data.getPack().anim.anims.length >= 7) {
			kbTime = -3; // conf 2 - Unborrow animation
			bdist = -1;
		} else if (conf == Proc.SUMMON_ANIM.BURROW_DISABLE && data.getPack().anim.anims.length >= 7) {
			kbTime = -3; // conf 3 - Unborrow with Disabled burrow
			status.burs[0] = 0;
			bdist = -1;
		} else if (conf == Proc.SUMMON_ANIM.ATTACK)
			atkm.setUp(); // conf 5 - Sets animation to attack animation. Used mainly for spirits
		else if (conf == Proc.SUMMON_ANIM.EVERYWHERE_DOOR) {
			if (basis.le.remove(this)) // conf 6 - Sets animation to Everywhere Door animation
				basis.doors.add(new DoorCont(basis, this));
		} else if (conf != Proc.SUMMON_ANIM.ENTRY)
			anim.setAnim(AnimU.TYPEDEF[AnimU.WALK], true); // conf 0 - Sets animation to walk animation. conf 4 - sets the animation to entry, if unit has one

		if (bond != null) {
			bond.bondTree.children.add(this);
			bondTree.children.add(bond);
		}
	}

	/**
	 * A quicker ctargetable with less mess, used only for targetOnly
	 * @param ent The unit's trait list
	 */
	public boolean targetable(Entity ent) {
		if (isBase) return true;
		boolean antiTrait = targetTraited(ent.traits);

		for (int j = 0; j < traits.size(); j++) {
			Trait tr = traits.get(j);
			if (ent.traits.contains(tr) || (antiTrait && tr.targetType) || (ent.dire == -1 && tr.others.contains(((MaskUnit)ent.data).getPack())))
				return true;
		}
		antiTrait = targetTraited(traits);
		if (dire == -1)
			for (int j = 0; j < ent.traits.size(); j++) {
				Trait tr = ent.traits.get(j);
				if ((antiTrait && tr.targetType) || tr.others.contains(((MaskUnit)data).getPack()))
					return true;
			}
		return false;
	}
	/**
	 * A more dedicate ctargetable used solely for active procs
	 * @param atk The attack in question
	 * @return true if the unit can receive procs
	 */
	public boolean btargetable(AttackAb atk) {
		if ((receive(1) || atk.dire == 1) && atk.matk.getATKTraits().isEmpty())
			return true; //Ignore traits if: Enemy Attacks Enemy, Enemy Attacks Unit, Unit Attacks Unit, and no traits are set for the attack
		else if (receive(1) && (status.curse > 0 || status.seal > 0))
			for (int j = 0; j < atk.trait.size(); j++)
				if (data.getTraits().contains(atk.trait.get(j)) || (dire == -1 && atk.trait.get(j).others.contains(((MaskUnit)data).getPack())))
					return true; //Cursed units lack traits, this "re-adds" them for enemies that consider traits debuff
		return ctargetable(atk.trait, atk.attacker); //Go to normal if no specialties apply
	}
	/**
	 * can be targeted by units that have traits in common with the entity they're attacking
	 * @param t The attack's trait list
	 * @param attacker The Entity attacking.
	 */
	@Override
	public boolean ctargetable(SortedPackSet<Trait> t, Entity attacker) {
		if (attacker != null) {
			if (attacker.dire == -1 && !attacker.traits.isEmpty()) {
				for (int i = 0; i < traits.size(); i++) {
					if (traits.get(i).BCTrait())
						continue;
					if (traits.get(i).others.contains(((MaskUnit) attacker.data).getPack()))
						return true;
				}
			} else if (dire == -1 && !traits.isEmpty()) {
				for (int i = 0; i < attacker.traits.size(); i++) {
					if (attacker.traits.get(i).BCTrait())
						continue;
					if (attacker.traits.get(i).others.contains(((MaskUnit) data).getPack()))
						return true;
				}
			}
		}
		if (targetTraited(t))
			for (int i = 0; i < traits.size(); i++)
				if (traits.get(i).targetType)
					return true;
		if (targetTraited(traits))
			for (int i = 0; i < t.size(); i++)
				if (t.get(i).targetType)
					return true;
		for (int j = 0; j < t.size(); j++)
			if (traits.contains(t.get(j)))
				return true;
		return t.contains(BCTraits.get(TRAIT_TOT));
	}

	/**
	 * Check if the unit can be considered an anti-traited
	 * @param targets The list of traits the unit targets
	 * @return true if the unit is anti-traited
	 */
	public static boolean targetTraited(SortedPackSet<Trait> targets) {
		SortedPackSet<Trait> temp = new SortedPackSet<>(BCTraits.subList(TRAIT_RED,TRAIT_WHITE));
		temp.remove(TRAIT_METAL);
		return targets.containsAll(temp);
	}

	/**
	 * get touch mode bitmask
	 */
	@Override
	public int touchable() {
		int n = (getAbi() & AB_GHOST) > 0 ? TCH_EX : TCH_N;
		int ex = getProc().REVIVE.revive_others ? TCH_ZOMBX : 0;
		if (kbTime == -1)
			return TCH_SOUL | ex;
		if (status.revs[1] >= REVIVE_SHOW_TIME && anim.corpse != null && anim.corpse.type != ZombieEff.BACK)
			return TCH_CORPSE | ex;
		if (status.burs[1] > 0)
			return n | TCH_UG | ex;
		if (kbTime == -3)
			return TCH_UG | ex;
		if (anim.anim.type == AnimU.TYPEDEF[AnimU.ENTRY])
			return TCH_ENTER | ex;
		return (kbTime == 0 ? n : TCH_KB) | ex;
	}

	/**
	 * Updates entity values that must be updated before calling update
	 */
	@Override
	public void preUpdate() {
		// if this entity is in kb state, do kbmove()
		if (kbTime > 0)
			kb.updateKB();
		if (kbTime == 0 && status.revs[1] == 0 && !killCounted) { // if this entity has auras and is not on HB, set them to all nearby units
			Proc.AURA aura = getProc().WEAKAURA;
			for (int i = 0; i < 2; i++) {
				if (aura.exists()) {
					int dir = i == 0 ? getDire() : -getDire();
					List<AbEntity> le = basis.inRange(getTouch(), dir, pos + (aura.min_dis * getDire()), pos + (aura.max_dis * getDire()), false);
					if (getProc().AI.ignHypno)
						le.removeIf(e -> e instanceof Entity && ((Entity)e).status.hypno > 0);
					if (aura.skip_self)
						le.remove(this);
					if (dir == 1 || basis.getBase(-1) instanceof ECastle)
						le.remove(basis.getBase(dir));
					for (AbEntity abE : le) {
						Entity e = (Entity) abE;
						if (aura.trait || e.targetable(this))
							e.auras.setAuras(aura, i == 0);
					}
				}
				aura = getProc().STRONGAURA;
			}
		}

		// update proc effects
		status.update();
		pois.update();
		barrier.update();

		// do move check if available, move if possible
		boolean nstop = status.stop[0] == 0 || status.stop[1] != 0;
		boolean canAct = kbTime == 0 && anim.anim.type != AnimU.TYPEDEF[AnimU.ENTRY];
		if (canAct && atkm.atkTime == 0 && status.revs[1] == 0) {
			checkTouch();

			if (!touch && nstop && health > 0) {
				double mov = updateMove(0);
				if (mov != 0) {
					if (mov > 0 || getAnim().anim().getEAnim(AnimU.TYPEDEF[AnimU.RETREAT]).unusable())
						anim.setAnim(AnimU.TYPEDEF[AnimU.WALK], true);
					else
						anim.setAnim(AnimU.TYPEDEF[AnimU.RETREAT], true);
				} else
					anim.setAnim(AnimU.TYPEDEF[AnimU.IDLE], true);
			}
		} else if (anim.anim.type == AnimU.TYPEDEF[AnimU.ENTRY])
			for (int i = spInd; i < data.getEntry().length; i++)
				if (anim.anim.f >= data.getEntry()[i].pre) {
					spInd++;
					basis.getAttack(aam.getSpAttack(ENTR, i));
				}
	}

	/**
	 * update the entity. order of update:
	 *  1st iteration (movement) :   TBA, procs time tick -> move (KB, burrow, standard) -> revive
	 *  2nd iteration (reactions):   validate walking OR go idle, start burrow, start attack -> update attack
	 */
	@Override
	public void update() {
		if(hit > 0)
			hit--;

		auras.updateAuras();
		// being frozen doesn't invalidate neither reactions nor walking readiness:
		// entities still change animation while frozen based on collisions
		// entities move in the same frame freeze proc ends
		boolean nstop = status.stop[0] == 0 || status.stop[1] != 0;
		boolean canAct = kbTime == 0 && anim.anim.type != AnimU.TYPEDEF[AnimU.ENTRY];
		// do move check if available, move if possible
		double tba = getEffectiveTBA();

		// update revive status, save acted
		zx.updateRevive();
		canAct &= status.revs[1] == 0;
		// check touch after KB or move
		checkTouch();
		// update burrow state if not stopped
		if (nstop && !skipSpawnBurrow && status.revs[1] == 0) {
			updateBurrow();
			canAct &= kbTime == 0;
		}

		if (tba > 0)
			waitTime = waitTime - getTime();
		boolean canAttack = canAct && (!isBase || !(data.getSpeed() == 0 && data.getRange() == 0 && data.allAtk(0) == 0));
		// update wait and attack state
		if (canAttack) {
			// if it can attack, setup attack state
			if (touchEnemy && atkm.attacksLeft != 0 && nstop && tba + atkm.atkTime == 0 && !(isBase && health <= 0))
				atkm.setUp();
			else if ((tba >= 0 || !touchEnemy) && touch && atkm.atkTime == 0 && !(isBase && health <= 0)) { // update waiting state
				double mov = getProc().AI.retreatDist > 0 ? getMov(0) : 0;
				if (anim.negSpeed) {
					pos += (float)(mov * getDire());
					if (getAnim().anim().getEAnim(AnimU.TYPEDEF[AnimU.RETREAT]).unusable())
						anim.setAnim(AnimU.TYPEDEF[AnimU.WALK], true);
					else
						anim.setAnim(AnimU.TYPEDEF[AnimU.RETREAT], true);
				} else
					anim.setAnim(AnimU.TYPEDEF[AnimU.IDLE], true);
			}
		}

		if (atkm.atkTime > 0 && nstop)
			atkm.updateAttack();
		else
			checkTouch();

		anim.update();
		bondTree.update();
	}

	/**
	 * Gets TBA with lethargy and aura calcs
	 * @return Effective TBA
	 */
	private double getEffectiveTBA() {
		double tba = waitTime + auras.getTbaAura() + status.getLethargy();
		return Math.max(0, tba);
	}

	@Override
	public void updateAnimation() {
		//update animation
		anim.updateAnimation();
	}

	/**
	 * Add multipliers from crit and savage blow, process metal trait damage reduction too
	 * @param isMetal If metal ability effects apply to entity
	 * @param ans Effective Damage modified by other procs/abilities
	 * @param atk Non-Damage attack data
	 * @return Damage with crit and SBlow and metal procession
	 */
	protected int critCalc(boolean isMetal, int ans, AttackAb atk) {
		double satk = atk.getProc().SATK.mult;
		if (satk > 0)
			ans = (int) (ans * (100 + satk) * 0.01);
		double crit = atk.getProc().CRIT.mult;
		double criti = getProc().CRITI.mult;
		if (criti == 100)
			crit = 0;
		else if (criti != 0)
			crit = crit * (100 - getProc().CRITI.mult) / 100.0;
		if (isMetal)
			if (crit > 0)
				ans = (int) (ans * 0.01 * crit);
			else if (crit < 0)
				ans = (int) Math.max(1, health * crit / -100);
			else
				ans = ans > 0 ? 1 : 0;
		else if (crit > 0)
			ans = (int) (ans * 0.01 * crit);
		else if (crit < 0)
			ans = (int) Math.max(1, health / 1000);
		return ans;
	}

	/**
	 * determine the amount of damage received from this attack
	 */
	protected int getDamage(AttackAb atk, int ans) {
		if (isBase)
			ans *= 1 + atk.getProc().ATKBASE.mult / 100.0;
		return ans;
	}

	/**
	 * Damage score prediction for AI attack selection. Used to
	 * @param dmg Effective damage
	 * @param e The attacked entity
	 * @param matk The Attack Data
	 * @return Damage score
	 */
	@Override
	public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
		if ((e.getAbi() & AB_ONLY) > 0 && !targetable(e))
			return 0;
		float ans = 1;
		if (status.curse == 0 && (matk.getDire() == -1 || receive(-1)) || ctargetable(matk.getATKTraits(), e)) {
			ans *= (100 - getProc().IMUATK.prob) / 100f;
			Proc.DMGCUT dmgcut = getProc().DMGCUT;
			if (dmgcut.prob > 0 && dmg < status.dcut && dmg > 0) {
				if (dmgcut.prob == 100) {
					if (dmgcut.reduction == 100)
						return 0;
					ans *= (100 - dmgcut.reduction) / 100f;
				} else
					ans *= (100 - (1f * dmgcut.reduction / dmgcut.prob)) / 100f;
			}
			Proc.DMGCAP dmgcap = getProc().DMGCAP;
			if (dmgcap.prob > 0 && dmg > status.dcap) {
				if (dmgcap.nullify) {
					return 0;
				} else
					ans = 1f * dmg / status.dcap;
			}
		}
		Proc.REMOTESHIELD remote = getProc().REMOTESHIELD;
		double stRange = Math.abs(e.pos - pos);
		if (remote.prob > 0 && remote.reduction + remote.block != 0 && ((!remote.traitCon && status.curse == 0) || ctargetable(matk.getATKTraits(), e))
			&& stRange >= remote.minrange && stRange <= remote.maxrange) {
			if (remote.reduction == 100 || remote.block == 100) {
				if (remote.prob == 100)
					return 0;
				ans *= (100 - remote.prob) / 100f;
			} else if (remote.prob == 100)
				ans *= (100 - remote.reduction) * (100 - remote.block) / 100f;
			else
				ans *= (100 - (1f * remote.reduction / remote.prob)) * (100 - (1f * remote.block / remote.prob)) / 100f;
		}
		if (isBase)
			ans *= 1 + matk.getProc().ATKBASE.mult / 100.0;
		if (barrier.health != 0) {
			if (matk.getProc().BREAK.prob > 0) {
				ans *= matk.getProc().BREAK.prob / 100f;
				if (dmg >= barrier.health) {
					return ans * barrier.health / dmg;
				}
			} else if (dmg >= barrier.health) {
				return 1f * barrier.health / dmg;
			} else {
				return 0;
			}
		}

		if (dire != e.dire) {
			SortedPackSet<Trait> sharedTraits = traits.inCommon(matk.getATKTraits());
			boolean isAntiTraited = targetTraited(matk.getATKTraits());
			sharedTraits.addIf(traits, t -> !t.BCTrait() && ((t.targetType && isAntiTraited) || t.others.contains((e.dire == -1 ? e : this).data.getPack())));//Ignore the warning, condition dictates unit

			if (!sharedTraits.isEmpty()) {
				if (e.status.curse == 0 && e.getProc().DMGINC.mult != 0)
					ans *= e.getProc().DMGINC.mult/100.0;
				if (status.curse == 0 && getProc().DEFINC.mult != 0)
					ans /= e.getProc().DEFINC.mult/100.0;
			}
		}

		return ans;
	}

	/**
	 * called when entity starts final hb, no revive, no lethal strike
	 */
	protected void onLastBreathe() {
		killCounted = true;
	}

	/**
	 * get max distance to go back
	 */
	protected abstract float getLim();

	/**
	 * Used to determine the faction of the entity (and apply hypno proc), which affects target entities, and moving direction.
	 * @return The facing direction of the entity
	 */
	public int getDire() {
		if (status.hypno > 0)
			return -dire;
		return dire;
	}

	/**
	 * Buffs level for summon
	 * @param lv total to buff
	 */
	public abstract double buff(int lv);

	/**
	 * move forward <br>
	 * @param extmov: distance to add to this movement
	 * @return distance moved
	 */
	protected float updateMove(float extmov) {
		float mov = getMov(extmov);
		pos += mov * getDire();
		return mov;
	}

	/**
	 * Gets distance needed to move forward, if it can. Also flips animation if speed is negative.
	 * @param extmov Distance added to movement speed
	 * @return Unit speed altered by battle factors
	 */
	protected float getMov(float extmov) {
		float mov = getSpeed(data.getSpeed(), extmov);
		if (mov > 0 && (getProc().AI.danger || getProc().AI.retreatDist > 0))
			mov = AIMove(mov);

		anim.negSpeed = mov < 0;
		if (basis.speedLimit(dire == 1) != -1)
			mov = Math.min(mov, basis.speedLimit(dire == 1) * 0.5f);
		return mov * getTimeFreeze();
	}

	/**
	 * Get speed after applying Slow/Haste, the extmov parameter, and auras
	 * @param spd raw speed
	 * @param extmov additional distance
	 * @return Effective Speed
	 */
	public float getSpeed(int spd, float extmov) {
		float mov = status.slow > 0 ? 0.25f : spd * 0.5f;
		if (!status.speeds.isEmpty() && status.slow == 0)
			mov = status.getSpeed(mov);

		if (status.adrenaline != 100) {
			mov *= status.adrenaline / 100f;
			mov = (float) Math.round(mov * 4f) / 4f;
		}
		mov += extmov;
		mov *= auras.getSpdAura();
		return mov;
	}

	/**
	 * Used for AI evasion. Refrain from moving or move backwards if danger is near.
	 * @param mov Moving distance
	 * @return Moving speed, 0 or negative depending on AI-Proc settings
	 */
	private float AIMove(float mov) {
		float mv = mov, predictedPos = pos + mv * getDire();

		for (Entity e : basis.le) {
			if (e.getDire() == getDire())
				continue;
			if ((getTouch() & e.touchable()) > 0)
				if (Math.abs(predictedPos - e.pos) <= getProc().AI.retreatDist) {
					mv = (Math.abs(pos - e.pos) < getProc().AI.retreatDist) ? getSpeed(-getProc().AI.retreatSpeed, 0) : 0;
					break;
				}
			if (!getProc().AI.danger || e.atkm.atkTime == 0 || e.aam.getAtk(e.atkm.preID, getTouch()) < 0)
				continue;
			float[] ds = e.aam.inRange(e.atkm.preID);
			float sta = Math.min(ds[0], ds[1]), end = Math.max(ds[0], ds[1]);
			if (pos < sta || predictedPos < sta || predictedPos > end) //Is already on blindspot if there is one, or doesn't run into risk of receiving an attack if it moves
				continue;
			if (e.data.getAtkModel(e.aam.atkType, e.atkm.preID).isLD() && sta >= data.getRange() && predictedPos * e.atkm.preTime < sta)
				continue; //Calculate if unit is fast enough to make it to blindspot before attacking

			if (pos > end)
				mv = 0;
			else
				mv = getSpeed(-getProc().AI.retreatSpeed, 0);
			break;
		}
		return mv;
	}

	/**
	 * Draw the hitboxes for the unit and its attacks
	 * @param gra Canvas
	 * @param p Position
	 * @param siz Zoom Size
	 */
	private void drawAxis(FakeGraphics gra, P p, float siz) {
		// after this is the drawing of hit boxes
		siz *= 1.25f;
		float rat = BattleConst.ratio;
		float poa = p.x - pos * rat * siz;
		int py = (int) p.y;
		int h = (int) (640 * rat * siz);
		gra.setColor(FakeGraphics.RED);
		for (int i = 0; i < data.getAtkCount(aam.atkType); i++) {
			float[] ds = aam.inRange(i);
			float d0 = Math.min(ds[0], ds[1]);
			float ra = Math.abs(ds[0] - ds[1]);
			int x = (int) (d0 * rat * siz + poa);
			int y = (int) (p.y + 100 * i * rat * siz);
			int w = (int) (ra * rat * siz);
			if (atkm.tempAtk == i)
				gra.fillRect(x, y, w, h);
			else
				gra.drawRect(x, y, w, h);
		}
		gra.setColor(FakeGraphics.YELLOW);
		int x = (int) ((pos + (status.hypno == 0 ? 0 : data.getWidth()) + data.getRange() * getDire()) * rat * siz + poa);
		gra.drawLine(x, py, x, py + h);
		gra.setColor(FakeGraphics.BLUE);
		int bx = (int) ((dire == -1 ? pos : pos - data.getWidth()) * rat * siz + poa);
		int bw = (int) (data.getWidth() * rat * siz);
		gra.drawRect(bx, (int) p.y, bw, h);
		gra.setColor(FakeGraphics.CYAN);
		gra.drawLine((int) (pos * rat * siz + poa), py, (int) (pos * rat * siz + poa), py + h);
	}

	/**
	 * get the extra proc time due to fruits, for EEnemy only
	 */
	public float getFruit(SortedPackSet<Trait> trait, int dire, int e) {
		if (!receive(dire) || receive(e))
			return 0;
		SortedPackSet<Trait> sharedTraits = trait.inCommon(traits);
		return basis.b.t().getFruit(sharedTraits);
	}

	/**
	 * called when last KB reached
	 */
	private void preKill() {
		Soul s = Identifier.get(data.getDeathAnim());
		if (s != null && s.audio != null)
			CommonStatic.setSE(s.audio);
		else
			CommonStatic.setSE(basis.r.irFloat() < 0.5f ? SE_DEATH_0 : SE_DEATH_1);

		if (zx.prekill())
			return;

		kill(false);
	}

	/**
	 * determines atk direction for procs and abilities
	 */
	private boolean receive(int dire) {
		return this.dire != dire;
	}

	private void updateBurrow() {
		if (kbTime == 0 && touch && status.burs[0] != 0) {
			float bpos = basis.getBase(getDire()).pos;
			boolean ntbs = (bpos - pos) * getDire() > data.touchBase();
			if (ntbs) {
				// setup burrow state
				status.burs[0]--;
				status.burs[1] = anim.setAnim(AnimU.TYPEDEF[AnimU.BURROW_DOWN], true);
				kbTime = -2;
			}
		}
		if (kbTime == -2) {
			// burrow down
			status.burs[1] -= getTimeFreeze();
			for (int i = spInd; i < data.getGouge().length; i++)
				if (anim.anim.len() - status.burs[1] >= data.getGouge()[i].pre) {
					spInd++;
					basis.getAttack(aam.getSpAttack(BUR, i));
				}
			if (status.burs[1] == 0) {
				kbTime = -3;
				anim.setAnim(AnimU.TYPEDEF[AnimU.UNDERGROUND], true);
				bdist = proc.BURROW.dis;
			}
		}
		if (kbTime == -3) {
			// move underground
			float oripos = pos;
			updateMove(0);
			bdist -= (pos - oripos) * getDire();
			if (bdist < 0 || (basis.getBase(getDire()).pos - pos) * getDire() - data.touchBase() <= 0) {
				bdist = 0;
				kbTime = -4;
				status.burs[1] = anim.setAnim(AnimU.TYPEDEF[AnimU.BURROW_UP], true) - 2;
			}
		}
		if (kbTime == -4) {
			// burrow up
			status.burs[1] -= getTimeFreeze();
			for (int i = spInd; i < data.getResurface().length; i++)
				if (anim.anim.len() - status.burs[1] - 2 >= data.getResurface()[i].pre) {
					spInd++;
					basis.getAttack(aam.getSpAttack(RESU, i));
				}
			if (status.burs[1] <= 0) {
				kbTime = 0;
				skipSpawnBurrow = status.burs[0] == 0;
			}
		}

	}

	/**
	 * get touch state, which is used to determine which state of entities will this one detect
	 */
	public int getTouch() {
		if ((getAbi() & AB_CKILL) > 0)
			return data.getTouch() | TCH_CORPSE;
		return data.getTouch();
	}

	/**
	 * detect nearby entities
	 */
	public boolean checkTouch() {
		touch = true;
		float[] ds = aam.touchRange();
		List<AbEntity> le = basis.inRange(getTouch(), getDire(), ds[0], ds[1], false);
		if (status.hypno > 0)
			le.remove(this);
		if (getProc().AI.ignHypno)
			le.removeIf(e -> e instanceof Entity && ((Entity)e).status.hypno > 0);

		float bpos = basis.getBase(getDire()).pos;
		float poss = status.hypno == 0 ? pos : pos + (data.getWidth() * -dire);
		boolean blds = (bpos - poss) * getDire() > data.touchBase();
		if (blds)
			le.remove(basis.getBase(getDire()));
		if (!le.contains(basis.getBase(getDire()))) {
			if (poss * getDire() >= bpos)
				le.add(basis.getBase(getDire()));
			blds &= le.isEmpty();
		}

		if (blds)
			touch = false;
		touchEnemy = touch;
		if ((getAbi() & AB_ONLY) > 0) {
			touchEnemy = false;
			for (AbEntity abE : le)
				if (abE.targetable(this)) {
					touchEnemy = true;
					break;
				}
		}
		return touch;
	}

	/**
	 * Get anim. Used only for Door
	 */
	public EAnimU getAnim() {
		return anim.anim;
	}

	public int getLayer() {
		if (anim.deathSurge == 0 && anim.dead >= 0)
			return 0;
		return layer;
	}

	protected boolean notAttacking() {
		return atkm.atkTime == 0;
	}

	private float getTime() {
		if ((getAbi() & AB_TIMEI) == 0)
			return basis.timeFlow >= 1 ? 1 : basis.timeFlow;
		return basis.timeFlow <= 1 ? 1 : basis.timeFlow;
	}
	public float getTimeFreeze() {
		float base = status.stop[0] != 0 && status.stop[1] != 0 ? (float)status.stop[1] : 1;
		return getTime() * base;
	}

	@Override
	public void added(int dire, float pos) {
		super.added(dire, pos);
		int spwn = basis.spawns.getOrDefault(data.getPack(), 0) + 1;
		basis.spawns.put(data.getPack(), spwn);
		if (spwn == 1)//first spawned
			basis.dmgStatistics.put(data.getPack(), new long[2]);

		if (getProc().DEATHSURGE.prob > 0 && spwn % getProc().DEATHSURGE.spawns != 0)
			getProc().DEATHSURGE.clear();
		if (getProc().MINIDEATHSURGE.prob > 0 && spwn % getProc().MINIDEATHSURGE.spawns != 0)
			getProc().MINIDEATHSURGE.clear();
		if (getProc().REFUND.prob > 0 && spwn % getProc().REFUND.count != 0)
			getProc().REFUND.clear();
	}
	@Override
	public int compareTo(@NotNull Entity ent) {
		return Integer.compare(layer, ent.layer);
	}
}