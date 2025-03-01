package common.util.stage;

import common.battle.LineUp;
import common.battle.data.MaskUnit;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.util.BattleStatic;
import common.util.Data;
import common.util.stage.MapColc.DefMapColc;
import common.util.unit.AbForm;
import common.util.unit.Form;

import java.util.LinkedList;

@JsonClass
public class Limit extends Data implements BattleStatic {

	public static class DefLimit extends Limit {

		public DefLimit(String[] strs) {
			int mid = Integer.parseInt(strs[0]);
			if(mid >= 22000 && mid < 22002)
				mid -= 18985; //3015
			StageMap map = DefMapColc.getMap(mid);

			setStar(Integer.parseInt(strs[1]));
			int sid = Integer.parseInt(strs[2]);
			if (sid != -1)
				map.list.get(sid).lim = this;
			else
				map.lim.add(this);
			rare = Integer.parseInt(strs[3]);
			num = Integer.parseInt(strs[4]);
			line = Integer.parseInt(strs[5]);
			min = Integer.parseInt(strs[6]);
			max = Integer.parseInt(strs[7]);
			group = UserProfile.getBCData().groups.getRaw(Integer.parseInt(strs[8]));
		}

	}

	@JsonClass
	public static class PackLimit extends Limit {

		@JsonField(defval = "isEmpty")
		public String name = "";

		public PackLimit() {
		}
	}

	@JsonField(defval = "0")
	public int rare, num, line, min, max;
	@JsonField(backCompat = JsonField.CompatType.FORK, defval = "0")
	public int star = 0, fa; //last var could be named forceAmount, but that'd take too much json space
	@JsonField(alias = Identifier.class, defval = "null")
	public CharaGroup group;
	@JsonField(alias = Identifier.class, defval = "null")
	public LvRestrict lvr;
	@JsonField(defval = "null||isBlank")
	public StageLimit stageLimit;

	@JsonField(io = JsonField.IOType.R)
	public int sid = -1; //Exclusively to parse sid from old packs


	/**
	 * for copy or combine only
	 */
	public Limit() {
	}

	public Limit(StageLimit slim) {
		stageLimit = slim;
	}

	@Override
	public Limit clone() {
		Limit l = new Limit();
		l.fa = fa;
		l.star = star;
		l.rare = rare;
		l.num = num;
		l.line = line;
		l.min = min;
		l.max = max;
		l.group = group;
		l.lvr = lvr;
		l.stageLimit = stageLimit != null ? stageLimit.clone() : null;
		return l;
	}

	public void combine(Limit l) {
		if (rare == 0)
			rare = l.rare;
		else if (l.rare != 0)
			rare &= l.rare;
		if (num * l.num > 0)
			num = Math.min(num, l.num);
		else
			num = Math.max(num, l.num);
		line |= l.line;
		min = Math.max(min, l.min);
		max = max > 0 && l.max > 0 ? Math.min(max, l.max) : (max + l.max);
		if (l.group != null) {
			if (group != null)
				group = group.combine(l.group);
			else
				group = l.group;
		}
		if (l.lvr != null) {
			if (lvr != null)
				lvr.combine(l.lvr);
			else
				lvr = l.lvr;
		}
		if (l.stageLimit != null)
			stageLimit = stageLimit != null ? stageLimit.combine(l.stageLimit) : l.stageLimit;
	}

	public boolean unusable(MaskUnit du, int price, byte row) {
		if (line > 0 && 2 - (line - row) != 1)
			return true;
		double cost = du.getPrice() * (1 + price * 0.5);
		if ((min > 0 && cost < min) || (max > 0 && cost > max))
			return true;
		Form f = du.getPack();
		if (rare != 0 && ((rare >> f.unit.rarity) & 1) == 0)
			return true;
		return group != null && !group.allow(f);
	}

	public boolean valid(LineUp lu) {
		if (group != null && group.type % 2 != 0) {
			SortedPackSet<Form> fSet = getValid(lu);
			if ((group.type == 1 && fSet.size() < fa) || (group.type == 3 && fSet.size() > fa))
				return false;
		}
		return lvr == null || lvr.isValid(lu);
	}

	public SortedPackSet<Form> getValid(LineUp lu) {
		SortedPackSet<Form> fSet = new SortedPackSet<>();
		boolean brek = false;
		for (AbForm[] fs : lu.fs) {
			for (AbForm f : fs) {
				if (brek = f == null)
					break;
				validForm(fSet, f);
			}
			if (brek)
				break;
		}
		return fSet;
	}

	private void validForm(SortedPackSet<Form> fSet, AbForm f) {
		if (f instanceof Form) {
			if (group.fset.contains(f))
				fSet.add((Form)f);
		} else if (f != null)
			for (AbForm ff : f.unit().getForms())
				validForm(fSet, ff);
	}

	private static LinkedList<Integer> formatStar(int star) {
		if (star <= 0)
			return new LinkedList<>();
		LinkedList<Integer> ints = new LinkedList<>();
		for (int i = 0; i < 4; i++)
			if ((star & (1 << i)) != 0)
				ints.add(i+1);
		return ints;
	}

	public void setStar(int newStar) {
		star = newStar < 0 ? 0 : 1 << newStar;
	}

	@Override
	public String toString() {
		if (star == 0)
			return "all stars";
		LinkedList<Integer> stars = formatStar(star);
		return stars + " star" + (stars.size() >= 2 ? "s" : "");
	}

	@JsonField(tag = "star", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public int oldStar() {
		for (int i = 0; i < 4; i++)
			if ((star & (1 << i)) != 0)
				return i;
		return -1;
	}

	public boolean none() {
		return fa + rare + line + num + min + max + fa == 0 && group == null && lvr == null && (stageLimit == null || stageLimit.isBlank());
	}
}
