package common.battle;

import common.CommonStatic;
import common.battle.attack.AttackAb;
import common.battle.attack.ContAb;
import common.battle.data.MaskUnit;
import common.battle.entity.*;
import common.pack.Identifier;
import common.util.BattleObj;
import common.util.CopRand;
import common.util.Data;
import common.util.Data.Proc.THEME;
import common.util.anim.AnimU;
import common.util.pack.Background;
import common.util.pack.EffAnim;
import common.util.pack.EffAnim.DefEff;
import common.util.pack.bgeffect.BackgroundEffect;
import common.util.stage.*;
import common.util.stage.MapColc.DefMapColc;
import common.util.unit.*;
import common.util.unit.Character;

import java.util.*;

public class StageBasis extends BattleObj {

	public final BasisLU b;
	public final Stage st;
	public final EStage est;
	public final ELineUp elu;
	public final HashMap<Character, Integer> spawns = new HashMap<>();
	public final HashMap<Character, long[]> dmgStatistics = new HashMap<>();
	public final int[] nyc;
	public final boolean[][] locks = new boolean[2][5];
	public final AbEntity ebase, ubase;
	public final Cannon canon;
	public final Sniper sniper;
	public final BattleList<Entity> le = new BattleList<>();
	public final List<EntCont> tempe = new ArrayList<>();
	public final BattleList<ContAb> lw = new BattleList<>();
	public final BattleList<ContAb> tlw = new BattleList<>();
	public final BattleList<EAnimCont> lea = new BattleList<>();
	public final BattleList<DoorCont> doors = new BattleList<>();
	public final List<EAnimCont> ebaseSmoke = new ArrayList<>();
	public final List<EAnimCont> ubaseSmoke = new ArrayList<>();

	public final int conf;
	public final CopRand r;
	final Recorder rx = new Recorder();
	public final boolean isOneLineup;
	public final boolean buttonDelayOn;
	public boolean goingUp = true;
	public byte changeFrame = -1;
	public int changeDivision = -1;
	public float buttonDelay = 0;
	public int[] selectedUnit = {-1, -1};
	public final float boss_spawn;
	public final int[] shakeCoolDown = {0, 0};

	public float siz, shockP = 700f;
	public int work_lv, money, maxMoney, maxCannon, upgradeCost, max_num, pos;
	public int frontLineup = 0;
	public boolean lineupChanging = false;
	public boolean shock = false;
	public int time, tstop;
	public float timeFlow = 1f, ftime, cannon;
	public byte[] shake;
	public byte shakeDuration;
	public float shakeOffset;

	public double respawnTime, unitRespawnTime;
	public Background bg;
	public BackgroundEffect bgEffect;

	/**
	 * Real groundHeight of battle
	 */
	public float midH = -1, battleHeight = -1;
	private final List<AttackAb> la = new ArrayList<>();
	private boolean lethal = false;
	public float themeTime;
	private Identifier<Background> theme = null;
	public Identifier<Music> mus = null;
	private THEME themeType;
	private boolean bgEffectInitialized = false;
	public int baseBarrier = 0, rem_spawns;

	private final int[][] dupeCount = new int[2][5];
	private final float[][] dupeTime = new float[2][5];

