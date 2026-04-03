package common.battle;

import common.CommonStatic;
import common.battle.data.PCoin;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.PackData;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.util.BattleStatic;
import common.util.Data;
import common.util.stage.CharaGroup;
import common.util.unit.*;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

@JsonClass
public class LineUp extends Data {

	public static class ComboBuff implements BattleStatic {
		public int[] inc = new int[C_TOT];
		public CharaGroup cg;

		public ComboBuff() {
			this(null);
		}
		public ComboBuff(CharaGroup cg) {
			if (cg != null)
				this.cg = cg;
		}
	}

	@JsonField(generic = { Identifier.class, Level.class })
	public final TreeMap<Identifier<AbUnit>, Level> map = new TreeMap<>();

	@JsonField(alias = AbForm.AbFormJson.class)
	public final AbForm[][] fs = new AbForm[2][5];
	public final IForm[][] efs = new IForm[2][5];

	public LinkedList<ComboBuff> incs = new LinkedList<>();
	private int[][] loc = new int[2][5];
	public SortedPackSet<Combo> coms = new SortedPackSet<>();

	private boolean updating = false;

	/**
	 * new LineUp object
	 */
	protected LineUp() {
		renew();
	}

	/**
	 * clone a LineUp object
	 */
	protected LineUp(LineUp ref) {
		for (int i = 0; i < 2; i++)
			System.arraycopy(ref.fs[i], 0, fs[i], 0, 5);
		for (Entry<Identifier<AbUnit>, Level> e : ref.map.entrySet()) {
			map.put(e.getKey(), e.getValue().clone());
		}

		renew();
	}

	/**
	 * shift all cats to lowest index possible
	 */
	public void arrange() {
		for (int i = 0; i < 10; i++)
			if (getFS(i) == null)
				for (int j = i + 1; j < 10; j++)
					if (getFS(j) != null) {
						setFS(getFS(j), i);
						setFS(null, j);
						break;
					} else if (j == 9)
						return;
	}

	/**
	 * test whether contains certain combo
	 */
	public boolean contains(Combo c) {
		for (Combo com : coms)
			if (com == c)
				return true;
		return false;
	}

	/**
	 * get level of an Unit, if no date recorded, record default one
	 */
	public synchronized Level getLv(AbForm u) {
		if (!map.containsKey(u.getID()))
			setLv(u.unit(), u.unit().getPrefLvs());
		if (u instanceof UniRand)
			return map.get(u.getID());
		return validateLevel((Form) u, map.get(u.getID()));
	}

	/**
	 * return how much space from 1st row a combo will need to put in this lineup
	 */
	public int occupance(Combo c) {
		Form[] com = c.forms;
		int rem = com.length;
		for (Form form : com)
			for (int j = 0; j < 5; j++)
				if (fs[0][j] instanceof Form) {
					Form f = (Form)fs[0][j];
					if (f == null)
						continue;
					if (f.unit == form.unit)
						rem--;
				}
		return rem;
	}

	@OnInjected
	public void load() {
		renew();
		map.keySet().removeIf(u -> {
			for (int i = 0; i < 2; i++)
				for (int j = 0; j < 5; j++) {
					if (fs[i][j] == null)
						break;
					if (u.equals(fs[i][j].getID()))
						return false;
				}
			return true;
		});
		reAddDefs();
	}

