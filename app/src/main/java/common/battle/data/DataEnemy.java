package common.battle.data;

import common.battle.Basis;
import common.pack.FixIndexList.FixIndexMap;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.util.pack.Soul;
import common.util.unit.Enemy;
import common.util.unit.Trait;

public class DataEnemy extends DefaultData implements MaskEnemy {

	private final Enemy enemy;

	private final int earn, star;
	public float limit;

	public DataEnemy(Enemy e, int[] ints) {
		enemy = e;
		proc = Proc.blank();

		hp = ints[0];
		hb = ints[1];
		speed = ints[2];
		atk[0] = ints[3];
		tba = ints[4];
		range = ints[5];
		earn = ints[6];
		width = ints[8];
		FixIndexMap<Trait> BCTraits = UserProfile.getBCData().traits;
		if (ints[10] == 1)
			//Red
			traits.add(BCTraits.get(TRAIT_RED));
		isrange = ints[11] == 1;
		pre[0] = ints[12];
		if (ints[13] == 1)
			//Floating
			traits.add(BCTraits.get(TRAIT_FLOAT));
		if (ints[14] == 1)
			//Black
			traits.add(BCTraits.get(TRAIT_BLACK));
		if (ints[15] == 1)
			//Metal
			traits.add(BCTraits.get(TRAIT_METAL));
		if (ints[16] == 1)
			//White
			traits.add(BCTraits.get(TRAIT_WHITE));
		if (ints[17] == 1)
			//Angel
			traits.add(BCTraits.get(TRAIT_ANGEL));
		if (ints[18] == 1)
			//Alien
			traits.add(BCTraits.get(TRAIT_ALIEN));
		if (ints[19] == 1)
			//Zombie
			traits.add(BCTraits.get(TRAIT_ZOMBIE));
		proc.KB.prob = ints[20];
		proc.STOP.prob = ints[21];
		proc.STOP.time = ints[22];
		proc.SLOW.prob = ints[23];
		proc.SLOW.time = ints[24];
		proc.CRIT.prob = ints[25];
		int a = 0;
		if (ints[26] == 1)
			proc.ATKBASE.mult = 300;
		if(ints.length < 87 || ints[86] != 1) {
			proc.WAVE.prob = ints[27];
			proc.WAVE.lv = ints[28];
		} else {
			proc.MINIWAVE.prob = ints[27];
			proc.MINIWAVE.lv = ints[28];
		}
		proc.WEAK.prob = ints[29];
		proc.WEAK.time = ints[30];
		proc.WEAK.mult = ints[31];
		proc.STRONG.health = ints[32];
		proc.STRONG.mult = ints[33];
		proc.LETHAL.prob = ints[34];

		lds[0] = ints[35];
		ldr[0] = ints[36];
		if (ints[37] == 1)
			proc.IMUWAVE.mult = 100;
		if (ints[38] == 1)
			proc.IMUWAVE.block = 100;
		if (ints[39] == 1)
			proc.IMUKB.mult = 100;
		if (ints[40] == 1)
			proc.IMUSTOP.mult = 100;
		if (ints[41] == 1)
			proc.IMUSLOW.mult = 100;
		if (ints[42] == 1)
			proc.IMUWEAK.mult = 100;
		proc.BURROW.count = ints[43];
		proc.BURROW.dis = ints[44] / 4;
		proc.REVIVE.count = ints[45];
		proc.REVIVE.time = ints[46];
		proc.REVIVE.health = ints[47];
		if (ints[48] == 1)
			//Witch
			traits.add(BCTraits.get(TRAIT_WITCH));
		if (ints[49] == 1)
			//Base
			traits.add(BCTraits.get(TRAIT_INFH));
		loop = ints[50];
		if (ints[52] == 2)
			a |= AB_GLASS;
		death = Identifier.parseInt(ints[54], Soul.class);
		if(ints[54] == -1 && ints[63] == 1)
			death = Identifier.parseInt(9, Soul.class);
		if (ints[55] != 0)
			if (ints[56] != 0) {
				atk = new int[]{atk[0], ints[55], ints[56]};
				pre = new int[]{pre[0], ints[57], ints[58]};
				abis = new boolean[]{ints[59] == 1, ints[60] == 1, ints[61] == 1};
			} else {
				atk = new int[]{atk[0], ints[55]};
				pre = new int[]{pre[0], ints[57]};
				abis = new boolean[]{ints[59] == 1, ints[60] == 1};
			}
		else
			abis[0] = ints[59] == 1;
		proc.BARRIER.health = ints[64];
		proc.WARP.prob = ints[65];
		proc.WARP.time = ints[66];
		proc.WARP.dis = Math.min(ints[67], ints[68]) / 4;
		proc.WARP.dis_1 = Math.max(ints[67], ints[68]) / 4;
		star = ints[69];
		if (ints[70] == 1)
			proc.IMUWARP.mult = 100;
		if (ints[71] == 1)
			//EVA
			traits.add(BCTraits.get(TRAIT_EVA));
		if (ints[72] == 1)
			//Relic
			traits.add(BCTraits.get(TRAIT_RELIC));
		proc.CURSE.prob = ints[73];
		proc.CURSE.time = ints[74];
		proc.SATK.prob = ints[75];
		proc.SATK.mult = ints[76];
		proc.IMUATK.prob = ints[77];
		proc.IMUATK.time = ints[78];
		proc.POIATK.prob = ints[79];
		proc.POIATK.mult = ints[80];
		if (ints.length >= 103 && ints[102] == 1) {
			proc.MINIVOLC.prob = ints[81];
			proc.MINIVOLC.dis_0 = ints[82] / 4;
			proc.MINIVOLC.dis_1 = ints[83] / 4 + proc.VOLC.dis_0;
			proc.MINIVOLC.time = ints[84] * VOLC_ITV;
		} else {
			proc.VOLC.prob = ints[81];
			proc.VOLC.dis_0 = ints[82] / 4;
			proc.VOLC.dis_1 = ints[83] / 4 + proc.VOLC.dis_0;
			proc.VOLC.time = ints[84] * VOLC_ITV;
		}
		if (ints[85] == 1)
			proc.IMUVOLC.mult = 100;
		proc.DEMONSHIELD.hp = ints[87];
		proc.DEMONSHIELD.regen = ints[88];
		proc.DEATHSURGE.prob = ints[89];
		proc.DEATHSURGE.dis_0 = ints[90] / 4;
		proc.DEATHSURGE.dis_1 = ints[91] / 4 + proc.DEATHSURGE.dis_0;
		proc.DEATHSURGE.time = ints[92] * VOLC_ITV;

		if(ints[93] == 1)
			traits.add(BCTraits.get(TRAIT_DEMON));
		if(ints[94] == 1)
			traits.add(BCTraits.get(TRAIT_BARON));

		try {
			if (getAtkCount(0) > 1) {
				int lds0 = lds[0];
				int ldr0 = ldr[0];
				lds = new int[getAtkCount(0)];
				ldr = new int[getAtkCount(0)];
				lds[0] = lds0;
				ldr[0] = ldr0;

				for (int i = 1; i < getAtkCount(0); i++) {
					if (ints[95 + (i - 1) * 3] == 1) {
						lds[i] = ints[95 + (i - 1) * 3 + 1];
						ldr[i] = ints[95 + (i - 1) * 3 + 2];
					} else {
						lds[i] = lds0;
						ldr[i] = ldr0;
					}
				}
			}
			if (ints[101] == 1)
				traits.add(BCTraits.get(TRAIT_BEAST));
			if (ints[103] == 1) {//Counter surge
				proc.DEMONVOLC.prob = 100;
				proc.DEMONVOLC.mult = 100;
			}
			if (ints[104] == 1)
				traits.add(BCTraits.get(TRAIT_SAGE));
			if (ints[105] == 1)
				proc.IMUCURSE.mult = 100;
			if (ints[106] != 0) {
				proc.BLAST.prob = ints[106];
				proc.BLAST.dis_0 = ints[107] / 4;
				proc.BLAST.dis_1 = ints[108] / 4 + proc.BLAST.dis_0;
			}
			if (ints[109] != 0)
				proc.IMUBLAST.mult = 100;
		} catch (IndexOutOfBoundsException ignored) {

		}

		abi = a;

		datks = new DataAtk[getAtkCount(0)];

		for (int i = 0; i < datks.length; i++) {
			datks[i] = new DataAtk(this, i);
		}
	}

	@Override
	public int getDrop() {
		return earn * 100;
	}

	@Override
	public Enemy getPack() {
		return enemy;
	}

	@Override
	public int getStar() {
		return star;
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
}
