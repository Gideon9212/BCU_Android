package common.battle.data;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Context.ErrType;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.system.files.VFile;
import common.util.Data;
import common.util.Data.Proc.ProcItem;
import common.util.unit.AbUnit;
import common.util.unit.Trait;
import common.util.unit.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

@JsonClass(read = JsonClass.RType.FILL)
public class PCoin extends Data {
	public static void read() {
		Queue<String> qs = VFile.readLine("./org/data/SkillAcquisition.csv");

		qs.poll();

		for (String str : qs) {
			String[] strs = str.trim().split(",");

			if (strs.length >= 2) {
				int[] data = CommonStatic.parseIntsN(str);

				AbUnit u = Identifier.parseInt(data[0], Unit.class).get();
				if (u != null) {
					if (u.getForms().length > 2)
						new PCoin(data, u.getForms()[2].du);
					if (u.getForms().length > 3)
						new PCoin(data, u.getForms()[3].du);
				}
			}
		}
	}

	private final MaskUnit du;
	public MaskUnit full = null;

	@JsonField(generic = Trait.class, alias = Identifier.class, defval = "isEmpty")
	public SortedPackSet<Trait> trait = new SortedPackSet<>();
	@JsonField(block = true)
	public int[] max;
	@JsonField(generic = int[].class, backCompat = JsonField.CompatType.FORK)
	public final ArrayList<int[]> info = new ArrayList<>();
	@JsonField(generic = int[][].class, backCompat = JsonField.CompatType.FORK, defval = "isEmpty||this.unusedAtk")
	public final ArrayList<int[][]> atks = new ArrayList<>();

	public boolean unusedAtk() {
		if (du.isCommon())
			return true;
		for (int[][] aa : atks)
			if (aa.length > 0)
				return false;
		return true;
	}

	public PCoin(CustomEntity ce) {
		du = (CustomUnit)ce;
		((CustomUnit)du).pcoin = this;
	}

	private PCoin(int[] strs, MaskUnit du) {
		this.du = du;
		trait = Trait.convertType(strs[1], true);

		for (int i = 0; i < 8; i++) {
			if (2 + i * 14 >= strs.length)
				break;
			if (strs[2 + i * 14] != 0) {
				int[] data = new int[14]; //Default length of BC
				for (int j = 0; j < 14; j++)
					data[j] = strs[2 + i * 14 + j];
				if (data[13] == 1) //Super Talent
					data[13] = 60;
				if (data[0] == 62) {//Miniwave
					if (data[6] == 0 && data[7] == 0) {
						data[6] = 20;
						data[7] = 20;
					}
				}

				int[] corres = get_CORRES(data[0]);
				if (corres[0] == -1) {
					CommonStatic.ctx.printErr(ErrType.WARN, "new PCoin ability for " + du.getPack() + " not yet handled by BCU: " + data[0] + "\nData is " + Arrays.toString(data));
					continue;
				}
				int[] trueArr;
				switch (corres[0]) {
					case PC_P:
						trueArr = Arrays.copyOf(data, 3 + (du.getProc().getArr(corres[1]).getAllFields().length - (corres.length >= 3 ? corres[2] : 0)) * 2); //The Math.min is for testing
						break;
					case PC_BASE:
						trueArr = Arrays.copyOf(data, 5);
						break;
					default:
						trueArr = Arrays.copyOf(data, 3);
				}
				trueArr[trueArr.length - 1] = Math.max(0, data[13]);
				info.add(trueArr);
			}
		}
		max = info.stream().mapToInt(i -> Math.max(1, i[1])).toArray();
		((DataUnit)du).pcoin = this;
		full = improve(max);
	}

	public void update() {
		full = improve(max);
	}

	public int getAtkInd(int in) {
		int rep = 0;
		for (int i = 0; i < in; i++) {
			int[] type = get_CORRES(info.get(i)[0]);
			if (type[0] == PC_P && !procSharable[type[1]])
				rep++;
		}
		if (rep >= atks.size())
			return -1;
		return rep;
	}

	public AtkDataModel[] getAtks(MaskUnit ans, int tal) {
		if (tal >= atks.size())
			return new AtkDataModel[0];
		int[][] inds = atks.get(getAtkInd(tal));
		AtkDataModel[] as = new AtkDataModel[inds.length];
		for (int i = 0; i < as.length; i++)
			as[i] = (AtkDataModel)ans.getAtkModel(inds[i][0], inds[i][1]);
		return as;
	}

