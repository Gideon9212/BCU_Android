package common.util.anim;

import common.io.InStream;
import common.io.OutStream;
import common.system.files.FileData;
import common.system.files.VFile;
import common.util.BattleStatic;
import common.util.Data;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.Queue;

public class MaAnim extends Data implements BattleStatic {

	public static MaAnim newIns(FileData f, int[] version) {
		if(f == null)
			return new MaAnim();

		try {
			return new MaAnim(f.readLine(), version);
		} catch (Exception e) {
			System.out.println("Error reading " + f.getPath());
			e.printStackTrace();
			return new MaAnim();
		}
	}

	public static MaAnim newIns(String str) {
		return new MaAnim(VFile.readLine(str), null);
	}

	public int n;
	public Part[] parts;

	public int max = 1, len = 1;

	public MaAnim() {
		n = 0;
		parts = new Part[0];
	}

	public MaAnim(@Nullable Queue<String> qs, int[] version) {
		if (qs != null) {
			qs.poll();
			qs.poll();

			n = Integer.parseInt(qs.poll().trim());

			parts = new Part[n];

			for (int i = 0; i < n; i++)
				parts[i] = new Part(qs, version);

			validate();
		} else {
			n = 0;
			parts = new Part[n];

			validate();
		}
	}

	private MaAnim(MaAnim ma) {
		n = ma.n;
		parts = new Part[n];
		for (int i = 0; i < n; i++)
			parts[i] = ma.parts[i].clone();
		validate();
	}

	@Override
	public MaAnim clone() {
		return new MaAnim(this);
	}

	public void revert() {
		for (Part p : parts)
			if (p.ints[1] == 11)
				for (int[] move : p.moves)
					move[1] *= -1;
	}

	public void validate() {
		for (Part part : parts)
			if (part.ints[1] == 2)
				for (int j = 0; j < part.moves.length; j++)
					if (part.moves[j][1] < 0)
						part.moves[j][1] = 0;
		max = 1;
		for (int i = 0; i < n; i++)
			max = Math.max(max, parts[i].getMax());
		len = max;
	}

	public void write(PrintStream ps) {
		ps.println("[maanim]");
		ps.println("1");
		ps.println(parts.length);
		for (Part p : parts)
			p.write(ps);
	}

	protected void restore(InStream is) {
		n = is.nextInt();
		parts = new Part[n];
		for (int i = 0; i < n; i++) {
			parts[i] = new Part();
			parts[i].restore(is);
		}
		validate();
	}

	protected void update(float f, EAnimD<?> eAnim, boolean rotate) {
		if (rotate)
			f %= max + 1;
		if (f == 0)
			for (EPart e : eAnim.ent)
				e.setValue(parts);

		for (int i = 0; i < n; i++) {
			int loop = parts[i].ints[2];
			int smax = parts[i].max;
			int fir = parts[i].fir;
			int lmax = smax - fir;
			boolean prot = rotate || loop == -1;
			float frame;
			if (prot) {
				int mf = loop == -1 ? smax : max + 1;
				frame = mf == 0 ? 0 : loop == -1 ? (f + parts[i].off) % mf : (f % mf) + parts[i].off;
			} else
				frame = f + parts[i].off;
			if (loop > 0 && lmax != 0) {
				if (frame > fir + loop * lmax) {
					parts[i].ensureLast(eAnim.ent);
					continue;
				}
				if (frame <= fir);
				else if (frame < fir + loop * lmax)
					frame = fir + (frame - fir) % lmax;
				else
					frame = smax;
			}
			parts[i].update(frame, eAnim.ent);
		}
		eAnim.sort();
	}

	protected void write(OutStream os) {
		os.writeInt(n);
		for (Part p : parts)
			p.write(os);
	}

}