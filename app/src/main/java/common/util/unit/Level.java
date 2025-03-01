package common.util.unit;

import common.battle.data.PCoin;
import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonField;
import common.util.BattleStatic;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

@JsonClass(noTag = NoTag.LOAD)
public class Level implements BattleStatic, LevelInterface {
	@JsonField(defval = "50")
	private int level = 50;
	@JsonField(defval = "0")
	private int plusLevel;
	@Nonnull
	@JsonField(defval = "this.noTalent")
	private int[] talents = new int[0];
	@JsonField(defval = "null")
	private int[][] orbs = null;

	public boolean noTalent() {
		return talents.length == 0;
	}

	public static Level lvList(AbUnit u, int[] arr, int[][] orbs) {
		int talentNumber = 0;
		PCoin coin = null;

		for(Form f : u.getForms()) {
			PCoin pc = f.du.getPCoin();
			if(pc != null && talentNumber < pc.max.length) {
				talentNumber = pc.max.length;
				coin = pc;
			}
		}
		Level lv = new Level(talentNumber);

		if (arr.length > 0) {
			lv.level = Math.max(1, Math.min(arr[0], u.getMaxLv()));
			if (u.getMaxPLv() != 0 && arr.length > 1)
				lv.plusLevel = Math.max(0, Math.min(arr[1], u.getMaxPLv()));
		}
		if(coin != null) {
			int[] talents = new int[coin.max.length];
			int min = u.getMaxPLv() != 0 ? 2 : 1;
			if(arr.length > min)
				System.arraycopy(arr, min, talents, 0, Math.min(talents.length, arr.length - min));

			lv.setTalents(talents);
		}
		lv.orbs = orbs;
		return lv;
	}

	public static String lvString(Level lvs) {
		StringBuilder str = new StringBuilder().append("Lv.").append(lvs.getLv()).append(" + ").append(lvs.getPlusLv());
		if (lvs.getTalents().length > 0) {
			str.append(" {");
			for (int i = 0; i < lvs.getTalents().length - 1; i++)
				str.append(lvs.getTalents()[i]).append(",");
			str.append(lvs.getTalents()[lvs.getTalents().length - 1]).append("}");
		}
		return str.toString();
	}

	@JsonClass.JCConstructor
	public Level() {
	}

	public Level(int talentNumber) {
		talents = new int[talentNumber];
	}

	public Level(int level, int plusLevel, @Nonnull int[] talents) {
		this.level = level;
		this.plusLevel = plusLevel;
		this.talents = talents.clone();
	}

	public Level(int level, int plusLevel, @Nonnull int[] talents, int[][] orbs) {
		this(level, plusLevel, talents);
		setOrbs(orbs);
	}

	@Override
	public Level clone() {
		try {
			return (Level) super.clone();
		} catch (CloneNotSupportedException ignored) {
			if (orbs != null)
				return new Level(level, plusLevel, talents, orbs.clone());
			return new Level(level, plusLevel, talents);
		}
	}

	public int getLv() {
		return level;
	}

	public int getPlusLv() {
		return plusLevel;
	}

	public int getTotalLv() {
		return level + plusLevel;
	}

	public int[][] getOrbs() {
		return orbs;
	}

	public int[] getTalents() {
		return talents;
	}

	public void setLevel(int lv) {
		level = lv;
	}

	public void setPlusLevel(int plusLevel) {
		this.plusLevel = plusLevel;
	}

	public void setTalents(@Nonnull int[] talents) {
		this.talents = talents.clone();
	}

	public void setLvs(Level lv) {
		level = Math.max(1, lv.level);
		plusLevel = lv.plusLevel;

		if(lv.talents.length < talents.length)
			System.arraycopy(lv.talents, 0, talents, 0, lv.talents.length);
		else
			talents = lv.talents.clone();

		if (lv.orbs != null)
			orbs = lv.orbs.clone();
	}

	public void setOrbs(int[][] orb) {
		if (orb == null) {
			orbs = null;
			return;
		}
		boolean valid = true;
		for (int[] data : orb) {
			if (data == null) {
				valid = false;
				break;
			}
			if (data.length == 0)
				continue;
			if (data.length != 3) {
				valid = false;
				break;
			}
		}
		if (valid)
			orbs = orb;
	}

	@JsonField(tag = "lvs", io = JsonField.IOType.R, generic = Integer.class)
	public void parseOldLevel(ArrayList<Integer> levels) {
		if (!levels.isEmpty()) {
			level = levels.get(0);
			if (levels.size() > 1) {
				talents = new int[levels.size() - 1];

				for (int i = 0; i < talents.length; i++)
					talents[i] = levels.get(i + 1);
			}
		}
	}

	@Override
	public String toString() {
		return "Level{" +
				"level=" + level +
				", plusLevel=" + plusLevel +
				", talents=" + Arrays.toString(talents) +
				", orbs=" + Arrays.deepToString(orbs) +
				'}';
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Level))
			return false;
		Level lv = (Level) obj;
		if (level + plusLevel != lv.level + lv.plusLevel)
			return false;
		return Arrays.equals(talents, lv.talents) && Arrays.deepEquals(orbs, lv.orbs);
	}
}