	public void verify() { // TODO: lmao
		if (du instanceof CustomUnit) {
			for (int i = 0; i < info.size(); i++) {
				if (info.get(i)[0] == PC_CORRES.length)
					continue;
				if (info.get(i)[0] == 23) {
					info.get(i)[0]--;
				} else if (info.get(i)[0] == 5) {
					info.set(i, Arrays.copyOf(info.get(i), 5));
					info.get(i)[0] = -38;
					info.get(i)[4] = info.get(i)[2];
					info.get(i)[2] = info.get(i)[3] = 150;
					info.add(new int[]{-39, 1, 200, 200, 0});
				} else if (info.get(i)[0] == 6 || info.get(i)[0] == 7) {
					boolean repl = false;
					for (int[] iii : info)
						if (info.get(i)[0] - iii[0] == -32) {
							repl = true;
							iii[0] *= info.get(i)[0] - 3;
							break;
						}
					if (repl)
						info.get(i)[0] = PC_CORRES.length;
				}
			}
			info.removeIf(ii -> ii[0] == PC_CORRES.length);
			onInjected();
		}
		/*Proc proc = du.getAllProc();
		for (int[] data : info) {
			data[1] = Math.max(data[1], 1);
			int type = Data.PC_CORRES[data[0]][1];
			ProcItem pi = proc.getArr(type);

			for (int i = 0; i < pi.getDeclaredFields().length - 1; i += 2) {
				int effPos = 2 + i * 2;
				switch (pi.getFieldName(type)) {
					case "prob":
						data[effPos] = MathUtil.clip(data[i], 0, 100 - pi.get(0));
						data[effPos] = MathUtil.clip(data[i + 1], data[i], 100 - pi.get(0));
						break;
				}
			}
			if (Data.PC_CORRES[data[0]][3] != -1) {
				data[1] = Data.PC_CORRES[Data.PC_CORRES[data[0]][3]][1];
				data[2] = data[3] = 100 - proc.getArr(type).get(0);
				return;
			}

			switch (data[0]) {
				case 0:
					break;
				case 56: case 65:
					data[2] = MathUtil.clip(data[2], 0, 100 - proc.getArr(type).get(0));
					data[3] = MathUtil.clip(data[3], data[2], 100 - proc.getArr(type).get(0));
					data[8] = Math.max(1, data[8] / Data.VOLC_ITV) * Data.VOLC_ITV;
					data[9] = Math.max(Math.max(1, data[9] / Data.VOLC_ITV) * Data.VOLC_ITV, data[8]);
					break;
				case 10:
					data[2] = MathUtil.clip(data[2], 0, 100 - proc.getArr(type).get(0));
					data[3] = MathUtil.clip(data[3], data[2], 100 - proc.getArr(type).get(0));
					data[4] = Math.max(data[4], 0);
					data[5] = Math.max(data[5], data[4]);
					break;
				case 61:
					data[2] = MathUtil.clip(data[2], 0, 100);
					data[3] = MathUtil.clip(data[3], data[2], 100);
                    break;
				case 25: case 26: case 31: case 32:
					data[2] = Math.max(data[2], 0);
					data[3] = Math.max(data[3], data[2]);
					break;
				case 64:
					data[2] = MathUtil.clip(data[2], 0, 100 - proc.getArr(type).get(1));
					data[3] = MathUtil.clip(data[3], data[2], 100 - proc.getArr(type).get(1));
					data[4] = Math.max(data[4], 0);
					data[5] = Math.max(data[5], data[4]);
					break;
				case 62: case 1:
					data[6] = Math.max(data[6], 0);
					data[7] = Math.max(data[7], data[6]);
				case 2: case 3: case 9: case 17: case 50: case 51: case 60:
					data[4] = Math.max(data[4], 0);
					data[5] = Math.max(data[5], data[4]);
				case 8: case 11: case 13: case 15: case 18: case 19: case 20: case 21: case 22: case 24: case 30:
				case 52: case 54: case 58:
					data[2] = MathUtil.clip(data[2], 0, 100 - proc.getArr(type).get(0));
					data[3] = MathUtil.clip(data[3], data[2], 100 - proc.getArr(type).get(0));
					break;
			}/
		}*/
	}

