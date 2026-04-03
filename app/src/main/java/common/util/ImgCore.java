package common.util;

import common.CommonStatic;
import common.io.assets.Admin.StaticPermitted;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;

import java.util.ArrayList;

public class ImgCore extends Data {

	@StaticPermitted
	public static final String[] NAME = new String[] { "opacity", "color", "accuracy", "scale" };
	@StaticPermitted
	public static final String[] VAL = new String[] { "fast", "default", "quality" };

	public static void set(FakeGraphics g) {
		if (CommonStatic.getConfig().battle)
			return;
		for (int i = 0; i < 4; i++)
			g.setRenderingHint(i, CommonStatic.getConfig().ints[i]);
	}

	protected static void drawImg(FakeGraphics g, FakeImage bimg, P piv, P sc, float opa, int glow,
			float extendX, float extendY) {
		boolean glowSupport = (glow >= 1 && glow <= 3) || glow == -1;
		if (opa < CommonStatic.getConfig().fullOpa * 0.01 - 1e-5) {
			if (glowSupport)
				g.setComposite(FakeGraphics.BLEND, (int) (opa * 256), glow);
			else
				g.setComposite(FakeGraphics.TRANS, (int) (opa * 256), 0);
		} else {
			if (glowSupport)
				g.setComposite(FakeGraphics.BLEND, 256, glow);
			else
				g.setComposite(FakeGraphics.DEF, 0, 0);
		}
		if (extendX == 0)
			extendX = 1;
		if (extendY == 0)
			extendY = 1;
		if (extendX == 1 && extendY == 1)
			drawImage(g, bimg, -piv.x, -piv.y, sc.x, sc.y);
		else {
			float oldExtendY = extendY;
			float x = -piv.x;
			float y = -piv.y;
			int w = bimg.getWidth();
			int h = bimg.getHeight();

			while (extendX > 0 || extendY > 0) {
				float scx = extendX < 1 ? sc.x * extendX : sc.x;
				while (extendY > 0) {
					float scy = extendY < 1 ? sc.y * extendY : sc.y;
					FakeImage pimg = scx == sc.x && scy == sc.y ? bimg : bimg.getSubimage(0, 0,
							scx == sc.x ? w : (int)Math.max(1, w * extendX), scy == sc.y ? h : (int)Math.max(1, h * extendY));
					drawImage(g, pimg, x, y, scx, scy);
					y += scy;
					extendY--;
				}
				x += scx;
				extendX--;
				if (extendX > 0) {
					y = -piv.y;
					extendY = oldExtendY;
				}
			}
		}
		g.setComposite(FakeGraphics.DEF, 0, 0);
	}

	protected static void drawRandom(FakeGraphics g, ArrayList<Integer> rands, FakeImage[] bimg, P piv, P sc, float opa, int glow, float extendX, float extendY) {
		if (extendX == 0)
			extendX = 1;
		if (extendY == 0)
			extendY = 1;

		int i = 0;
		float oldExtendY = extendY;
		float oldY = piv.y;

		while (extendX > 0 || extendY > 0) {
			while (extendY > 0) {
				int data;
				if (i >= rands.size()) {
					data = (int) (Math.random() * bimg.length);
					rands.add(data);
				} else
					data = rands.get(i);
				drawImg(g, bimg[data], piv, sc, opa, glow, Math.min(extendX, 1), Math.min(extendY, 1));
				piv.y -= sc.y;
				extendY--;
				i++;
			}
			piv.x -= sc.x;
			extendX--;
			if (extendX > 0) {
				piv.y = oldY;
				extendY = oldExtendY;
			}
		}
	}

	protected static void drawSca(FakeGraphics g, P piv, P sc) {
		g.setColor(FakeGraphics.RED);
		g.fillOval(-10, -10, 20, 20);
		g.drawOval(-40, -40, 80, 80);
		int x = (int) -piv.x;
		int y = (int) -piv.y;
		if (sc.x < 0)
			x += sc.x;
		if (sc.y < 0)
			y += sc.y;
		int sx = (int) Math.abs(sc.x);
		int sy = (int) Math.abs(sc.y);
		g.drawRect(x, y, sx, sy);
		g.setColor(FakeGraphics.YELLOW);
		g.drawRect(x - 40, y - 40, sx + 80, sy + 80);
	}

	private static void drawImage(FakeGraphics g, FakeImage bimg, float x, float y, float w, float h) {
		int ix = Math.round(x);
		int iy = Math.round(y);
		int iw = Math.round(w);
		int ih = Math.round(h);
		g.drawImage(bimg, ix, iy, iw, ih);
	}

}