	public StageBasis(BattleField bf, EStage stage, BasisLU bas, int cnf, long seed, boolean buttonDelayOn, byte saveMode) {
		b = bas;
		r = new CopRand(seed);
		nyc = bas.nyc;
		est = stage;
		st = est.s;
		elu = new ELineUp(bas.lu, this, saveMode);
		setBackground(st.bg);
		boss_spawn = Identifier.getOr(st.castle, CastleImg.class).boss_spawn;

		EEnemy ee = est.base(this);
		if (ee != null) {
			ebase = ee;
			shock = ee.mark == -2;
			ebase.added(1, shock ? boss_spawn : 700);
		} else {
			ebase = new ECastle(this);
			ebase.added(1, 800);
		}
		EUnit eu = est.ubase(this);
		ubase = eu != null ? eu : new ECastle(this, bas);
		ubase.added(-1, st.len - 800);
		if (st.preset != null && st.preset.baseHealthBoost) {
			ubase.maxH += 20000;
			ubase.health = ubase.maxH;
		}
		est.assign(this);
		est.setBaseBarrier();
		int sttime = 3;
		if (st.getMC() == DefMapColc.getMap("CH")) {
			if (st.getCont().id.id == 9)
				sttime = (int) Math.round(Math.log(est.mul) / Math.log(2));
			if (st.getCont().id.id < 3)
				sttime = st.getCont().id.id;
		}
		int max = est.lim != null ? est.lim.num : 50;
		max_num = max <= 0 ? 50 : max;
		maxCannon = bas.t().CanonTime(sttime, elu.getInc(C_C_SPE));
		le.initCapacity(max_num + st.max);

		int bank = maxBankLimit();
		if (bank > 0) {
			work_lv = 8;
			money = bank * 100;
		} else {
			work_lv = 1 + elu.getInc(C_M_LV);
			money = elu.getInc(C_M_INI) * 100;
		}
		cannon = maxCannon * elu.getInc(C_C_INI) / 100;
		canon = new Cannon(this, nyc);
		conf = cnf;
		if (est.lim != null && est.lim.stageLimit != null && est.lim.stageLimit.coolStart)
			for (int i = 0; i < 2; i++)
				for (int j = 0; j < 5; j++)
					elu.resetCD(i, j);

		if(st.minSpawn <= 0 || st.maxSpawn <= 0)
			respawnTime = 1;
		else if(st.minSpawn == st.maxSpawn)
			respawnTime = st.minSpawn;
		else
			respawnTime = st.minSpawn + (int) ((st.maxSpawn - st.minSpawn) * r.nextFloat());

		respawnTime--;
		if ((conf & 1) > 0)
			work_lv = 8;
		if ((conf & 2) > 0)
			sniper = new Sniper(this, bf);
		else
			sniper = null;
		upgradeCost = bas.t().getLvCost(work_lv);

		boolean oneLine = true;
		for(AbForm f : b.lu.fs[1]) {
			if(f != null) {
				oneLine = false;
				break;
			}
		}

		isOneLineup = oneLine;
		this.buttonDelayOn = buttonDelayOn;
		rem_spawns = est.lim != null && est.lim.stageLimit != null && est.lim.stageLimit.maxUnitSpawn > 0 ? est.lim.stageLimit.maxUnitSpawn : -1;
	}

	/**
	 * returns visual money.
	 */
	public int getMoney() {
		return money / 100;
	}

	/**
	 * returns visual max money
	 */
	public int getMaxMoney() {
		return maxMoney / 100;
	}

	/**
	 * returns visual next level.
	 */
	public int getUpgradeCost() {
		return upgradeCost == -1 ? -1 : upgradeCost / 100;
	}

	public void changeTheme(THEME th) {
		theme = th.id;
		mus = th.mus;
		themeTime = th.time;
		themeType = th;
	}

	public void changeWorkerLv(int lv) {
		if (lv > 0)
			CommonStatic.setSE(SE_P_WORKERLVUP);
		else
			CommonStatic.setSE(SE_P_WORKERLVDOWN);

		work_lv = Math.max(1, work_lv + lv);
		work_lv = Math.min(8, work_lv);

		upgradeCost = b.t().getLvCost(work_lv);
		maxMoney = b.t().getMaxMon(work_lv, elu.getInc(C_M_MAX));
		money = Math.min(money, maxMoney);
	}

