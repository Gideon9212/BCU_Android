package common.util;

import common.CommonStatic;
import common.CommonStatic.Lang.Locale;
import common.CommonStatic.BCAuxAssets;
import common.util.anim.ImgCut;
import common.battle.entity.AbEntity;
import common.system.P;
import common.system.SymCoord;
import common.system.VImg;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.ImageBuilder;

/**
 * This class gets UI images, as well as reading them during the initial loading.
 */
public class Res extends ImgCore {

	public static P getBase(AbEntity ae, SymCoord coor, boolean dojo) {
		BCAuxAssets aux = CommonStatic.getBCAssets();

		if(ae.dire == 1 && dojo) {
			FakeImage[] input = new FakeImage[15];
			for(int i = 0; i < 7; i++)
				input[i] = aux.num[5][11].getImg();
			input[7] = aux.num[5][10].getImg();
			for(int i = 0; i < 7; i++)
				input[i+8] = aux.num[5][11].getImg();

			return coor.draw(input);
		} else {
			long h = ae.health;
			if (h < 0)
				h = 0;
			int[] val0 = getLab(h);
			int[] val1 = getLab(ae.maxH);
			FakeImage[] input = new FakeImage[val0.length + val1.length + 1];
			for (int i = 0; i < val0.length; i++)
				input[i] = aux.num[5][val0[i]].getImg();
			input[val0.length] = aux.num[5][10].getImg();
			for (int i = 0; i < val1.length; i++)
				input[val0.length + i + 1] = aux.num[5][val1[i]].getImg();
			return coor.draw(input);
		}
	}

	public static void getLv(int lv, SymCoord coor) {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		int[] val = getLab(lv);
		FakeImage[] input = new FakeImage[val.length];
		for (int i = 0; i < val.length; i++)
			input[i] = aux.num[3][val[i]].getImg();
		coor.draw(input);
	}

	public static P getCost(int cost, boolean enable, SymCoord coor) {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		if (cost == -1)
			return coor.draw(aux.battle[0][3].getImg());
		int[] val = getLab(cost);
		FakeImage[] input = new FakeImage[val.length + 1];
		for (int i = 0; i < val.length; i++)
			input[i] = aux.num[enable ? 3 : 4][val[i]].getImg();
		input[input.length - 1] = aux.moneySign[enable ? 2 : 3][decideLocale().ordinal()].getImg();
		return coor.draw(input);
	}

	public static P getMoney(int mon, int max, SymCoord sym) {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		int[] val0 = getLab(mon);
		int[] val1 = getLab(max);
		FakeImage[] input = new FakeImage[val0.length + val1.length + 2];
		for (int i = 0; i < val0.length; i++)
			input[i] = aux.num[0][val0[i]].getImg();
		input[val0.length] = aux.num[0][10].getImg();
		for (int i = 0; i < val1.length; i++)
			input[val0.length + i + 1] = aux.num[0][val1[i]].getImg();
		input[input.length - 1] = aux.moneySign[0][decideLocale().ordinal()].getImg();

		return sym.draw(input);
	}

	public static P getWorkerLv(int lv, boolean enable, SymCoord coor) {
		BCAuxAssets aux = CommonStatic.getBCAssets();

		int[] val = getLab(lv);

		FakeImage[] input = new FakeImage[val.length + 1];

		input[0] = aux.num[enable ? 1 : 2][10].getImg();

		for(int i = 1; i < input.length; i++) {
			input[i] = aux.num[enable ? 1 : 2][val[i - 1]].getImg();
		}

		return coor.draw(input);
	}

