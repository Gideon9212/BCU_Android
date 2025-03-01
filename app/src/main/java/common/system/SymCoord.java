package common.system;

import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;

public class SymCoord {

	public FakeGraphics g;
	public float r, x, y;
	public int type;

	private final P size = new P(0, 0);
	private final P pos = new P(0, 0);

	public SymCoord(FakeGraphics fg, float R, float X, float Y, int t) {
		g = fg;
		r = R;
		x = X;
		y = Y;
		type = t;
	}

	public P draw(FakeImage... fis) {
		setSize(0, 0);
		for (FakeImage f : fis) {
			size.x += f.getWidth();
			size.y = Math.max(size.y, f.getHeight());
		}
		size.times(r);
		setPos(x, y);
		if ((type & 1) > 0)
			pos.x -= size.x;
		if ((type & 2) > 0)
			pos.y -= size.y;
		for (FakeImage f : fis) {
			if (r == 1)
				g.drawImage(f, pos.x, pos.y);
			else
				g.drawImage(f, pos.x, pos.y, f.getWidth() * r, f.getHeight() * r);
			pos.x += f.getWidth() * r;
		}
		return size;
	}

	private void setPos(float x, float y) {
		pos.x = x;
		pos.y = y;
	}

	private void setSize(float x, float y) {
		size.x = x;
		size.y = y;
	}

}