	public void changeUnitCooldown(int amount, int slot, int type) {
		if (b.lu.efs[0][0] == null)
			return; //skip if player for some reason didn't bring a lineup
		int totUni = 0;
		while (b.lu.efs[totUni >= 5 ? 1 : 0][totUni % 5] != null && totUni < 10)
			totUni++;
		if (slot == -1 || b.lu.efs[Math.floorDiv(slot, 5)][slot % 5] == null)
			slot = r.nextInt(totUni); //Pick random unit if chosen one isn't there

		if (CDChange(amount, slot / 5, slot % 5, type))
			CommonStatic.setSE(amount < 0 ? SE_P_RESEARCHUP : SE_P_RESEARCHDOWN);
	}
	public void changeUnitsCooldown(int amount, int type) {
		for (byte s = 0; s < 10; s++) {
			byte r = (byte)(s / 5), c = (byte)(s % 5);
			if (b.lu.efs[r][c] == null)
				break;
			if (CDChange(amount, r, c, type))
				CommonStatic.setSE(amount < 0 ? SE_P_RESEARCHUP : SE_P_RESEARCHDOWN);
		}
	}
	private boolean CDChange(int amount, int r, int c, int type) {
		double curC = elu.cool[r][c];
		if (type == 0)
			elu.cool[r][c] += amount;
		else if (type == 1)
			elu.cool[r][c] += elu.maxC[r][c] * (amount / 100.0);
		else
			elu.cool[r][c] = amount;
		elu.cool[r][c] = Math.min(elu.maxC[r][c], elu.cool[r][c]);
		return curC != elu.cool[r][c];
	}

	public void changeBG(Identifier<Background> id) {
		theme = id;
	}

	public int entityCount(int d) {
		int ans = 0;
		if (ebase instanceof EEnemy && d == 1)
			ans += ((EEnemy)ebase).data.getWill() + 1;
		if (d == 1)
			for (DoorCont door : doors)
				ans += door.getWill();

		for (Entity ent : le) {
			if (ent.dire == d && !ent.dead)
				ans += ent.data.getWill() + 1;
		}
		return ans;
	}
	public boolean cantDeploy(int rare, int wp) {
		if (rare != -1 && est.lim.stageLimit != null && est.lim.stageLimit.rarityDeployLimit[rare] > 0) {
			int ans = wp;
			for (Entity ent : le)
				if (ent.dire == -1 && !ent.dead && ((MaskUnit) ent.data).getPack().unit.rarity == rare)
					ans += ent.data.getWill() + 1;
			if (ans >= est.lim.stageLimit.rarityDeployLimit[rare])
				return true;
		}
		return entityCount(-1) + wp >= max_num;
	}

	public int entityCount(int d, int g) {
		int ans = 0;
		for (Entity ent : le)
			if (ent.dire == d && ent.group == g && !ent.dead)
				ans += ent.data.getWill() + 1;
		return ans;
	}

	/**
	 * receive attacks and excuse together, capture targets first
	 */
	public void getAttack(AttackAb a) {
		if (a == null)
			return;
		la.add(a);
	}

	/**
	 * the base that entity with this direction will attack
	 */
	public AbEntity getBase(int dire) {
		return dire == 1 ? ubase : ebase;
	}

	public float getEBHP() {
		return Math.min(100f, 100f * ebase.health / ebase.maxH);
	}

	/**
	 * list of entities in the range d0 ~ d1 that can be touched by entity with given direction and touch mode
	 * entity is picked if d0 <= pos <= d1 when excludeRightEdge is false
	 *                  if d0 <= pos <  d1 when excludeRightEdge is true (currently only used by bblast, TODO: waves should use it)
	 */
	public List<AbEntity> inRange(int touch, int dire, float d0, float d1, boolean excludeRightEdge) {
		float start = Math.min(d0, d1);
		float end = Math.max(d0, d1);
		List<AbEntity> ans = new ArrayList<>();
		if (dire == 0)
			return ans;
		for (int i = 0; i < le.size(); i++)
			if ((dire == 2 || le.get(i).dire * dire == -1) && (le.get(i).touchable() & touch) > 0 &&
				le.get(i).pos >= start && (excludeRightEdge ? le.get(i).pos < end : le.get(i).pos <= end))
				ans.add(le.get(i));
		AbEntity b = dire == 1 ? ubase : ebase;
		if ((b.touchable() & touch) > 0 && (b.pos - d0) * (b.pos - d1) <= 0)
			ans.add(b);
		if (dire == 2 && ((ubase.touchable() & touch) > 0 && (ubase.pos - d0) * (ubase.pos - d1) <= 0))
			ans.add(ubase);

		return ans;
	}