	public static void readData() {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		aux.unicut = ImgCut.newIns("./org/data/uni.imgcut");
		aux.udicut = ImgCut.newIns("./org/data/udi.imgcut");
		VImg uni = new VImg("./org/page/uni.png");
		uni.setCut(aux.unicut);
		aux.slot[0] = uni;

		aux.ico[0] = new VImg[9];
		aux.ico[1] = new VImg[5];
		aux.ico[0][0] = new VImg("./org/page/foreground.png");
		aux.ico[0][1] = new VImg("./org/page/starFG.png");
		aux.ico[0][2] = new VImg("./org/page/soulFG.png");
		aux.ico[0][3] = new VImg("./org/page/EFBG.png");
		aux.ico[0][4] = new VImg("./org/page/TFBG.png");
		aux.ico[0][5] = new VImg("./org/page/ZFBG.png");
		aux.ico[0][6] = new VImg("./org/page/glow.png");
		aux.ico[0][7] = new VImg("./org/page/EFFG.png");
		aux.ico[0][8] = new VImg("./org/page/glow2.png");
		aux.ico[1][0] = new VImg("./org/page/uni_f.png");
		aux.ico[1][1] = new VImg("./org/page/uni_c.png");
		aux.ico[1][2] = new VImg("./org/page/uni_s.png");
		aux.ico[1][3] = new VImg("./org/page/uni_u.png");
		aux.ico[1][4] = new VImg("./org/page/uni_box.png");
		for (VImg vs : aux.ico[1])
			vs.setCut(aux.unicut);

		for (int i : GATYA)
			aux.gatyaitem.put(i, new VImg("./org/page/catfruit/gatyaitemD_" + i + "_f.png"));

		ImgCut xp = new ImgCut();
		xp.cuts = new int[][] { { 27, 43, 74, 42 } }; // TODO may need to un-hardcode this
		aux.XP = new VImg("./org/page/catfruit/xp.png");
		aux.XP.setCut(xp);

		aux.slot[1] = makeLineupIconFrame(true);
		aux.slot[2] = makeLineupIconFrame(false);
		aux.dummyTrait = new VImg("./org/page/Trait.png");
		readAbiIcon();
		readBattle();
	}

	private static int[] getLab(long cost) {
		if (cost < 0)
			cost = 0;

		int len = (int) Math.log10(cost == 0 ? 1 : cost) + 1;

		if (len < 0)
			len = 0;

		int[] input = new int[len];
		for (int i = 0; i < len; i++) {
			input[len - i - 1] = (int) (cost % 10);
			cost /= 10;
		}
		return input;
	}

