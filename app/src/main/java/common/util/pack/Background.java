package common.util.pack;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.CommonStatic.BCAuxAssets;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.IndexContainer.IndexCont;
import common.pack.IndexContainer.Indexable;
import common.pack.PackData;
import common.pack.Source;
import common.pack.UserProfile;
import common.system.P;
import common.system.VImg;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeImage.Marker;
import common.system.files.VFile;
import common.util.Data;
import common.util.anim.AnimI;
import common.util.anim.EAnimD;
import common.util.anim.ImgCut;
import common.util.pack.bgeffect.BackgroundEffect;

import java.util.Queue;
import java.util.function.Consumer;

@IndexCont(PackData.class)
@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class Background extends AnimI<Background, Background.BGWvType> implements Indexable<PackData, Background> {

	public enum BGWvType implements AnimI.AnimType<Background, BGWvType> {
		ENEMY, UNIT
	}

	public static final int BG = 0, TOP = 20, shift = 65; // in pix

	public static void read(Consumer<Double> prog) {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		String path = "./org/battle/bg/";
		for (VFile vf : VFile.get("./org/battle/bg").list()) {
			String name = vf.getName();
			if (name.length() != 11 || !name.endsWith(".imgcut"))
				continue;
			aux.iclist.add(ImgCut.newIns(path + name));
		}
		Queue<String> qs = VFile.readLine("./org/battle/bg/bg.csv");
		qs.poll();

		String q;

		int loaded = 0, tot = qs.size();
		while((q = qs.poll()) != null) {
			int[] ints = CommonStatic.parseIntsN(q);

			if((ints.length < 16 || ints[15] == -1) && VFile.get("./org/img/bg/bg" + Data.trio(ints[0]) + ".png") == null) {
				continue;
			}

			Background bg = new Background(ints);
			switch (bg.id.id) {
				case 2:
				case 14:
				case 26:
				case 34:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_STAR, BackgroundEffect.class);
					break;
				case 33:
				case 58:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_RAIN, BackgroundEffect.class);
					break;
				case 13:
				case 15:
				case 72:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_BUBBLE, BackgroundEffect.class);
					break;
				case 40:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_FALLING_SNOW, BackgroundEffect.class);
					break;
				case 3:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_SNOW, BackgroundEffect.class);
					break;
				case 27:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_SNOWSTAR, BackgroundEffect.class);
					break;
				case 46:
				case 47:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_BLIZZARD, BackgroundEffect.class);
					break;
				case 55:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_SHINING, BackgroundEffect.class);
					break;
				case 81:
				case 101:
				case 123:
				case 146:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_BALLOON, BackgroundEffect.class);
					break;
				case 41:
				case 75:
					bg.bgEffect = Identifier.rawParseInt(BG_EFFECT_ROCK, BackgroundEffect.class);
					break;
			}

			switch (bg.id.id) {
				case 13:
					bg.overlayAlpha = 51;
					bg.overlay = new int[][] {
							{0, 226, 255},
							{255, 255, 255}
					};
					break;
				case 15:
					bg.overlayAlpha = 51;
					bg.overlay = new int[][] {
							{0, 73, 173},
							{66, 187, 255}
					};
					break;
				case 19:
					bg.overlayAlpha = 51;
					bg.overlay = new int[][] {
							{160, 33, 32},
							{240, 169, 54}
					};
					break;
				case 46:
				case 47:
					bg.overlayAlpha = 67;
					bg.overlay =  new int[][] {
							{255, 255, 255},
							{255, 255, 255}
					};
					break;
				case 71:
					bg.overlayAlpha = 51;
					bg.overlay = new int[][] {
							{145, 45, 5},
							{235, 160, 60}
					};
					break;
				case 72:
					bg.overlayAlpha = 51;
					bg.overlay = new int[][] {
							{0, 35, 125},
							{0, 0, 0}
					};
					break;
				case 73:
					bg.overlayAlpha = 51;
					bg.overlay = new int[][] {
							{15, 30, 120},
							{15, 30, 120}
					};
					break;
				case 156:
					bg.overlayAlpha = 51;
					bg.overlay = new int[][] {
							{255, 255, 255},
							{255, 255, 255}
					};
					break;
				case 167:
					bg.overlayAlpha = 76;
					bg.overlay = new int[][] {
							{175, 104, 104},
							{137, 104, 88}
					};
					break;
				case 168:
					bg.overlayAlpha = 76;
					bg.overlay = new int[][] {
							{49, 73, 97},
							{68, 75, 77}
					};
			}
			prog.accept(1.0 * ++loaded / tot);
		}

		for(int i = 0; i < UserProfile.getBCData().bgs.size(); i++) {
			Background bg = UserProfile.getBCData().bgs.get(i);

			if(bg.reference != null) {
				Background ref = bg.reference.get();

				if(ref.overlay != null) {
					bg.overlay = ref.overlay.clone();
					bg.overlayAlpha = ref.overlayAlpha;
				}
			}
		}
	}

	@JsonClass.JCIdentifier
	@JsonField
	public Identifier<Background> id;
	@JsonField(backCompat = JsonField.CompatType.FORK, defval = "null")
	public Identifier<Background> reference;
	public VImg img;
	@JsonField
	public int[][] cs = new int[4][3];
	@JsonField(backCompat = JsonField.CompatType.FORK, defval = "null")
	public Identifier<BackgroundEffect> bgEffect = null;
	@JsonField(defval = "0")
	public int overlayAlpha;
	@JsonField
	public int[][] overlay;

	public int ic;
	@JsonField
	public boolean top;

	public FakeImage[] parts = null;

	private boolean loaded = false;

	@JsonClass.JCConstructor
	public Background() {
		ic = 1;
		top = true;
	}

	public Background(Identifier<Background> id, VImg vimg) {
		this.id = id;
		img = vimg;
		ic = 1;
		top = vimg.getImg().getHeight() == 1024;
	}

	public Background(Identifier<Background> id, VImg vimg, Identifier<Background> ref) {
		this.id = id;
		reference = ref;
		img = vimg;
		ic = 1;
		top = vimg.getImg().getHeight() == 1024;
	}

	private Background(int[] ints) {
		int id = ints[0];
		this.id = Identifier.rawParseInt(id, Background.class);

		VImg image;

		if(ints.length >= 16 && ints[15] != -1) {
			reference = new Identifier<>(Identifier.DEF, Background.class, ints[15]);

			if(id == 185) {
				reference = null;

				image = new VImg(VFile.get("./org/img/bg/bg"+Data.trio(id)+".png"));
			} else {
				image = new VImg(VFile.get("./org/img/bg/bg"+Data.trio(ints[15])+".png"));
			}
		} else {
			image = new VImg(VFile.get("./org/img/bg/bg"+Data.trio(id)+".png"));
		}

		img = image;

		top = ints[14] == 1 || ints[13] == 8;
		ic = ints[13] == 8 ? 1 : ints[13];

		for (int i = 0; i < 4; i++)
			cs[i] = new int[] { ints[i * 3 + 1], ints[i * 3 + 2], ints[i * 3 + 3] };

		if(id == 185) {
			ic = 11;
			cs[1][2] = 46;
		}

		UserProfile.getBCData().bgs.set(id, this);

		if(ints.length == 20) {
			overlayAlpha = ints[19];
			overlay = new int[][] {
					{ints[16], ints[17], ints[18]},
					{ints[16], ints[17], ints[18]}
			};
		} else if(ints.length == 24) {
			if(ints[19] != ints[23]) {
				System.out.println("W/Background | Different overlay alpha value found! : A0 = "+ints[18]+" / A1 = "+ints[22]);
			}

			overlayAlpha = ints[19];
			overlay = new int[][] {
					{ints[16], ints[17], ints[18]},
					{ints[20], ints[21], ints[22]}
			};
		}
	}

	@Override
	public void check() {
		if (parts != null)
			return;
		load();

	}

	public Background copy(Identifier<Background> id) {
		Background bg = new Background(id, new VImg(img.getImg()), reference == null ? this.id : reference);
		System.arraycopy(cs, 0, bg.cs, 0, 4);
		bg.top = top;
		bg.ic = ic;
		bg.bgEffect = bgEffect;
		bg.overlay = overlay;
		bg.overlayAlpha = overlayAlpha;
		return bg;
	}

	public void draw(FakeGraphics g, int w, int h) {
		check();

		int groundHeight = parts[Background.BG].getHeight();
		int skyHeight = top ? parts[Background.TOP].getHeight() : 1020 - groundHeight;

		if(skyHeight < 0)
			skyHeight = 0;

		double r = h / (double) (groundHeight + skyHeight + 408);

		int fw = (int) (768 * r);
		int sh = (int) (skyHeight * r);
		int gh = (int) (groundHeight * r);
		int skyGround = (int) (204 * r);

		if (top && parts.length > Background.TOP) {
			for (int i = 0; i * fw < w; i++)
				g.drawImage(parts[Background.TOP], fw * i, skyGround, fw, sh);

			g.gradRect(0, 0, w, skyGround, 0, 0, cs[0], 0, skyGround, cs[1]);
		} else {
			g.gradRect(0, 0, w, sh + skyGround, 0, 0, cs[0], 0, sh + skyGround, cs[1]);
		}

		for (int i = 0; i * fw < w; i++)
			g.drawImage(parts[Background.BG], fw * i, sh + skyGround, fw, gh);

		g.gradRect(0, sh + gh + skyGround, w, skyGround, 0, sh + gh + skyGround, cs[2], 0, h, cs[3]);
	}

	public void draw(FakeGraphics g, P rect, final int pos, final int h, final double siz, final int groundHeight) {
		check();
		int fw = (int) (parts[BG].getWidth() * siz);
		int fh = (int) (parts[BG].getHeight() * siz);

		final int off = (int) (pos + 200 * siz - fw);

		g.gradRect(0, h, (int) rect.x, groundHeight, 0, h, cs[2], 0, h + groundHeight, cs[3]);

		if (h > fh) {
			int y = h - fh * 2;

			if (top && parts.length > TOP) {
				int tw = (int) (parts[TOP].getWidth() * siz);
				int th = (int) (parts[TOP].getHeight() * siz);

				y += fh - th;

				for (int x = off; x < rect.x; x += tw)
					if (x + tw > 0)
						g.drawImage(parts[TOP], x, y, tw, th);

				if(y > 0)
					g.gradRect(0, 0, (int) rect.x, y, 0, 0, cs[0], 0, y, cs[1]);
			} else {
				g.gradRect(0, 0, (int) rect.x, fh + y, 0, y, cs[0], 0, y + fh, cs[1]);
			}
		}

		for (int x = off; x < rect.x; x += fw)
			if (x + fw > 0) {

				g.drawImage(parts[BG], x, h - fh, fw, fh);
			}
	}

	@Override
	public EAnimD<EffAnim.DefEff> getEAnim(BGWvType t) {
		if (t == BGWvType.ENEMY)
			return effas().A_E_WAVE.getEAnim(EffAnim.DefEff.DEF);
		else if (t == BGWvType.UNIT)
			return effas().A_WAVE.getEAnim(EffAnim.DefEff.DEF);
		else
			return null;
	}

	@Override
	public Identifier<Background> getID() {
		return id;
	}

	@Override
	public void load() {
		if(loaded)
			return;

		if (img == null) {
			PackData pack = getCont();
			if (pack == null)
				return;

			if (pack instanceof PackData.DefPack) {
				if (reference == null)
					img = new VImg(VFile.get("./org/img/bg/bg"+Data.trio(id.id)+".png"));
				else
					img = new VImg(VFile.get("./org/img/bg/bg"+Data.trio(reference.id)+".png"));
			} else if (pack instanceof PackData.UserPack)
				img = ((PackData.UserPack) getCont()).source.readImage(Source.BasePath.BG.toString(), id.id);
			else
				return;
		}

		img.mark(Marker.BG);
		BCAuxAssets aux = CommonStatic.getBCAssets();
		parts = aux.iclist.get(ic).cut(img.getImg());
		loaded = true;
	}

	public void unload() {
		if (!loaded)
			return;

		img.unload();
		img = null;
		for(int i = 0; i < parts.length; i++) {
			parts[i].unload();
			parts[i] = null;
		}
		parts = null;
		loaded = false;
	}

	public void forceLoad() {
		img.mark(Marker.BG);
		BCAuxAssets aux = CommonStatic.getBCAssets();
		parts = aux.iclist.get(ic).cut(img.getImg());
		loaded = true;
	}

	@Override
	public final String[] names() {
		return new String[] { toString(), "enemy wave", "unit wave" };
	}


	@OnInjected
	public void onInjected(JsonObject jbg) {
		if (reference != null)
			img = new VImg(reference.get().img.getImg());
		else
			img = ((PackData.UserPack) getCont()).source.readImage(Source.BasePath.BG.toString(), id.id);

		if (((PackData.UserPack) getCont()).desc.FORK_VERSION < 1 && jbg.has("effect")) {
			int effect = jbg.get("effect").getAsInt();
			if (effect >= 0)
				bgEffect = UserProfile.getBCData().bgEffects.get(effect).getID();
		}
	}

	@Override
	public FakeImage parts(int i) {
		return parts[i];
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	public BGWvType[] types() {
		return BGWvType.values();
	}

	@JsonField(tag = "effect", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public int getBGEff() {
		if (bgEffect == null || !bgEffect.pack.equals(Identifier.DEF))
			return -1;
		return bgEffect.id;
	}
}