	public void registerBattleDimension(float midH, float battleHeight) {
		this.midH = midH;
		this.battleHeight = battleHeight;
	}

	public void notifyUnitDeath() {
		float percentage = ebase.health * 100f / ebase.maxH;

		for(int i = 0; i < est.killCounter.length; i++) {
			SCDef.Line line = est.s.data.datas[i];

			if(est.killCounter[i] == 0 || line.castle_0 == 0)
				continue;

			if(line.castle_0 == line.castle_1 && percentage <= line.castle_0) {
				est.killCounter[i] -= 1;
			} else if(line.castle_0 != line.castle_1 && percentage >= Math.min(line.castle_0, line.castle_1) && percentage <= Math.max(line.castle_0, line.castle_1)) {
				est.killCounter[i] -= 1;
			}
		}
	}

	public void release() {
		if(bgEffect != null)
			bgEffect.release();
	}

	protected boolean act_can() {
		if(buttonDelay > 0)
			return false;

		if(ubase.health <= 0 || ebase.health <= 0)
			return false;

		if (cannon == maxCannon) {
			if(canon.id == BASE_WALL && cantDeploy(1, 0)) {
				CommonStatic.setSE(SE_SPEND_FAIL);
				return false;
			}

			CommonStatic.setSE(SE_SPEND_SUC);
			canon.activate();
			cannon = 0;
			return true;
		}
		CommonStatic.setSE(SE_SPEND_FAIL);
		return false;
	}

	protected void act_lock(int i, int j) {
		locks[i][j] = !locks[i][j];
	}

	protected boolean act_mon() {
		if(buttonDelay > 0)
			return false;

		if (work_lv < 8 && money > upgradeCost) {
			CommonStatic.setSE(SE_SPEND_SUC);
			money -= upgradeCost;
			work_lv++;
			upgradeCost = b.t().getLvCost(work_lv);
			maxMoney = b.t().getMaxMon(work_lv, elu.getInc(C_M_MAX));
			return true;
		}
		CommonStatic.setSE(SE_SPEND_FAIL);
		return false;
	}

	protected boolean act_sniper() {
		if (sniper != null) {
			sniper.enabled = !sniper.enabled;
			sniper.cancel();
			return true;
		}
		return false;
	}

	protected boolean act_continue() {
		if (!st.non_con && ubase.health <= 0) {
			if (ubase instanceof EUnit)
				((EUnit)ubase).cont();
			ubase.health = ubase.maxH;
			if (getEBHP() <= st.mush)
				CommonStatic.setBGM(st.mus1);
			else
				CommonStatic.setBGM(st.mus0);
			if (work_lv < 8) {
				work_lv = 8;
				upgradeCost = -1;
				maxMoney = b.t().getMaxMon(8, elu.getInc(C_M_MAX));
			}

			money = maxMoney;
			cannon = maxCannon;

			lw.clear();
			tlw.clear();
			for (Entity e : le)
				e.cont();
			for(double[] c : elu.cool)
				Arrays.fill(c, 0);
			return true;
		}
		return false;
	}

	protected boolean act_change_up() {
		if(lineupChanging || isOneLineup || ubase.health == 0)
			return false;
		lineupChanging = true;
		goingUp = true;
		changeFrame = Data.LINEUP_CHANGE_TIME;
		changeDivision = changeFrame / 2;
		return true;
	}

	protected boolean act_change_down() {
		if(lineupChanging || isOneLineup || ubase.health == 0)
			return false;
		lineupChanging = true;
		goingUp = false;
		changeFrame = Data.LINEUP_CHANGE_TIME;
		changeDivision = changeFrame / 2;
		return true;
	}