	/**
	 * Reads the icons for procs, abilities, BC Traits, Atk types, and talents
	 */
	private static void readAbiIcon() {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		CommonStatic.getConfig().icon = true;
		ImgCut ic015 = ImgCut.newIns("./org/page/img015.imgcut");
		VImg img015 = new VImg("./org/page/img015.png");
		FakeImage[] parts = ic015.cut(img015.getImg());
		aux.icon[0] = new VImg[ABI_TOT];
		aux.icon[1] = new VImg[PROC_TOT];
		aux.icon[2] = new VImg[ATK_TOT];
		aux.icon[3] = new VImg[TRAIT_TOT];
		aux.icon[4] = new VImg[PC2_TOT];
		//Icons for abilities
		aux.icon[0][ABI_EKILL] = new VImg(parts[110]);
		aux.icon[0][ABI_ONLY] = new VImg(parts[202]);
		aux.icon[0][ABI_METALIC] = new VImg(parts[209]);
		aux.icon[0][ABI_WKILL] = new VImg(parts[258]);
		aux.icon[0][ABI_ZKILL] = new VImg(parts[260]);
		aux.icon[0][ABI_BAKILL] = new VImg(parts[297]);
		aux.icon[0][ABI_CKILL] = new VImg(parts[300]);
		aux.icon[0][ABI_THEMEI] = new VImg("./org/page/icons/ThemeX.png");
		aux.icon[0][ABI_TIMEI] = new VImg("./org/page/icons/TimeX.png");
		aux.icon[0][ABI_IMUSW] = new VImg("./org/page/icons/BossWaveX.png");
		aux.icon[0][ABI_SNIPERI] = new VImg("./org/page/icons/SnipeX.png");
		aux.icon[0][ABI_GHOST] = new VImg("./org/page/icons/Ghost.png");
		aux.icon[0][ABI_GLASS] = new VImg("./org/page/icons/Suicide.png");
		aux.icon[0][ABI_SKILL] = new VImg(parts[319]);
		//Icons for procs
		aux.icon[1][P_IMUCURSE] = new VImg(parts[116]);
		aux.icon[1][P_WEAK] = new VImg(parts[195]);
		aux.icon[1][P_STRONG] = new VImg(parts[196]);
		aux.icon[1][P_STOP] = new VImg(parts[197]);
		aux.icon[1][P_SLOW] = new VImg(parts[198]);
		aux.icon[1][P_LETHAL] = new VImg(parts[199]);
		aux.icon[1][P_ATKBASE] = new VImg(parts[200]);
		aux.icon[1][P_CRIT] = new VImg(parts[201]);
		aux.icon[1][P_BSTHUNT] = new VImg(parts[302]);
		aux.icon[1][P_BOUNTY] = new VImg(parts[205]);
		aux.icon[1][P_KB] = new VImg(parts[207]);
		aux.icon[1][P_WAVE] = new VImg(parts[208]);
		aux.icon[1][P_IMUWAVE] = new VImg(parts[210]);
		aux.icon[1][P_IMUWEAK] = new VImg(parts[213]);
		aux.icon[1][P_IMUSTOP] = new VImg(parts[214]);
		aux.icon[1][P_IMUSLOW] = new VImg(parts[215]);
		aux.icon[1][P_IMUKB] = new VImg(parts[216]);
		aux.icon[1][P_IMUWARP] = new VImg(parts[262]);
		aux.icon[1][P_BREAK] = new VImg(parts[264]);
		aux.icon[1][P_WARP] = new VImg(parts[266]);
		aux.icon[1][P_SATK] = new VImg(parts[229]);
		aux.icon[1][P_IMUATK] = new VImg(parts[231]);
		aux.icon[1][P_VOLC] = new VImg(parts[239]);
		aux.icon[1][P_MINIVOLC] = new VImg(parts[310]);
		aux.icon[1][P_IMUPOIATK] = new VImg(parts[237]);
		aux.icon[1][P_IMUVOLC] = new VImg(parts[243]);
		aux.icon[1][P_CURSE] = new VImg(parts[289]);
		aux.icon[1][P_POIATK] = new VImg(parts[329]);
		aux.icon[1][P_MINIWAVE] = new VImg(parts[293]);
		aux.icon[1][P_SHIELDBREAK] = new VImg(parts[296]);
		aux.icon[1][P_SPIRIT] = new VImg(parts[317]);
		aux.icon[1][P_METALKILL] = new VImg(parts[321]);
		aux.icon[1][P_DEMONSHIELD] = new VImg(parts[331]);
		aux.icon[1][P_DEATHSURGE] = new VImg(parts[332]);
		aux.icon[1][P_BURROW] = new VImg("./org/page/icons/Burrow.png");
		aux.icon[1][P_REVIVE] = new VImg("./org/page/icons/Revive.png");
		aux.icon[1][P_CRITI] = new VImg("./org/page/icons/CritX.png");
		aux.icon[1][P_ARMOR] = new VImg("./org/page/icons/ArmorBreak.png");
		aux.icon[1][P_SPEED] = new VImg("./org/page/icons/Speed.png");
		aux.icon[1][P_BLAST] = new VImg(parts[335]);
		aux.icon[1][P_IMUBLAST] = new VImg(parts[337]);

		aux.icon[1][P_IMUSUMMON] = new VImg("./org/page/icons/SummonX.png");
		aux.icon[1][P_DMGCUT] = new VImg("./org/page/icons/DmgCut.png");
		aux.icon[1][P_DMGCAP] = new VImg("./org/page/icons/DmgCap.png");
		aux.icon[1][P_IMUARMOR] = new VImg("./org/page/icons/ArmorBreakX.png");
		aux.icon[1][P_IMUSPEED] = new VImg("./org/page/icons/SpeedX.png");
		aux.icon[1][P_BARRIER] = new VImg(parts[330]);
		aux.icon[1][P_COUNTER] = new VImg("./org/page/icons/Counter.png");
		aux.icon[1][P_IMUCANNON] = new VImg("./org/page/icons/CannonX.png");
		aux.icon[1][P_THEME] = new VImg("./org/page/icons/Theme.png");
		aux.icon[1][P_TIME] = new VImg("./org/page/icons/Time.png");
		aux.icon[1][P_BOSS] = new VImg("./org/page/icons/BossWave.png");
		aux.icon[1][P_SNIPER] = new VImg("./org/page/icons/Snipe.png");
		aux.icon[1][P_POISON] = new VImg("./org/page/icons/Poison.png");
		aux.icon[1][P_IMUPOI] = new VImg("./org/page/icons/PoisonX.png");
		aux.icon[1][P_IMUSEAL] = new VImg("./org/page/icons/SealX.png");
		aux.icon[1][P_SEAL] = new VImg("./org/page/icons/Seal.png");
		aux.icon[1][P_MOVEWAVE] = new VImg("./org/page/icons/Moving.png");
		aux.icon[1][P_SUMMON] = new VImg("./org/page/icons/Summon.png");
		aux.icon[1][P_IMUMOVING] = new VImg("./org/page/icons/MovingX.png");
		aux.icon[1][P_WORKERLV] = new VImg("./org/page/icons/WorkerChange.png");
		aux.icon[1][P_CDSETTER] = new VImg("./org/page/icons/CooldownChange.png");
		aux.icon[1][P_WEAKAURA] = new VImg("./org/page/icons/AuraWeak.png");
		aux.icon[1][P_STRONGAURA] = new VImg("./org/page/icons/AuraStrong.png");
		aux.icon[1][P_LETHARGY] = new VImg("./org/page/icons/Lethargy.png");
		aux.icon[1][P_IMULETHARGY] = new VImg("./org/page/icons/ImuLethargy.png");
		aux.icon[1][P_REMOTESHIELD] = new VImg("./org/page/icons/RemoteShield.png");
		aux.icon[1][P_AI] = new VImg("./org/page/icons/Strategize.png");
		aux.icon[1][P_RAGE] = new VImg("./org/page/icons/Rage.png");
		aux.icon[1][P_IMURAGE] = new VImg("./org/page/icons/ImuRage.png");
		aux.icon[1][P_HYPNO] = new VImg("./org/page/icons/Hypno.png");
		aux.icon[1][P_IMUHYPNO] = new VImg("./org/page/icons/ImuHypno.png");
		aux.icon[1][P_DEMONVOLC] = new VImg(parts[315]);
		aux.icon[1][P_DEFINC] = new VImg(parts[204]);
		aux.icon[1][P_DMGINC] = new VImg(parts[206]);
		aux.icon[1][P_RANGESHIELD] = new VImg("./org/page/icons/TargetShield.png");
		aux.icon[1][P_DRAIN] = new VImg("./org/page/icons/Drain.png");
		aux.icon[1][P_BLESS] = new VImg("./org/page/icons/Blessing.png");

		langIcons();
		//Trait icons that remain the same regardless of language
		aux.icon[3][TRAIT_WHITE] = new VImg(parts[86]);
		aux.icon[3][TRAIT_WITCH] = new VImg("./org/page/icons/Witch.png");
		aux.icon[3][TRAIT_EVA] = new VImg("./org/page/icons/Eva.png");
		aux.icon[3][TRAIT_BARON] = new VImg("./org/page/icons/Baron.png");
		aux.icon[3][TRAIT_BEAST] = new VImg("./org/page/icons/Beast.png");
		aux.icon[3][TRAIT_SAGE] = new VImg("./org/page/icons/SuperSage.png");
		aux.icon[3][TRAIT_INFH] = new VImg("./org/page/icons/Base.png");

		//These are used for talent edit page icons
		aux.icon[4][PC2_HP] = new VImg(parts[120]);
		aux.icon[4][PC2_ATK] = new VImg(parts[118]);
		aux.icon[4][PC2_SPEED] = new VImg(parts[96]);
		aux.icon[4][PC2_COST] = new VImg(parts[92]);
		aux.icon[4][PC2_CD] = new VImg(parts[94]);
		aux.icon[4][PC2_HB] = new VImg(parts[98]);
		aux.icon[4][PC2_TBA] = new VImg(parts[305]);
		aux.icon[4][PC2_RNG] = new VImg("./org/page/icons/PlusRange.png");
		//miscellaneous
		aux.waveShield = new VImg(parts[218]);
		aux.dmgIcons[0] = new VImg(parts[203]); //strong vs
		aux.dmgIcons[1] = new VImg(parts[122]); //ins resist
		aux.dmgIcons[2] = new VImg(parts[114]); //ins massive
		CommonStatic.getConfig().icon = false;
	}

