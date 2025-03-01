package common.util.stage;

import common.battle.StageBasis;
import common.io.json.JsonClass;
import common.io.json.JsonClass.JCConstructor;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonField;
import common.io.json.JsonField.GenType;
import common.pack.FixIndexList;
import common.pack.Identifier;
import common.system.Copable;
import common.util.unit.AbEnemy;
import common.util.unit.EneRand;
import common.util.unit.Enemy;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@JsonClass
public class SCDef implements Copable<SCDef> {

	@JsonClass
	public static class Line implements Cloneable {
		@JsonField(defval = "null")
		public Identifier<AbEnemy> enemy;
		@JsonField(defval = "1")
		public int number = 1;
		@JsonField(defval = "0")
		public int boss, group, spawn_0, spawn_1, respawn_0, respawn_1, castle_1, layer_0, kill_count;
		@JsonField(defval = "100")
		public int multiple = 100, mult_atk = 100, castle_0 = 100;
		@JsonField(defval = "9")
		public int layer_1 = 9;

		@JsonField(backCompat = JsonField.CompatType.FORK, defval = "0")
		public byte doorchance, doordis_0, doordis_1;
		@JsonField(backCompat = JsonField.CompatType.FORK, defval = "null")
		public Revival rev;

		@JCConstructor
		public Line() {
		}

		public Line(int[] arr) {
			enemy = Identifier.parseInt(arr[E], AbEnemy.class);
			number = arr[N];
			boss = arr[B];
			multiple = arr[M];
			group = arr[G];
			spawn_0 = arr[S0];
			respawn_0 = arr[R0];
			castle_0 = arr[C0];
			layer_0 = arr[L0];
			spawn_1 = arr[S1];
			respawn_1 = arr[R1];
			castle_1 = arr[C1];
			layer_1 = arr[L1];
			mult_atk = arr[M1];
			kill_count = arr[KC];
		}

		@Override
		public Line clone() {
			try {
				return (Line) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static final int SIZE = 15, E = 0, N = 1, S0 = 2, R0 = 3, R1 = 4, C0 = 5, L0 = 6, L1 = 7, B = 8, M = 9,
			S1 = 10, C1 = 11, G = 12, M1 = 13, KC = 14;

	@JsonField(defval = "isEmpty")
	public Line[] datas;
	@JsonField(gen = GenType.FILL, defval = "isEmpty")
	public final FixIndexList<SCGroup> sub = new FixIndexList<>(SCGroup.class);
	@JsonField(generic = { Identifier.class, Integer.class }, defval = "isEmpty")
	public final TreeMap<Identifier<AbEnemy>, Integer> smap = new TreeMap<>();
	@JsonField(defval = "0")
	public int sdef = 0;

	@JCConstructor
	public SCDef() {
	}

	public SCDef(int s) {
		datas = new Line[s];
	}

	public int allow(StageBasis sb, AbEnemy e) {
		Integer o = smap.getOrDefault(e.getID(), sdef);
		if (allow(sb, o, e))
			return o;
		return -1;
	}

	public boolean allow(StageBasis sb, int val, AbEnemy en) {
		Enemy e = null;

		if(en instanceof Enemy) {
			e = (Enemy) en;
		} else if(en instanceof EneRand) {
			Set<Enemy> enemies = en.getPossible();

			for(Enemy enemy : enemies)
				if(e == null || e.de.getWill() < enemy.de.getWill())
					e = enemy;

			if(e == null)
				return false;
		} else {
			System.out.println("W/SCDef | Unknown type of AbEnemy found : "+(en != null ? en.getClass().getName() : "Null"));
			return false;
		}

		if (sb.entityCount(1) >= sb.st.max - e.de.getWill())
			return false;
		if (val < 0 || val > 1000 || sub.get(val) == null)
			return true;
		SCGroup g = sub.get(val);
		return sb.entityCount(1, val) < g.getMax(sb.est.star);
	}

	public boolean contains(AbEnemy e) {
		if (e instanceof Enemy) {
			for (Line dat : datas) {
				if (dat.enemy == null)
					continue;

				if (dat.enemy.cls == EneRand.class) {
					EneRand rand = (EneRand) Identifier.get(dat.enemy);

					if (rand != null && rand.contains(e.getID(), dat.enemy))
						return true;
				} else if (dat.enemy.cls == Enemy.class) {
					if (dat.enemy.equals(e.getID()))
						return true;
				}
			}
		} else {
			for (Line dat : datas) {
				if (dat.enemy == null || dat.enemy.cls != EneRand.class)
					continue;

				EneRand rand = (EneRand) Identifier.get(dat.enemy);
				if (rand != null && (rand.equals(e) || rand.contains(e.getID(), dat.enemy)))
					return true;
			}
		}
		return false;
	}

	@Override
	public SCDef copy() {
		SCDef ans = new SCDef(datas.length);
		for (int i = 0; i < datas.length; i++)
			ans.datas[i] = datas[i].clone();
		ans.sdef = sdef;
		ans.smap.putAll(smap);
		sub.forEach((i, e) -> ans.sub.set(i, e.copy(i)));
		return ans;
	}

	public Set<Enemy> getAllEnemy() {
		Set<Enemy> l = new TreeSet<>();
		for (Line dat : datas)
			l.addAll(Identifier.getOr(dat.enemy, AbEnemy.class).getPossible());
		for (AbEnemy e : getSummon())
			l.addAll(e.getPossible());
		return l;
	}

	public Line[] getSimple() {
		return datas;
	}

	public Line getSimple(int i) {
		return datas[i];
	}

	@SuppressWarnings("unchecked")
	public Entry<Identifier<AbEnemy>, Integer>[] getSMap() {
		return smap.entrySet().toArray(new Entry[0]);
	}

	public Set<AbEnemy> getSummon() {
		Set<AbEnemy> ans = new TreeSet<>();
		Set<AbEnemy> temp = new TreeSet<>();
		Set<Enemy> pre = new TreeSet<>();
		Set<Enemy> post = new TreeSet<>();
		for (Line line : datas) {
			AbEnemy e = Identifier.get(line.enemy);
			if (e != null)
				pre.addAll(e.getPossible());
		}
		while (pre.size() > 0) {
			for (Enemy e : pre)
				temp.addAll(e.de.getSummon());
			ans.addAll(temp);
			post.addAll(pre);
			pre.clear();
			for (AbEnemy e : temp)
				pre.addAll(e.getPossible());
			pre.removeAll(post);
			temp.clear();
		}
		return ans;
	}

	public boolean isTrail() {
		for (Line data : datas)
			if (data.castle_0 > 100)
				return true;
		return false;
	}

	public boolean empty() {
		return datas.length + sdef == 0 && smap.isEmpty() && sub.size() == 0;
	}
}
