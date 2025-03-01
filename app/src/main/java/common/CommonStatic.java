package common;

import common.battle.data.PCoin;
import common.io.assets.Admin.StaticPermitted;
import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonField;
import common.pack.Context;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.util.Data;
import common.util.anim.ImgCut;
import common.util.anim.MaModel;
import common.util.pack.DemonSoul;
import common.util.pack.EffAnim.EffAnimStore;
import common.util.pack.NyCastle;
import common.util.stage.Music;
import common.util.unit.*;

import java.io.File;
import java.lang.Character;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.isDigit;

public class CommonStatic {

	public interface BattleConst {

		float ratio = 768f / 2400f;// r = p/u

	}

	public static class BCAuxAssets {

		// Res resources
		public final VImg[] slot = new VImg[3];
		public final VImg[][] ico = new VImg[2][];
		public final VImg[][] num = new VImg[9][11];
		public final VImg[][] battle = new VImg[3][];
		public final VImg[][] icon = new VImg[5][];
		public final VImg[] timer = new VImg[11];
		public final VImg[] spiritSummon = new VImg[4];

		public VImg emptyEdi = null;

		public Map<Integer, VImg> gatyaitem = new HashMap<>();
		public VImg XP;
		public VImg[][] moneySign = new VImg[4][4]; //Money on, off/Cost on, off
		/**
		 * Use this if trait.icon is null
		 */
		public VImg dummyTrait;
		public VImg waveShield;
		public VImg[] dmgIcons = new VImg[3];

		// Background resources
		public final List<ImgCut> iclist = new ArrayList<>();

		// Available data for orb, will be used for GUI
		// Map<Type, Map<Trait, Grades>>
		public final Map<Byte, Map<Integer, List<Byte>>> ORB = new TreeMap<>();
		public final Map<Byte, Integer> DATA = new HashMap<>();
		public final SortedPackSet<DemonSoul> demonSouls = new SortedPackSet<>();

		public FakeImage[] TYPES;
		public FakeImage[] TRAITS;
		public FakeImage[] GRADES;

		// NyCastle
		public final VImg[][] main = new VImg[3][NyCastle.TOT];
		public final NyCastle[] atks = new NyCastle[NyCastle.TOT];

		// EffAnim
		public final EffAnimStore effas = new EffAnimStore();
		public final int[][] values = new int[Data.C_TOT][5];
		public int[][] filter;

		// Form cuts
		public ImgCut unicut, udicut;

		// RandStage
		public final int[][] randRep = new int[5][];

		// def unit level
		public UnitLevel defLv;
	}

	@JsonClass(noTag = NoTag.LOAD)
	public static class LocalMaps {
		@JsonField(generic = { String.class, String.class })
		public HashMap<String, String> localLangMap = new HashMap<>();

		@JsonField(generic = { Integer.class, String.class })
		public HashMap<Integer, String> localMusicMap = new HashMap<>();
	}

	@JsonClass(noTag = NoTag.LOAD)
	public static class Config {

		// ImgCore
		@JsonField(defval = "10")
		public int deadOpa = 10;
		@JsonField(defval = "90")
		public int fullOpa = 90;
		public int[] ints = new int[] { 1, 1, 1, 2 };
		@JsonField(defval = "true")
		public boolean ref = true, twoRow = true;
		@JsonField(defval = "false")
		public boolean battle = false, icon = false;
		/**
		 * Use this variable to unlock plus level for aku outbreak
		 */
		@JsonField(defval = "false")
		public boolean plus = false;
		/**
		 * Use this variable to adjust level limit for aku outbreak
		 */
		@JsonField(defval = "0")
		public int levelLimit = 0;
		@JsonField(defval = "this.defaultLangOrder")
		public Lang.Locale[] langs = Lang.Locale.values();
		public boolean defaultLangOrder() {
			Lang.Locale[] def = Lang.Locale.values();
			for (int i = 0; i < def.length; i++)
				if (def[i] != langs[i])
					return false;
			return true;
		}

		/**
		 * Restoration target backup file, null means none
		 */
		public String backupFile;
		/**
		 * Used for partial restoration
		 */
		public String backupPath;
		/**
		 * Maximum number of backups, 0 means infinite
		 */
		@JsonField(defval = "5")
		public int maxBackup = 5;

		/**
		 * Decide whehter draw bg effect or not
		 */
		@JsonField(defval = "true")
		public boolean drawBGEffect = true;

		/**
		 * Enable 6f button delay on spawn
		 */
		@JsonField(defval = "true")
		public boolean buttonDelay = true;

		/**
		 * Color of background in viewer
		 */
		@JsonField(defval = "-1")
		public int viewerColor = -1;