	/**
	 * Gets icons that change depending on the language set in config. Currently only EN and JP available.
	 */
	public static void langIcons() {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		ImgCut ic015 = ImgCut.newIns("./org/page/img015.imgcut");
		VImg img015 = new VImg("./org/page/img015" + (CommonStatic.getConfig().langs[0] == Locale.EN ? "_en" : "") + ".png");
		FakeImage[] parts = ic015.cut(img015.getImg());
		aux.icon[2][ATK_SINGLE] = new VImg(parts[217]);
		aux.icon[2][ATK_AREA] = new VImg(parts[211]);
		aux.icon[2][ATK_LD] = new VImg(parts[212]);
		aux.icon[2][ATK_OMNI] = new VImg(parts[112]);

		aux.icon[3][TRAIT_RED] = new VImg(parts[77]);
		aux.icon[3][TRAIT_FLOAT] = new VImg(parts[78]);
		aux.icon[3][TRAIT_BLACK] = new VImg(parts[79]);
		aux.icon[3][TRAIT_METAL] = new VImg(parts[80]);
		aux.icon[3][TRAIT_ANGEL] = new VImg(parts[81]);
		aux.icon[3][TRAIT_ALIEN] = new VImg(parts[82]);
		aux.icon[3][TRAIT_ZOMBIE] = new VImg(parts[83]);
		aux.icon[3][TRAIT_RELIC] = new VImg(parts[84]);
		aux.icon[3][TRAIT_DEMON] = new VImg(parts[85]);
	}

