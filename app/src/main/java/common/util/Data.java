package common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.CommonStatic;
import common.io.assets.Admin.StaticPermitted;
import common.io.json.*;
import common.io.json.FieldOrder.Order;
import common.io.json.JsonClass.NoTag;
import common.pack.Context.ErrType;
import common.pack.Context.RunExc;
import common.pack.Context.SupExc;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.pack.Background;
import common.util.pack.EffAnim;
import common.util.stage.Music;
import common.util.unit.Trait;
import common.util.unit.Unit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

@SuppressWarnings("unused")
@StaticPermitted
public class Data {

	public static final Proc empty = Proc.blank();
	@JsonClass(read = JsonClass.RType.MANUAL, write = JsonClass.WType.CLASS, generator = "genProc", serializer = "serProc")
	public static class Proc implements BattleStatic {

		@JsonClass(noTag = NoTag.LOAD)
		public static class PROB extends ProcItem {
			@Order(0)
			public float prob;

			@Override
			public boolean perform(CopRand r) {
				return prob > 0 && (prob >= 100 || r.nextInt(100) < prob);
			}

			@Override
			public int[] setTalent(int[] nps) {
				if (prob == 0) {
					nps[2] = Math.max(1, nps[2]);
					nps[3] = Math.max(1, nps[3]);
				}
				nps[2] = Math.min(nps[2], (int)(100-prob));
				nps[3] = Math.min(nps[3], (int)(100-prob));
				return super.setTalent(nps);
			}

