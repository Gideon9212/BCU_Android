package common.battle.attack;

import common.CommonStatic;
import common.battle.data.MaskAtk;
import common.battle.entity.AbEntity;
import common.battle.entity.Entity;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.BattleObj;
import common.util.stage.Music;
import common.util.unit.Trait;
import common.util.Data.Proc.SPEED;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;

public abstract class AttackAb extends BattleObj {

	public final int abi;
	public int atk;
	public final SortedPackSet<Trait> trait;
	public final AtkModelAb model;
	public final AttackAb origin;
	public final MaskAtk matk;
	public final Entity attacker;
	public final int layer;
	public final boolean isLongAtk;
	public int duration;
	public boolean excludeRightEdge = false, isCounter = false;

	public int touch = TCH_N, dire, canon = -2, waveType = 0;
	private boolean playedSound = false;

	protected final Proc proc;
	public final HashSet<Proc.REMOTESHIELD> r = new HashSet<>();
	protected final ArrayList<AbEntity> capt = new ArrayList<>();
	protected float sta, end;

	protected AttackAb(Entity attacker, AtkModelAb ent, int ATK, SortedPackSet<Trait> tr, int eab, Proc pro, float p0, float p1, MaskAtk matk, int layer, boolean isLongAtk, int time) {
		this.attacker = attacker;
		dire = ent.getDire();
		origin = this;
		model = ent;
		trait = matk != null && !matk.getATKTraits().isEmpty() ? matk.getATKTraits() : tr;
		atk = ATK;
		proc = pro;
		abi = eab;
		sta = p0;
		end = p1;
		duration = time;
		this.matk = matk;
		this.layer = layer;
		this.isLongAtk = isLongAtk;
	}

	protected AttackAb(Entity attacker, AttackAb a, float STA, float END, boolean isLongAtk) {
		this.attacker = attacker;
		dire = a.dire;
		origin = a.origin;
		model = a.model;
		atk = a.atk;
		abi = a.abi;
		trait = a.trait;
		proc = a.proc;
		touch = a.touch;
		canon = a.canon;
		sta = STA;
		end = END;
		duration = 1;
		matk = a.matk;
		layer = a.layer;
		isCounter = a.isCounter;
		this.isLongAtk = isLongAtk;
	}

	/**
	 * capture the entities
	 */
	public abstract void capture();

	/**
	 * apply this attack to the entities captured
	 */
	public abstract void excuse();

	public Proc getProc() {
		return proc;
	}