	protected boolean act_spawn(int i, int j, boolean manual) {
		if (buttonDelay > 0 || ubase.health == 0 || unitRespawnTime > 0 || rem_spawns == 0)
			return false;

		if(buttonDelayOn && manual && selectedUnit[0] == -1) {
			if(elu.price[i][j] != -1 || b.lu.fs[i][j] == null) {
				if (lineupChanging)
					return false;

				buttonDelay = 6;
				selectedUnit[0] = i;
				selectedUnit[1] = j;

				return true;
			}
		}

		if (elu.cool[i][j] > 0 && !elu.validSpirit(i,j)) {
			if(manual)
				CommonStatic.setSE(SE_SPEND_FAIL);
			return false;
		}
		if (elu.price[i][j] == -1 || elu.price[i][j] == -2)
			return false;

		//Oh, yeah? I think my scary otherworldly shadowy spirit friends might have something to say about that
		if (b.lu.efs[i][j] instanceof EForm) {
			EUnit spirit = locks[i][j] || manual ? ((EForm) b.lu.efs[i][j]).invokeSpirit(this, new int[]{i, j}) : null;
			if (spirit != null) {
				elu.deploySpirit(i, j, this, spirit);
				return true;
			} else if (elu.validSpirit(i,j)) {
				if(manual)
					CommonStatic.setSE(SE_SPEND_FAIL);
				return false;
			}
		}

		if (elu.price[i][j] > money) {
			if (manual)
				CommonStatic.setSE(SE_SPEND_FAIL);
			return false;
		}

		if (locks[i][j] || manual) {
			if (cantDeploy(b.lu.fs[i][j].unit().getRarity(), b.lu.efs[i][j].getWill())) {
				if(manual)
					CommonStatic.setSE(SE_SPEND_FAIL);
				return false;
			}
			IForm f = b.lu.efs[i][j];
			if (f == null)
				return false;

			EUnit eu = f.getEntity(this, new int[]{i, j}, false);
			if (eu == null) {
				if (manual)
					CommonStatic.setSE(SE_SPEND_FAIL);
				return false;
			}
			CommonStatic.setSE(SE_SPEND_SUC);
			elu.resetCD(i, j);
			elu.smnd[i][j] = true;
			rem_spawns--;
			eu.added(-1, st.len - 700);

			le.add(eu);
			money -= elu.price[i][j];
			if (st.minUSpawn == st.maxUSpawn)
				unitRespawnTime = st.minUSpawn;
			else
				unitRespawnTime = st.minUSpawn + (int) ((st.maxUSpawn - st.minUSpawn) * r.nextFloat());

			int[] dupe = duplicateDeployData(b.lu.fs[i][j].unit().getRarity());
			if (dupe[0] != 0) {
				dupeCount[i][j] = dupe[0];
				dupeTime[i][j] = dupe[1];
			}
			return true;
		}
		return false;
	}

	@Override
	protected void performDeepCopy() {
		super.performDeepCopy();
	}

	/**
	 * process actions and add enemies from stage first then update each entity
	 * and receive attacks then excuse attacks and do post update then delete dead
	 * entities
	 */
	protected void update() {
		if (tstop > 0) {
			tstop--;
			if (tstop == 0)
				timeFlow = 1;
		}
		ftime += timeFlow;
		boolean active = ebase.health > 0 && ubase.health > 0;

		if (midH != -1 && bgEffect != null && !bgEffectInitialized) {
			bgEffect.initialize(st.len, battleHeight, midH, bg);
			bgEffectInitialized = true;
		}

		if(unitRespawnTime > 0 && active)
			unitRespawnTime -= timeFlow;
		if(respawnTime > 0 && active)
			respawnTime -= timeFlow;
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++)
				if (dupeCount[i][j] != 0) {
					dupeTime[i][j] -= timeFlow;
					if (dupeTime[i][j] <= 0 && !cantDeploy(b.lu.fs[i][j].unit().getRarity(), b.lu.efs[i][j].getWill()) && b.lu.efs[i][j] != null) {
						EUnit eu = b.lu.efs[i][j].getEntity(this, new int[]{i, j}, false);
						if (eu != null) {
							eu.added(-1, st.len - 700);
							le.add(eu);

							dupeCount[i][j]--;
							dupeTime[i][j] = duplicateDeployData(b.lu.fs[i][j].unit().getRarity())[1];
						}
					}
				}
		elu.update(ftime, timeFlow);

		if (buttonDelay > 0 && (buttonDelay -= timeFlow) <= 0) {
			act_spawn(selectedUnit[0], selectedUnit[1], true);
			selectedUnit[0] = -1;
			selectedUnit[1] = -1;
		}