			@Override
			public boolean exists() {
				return prob > 0;
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class MULT extends ProcItem {
			@Order(0)
			public double mult;

			@Override
			public int[] setTalent(int[] nps) {
				if (mult == 0) {
					if (nps[2] == 0)
						nps[2] = 1;
					if (nps[3] == 0)
						nps[3] = 1;
				}
				return super.setTalent(nps);
			}

			@Override
			public boolean exists() {
				return mult != 0;
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class PM extends PROB {
			@Order(1)
			public double mult;

			@Override
			public int[] setTalent(int[] nps) {
				if (mult == 0) {
					if (nps[4] == 0)
						nps[4] = 1;
					if (nps[5] == 0)
						nps[5] = 1;
				}
				return super.setTalent(nps);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class PT extends PROB {
			@Order(1)
			public int time;

			@Override
			public int[] setTalent(int[] nps) {
				if (time == 0) {
					if (nps[4] == 0)
						nps[4] = 1;
					if (nps[5] == 0)
						nps[5] = 1;
				}
				return super.setTalent(nps);
			}

			@Override
			public boolean perform(CopRand r) {
				if (time == 0)
					return false;
				return super.perform(r);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class PTD extends PT {
			@Order(2)
			public int dis;

			@Override
			public int[] setTalent(int[] nps) {
				if (time == 0) {
					time = 1;
					nps = super.setTalent(nps);
					time = 0;
				} else
					nps = super.setTalent(nps);
				return nps;
			}

			@Override
			public boolean perform(CopRand r) {
				return prob > 0 && (prob >= 100 || r.nextInt(100) < prob);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class PTM extends PT {
			@Order(2)
			public double mult;

			@Override
			public int[] setTalent(int[] nps) {
				if (mult == 0) {
					if (nps[6] == 0)
						nps[6] = 1;
					if (nps[7] == 0)
						nps[7] = 1;
				}
				return super.setTalent(nps);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class PTMS extends PTM {
			@Order(3)
			public boolean stackable;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class IMU extends MULT {
			@Order(1)
			public float block;

			@Override
			public int[] setTalent(int[] nps) {
				nps[2] = Math.min(nps[2], (int)(100-mult));
				nps[3] = Math.min(nps[3], (int)(100-mult));
				nps[4] = Math.min(nps[4], (int)(100-block));
				nps[5] = Math.min(nps[5], (int)(100-block));
				return super.setTalent(nps);
			}

			@Override
			public boolean exists() {
				return mult != 0 || block != 0;
			}
		}

		@JsonClass(noTag = NoTag.LOAD) // Similar to IMU. Supports ids
		public static class IMUI extends IMU {
			@Order(2)
			@JsonField(defval = "isEmpty")
			public ProcID pid = new ProcID();
		}

		@JsonClass(noTag = NoTag.LOAD) // Similar to WAVEI. Supports ids
		public static class MOVEI extends MULT {
			@Order(1)
			@JsonField(defval = "isEmpty")
			public ProcID pid = new ProcID();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class IMUAD extends IMU {
			public enum FOCUS {
				ALL(0),
				DEBUFF(1),
				BUFF(-1);

				public final int effect;
				FOCUS(int eff) {
					effect = eff;
				}
			}
			@Order(2)
			@JsonField(defval = "null||ALL")
			public FOCUS focus = FOCUS.ALL;

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("smartImu")) {
					int i = jobj.get("smartImu").getAsInt();
					focus = FOCUS.values()[i == -1 ? 2 : i];
				}
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class WORKLV extends PROB {
			@Order(1)
			public int mult; //The same as PM, but mult is an int

			@Override
			public void add(ProcItem proc) {
				super.add(proc);
				mult = Math.max(-7, Math.min(mult, 7));
			}

			@Override
			public boolean perform(CopRand r) {
				if (mult == 0)
					return false;
				return super.perform(r);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class REFUND extends PM {
			@Order(2)
			@JsonField(defval = "1")
			public int count = 1;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class WAVE extends PROB {
			@Order(1)
			public int lv;
			@Order(3)
			public boolean hitless;
			@Order(4)
			public boolean inverted;
			@Order(5)
			@JsonField(defval = "isEmpty")
			public ProcID pid = new ProcID();

			@Override
			public int[] setTalent(int[] nps) {
				int min = lv == 0 ? 1 : 0;
				nps[4] = Math.max(min, nps[4]);
				nps[5] = Math.max(min, nps[5]);
				return super.setTalent(nps);
			}

			@Override
			public void add(ProcItem proc) {
				super.add(proc);
				lv = Math.max(1, lv);
			}

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type"))
					hitless = jobj.getAsJsonObject("type").get("hitless").getAsBoolean();
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class MINIWAVE extends WAVE {
			@Order(2)
			@JsonField(defval = "20")
			public int multi = 20;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class CANNI extends MULT {
			@Order(1)
			@BitMasked
			public int type;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class VOLC extends PROB {
			@Order(1)
			public int dis_0;
			@Order(2)
			public int dis_1;
			@Order(3)
			public int time;
			@Order(5)
			public boolean hitless;
			@Order(6)
			@JsonField(defval = "isEmpty")
			public ProcID pid = new ProcID();
			@Order(7)
			@JsonField(defval = "1")
			public int spawns = 1;//Only for deathsurges

			@Override
			public int[] setTalent(int[] nps) {
				int min = time == 0 ? 1 : 0;
				nps[8] = Math.max(min, nps[8] / VOLC_ITV) * VOLC_ITV;
				nps[9] = Math.max(min, nps[9] / VOLC_ITV) * VOLC_ITV;
				int d0 = nps[4], d1 = nps[5];
				nps[4] = Math.min(d0, nps[6]);
				nps[5] = Math.min(d1, nps[7]);
				nps[6] = Math.max(d0, nps[6]);
				nps[7] = Math.max(d1, nps[7]);
				return super.setTalent(nps);
			}

			@Override
			public void add(ProcItem proc) {
				super.add(proc);
				time = Math.max(VOLC_ITV, time);
			}

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type"))
					hitless = jobj.getAsJsonObject("type").get("hitless").getAsBoolean();
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class MINIVOLC extends VOLC {
			@Order(4)
			@JsonField(defval = "20")
			public int mult = 20;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class BLAST extends PROB {
			@Order(1)
			public int dis_0;
			@Order(2)
			public int dis_1;
			@Order(3)
			@JsonField(defval = "3")
			public int lv = 3;
			@Order(4)
			@JsonField(defval = "30")
			public float reduction = 30;
			@Order(5)
			@JsonField(defval = "isEmpty")
			public ProcID pid = new ProcID();

			@Override
			public int[] setTalent(int[] nps) {
				int d0 = nps[4], d1 = nps[5];
				nps[4] = Math.min(d0, nps[6]);
				nps[5] = Math.min(d1, nps[7]);
				nps[6] = Math.max(d0, nps[6]);
				nps[7] = Math.max(d1, nps[7]);

				int min = lv == 0 ? 1 : 0;
				nps[8] = Math.max(min, nps[8]);
				nps[9] = Math.max(min, nps[9]);
				nps[10] = (int)Math.min(reduction + nps[10], 100f / (lv + nps[8]));
				nps[11] = (int)Math.min(reduction + nps[11], 100f / (lv + nps[9]));

				return super.setTalent(nps);
			}

			@Override
			public void add(ProcItem proc) {
				super.add(proc);
				lv = Math.max(1, lv);
				reduction = Math.min(reduction, 100f / lv);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class STRONG extends ProcItem {
			@Order(0)
			public float health;
			@Order(1)
			public int mult;
			@Order(2)
			public boolean incremental;

			@Override
			public int[] setTalent(int[] nps) {
				int min = health == 0 ? 1 : 0;
				for (byte i = 2; i < 6; i++)
					nps[i] = Math.max(min, nps[i]);
				return super.setTalent(nps);
			}

			@Override
			public boolean exists() {
				return health > 0;
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class IMUATK extends PT {
			@Order(2)
			public int cd;

			@Override
			public void add(ProcItem proc) {
				super.add(proc);
				cd = Math.max(0, cd);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class BURROW extends ProcItem {
			@Order(0)
			public int count;
			@Order(1)
			public int dis;

			@Override
			public int[] setTalent(int[] nps) {
				int min = count == 0 ? 1 : 0;
				for (byte i = 2; i < 6; i++)
					nps[i] = Math.max(min, nps[i]);
				return super.setTalent(nps);
			}

			@Override
			public boolean exists() {
				return count != 0;
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class REVIVE extends ProcItem {
			public enum RANGE {
				ACTIVE,
				PRESENT,
				ALIVE,
				FOREVER
			}
			@Order(0)
			public int count;
			@Order(1)
			public int time;
			@Order(2)
			public int health;
			@Order(3)
			public int dis_0;
			@Order(4)
			public int dis_1;
			@Order(5)
			@JsonField(defval = "null||ACTIVE")
			public RANGE range_type = RANGE.ACTIVE;
			@Order(6)
			public boolean imu_zkill;
			@Order(7)
			public boolean revive_non_zombie;
			@Order(8)
			public boolean revive_others;

			@Override
			public int[] setTalent(int[] nps) {
				int min = count == 0 ? 1 : 0;
				for (byte i = 2; i < 8; i++)
					nps[i] = Math.max(min, nps[i]);
				int d0 = nps[8], d1 = nps[9];
				nps[8] = Math.min(d0, nps[10]);
				nps[9] = Math.min(d1, nps[11]);
				nps[10] = Math.max(d0, nps[10]);
				nps[11] = Math.max(d1, nps[11]);
				return super.setTalent(nps);
			}

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type")) {
					JsonObject type = jobj.getAsJsonObject("type");
					if (type.has("range_type"))
						range_type = RANGE.values()[type.get("range_type").getAsInt()];
					if (type.has("imu_zkill"))
						imu_zkill = type.get("imu_zkill").getAsBoolean();
					if (type.has("revive_non_zombie"))
						revive_non_zombie = type.get("revive_non_zombie").getAsBoolean();
					if (type.has("revive_others"))
						revive_others = type.get("revive_others").getAsBoolean();
				}
			}

			@Override
			public boolean exists() {
				return count != 0;
			}
		}

		@JsonClass(noTag = NoTag.LOAD) // Starred Barrier
		public static class BARRIER extends ProcItem {
			@Order(0)
			public int health;
			@Order(1)
			public int regentime;
			@Order(2)
			public int timeout;
			@Order(3)
			public boolean magnif;

			@Override
			public int[] setTalent(int[] nps) {
				int min = health == 0 ? 1 : 0;
				nps[2] = Math.max(min, nps[2]);
				nps[3] = Math.max(min, nps[3]);
				return super.setTalent(nps);
			}

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type"))
					magnif = jobj.getAsJsonObject("type").get("magnif").getAsBoolean();
			}

			@Override
			public boolean exists() {
				return health > 0;
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class DSHIELD extends ProcItem {
			@Order(0)
			public int hp;
			@Order(1)
			public int regen;

			@Override
			public int[] setTalent(int[] nps) {
				int min = hp == 0 ? 1 : 0;
				for (byte i = 2; i < 6; i++)
					nps[i] = Math.max(min, nps[i]);
				return super.setTalent(nps);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class BSTHUNT extends ProcItem {
			@Order(0)
			@JsonField(defval = "dodges")
			public boolean active;
			@Order(1)
			public float prob;
			@Order(2)
			public int time;

			@Override
			public int[] setTalent(int[] nps) {
				int min = prob == 0 ? 1 : 0;
				for (byte i = 2; i < 6; i++)
					nps[i] = Math.max(min, nps[i]);
				nps[2] = Math.min(nps[2], (int)(100-prob));
				nps[3] = Math.min(nps[3], (int)(100-prob));
				return super.setTalent(nps);
			}
			public boolean dodges() {
				return prob > 0;
			}
			@JsonDecoder.OnInjected
			public void inject() {
				active = true;
			}

			@Override
			public boolean exists() {
				return active || prob > 0;
			}
			@Override
			public boolean perform(CopRand r) {
				return time > 0 && prob > 0 && (prob >= 100 || r.nextInt(100) < prob);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class CDSETTER extends PROB {
			@Order(1)
			public int amount;
			@Order(2)
			public int slot;
			@Order(3)
			public int type; //0 - frames, 1 - %, 2 - set
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class AURA extends ProcItem {
			@Order(0)
			public int amult; //Modifies Damage
			@Order(1)
			public int dmult; //Modifies Defense
			@Order(2)
			public int smult; //Modifies Speed
			@Order(3)
			public int tmult; //Modifies TBA
			@Order(4)
			public int min_dis;
			@Order(5)
			public int max_dis;
			@Order(6)
			public boolean trait;
			@Order(7)
			public boolean skip_self;

			@Override
			public int[] setTalent(int[] nps) {
				boolean ne = !exists();
				if (ne)
					for (byte i = 2; i < 10; i += 2)
						if (nps[i] != 0) {
							ne = false;
							break;
						}
				if (ne)
					nps[2] = nps[3] = 1;
				int d0 = nps[10], d1 = nps[11], minus = exists() ? 0 : 1;
				nps[10] = Math.min(d0, nps[12] - minus);
				nps[11] = Math.min(d1, nps[13] - minus);
				nps[12] = Math.max(d0, nps[12]);
				nps[13] = Math.max(d1, nps[13]);
				return super.setTalent(nps);
			}

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type"))
					trait = jobj.getAsJsonObject("type").get("trait").getAsBoolean();
			}

			@Override
			public boolean exists() {
				return amult != 0 || dmult != 0 || smult != 0 || tmult != 0;
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class LETHARGY extends PTMS {
			@Order(3)
			public boolean percentage;

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type"))
					percentage = jobj.getAsJsonObject("type").get("percentage").getAsBoolean();
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class MOVEWAVE extends PROB {
			@Order(1)
			public int speed;
			@Order(2)
			public int width;
			@Order(3)
			public int time;
			@Order(4)
			public int dis;
			@Order(5)
			public int itv;
			@Order(6)
			@JsonField(defval = "isEmpty")
			public ProcID pid = new ProcID();
			@Order(7)
			public boolean hit_base;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class POISON extends PT {
			public enum TYPE {
				BURN,
				POISON,
				BLEED,
				CORRODE
			}
			@Order(2)
			public int damage;
			@Order(3)
			public int itv;
			@Order(4)
			@JsonField(defval = "null||BURN")
			public TYPE damage_type = TYPE.BURN;
			@Order(5)
			public boolean unstackable;
			@Order(6)
			public boolean ignoreMetal;
			@Order(7)
			public boolean modifAffected;

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type")) {
					JsonObject type = jobj.getAsJsonObject("type");
					if (type.has("damage_type"))
						damage_type = TYPE.values()[type.get("damage_type").getAsInt()];
					if (type.has("unstackable"))
						unstackable = type.get("unstackable").getAsBoolean();
					if (type.has("ignoreMetal"))
						ignoreMetal = type.get("ignoreMetal").getAsBoolean();
					if (type.has("modifAffected"))
						modifAffected = type.get("modifAffected").getAsBoolean();
				}
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class WARP extends PTD {
			@Order(3)
			public int dis_1;

			@Override
			public int[] setTalent(int[] nps) {
				int d0 = nps[6], d1 = nps[7], minus = exists() ? 0 : 1;
				nps[6] = Math.min(d0, nps[8] - minus);
				nps[7] = Math.min(d1, nps[9] - minus);
				nps[8] = Math.max(d0, nps[8]);
				nps[9] = Math.max(d1, nps[9]);
				return super.setTalent(nps);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class TIME extends PT {
			@Order(2)
			@JsonField(defval = "100")
			public float intensity = 100;

			@Override
			public int[] setTalent(int[] nps) {
				int min = prob > 0 ? 0 : 1;
				nps[6] = Math.max(min, Math.min(nps[6], time + nps[4]));
				nps[7] = Math.max(min, Math.min(nps[7], time + nps[5]));
				return super.setTalent(nps);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class SPEED extends PT {
			public enum TYPE {
				FIXED,
				PERCENTAGE,
				SET
			}
			@Order(2)
			public int speed;
			@Order(3)
			@JsonField(defval = "null||FIXED")
			public TYPE type = TYPE.FIXED;
			@Order(4)
			public boolean stackable;
		}

		public enum SUMMON_ANIM {
			NONE,
			WARP,
			BURROW,
			BURROW_DISABLE,
			ENTRY,
			ATTACK,
			EVERYWHERE_DOOR
		}
		@JsonClass(noTag = NoTag.LOAD)
		public static class SUMMON extends PROB {
			@Order(1)
			public Identifier<?> id;
			@Order(2)
			public int dis;
			@Order(3)
			public int max_dis;
			@Order(4)
			@JsonField(defval = "1")
			public int mult = 1;
			@Order(5)
			public int min_layer;
			@Order(6)
			@JsonField(defval = "9")
			public int max_layer = 9;
			@Order(7)
			@JsonField(defval = "null||NONE")
			public SUMMON_ANIM anim_type = SUMMON_ANIM.NONE;
			@Order(8)
			public boolean ignore_limit;
			@Order(9)
			public boolean fix_buff;
			@Order(10)
			public boolean same_health;
			@Order(11)
			public boolean bond_hp;
			@Order(12)
			public boolean on_hit;
			@Order(13)
			public boolean on_kill;
			@Order(14)
			@BitMasked
			public int pass_proc;
			@Order(15)
			public int time;
			@Order(16)
			@JsonField(defval = "1")
			public int amount = 1;
			@Order(17)
			@JsonField(defval = "1")
			public int form = 1;
			@Order(18)
			public int interval;

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type")) {
					JsonObject type = jobj.getAsJsonObject("type");
					if (type.has("anim_type"))
						anim_type = SUMMON_ANIM.values()[type.get("anim_type").getAsInt()];
					if (type.has("ignore_limit"))
						ignore_limit = type.get("ignore_limit").getAsBoolean();
					if (type.has("fix_buff"))
						fix_buff = type.get("fix_buff").getAsBoolean();
					if (type.has("same_health"))
						same_health = type.get("same_health").getAsBoolean();
					if (type.has("bond_hp"))
						bond_hp = type.get("bond_hp").getAsBoolean();
					if (type.has("on_hit"))
						on_hit = type.get("on_hit").getAsBoolean();
					if (type.has("on_kill"))
						on_kill = type.get("on_kill").getAsBoolean();
					if (type.has("pass_proc"))
						pass_proc = type.get("pass_proc").getAsInt();
				}
			}

			@Override
			public void add(ProcItem itm) {
				SUMMON_ANIM a = prob > 0 ? anim_type : ((SUMMON)itm).anim_type;
				super.add(itm);
				anim_type = a;
				amount = Math.max(1, amount);
				interval = Math.max(0, interval);
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class THEME extends PT {
			@Order(2)
			public Identifier<Background> id;
			@Order(3)
			public Identifier<Music> mus;
			@Order(4)
			public boolean kill;

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type"))
					kill = jobj.getAsJsonObject("type").get("kill").getAsBoolean();
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class COUNTER extends PROB {
			public enum CWAVE {
				NONE,
				COUNTER,
				REFLECT
			}
			@Order(1)
			public int damage;
			@Order(2)
			public int minRange;
			@Order(3)
			public int maxRange;
			@Order(4)
			@JsonField(defval = "null||NONE")
			public CWAVE counterWave = CWAVE.NONE;
			@Order(5)
			@BitMasked
			public int procType;
			@Order(6)
			public boolean useOwnDamage;
			@Order(7)
			public boolean outRange;
			@Order(8)
			public boolean areaAttack;
			@Order(9)
			public int maxDamage;

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type")) {
					JsonObject type = jobj.getAsJsonObject("type");
					if (type.has("counterWave"))
						counterWave = CWAVE.values()[type.get("counterWave").getAsInt()];
					if (type.has("procType"))
						procType = type.get("procType").getAsInt();
					if (type.has("useOwnDamage"))
						useOwnDamage = type.get("useOwnDamage").getAsBoolean();
					if (type.has("outRange"))
						outRange = type.get("outRange").getAsBoolean();
					if (type.has("areaAttack"))
						areaAttack = type.get("areaAttack").getAsBoolean();
				}
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class DMGCUT extends PROB {
			@Order(1)
			public int dmg;
			@Order(2)
			public int reduction;
			@Order(3)
			public boolean traitIgnore;
			@Order(4)
			public boolean procs;
			@Order(5)
			public boolean magnif;

			@Override
			public int[] setTalent(int[] nps) {
				int min = prob > 0 ? 0 : 1;
				nps[4] = Math.max(min, nps[4]);
				nps[5] = Math.max(min, nps[5]);
				return super.setTalent(nps);
			}

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type")) {
					JsonObject type = jobj.getAsJsonObject("type");
					if (type.has("traitIgnore"))
						traitIgnore = type.get("traitIgnore").getAsBoolean();
					if (type.has("procs"))
						procs = type.get("procs").getAsBoolean();
					if (type.has("magnif"))
						magnif = type.get("magnif").getAsBoolean();
				}
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class DMGCAP extends PROB {
			@Order(1)
			public int dmg;
			@Order(2)
			public boolean traitIgnore;
			@Order(3)
			public boolean nullify;
			@Order(4)
			public boolean procs;
			@Order(5)
			public boolean magnif;

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type")) {
					JsonObject type = jobj.getAsJsonObject("type");
					if (type.has("traitIgnore"))
						traitIgnore = type.get("traitIgnore").getAsBoolean();
					if (type.has("nullify"))
						nullify = type.get("nullify").getAsBoolean();
					if (type.has("procs"))
						procs = type.get("procs").getAsBoolean();
					if (type.has("magnif"))
						magnif = type.get("magnif").getAsBoolean();
				}
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class REMOTESHIELD extends PROB {
			@Order(1)
			public int minrange;
			@Order(2)
			public int maxrange;
			@Order(3)
			public int reduction;
			@Order(4)
			public int block;
			@Order(5)
			public boolean traitCon;
			@Order(6)
			public boolean procs;
			@Order(7)
			public boolean waves;

			@Override
			public int[] setTalent(int[] nps) {
				int d0 = nps[4], d1 = nps[5], minus = exists() ? 0 : 1;
				nps[4] = Math.min(d0, nps[6] - minus);
				nps[5] = Math.min(d1, nps[7] - minus);
				nps[6] = Math.max(d0, nps[6]);
				nps[7] = Math.max(d1, nps[7]);

				int min = prob == 0 && nps[10] == 0 ? 1 : 0;
				nps[8] = Math.max(min, nps[8]);
				nps[9] = Math.max(min, nps[9]);

				return super.setTalent(nps);
			}

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type")) {
					JsonObject type = jobj.getAsJsonObject("type");
					if (type.has("traitCon"))
						traitCon = type.get("traitCon").getAsBoolean();
					if (type.has("procs"))
						procs = type.get("procs").getAsBoolean();
					if (type.has("waves"))
						waves = type.get("waves").getAsBoolean();
				}
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class AI extends ProcItem {
			@Order(0)
			public int retreatDist;
			@Order(1)
			public int retreatSpeed;
			@Order(2)
			public boolean calcstrongest;
			@Order(3)
			public boolean calcblindspot;
			@Order(4)
			public boolean danger;
			@Order(5)
			public boolean ignHypno;
			//@Order(6)
			//public boolean manualcontrol; //The player controls the unit manually; Arrow keys to move, spacebar to attack

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type")) {
					JsonObject type = jobj.getAsJsonObject("type");
					if (type.has("calcstrongest"))
						calcstrongest = type.get("calcstrongest").getAsBoolean();
					if (type.has("calcblindspot"))
						calcblindspot = type.get("calcblindspot").getAsBoolean();
				}
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class RANGESHIELD extends PM {
			@Order(2)
			public boolean range;

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type") && jobj.getAsJsonObject("type").has("range"))
					range = jobj.getAsJsonObject("type").get("range").getAsBoolean();
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class SPIRIT extends ProcItem {
			@Order(0)
			public Identifier<?> id;
			@Order(1)
			@JsonField(defval = "15")
			public int cd0 = 15;
			@Order(2)
			@JsonField(defval = "15")
			public int cd1 = 15;
			@Order(3)
			@JsonField(defval = "1")
			public int amount = 1;
			@Order(4)
			public int summonerCd;
			@Order(5)
			public int moneyCost;
			@Order(6)
			@JsonField(defval = "null||NONE")
			public SUMMON_ANIM animType = SUMMON_ANIM.NONE;
			@Order(7)
			@JsonField(defval = "1")
			public int form = 1;
			@Order(8)
			public boolean inv;

			public int[] setTalent(int[] nps) {
				if (id == null && nps[2] == 0)
					nps[2] = nps[3] = 1;
				nps[4] = Math.max(nps[4], 15-cd0);
				nps[5] = Math.max(nps[5], 15-cd0);
				nps[6] = Math.max(nps[6], 15-cd1);
				nps[7] = Math.max(nps[7], 15-cd1);

				nps[8] = Math.max(nps[8], amount == 0 ? 1 : 0);
				nps[9] = Math.max(nps[9], amount == 0 ? 1 : 0);

				nps[14] = nps[15] = Math.max(0,Math.min(nps[14], SUMMON_ANIM.values().length - 1));
				nps[16] = Math.max(nps[16], 1-form);
				nps[17] = Math.max(nps[17], 1-form);
				if (nps[2] > 0) {//Dunno how to custom unit
					int fs = ((Unit)Identifier.rawParseInt(nps[2]-1, Unit.class).get()).forms.length - form;
					nps[16] = Math.min(nps[16], fs);
					nps[17] = Math.min(nps[17], fs);
				} else if (id != null) {
					int fs = ((Unit)id.get()).forms.length - form;
					nps[16] = Math.min(nps[16], fs);
					nps[17] = Math.min(nps[17], fs);
				}
				return super.setTalent(nps);
			}

			@Override
			public boolean def_exists() {
				return id != null;
			}

			@JsonDecoder.OnInjected
			public void inject(JsonObject jobj) {
				if (jobj.has("type") && jobj.getAsJsonObject("type").has("inv"))
					inv = jobj.getAsJsonObject("type").get("inv").getAsBoolean();
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class BLESSING extends PT {
			@Order(2)
			public boolean stackable;
			@Order(3)
			public int abis;
			@Order(4)
			@JsonField(defval = "null||isBlank")
			public Proc procs;
			@Order(5)
			@JsonField(generic = Trait.class, alias = Identifier.class, defval = "isEmpty")
			public SortedPackSet<Trait> traits = new SortedPackSet<>();
		}
		@JsonClass(noTag = NoTag.LOAD)
		public static class STATINC extends MULT { //It has no params, it just dictates behavior for strong v bless
			@Override
			public void add(ProcItem pi) {
				double m = ((MULT)pi).mult;
				if (m == 0)
					return;
				if (mult == 0)
					mult += 100;
				if (mult + m == 0)
					m -= m > 0 ? 0.01 : -0.01; //Negligible difference but doesn't reset the markiplier
				mult += m;
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class ProcID implements Cloneable, BattleStatic {
			@Order(0)
			@JsonField(generic = Integer.class)
			public SortedPackSet<Integer> l = new SortedPackSet<>();

			public ProcID() {
			}
			public ProcID(ProcID pid) {
				setData(pid.getData());
			}
			public void setData(int[] data) {
				l.clear();
				for (int i : data)
					l.add(i);
			}

			public boolean match(ProcID pid) {
				if (isEmpty() || ((pid.isEmpty()) && l.contains(0)))
					return true; //ID 0 grants immunity to all default procs. It's like ID 0
				for (int pack : pid.l)
					if (l.contains(pack))
						return true;
				return false;
			}

			public boolean isEmpty() {
				return l.isEmpty();
			}
			public void clear() {
				l.clear();
			}

			public int[] getData() {
				int[] is = new int[l.size()];
				int i = 0;
				for (int k : l)
					is[i++] = k;
				return is;
			}

			@Override
			public ProcID clone() {
				return new ProcID(this);
			}
			@Override
			public String toString() {
				return l.toString();
			}
		}

		public static abstract class ProcItem implements Cloneable, BattleStatic {
			@Retention(RetentionPolicy.RUNTIME)
			public @interface BitMasked {//Nothingburger
			}

			public ProcItem clear() {
				try {
					Field[] fs = getDeclaredFields();
					for (Field f : fs)
						if (f.getType() == int.class || f.getType() == float.class || f.getType() == double.class)
							f.set(this, 0);
						else if (f.getType() == boolean.class)
							f.setBoolean(this, false);
						else if (f.getType() == Identifier.class || f.getType() == Proc.class)
							f.set(this, null);
						else if (f.getType() == SortedPackSet.class)
							((SortedPackSet<?>)f.get(this)).clear();
						else if (f.getType() == ProcID.class)
							((ProcID)f.get(this)).clear();
						else if (Enum.class.isAssignableFrom(f.getType()))
							f.set(this, f.getType().getEnumConstants()[0]);
						else
							throw new Exception("unknown field " + f.getType() + " " + f.getName());
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Error clearing Proc of type " + getClass().getName());
				}
				return this;
			}

			@Override
			public ProcItem clone() {
				try {
					ProcItem ans = (ProcItem) super.clone();
					for (Field f : getDeclaredFields())
						if (f.get(this) != null) {
							if (f.getType() == Identifier.class)
								f.set(ans, ((Identifier<?>) f.get(this)).clone());
							else if (f.getType() == Proc.class) {
								f.set(ans, ((Proc) f.get(this)).clone());
								for (Field ff : Proc.getDeclaredFields())
									ff.setAccessible(true);
							} else if (f.getType() == SortedPackSet.class) {
								f.set(ans, ((SortedPackSet<?>) f.get(this)).clone());
							} else if (f.getType() == ProcID.class)
								f.set(ans, ((ProcID)f.get(this)).clone());
						}
					return ans;
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Error cloning Proc of type " + getClass().getName());
					return null;
				}
			}

			public boolean def_exists() {
				try {
					Field[] fs = getDeclaredFields();
					for (Field f : fs)
						if (f.getType() == double.class || f.getType() == float.class) {
							if (f.getDouble(this) != (f.getAnnotation(JsonField.class) != null ? Double.parseDouble(f.getAnnotation(JsonField.class).defval()) : 0))
								return true;
						} else if (f.getType() == int.class) {
							if (f.getInt(this) != (f.getAnnotation(JsonField.class) != null ? Integer.parseInt(f.getAnnotation(JsonField.class).defval()) : 0))
								return true;
						} else if (f.getType() == Identifier.class) {
							if (f.get(this) != null)
								return true;
						} else if (f.getType() == boolean.class) {
							if (f.getBoolean(this))
								return true;
						} else if (f.getType() == ProcID.class) {
							if (!((ProcID)f.get(this)).isEmpty())
								return true;
						} else if (Enum.class.isAssignableFrom(f.getType()))
							if (!f.get(this).toString().equals(f.getAnnotation(JsonField.class).defval()))
								return true;
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Error checking if Proc of type " + getClass().getName() + " exists");
				}
				return false;
			}
			public boolean exists() {
				return def_exists();
			}

			public int get(int i) { //Only used for talents
				try {
					Field f = getDeclaredFields()[i];
					if (f.getType() == Identifier.class)
						return ((Identifier<?>)f.get(this)).id;
					else if (Enum.class.isAssignableFrom(f.getType()))
						return ((Enum<?>)f.get(this)).ordinal();
					else if (f.getType() == boolean.class)
						return f.getBoolean(this) ? 1 : 0;
					else if (f.getType() == ProcID.class) {
						SortedPackSet<Integer> l = ((ProcID)f.get(this)).l;
						return l.isEmpty() ? 0 : l.get(l.size() - 1);
					}
					return f.getType() == int.class ? f.getInt(this) : (int)f.getDouble(this);
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Error getting talent " + i + " for proc type " + getClass().getName());
					return 0;
				}
			}

			public String getFieldName(int i) {
				try {
					return getDeclaredFields()[i].getName();
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, i < 0 ? "Negative Index" : getClass().getName() + " has less than " + i + " fields");
					return null;
				}
			}

			public Field[] getDeclaredFields() {
				return FieldOrder.getFields(this.getClass());
			}

			public boolean perform(CopRand r) {
				return exists();
			}

			public Field get(String name) {
				try {
					return this.getClass().getField(name);
				} catch (Exception ignored) {
					return null;
				}
			}

			/**
			 * should not modify Identifier, used for talents only
			 */
			@Deprecated
			public void set(int i, int v) {
				try {
					Field f = getDeclaredFields()[i];
					if (f.getType() == boolean.class)
						f.set(this, v != 0);
					else if (f.getType() == ProcID.class) {
						ProcID pid = (ProcID)f.get(this);
						if (v != 0 || !pid.isEmpty())
							pid.l.add(v);
					} else if (Enum.class.isAssignableFrom(f.getType()))
						f.set(this, f.getType().getEnumConstants()[v]);
					else
						f.set(this, v);
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Error setting " + v + " for talent " + i + " for proc type " + getClass().getName());
				}
			}
			/**
			 * Modifies Identifier, used for talents only
			 */
			@Deprecated
			public void set(int i, Identifier<?> id) {
				try {
					Field f = getDeclaredFields()[i];
					f.set(this, id);
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Error setting " + id + " for talent " + i + " for proc type " + getClass().getName());
				}
			}

			public void set(ProcItem pi) {
				try {
					for (Field f : getDeclaredFields())
						if (f.getType().isPrimitive() || Enum.class.isAssignableFrom(f.getType()))
							f.set(this, f.get(pi));
						else if (f.getType() == Identifier.class) {
							Identifier<?> id = (Identifier<?>) f.get(pi);
							f.set(this, id == null ? null : id.clone());
						} else if (f.getType() == Proc.class) {
							Proc p = (Proc) f.get(pi);
							f.set(this, p == null ? null : p.clone());
						} else if (f.getType() == SortedPackSet.class) {
							SortedPackSet<?> l = (SortedPackSet<?>)f.get(pi);
							f.set(this, l.clone());
						} else if (f.getType() == ProcID.class) {
							ProcID l = (ProcID)f.get(pi);
							f.set(this, l.clone());
						} else
							throw new Exception("unknown field " + f.getType() + " " + f.getName());
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Error setting data for proc type " + getClass().getName());
				}
			}
			public void add(ProcItem pi) {
				if (!pi.def_exists())
					return;
				try {
					for (Field f : getDeclaredFields()) {
						if (f.getType() == int.class) {
							if (f.getAnnotation(BitMasked.class) != null)
								f.set(this, (int) f.get(this) | (int) f.get(pi));
							f.set(this, (int) f.get(this) + (int) f.get(pi));
						} else if (f.getType() == float.class)
							f.set(this, (float)f.get(this) + (float)f.get(pi));
						else if (f.getType() == double.class)
							f.set(this, (double)f.get(this) + (double)f.get(pi));
						else if (f.getType() == boolean.class)
							f.set(this, (boolean)f.get(this) || (boolean)f.get(pi));
						else if (Enum.class.isAssignableFrom(f.getType()))
							f.set(this, f.get(pi));
						else if (f.getType() == Identifier.class) {
							Identifier<?> id = (Identifier<?>)f.get(pi);
							if (id != null)
								f.set(this, id.clone());
						} else if (f.getType() == Proc.class) {
							Proc p = (Proc)f.get(pi);
							if (p != null)
								f.set(this, p.clone());
						} else if (f.getType() == SortedPackSet.class) {
							SortedPackSet<? extends Comparable<?>> l = (SortedPackSet<? extends Comparable<?>>) f.get(pi), m = (SortedPackSet<? extends Comparable<?>>) f.get(this);
							m.addAll(l);
							f.set(this, m);
						} else if (f.getType() == ProcID.class) {
							ProcID l = (ProcID)f.get(pi), m = (ProcID)f.get(this);
							m.l.addAll(l.l);
							f.set(this, m);
						} else
							throw new Exception("unknown field " + f.getType() + " " + f.getName());
					}
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Error adding bless effect for proc type " + getClass().getName());
				}
			}

			public int[] setTalent(int[] nps) {
				for (int i = 2; i < nps.length - 1; i += 2) {
					int m = nps[i];
					if (m * nps[i+1] < 0)
						continue;
					if (m >= 0 && nps[i + 1] > 0) {
						nps[i] = Math.min(m, nps[i + 1]);
						nps[i + 1] = Math.max(m, nps[i + 1]);
					} else {
						nps[i] = Math.max(m, nps[i + 1]);
						nps[i + 1] = Math.min(m, nps[i + 1]);
					}
				}
				return nps;
			}

			@Override
			public String toString() {
				return JsonEncoder.encode(this).toString();
			}

		}

		public static Proc blank() {
			return new Proc();
		}

		public static Field[] getDeclaredFields() {
			return FieldOrder.getFields(Proc.class);
		}

		public static String getName(int i) {
			return getDeclaredFields()[i].getName();
		}

		@Order(0)
		public final PTD KB = new PTD();
		@Order(1)
		public final PTM STOP = new PTM();
		@Order(2)
		public final PT SLOW = new PT();
		@Order(3)
		public final PM CRIT = new PM();
		@Order(4)
		public final WAVE WAVE = new WAVE();
		@Order(5)
		public final MINIWAVE MINIWAVE = new MINIWAVE();
		@Order(6)
		public final MOVEWAVE MOVEWAVE = new MOVEWAVE();
		@Order(7)
		public final VOLC VOLC = new VOLC();
		@Order(8)
		public final PTMS WEAK = new PTMS();
		@Order(9)
		public final PROB BREAK = new PROB();
		@Order(10)
		public final PROB SHIELDBREAK = new PROB();
		@Order(11)
		public final WARP WARP = new WARP();
		@Order(12)
		public final PT CURSE = new PT();
		@Order(13)
		public final PT SEAL = new PT();
		@Order(14)
		public final SUMMON SUMMON = new SUMMON();
		@Order(15)
		public final TIME TIME = new TIME();
		@Order(16)
		public final PROB SNIPER = new PROB();
		@Order(17)
		public final THEME THEME = new THEME();
		@Order(18)
		public final PROB BOSS = new PROB();
		@Order(19)
		public final POISON POISON = new POISON();
		@Order(20)
		public final PM SATK = new PM();
		@Order(21)
		public final PM POIATK = new PM();
		@Order(22)
		public final PTMS ARMOR = new PTMS();
		@Order(23)
		public final SPEED SPEED = new SPEED();
		@Order(24)
		public final STRONG STRONG = new STRONG();
		@Order(25)
		public final PROB LETHAL = new PROB();
		@Order(26)
		public final IMU IMUKB = new IMU();
		@Order(27)
		public final IMU IMUSTOP = new IMU();
		@Order(28)
		public final IMU IMUSLOW = new IMU();
		@Order(29)
		public final IMUI IMUWAVE = new IMUI();
		@Order(30)
		public final IMUI IMUVOLC = new IMUI();
		@Order(31)
		public final IMUAD IMUWEAK = new IMUAD();
		@Order(32)
		public final IMU IMUWARP = new IMU();
		@Order(33)
		public final IMU IMUCURSE = new IMU();
		@Order(34)
		public final IMU IMUSEAL = new IMU();
		@Order(35)
		public final IMU IMUSUMMON = new IMU();
		@Order(36)
		public final IMUAD IMUPOI = new IMUAD();
		@Order(37)
		public final IMU IMUPOIATK = new IMU();
		@Order(38)
		public final MOVEI IMUMOVING = new MOVEI();
		@Order(39)
		public final CANNI IMUCANNON = new CANNI();
		@Order(40)
		public final IMUAD IMUARMOR = new IMUAD();
		@Order(41)
		public final IMUAD IMUSPEED = new IMUAD();
		@Order(42)
		public final IMU CRITI = new IMU();
		@Order(43)
		public final COUNTER COUNTER = new COUNTER();
		@Order(44)
		public final IMUATK IMUATK = new IMUATK();
		@Order(45)
		public final DMGCUT DMGCUT = new DMGCUT();
		@Order(46)
		public final DMGCAP DMGCAP = new DMGCAP();
		@Order(47)
		public final BURROW BURROW = new BURROW();
		@Order(48)
		public final REVIVE REVIVE = new REVIVE();
		@Order(49)
		public final BARRIER BARRIER = new BARRIER();
		@Order(50)
		public final DSHIELD DEMONSHIELD = new DSHIELD();
		@Order(51)
        public final VOLC DEATHSURGE = new VOLC();
		@Order(52)
		public final MULT BOUNTY = new MULT();
		@Order(53)
		public final MULT ATKBASE = new MULT();
		@Order(54)
		public final BSTHUNT BSTHUNT = new BSTHUNT();
		@Order(55)
		public final WORKLV WORKERLV = new WORKLV();
		@Order(56)
		public final CDSETTER CDSETTER = new CDSETTER();
		@Order(57)
		public final AURA WEAKAURA = new AURA();
		@Order(58)
		public final AURA STRONGAURA = new AURA();
		@Order(59)
		public final LETHARGY LETHARGY = new LETHARGY();
		@Order(60)
		public final IMUAD IMULETHARGY = new IMUAD();
		@Order(61)
		public final REMOTESHIELD REMOTESHIELD = new REMOTESHIELD();
		@Order(62)
		public final AI AI = new AI();
		@Order(63)
		public final PT RAGE = new PT();
		@Order(64)
		public final PT HYPNO = new PT();
		@Order(65)
		public final IMU IMURAGE = new IMU();
		@Order(66)
		public final IMU IMUHYPNO = new IMU();
		@Order(67)
		public final MINIVOLC MINIVOLC = new MINIVOLC();
		@Order(68)
		public final PM DEMONVOLC = new PM();
		@Order(69)
		public final STATINC DMGINC = new STATINC(); //Merges Strong against, Massive Damage, and Insane Damage
		@Order(70)
		public final STATINC DEFINC = new STATINC(); //Merges Strong against, Resistant, and Insane Resist
		@Order(71)
		public final RANGESHIELD RANGESHIELD = new RANGESHIELD();
		@Order(72)
		public final SPIRIT SPIRIT = new SPIRIT();
		@Order(73)
		public final MULT METALKILL = new MULT();
		@Order(74)
		public final BLAST BLAST = new BLAST();
		@Order(75)
		public final IMUI IMUBLAST = new IMUI();
		@Order(76)
		public final PM DRAIN = new PM();
		@Order(77)
		public final BLESSING BLESSING = new BLESSING();
		@Order(78)
		public final STRONG SPEEDUP = new STRONG();
		@Order(79)
		public final MINIVOLC MINIDEATHSURGE = new MINIVOLC();
		@Order(80)
		public final REFUND REFUND = new REFUND();

		@Override
		public Proc clone() {
			try {
				Proc ans = new Proc();
				for (Field f : getDeclaredFields()) {
					f.setAccessible(true);
					if(f.get(this) != null)
						f.set(ans, ((ProcItem) f.get(this)).clone());
					f.setAccessible(false);
				}
				return ans;
			} catch (Exception e) {
				CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Error cloning proc");
				return null;
			}
		}

		public boolean isBlank() {
			for (int i = 0; i < PROC_TOT; i++)
				if (getArr(i).def_exists())
					return false;
			return true;
		}

		public ProcItem get(String id) {
			try {
				return (ProcItem) Proc.class.getField(id).get(this);
			} catch (Exception e) {
				CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Couldn't get proc " + id);
				return null;
			}
		}

		public ProcItem getArr(int i) {
			try {
				return (ProcItem) getDeclaredFields()[i].get(this);
			} catch (Exception e) {
				CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Couldn't get proc " + i);
				return null;
			}
		}

		public static boolean sharable(int i) {
			if(i < procSharable.length)
				return procSharable[i];
			System.out.println("Warning : "+i+" is out of index of procSharable");
			return false;
		}

		@Override
		public String toString() {
			return JsonEncoder.encode(this).toString();
		}

		/**
		 * Used to parse procs into pack data
		 * @return The encoded proc as json object
		 */
		@SuppressWarnings("unused")
		public JsonObject serProc() {
			JsonObject obj = new JsonObject();

			for(Field f : getDeclaredFields()) {
				try {
					String tag = f.getName();
					ProcItem proc = (ProcItem) f.get(this);

					if(proc.def_exists())
						obj.add(tag, JsonEncoder.encode(proc));
				} catch (IllegalAccessException e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Couldn't serializa proc " + f.getName());
				}
			}

			return obj;
		}

		/**
		 * Used to generate procs from the pack's json data
		 * @param elem Json Component containing the proc's data
		 * @return The decoded proc
		 */
		@SuppressWarnings("unused")
		public static Proc genProc(JsonElement elem) {
			Proc proc = Proc.blank();
			if(elem == null)
				return proc;

			JsonObject obj = elem.getAsJsonObject();
			if(obj == null)
				return proc;

			for(Field f : getDeclaredFields()) {
				String tag = f.getName();
				try {
					if(obj.has(tag) && !obj.get(tag).isJsonNull()) {
						f.setAccessible(true);
						f.set(proc, JsonDecoder.decode(obj.get(tag), f.getType()));
					}
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Couldn't generate proc " + f.getName());
				}
			}

			return proc;
		}

	}

	public static final byte restrict_name = 32;
	public static final byte SE_VICTORY = 8;
	public static final byte SE_DEFEAT = 9;
	public static final byte SE_HIT_0 = 20;
	public static final byte SE_HIT_1 = 21;
	public static final byte SE_DEATH_0 = 23;
	public static final byte SE_DEATH_1 = 24;
	public static final byte SE_HIT_BASE = 22;
	public static final byte SE_ZKILL = 59;
	public static final byte SE_CRIT = 44;
	public static final byte SE_SATK = 90;
	public static final byte SE_WAVE = 26;
	public static final byte SE_LETHAL = 50;
	public static final byte SE_P_WORKERLVUP = 53;
	public static final byte SE_P_WORKERLVDOWN = 107;
	public static final byte SE_P_RESEARCHUP = 18;
	public static final byte SE_P_RESEARCHDOWN = 36;
	public static final byte SE_WARP_ENTER = 73;
	public static final byte SE_WARP_EXIT = 74;
	public static final byte SE_BOSS = 45;
	public static final byte SE_SPEND_FAIL = 15;
	public static final byte SE_SPEND_SUC = 19;
	public static final byte SE_SPEND_REF = 27;
	public static final byte SE_RANGESHIELD = 17;
	public static final byte SE_CANNON_CHARGE = 28;
	public static final byte SE_BARRIER_ABI = 70;
	public static final byte SE_BARRIER_NON = 71;
	public static final byte SE_BARRIER_ATK = 72;
	public static final byte SE_POISON = 110;
	public static final byte SE_VOLC_START = 111;
	public static final byte SE_VOLC_LOOP = 112;
	public static final short SE_SHIELD_HIT = 136;
	public static final short SE_SHIELD_BROKEN = 139;
	public static final short SE_SHIELD_REGEN = 138;
	public static final short SE_SHIELD_BREAKER = 137;
	public static final short SE_DEATH_SURGE = 143;
	public static final short SE_COUNTER_SURGE = 159;
	public static final short SE_SPIRIT_SUMMON = 162;

	public static final byte[][] SE_CANNON = { { 25, 26 }, { 60 }, { 61 }, { 36, 37 }, { 65, 83 }, { 84, 85 }, { 86 },
			{ 124 } };

	public static final short[] SE_ALL = { SE_VICTORY, SE_DEFEAT, SE_SPEND_FAIL, SE_RANGESHIELD, SE_SPEND_SUC, SE_HIT_0, SE_HIT_1, SE_HIT_BASE, SE_DEATH_0, SE_DEATH_1, 25, 26, SE_SPEND_REF,
			SE_CANNON_CHARGE, 37, 44, 45, 50, 59, 60, 61, 65, 73, 74, 83, 84, 85, 86, 90, SE_POISON, SE_VOLC_START, SE_VOLC_LOOP, 124, SE_SHIELD_HIT, SE_SHIELD_BREAKER, SE_SHIELD_REGEN,
			SE_SHIELD_BROKEN, SE_DEATH_SURGE, SE_COUNTER_SURGE };

	public static final byte RARITY_TOT = 6;

	// trait bit filter
	public static final byte TB_RED = 1;
	public static final byte TB_FLOAT = 2;
	public static final byte TB_BLACK = 4;
	public static final byte TB_METAL = 8;
	public static final byte TB_ANGEL = 16;
	public static final byte TB_ALIEN = 32;
	public static final byte TB_ZOMBIE = 64;
	public static final short TB_RELIC = 128;
	public static final short TB_WHITE = 256;
	public static final short TB_EVA = 512;
	public static final short TB_WITCH = 1024;
	public static final short TB_INFH = 2048;
	public static final short TB_DEMON = 4096, TB_DEMON_T = 2048;

	// trait index
	public static final byte TRAIT_RED = 0;
	public static final byte TRAIT_FLOAT = 1;
	public static final byte TRAIT_BLACK = 2;
	public static final byte TRAIT_METAL = 3;
	public static final byte TRAIT_ANGEL = 4;
	public static final byte TRAIT_ALIEN = 5;
	public static final byte TRAIT_ZOMBIE = 6;
	public static final byte TRAIT_DEMON = 7;
	public static final byte TRAIT_RELIC = 8;
	public static final byte TRAIT_WHITE = 9;
	public static final byte TRAIT_EVA = 10;
	public static final byte TRAIT_WITCH = 11;
	public static final byte TRAIT_BARON = 12;
	public static final byte TRAIT_BEAST = 13;
	public static final byte TRAIT_SAGE = 14;
	public static final byte TRAIT_INFH = 15;
	public static final byte TRAIT_TOT = 16;

	// treasure
	public static final byte T_RED = 0;
	public static final byte T_FLOAT = 1;
	public static final byte T_BLACK = 2;
	public static final byte T_ANGEL = 3;
	public static final byte T_METAL = 4;
	public static final byte T_ALIEN = 5;
	public static final byte T_ZOMBIE = 6;

	// default tech value
	public static final int[] MLV = new int[] { 30, 30, 30, 30, 30, 30, 30, 10, 30 };

	// tech index
	public static final byte LV_RES = 0;
	public static final byte LV_ACC = 1;
	public static final byte LV_BASE = 2;
	public static final byte LV_WORK = 3;
	public static final byte LV_WALT = 4;
	public static final byte LV_RECH = 5;
	public static final byte LV_CATK = 6;
	public static final byte LV_CRG = 7;
	public static final int LV_XP = 8;
	public static final byte LV_TOT = 9;

	// default treasure value
	public static final int[] MT = new int[] { 300, 300, 300, 300, 300, 300, 600, 600, 600, 300, 300 };

	// treasure index
	public static final byte T_ATK = 0;
	public static final byte T_DEF = 1;
	public static final byte T_RES = 2;
	public static final byte T_ACC = 3;
	public static final byte T_WORK = 4;
	public static final byte T_WALT = 5;
	public static final byte T_RECH = 6;
	public static final byte T_CATK = 7;
	public static final byte T_BASE = 8;
	public static final byte T_XP1 = 9;
	public static final byte T_XP2 = 10;
	public static final byte T_TOT = 11;

	// abi bit filter
	public static final byte AB_ONLY = 1;
	public static final byte AB_METALIC = 1 << 1;
	public static final byte AB_SNIPERI = 1 << 2;
	public static final byte AB_TIMEI = 1 << 3;
	public static final byte AB_GHOST = 1 << 4;
	public static final byte AB_ZKILL = 1 << 5;
	public static final byte AB_WKILL = 1 << 6;
	public static final short AB_GLASS = 1 << 7;
	public static final short AB_THEMEI = 1 << 8;
	public static final short AB_EKILL = 1 << 9;
	public static final short AB_IMUSW = 1 << 10;
	public static final short AB_BAKILL = 1 << 11;
	public static final short AB_CKILL = 1 << 12;
	public static final int AB_SKILL = 1 << 13;

	public static final byte ABI_ONLY = 0;
	public static final byte ABI_METALIC = 1;
	public static final byte ABI_SNIPERI = 2;
	public static final byte ABI_TIMEI = 3;
	public static final byte ABI_GHOST = 4;
	public static final byte ABI_ZKILL = 5;
	public static final byte ABI_WKILL = 6;
	public static final byte ABI_GLASS = 7;
	public static final byte ABI_THEMEI = 8;
	public static final byte ABI_EKILL = 9;
	public static final byte ABI_IMUSW = 10;
	public static final byte ABI_BAKILL = 11;
	public static final byte ABI_CKILL = 12;
	public static final byte ABI_SKILL = 13;
	public static final byte ABI_TOT = 14;// 18 currently

	// proc index
	public static final byte P_KB = 0;
	public static final byte P_STOP = 1;
	public static final byte P_SLOW = 2;
	public static final byte P_CRIT = 3;
	public static final byte P_WAVE = 4;
	public static final byte P_MINIWAVE = 5;
	public static final byte P_MOVEWAVE = 6;
	public static final byte P_VOLC = 7;
	public static final byte P_WEAK = 8;
	public static final byte P_BREAK = 9;
	public static final byte P_SHIELDBREAK = 10;
	public static final byte P_WARP = 11;
	public static final byte P_CURSE = 12;
	public static final byte P_SEAL = 13;
	public static final byte P_SUMMON = 14;
	/**
	 * 0:prob, 1:speed, 2:width (left to right), 3:time, 4:origin (center), 5:itv
	 */
	public static final byte P_TIME = 15;
	public static final byte P_SNIPER = 16;
	/**
	 * 0:prob, 1:time (-1 means infinite), 2:ID, 3: type 0 : Change only BG 1 : Kill
	 * all and change BG
	 */
	public static final byte P_THEME = 17;
	public static final byte P_BOSS = 18;
	/**
	 * 0:prob, 1:time, 2:dmg, 3:itv, 4: conf +0: normal, +1: of total, +2: of
	 * current, +3: of lost, +4: unstackable
	 */
	public static final byte P_POISON = 19;
	public static final byte P_SATK = 20;
	/**
	 * official poison
	 */
	public static final byte P_POIATK = 21;
	/**
	 * Make target receive n% damage more/less 0: chance, 1: duration, 2: debuff
	 */
	public static final byte P_ARMOR = 22;
	/**
	 * Make target move faster/slower 0: chance, 1: duration, 2: speed, 3: type
	 * 0: Current speed * (100 + n)% type 1: Current speed + n type 2: Fixed speed
	 */
	public static final byte P_SPEED = 23;
	public static final byte P_STRONG = 24;
	public static final byte P_LETHAL = 25;
	public static final byte P_IMUKB = 26;
	public static final byte P_IMUSTOP = 27;
	public static final byte P_IMUSLOW = 28;
	public static final byte P_IMUWAVE = 29;
	public static final byte P_IMUVOLC = 30;
	public static final byte P_IMUWEAK = 31;
	public static final byte P_IMUWARP = 32;
	public static final byte P_IMUCURSE = 33;
	public static final byte P_IMUSEAL = 34;
	public static final byte P_IMUSUMMON = 35;
	public static final byte P_IMUPOI = 36;
	public static final byte P_IMUPOIATK = 37;
	public static final byte P_IMUMOVING = 38;
	public static final byte P_IMUCANNON = 39;
	public static final byte P_IMUARMOR = 40;
	public static final byte P_IMUSPEED = 41;
	public static final byte P_CRITI = 42;
	public static final byte P_COUNTER = 43;
	public static final byte P_IMUATK = 44;
	public static final byte P_DMGCUT = 45;
	public static final byte P_DMGCAP = 46;
	public static final byte P_BURROW = 47;
	/**
	 * body proc: 0: add revive time for zombies, -1 to make it infinite, revivable
	 * zombies only 1: revive time 2: revive health 3: point 1 4: point 2 5: type:
	 * 0/1/2/3: duration: in range and normal/in range/ master lifetime/permanent
	 * +4: make Z-kill unusable +8: revive non-zombie also +16: applicapable to
	 * others
	 */
	public static final byte P_REVIVE = 48;
	public static final byte P_BARRIER = 49;
	public static final byte P_DEMONSHIELD = 50;
	public static final byte P_DEATHSURGE = 51;
	public static final byte P_BOUNTY = 52;
	public static final byte P_ATKBASE = 53;
	public static final byte P_BSTHUNT = 54; //Beast Killer
	public static final byte P_WORKERLV = 55;
	public static final byte P_CDSETTER = 56;
	public static final byte P_WEAKAURA = 57;
	public static final byte P_STRONGAURA = 58;
	public static final byte P_LETHARGY = 59;
	public static final byte P_IMULETHARGY = 60;
	public static final byte P_REMOTESHIELD = 61;
	public static final byte P_AI = 62;
	public static final byte P_RAGE = 63;
	public static final byte P_HYPNO = 64;
	public static final byte P_IMURAGE = 65;
	public static final byte P_IMUHYPNO = 66;
	public static final byte P_MINIVOLC = 67;
	public static final byte P_DEMONVOLC = 68;
	public static final byte P_DMGINC = 69; // nice
	public static final byte P_DEFINC = 70;
	public static final byte P_RANGESHIELD = 71;
	public static final byte P_SPIRIT = 72;
	public static final byte P_METALKILL = 73;
	public static final byte P_BLAST = 74;
	public static final byte P_IMUBLAST = 75;
	public static final byte P_DRAIN = 76;
	public static final byte P_BLESS = 77;
	public static final byte P_SPEEDUP = 78;
	public static final byte P_MINIDEATHSURGE = 79;
	public static final byte P_REFUND = 80;
	public static final byte PROC_TOT = 81;

	public static final boolean[] procSharable = {
			false, //kb
			false, //freeze
			false, //slow
			false, //critical
			false, //wave
			false, //miniwave
			false, //move wave
			false, //volcano
			false, //weaken
			false, //barrier breaker
			false, //shield breaker
			false, //warp
			false, //curse
			false, //seal
			false, //summon
			false, //time
			false, //sniper
			false, //theme
			false, //boss wave
			false, //venom
			false, //savage blow
			false, //poison
			false, //armor
			false, //haste
			true,  //strengthen
			true,  //survive
			true,  //imu.kb
			true,  //imu.freeze
			true,  //imu.slow
			true,  //imu.wave
			true,  //imu.volcano
			true,  //imu.weaken
			true,  //imu.warp
			true,  //imu.curse
			true,  //imu.seal
			true,  //imu.summon
			true,  //imu.BCU poison
			true,  //imu.poison
			true,  //imu.moving atk
			true,  //imu.cannon
			true,  //imu.armor break
			true,  //imu.haste
			true,  //imu. critical
			true,  //invincibility
			true,  //damage cut
			true,  //damage cap
			true,  //counter
			true,  //burrow
			true,  //revive
			true,  //barrier
			true,  //demon barrier
			true,  //death surge
			false, //2x money
			false, //base destroyer
			true,  //beast hunter
			false, //Worker change
			false, //Cooldown change
			true,  //Weaken Aura
			true,  //Strengthen Aura
			false, //Lethargy
			true,  //Imu.Lethargy
			true,  //Remote shield
			true,  //AI
			false, //Rage
			false, //Hypno
			true,  //Imu. Rage
			true,  // Imu Hypno
			false, //Mini surge
			true,  //Counter Volc
			true,  //Massive DMG but good
			true,  //Resistant but good
			true,  //Range Shield
			true,  //spirit summon
			false, //TOTAL METALHEAD DEATH
			false, //BAJA BLAST
			true,  //imu.blast
			false, //Drain/ABsorb
			false, //Bless
			true,  //adrenaline
			true,  //Mini Death Surge
			true   //Refund
	};

	/**
	 * Procs in here are shareable on any hit for BC entities, but not shareable for custom entities
	 */
	public static final int[] BCShareable = { P_BOUNTY, P_ATKBASE };
	public static boolean bcShareable(int i) {
		for (int bc : BCShareable)
			if (bc == i)
				return true;
		return false;
	}

	public static final byte WT_WAVE = 1;
	public static final byte WT_MOVE = 2;
	public static final byte WT_CANN = 2;
	public static final byte WT_VOLC = 4;
	public static final byte WT_MINI = 8;
	public static final byte WT_MIVC = 16;
	public static final byte WT_MEGA = 64;
	public static final byte WT_BLST = 32;
	public static final byte PC_P = 0, PC_AB = 1, PC_BASE = 2, PC_IMU = 3, PC_TRAIT = 4;
	public static final byte PC2_HP = 0;
	public static final byte PC2_ATK = 1;
	public static final byte PC2_SPEED = 2;
	public static final byte PC2_COST = 3;
	public static final byte PC2_CD = 4;
	public static final byte PC2_HB = 5;
	public static final byte PC2_TBA = 6;
	public static final byte PC2_RNG = 7;
	public static final byte PC2_TOT = 8;
	// -1 for None
	// 0 for Proc
	// 1 for Ability
	// 2 for Base stat
	// 3 for Immune
	// 4 for Trait
	// 5 for special cases
	public static final int[][] PC_CORRES = new int[][] { // NP value table
			{ -1, 0 }, // 0:
			{ PC_P, P_WEAK }, // 1: weak, reversed health or relic-weak
			{ PC_P, P_STOP }, // 2: stop
			{ PC_P, P_SLOW }, // 3: slow
			{ PC_AB, AB_ONLY }, // 4:Target Obnly
			{ 5, P_DMGINC, 150 }, // 5:Strong Vs.
			{ 5, P_DEFINC, 400 }, // 6:Resistant
			{ 5, P_DMGINC, 300 }, // 7:Massive Dmg
			{ PC_P, P_KB }, // 8: kb
			{ PC_P, P_WARP }, // 9:Warp
			{ PC_P, P_STRONG }, // 10: berserker, reversed health
			{ PC_P, P_LETHAL }, // 11: lethal
			{ PC_P, P_ATKBASE }, // 12: Base Destroyer
			{ PC_P, P_CRIT }, // 13: crit
			{ PC_AB, AB_ZKILL }, // 14: zkill
			{ PC_P, P_BREAK }, // 15: break
			{ PC_P, P_BOUNTY }, // 16: 2x income
			{ PC_P, P_WAVE }, // 17: wave
			{ PC_P, P_IMUWEAK }, // 18: res weak
			{ PC_P, P_IMUSTOP }, // 19: res stop
			{ PC_P, P_IMUSLOW }, // 20: res slow
			{ PC_P, P_IMUKB }, // 21: res kb
			{ PC_P, P_IMUWAVE }, // 22: res wave
			{ 5, P_IMUWAVE }, // 23: waveblock
			{ PC_P, P_IMUWARP }, // 24: res warp
			{ PC_BASE, PC2_COST }, // 25: reduce cost
			{ PC_BASE, PC2_CD }, // 26: reduce cooldown
			{ PC_BASE, PC2_SPEED }, // 27: inc speed
			{ PC_BASE, PC2_HB }, // 28: inc knockbacks
			{ PC_IMU, P_IMUCURSE }, // 29: imu curse
			{ PC_P, P_IMUCURSE }, // 30: res curse
			{ PC_BASE, PC2_ATK }, // 31: inc ATK
			{ PC_BASE, PC2_HP }, // 32: inc HP
			{ PC_TRAIT, TRAIT_RED }, // 33: target red
			{ PC_TRAIT, TRAIT_FLOAT }, // 34: target floating
			{ PC_TRAIT, TRAIT_BLACK }, // 35: target black
			{ PC_TRAIT, TRAIT_METAL }, // 36: target metal
			{ PC_TRAIT, TRAIT_ANGEL }, // 37: target angel
			{ PC_TRAIT, TRAIT_ALIEN }, // 38: target alien
			{ PC_TRAIT, TRAIT_ZOMBIE }, // 39: target zombie
			{ PC_TRAIT, TRAIT_RELIC }, // 40: target relic
			{ PC_TRAIT, TRAIT_WHITE }, // 41: target white
			{ PC_TRAIT, TRAIT_WITCH }, // 42: target witch
			{ PC_TRAIT, TRAIT_EVA }, // 43: target EVA
			{ PC_IMU, P_IMUWEAK }, // 44: immune to weak
			{ PC_IMU, P_IMUSTOP }, // 45: immune to freeze
			{ PC_IMU, P_IMUSLOW }, // 46: immune to slow
			{ PC_IMU, P_IMUKB }, // 47: immune to kb
			{ PC_IMU, P_IMUWAVE }, // 48: immune to wave
			{ PC_IMU, P_IMUWARP }, // 49: immune to warp
			{ PC_P, P_SATK }, // 50: savage blow
			{ PC_P, P_IMUATK }, // 51: dodge
			{ PC_P, P_IMUPOIATK }, // 52: resist to poison ?
			{ PC_IMU, P_IMUPOIATK }, // 53: immune to poison
			{ PC_P, P_IMUVOLC }, // 54: resist to surge ?
			{ PC_IMU, P_IMUVOLC }, // 55: immune to surge
			{ PC_P, P_VOLC }, // 56: surge, level up to chance up
			{ PC_TRAIT, TRAIT_DEMON }, // 57: Targetting Aku
			{ PC_P, P_SHIELDBREAK }, //58 : shield piercing
			{ PC_AB, AB_CKILL }, //59 : corpse killer
			{ PC_P, P_CURSE }, //60 : curse
			{ PC_BASE, PC2_TBA }, //61 : tba
			{ PC_P, P_MINIWAVE }, //62 : mini-wave
			{ PC_AB, AB_BAKILL }, //63 : baron killer
			{ PC_P, P_BSTHUNT, 1 }, //64 : behemoth slayer
			{ PC_P, P_MINIVOLC }, //65 : MiniSurge
			{ PC_AB, AB_SKILL }, //66 : super sage hunter
			{ PC_P, P_BLAST } //67 : Blast but labeled 74 above. Unsure which one is correct.
	};
	public static final int[][] PC_CUSTOM = new int[][] { //Use negative ints to handle
			{ -1, 0 }, // 0:
			{ PC_P, P_BURROW}, // 1: Burrow
			{ PC_P, P_REVIVE}, //2: Revive
			{ PC_P, P_BARRIER}, //3: Barrier
			{ PC_P, P_DEMONSHIELD}, //4: Aku Shield
			{ PC_P, P_DEATHSURGE}, //5: Death Surge
			{ PC_P, P_DEMONVOLC}, //6: Surge Counter
			{ PC_P, P_SEAL}, // 7: Seal
			{ PC_P, P_COUNTER}, // 8: Counter
			{ PC_P, P_DMGCUT}, // 9: Super Armor
			{ PC_P, P_DMGCAP}, // 10: Mystic Shield
			{ PC_P, P_REMOTESHIELD}, // 11: Remote Shield
			{ PC_P, P_ARMOR}, // 12: Armor break
			{ PC_P, P_SPEED}, // 13: Haste
			{ PC_P, P_RAGE}, // 14: Rage
			{ PC_P, P_HYPNO}, // 15: Hypno
			{ PC_P, P_CRITI}, // 16: Criti
			{ PC_P, P_IMUSUMMON}, // 17: Summon immune
			{ PC_P, P_IMUSEAL}, // 18: Seal immune
			{ PC_P, P_IMUARMOR}, // 19: Armor Break immune
			{ PC_P, P_IMUSPEED}, // 20: Haste immune
			{ PC_P, P_IMULETHARGY}, // 21: Lethargy Immunity
			{ PC_P, P_IMURAGE}, // 22: Rage Immunity
			{ PC_P, P_IMUHYPNO}, // 23: Hypno Immunity
			{ PC_BASE, PC2_RNG}, // 24: Range
			{ PC_P, P_POIATK}, // 25: Toxic
			{ PC_P, P_IMUPOI}, // 26: Imu. BCU Poison
			{ PC_AB, AB_SNIPERI}, // 27: IMU.Sniper
			{ PC_AB, AB_TIMEI}, // 28: IMU.TimeStop
			{ PC_AB, AB_THEMEI}, // 29: IMU.Theme
			{ PC_AB, AB_IMUSW}, // 30: IMU.BossWave
			{ PC_P, P_LETHARGY}, // 31: Lethargy
			{ PC_P, P_SNIPER}, // 32: Sniper KB
			{ PC_P, P_BOSS}, // 33: Bosswave
			{ PC_P, P_TIME}, // 34: Timestop
			{ PC_P, P_IMUMOVING}, // 35: IMU.MoveATK
			{ PC_P, P_WEAKAURA}, // 36: WeakenAura
			{ PC_P, P_STRONGAURA}, // 37: StrengthAura
			{ PC_P, P_DMGINC}, // 38: ExtraDmg
			{ PC_P, P_DEFINC},  // 39: Resistance
			{ PC_P, P_RANGESHIELD}, //40: Range Shield
			{ PC_P, P_SPIRIT}, //41: Spirit summon
			{ PC_P, P_DRAIN}, //42: Drain
			{ PC_P, P_SPEEDUP}, //43: Adrenaline
			{ PC_P, P_REFUND}, //44: Refund
			{ PC_P, P_MINIDEATHSURGE} //45: Mini-Deathsurge
	};

	public static int[] get_CORRES(int ind) {
		if (ind < 0)
			return PC_CUSTOM[Math.abs(ind)];

		if (ind >= PC_CORRES.length)
			return PC_CORRES[0];
		return PC_CORRES[ind];
	}

	// foot icon index used in battle
	public static final byte INV = -1;
	public static final byte INVWARP = -2;
	public static final byte STPWAVE = -3;
	public static final byte BREAK_ABI = -4;
	public static final byte BREAK_ATK = -5;
	public static final byte BREAK_NON = -6;
	public static final byte HEAL = -7;
	public static final byte SHIELD_HIT = -8;
	public static final byte SHIELD_BROKEN = -9;
	public static final byte SHIELD_REGEN = -10;
	public static final byte SHIELD_BREAKER = -11;
	public static final byte DMGCAP_FAIL = -12;
	public static final byte DMGCAP_SUCCESS = -13;
	public static final byte REMSHIELD_NEAR = -14;
	public static final byte REMSHIELD_FAR = -15;
	public static final byte A_WEAKAURASTR = -16;
	public static final byte A_STRAURAWEAK = -17;
	public static final byte RANGESHIELD_SINGLE = -18;
	public static final byte A_GUARD = -19;
	public static final byte A_GUARD_BRK = -20;

	// Combo index
	public static final byte C_ATK = 0;
	public static final byte C_DEF = 1;
	public static final byte C_SPE = 2;
	public static final byte C_GOOD = 14;
	public static final byte C_MASSIVE = 15;
	public static final byte C_RESIST = 16;
	public static final byte C_KB = 17;
	public static final byte C_SLOW = 18;
	public static final byte C_STOP = 19;
	public static final byte C_WEAK = 20;
	public static final byte C_STRONG = 21;
	public static final byte C_WKILL = 22;
	public static final byte C_EKILL = 23;
	public static final byte C_CRIT = 24;
	public static final byte C_C_INI = 3;
	public static final byte C_C_ATK = 6;
	public static final byte C_C_SPE = 7;
	public static final byte C_BASE = 10;
	public static final byte C_M_INI = 5;
	public static final byte C_M_LV = 4;
	public static final byte C_M_INC = 8;
	public static final byte C_M_MAX = 9;
	public static final byte C_RESP = 11;
	public static final byte C_MEAR = 12;
	public static final byte C_TOT = 25;

	// Effects Anim index
	public static final byte A_DOWN = 0;
	public static final byte A_UP = 1;
	public static final byte A_SLOW = 2;
	public static final byte A_STOP = 3;
	public static final byte A_CURSE = 4;
	public static final byte A_SHIELD = 5;
	public static final byte A_FARATTACK = 6;
	public static final byte A_WAVE_INVALID = 7;
	public static final byte A_WAVE_STOP = 8;
	public static final byte A_EFF_INV = 9;
	public static final byte A_B = 10;
	public static final byte A_SEAL = 11;
	public static final byte A_POI0 = 12;
	public static final byte A_POI1 = 13;
	public static final byte A_POI2 = 14;
	public static final byte A_POI3 = 15;
	public static final byte A_POI4 = 16;
	public static final byte A_POI5 = 17;
	public static final byte A_POI6 = 18;
	public static final byte A_POI7 = 19;
	public static final byte[] A_POIS = { A_POI0, A_POI1, A_POI2, A_POI3, A_POI4, A_POI5, A_POI6, A_POI7 };
	public static final byte A_IMUATK = 20;
	public static final byte A_ARMOR = 21;
	public static final byte A_SPEED = 22;
	public static final byte A_HEAL = 23;
	public static final byte A_DEMON_SHIELD = 24;
	public static final byte A_COUNTER = 25;
	public static final byte A_DMGCUT = 26;
	public static final byte A_DMGCAP = 27;
	public static final byte A_LETHARGY = 28;
	public static final byte A_REMSHIELD = 29;
	public static final byte A_WEAKAURA = 30;
	public static final byte A_STRONGAURA = 31;
	public static final byte A_RAGE = 32;
	public static final byte A_HYPNO = 33;
	public static final byte A_RANGESHIELD = 34;
	public static final byte A_DRAIN = 35;
	public static final byte A_BLESS = 36;
	public static final byte A_DRENALINE = 37;
	public static final byte A_TOT = 38;

	// atk type index used in filter page
	public static final byte ATK_SINGLE = 0;
	public static final byte ATK_AREA = 1;
	public static final byte ATK_LD = 2;
	public static final byte ATK_OMNI = 4;
	public static final byte ATK_TOT = 12;

	// base and canon level
	public static final byte BASE_H = 0;
	public static final byte BASE_SLOW = 1;
	public static final byte BASE_WALL = 2;
	public static final byte BASE_STOP = 3;
	public static final byte BASE_WATER = 4;
	public static final byte BASE_GROUND = 5;
	public static final byte BASE_BARRIER = 6;
	public static final byte BASE_CURSE = 7;
	public static final byte BASE_TOT = 8;

	// base type
	public static final byte BASE_ATK_MAGNIFICATION = 0;
	public static final byte BASE_SLOW_TIME = 1;
	public static final byte BASE_TIME = 2;
	public static final byte BASE_WALL_MAGNIFICATION = 3;
	public static final byte BASE_WALL_ALIVE_TIME = 4;
	public static final byte BASE_RANGE = 5;
	//Figure out type 6
	public static final byte BASE_HEALTH_PERCENTAGE = 7;
	//Figure out type 8
	public static final byte BASE_HOLY_ATK_SURFACE = 9;
	public static final byte BASE_HOLY_ATK_UNDERGROUND = 10;
	//Figure out type 11
	public static final byte BASE_CURSE_TIME = 12;

	// decoration/base level
	public static final byte DECO_BASE_SLOW = 1;
	public static final byte DECO_BASE_WALL = 2;
	public static final byte DECO_BASE_STOP = 3;
	public static final byte DECO_BASE_WATER = 4;
	public static final byte DECO_BASE_GROUND = 5;
	public static final byte DECO_BASE_BARRIER = 6;
	public static final byte DECO_BASE_CURSE = 7;
	public static final byte DECO_BASE_TOT = 7;

	public static final byte[] DECOS = new byte[]{P_SLOW, -1, P_STOP, -1, P_WEAK, P_POIATK, P_CURSE}; //-1s are wave and surge (in that order)

	// touchable ID
	public static final byte TCH_N = 1;
	public static final byte TCH_KB = 2;
	public static final byte TCH_UG = 4;
	public static final byte TCH_CORPSE = 8;
	public static final byte TCH_SOUL = 16;
	public static final byte TCH_EX = 32;
	public static final byte TCH_ZOMBX = 64;
	public static final short TCH_ENTER = 128;

	public static final String[] A_PATH = new String[] { "down", "up", "slow", "stop", "shield", "farattack",
			"wave_invalid", "wave_stop", "waveguard" };

	// After this line all number is game data

	public static final byte INT_KB = 0, INT_HB = 1, INT_SW = 2, INT_ASS = 3, INT_WARP = 4;

	public static final byte[] KB_PRI = new byte[] { 2, 4, 5, 1, 3 };
	public static final byte[] KB_TIME = new byte[] { 11, 23, 47, 11, -1 };
	public static final short[] KB_DIS = new short[] { 165, 345, 705, 55, -1 };

	public static final float W_E_INI = -32.75f;
	public static final float W_U_INI = -67.5f;
	public static final short W_PROG = 200;
	public static final short W_E_WID = 500;
	public static final short W_U_WID = 400;
	public static final byte W_TIME = 3;
	public static final byte W_MINI_TIME = 1; // mini wave spawn interval
	public static final byte W_MEGA_TIME = 6;
	public static final short W_VOLC = 375;
	public static final short W_VOLC_INNER = 250; // volcano inner width
	public static final byte W_VOLC_PIERCE = 125; // volcano pierce width
	public static final byte VOLC_ITV = 20;

	public static final byte VOLC_PRE = 15; // volcano pre-atk
	public static final byte VOLC_POST = 10; // volcano post-atk
	public static final byte VOLC_SE = 30; // volcano se loop duration

	public static final byte BLAST_ITV = 10;
	public static final byte BLAST_PRE = 11;
	public static final byte BLAST_DURATION = 15;
	public static final short EXPLOSION_SE = 167;

	public static final byte[] NYPRE = new byte[] { 18, 1, -1, 27, 37, 18, 10, 1 };
	public static final float[] NYRAN = new float[] { 400, 82.5f, -1, 500, 500, 400, 100, 82.5f };
	public static final short SNIPER_CD = 300;
	public static final byte SNIPER_PRE = 10;
	public static final float SNIPER_POS = 442.5f;
	public static final byte REVIVE_SHOW_TIME = 14;

	public static final byte ORB_ATK = 0;
	public static final byte ORB_RES = 1;
	public static final byte ORB_STRONG = 2;
	public static final byte ORB_MASSIVE = 3;
	public static final byte ORB_RESISTANT = 4;
	public static final byte ORB_MINIDEATHSURGE = 5;
	public static final byte ORB_RESWAVE = 6;
	public static final byte ORB_REFUND = 7;
	public static final byte ORB_RESKB = 8;
	public static final byte ORB_SOLBUFF = 9;
	public static final byte ORB_BAKILL = 10;
	public static final byte ORB_TYPE_TOTAL = 11;
	public static final byte ORB_TYPE = 0, ORB_TRAIT = 1, ORB_GRADE = 2, ORB_TOT = 3;

	public static final short[] GATYA = { 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 160, 161, 164, 167,
			168, 169, 170, 171, 179, 180, 181, 182, 183, 184};

	public static final int ORB_DEATH_SURGE_SPAWN_MIN = 200;
	public static final int ORB_DEATH_SURGE_SPAWN_MAX = 500;

	public static final short MUSIC_DELAY = 2344; //Music change delay with milliseconds accuracy

	public static final byte LINEUP_CHANGE_TIME = 6; //in frame

	public static final byte BG_EFFECT_STAR = 0;
	public static final byte BG_EFFECT_RAIN = 1;
	public static final byte BG_EFFECT_BUBBLE = 2;
	public static final byte BG_EFFECT_FALLING_SNOW = 3;
	public static final byte BG_EFFECT_SNOW = 4;
	public static final byte BG_EFFECT_SNOWSTAR = 5;
	public static final byte BG_EFFECT_BLIZZARD = 6;
	public static final byte BG_EFFECT_SHINING = 7;
	public static final byte BG_EFFECT_BALLOON = 8;
	public static final byte BG_EFFECT_ROCK = 9;

	//Below are completely guessed
	public static final int BG_EFFECT_STAR_TIME = 35;
	public static final short BG_EFFECT_STAR_Y_RANGE = 140;
	public static final byte BG_EFFECT_SPLASH_MIN_HEIGHT = 90;
	public static final byte BG_EFFECT_SPLASH_RANGE = 60;
	public static final short BG_EFFECT_BUBBLE_TIME = 780;
	public static final byte BG_EFFECT_BUBBLE_FACTOR = 32;
	public static final byte BG_EFFECT_BUBBLE_STABILIZER = 7;
	public static final byte BG_EFFECT_SNOW_SPEED = 8;
	public static final float[] BG_EFFECT_BLIZZARD_SIZE = {1.0f, 1.5f, 2.0f};
	public static final float BG_EFFECT_BLIZZARD_SPEED = 40;
	public static final byte BG_EFFECT_FALLING_SNOW_SPEED = 3;
	public static final float BG_EFFECT_FALLING_SNOW_SIZE = 2.0f;
	public static final byte BG_EFFECT_SHINING_TIME = 8;
	public static final float BG_EFFECT_BALLOON_SPEED = 1f;
	public static final byte BG_EFFECT_BALLOON_FACTOR = 32;
	public static final byte BG_EFFECT_BALLOON_STABILIZER = 25;
	public static final float[] BG_EFFECT_ROCK_SIZE = {1.0f, 2.25f};
	public static final byte[] BG_EFFECT_ROCK_SPEED = {1, 3};
	public static final short BG_EFFECT_ROCK_BEHIND_SPAWN_OFFSET = 190;

	public static final byte[] SHAKE_MODE_HIT = {5, 7, 2, 30};
	public static final byte[] SHAKE_MODE_BOSS = {10, 15, 2, 0};
	public static final byte SHAKE_DURATION = 0;
	public static final byte SHAKE_INITIAL = 1;
	public static final byte SHAKE_END = 2;
	public static final byte SHAKE_COOL_DOWN = 3;
	public static final float SHAKE_STABILIZER = 2.5f;
	public static final int COUNTER_SURGE_FORESWING = 50;
	public static final int COUNTER_SURGE_SOUND = 18;
	public static final int SPIRIT_SUMMON_RANGE = 150;
	public static final int SPIRIT_SUMMON_DELAY = 15; // unsure
	public static final float SUPER_SAGE_RESIST = 0.3f;
	public static final float SUPER_SAGE_HUNTER_ATTACK = 1.2f;
	public static final float SUPER_SAGE_HUNTER_HP = 0.5f;
	public static final float SUPER_SAGE_HUNTER_RESIST = 0.7f;

	public static final char[] SUFX = new char[] { 'f', 'c', 's', 'u' };

	public static EffAnim.EffAnimStore effas() {
		return CommonStatic.getBCAssets().effas;
	}

	/**
	 * convenient method to log an unexpected error. Don't use it to process any
	 * expected error
	 */
	public static boolean err(RunExc s) {
		return CommonStatic.ctx.noticeErr(s, ErrType.ERROR, "unexpected error");
	}

	/**
	 * convenient method to log an unexpected error. Don't use it to process any
	 * expected error.
	 */
	public static <T> T err(SupExc<T> s) {
		try {
			return s.get();
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.ERROR, "Unexpected Error: " + e + " in " + e.getStackTrace()[0].toString());
			return null;
		}
	}

	public static int getVer(String ver) {
		int ans = 0;
		int[] strs = CommonStatic.parseIntsN(ver);
		for (int str : strs) {
			ans *= 100;
			ans += str;
		}
		return ans;
	}

	public static String hex(int id) {
		return trio(id / 1000) + trio(id % 1000);
	}

	public static <T> T ignore(SupExc<T> sup) {
		try {
			return sup.get();
		} catch (Exception e) {
			return null;
		}
	}
	//Same as ignore. but prints log
	public static <T> T silent(SupExc<T> sup) {
		try {
			return sup.get();
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.DEBUG, "Unexpected Error");
			return null;
		}
	}

	public static String restrict(String str) {
		if (str.length() < restrict_name)
			return str;
		return str.substring(0, restrict_name);
	}

	public static String revVer(int ver) {
		return ver / 1000000 % 100 + "-" + ver / 10000 % 100 + "-" + ver / 100 % 100 + "-" + ver % 100;
	}

	public static String duo(int i) {
		if(i < 10) {
			return "0"+ i;
		} else {
			return "" + i;
		}
	}

	public static String trio(int i) {
		if(i < 10)
			return "00" + i;
		else if(i < 100)
			return "0" + i;
		else
			return "" + i;
	}
}