	@SuppressWarnings("deprecation")
	public MaskUnit improve(int[] talents) {
		MaskUnit ans = du.clone();

		int[] temp;
		if (talents.length < max.length) {
			temp = new int[max.length];
			System.arraycopy(talents, 0, temp, 0, talents.length);
			System.arraycopy(max, talents.length, temp, talents.length, max.length - talents.length);
		} else
			temp = talents.clone();

		talents = temp;
		int atki = 0;
		for (int i = 0; i < info.size(); i++) {
			if (talents[i] <= 0)
				continue;

			int[] type = get_CORRES(info.get(i)[0]);
			//Targettings that come with a talent, such as Hyper Mr's
			if (!this.trait.isEmpty())
				ans.getTraits().addAll(this.trait);

			int offset = type.length >= 3 && type[0] == PC_P ? type[2] : 0;
			int fieldTOT = -offset;
			if (type[0] == PC_P)
				fieldTOT += ans.getProc().getArr(type[1]).getAllFields().length;
			else if (type[0] == PC_BASE)
				fieldTOT = 1;
			if (du instanceof DataUnit)
				fieldTOT = Math.min(fieldTOT, 4);

			int maxlv = info.get(i)[1];
			int[] modifs = new int[fieldTOT];

			if (maxlv > 1) {
				for (int j = 0; j < fieldTOT; j++) {
					int v0 = info.get(i)[2 + j * 2];
					int v1 = info.get(i)[3 + j * 2];
					modifs[j] = (v1 - v0) * (talents[i] - 1) / (maxlv - 1) + v0;
				}
			} else
				for (int j = 0; j < fieldTOT; j++)
					modifs[j] = info.get(i)[3 + j * 2];

			if (type[0] == PC_P) {
				ProcItem tar = ans.getProc().getArr(type[1]);

				if (type[1] == P_VOLC || type[1] == P_MINIVOLC) {
					if (du instanceof DataUnit) {
						tar.set(0, modifs[0]);
						tar.set(1, modifs[2] / 4);
						tar.set(2, (modifs[2] + modifs[3]) / 4);
						tar.set(3, modifs[1] * 20);
						if (type[1] == P_MINIVOLC && tar.get(5) == 0)
							tar.set(4, 20);
					} else {
						tar.set(0, tar.get(0) + modifs[0]);
						tar.set(1, tar.get(1) + Math.min(modifs[1], modifs[2]));
						tar.set(2, tar.get(2) + Math.max(modifs[1], modifs[2]));
						for (int j = 3; j < fieldTOT; j++)
							tar.set(j, tar.get(j) + modifs[j]);
					}
				} else if (du instanceof DataUnit && type[1] == P_BLAST) {
					tar.set(0, modifs[0]);
					tar.set(1, modifs[1] / 4);
					tar.set(2, (modifs[1] + modifs[2]) / 4);
					tar.set(3, 3);
					tar.set(4, 30);
				} else if (du instanceof DataUnit || ((CustomEntity)du).common || procSharable[type[1]])
					for (int j = 0; j < fieldTOT; j++)
						if (tar.getAllFields()[j].getType() == Identifier.class) {
							if (modifs[j] == 0)
								continue;
							tar.set(j, (modifs[j] > 0 ? UserProfile.getBCData() : du.getPack().getPack()).units.get(Math.abs(modifs[j])-1).getID());
						} else if (modifs[j] != 0)
							tar.set(j+offset, tar.get(j+offset) + modifs[j]);
				if (type[1] == P_BSTHUNT)
					ans.getProc().BSTHUNT.type.active |= modifs[0] > 0;

				if (du instanceof DataUnit) {
					if (type[1] == P_STRONG && modifs[0] != 0)
						tar.set(0, 100 - tar.get(0));
					else if (type[1] == P_WEAK)
						tar.set(2, 100 - tar.get(2));
					else if (type[1] == P_BOUNTY)
						tar.set(0, 100);
					else if (type[1] == P_ATKBASE)
						tar.set(0, 300);
				} else if (!((CustomEntity)du).common && !procSharable[type[1]]) {
					AtkDataModel[] d = getAtks(ans, atki++);
					if (d.length != 0)
						improveAtks(d, type[1], modifs, fieldTOT, offset);
					else {
						for (AtkDataModel[] atkss : ((CustomEntity) ans).hits)
							improveAtks(atkss, type[1], modifs, fieldTOT, offset);
						for (AtkDataModel[] atks : ans.getSpAtks(true))
							improveAtks(atks, type[1], modifs, fieldTOT, offset);
					}
				}
			} else if (type[0] == PC_AB || type[0] == PC_BASE)
				ans.improve(type, type[0] == PC_BASE ? modifs[0] : 0);
			else if (type[0] == PC_IMU)
				ans.getProc().getArr(type[1]).set(0, 100);
			else if (type[0] == PC_TRAIT)
				ans.getTraits().add(UserProfile.getBCData().traits.get(type[1]));
			else if (type[0] == 5) { //special cases
				if (type[1] == P_IMUWAVE)
					ans.getProc().getArr(type[1]).set(1, 100);
				else {
					if (type[2] == 150)
						ans.getProc().DEFINC.mult += 200;
					ans.getProc().getArr(type[1]).set(0,ans.getProc().getArr(type[1]).get(0) + type[2]);
				}
			}
		}
		return ans;
	}
	@SuppressWarnings("deprecation")
	private void improveAtks(AtkDataModel[] atkss, int ptype, int[] modifs, int fieldTOT, int offset) { //ptype is type[1]
		for (AtkDataModel atk : atkss) {
			ProcItem atks = atk.proc.getArr(ptype);
			if (ptype == P_VOLC || ptype == P_MINIVOLC) {
				atks.set(0, modifs[0]);
				atks.set(1, Math.min(modifs[1], modifs[2]));
				atks.set(2, Math.max(modifs[1], modifs[2]));
				for (int j = 3; j < fieldTOT; j++)
					atks.set(j, atks.get(j) + modifs[j]);
			} else
				for (int j = 0; j < fieldTOT; j++)
					if (modifs[j] > 0)
						atks.set(j+offset, atks.get(j+offset) + modifs[j]);
		}
	}

