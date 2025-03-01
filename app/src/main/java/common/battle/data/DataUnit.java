package common.battle.data;

import common.CommonStatic;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.pack.Soul;
import common.util.unit.Form;
import common.util.unit.Trait;
import common.util.unit.Unit;

public class DataUnit extends DefaultData implements MaskUnit, Cloneable {

	private final Form form;
	public int price, respawn, limit, touch = TCH_N;
	private final int front;
	private final int back;

	public PCoin pcoin = null;

	public DataUnit(Form f, String[] data) {
		form = f;
		int[] ints = new int[data.length];
		for (int i = 0; i < data.length; i++)
			ints[i] = CommonStatic.parseIntN(data[i]);
		hp = ints[0];
		hb = ints[1];
		speed = ints[2];
		atk[0] = ints[3];
		tba = ints[4];
		range = ints[5];
		price = ints[6];
		respawn = ints[7] * 2;
		width = ints[9];
		int t = 0;
		if (ints[10] == 1)
			t |= TB_RED;
		isrange = ints[12] == 1;
		pre[0] = ints[13];
		front = ints[14];
		back = ints[15];
		if (ints[16] == 1)
			t |= TB_FLOAT;
		if (ints[17] == 1)
			t |= TB_BLACK;
		if (ints[18] == 1)
			t |= TB_METAL;
		if (ints[19] == 1)
			t |= TB_WHITE;
		if (ints[20] == 1)
			t |= TB_ANGEL;
		if (ints[21] == 1)
			t |= TB_ALIEN;
		if (ints[22] == 1)
			t |= TB_ZOMBIE;
		int a = 0;
		proc = Proc.blank();
		if (ints[23] == 1) {
			proc.DMGINC.mult = 150;//a |= AB_GOOD;
			proc.DEFINC.mult = 200;
		}
		proc.KB.prob = ints[24];
		proc.STOP.prob = ints[25];
		proc.STOP.time = ints[26];
		proc.SLOW.prob = ints[27];
		proc.SLOW.time = ints[28];
		if (ints[29] == 1)
			proc.DEFINC.mult = 400;//a |= AB_RESIST;
		if (ints[30] == 1)
			proc.DMGINC.mult = 300;//a |= AB_MASSIVE;
		proc.CRIT.prob = ints[31];
		if (ints[32] == 1)
			a |= AB_ONLY;
		if (ints[33] == 1)
			proc.BOUNTY.mult = 100;
		if (ints[34] == 1)
			proc.ATKBASE.mult = 300;
		if(ints.length < 95 || ints[94] != 1) {
			proc.WAVE.prob = ints[35];
			proc.WAVE.lv = ints[36];
		} else {
			proc.MINIWAVE.prob = ints[35];
			proc.MINIWAVE.lv = ints[36];
			proc.MINIWAVE.multi = 20;
		}
		proc.WEAK.prob = ints[37];
		proc.WEAK.time = ints[38];
		proc.WEAK.mult = ints[39];
		proc.STRONG.health = ints[40];
		proc.STRONG.mult = ints[41];
		proc.LETHAL.prob = ints[42];
		if (ints[43] == 1)
			a |= AB_METALIC;
		lds[0] = ints[44];
		ldr[0] = ints[45];

		if (ints[46] == 1)
			proc.IMUWAVE.mult = 100;
		if (ints[47] == 1)
			proc.IMUWAVE.block = 100;
		if (ints[48] == 1)
			proc.IMUKB.mult = 100;
		if (ints[49] == 1)
			proc.IMUSTOP.mult = 100;
		if (ints[50] == 1)
			proc.IMUSLOW.mult = 100;
		if (ints[51] == 1)
			proc.IMUWEAK.mult = 100;
		try {
			if(ints.length < 68) {
				death = Identifier.parseInt(0, Soul.class);
			}
			if (ints[52] == 1)
				a |= AB_ZKILL;
			if (ints[53] == 1)
				a |= AB_WKILL;
			loop = ints[55];
			if (ints[56] != 0)
				a |= AB_IMUSW;
			if (ints[58] == 2)
				a |= AB_GLASS;
			if (ints[59] != 0)
				if (ints[60] != 0) {
					atk = new int[]{atk[0], ints[59], ints[60]};
					pre = new int[]{pre[0], ints[61], ints[62]};
					abis = new boolean[]{ints[63] == 1, ints[64] == 1, ints[65] == 1};
				} else {
					atk = new int[]{atk[0], ints[59]};
					pre = new int[]{pre[0], ints[61]};
					abis = new boolean[]{ints[63] == 1, ints[64] == 1};
				}
			else
				abis[0] = ints[63] == 1;
			death = Identifier.parseInt(ints[67], Soul.class);
			proc.BREAK.prob = ints[70];
			if (ints[75] == 1)
				proc.IMUWARP.mult = 100;
			if (ints[77] == 1)
				a |= AB_EKILL;
			if (ints[78] == 1)
				t |= TB_RELIC;
			if (ints[79] == 1)
				proc.IMUCURSE.mult = 100;
			if (ints[80] == 1)
				proc.DEFINC.mult = 600;//a |= AB_RESISTS;
			if (ints[81] == 1)
				proc.DMGINC.mult = 500;//a |= AB_MASSIVES;
			proc.SATK.prob = ints[82];
			proc.SATK.mult = ints[83];
			proc.IMUATK.prob = ints[84];
			proc.IMUATK.time = ints[85];
			if (ints.length >= 109 && ints[108] == 1) {
				proc.MINIVOLC.prob = ints[86];
				proc.MINIVOLC.dis_0 = ints[87] / 4;
				proc.MINIVOLC.dis_1 = ints[88] / 4 + proc.MINIVOLC.dis_0;
				proc.MINIVOLC.time = ints[89] * VOLC_ITV;
				proc.MINIVOLC.mult = 20;
			} else {
				proc.VOLC.prob = ints[86];
				proc.VOLC.dis_0 = ints[87] / 4;
				proc.VOLC.dis_1 = ints[88] / 4 + proc.VOLC.dis_0;
				proc.VOLC.time = ints[89] * VOLC_ITV;
			}
			if (ints[90] == 1)
				proc.IMUPOIATK.mult = 100;
			if (ints[91] == 1)
				proc.IMUVOLC.mult = 100;
			proc.CURSE.prob = ints[92];
			proc.CURSE.time = ints[93];
			proc.SHIELDBREAK.prob = ints[95];
			if (ints[96] == 1)
				t |= TB_DEMON;
			if (ints[97] == 1)
				a |= AB_BAKILL;
			if (ints[98] == 1) {
				a |= AB_CKILL;
				touch |= TCH_CORPSE;
			}
			if(getAtkCount(0) > 1) {
				int lds0 = lds[0];
				int ldr0 = ldr[0];
				lds = new int[getAtkCount(0)];
				ldr = new int[getAtkCount(0)];
				lds[0] = lds0;
				ldr[0] = ldr0;

				for(int i = 1; i < getAtkCount(0); i++) {
					if(ints[99 + (i - 1) * 3] == 1) {
						lds[i] = ints[99 + (i - 1) * 3 + 1];
						ldr[i] = ints[99 + (i - 1) * 3 + 2];
					} else {
						lds[i] = lds0;
						ldr[i] = ldr0;
					}
				}
			}

			if (ints[105] == 1) {
				proc.BSTHUNT.type.active = true;
				proc.BSTHUNT.prob = ints[106];
				proc.BSTHUNT.time = ints[107];
			}
			if (ints[109] == 1) {
				proc.DEMONVOLC.prob = 100;
				proc.DEMONVOLC.mult = 100;
			}
			if (ints[110] != -1) {
				proc.SPIRIT.id = Identifier.parseInt(ints[110], Unit.class);
				proc.SPIRIT.amount = 1;
				proc.SPIRIT.cd0 = SPIRIT_SUMMON_DELAY;
				proc.SPIRIT.form = 1;
				proc.SPIRIT.type.inv = true;
			}
			if (ints[111] == 1)
				a |= AB_SKILL; //This is Sage Killer, nice naming upstream devs
			if (ints[112] != 0)
				proc.METALKILL.mult = ints[112];
			if (ints[113] != 0) {
				proc.BLAST.prob = ints[113];
				proc.BLAST.dis_0 = ints[114] / 4;
				proc.BLAST.dis_1 = ints[115] / 4 + proc.BLAST.dis_0;
				proc.BLAST.lv = 3;
				proc.BLAST.reduction = 30;
			}
			if (ints[116] != 0)
				proc.IMUBLAST.mult = 100;
		} catch (IndexOutOfBoundsException ignored) {
		}

		traits = new SortedPackSet<>(Trait.convertType(t, false));
		abi = a;

		datks = new DataAtk[getAtkCount(0)];

		for (int i = 0; i < datks.length; i++) {
			datks[i] = new DataAtk(this, i);
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
		return form.orbs;
	}

	@Override
	public Form getPack() {
		return form;
	}

	@Override
	public int getPrice() {
		return price;
	}

	@Override
	public int getRespawn() {
		return respawn;
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
					respawn -= mod;
					break;
				case PC2_COST:
					price -= mod;
					break;
				case PC2_HB:
					hb += mod;
					break;
				case PC2_TBA:
					tba = (int) (tba * (100 - mod) / 100.0);
			}
	}

	@Override
	public DataUnit clone() {
		DataUnit ans = (DataUnit) err(super::clone);
		ans.traits = new SortedPackSet<>(this.traits);
		ans.proc = proc.clone();
		ans.datks = new DataAtk[ans.getAtkCount(0)];
		for (int i = 0; i < ans.getAtkCount(0); i++)
			ans.datks[i] = new DataAtk(ans, i);
		return ans;
	}

	@Override
	public int getTouch() {
		return touch;
	}
}