		/**
		 * Make BCU show ex stage continuation pop-up if true
		 */
		@JsonField(defval = "false")
		public boolean exContinuation = false;

		/**
		 * Make EX stage pop-up shown considering real chance
		 */
		@JsonField(defval = "false")
		public boolean realEx = false;

		/**
		 * Make stage name image displayed in battle
		 */
		@JsonField(defval = "true")
		public boolean stageName = true;

		/**
		 * Make battle shaken
		 */
		@JsonField(defval = "true")
		public boolean shake = true;

		/**
		 * Replace old music when updated
		 */
		@JsonField(defval = "true")
		public boolean updateOldMusic = true;

		/**
		 * Perform BC levelings
		 */
		@JsonField(defval = "false")
		public boolean realLevel = false;

		/**
		 * Use raw damage taken for TotalDamageTable
		 */
		@JsonField(defval = "true")
		public boolean rawDamage = true;

		/**
		 * Store whether to apply the combos from a given pack or not
		 */
		@JsonField(generic = { String.class }, defval = "isEmpty")
		public HashSet<String> excludeCombo = new HashSet<>();

		/**setLvs
		 * Use progression mode to store save data
		 */
		@JsonField(defval = "false")
		public boolean prog = false;

		/**
		 * 60 fps mode
		 */
		@JsonField(defval = "false")
		public boolean fps60 = false;

		/**
		 * Stat
		 */
		@JsonField(defval = "false")
		public boolean stat = false;
	}

	@JsonClass
	public static class Preflvs {
		@JsonField(defval = "this.allDefs")
		public Level[] rare = new Level[6];
		@JsonField(generic = {Identifier.class, Level.class}, defval = "isEmpty")
		public HashMap<Identifier<AbUnit>, Level> uni = new HashMap<>();

		@JsonClass.JCConstructor
		public Preflvs() {
			for (byte i = 0; i < rare.length; i++)
				rare[i] = new Level(50, i < 2 ? 100 : 0, new int[0]);
		}

		public boolean allDefs() {
			for (byte i = 0; i < rare.length; i++)
				if (rare[i].getLv() != 50 || rare[i].getPlusLv() != (i < 2 ? 100 : 0) || rare[i].getTalents().length > 0)
					return false;
			return true;
		}

		public boolean equalsDef(Form f, Level lv) {
			Level rv = rare[f.unit.rarity];
			if (Math.min(f.unit.max, lv.getLv()) + Math.min(f.unit.maxp, lv.getPlusLv()) != Math.min(f.unit.max, rv.getLv()) + Math.min(f.unit.maxp, rv.getPlusLv()))
				return false;
			PCoin pc = f.du.getPCoin();
			if (pc == null) {
				for (Form ff : f.unit.forms)
					if (ff.du.getPCoin() != null && (pc == null || ff.du.getPCoin().max.length >= pc.max.length))
						pc = ff.du.getPCoin();
				if (pc == null)
					return Arrays.deepEquals(lv.getOrbs(), rv.getOrbs());
			}
			int[] rnp = rv.getTalents(), lnp = lv.getTalents();
			for (int i = 0; i < Math.min(rnp.length, lnp.length); i++)
				if (lnp[i] != Math.min(rnp[i], pc.max[i]))
					return false;
			for (int i = rnp.length; i < Math.min(lnp.length, pc.max.length); i++)
				if (lnp[i] < pc.max[i])
					return false;
			return Arrays.deepEquals(lv.getOrbs(), rv.getOrbs());
		}
	}

	@JsonClass
	public static class Faves {
		@JsonField(generic = AbForm.class, alias = AbForm.AbFormJson.class, defval = "isEmpty")
		public HashSet<AbForm> units = new HashSet<>();
		@JsonField(generic = AbEnemy.class, alias = Identifier.class, defval = "isEmpty")
		public SortedPackSet<AbEnemy> enemies = new SortedPackSet<>();
	}

	public interface EditLink {

		void review();

	}

	public interface FakeKey {

		boolean pressed(int i, int j);

		void remove(int i, int j);

	}

	public interface Itf {

		/**
		 * exit
		 */
		void save(boolean save, boolean genBackup, boolean exit);

		long getMusicLength(Music f);

		@Deprecated
		File route(String path);

		void setSE(int mus);

		void setSE(Identifier<Music> mus);

		void setBGM(Identifier<Music> mus);

		String getUILang(int m, String s);

		String[] lvText(AbForm f, Level lv);
	}