		tempe.removeIf(e -> {
			if (e.t <= 0) {
				if (e.door == null)
					le.add(e.ent);
				else
					doors.add(e.door);
			}
			return e.t <= 0;
		});
		if (timeFlow > 0 || (ebase.getAbi() & AB_TIMEI) != 0) {
			ebase.preUpdate();
			ebase.update();
		}
		if (timeFlow > 0 || (ubase.getAbi() & AB_TIMEI) != 0) {
			ubase.preUpdate();
			ubase.update();
		}

		if (timeFlow > 0) {
			if(bgEffect != null)
				bgEffect.update(st.len, battleHeight, midH, timeFlow);

			int allow = st.max - entityCount(1);
			if (respawnTime <= 0 && active && allow > 0) {
				EEnemy e = est.allow();

				if (e != null) {
					e.added(1, (e.mark >= 1 ? boss_spawn : 700f) + e.door);

					if (e.door != 0 && e.getAnim().type != AnimU.TYPEDEF[AnimU.ENTRY] && !e.getAnim().anim().getEAnim(AnimU.TYPEDEF[AnimU.WALK]).unusable())
						doors.add(new DoorCont(this, e));
					else
						le.add(e);

					if(st.minSpawn <= 0 || st.maxSpawn <= 0)
						respawnTime = 1;
					else if(st.minSpawn == st.maxSpawn)
						respawnTime = st.minSpawn;
					else
						respawnTime = st.minSpawn + (int) ((st.maxSpawn - st.minSpawn) * r.nextFloat());
				}
			}
			if(cannon == maxCannon -1)
				CommonStatic.setSE(SE_CANNON_CHARGE);
			if (active) {
				cannon += timeFlow;
				int bank = maxBankLimit();
				if (bank > 0)
					maxMoney = bank * 100;
				else {
					maxMoney = b.t().getMaxMon(work_lv, elu.getInc(C_M_MAX));
					money += (int)(b.t().getMonInc(work_lv) * (elu.getInc(C_M_INC) / 100 + 1) * timeFlow);
				}
			}

			if (active)
				est.update();

			if (sniper != null && active)
				sniper.update();

			tempe.forEach(e -> e.update(timeFlow));

			if(shakeDuration <= 0) {
				shake = null;
				shakeOffset = 0;
			}

			if(shake != null) {
				shakeOffset = getOffset();
				shakeDuration--;
			}

			for(int i = 0; i < shakeCoolDown.length; i++)
				if(shakeCoolDown[i] != 0)
					shakeCoolDown[i] -= 1;
		}
		updateEntities(timeFlow > 0);

		canon.update();
		if (timeFlow > 0) {
			lea.forEach(e -> e.update(timeFlow));
			doors.forEach(e -> e.update(timeFlow));
			ebaseSmoke.forEach(e -> e.update(timeFlow));
			ubaseSmoke.forEach(e -> e.update(timeFlow));
			lw.addAll(tlw);
			tlw.clear();
		} else
			for (int i = 0; i < lea.size(); i++) {
				EAnimCont content = lea.get(i);
				if (content instanceof WaprCont && ((WaprCont) content).timeImmune)
					content.update(timeFlow);
			}

		la.forEach(AttackAb::capture);
		la.forEach(AttackAb::excuse);
		la.removeIf(a -> a.duration <= 0);

		if(timeFlow > 0 || (ebase.getAbi() & AB_TIMEI) != 0) {
			ebase.postUpdate();

			if (!lethal && ebase instanceof ECastle && ebase.health <= 0 && est.hasBoss()) {
				lethal = true;
				ebase.health = 1;
			}
		}

		if(timeFlow > 0 || (ubase.getAbi() & AB_TIMEI) != 0)
			ubase.postUpdate();

