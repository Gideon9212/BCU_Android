package common.util.anim;

import common.io.InStream;
import common.io.OutStream;
import common.system.files.FileData;
import common.system.files.VFile;
import common.util.BattleStatic;
import common.util.Data;

import java.io.PrintStream;
import java.util.*;

public class MaModel extends Data implements Cloneable, BattleStatic {

	public static MaModel newIns(FileData f) {
		if(f == null) {
			return new MaModel();
		}

		try {
			return new MaModel(f.readLine());
		} catch (Exception e) {
			System.out.println("Error Reading Mamodel data for " + f.getPath());
			e.printStackTrace();
			return new MaModel();
		}

	}

	public static MaModel newIns(String path) {
		return new MaModel(VFile.readLine(path));
	}

	public int n, m;
	public int[] ints = new int[3];
	public int[][] confs, parts;
	public String[] strs0;

	public Map<int[], Integer> status = new HashMap<>();

	public MaModel() {
		n = 1;
		m = 1;
		parts = new int[][] { { -1, -1, 0, 0, 0, 0, 0, 0, 1000, 1000, 0, 1000, 0, 0 } };
		ints = new int[] { 1000, 3600, 1000 };
		confs = new int[][] { { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 } };
		strs0 = new String[] { "def" };
	}

	protected MaModel(Queue<String> qs) {
		qs.poll();
		qs.poll();
		n = Integer.parseInt(qs.poll().trim());
		parts = new int[n][14];
		strs0 = new String[n];
		for (int i = 0; i < n; i++) {
			String[] ss = qs.poll().trim().split(",");
			for (int j = 0; j < 13; j++)
				parts[i][j] = Integer.parseInt(ss[j].trim());
			if (ss.length == 14)
				strs0[i] = restrict(ss[13]);
			else
				strs0[i] = "";
		}
		String[] ss = qs.poll().trim().split(",");
		for (int i = 0; i < 3; i++)
			ints[i] = Integer.parseInt(ss[i].trim());
		m = Math.min(Integer.parseInt(qs.poll().trim()), qs.size());
		confs = new int[m][6];
		for (int i = 0; i < m; i++) {
			ss = qs.poll().trim().split(",");
			for (int j = 0; j < 6; j++)
				confs[i][j] = Integer.parseInt(ss[j].trim());
		}
	}

	private MaModel(MaModel mm) {
		n = mm.n;
		m = mm.m;
		ints = mm.ints.clone();
		parts = new int[n][];
		confs = new int[m][];
		for (int i = 0; i < n; i++)
			parts[i] = mm.parts[i].clone();
		for (int i = 0; i < m; i++)
			confs[i] = mm.confs[i].clone();
		strs0 = mm.strs0.clone();
	}

	/**
	 * regulate check imgcut id and detect parent loop
	 */
	public void check(AnimD<?, ?> anim) {
		int ics = anim.imgcut.n;
		for (int[] p : parts) {
			if (p[2] >= ics)
				p[2] = 0;
			if (p[0] > n)
				p[0] = 0;
		}
		int[] temp = new int[parts.length];
		for (int i = 0; i < parts.length; i++)
			check(temp, i);
		for (int i = 0; i < parts.length; i++)
			if (temp[i] == 2)
				parts[i][0] = -1;
	}

	public void clearAnim(boolean[] bs, MaAnim[] as) {
		for (MaAnim ma : as) {
			List<Part> lp = new ArrayList<>();
			for (Part p : ma.parts)
				if (!bs[p.ints[0]])
					lp.add(p);
			ma.parts = lp.toArray(new Part[0]);
			ma.n = ma.parts.length;
		}
	}

	@Override
	public MaModel clone() {
		return new MaModel(this);
	}

	public int getChild(boolean[] bs) {
		int total = 0;
		int count = 1;
		while (count > 0) {
			count = 0;
			for (int i = 0; i < n; i++)
				if (!bs[i] && parts[i][0] >= 0 && bs[parts[i][0]]) {
					count++;
					total++;
					bs[i] = true;
				}
		}
		return total;
	}

	public void reorder(int[] move) {
		int[][] data = parts;
		String[] name = strs0;
		parts = new int[move.length][];
		strs0 = new String[move.length];
		for (int i = 0; i < n; i++)
			if (move[i] < 0 || move[i] >= data.length) {
				parts[i] = new int[] { 0, -1, 0, 0, 0, 0, 0, 0, 1000, 1000, 0, 1000, 0, 0 };
				strs0[i] = "new part";
			} else {
				parts[i] = data[move[i]];
				strs0[i] = name[move[i]];
			}

	}

	public void revert() {
		parts[0][8] *= -1;
		for (int[] sets : parts)
			sets[10] *= -1;
	}

	public void write(PrintStream ps) {
		ps.println("[mamodel]");
		ps.println(3);
		ps.println(n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 13; j++)
				ps.print(parts[i][j] + ",");
			ps.println(strs0[i]);
		}
		ps.println(ints[0] + "," + ints[1] + "," + ints[2]);
		ps.println(m);
		for (int i = 0; i < m; i++)
			for (int j = 0; j < confs[i].length; j++)
				ps.print(confs[i][j] + ",");
	}

	protected EPart[] arrange(EAnimI e) {
		EPart[] ents = new EPart[n];
		EPart.EBase base = new EPart.EBase(this, e.anim(), ents);
		for (int i = 0; i < n; i++)
			ents[i] = new EPart(base, parts[i], strs0[i], i);
		return ents;
	}

	protected void restore(InStream is) {
		n = is.nextInt();
		m = is.nextInt();
		ints = is.nextIntsB();
		parts = is.nextIntsBB();
		confs = is.nextIntsBB();
		strs0 = new String[n];
		for (int i = 0; i < n; i++)
			strs0[i] = is.nextString();
	}

	protected void write(OutStream os) {
		os.writeInt(n);
		os.writeInt(m);
		os.writeIntB(ints);
		os.writeIntBB(parts);
		os.writeIntBB(confs);
		for (String str : strs0)
			os.writeString(str);
	}

	/**
	 * detect loop
	 */
	private int check(int[] temp, int p) {
		if (temp[p] > 0)
			return temp[p];
		if (parts[p][0] == -1)
			return temp[p] = 1;
		temp[p] = 2;
		if (parts[p][0] >= parts.length)
			parts[p][0] = 0;
		return temp[p] = check(temp, parts[p][0]);
	}

}
