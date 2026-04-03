package common.util.pack;

import common.system.fake.FakeImage;
import common.util.anim.AnimI;
import common.util.anim.EAnimD;
import common.util.anim.MaAnim;
import common.util.anim.MaModel;

public class WaveAnim extends AnimI<WaveAnim, WaveAnim.WaveType> {

	public enum WaveType implements AnimI.AnimType<WaveAnim, WaveType> {
		DEF
	}

	private final Background bg;
	private final MaModel mamodel;
	private final MaAnim maanim;

	private FakeImage[] parts;

	public WaveAnim(Background BG, MaModel model, MaAnim anim) {
		bg = BG;
		mamodel = model;
		maanim = anim;
	}

	@Override
	public void check() {
		if (parts == null)
			load();
	}

	@Override
	public EAnimD<WaveType> getEAnim(WaveType t) {
		return new EAnimD<>(this, mamodel, maanim, t);
	}

	@Override
	public void load() {
		bg.check();
		parts = bg.parts;
	}

	@Override
	public String[] names() {
		return translate(WaveType.DEF);
	}

	@Override
	public FakeImage parts(int i) {
		check();
		return parts[i];
	}

	@Override
	public WaveType[] types() {
		return WaveType.values();
	}

}
