package common.util.pack;

import common.CommonStatic;
import common.io.json.FieldOrder;
import common.io.json.FieldOrder.Order;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.system.fake.FakeImage.Marker;
import common.util.Data;
import common.util.anim.*;

import java.lang.reflect.Field;
import java.util.function.Function;

public class EffAnim<T extends Enum<T> & EffAnim.EffType<T>> extends AnimD<EffAnim<T>, T> {

	public interface EffType<T extends Enum<T> & EffType<T>> extends AnimI.AnimType<EffAnim<T>, T> {
		String path();
	}

	public enum KBEff implements EffType<KBEff> {
		KB("_hb"), SW("_sw"), ASS("_ass");

		private final String path;

		KBEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}

	}

	public enum SniperEff implements EffType<SniperEff> {
		IDLE("00"), ATK("01");

		private final String path;

		SniperEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum SpeedEff implements EffType<SpeedEff> {
		UP("up"), DOWN("down");

		private final String path;

		SpeedEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum VolcEff implements EffType<VolcEff> {
		START("00"), DURING("01"), END("02");

		private final String path;

		VolcEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum WarpEff implements EffType<WarpEff> {
		ENTER("_entrance"), EXIT("_exit");

		private final String path;

		WarpEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum WeakUpEff implements EffType<WeakUpEff> {
		UP("up");

		private final String path;

		WeakUpEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum ZombieEff implements EffType<ZombieEff> {
		REVIVE("_revive"), DOWN("_down"), BACK("_back");

		private final String path;

		ZombieEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum ArmorEff implements EffType<ArmorEff> {
		BUFF("buff"), DEBUFF("debuff");

		private final String path;

		ArmorEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum BarrierEff implements EffType<BarrierEff> {
		BREAK("_breaker"), DESTR("_destruction"), NONE("");

		private final String path;

		BarrierEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum GuardEff implements  EffType<GuardEff> {
		NONE(""), BREAK("_breaker");

		private final String path;

		GuardEff(String path) {
			this.path = path;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum ShieldEff implements EffType<ShieldEff> {
		FULL("00"), HALF("01"), BROKEN("_destruction"), BREAKER("_breaker"), REGENERATION("_revive");

		private final String path;

		ShieldEff(String str) {
			this.path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum DmgCap implements EffType<DmgCap> {
		FAIL("_fail"), SUCCESS("_success");

		private final String path;

		DmgCap(String str) {
			this.path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum DefEff implements EffType<DefEff> {
		DEF("");

		private final String path;

		DefEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum LethargyEff implements EffType<LethargyEff> {
		DOWN("0"), UP("1");

		private final String path;

		LethargyEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}
	public enum BlastEff implements EffType<BlastEff> {
		START("00"),
		EXPLODE("01"),
		DUMMY("02"),
		SINGLE("03");

		private final String path;

		BlastEff(String path) {
			this.path = path;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum RemShieldEff implements EffType<RemShieldEff> {
		NEAR("0"), FAR("1");

		private final String path;

		RemShieldEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum AuraEff implements EffType<AuraEff> {
		ENEDOWN("-ew"), ENEUP("-es"), UNIDOWN("-aw"), UNIUP("-as");

		private final String path;

		AuraEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum RangeShieldEff implements EffType<RangeShieldEff> {
		SINGLE("-SINGLE"), AREA("-AREA");

		private final String path;
		RangeShieldEff(String str) {
			path = str;
		}
		@Override
		public String path() {
			return path;
		}
	}

	public static class EffAnimStore {

		@Order(0)
		public EffAnim<DefEff> A_DOWN;
		@Order(1)
		public EffAnim<DefEff> A_E_DOWN;
		@Order(2)
		public EffAnim<DefEff> A_UP;
		@Order(3)
		public EffAnim<DefEff> A_E_UP;
		@Order(4)
		public EffAnim<DefEff> A_SLOW;
		@Order(5)
		public EffAnim<DefEff> A_E_SLOW;
		@Order(6)
		public EffAnim<DefEff> A_STOP;
		@Order(7)
		public EffAnim<DefEff> A_E_STOP;
		@Order(8)
		public EffAnim<DefEff> A_SHIELD;
		@Order(9)
		public EffAnim<DefEff> A_E_SHIELD;
		@Order(10)
		public EffAnim<DefEff> A_FARATTACK;
		@Order(11)
		public EffAnim<DefEff> A_E_FARATTACK;
		@Order(12)
		public EffAnim<DefEff> A_WAVE_INVALID;
		@Order(13)
		public EffAnim<DefEff> A_E_WAVE_INVALID;
		@Order(14)
		public EffAnim<DefEff> A_WAVE_STOP;
		@Order(15)
		public EffAnim<DefEff> A_E_WAVE_STOP;
		@Order(16)
		public EffAnim<DefEff> A_WAVEGUARD;// unused
		@Order(17)
		public EffAnim<DefEff> A_E_WAVEGUARD;// unused
		@Order(18)
		public EffAnim<DefEff> A_EFF_INV;
		@Order(19)
		public EffAnim<DefEff> A_EFF_DEF;// unused
		@Order(20)
		public EffAnim<DefEff> A_Z_STRONG;
		@Order(21)
		public EffAnim<BarrierEff> A_B;
		@Order(22)
		public EffAnim<BarrierEff> A_E_B;
		@Order(23)
		public EffAnim<WarpEff> A_W;
		@Order(24)
		public EffAnim<WarpEff> A_W_C;
		@Order(25)
		public EffAnim<DefEff> A_CURSE;
		@Order(26)
		public EffAnim<ZombieEff> A_ZOMBIE;
		@Order(27)
		public EffAnim<DefEff> A_SHOCKWAVE;
		@Order(28)
		public EffAnim<DefEff> A_CRIT;
		@Order(29)
		public EffAnim<KBEff> A_KB;
		@Order(30)
		public EffAnim<SniperEff> A_SNIPER;
		@Order(31)
		public EffAnim<ZombieEff> A_U_ZOMBIE;
		@Order(34)
		public EffAnim<DefEff> A_SEAL;
		@Order(35)
		public EffAnim<DefEff> A_E_SEAL;
		@Order(36)
		public EffAnim<DefEff> A_POI0;
		@Order(37)
		public EffAnim<DefEff> A_POI1;
		@Order(38)
		public EffAnim<DefEff> A_POI1_E;
		@Order(39)
		public EffAnim<DefEff> A_POI2;
		@Order(40)
		public EffAnim<DefEff> A_POI3;
		@Order(41)
		public EffAnim<DefEff> A_POI4;
		@Order(42)
		public EffAnim<DefEff> A_POI5;
		@Order(43)
		public EffAnim<DefEff> A_POI6;
		@Order(44)
		public EffAnim<DefEff> A_POI7;
		@Order(45)
		public EffAnim<DefEff> A_SATK;
		@Order(46)
		public EffAnim<DefEff> A_IMUATK;
		@Order(47)
		public EffAnim<DefEff> A_POISON;
		@Order(48)
		public EffAnim<VolcEff> A_VOLC;
		@Order(49)
		public EffAnim<VolcEff> A_E_VOLC;
		@Order(50)
		public EffAnim<DefEff> A_E_CURSE;
		@Order(51)
		public EffAnim<DefEff> A_WAVE;
		@Order(52)
		public EffAnim<DefEff> A_E_WAVE;
		@Order(53)
		public EffAnim<ArmorEff> A_ARMOR;
		@Order(54)
		public EffAnim<ArmorEff> A_E_ARMOR;
		@Order(55)
		public EffAnim<SpeedEff> A_SPEED;
		@Order(56)
		public EffAnim<SpeedEff> A_E_SPEED;
		@Order(57)
		public EffAnim<WeakUpEff> A_WEAK_UP;
		@Order(58)
		public EffAnim<WeakUpEff> A_E_WEAK_UP;
		@Order(59)
		public EffAnim<DefEff> A_MINIWAVE;
		@Order(60)
		public EffAnim<DefEff> A_E_MINIWAVE;
		@Order(61)
		public EffAnim<DefEff> A_ATK_SMOKE;
		@Order(62)
		public EffAnim<DefEff> A_WHITE_SMOKE;
		@Order(63)
		public EffAnim<DefEff> A_HEAL;
		@Order(64)
		public EffAnim<DefEff> A_E_HEAL;
		@Order(65)
		public EffAnim<ShieldEff> A_DEMON_SHIELD;
		@Order(66)
		public EffAnim<ShieldEff> A_E_DEMON_SHIELD;
		@Order(67)
		public EffAnim<DefEff> A_COUNTER;
		@Order(68)
		public EffAnim<DefEff> A_E_COUNTER;
		@Order(69)
		public EffAnim<DefEff> A_DMGCUT;
		@Order(70)
		public EffAnim<DefEff> A_E_DMGCUT;
		@Order(71)
		public EffAnim<DmgCap> A_DMGCAP;
		@Order(72)
		public EffAnim<DmgCap> A_E_DMGCAP;
		@Order(73)
		public EffAnim<DefEff> A_DOOR;
		@Order(74)
		public EffAnim<LethargyEff> A_LETHARGY;
		@Order(75)
		public EffAnim<LethargyEff> A_E_LETHARGY;
		@Order(76)
		public EffAnim<RemShieldEff> A_REMSHIELD;
		@Order(77)
		public EffAnim<RemShieldEff> A_E_REMSHIELD;
		@Order(78)
		public EffAnim<AuraEff> A_AURA;
		@Order(79)
		public EffAnim<AuraEff> A_E_AURA;
		@Order(80)
		public EffAnim<DefEff> A_RAGE;
		@Order(81)
		public EffAnim<DefEff> A_HYPNO;
		@Order(82)
		public EffAnim<VolcEff> A_MINIVOLC;
		@Order(83)
		public EffAnim<VolcEff> A_E_MINIVOLC;
		@Order(84)
		public EffAnim<DefEff> A_COUNTERSURGE;
		@Order(85)
		public EffAnim<DefEff> A_E_COUNTERSURGE;
		@Order(86)
		public EffAnim<RangeShieldEff> A_RANGESHIELD;
		@Order(87)
		public EffAnim<RangeShieldEff> A_E_RANGESHIELD;
		@Order(88)
		public EffAnim<DefEff> A_MEGAWAVE;
		@Order(89)
		public EffAnim<DefEff> A_E_MEGAWAVE;
		@Order(90)
		public EffAnim<GuardEff> A_E_GUARD; // Dummy "yet"
		@Order(91)
		public EffAnim<DefEff> A_METAL_KILLER;
		@Order(92)
		public EffAnim<DefEff> A_E_METAL_KILLER;
		@Order(93)
		public EffAnim<BlastEff> A_BLAST;
		@Order(94)
		public EffAnim<BlastEff> A_E_BLAST;
		@Order(95)
		public EffAnim<DefEff> A_DRAIN;
		@Order(96)
		public EffAnim<DefEff> A_E_DRAIN;
		@Order(97)
		public EffAnim<DefEff> A_BLESS;

		public EffAnim<?>[] values() {
			Field[] fld = FieldOrder.getDeclaredFields(EffAnimStore.class);
			EffAnim<?>[] ans = new EffAnim[fld.length];
			Data.err(() -> {
				for (int i = 0; i < ans.length; i++)
					ans[i] = (EffAnim<?>) fld[i].get(this);
			});
			return ans;
		}

		private void set(int i, EffAnim<DefEff> eff) {
			err(() -> FieldOrder.getDeclaredFields(EffAnimStore.class)[i].set(this, eff));
		}

	}

	public static void read() {
		EffAnimStore effas = CommonStatic.getBCAssets().effas;
		String stre = "./org/battle/e1/set_enemy001_zombie";
		VImg ve = new VImg(stre + ".png");
		ImgCut ice = ImgCut.newIns(stre + ".imgcut");
		String stra = "./org/battle/a/";
		VImg va = new VImg(stra + "000_a.png");
		ImgCut ica = ImgCut.newIns(stra + "000_a.imgcut");
		String ski = "skill00";
		String[] stfs = new String[4];
		VImg[] vfs = new VImg[4];
		ImgCut[] icfs = new ImgCut[4];
		for (int i = 0; i < 4; i++) {
			stfs[i] = "./org/battle/s" + i + "/";
			vfs[i] = new VImg(stfs[i] + ski + i + ".png");
			icfs[i] = ImgCut.newIns(stfs[i] + ski + i + ".imgcut");
		}
		effas.A_ATK_SMOKE = new EffAnim<>(stra+"attack_smoke", va, ica, DefEff.values());
		effas.A_WHITE_SMOKE = new EffAnim<>(stra+"white_smoke", va, ica, DefEff.values());
		effas.A_SHOCKWAVE = new EffAnim<>(stra + "boss_welcome", va, ica, DefEff.values());
		effas.A_CRIT = new EffAnim<>(stra + "critical", va, ica, DefEff.values());
		effas.A_KB = new EffAnim<>(stra + "kb", va, ica, KBEff.values());
		effas.A_ZOMBIE = new EffAnim<>(stre, ve, ice, ZombieEff.values());
		effas.A_U_ZOMBIE = new EffAnim<>(stre, ve, ice, ZombieEff.values());
		effas.A_U_ZOMBIE.rev = true;
		ski = "skill_";
		for (int i = 0; i < A_PATH.length; i++) {
			String path = stfs[0] + A_PATH[i] + "/" + ski + A_PATH[i];
			effas.set(i * 2, new EffAnim<>(path, vfs[0], icfs[0], DefEff.values()));
			effas.set(i * 2 + 1, new EffAnim<>(path + "_e", vfs[0], icfs[0], DefEff.values()));
		}
		effas.A_EFF_INV = new EffAnim<>(stfs[0] + ski + "effect_invalid", vfs[0], icfs[0], DefEff.values());
		effas.A_EFF_DEF = new EffAnim<>(stfs[0] + ski + "effectdef", vfs[0], icfs[0], DefEff.values());
		effas.A_Z_STRONG = new EffAnim<>(stfs[1] + ski + "zombie_strong", vfs[1], icfs[1], DefEff.values());
		effas.A_B = new EffAnim<>(stfs[2] + ski + "barrier_e", vfs[2], icfs[2], BarrierEff.values());
		effas.A_B.rev = true;
		effas.A_E_B = new EffAnim<>(stfs[2] + ski + "barrier_e", vfs[2], icfs[2], BarrierEff.values());
		effas.A_W = new EffAnim<>(stfs[2] + ski + "warp", vfs[2], icfs[2], WarpEff.values());
		effas.A_W_C = new EffAnim<>(stfs[2] + ski + "warp_chara", vfs[2], icfs[2], WarpEff.values());
		String strs = "./org/battle/sniper/";
		String strm = "img043";
		VImg vis = new VImg(strs + strm + ".png");
		ImgCut ics = ImgCut.newIns(strs + strm + ".imgcut");
		effas.A_SNIPER = new EffAnim<>(strs + "000_snyaipa", vis, ics, SniperEff.values());
		effas.A_CURSE = new EffAnim<>(stfs[3] + ski + "curse", vfs[3], icfs[3], DefEff.values());

		readCustom(stfs);

		VImg vuw = new VImg("./org/battle/s4/skill004.png");
		ImgCut icsvuw = ImgCut.newIns("./org/battle/s4/skill004.imgcut");
		effas.A_WAVE = new EffAnim<>("./org/battle/s4/skill_wave_attack", vuw, icsvuw, DefEff.values());
		VImg vew = new VImg("./org/battle/s5/skill005.png");
		ImgCut icsvew = ImgCut.newIns("./org/battle/s5/skill005.imgcut");
		effas.A_E_WAVE = new EffAnim<>("./org/battle/s5/skill_wave_attack_e", vew, icsvew, DefEff.values());
		VImg vsatk = new VImg("./org/battle/s6/skill006.png");
		ImgCut icsatk = ImgCut.newIns("./org/battle/s6/skill006.imgcut");
		effas.A_SATK = new EffAnim<>("./org/battle/s6/strong_attack", vsatk, icsatk, DefEff.values());
		VImg viatk = new VImg("./org/battle/s7/skill007.png");
		ImgCut iciatk = ImgCut.newIns("./org/battle/s7/skill007.imgcut");
		effas.A_IMUATK = new EffAnim<>("./org/battle/s7/skill_attack_invalid", viatk, iciatk, DefEff.values());
		VImg vip = new VImg("./org/battle/s8/skill008.png");
		ImgCut icp = ImgCut.newIns("./org/battle/s8/skill008.imgcut");
		effas.A_POISON = new EffAnim<>("./org/battle/s8/skill_percentage_attack", vip, icp, DefEff.values());
		VImg vic = new VImg("./org/battle/s9/skill009.png");
		ImgCut icc = ImgCut.newIns("./org/battle/s9/skill009.imgcut");
		effas.A_VOLC = new EffAnim<>("./org/battle/s9/skill_volcano", vic, icc, VolcEff.values());
		vic = new VImg("./org/battle/s10/skill010.png");
		icc = ImgCut.newIns("./org/battle/s10/skill010.imgcut");
		effas.A_E_VOLC = new EffAnim<>("./org/battle/s10/skill_volcano", vic, icc, VolcEff.values());
		VImg vcu = new VImg("./org/battle/s11/skill011.png");
		ImgCut iccu = ImgCut.newIns("./org/battle/s11/skill011.imgcut");
		effas.A_E_CURSE = new EffAnim<>("./org/battle/s11/skill_curse_e", vcu, iccu, DefEff.values());
		VImg vmw = new VImg("./org/battle/s12/skill012.png");
		ImgCut icmw = ImgCut.newIns("./org/battle/s12/skill012.imgcut");
		effas.A_MINIWAVE = new EffAnim<>("./org/battle/s12/skill_smallwave_attack", vmw, icmw, DefEff.values());
		vmw = new VImg("./org/battle/s13/skill013.png");
		icmw = ImgCut.newIns("./org/battle/s13/skill013.imgcut");
		effas.A_E_MINIWAVE = new EffAnim<>("./org/battle/s13/skill_smallwave_attack_e", vmw, icmw, DefEff.values());
		VImg vsh = new VImg("./org/battle/s14/skill014.png");
		ImgCut icsh = ImgCut.newIns("./org/battle/s14/skill014.imgcut");
		effas.A_DEMON_SHIELD = new EffAnim<>("./org/battle/s14/skill_demonshield", vsh, icsh, ShieldEff.values());
		effas.A_DEMON_SHIELD.rev = true;
		effas.A_E_DEMON_SHIELD = new EffAnim<>("./org/battle/s14/skill_demonshield", vsh, icsh, ShieldEff.values());
		VImg vmv = new VImg("./org/battle/s15/skill015.png");
		ImgCut icmv = ImgCut.newIns("./org/battle/s15/skill015.imgcut");
		effas.A_MINIVOLC = new EffAnim<>("./org/battle/s15/skill_smallvolcano", vmv, icmv, VolcEff.values());
		vmv = new VImg("./org/battle/s16/skill016.png");
		icmv = ImgCut.newIns("./org/battle/s16/skill016.imgcut");
		effas.A_E_MINIVOLC = new EffAnim<>("./org/battle/s16/skill_smallvolcano_e", vmv, icmv, VolcEff.values());
		ImgCut iccs = ImgCut.newIns("./org/battle/s17/skill017.imgcut");
		VImg vcs = new VImg("./org/battle/s17/skill017.png");
		effas.A_E_COUNTERSURGE = new EffAnim<>("./org/battle/s17/skill_demonsummon_e", vcs, iccs, DefEff.values());
		ImgCut icct = ImgCut.newIns("./org/battle/s18/skill018.imgcut");
		VImg vct = new VImg("./org/battle/s18/skill018.png");
		effas.A_COUNTERSURGE = new EffAnim<>("./org/battle/s18/skill_demonsummon", vct, icct, DefEff.values());
		VImg vgd = new VImg("./org/battle/s19/skill019.png");
		ImgCut icgd = ImgCut.newIns("./org/battle/s19/skill019.imgcut");
		effas.A_E_GUARD = new EffAnim<>("./org/battle/s19/skill_guard_e", vgd, icgd, GuardEff.values());
		VImg vmk = new VImg("./org/battle/s20/skill020.png");
		ImgCut icmk = ImgCut.newIns("./org/battle/s20/skill020.imgcut");
		effas.A_METAL_KILLER = new EffAnim<>("./org/battle/s20/skill_metal_strong", vmk, icmk, DefEff.values());
		effas.A_E_METAL_KILLER = new EffAnim<>("./org/battle/s20/skill_metal_strong", vmk, icmk, DefEff.values());
		effas.A_E_METAL_KILLER.rev = true;
		VImg vbs = new VImg("./org/battle/s21/skill021.png");
		ImgCut icbs = ImgCut.newIns("./org/battle/s21/skill021.imgcut");
		effas.A_BLAST = new EffAnim<>("./org/battle/s21/skill_explosion", vbs, icbs, BlastEff.values());
		vbs = new VImg("./org/battle/s22/skill022.png");
		icbs = ImgCut.newIns("./org/battle/s22/skill022.imgcut");
		MaModel mmbs = MaModel.newIns("./org/battle/s22/skill_explosion_e.mamodel");
		effas.A_E_BLAST = new EffAnim<>("./org/battle/s22/skill_explosion", vbs, icbs, mmbs, BlastEff.values());
	}

	private static void excColor(FakeImage fimg, Function<int[], Integer> f) {
		fimg.mark(Marker.RECOLOR);
		int w = fimg.getWidth();
		int h = fimg.getHeight();
		for (int i = 0; i < w; i++)
			for (int j = 0; j < h; j++) {
				int p = fimg.getRGB(i, j);
				int b = p & 255;
				int g = p >> 8 & 255;
				int r = p >> 16 & 255;
				int a = p >> 24;
				p = f.apply(new int[] { a, r, g, b });
				fimg.setRGB(i, j, p);
			}
		fimg.mark(Marker.RECOLORED);
	}

	private static void readCustom(String[] stfs) {
		EffAnimStore effas = CommonStatic.getBCAssets().effas;

		String seal = stfs[3]+"seal/seal";
		VImg vseal = new VImg(seal+".png");
		ImgCut icseal = ImgCut.newIns(seal+".imgcut");
		effas.A_SEAL = new EffAnim<>(seal, vseal, icseal, DefEff.values());
		seal = stfs[3] +"seal_e/seal_e";
		vseal = new VImg(seal+".png");
		icseal = ImgCut.newIns(seal+".imgcut");
		effas.A_E_SEAL = new EffAnim<>(seal, vseal, icseal, DefEff.values());
		String burn = stfs[3] + "burn/burn";
		ImgCut icburn = ImgCut.newIns(burn+".imgcut");
		VImg vpois = new VImg(burn + ".png");
		effas.A_POI0 = new EffAnim<>(burn, vpois, icburn, DefEff.values());
		effas.A_POI0.name = "poison_DF";
		String poison = stfs[3] + "poison/poison";
		ImgCut icpoi = ImgCut.newIns(poison+".imgcut");
		vpois = new VImg(stfs[3] + "poison.png");
		effas.A_POI1 = new EffAnim<>(poison, vpois, icpoi, DefEff.values());
		effas.A_POI1.name = "poison_DT0";
		poison = stfs[3] + "poison/poison_e";
		icpoi = ImgCut.newIns(poison+".imgcut");
		effas.A_POI1_E = new EffAnim<>(poison, vpois, icpoi, DefEff.values());
		effas.A_POI1_E.name = "poison_DT0_e";
		String strpb = stfs[3] + "poisbub/poisbub";
		vpois = new VImg(strpb + ".png");
		ImgCut icpois = ImgCut.newIns(strpb + ".imgcut");
		effas.A_POI2 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI2.name = "poison_purple";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[1] << 16 | is[3] << 8 | is[2]));
		effas.A_POI3 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI3.name = "poison_green";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[2] << 16 | is[1] << 8 | is[3]));
		effas.A_POI4 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI4.name = "poison_blue";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[2] << 16 | is[3] << 8 | is[1]));
		effas.A_POI5 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI5.name = "poison_cyan";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[3] << 16 | is[1] << 8 | is[2]));
		effas.A_POI6 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI6.name = "poison_orange";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[3] << 16 | is[2] << 8 | is[1]));
		effas.A_POI7 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI7.name = "poison_pink";

		String breaker = stfs[3] + "armor_break/armor_break";
		VImg vbreak = new VImg(breaker + ".png");
		ImgCut icbreak = ImgCut.newIns(breaker + ".imgcut");
		effas.A_ARMOR = new EffAnim<>(breaker, vbreak, icbreak, ArmorEff.values());
		breaker = stfs[3] + "armor_break_e/armor_break_e";
		icbreak = ImgCut.newIns(breaker + ".imgcut");
		vbreak = new VImg(breaker + ".png");
		effas.A_E_ARMOR = new EffAnim<>(breaker, vbreak, icbreak, ArmorEff.values());

		String speed = stfs[3] + "speed/speed";
		VImg vspeed = new VImg(speed + ".png");
		ImgCut icspeed = ImgCut.newIns(speed + ".imgcut");
		effas.A_SPEED = new EffAnim<>(speed, vspeed, icspeed, SpeedEff.values());
		speed = stfs[3] + "speed_e/speed_e";
		vspeed = new VImg(speed + ".png");
		icspeed = ImgCut.newIns(speed + ".imgcut");
		effas.A_E_SPEED = new EffAnim<>(speed, vspeed, icspeed, SpeedEff.values());

		String wea = "./org/battle/";
		String weakup = wea + "weaken_up/weaken_up";
		VImg vwea = new VImg(weakup + ".png");
		ImgCut icwea = ImgCut.newIns(weakup + ".imgcut");
		effas.A_WEAK_UP = new EffAnim<>(weakup, vwea, icwea, WeakUpEff.values());

		weakup = wea + "weaken_up_e/weaken_up_e";
		vwea = new VImg(weakup + ".png");
		icwea = ImgCut.newIns(weakup + ".imgcut");
		effas.A_E_WEAK_UP = new EffAnim<>(weakup, vwea, icwea, WeakUpEff.values());

		String heal = stfs[3] + "heal/heal";
		VImg vheal = new VImg(heal + ".png");
		ImgCut icheal = ImgCut.newIns(heal+".imgcut");
		effas.A_HEAL = new EffAnim<>(heal, vheal, icheal, DefEff.values());

		heal = stfs[3] + "heal_e/heal_e";
		vheal = new VImg(heal + ".png");
		icheal = ImgCut.newIns(heal+".imgcut");
		effas.A_E_HEAL = new EffAnim<>(heal, vheal, icheal, DefEff.values());

		String counter = stfs[3] + "counter/counter";
		VImg vcount = new VImg(counter+".png");
		ImgCut iccount = ImgCut.newIns(counter+".imgcut");
		effas.A_COUNTER =  new EffAnim<>(counter, vcount, iccount, DefEff.values());
		effas.A_E_COUNTER = new EffAnim<>(counter, vcount, iccount, DefEff.values());
		effas.A_E_COUNTER.rev = true;

		String dmgcut = stfs[3] + "dmgcut/dmgcut";
		VImg vdmgcut = new VImg(dmgcut+".png");
		ImgCut icdmgcut = ImgCut.newIns(dmgcut+".imgcut");
		effas.A_DMGCUT = new EffAnim<>(dmgcut, vdmgcut, icdmgcut, DefEff.values());
		effas.A_E_DMGCUT = new EffAnim<>(dmgcut, vdmgcut, icdmgcut, DefEff.values());
		effas.A_E_DMGCUT.rev = true;

		String dmgcap = stfs[3] + "dmgcap/dmgcap";
		VImg vdmgcap = new VImg(dmgcap+".png");
		ImgCut icdmgcap = ImgCut.newIns(dmgcap+".imgcut");
		effas.A_DMGCAP = new EffAnim<>(dmgcap, vdmgcap, icdmgcap, DmgCap.values());
		effas.A_DMGCAP.rev = true;
		effas.A_E_DMGCAP = new EffAnim<>(dmgcap, vdmgcap, icdmgcap, DmgCap.values());

		String path = "./org/battle/skill_door/skill_door";
		VImg vdr = new VImg(path + ".png");
		ImgCut icdr = ImgCut.newIns(path + ".imgcut");
		effas.A_DOOR = new EffAnim<>(path, vdr, icdr, DefEff.values());

		path = "./org/battle/skill_lethargy/skill_lethargy";
		VImg vlt = new VImg(path + ".png");
		ImgCut iclt = ImgCut.newIns(path + ".imgcut");
		effas.A_LETHARGY = new EffAnim<>(path, vlt, iclt, LethargyEff.values());
		effas.A_E_LETHARGY = new EffAnim<>(path, vlt, iclt, LethargyEff.values());
		effas.A_E_LETHARGY.rev = true;

		path = "./org/battle/skill_remoteshield/skill_remoteshield";
		VImg vrs = new VImg(path + ".png");
		ImgCut icrs = ImgCut.newIns(path + ".imgcut");
		effas.A_REMSHIELD = new EffAnim<>(path, vrs, icrs, RemShieldEff.values());
		effas.A_E_REMSHIELD = new EffAnim<>(path, vrs, icrs, RemShieldEff.values());
		effas.A_E_REMSHIELD.rev = true;

		path = "./org/battle/skill_aura/skill_aura";
		VImg vau = new VImg(path + ".png");
		ImgCut icau = ImgCut.newIns(path + ".imgcut");
		effas.A_AURA = new EffAnim<>(path, vau, icau, AuraEff.values());
		effas.A_E_AURA = new EffAnim<>(path, vau, icau, AuraEff.values());
		effas.A_E_AURA.rev = true;

		path = "./org/battle/skill_rage/skill_rage";
		VImg var = new VImg(path + ".png");
		ImgCut icar = ImgCut.newIns(path + ".imgcut");
		effas.A_RAGE = new EffAnim<>(path, var, icar, DefEff.values());

		path = "./org/battle/skill_hypno/skill_hypno";
		VImg vah = new VImg(path + ".png");
		ImgCut icah = ImgCut.newIns(path + ".imgcut");
		effas.A_HYPNO = new EffAnim<>(path, vah, icah, DefEff.values());

		path = "./org/battle/skill_targetshield/skill_targetshield";
		VImg sah = new VImg(path + ".png");
		ImgCut isah = ImgCut.newIns(path + ".imgcut");
		effas.A_RANGESHIELD = new EffAnim<>(path, sah, isah, RangeShieldEff.values());
		effas.A_E_RANGESHIELD = new EffAnim<>(path, sah, isah, RangeShieldEff.values());
		effas.A_E_RANGESHIELD.rev = true;

		path = "./org/battle/ChargedWave/ChargedWave";
		ImgCut iwah = ImgCut.newIns(path + ".imgcut");
		VImg wah = new VImg(path + "U.png");
		effas.A_MEGAWAVE = new EffAnim<>(path, wah, iwah, DefEff.values());
		wah = new VImg(path + ".png");
		effas.A_E_MEGAWAVE = new EffAnim<>(path, wah, iwah, DefEff.values());

        path = "./org/battle/skill_drain/skill_drain";
        ImgCut idrn = ImgCut.newIns(path + ".imgcut");
        VImg drn = new VImg(path + ".png");
        effas.A_DRAIN = new EffAnim<>(path, drn, idrn, DefEff.values());
        effas.A_E_DRAIN = new EffAnim<>(path, drn, idrn, DefEff.values());
        effas.A_DRAIN.rev = true;

        path = "./org/battle/skill_blessing/skill_blessing";
        ImgCut ibls = ImgCut.newIns(path + ".imgcut");
        VImg bls = new VImg(path + ".png");
        effas.A_BLESS = new EffAnim<>(path, bls, ibls, DefEff.values());
	}

	private final VImg vimg;
	private boolean rev;
	private String name = "";

	public EffAnim(String st, VImg vi, ImgCut ic, T[] anims) {
		super(st);
		vimg = vi;
		imgcut = ic;
		types = anims;
	}

	public EffAnim(String st, VImg vi, ImgCut ic, MaModel model, T[] anims) {
		super(st);
		vimg = vi;
		imgcut = ic;
		mamodel = model;
		types = anims;
	}

	@Override
	public FakeImage getNum() {
		return vimg.getImg();
	}

	@Override
	public void load() {
		loaded = true;
		parts = imgcut.cut(vimg.getImg());
		if (mamodel == null) {
			mamodel = MaModel.newIns(str + ".mamodel");
		}
		anims = new MaAnim[types.length];
		for (int i = 0; i < types.length; i++) {
			anims[i] = MaAnim.newIns(str + types[i].path() + ".maanim");
			for (Part p : anims[i].parts)
				for (int j = 0; j < p.moves.length - 1; j++)
					if (p.moves[j][0] == p.moves[j+1][0]-1)
						p.moves[j][2] = 1;
		}
		if (rev)
			revert();
	}

	@Override
	public String toString() {
		if (!name.isEmpty())
			return name;

		String[] ss = str.split("/");

		return ss[ss.length - 1];
	}

}