	protected void process() {
		duration--;
		playedSound = false;
		final ArrayList<AbEntity> uncapt = new ArrayList<>(capt.size());
		for (AbEntity ae : capt) {
			if (ae instanceof Entity) {
				Entity e = (Entity) ae;
				Proc imus = e.getProc();
				float f = e.getFruit(trait, dire, 1);
				float time = origin instanceof AttackCanon ? 1 : 1 + f * 0.2f / 3;
				boolean blocked = false;
				if (proc.KB.dis > 0 && imus.IMUKB.block != 0) {
					if (imus.IMUKB.block > 0)
						blocked = true;
					if (imus.IMUKB.block == 100) {
						if (imus.IMUKB.mult < 0)
							e.knockback(this, f);
						proc.KB.clear();
					} else
						proc.KB.dis = (int)(proc.KB.dis * (100 - imus.IMUKB.block) / 100.0);
				}
				if (proc.SLOW.time > 0 && imus.IMUSLOW.block != 0) {
					if (imus.IMUSLOW.block > 0)
						blocked = true;
					if (imus.IMUSLOW.block == 100) {
						if (imus.IMUSLOW.mult < 0)
							e.slow(this, time);
						proc.SLOW.clear();
					} else
						proc.SLOW.time = (int)(proc.SLOW.time * (100 - imus.IMUSLOW.block) / 100.0);
				}
				if (proc.STOP.time > 0 && imus.IMUSTOP.block != 0) {
					if (imus.IMUSTOP.block > 0)
						blocked = true;
					if (imus.IMUSTOP.block == 100) {
						if (imus.IMUSTOP.mult < 0)
							e.freeze(this, time);
						proc.STOP.clear();
					} else
						proc.STOP.time = (int)(proc.STOP.time * (100 - imus.IMUSTOP.block) / 100.0);
				}
				if (proc.WEAK.time > 0 && Entity.checkAIImmunity(proc.WEAK.mult - 100,imus.IMUWEAK.focus, imus.IMUWEAK.block > 0)) {
					if (imus.IMUWEAK.block > 0)
						blocked = true;
					if (imus.IMUWEAK.block == 100) {
						if (imus.IMUWEAK.mult < 0)
							e.weaken(this, time);
						proc.WEAK.clear();
					} else
						proc.WEAK.time = (int)(proc.WEAK.time * (100 - imus.IMUWEAK.block) / 100.0);
				}
				if (proc.LETHARGY.time > 0 && Entity.checkAIImmunity(proc.LETHARGY.mult,imus.IMULETHARGY.focus, imus.IMULETHARGY.block > 0)) {
					if (imus.IMULETHARGY.block > 0)
						blocked = true;
					if (imus.IMULETHARGY.block == 100) {
						if (imus.IMULETHARGY.mult < 0)
							e.lethargy(this, time);
						proc.LETHARGY.clear();
					} else
						proc.LETHARGY.time = (int)(proc.LETHARGY.time * (100 - imus.IMULETHARGY.block) / 100.0);
				}
				if (proc.WARP.prob > 0 && imus.IMUWARP.block != 0) {
					if (imus.IMUWARP.block > 0)
						blocked = true;
					if (imus.IMUWARP.block == 100) {
						if (imus.IMUWARP.mult < 0)
							e.warp(this);
						proc.WARP.clear();
					} else
						proc.WARP.time = (int)(proc.WARP.time * (100 - imus.IMUWARP.block) / 100.0);
				}
				if (proc.CURSE.time > 0 && imus.IMUCURSE.block != 0) {
					if (imus.IMUCURSE.block > 0)
						blocked = true;
					if (imus.IMUCURSE.block == 100) {
						if (imus.IMUCURSE.mult < 0)
							e.curse(this, time);
						proc.CURSE.clear();
					} else
						proc.CURSE.time = (int)(proc.CURSE.time * (100 - imus.IMUCURSE.block) / 100.0);
				}
				if (proc.POISON.damage != 0 && imus.IMUPOI.block != 0 && Entity.checkAIImmunity(proc.POISON.damage, imus.IMUPOI.focus, imus.IMUPOI.block < 0)) {
					if (imus.IMUPOI.block > 0)
						blocked = true;
					if (imus.IMUPOI.block == 100) {
						if (imus.IMUPOI.mult < 0)
							e.poison(this);
						proc.POISON.clear();
					} else
						proc.POISON.damage = (int)(proc.POISON.damage * (100 - imus.IMUPOI.block) / 100.0);
				}
				if (proc.SEAL.time > 0 && imus.IMUSEAL.block != 0) {
					if (imus.IMUSEAL.block > 0)
						blocked = true;
					if (imus.IMUSEAL.block == 100) {
						if (imus.IMUSEAL.mult < 0)
							e.seal(this, time);
						proc.SEAL.clear();
					} else
						proc.SEAL.time = (int)(proc.SEAL.time * (100 - imus.IMUSEAL.block) / 100.0);
				}
				if (proc.RAGE.time > 0 && imus.IMURAGE.block != 0) {
					if (imus.IMURAGE.block > 0)
						blocked = true;
					if (imus.IMURAGE.block == 100) {
						if (imus.IMURAGE.mult < 0)
							e.enrage(this, time);
						proc.RAGE.clear();
					} else
						proc.RAGE.time = (int)(proc.RAGE.time * (100 - imus.IMURAGE.block) / 100.0);
				}
				if (proc.HYPNO.time > 0 && imus.IMUHYPNO.block != 0) {
					if (imus.IMUHYPNO.block > 0)
						blocked = true;
					if (imus.IMUHYPNO.block == 100) {
						if (imus.IMUHYPNO.mult < 0)
							e.hypnotize(this, time);
						proc.HYPNO.clear();
					} else
						proc.HYPNO.time = (int)(proc.HYPNO.time * (100 - imus.IMUHYPNO.block) / 100.0);
				}
				if (proc.ARMOR.time > 0 && imus.IMUARMOR.block != 0 && Entity.checkAIImmunity(proc.ARMOR.mult, imus.IMUARMOR.focus, imus.IMUARMOR.block < 0)) {
					if (imus.IMUARMOR.block > 0)
						blocked = true;
					if (imus.IMUARMOR.block == 100) {
						if (!e.isBase() && imus.IMUARMOR.mult < 0)
							e.breakArmor(this, time);
						proc.ARMOR.clear();
					} else
						proc.ARMOR.time = (int)(proc.ARMOR.time * (100 - imus.IMUARMOR.block) / 100.0);
				}
				if (proc.SPEED.time > 0 && imus.IMUSPEED.block != 0) {
					boolean b;
					if (proc.SPEED.type != SPEED.TYPE.SET)
						b = imus.IMUSPEED.block < 0;
					else
						b = (e.data.getSpeed() > proc.SPEED.speed && imus.IMUSPEED.block > 0) || (e.data.getSpeed() < proc.SPEED.speed && imus.IMUSPEED.block < 0);

					if (Entity.checkAIImmunity(proc.SPEED.speed, imus.IMUSPEED.focus, b)) {
						if (imus.IMUSPEED.block > 0)
							blocked = true;
						if (imus.IMUSPEED.block == 100) {
							if (imus.IMUSPEED.mult < 0)
								e.hasten(this, time);
							proc.SPEED.clear();
						} else
							proc.SPEED.time = (int)(proc.SPEED.time * (100 - imus.IMUSPEED.block) / 100.0);
					}
				}
				if (handleMisc(e))
					uncapt.add(e);
				if (proc.POIATK.mult != 0 && imus.IMUPOIATK.block != 0) {
					if (imus.IMUPOIATK.block > 0)
						blocked = true;
					if (imus.IMUPOIATK.block == 100)
						proc.POIATK.clear();
					else
						proc.POIATK.mult *= (100 - imus.IMUPOIATK.block) / 100.0;
				}
				if (proc.SUMMON.mult > 0 && imus.IMUSUMMON.block != 0) {
					if (imus.IMUSUMMON.block > 0)
						blocked = true;
					if (imus.IMUSUMMON.block == 100)
						proc.SUMMON.clear();
					else
						proc.SUMMON.mult = (int)(proc.SUMMON.mult * (100 - imus.IMUSUMMON.block) / 100.0);
				}
				if (proc.CRIT.mult > 0 && imus.CRITI.block != 0) {
					if (imus.CRITI.block > 0)
						blocked = true;
					if (imus.CRITI.block == 100)
						proc.CRIT.clear();
					else
						proc.CRIT.mult *= (100 - imus.CRITI.block) / 100.0;
				}

				if (blocked)
					e.anim.getEff(STPWAVE);
			}
		}
		capt.removeAll(uncapt);
	}
	private boolean handleMisc(Entity e) {//Does toxic/crit/etc atk prior to their removal
		Proc imus = e.getProc();
		int atkd = 0;
		if (proc.POIATK.mult != 0 && imus.IMUPOIATK.block == 100 && imus.IMUPOIATK.mult < 0) {
			imus.IMUPOIATK.mult += 100;
			atkd |= 1;
		}
		if (proc.SUMMON.mult > 0 && imus.IMUSUMMON.block == 100 && imus.IMUSUMMON.mult < 0) {
			imus.IMUSUMMON.mult += 100;
			atkd |= 2;
		}
		if (proc.CRIT.mult > 0 && imus.CRITI.block == 100 && imus.CRITI.mult < 0) {
			imus.CRITI.mult += 100;
			atkd |= 4;
		}
		if (atkd != 0) {
			e.damaged(this);
			if ((atkd & 1) > 0)
				imus.IMUPOIATK.mult -= 100;
			if ((atkd & 2) > 0)
				imus.IMUSUMMON.mult -= 100;
			if ((atkd & 4) > 0)
				imus.CRITI.mult -= 100;
		}
		return atkd != 0;
	}

	/**
	 * Plays the default hit sound. If this attack has a custom sound effect, it is played over the in-game sound effects
	 * @param isBase If attacked entity is base
	 */
	public void playSound(boolean isBase) {
		if (isBase)
			CommonStatic.setSE(SE_HIT_BASE);
		if (isBase || playedSound)
			return;
		playedSound = true;
		Identifier<Music> csfx = matk == null ? null : matk.getAudio();
		if (csfx == null)
			CommonStatic.setSE(Math.random() < 0.5 ? SE_HIT_0 : SE_HIT_1);
		else
			CommonStatic.setSE(csfx);
	}

	public void notifyEntity(Consumer<Entity> notifier) {
		if (attacker != null)
			notifier.accept(attacker);
	}
}