		if (timeFlow > 0) {
			if (ebase.health <= 0) {
				for (Entity entity : le)
					if (entity.dire == 1)
						entity.kill(false);

				if(ebaseSmoke.size() <= 7 && time % 2 == 0) {
					float x = ebase.pos + 50f - 500f * r.irFloat();
					float y = r.irFloat() * -288;

					ebaseSmoke.add(new EAnimCont(x, 0, EffAnim.effas().A_ATK_SMOKE.getEAnim(DefEff.DEF), y));
				}
			}
			if (ubase.health <= 0) {
				for (int i = 0; i < le.size(); i++)
					if (le.get(i).dire == -1)
						le.get(i).kill(false);

				if(ubaseSmoke.size() <= 7 && time % 2 == 0) {
					float x = ubase.pos - 50f + 500f * r.irFloat();
					float y = r.irFloat() * -288;

					ubaseSmoke.add(new EAnimCont(x, 0, EffAnim.effas().A_ATK_SMOKE.getEAnim(DefEff.DEF), y));
				}
			}
		}
		for (int i = 0; i < le.size(); i++)
			if (timeFlow > 0 || (le.get(i).getAbi() & AB_TIMEI) != 0)
				le.get(i).postUpdate();

		if (shock) {
			for (Entity entity : le)
				if (entity.dire == -1 && (entity.touchable() & TCH_N) > 0) {
					entity.interrupt(INT_SW, KB_DIS[INT_SW]);
					entity.postUpdate();
				}
			lea.add(new EAnimCont(shockP, 9, effas().A_SHOCKWAVE.getEAnim(DefEff.DEF)));
			CommonStatic.setSE(SE_BOSS);
			shock = false;
			shockP = 700;
		}

		if (timeFlow > 0) {
			le.removeIf(e -> e.anim.dead == 0 && e.summoned.isEmpty());
			lw.removeIf(w -> !w.activate);
			lea.removeIf(EAnimCont::done);
			doors.removeIf(EAnimCont::done);
			ebaseSmoke.removeIf(EAnimCont::done);
			ubaseSmoke.removeIf(EAnimCont::done);
		} else
			lea.removeIf(content -> content instanceof WaprCont && ((WaprCont) content).timeImmune && content.done());
		updateTheme();

		cannon = Math.min(maxCannon, Math.max(0, cannon));
		money = Math.min(maxMoney, Math.max(0, money));

