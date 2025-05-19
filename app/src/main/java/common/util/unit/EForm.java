package common.util.unit;

import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.data.MaskUnit;
import common.battle.data.PCoin;
import common.battle.entity.ESpirit;
import common.battle.entity.EUnit;
import common.pack.Identifier;
import common.util.Data;
import common.util.anim.AnimU;
import common.util.anim.EAnimU;

public class EForm extends Data implements IForm {

	private final Form f;
	private final Level level;

	public final MaskUnit du;

	public EForm(Form form, int lv) {
		f = form;
		level = new Level(form.du.getPCoin() == null ? 0 : form.du.getPCoin().info.size());
		level.setLevel(lv);

		du = form.du;
	}

	public EForm(Form form, Level level) {
		f = form;
		this.level = level;
		PCoin pc = f.du.getPCoin();
		if (pc != null)
			du = pc.improve(level.getTalents());
		else
			du = form.du;
	}

	@Override
	public EUnit getEntity(StageBasis b, int[] index, boolean isBase) {
		if(b.st.isAkuStage())
			getAkuStageLevel();

		float d = f.unit.lv.getMult(level.getTotalLv());
		EAnimU anim = getEntryAnim();
		return new EUnit(b, du, anim, d, du.getFront(), du.getBack(), level, f.du.getPCoin(), index, isBase);
	}

	public EUnit invokeSpirit(StageBasis b, int[] index) {
		if (!b.elu.readySpirit(index[0],index[1]) || b.money < b.elu.spiritCost(index[0], index[1], b.st.getCont().price))
			return null;
		Form spirit = Identifier.getOr(du.getProc().SPIRIT.id, Unit.class).forms[du.getProc().SPIRIT.form - 1];
		if (b.cantDeploy(spirit.unit.rarity, spirit.du.getWill()))
			return null;

		float d = spirit.unit.lv.getMult(level.getTotalLv());
		spirit.anim.partial();
		if (spirit.anim.getAtkCount() == 0)
			return new ESpirit(b, spirit.du, spirit.getEAnim(AnimU.SOUL[0]), d, du.getFront(), du.getBack(), level, index); //For BC-Accurate Spirits
		EUnit esp = new EUnit(b, spirit.du, spirit.getEAnim(AnimU.SOUL[0]), d, du.getFront(), du.getBack(), level, spirit.du.getPCoin(), index, false);
		if (du.getProc().SPIRIT.inv)
			esp.status.inv[0] = -1;
		return esp; //For Custom Units with spirits
	}

	@Override
	public EUnit invokeEntity(StageBasis b, int Lvl, int minLayer, int maxLayer) {
		float d = f.unit.lv.getMult(Lvl);
		EAnimU anim = getEntryAnim();
		return new EUnit(b, du, anim, d, minLayer, maxLayer, level, f.du.getPCoin(), null, false);
	}

	@Override
	public int getWill() {
		return du.getWill();
	}

	@Override
	public float getPrice(int sta) {
		return du.getPrice() * (1 + sta * 0.5f);
	}

	@Override
	public int getRespawn() {
		return du.getRespawn();
	}

	public EAnimU getEntryAnim() {
		EAnimU anim = f.getEAnim(AnimU.TYPEDEF[AnimU.ENTRY]);
		if (anim.unusable())
			anim = f.getEAnim(AnimU.TYPEDEF[AnimU.WALK]);

		anim.setTime(0);
		return anim;
	}

	private void getAkuStageLevel() {
		if(CommonStatic.getConfig().levelLimit == 0)
			return;
		level.setLevel(Math.min(level.getLv(), CommonStatic.getConfig().levelLimit));
		level.setPlusLevel(CommonStatic.getConfig().plus ? level.getPlusLv() : 0);
	}
}