	public void reAddDefs() {
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				if (fs[i][j] == null)
					break;
				if (!map.containsKey(fs[i][j].getID()))
					map.put(fs[i][j].getID(), fs[i][j].unit().getPrefLvs());
			}
	}
	public void removeDefs() {
		load();
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				if (fs[i][j] instanceof Form) {
					Identifier<AbUnit> u = fs[i][j].getID();
					Level lv = map.get(u);
					if ((CommonStatic.getPrefLvs().uni.containsKey(u) && CommonStatic.getPrefLvs().uni.get(u).equals(lv))
							|| CommonStatic.getPrefLvs().equalsDef(u.get().getForms()[u.get().getForms().length - 1], lv))
						map.remove(u);
				} else if (fs[i][j] == null)
					break;
			}
	}

	public void renew() {
		validate();
		renewEForm();
		renewCombo();
	}

	/**
	 * apply a combo
	 */
	public void set(Combo co) {
		Form[] com = co.forms;
		// if a unit in the combo is already present in the lineup
		boolean[] exi = new boolean[com.length];
		// the number of units required to inject
		int rem = com.length;
		byte row = co.row == 0 || co.row == 3 ? 0 : (byte)(co.row-1);
		for (int i = 0; i < com.length; i++)
			for (int j = 0; j < 5; j++) {
				if (fs[row][j] == null || fs[row][j] instanceof UniRand)
					continue;

				Form f = (Form)fs[0][j];
				int formID = com[i].fid;
				if (f.unit == com[i].unit) {
					exi[i] = true;
					if (f.fid < formID)
						fs[row][j] = f.unit.forms[formID];
					loc[row][j]++;
					rem--;
				}
			}
		// number of units not present in any combo
		int free = 0;
		for (int i = 0; i < 5; i++)
			if (loc[row][i] == 0)
				free++;

		if (free < rem) {
			// required to remove some combo

			int del = rem - free;
			while (del > 0) {
				Combo c = coms.remove(0);
				for (int i = 0; i < c.forms.length; i++) {
					if (c.forms[i] == null)
						break;
					for (int j = 0; j < 5; j++) {
						if (fs[row][j] == null)
							break;
						if (fs[row][j] instanceof UniRand)
							continue;
						Form f = (Form)fs[row][j];
						if (f.unit != c.forms[i].unit)
							continue;
						loc[row][j]--;
						if (loc[row][j] == 0)
							del--;
						break;
					}
				}
			}
		}
		for (int i = 0; i < 5; i++)
			for (Form form : com)
				if (fs[1-row][i] != null && fs[1-row][i] instanceof Form && fs[1-row][i].unit() == form.unit) {
					fs[1-row][i] = null;
					break;
				}
		arrange();
		int emp = 0;
		for (int i = 0; i < 10; i++)
			if (getFS(i) == null)
				emp++;
		if (emp < rem) {
			for (int i = 10 - rem; i < 10 - emp; i++)
				setFS(null, i);
			emp = rem;
		}
		int p = 0, r = 0, i = 0, j = 10 - emp;
		while (r < rem) {
			while (loc[row][i] != 0)
				i++;
			while (exi[p])
				p++;
			setFS(getFS(i), j++);
			Form c = com[p++];
			setFS(c, i++);
			r++;
		}
		renew();
	}

	/**
	 * set level record of an Unit
	 */
	public synchronized void setLv(AbUnit u, Level lv) {
		boolean sub = updating;
		updating = true;

		Level l = map.get(u.getID());
		if (l != null)
			l.setLvs(lv);
		else {
			l = lv.clone();
			map.put(u.getID(), l);
		}
		if (!sub)
			renewEForm();
		updating &= sub;
	}

	/**
	 * set orb data of an Unit
	 */
	public synchronized void setOrb(Unit u, Level lv, int[][] orbs) {
		// lvs must be generated before doing something with orbs
		boolean sub = updating;
		updating = true;

		Level l = map.get(u.id);

		if (l != null) {
			l.setLvs(lv);
			l.setOrbs(orbs);
		} else {
			l = lv.clone();
			l.setOrbs(orbs);

			map.put(u.id, l);
		}

		if (!sub)
			renewEForm();

		updating &= sub;
	}

	/**
	 * return whether implementing this combo will replace other combo
	 */
	public boolean willRem(Combo c) {
		int free = 0;
		byte[] rows = c.row == 0 || c.row == 3 ? new byte[]{0,2} : new byte[]{(byte)(c.row-1), c.row};
		for (byte j = rows[0]; j < rows[1]; j++) {
			free = 0;
			for (int i = 0; i < 5; i++)
				if (fs[j][i] == null)
					free++;
				else if (loc[j][i] == 0) {
					boolean b = true;

					for (Form is : c.forms)
						if (fs[j][i].unit() == is.unit) {
							b = false;
							break;
						}
					if (b)
						free++;
				}
		}
		return free < occupance(c);
	}

	/**
	 * set slot using 1 dim index
	 */
	protected void setFS(AbForm f, int i) {
		fs[i / 5][i % 5] = f;
	}

	/**
	 * get Form from 1 dim index
	 */
	private AbForm getFS(int i) {
		return fs[i / 5][i % 5];
	}

	/**
	 * check combo information
	 */
	public void renewCombo() {
		coms.clear();
		incs.clear();
		incs.add(new ComboBuff());
		loc = new int[2][5];
		CommonStatic.Config cfg = CommonStatic.getConfig();
		for (PackData p : UserProfile.getAllPacks()) {
			if (cfg.excludeCombo.contains(p.getSID()))
				continue;

			for (Combo c : p.combos)
				renewCombo(c, false);
		}
	}

	/**
	 * AAA	A
	 * @param cg Only units affected. Can be null for universal buffs
	 * @param addIfAbsent duh
	 * @return Buffs acccounting restrictions
	 */
	public ComboBuff getCombosFor(CharaGroup cg, boolean addIfAbsent) {
		if (cg == null)
			return incs.getFirst();
		ComboBuff buff = null;
		for (ComboBuff cb : incs)
			if (cb.cg == cg) {
				buff = cb;
				break;
			}
		if (buff == null && addIfAbsent)
			incs.add(buff = new ComboBuff(cg));
		return buff;
	}
	public void validateIncs() {
		incs.removeIf(inc -> {
			if (inc.cg == null)
				return false;
			for (Combo c : coms)
				if (c.group == inc.cg)
					return false;
			return true;
		});
	}

	public void renewCombo(Combo c, boolean locChk) {
		if (locChk)
			for (int j = 0; j < 2; j++)
				for (int i = 0; i < 5; i++)
					loc[j][i]--;

		boolean b = true;
		byte[] rows = c.row == 0 || c.row == 3 ? new byte[]{0,2} : new byte[]{(byte)(c.row-1), c.row};
		byte foundRow = (byte)(c.row == 3 ? -2 : -1);//For combo row 3 check
		for (int i = 0; i < c.forms.length; i++) {
			Form fu = c.forms[i];
			if (fu == null)
				break;
			boolean b0 = false;
			for (byte k = rows[0]; k < rows[1]; k++)
				for (int j = 0; j < 5; j++) {
					if (fs[k][j] instanceof UniRand)
						continue;
					Form f = (Form) fs[k][j];
					if (f == null)
						break;
					if (f.unit != fu.unit || !c.correctForm(i, f.fid))
						continue;
					if (foundRow == -2)
						foundRow = k;
					b0 = foundRow == -1 || foundRow == k;
					break;
				}
			if (b0)
				continue;
			b = false;
			break;
		}
		ComboBuff buff = getCombosFor(c.group, b);
		if (b) {
			if (!coms.add(c))//It's a set so it prevents combo buffs from being applied twice
				return;
			buff.inc[c.type] += CommonStatic.getBCAssets().values[c.type][c.lv];
			for (int i = 0; i < c.forms.length; i++) {
				Form fu = c.forms[i];
				for (byte k = rows[0]; k < rows[1]; k++)
					for (int j = 0; j < 5; j++) {
						if (!(fs[rows[0]][j] instanceof Form))
							continue;
						Form f = (Form) fs[rows[0]][j];
						if (f.unit == fu.unit && c.correctForm(i, f.fid))
							loc[k][j]++;
					}
				}
		} else
			removeCombo(c);
	}

	public void removeCombo(Combo c) {
		if (!coms.contains(c))
			return;
		coms.remove(c);
		ComboBuff buff = getCombosFor(c.group, false);
		buff.inc[c.type] -= CommonStatic.getBCAssets().values[c.type][c.lv];
		byte[] rows = c.row == 0 || c.row == 3 ? new byte[]{0,2} : new byte[]{(byte)(c.row-1), c.row};
		for (int i = 0; i < c.forms.length; i++) {
			Form fu = c.forms[i];
			for (byte k = rows[0]; k < rows[1]; k++)
				for (int j = 0; j < 5; j++) {
					if (!(fs[rows[0]][j] instanceof Form))
						continue;
					Form f = (Form) fs[rows[0]][j];
					if (f.unit == fu.unit && c.correctForm(i, f.fid))
						loc[k][j]--;
				}
		}
		validateIncs();
	}

	private void renewEForm() {
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++)
				if (fs[i][j] == null)
					efs[i][j] = null;
				else
					efs[i][j] = IForm.newIns(fs[i][j], getLv(fs[i][j]));
	}

	private void validate() {
		for (int i = 0; i < 10; i++)
			if (getFS(i) != null) {
				Identifier<AbUnit> id = getFS(i).getID();
				int f = getFS(i).getFid();
				AbUnit u = Identifier.get(id);
				if (u == null || f >= u.getForms().length || u.getForms()[f] == null)
					setFS(null, i);
			}
		arrange();
	}
	private Level validateLevel(Form f, Level lv) {
		Unit u = f.unit;

		int maxTalent = 0;
		PCoin pc = null;

		for(Form form : u.forms) {
			if(form.du.getPCoin() != null && form.du.getPCoin().max.length > maxTalent) {
				pc = form.du.getPCoin();
				maxTalent = pc.max.length;
			}
		}

		int plusLv = Math.max(0, lv.getLv() - u.max);
		lv.setLevel(Math.max(1, Math.min(u.max, lv.getLv())));
		lv.setPlusLevel(Math.max(0, Math.min(u.maxp, lv.getPlusLv() + plusLv)));

		if(pc != null) {
			int[] max = pc.max;

			if(lv.getTalents().length < max.length) {
				int[] talents = new int[max.length];

				for(int i = 0; i < lv.getTalents().length; i++) {
					talents[i] = lv.getTalents()[i];
				}

				if (max.length - lv.getTalents().length >= 0)
					System.arraycopy(max, lv.getTalents().length, talents, lv.getTalents().length, max.length - lv.getTalents().length);

				lv.setTalents(talents);
			}
		}

		return lv;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof LineUp))
			return false;
		LineUp lu = (LineUp)obj;
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				if (fs[i][j] == null && lu.fs[i][j] == null)
					break;
				if (fs[i][j] == null || lu.fs[i][j] == null)
					return false;
				if (fs[i][j] != lu.fs[i][j] || !getLv(fs[i][j]).equals(lu.getLv(fs[i][j])))
					return false;
			}
		return true;
	}
}