		if(changeFrame != -1) {
			changeFrame--;
			if(changeFrame == 0) {
				changeFrame = -1;
				changeDivision = -1;
				lineupChanging = false;
			} else if(changeFrame == changeDivision - 1)
				frontLineup = 1 - frontLineup;
		}
	}

	protected void updateAnimation() {
		boolean active = ebase.health > 0 && ubase.health > 0;
		if (timeFlow > 0 || (ebase.getAbi() & AB_TIMEI) != 0)
			ebase.updateAnimation();

		if (timeFlow > 0) {
			if(bgEffect != null)
				bgEffect.updateAnimation(st.len, battleHeight, midH, timeFlow);
			ubase.updateAnimation();
			canon.updateAnimation();

			if (sniper != null && active)
				sniper.updateAnimation();
		}
		updateEntitiesAnimation(timeFlow > 0);

		if (timeFlow > 0) {
			lea.forEach(e -> e.update(timeFlow));
			ebaseSmoke.forEach(e -> e.update(timeFlow));
			ubaseSmoke.forEach(e -> e.update(timeFlow));
		} else
			for (int i = 0; i < lea.size(); i++) {
				EAnimCont content = lea.get(i);
				if (content instanceof WaprCont && ((WaprCont) content).timeImmune)
					content.update(timeFlow);
			}
	}

	private void updateEntities(boolean time) {
		for (int i = 0; i < le.size(); i++)
			if (time || (le.get(i).getAbi() & AB_TIMEI) != 0)
				le.get(i).preUpdate();
		for (int i = 0; i < le.size(); i++)
			if (time || (le.get(i).getAbi() & AB_TIMEI) != 0)
				le.get(i).update();

		for (int i = 0; i < tlw.size(); i++)
			if (time || tlw.get(i).IMUTime())
				tlw.get(i).update();
		for (int i = 0; i < lw.size(); i++)
			if (time || lw.get(i).IMUTime())
				lw.get(i).update();

		if (!time)
			for (int i = 0; i < tlw.size(); i++)
				if (tlw.get(i).IMUTime()) {
					lw.add(tlw.get(i));
					tlw.remove(i--);
				}
	}

	private void updateEntitiesAnimation(boolean time) {
		for (int i = 0; i < le.size(); i++)
			if (time || (le.get(i).getAbi() & AB_TIMEI) != 0)
				le.get(i).updateAnimation();

		for (int i = 0; i < tlw.size(); i++)
			if (time || tlw.get(i).IMUTime())
				tlw.get(i).updateAnimation();

		for (int i = 0; i < lw.size(); i++)
			if (time || lw.get(i).IMUTime())
				lw.get(i).updateAnimation();
	}

	private void updateTheme() {
		if (theme != null) {
			setBackground(theme);
			if (themeType != null && themeType.kill) {
				le.removeIf(e -> (e.getAbi() & AB_THEMEI) == 0);
				lw.clear();
				la.clear();
				tlw.clear();
				lea.clear();
				tempe.removeIf(e -> (e.ent.getAbi() & AB_THEMEI) == 0);
				doors.removeIf(d -> (d.ent.getAbi() & AB_THEMEI) == 0);
			}
			theme = null;
		}
		if (timeFlow > 0 && themeTime > 0) {
			themeTime -= timeFlow;
			if (themeTime <= 0) {
				if (getEBHP() < st.bgh)
					theme = st.bg1;
				else
					theme = st.bg;

				mus = null;
			}
		}
	}

	private float getOffset() {
		if(shake == null)
			return 0;
		return (1 - 2 * ((shake[SHAKE_DURATION] - shakeDuration) % 2)) * (1f * (shake[SHAKE_END] - shake[SHAKE_INITIAL]) / (shake[SHAKE_DURATION] - 1) * (shake[SHAKE_DURATION] - shakeDuration) + shake[SHAKE_INITIAL]) / SHAKE_STABILIZER;
	}

	private void setBackground(Identifier<Background> id) {
		Background newBg = Identifier.getOr(id, Background.class);
		if (bg != null && bg.id.equals(newBg.id))
			return;
		if ((bg != null && bg.bgEffect != newBg.bgEffect) || (bg == null && newBg.bgEffect != null)) {
			bgEffectInitialized = false;
			if (newBg.bgEffect == null)
				bgEffect = null;
			else
				bgEffect = newBg.bgEffect.get();
		}
		bg = newBg;
	}

	public boolean isBanned(byte comboId) {
		if (est.lim.stageLimit == null)
			return false;
		return est.lim.stageLimit.bannedCatCombo.contains((int) comboId);
	}

	public int maxBankLimit() {
		if (est.lim.stageLimit == null)
			return 0;
		return est.lim.stageLimit.maxMoney;
	}

	public int globalCdLimit() {
		if (est.lim.stageLimit == null)
			return 0;
		return est.lim.stageLimit.globalCooldown;
	}

	public int globalPrice() {
		if (est.lim.stageLimit == null)
			return 0;
		return est.lim.stageLimit.globalCost * 100;
	}

	public int[] duplicateDeployData(int rarity) {
		if (est.lim.stageLimit == null)
			return new int[]{0, 0};
		return new int[]{est.lim.stageLimit.deployDuplicationTimes[rarity], est.lim.stageLimit.deployDuplicationDelay[rarity]};
	}

	public float speedLimit(boolean isEnemy) {
		if (est.lim.stageLimit == null)
			return -1;
		return isEnemy ? est.lim.stageLimit.enemySpeedLimit : est.lim.stageLimit.unitSpeedLimit;
	}

	public BattleList<EUnit> getAllOf(int i, int j) {
		BattleList<EUnit> es = new BattleList<>(le.size());
		for (Entity e : le)
			if (e.anim.dead == -1 && e instanceof EUnit && e.group != -1) {
				EUnit eu = (EUnit)e;
				if (eu.index != null && eu.index[0] == i && eu.index[1] == j)
					es.add((EUnit) e);
			}
		return es;
	}
}