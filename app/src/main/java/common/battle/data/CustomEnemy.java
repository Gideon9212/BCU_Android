package common.battle.data;

import common.battle.Basis;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.util.anim.AnimU;
import common.util.unit.AbEnemy;
import common.util.unit.Enemy;

import java.util.Set;
import java.util.TreeSet;

@JsonClass
public class CustomEnemy extends CustomEntity implements MaskEnemy {

	public Enemy pack;

	@JsonField
	public int star;
	@JsonField(defval = "100")
	public int drop = 100;
	@JsonField
	public float limit;

	public CustomEnemy() {
		super();
		hp = 10000;
		traits.add(UserProfile.getBCData().traits.get(TRAIT_RED));
	}

	public CustomEnemy(AnimU<?> ene) {
		this();
		share = new int[ene.anim.getAtkCount()];
		share[0] = 1;
		for (int i = hits.size(); i < share.length; i++) {
			hits.add(new AtkDataModel[1]);
			hits.get(i)[0] = new AtkDataModel(this);
			share[i] = 1;
		}
	}

	public CustomEnemy copy(Enemy e) {
		CustomEnemy ce = new CustomEnemy(e.anim);
		ce.importData(this);
		ce.pack = e;

		return ce;
	}

	@Override
	public int getDrop() {
		return drop * 100;
	}

	@Override
	public Enemy getPack() {
		return pack;
	}

	@Override
	public int getStar() {
		return star;
	}

	@Override
	public Set<AbEnemy> getSummon() {
		Set<AbEnemy> ans = new TreeSet<>();
		if (common) {
			if (rep.proc.SUMMON.prob > 0 && (rep.proc.SUMMON.id == null || AbEnemy.class.isAssignableFrom(rep.proc.SUMMON.id.cls)))
				ans.add(Identifier.getOr(rep.proc.SUMMON.id, AbEnemy.class));
		} else
			for (AtkDataModel[] adms : hits)
				for (AtkDataModel adm : adms)
					if (adm.proc.SUMMON.prob > 0 && (adm.proc.SUMMON.id == null || AbEnemy.class.isAssignableFrom(adm.proc.SUMMON.id.cls)))
						ans.add(Identifier.getOr(adm.proc.SUMMON.id, AbEnemy.class));
		return ans;
	}

	@Override
	public void importData(MaskEntity de) {
		super.importData(de);
		if (de instanceof MaskEnemy) {
			MaskEnemy me = (MaskEnemy) de;
			star = me.getStar();
			drop = me.getDrop() / 100;
			limit = me.getLimit();
		}
	}

	@Override
	public float multi(Basis b) {
		if (star > 0)
			return b.t().getStarMulti(star);
		if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_ALIEN)))
			return b.t().getAlienMulti();
		return 1;
	}

	@Override
	public float getLimit() {
		return limit;
	}

	@Override
	public boolean defTrait() {
		return traits.size() == 1 && traits.get(0).equals(UserProfile.getBCData().traits.get(TRAIT_RED));
	}
}