	private static void readBattle() {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		aux.battle[0] = new VImg[4];
		aux.battle[1] = new VImg[22];
		aux.battle[2] = new VImg[12];
		ImgCut ic001 = ImgCut.newIns("./org/page/img001.imgcut");
		VImg img001 = new VImg("./org/page/img001.png");
		FakeImage[] parts = ic001.cut(img001.getImg());
		byte[] vals = new byte[] { 5, 19, 30, 40, 51, 62, 73, 88, 115 };
		byte[] adds = new byte[] { 1, 2, 2, 0, 0, 1, 1, 1, 0 };
		aux.num[0] = new VImg[12];
		aux.num[5] = new VImg[12];
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 10; j++)
				aux.num[i][j] = new VImg(parts[vals[i] - 5 + j]);
			if (adds[i] == 1)
				aux.num[i][10] = new VImg(parts[vals[i] + 5]);
			if (adds[i] == 2)
				aux.num[i][10] = new VImg(parts[vals[i] - 6]);
			if (i == 0)
				aux.num[i][11] = new VImg(parts[103]);
			if (i == 5)
				aux.num[i][11] = new VImg(parts[108]);
		}
		aux.battle[0][3] = new VImg(parts[81]);

		for(int i = 0; i < aux.timer.length; i++)
			aux.timer[i] = new VImg(parts[i + 83]);

		ImgCut moneyCut = ImgCut.newIns("./org/page/moneySign.imgcut");
		VImg moneyImg = new VImg("./org/page/moneySign.png");

		parts = moneyCut.cut(moneyImg.getImg());

		for(int i = 0; i < aux.moneySign.length; i++)
			for(int j = 0; j < aux.moneySign[0].length; j++)
				aux.moneySign[i][j] = new VImg(parts[i * 4 + j]);

		ImgCut ic002 = ImgCut.newIns("./org/page/img002.imgcut");
		VImg img002 = new VImg("./org/page/img002.png");

		parts = ic002.cut(img002.getImg());
		aux.battle[0][0] = new VImg(parts[5]);
		aux.battle[0][1] = new VImg(parts[24]);
		aux.battle[0][2] = new VImg(parts[6]);
		aux.battle[1][0] = new VImg(parts[8]);
		aux.battle[1][1] = new VImg(parts[7]);
		for (int i = 0; i < 10; i++)
			aux.battle[1][2 + i] = new VImg(parts[11 + i]);

		//jp fire
		aux.battle[1][12] = new VImg(parts[9]);
		aux.battle[1][13] = new VImg(parts[10]);
		aux.spiritSummon[3] = new VImg(parts[53]);

		ic002 = ImgCut.newIns("./org/page/img002_en.imgcut");
		img002 = new VImg("./org/page/img002_en.png");
		parts = ic002.cut(img002.getImg());
		//en fire
		aux.battle[1][14] = new VImg(parts[9]);
		aux.battle[1][15] = new VImg(parts[10]);
		aux.spiritSummon[0] = parts.length >= 54 ? new VImg(parts[53]) : aux.spiritSummon[3];

		ic002 = ImgCut.newIns("./org/page/img002_ko.imgcut");
		img002 = new VImg("./org/page/img002_ko.png");
		parts = ic002.cut(img002.getImg());
		//kr fire
		aux.battle[1][16] = new VImg(parts[9]);
		aux.battle[1][17] = new VImg(parts[10]);
		aux.spiritSummon[2] = parts.length >= 54 ? new VImg(parts[53]) : aux.spiritSummon[3];

		ic002 = ImgCut.newIns("./org/page/img002_tw.imgcut");
		img002 = new VImg("./org/page/img002_tw.png");
		parts = ic002.cut(img002.getImg());
		//tw fire
		aux.battle[1][18] = new VImg(parts[9]);
		aux.battle[1][19] = new VImg(parts[10]);
		aux.spiritSummon[1] = parts.length >= 54 ? new VImg(parts[53]) : aux.spiritSummon[3];

		aux.battle[1][20] = new VImg(parts[0]);
		aux.battle[1][21] = new VImg(parts[1]);

		ImgCut icMap = ImgCut.newIns("./org/page/mapicon.imgcut");
		VImg imgMap = new VImg("./org/page/mapicon.png");
		parts = icMap.cut(imgMap.getImg());

		aux.battle[2][0] = new VImg(parts[0]);
		aux.battle[2][1] = new VImg(parts[2]);
		aux.battle[2][2] = new VImg(parts[5]);
		aux.battle[2][3] = new VImg(parts[6]);
		aux.battle[2][4] = new VImg(parts[11]);
		aux.battle[2][5] = new VImg(parts[16]);

		for(int i = 6; i < aux.battle[2].length; i++)
			aux.battle[2][i] = new VImg("./org/page/speedUp" + (i - 3) + ".png");
		// money, lv, lv dark,cost,cost dark,hp, money light,time,point
	}

	private static VImg makeLineupIconFrame(boolean isMagenta) {
		FakeImage fimg = ImageBuilder.builder.build(110, 85);
		FakeGraphics g = fimg.getGraphics();

		if(g != null) {
			if(isMagenta) {
				g.setColor(FakeGraphics.MAGENTA);
			} else {
				g.setColor(255, 255, 0);
			}

			g.fillRect(0, 0, 5, 85);
			g.fillRect(105, 0, 5, 85);
			g.fillRect(0, 0, 110, 5);
			g.fillRect(0, 80, 110, 5);
		}

		return new VImg(fimg);
	}

	public static Locale decideLocale() {
		switch (CommonStatic.getConfig().langs[0]) {
			case EN:
			case KR:
			case JP:
				return CommonStatic.getConfig().langs[0];
			default:
				return Locale.EN;
		}
	}
}