	public double getStatMultiplication(byte mult, int[] talents) {
		for(int i = 0; i < info.size(); i++) {
			if (i >= talents.length)
				break;
			if(talents[i] == 0 || info.get(i)[0] >= PC_CORRES.length || info.get(i)[0] < 0)
				continue;

			int[] type = PC_CORRES[info.get(i)[0]];
			if(type[0] == PC_BASE && type[1] == mult) {
				int maxlv = info.get(i)[1];
				if (maxlv > 1) {
					int v0 = info.get(i)[2];
					int v1 = info.get(i)[3];
					int modif = (v1 - v0) * (talents[i] - 1) / (maxlv - 1) + v0;
					return 1 + modif * 0.01;
				}
				return 1 + info.get(i)[3] * 0.01;
			}
		}
		return 1.0;
	}

	public int getReqLv(int i) {
		int[] tal = info.get(i);
		return tal[tal.length - 1];
	}

	@OnInjected
	public void onInjected() {
		max = info.stream().mapToInt(i -> Math.max(1, i[1])).toArray();
		boolean old = atks.isEmpty();
		for (int i = 0; i < info.size(); i++) {
			int[] type = get_CORRES(info.get(i)[0]);
			if (type[0] != PC_P)
				continue;
			if (old && !procSharable[type[1]])
				atks.add(new int[0][]);

			int fieldTOT = (type.length >= 3 ? -type[2] : 0) + du.getProc().getArr(type[1]).getAllFields().length * 2;
			if (info.get(i).length - 3 == fieldTOT)
				continue;
			int[] modifs = Arrays.copyOf(info.get(i), fieldTOT + 3);
			modifs[fieldTOT + 2] = info.get(i)[info.get(i).length - 1];
			if (info.get(i).length - 3 < fieldTOT)
				modifs[info.get(i).length - 1] = 0;
			info.set(i, modifs);
		}
	}

	@JsonField(tag = "info", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public ArrayList<int[]> oldInfo() {
		ArrayList<int[]> oi = new ArrayList<>();
		for (int[] ii : info) {
			int[] ni = Arrays.copyOf(ii, 14);
			ni[13] = ii[ii.length - 1];
			if (ii.length < 14)
				ni[ii.length - 1] = 0;

			int[] COR = get_CORRES(ii[0]);
			if (COR[0] == 5) {
				if (COR[1] == P_IMUWAVE && ii[5] >= 100)
					ni[0] = 23;//Port old waveblock talent
				else if (COR[1] == P_DMGINC)
					ni[0] = ni[3] < 300 ? 5 : 7;
				else if (COR[1] == P_DEFINC)
					ni[0] = ni[3] < 400 ? 5 : 6;
			}

			boolean add = ni[0] > 0; //Prevent custom talent porting coz it crash main
			if (ni[0] == 5) {
				for (int[] iii : oi)
					if (iii[0] == 5) {
						add = false;
						break;
					}
			}
			if (add)
				oi.add(ni);
		}
		return oi;
	}
}