	public static class Lang {
		@StaticPermitted
		public enum Locale {
			EN("en", "English"),
			ZH("zh", "中文"),
			KR("kr", "한국어"),
			JP("jp", "日本語"),
			RU("ru", "Русский"),
			DE("de", "Deutsche"),
			FR("fr", "Français"),
			ES("es", "Español"),
			IT("it", "Italiano"),
			TH("th", "Thai");

			public final String code, name;

			Locale(String localeCode, String n) {
				code = localeCode;
				name = n;
			}

			@Override
			public String toString() {
				return code;
			}
		}
	}

	@StaticPermitted(StaticPermitted.Type.ENV)
	public static Itf def;

	@StaticPermitted(StaticPermitted.Type.ENV)
	public static Context ctx;

	@StaticPermitted(StaticPermitted.Type.FINAL)
	public static final BigInteger max = new BigInteger(String.valueOf(Integer.MAX_VALUE));
	@StaticPermitted(StaticPermitted.Type.FINAL)
	public static final BigDecimal maxdbl = new BigDecimal(String.valueOf(Double.MAX_VALUE));
	@StaticPermitted(StaticPermitted.Type.FINAL)
	public static final BigInteger min = new BigInteger(String.valueOf(Integer.MIN_VALUE));
	@StaticPermitted(StaticPermitted.Type.FINAL)
	public static final BigDecimal maxfbl = new BigDecimal(String.valueOf(Float.MAX_VALUE));

	public static BCAuxAssets getBCAssets() {
		return UserProfile.getStatic("BCAuxAssets", BCAuxAssets::new);
	}

	public static LocalMaps getDataMaps() {
		return UserProfile.getStatic("localMaps", LocalMaps::new);
	}
	public static Config getConfig() {
		return UserProfile.getStatic("config", Config::new);
	}
	public static Faves getFaves() {
		return UserProfile.getStatic("faves", Faves::new);
	}

	public static Preflvs getPrefLvs() {
		return UserProfile.getStatic("prefLvs", Preflvs::new);
	}

	public static boolean isInteger(String str) {
		str = str.trim();

		for (int i = 0; i < str.length(); i++)
			if (!Character.isDigit(str.charAt(i)))
				if(str.charAt(i) != '-' || i != 0)
					return false;
		return !str.isEmpty();
	}

	public static boolean isDouble(String str) {
		boolean dotted = false;
		for (int i = 0; i < str.length(); i++)
			if (!Character.isDigit(str.charAt(i))) {
				if((i == 0 && str.charAt(i) != '-') || (i > 0 && str.charAt(i) != '.') || dotted)
					return false;
				else
					dotted = str.charAt(i) == '.';
			}
		return true;
	}

	public static int parseIntN(String str) {
		try {
			return parseIntsN(str)[0];
		} catch (Exception e) {
			return -1;
		}
	}

	public static String verifyFileName(String str) {
		return str.replaceAll("[\\\\/:*<>?\"|]", "_");
	}

	public static double parseDoubleN(String str) {
		try {
			return parseDoublesN(str)[0];
		} catch (Exception e) {
			return -1.0;
		}
	}

	public static float parseFloatN(String str) {
		try {
			return parseFloatsN(str)[0];
		} catch (Exception e) {
			return -1f;
		}
	}

	public static double[] parseDoublesN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		Matcher matcher = Pattern.compile("-?(([.|,]\\d+)|\\d+([.|,]\\d*)?)").matcher(str);

		while (matcher.find())
			lstr.add(matcher.group());

