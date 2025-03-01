package common.util.anim;

import common.io.assets.Admin.StaticPermitted;
import common.system.VImg;
import common.system.fake.FakeImage;

import java.util.Arrays;

public abstract class AnimU<T extends AnimU.ImageKeeper> extends AnimD<AnimU<?>, AnimU.UType> {

	public interface EditableType {
		boolean rotate();
	}

	public interface ImageKeeper {
		enum AnimationType { //TODO: Delet this
			SOUL,
			ENEMY,
			UNIT
		}

		default VImg getPreviewIcon() {
			return null;
		}

		VImg getEdi();

		ImgCut getIC();

		MaAnim[] getMA();

		MaModel getMM();

		FakeImage getNum();

		VImg getUni();

		void unload();
	}

	public static class UType implements AnimI.AnimType<AnimU<?>, UType>, EditableType {
		private String name;
		private final boolean rotate;

		UType(String name, boolean rotate) {
			this.name = name;
			this.rotate = rotate;
		}

		public void changeName(String str) {
			name = str;
		}
		@Override
		public boolean rotate() {
			return rotate;
		}
		@Override
		public String toString() {
			return name;
		}
	}

	public static final int WALK = 0, IDLE = 1, HB = 3, BURROW_DOWN = 4, UNDERGROUND = 5, BURROW_UP = 6, ENTRY = 7, RETREAT = 8;
	@StaticPermitted
	public static final UType[] TYPEDEF = { new UType("walk", false), new UType("idle", false), new UType("attack", true),
			new UType("kb", false), new UType("burrow_down", true), new UType("burrow_move", false), new UType("burrow_up", true),
			new UType("entry", true), new UType("retreat", false) };

	@StaticPermitted
	public static final UType[] SOUL = { new UType("soul", true) };
	@StaticPermitted
	public static final UType[] BGEFFECT = { new UType("background", false), new UType("foreground", false) };

	protected boolean partial = false;
	public final T loader;

	protected AnimU(String path, T load) {
		super(path);
		loader = load;
	}

	protected AnimU(T load) {
		super("");
		loader = load;
	}

	public final int getAtkCount() {
		if (types == null)
			partial();

		if (types.length < 4)
			return 0;
		if (types.length <= TYPEDEF.length)
			return 1;
		return types.length - TYPEDEF.length + 1;
	}

	public int getAtkLen(int atk) {
		partial();
		if (getAtkCount() == 0)
			return anims[0].len;
		return anims[2 + atk].len + 1;
	}

	@Override
	public final EAnimU getEAnim(UType t) {
		check();
		return new EAnimU(this, t);
	}

	public final VImg getEdi() {
		return loader.getEdi();
	}

	@Override
	public final FakeImage getNum() {
		return loader.getNum();
	}

	public final VImg getUni() {
		return loader.getUni();
	}

	public final VImg getPreviewIcon() {
		return loader.getPreviewIcon();
	}

	@Override
	public void load() {
		loaded = true;
		try {
			imgcut = loader.getIC();
			if (getNum() == null) {
				mamodel = null;
				return;
			}
			parts = imgcut.cut(getNum());
			partial();
		} catch (Exception e) {
			e.printStackTrace();
			loaded = imgcut != null;//Used to be false, but this way stops stackOverflowError on corrupted animations. It turns those into NPEs, but those are far better than an SOE
		}
	}

	@Override
	public String[] names() {
		check();
		String[] str = translate(types);
		if (types.length > TYPEDEF.length) {
			String st = str[2];
			for (int i = 2; i < str.length - 6; i++)
				str[i] = st + " " + (i - 1);
		}
		return str;
	}

	@Override
	public final void unload() {
		loader.unload();
		super.unload();
	}

	public void partial() {
		if (!partial) {
			try {
				partial = true;
				imgcut = loader.getIC();
				mamodel = loader.getMM();
				anims = loader.getMA();
				types = new UType[anims.length];
				if (types.length <= TYPEDEF.length) {
					types = types.length == 1 ? SOUL : types.length == 2 ? BGEFFECT : types.length == TYPEDEF.length ? TYPEDEF : Arrays.copyOf(TYPEDEF, types.length);
				} else {
					for (int i = 0; i < types.length; i++) {
						if (i < 3)
							types[i] = TYPEDEF[i];
						else if (i >= anims.length - 6)
							types[i] = TYPEDEF[i - (anims.length - TYPEDEF.length)];
						else
							types[i] = new UType("attack" + (i - 2), true);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				partial = false;
			}
		}
	}
}
