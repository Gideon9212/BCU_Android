package common.battle.data;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.Data;
import common.util.anim.AnimU;
import common.util.unit.Form;

@JsonClass
public class CustomUnit extends CustomEntity implements MaskUnit, Cloneable {

	public Form pack;

	@JsonField(defval = "50")
	public int price = 50;
	@JsonField(defval = "60")
	public int resp = 60;
	@JsonField
	public int back, limit;
	@JsonField(defval = "9")
	public int front = 9;
	@JsonField(gen = JsonField.GenType.GEN)
	public PCoin pcoin = null;

	public CustomUnit() {
		super();
		hp = 1000;
	}

	public CustomUnit(AnimU<?> uni) {
		this();
		share = new int[Math.max(1, uni.anim.getAtkCount())];
		share[0] = 1;
		for (int i = hits.size(); i < share.length; i++) {
			hits.add(new AtkDataModel[1]);
			hits.get(i)[0] = new AtkDataModel(this);
			share[i] = 1;
		}
	}

	@Override
	public int getBack() {
		return back;
	}

	@Override
	public int getFront() {
		return front;
	}

	@Override
	public Orb getOrb() {
		return pack.orbs;
	}

	@Override
	public Form getPack() {
		return pack;
	}

	@Override
	public int getPrice() {
		return price;
	}

	@Override
	public int getRespawn() {
		return resp;
	}

	@Override
	public int getLimit() {
		return limit;
	}

	@Override
	public PCoin getPCoin() { return pcoin; }

	@Override
	public void improve(int[] type, int mod) {
		if (type[0] == PC_AB)
			abi |= type[1];
		else
			switch (type[1]) {
				case PC2_SPEED:
					speed += mod;
					break;
				case PC2_CD:
					resp -= mod;
					break;
				case PC2_COST:
					price -= mod;
					break;
				case PC2_HB:
					hb += mod;
					break;
				case PC2_TBA:
					tba = (int) (tba * (100 - mod) / 100.0);
					break;
				case PC2_RNG:
					range += mod;
			}
	}

	@Override
	public void importData(MaskEntity de) {
		super.importData(de);

		if (de instanceof MaskUnit) {
			MaskUnit mu = (MaskUnit) de;

			price = mu.getPrice();
			resp = mu.getRespawn();
			back = Math.min(mu.getBack(), mu.getFront());
			front = Math.max(mu.getBack(), mu.getFront());
			limit = mu.getLimit();

			PCoin p = mu.getPCoin();
			if (p != null) {
				pcoin = new PCoin(this);
				pcoin.max = p.max.clone();

				for (int[] i : p.info) {
					int[] j = i.clone();
					j[1] = Math.max(1, j[1]);
					j[j.length - 1] = Math.max(0, j[j.length - 1]);
					pcoin.info.add(j);
				}
			}
		}
	}

	@Override
	public CustomUnit clone() {
		CustomUnit ans = (CustomUnit) Data.err(super::clone);
		ans.importData(this);
		ans.pack = getPack();
		ans.getPack().anim = getPack().anim;
		return ans;
	}
}