		double[] result = new double[lstr.size()];
		for (int i = 0; i < lstr.size(); i++)
			result[i] = safeParseDouble(lstr.get(i));
		return result;
	}

	public static float[] parseFloatsN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		Matcher matcher = Pattern.compile("-?(([.|,]\\d+)|\\d+([.|,]\\d*)?)").matcher(str);

		while (matcher.find())
			lstr.add(matcher.group());

		float[] result = new float[lstr.size()];

		for (int i = 0; i < lstr.size(); i++)
			result[i] = safeParseFloat(lstr.get(i));

		return result;
	}

	public static int[] parseIntsN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		int t = -1;
		for (int i = 0; i < str.length(); i++)
			if (t == -1) {
				if (isDigit(str.charAt(i)) || str.charAt(i) == '-')
					t = i;
			} else if (!isDigit(str.charAt(i))) {
				lstr.add(str.substring(t, i));
				t = -1;
			}
		if (t != -1)
			lstr.add(str.substring(t));
		int ind = 0;
		while (ind < lstr.size()) {
			if (isDigit(lstr.get(ind).charAt(0)) || lstr.get(ind).length() > 1)
				ind++;
			else
				lstr.remove(ind);
		}
		int[] ans = new int[lstr.size()];
		for (int i = 0; i < lstr.size(); i++)
			ans[i] = safeParseInt(lstr.get(i));
		return ans;
	}

	public static int safeParseInt(String v) {
		if(isInteger(v)) {
			BigInteger big = new BigInteger(v);

			if(big.compareTo(max) > 0) {
				return Integer.MAX_VALUE;
			} else if(big.compareTo(min) < 0) {
				return Integer.MIN_VALUE;
			} else
				return Integer.parseInt(v);
		} else
			throw new IllegalStateException("Value "+v+" isn't a number");
	}

	public static double safeParseDouble(String v) {
		if(isDouble(v)) {
			BigDecimal big = new BigDecimal(v);

			if(big.compareTo(maxdbl) > 0) {
				return Double.MAX_VALUE;
			} else
				return Double.parseDouble(v);
		} else
			throw new IllegalStateException("Value "+v+" isn't a number");
	}

	public static float safeParseFloat(String v) {
		if(isDouble(v)) {
			BigDecimal big = new BigDecimal(v);

			if(big.compareTo(maxfbl) > 0) {
				return Float.MAX_VALUE;
			} else
				return Float.parseFloat(v);
		} else
			throw new IllegalStateException("Value "+v+" isn't a number");
	}

	public static long parseLongN(String str) {
		long ans;
		try {
			ans = parseLongsN(str)[0];
		} catch (Exception e) {
			ans = -1;
		}
		return ans;
	}

	public static long[] parseLongsN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		int t = -1;
		for (int i = 0; i < str.length(); i++)
			if (t == -1) {
				if (isDigit(str.charAt(i)) || str.charAt(i) == '-' || str.charAt(i) == '+')
					t = i;
			} else if (!isDigit(str.charAt(i))) {
				lstr.add(str.substring(t, i));
				t = -1;
			}
		if (t != -1)
			lstr.add(str.substring(t));
		int ind = 0;
		while (ind < lstr.size()) {
			if (isDigit(lstr.get(ind).charAt(0)) || lstr.get(ind).length() > 1)
				ind++;
			else
				lstr.remove(ind);
		}
		long[] ans = new long[lstr.size()];
		for (int i = 0; i < lstr.size(); i++)
			ans[i] = Long.parseLong(lstr.get(i));
		return ans;
	}

	/**
	 * play sound effect
	 */
	public static void setSE(int ind) {
		def.setSE(ind);
	}

	/**
	 * play sound effect with identifier
	 */
	public static void setSE(Identifier<Music> mus) {
		def.setSE(mus);
	}

	/**
	 * play background music
	 * @param music Music
	 */
	public static void setBGM(Identifier<Music> music) {
		def.setBGM(music);
	}

	public static String toArrayFormat(int... data) {
		StringBuilder res = new StringBuilder("{");

		for (int i = 0; i < data.length; i++) {
			if (i == data.length - 1) {
				res.append(data[i]).append("}");
			} else {
				res.append(data[i]).append(", ");
			}
		}

		return res.toString();
	}

	/**
	 * Gets the minimum position value for a data enemy.
	 */
	public static float dataEnemyMinPos(MaModel model) {
		int x = ((model.confs[0][2] - model.parts[0][6]) * model.parts[0][8]) / model.ints[0];
		return 2.5f * x;
	}

	/**
	 * Gets the minimum position value for a custom enemy.
	 */
	public static float customEnemyMinPos(MaModel model) {
		int x = ((model.confs[0][2] - model.parts[0][6]) * model.parts[0][8]) / model.ints[0];
		return 2.5f * x;
	}

	/**
	 * Gets the minimum position value for a data cat unit.
	 */
	public static int dataFormMinPos(MaModel model) {
		int x = ((model.confs[1][2] - model.parts[0][6]) * model.parts[0][8]) / model.ints[0];
		return 5 * x;

	}

	/**
	 * Gets the minimum position value for a custom cat unit.
	 */
	public static int customFormMinPos(MaModel model) {
		int x = (-model.parts[0][6] * model.parts[0][8]) / model.ints[0];
		return 5 * x;
	}

	/**
	 * Gets the boss spawn point for a castle.
	 * Basically 3200 + yx/10 + 0.9*z but the 0.9*z part appears to use a quirky rounding
	 */
	public static float bossSpawnPoint(int y, int z) {
		return (float) (3200 + (y * z / 10) + (9 * z + 8 * z % 10) / 10) / 4f;
	}

	public static float fltFpsDiv(float f) {
		return getConfig().fps60 ? f / 2f : f;
	}

	public static float fltFpsMul(float f) {
		return getConfig().fps60 ? f * 2f : f;
	}
}