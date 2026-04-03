package common.util.anim;

import common.io.InStream;
import common.io.OutStream;
import common.system.fake.FakeImage;
import common.system.files.FileData;
import common.system.files.VFile;
import common.util.Data;

import java.io.PrintStream;
import java.util.Queue;

public class ImgCut extends Data implements Cloneable {

	public static ImgCut newIns(FileData f) {
		if(f == null)
			return new ImgCut();

		try {
			return new ImgCut(f.readLine());
		} catch (Exception e) {
			System.out.println("Error Reading ImgCut data for " + f.getPath());
			e.printStackTrace();
			return new ImgCut();
		}
	}

	public static ImgCut newIns(String path) {
		Queue<String> lines = VFile.readLine(path);

		assert lines != null;

		return new ImgCut(lines);
	}

	public String name;
	public int n;
	public int[][] cuts;
	public String[] strs;

	public ImgCut() {
		n = 1;
		cuts = new int[][] { { 0, 0, 1, 1 } };
		strs = new String[] { "default" };
	}

	protected ImgCut(Queue<String> qs) {
		qs.poll();
		qs.poll();

		String line = qs.poll();

		name = restrict(line == null ? "" : line);

		line = qs.poll();

		n = Integer.parseInt(line == null ? "0" : line.trim());
		cuts = new int[n][4];
		strs = new String[n];
		for (int i = 0; i < n; i++) {
			line = qs.poll();

			String[] ss = (line == null ? "0, 0, 1, 1" : line).trim().split(",");
			for (int j = 0; j < 4; j++)
				cuts[i][j] = Integer.parseInt(ss[j].trim());
			if (ss.length == 5)
				strs[i] = restrict(ss[4]);
			else
				strs[i] = "";
		}
	}

	private ImgCut(ImgCut ic) {
		name = ic.name;
		n = ic.n;
		cuts = new int[n][];
		for (int i = 0; i < n; i++)
			cuts[i] = ic.cuts[i].clone();
		strs = ic.strs.clone();
	}

	public void addLine(int ind) {
		int[][] data = cuts;
		String[] name = strs;

		cuts = new int[++n][];
		strs = new String[n];

		for (int i = 0; i < data.length; i++) {
			cuts[i] = data[i];
			strs[i] = name[i];
		}

		if (ind >= 0)
			cuts[n - 1] = cuts[ind].clone();
		else
			cuts[n - 1] = new int[] { 0, 0, 1, 1 };
		strs[n - 1] = "";
	}

	@Override
	public ImgCut clone() {
		return new ImgCut(this);
	}

	public FakeImage[] cut(FakeImage bimg) {
		int w = bimg.getWidth();
		int h = bimg.getHeight();
		FakeImage[] parts = new FakeImage[n];
		for (int i = 0; i < n; i++) {
			int[] cut = cuts[i].clone();
			cut[0] = Math.max(0, Math.min(cut[0], w - 1));
			cut[1] = Math.max(0, Math.min(cut[1], h - 1));
			cut[2] = Math.max(1, Math.min(cut[2], w - cut[0]));
			cut[3] = Math.max(1, Math.min(cut[3], h - cut[1]));
			parts[i] = bimg.getSubimage(cut[0], cut[1], cut[2], cut[3]);
		}
		return parts;
	}

	public void write(PrintStream ps) {
		ps.println("[imgcut]");
		ps.println("0");
		ps.println(name);
		ps.println(n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 4; j++)
				ps.print(cuts[i][j] + ",");
			ps.println(strs[i]);
		}
	}

	protected void restore(InStream is) {
		n = is.nextInt();
		cuts = is.nextIntsBB();
		strs = new String[n];
		for (int i = 0; i < n; i++)
			strs[i] = is.nextString();
	}

	protected void write(OutStream os) {
		os.writeInt(n);
		os.writeIntBB(cuts);
		for (String str : strs)
			os.writeString(str);
	}

}
