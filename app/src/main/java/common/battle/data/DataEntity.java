package common.battle.data;

import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.Data;
import common.util.pack.Soul;
import common.util.unit.Trait;

@JsonClass(noTag = NoTag.LOAD)
public abstract class DataEntity extends Data implements MaskEntity {

	public int hp, range, will;
	@JsonField(defval = "1")
	public int hb = 1;
	@JsonField(defval = "8")
	public int speed = 8;
	@JsonField(defval = "320")
	public int width = 320;
	@JsonField(defval = "-1")
	public int loop = -1;
	@JsonField(backCompat = JsonField.CompatType.FORK)
	public int tba, abi;

	@JsonField(defval = "this.defSoul")
	public Identifier<Soul> death = new Identifier<>(Identifier.DEF, Soul.class, 0);
	@JsonField(generic = Trait.class, alias = Identifier.class, defval = "this.defTrait")
	public SortedPackSet<Trait> traits = new SortedPackSet<>();

	public boolean defSoul() {
		return death != null && death.id == 0 && death.pack.equals(Identifier.DEF);
	}
	public boolean defTrait() {
		return traits.isEmpty();
	}

	@JsonField(tag = "tba", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public int getUTBA() {
		return Math.abs(tba);
	}

	@JsonField(tag = "abi", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public int getUAbi() {
		int nabi = 0;
		int abiSub = 3;
		for (int i = 0; i < ABI_TOT; i++) {
			if (i == 2)
				abiSub++;
			else if (i == 11)
				abiSub += 2;
			if (((abi >> i) & 1) > 0)
				nabi |= 1 << i + abiSub;
		}

		int str = (int) getProc().DMGINC.mult;
		if ((str >= 150 && str < 300) || (str >= 450 && str < 500) || (str >= 750 && str < 1500) || str >= 2250)
			nabi |= 1;
		if ((str >= 300 && str < 500) || str >= 1500)
			nabi |= 4;
		if (str >= 500)
			nabi |= 1 << 16;

		str = (int) getProc().DEFINC.mult;
		if ((str >= 200 && str < 400) || (str >= 800 && str < 2400) || str >= 4800)
			nabi |= 1;
		if ((str >= 400 && str < 600) || (str >= 800 && str < 1200) || str >= 2400)
			nabi |= 2;
		if ((str >= 600 && str < 800) || str >= 1200)
			nabi |= 1 << 15;

		if (getProc().IMUWAVE.block == 100)
			nabi |= 1 << 5;
		if (getProc().DEMONVOLC.prob > 0)
			nabi |= 1 << 19;
		return nabi;
	}

	@Override
	public int getAbi() {
		return abi;
	}

	@Override
	public int getAtkLoop() {
		return loop;
	}

	@Override
	public Identifier<Soul> getDeathAnim() {
		return death;
	}

	@Override
	public SortedPackSet<Trait> getTraits() {
		return traits;
	}

	@Override
	public int getHb() {
		return hb;
	}

	@Override
	public int getHp() {
		return hp;
	}

	@Override
	public int getRange() {
		return range;
	}

	@Override
	public int getSpeed() {
		return speed;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getWill() {
		return will;
	}
}
