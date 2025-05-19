package common.util.stage;

import com.google.gson.*;
import common.CommonStatic;
import common.io.assets.Admin.StaticPermitted;
import common.io.json.JsonClass;
import common.io.json.JsonClass.RType;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.FixIndexList.FixIndexMap;
import common.pack.IndexContainer;
import common.pack.PackData.UserPack;
import common.pack.SaveData;
import common.pack.UserProfile;
import common.system.files.VFile;
import common.util.Data;
import common.util.lang.MultiLangCont;
import common.util.stage.info.CustomStageInfo;
import common.util.stage.info.DefStageInfo;
import common.util.unit.Level;
import common.util.unit.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@JsonClass(read = RType.FILL)
public abstract class MapColc extends Data implements IndexContainer.SingleIC<StageMap> {

	public static class DefMapColc extends MapColc {

		private static final String REG_IDMAP = "DefMapColc_idmap";

		/**
		 * get a BC stage
		 */
		public static StageMap getMap(int mid) {
			Map<String, MapColc> map = UserProfile.getRegister(REG_MAPCOLC);
			MapColc mc = map.get(Data.hex(mid / 1000));
			if (mc == null)
				return null;
			return mc.maps.get(mid % 1000);
		}

		public static DefMapColc getMap(String id) {
			return (DefMapColc) UserProfile.getRegister(REG_MAPCOLC)
					.get(Data.hex(UserProfile.getRegister(REG_IDMAP, int.class).get(id)));
		}

		public static void read(Consumer<Double> bar) {
			final Map<String, Integer> idmap = UserProfile.getRegister(REG_IDMAP);
			idmap.put("CH", 3);
			idmap.put("M", 12);
			idmap.put("N", 0);
			idmap.put("A", 13);
			idmap.put("ND", 34);
			idmap.put("S", 1);
			idmap.put("C", 2);
			idmap.put("E", 4);
			idmap.put("B", 14);
			idmap.put("V", 7);
			idmap.put("H", 25);
			idmap.put("Q", 31);
			idmap.put("L", 33);
			idmap.put("RA", 24);
			idmap.put("CA", 27);
			idmap.put("T", 6);
			idmap.put("R", 11);
			idmap.put("SR", 36);
			idmap.put("G", 37);

			for (int i = 0; i < strs.length; i++)
				new CastleList.DefCasList(Data.hex(i), strs[i]);
			VFile f = VFile.get("./org/stage/");
			if (f == null)
				return;
			ArrayList<VFile> sortedFiles = new ArrayList<>(f.list());
			sortedFiles.removeIf(v -> v.getName().equals("CH") || v.getName().equals("D") || v.getName().equals("DM"));
			sortedFiles.sort((vff, vf) -> {
				Set<String> ids = idmap.keySet();
				int n = 0, o = -1, p = -1;
				for (String str : ids)
					if (str.equals(vff.getName())) {
						o = n;
						if (p != -1)
							return 1;
					} else if (str.equals(vf.getName())) {
						p = n;
						if (o != -1)
							return -1;
					} else
						n++;
				return vff.compareTo(vf);
			});
			new DefMapColc();
			double load = 0.0;
			for (VFile fi : sortedFiles) {
				bar.accept(++load / idmap.size());
				List<VFile> list = new ArrayList<>(fi.list());
				VFile map = list.get(0);
				List<VFile> stage = new ArrayList<>();
				for (int i = 1; i < list.size(); i++) {
					if(fi.getName().equals("N") && list.get(i).getName().contains("stageRN-1"))
						continue;

					if (list.get(i).list() != null)
						stage.addAll(list.get(i).list());
				}
				new DefMapColc(fi.getName(), idmap.get(fi.getName()), stage, map);
			}
			Queue<String> qs = VFile.readLine("./org/data/Map_option.csv");
			qs.poll();
			for (String str : qs) {
				String[] strs = str.trim().split(",");
				int id = Integer.parseInt(strs[0]);
				StageMap sm = getMap(id);
				if (sm == null)
					continue;
				int len = Integer.parseInt(strs[1]);
				sm.stars = new int[len];
				for (int i = 0; i < len; i++)
					sm.stars[i] = Integer.parseInt(strs[2 + i]);
				sm.names.put(sm.names + strs[10]);
				sm.starMask = Integer.parseInt(strs[12]);

				if(sm.info != null) {
					if(!strs[7].equals("0")) {
						sm.info.resetMode = Integer.parseInt(strs[7]);

						if(sm.info.resetMode > 3) {
							System.out.println("W/MapColc | Unknown stage reward reset mode " + sm.info.resetMode);
						}
					}

					if(!strs[8].equals("0")) {
						sm.info.clearLimit = Integer.parseInt(strs[8]);
					}

					sm.info.hiddenUponClear = !strs[13].equals("0");

					if(!strs[10].equals("0")) {
						sm.info.waitTime = Integer.parseInt(strs[10]);
					}
				}
			}
			qs = VFile.readLine("./org/data/EX_lottery.csv");
			List<Stage> exLottery = new ArrayList<>();

			String lotteryLine = qs.poll();
			boolean canGo = true;

			while(lotteryLine != null && !lotteryLine.isEmpty()) {
				int[] lotteryData = CommonStatic.parseIntsN(lotteryLine);

				if(lotteryData.length != 2) {
					System.out.println("W/MapColc | New format of EX lottery line found : "+Arrays.toString(lotteryData));

					canGo = false;
					break;
				}

				StageMap sm = getMap(lotteryData[0]);

				if(sm == null) {
					System.out.println("W/MapColc | No such stage map found : "+lotteryData[0]);

					canGo = false;
					break;
				}

				Stage s = sm.list.get(lotteryData[1]);

				if(s == null) {
					System.out.println("W/MapColc | No such stage found : "+lotteryData[0]+" - "+lotteryData[1]);

					canGo = false;
					break;
				}

				exLottery.add(s);

				lotteryLine = qs.poll();
			}

			if(canGo) {
				qs = VFile.readLine("./org/data/EX_group.csv");

				String groupLine = qs.poll();

				while(groupLine != null && !groupLine.isEmpty()) {
					int[] groupData = CommonStatic.parseIntsN(groupLine);

					float maxPercentage = groupData[0];

					StageMap sm = getMap(groupData[1]);

					if(sm == null) {
						groupLine = qs.poll();

						continue;
					}

					Stage s = sm.list.get(groupData[2]);

					if(s == null || s.info == null) {
						groupLine = qs.poll();

						continue;
					}

					int exLength = groupData.length - 3;

					if(exLength % 2 != 0) {
						System.out.println("W/MapColc | Invalid EX group format : " + Arrays.toString(groupData));

						groupLine = qs.poll();

						continue;
					}

					exLength /= 2;

					Stage[] exStage = new Stage[exLength];
					float[] exChance = new float[exLength];

					for(int i = 0; i < exLength; i++) {
						if(groupData[i * 2 + 3] >= exLottery.size()) {
							System.out.println("M/MapColc | EX lottery ID is higher than actual length : In group -> "+groupData[i * 2 + 3]+" / In lottery -> "+exLottery.size());

							break;
						}

						exStage[i] = exLottery.get(groupData[i * 2 + 3]);
						exChance[i] = maxPercentage * (groupData[i * 2 + 4] / 100f);

						maxPercentage -= exChance[i];
					}

					maxPercentage = groupData[0];

					for(int i = 0; i < exLength; i++) {
						exChance[i] /= maxPercentage / 100;
					}

					DefStageInfo def = ((DefStageInfo)s.info);
					def.exStages = exStage;
					def.exChances = exChance;

					groupLine = qs.poll();
				}
			}

			qs = VFile.readLine("./org/data/DropItem.csv");

			qs.poll();

			String dropLine = qs.poll();
			while(dropLine != null && !dropLine.isEmpty()) {
				String[] dropData = dropLine.split(",");

				int mapID = CommonStatic.safeParseInt(dropData[0]);
				StageMap sm = getMap(mapID);

				if(sm != null && sm.info != null)
					sm.info.injectMaterialDrop(dropData);
				dropLine = qs.poll();
			}

			qs = VFile.readLine("./org/data/LockSkipData.csv");
			String skipLine = qs.poll();
			while(skipLine != null && !skipLine.isEmpty()) {
				String[] skipData = skipLine.split(",");

				if (skipData.length != 3 || !CommonStatic.isInteger(skipData[1])) {
					skipLine = qs.poll();
					continue;
				}

				int mapID = CommonStatic.safeParseInt(skipData[1]);
				boolean wholeCollection = CommonStatic.safeParseInt(skipData[0]) == 0;
				if (wholeCollection) {
					MapColc mc = get(Data.hex(mapID / 1000));
					for (StageMap map : mc.maps)
						if (map != null && map.info != null)
							map.info.unskippable = true;
				} else {
					StageMap sm = getMap(mapID);
					if (sm != null && sm.info != null)
						sm.info.unskippable = true;
				}
				skipLine = qs.poll();
			}

			VFile ruleFile = VFile.get("./org/data/SpecialRulesMap.json");
			VFile ruleOptionFile = VFile.get("./org/data/SpecialRulesMapOption.json");

			String specialRules = new String(ruleFile.getData().getBytes());
			String specialRulesOption = new String(ruleOptionFile.getData().getBytes());

			JsonElement ruleElement = JsonParser.parseString(specialRules);
			JsonElement ruleOptionElement = JsonParser.parseString(specialRulesOption);

			if (ruleElement.isJsonObject() && ruleOptionElement.isJsonObject()) {
				JsonObject rule = ruleElement.getAsJsonObject();
				JsonObject ruleOption = ruleOptionElement.getAsJsonObject();

				Map<Integer, List<Integer>> bannedComboData = new HashMap<>();

				JsonObject ruleList = ruleOption.getAsJsonObject("RuleType");

				for (String key : ruleList.keySet()) {
					JsonElement comboData = ruleList.get(key);

					int ruleID = CommonStatic.parseIntN(key);

					JsonArray comboArray = comboData.getAsJsonObject().getAsJsonArray("InvalidNyancomboID");

					List<Integer> bannedCombo = new ArrayList<>();

					for (JsonElement element : comboArray) {
						if (!element.isJsonPrimitive())
							continue;

						bannedCombo.add(element.getAsInt());
					}

					bannedComboData.put(ruleID, bannedCombo);
				}

				JsonObject mapIDs = rule.getAsJsonObject("MapID");

				for (String id : mapIDs.keySet()) {
					JsonObject ruleData = mapIDs.getAsJsonObject(id);
					int mapID = CommonStatic.safeParseInt(id);

					StageMap map = getMap(mapID);

					if (map == null)
						continue;

					JsonObject ruleTypes = ruleData.getAsJsonObject("RuleType");

					for (String key : ruleTypes.keySet()) {
						int ruleID = CommonStatic.parseIntN(key);
						JsonObject parameterData = ruleTypes.getAsJsonObject(key);

						JsonArray parameter = parameterData.getAsJsonArray("Parameters");

						switch (ruleID) {
							// Max Money
							case 0:
								if (!parameter.isEmpty()) {
									if (parameter.size() != 1)
										System.out.printf("W/MapColc::read - Unknown parameter data found for rule type %d : %s%n", ruleID, parameter);

									int maxMoney = parameter.get(0).getAsInt();
									List<Integer> bannedCombo = bannedComboData.compute(ruleID, (k, v) -> {
										if (v == null) {
											System.out.printf("W/MapColc::read - Unknown banned cat combo data found for rule type %d%n", ruleID);
											return new ArrayList<>();
										} else
											return v;
									});
									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									StageLimit slim = map.lim.get(map.lim.size() - 1).stageLimit;
									slim.maxMoney = maxMoney;
									slim.bannedCatCombo.addAll(bannedCombo);
									slim.coolStart = true;
								}

								break;
							// Global Cooldown
							case 1:
								if (!parameter.isEmpty()) {
									if (parameter.size() != 1)
										System.out.printf("W/MapColc::read - Unknown parameter data found for rule type %d : %s%n", ruleID, parameter);

									int globalCooldown = parameter.get(0).getAsInt();
									List<Integer> bannedCombo = bannedComboData.compute(ruleID, (k, v) -> {
										if (v == null) {
											System.out.printf("W/MapColc::read - Unknown banned cat combo data found for rule type %d%n", ruleID);

											return new ArrayList<>();
										} else
											return v;
									});
									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									StageLimit slim = map.lim.get(map.lim.size() - 1).stageLimit;
									slim.globalCooldown = globalCooldown;
									slim.bannedCatCombo.addAll(bannedCombo);
								}

								break;
							// Rarity deploy limit
							case 3:
								if (!parameter.isEmpty()) {
									int[] deployLimit = new int[parameter.size()];
									for (int i = 0; i < parameter.size(); i++) {
										deployLimit[i] = parameter.get(i).getAsInt();
										if (deployLimit[i] <= 0)
											System.out.printf("W/MapColc::read - Unexpected deploy limit value for map %d : Index = %d, Value = %d", mapID, i, deployLimit[i]);
									}
									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									StageLimit slim = map.lim.get(map.lim.size() - 1).stageLimit;
									System.arraycopy(deployLimit, 0, slim.rarityDeployLimit, 0, Math.min(slim.rarityDeployLimit.length, deployLimit.length));
								}
								break;
							// Global Cost
							case 4:
								if (!parameter.isEmpty()) {
									if (parameter.size() != 1)
										System.out.printf("W/MapColc::read - Unknown parameter data found for rule type %d : %s%n", ruleID, parameter);

									int globalCost = parameter.get(0).getAsInt();
									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									map.lim.get(map.lim.size() - 1).stageLimit.globalCost = globalCost;
								}
								break;
							// Cost Multiplier
							case 5:
								if (!parameter.isEmpty()) {
									int[] multiplier = new int[parameter.size()];
									for (int i = 0; i < parameter.size(); i++)
										multiplier[i] = parameter.get(i).getAsInt();
									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									StageLimit slim = map.lim.get(map.lim.size() - 1).stageLimit;

									if (slim.costMultiplier.length != multiplier.length)
										System.out.printf(
												"W/MapColc::read - Desynced cost multiplier data -> Original = %d, Obtained = %d, Data = [ %s ]%n",
												slim.costMultiplier.length, multiplier.length, Arrays.toString(multiplier)
										);
									System.arraycopy(multiplier, 0, slim.costMultiplier, 0, Math.min(slim.costMultiplier.length, multiplier.length));
								}
								break;
							// Cooldown Multiplier
							case 6:
								if (!parameter.isEmpty()) {
									int[] multiplier = new int[parameter.size()];
									for (int i = 0; i < parameter.size(); i++)
										multiplier[i] = parameter.get(i).getAsInt();
									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									StageLimit slim = map.lim.get(map.lim.size() - 1).stageLimit;

									if (slim.cooldownMultiplier.length != multiplier.length)
										System.out.printf(
												"W/MapColc::read - Desynced cost multiplier data -> Original = %d, Obtained = %d, Data = [ %s ]%n",
												slim.cooldownMultiplier.length, multiplier.length, Arrays.toString(multiplier)
										);
									System.arraycopy(multiplier, 0, slim.cooldownMultiplier, 0, Math.min(slim.cooldownMultiplier.length, multiplier.length));
								}
								break;
							// Max spawn units
							case 7:
								if (!parameter.isEmpty()) {
									if (parameter.size() > 1) {
										System.out.printf(
												"W/MapColc::read - Unexpected parameter size for map %d : Size = %d\n",
												mapID, parameter.size()
										);
									}
									int maxUnitSpawn = parameter.get(0).getAsInt();
									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									StageLimit slim = map.lim.get(map.lim.size() - 1).stageLimit;
									slim.maxUnitSpawn = maxUnitSpawn;
								}
								break;
							case 8:
								if (!parameter.isEmpty()) {
									if (parameter.size() > 3) {
										System.out.printf(
												"W/MapColc::read - Unexpected parameter size for map %d : Size = %d\n",
												mapID, parameter.size()
										);
									}
									List<Integer> indices = new ArrayList<>();

									int bitMask = parameter.get(0).getAsInt();
									int deployTimes = parameter.get(1).getAsInt();
									int deployDelay = parameter.get(2).getAsInt();
									for (int i = 0; i < 5; i++)
										if ((bitMask & (1 << i)) != 0)
											indices.add(i);

									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									StageLimit slim = map.lim.get(map.lim.size() - 1).stageLimit;

									for (int index : indices) {
										slim.deployDuplicationTimes[index] = deployTimes;
										slim.deployDuplicationDelay[index] = deployDelay;
									}
								}
								break;
							case 9:
								if (!parameter.isEmpty()) {
									if (parameter.size() > 1) {
										System.out.printf(
												"W/MapColc::read - Unexpected parameter size for map %d : Size = %d\n",
												mapID,
												parameter.size()
										);
									}
									int cannonMultiplier = parameter.get(0).getAsInt();

									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									map.lim.get(map.lim.size() - 1).stageLimit.cannonMultiplier = cannonMultiplier;
								}
								break;
							case 10:
								if (!parameter.isEmpty()) {
									if (parameter.size() < 4) {
										System.out.printf(
												"W/MapColc::read - Unexpected parameter size for map %d : Size = %d\n",
												mapID,
												parameter.size()
										);
									}
									int unitActivate = parameter.get(0).getAsInt();
									int enemyActivate = parameter.get(2).getAsInt();

									if (map.lim.isEmpty() || map.lim.get(map.lim.size() - 1).stageLimit == null)
										map.lim.add(new Limit(new StageLimit()));
									StageLimit slim = map.lim.get(map.lim.size() - 1).stageLimit;
									if (unitActivate > 0)
										slim.unitSpeedLimit = parameter.get(1).getAsInt();
									if (enemyActivate > 0)
										slim.enemySpeedLimit = parameter.get(3).getAsInt();
								}
								break;
						}
					}
				}
			}
			// Battle preset
			qs = VFile.readLine("./org/data/fixed_formation.csv");
			if (qs != null) {
				qs.poll();
				while(!qs.isEmpty()) {
					String[] presetData = qs.poll().split(",");

					if (presetData.length < 4)
						continue;
					int mapID = CommonStatic.safeParseInt(presetData[0]);
					StageMap map = getMap(mapID);
					if (map == null)
						continue;
					int stageID = CommonStatic.safeParseInt(presetData[2]);
					if (stageID >= map.list.size())
						continue;
					Stage targetStage = map.list.get(stageID);
					if (targetStage == null)
						continue;
					String presetFileName = presetData[3];
					targetStage.preset = new BattlePreset();
					targetStage.preset.level = CommonStatic.safeParseInt(presetData[1]);

					VFile presetFile = VFile.get("./org/battle/preset/" + presetFileName);
					if (presetFile == null)
						continue;
					String presetFileContents = new String(presetFile.getData().getBytes());
					JsonElement presetElement = JsonParser.parseString(presetFileContents);
					if (!presetElement.isJsonObject())
						continue;
					JsonObject presetObject = presetElement.getAsJsonObject();
					if (
						!presetObject.has("chara") ||
						!presetObject.has("slot") ||
						!presetObject.has("ability") ||
						!presetObject.has("cannon") ||
						!presetObject.has("treasure")
					) {
						System.out.printf("W/MapColc::read - Invalid preset found : Map ID = %d, Stage ID = %d, Preset File Name = %s\n", mapID, stageID, presetFileName);
						continue;
					}
					Map<Unit, BattlePreset.LevelObject> levelObjects = new HashMap<>();
					JsonObject charaData = presetObject.getAsJsonObject("chara").getAsJsonObject("data");
					for (String key : charaData.keySet()) {
						JsonObject o = charaData.getAsJsonObject(key);

						if (o.has("remove") && o.get("remove").getAsBoolean())
							continue;
						int unitID = CommonStatic.safeParseInt(key);
						Unit u = UserProfile.getBCData().units.get(unitID);
						if (u == null)
							continue;

						BattlePreset.LevelObject levelData = new BattlePreset.LevelObject();
						if (o.has("evolution")) {
							levelData.evolution = o.get("evolution").getAsInt() - 1;
						}
						if (o.has("level")) {
							levelData.level = o.get("level").getAsInt();
						}
						if (o.has("plus")) {
							levelData.plusLevel = o.get("plus").getAsInt();
						}
						if (levelData.evolution >= u.forms.length)
							continue;
						levelObjects.put(u, levelData);
					}

					JsonObject slotData = presetObject.getAsJsonObject("slot")
							.getAsJsonObject("data")
							.getAsJsonObject("0");
					JsonArray unitSlotData = slotData.getAsJsonArray("chara");

					int i = 0;
					for (JsonElement e : unitSlotData) {
						if (!(e instanceof JsonPrimitive)) {
							continue;
						}

						JsonPrimitive id = e.getAsJsonPrimitive();
						int unitID;
						if (id.isNumber()) {
							unitID = id.getAsInt();
						} else {
							unitID = CommonStatic.safeParseInt(id.getAsString());
						}

						Unit u = UserProfile.getBCData().units.get(unitID);
						if (u == null)
							continue;

						BattlePreset.LevelObject levelData = levelObjects.get(u);
						if (levelData == null) {
							System.out.printf("W/MapColc::read - No LevelObject found for unit : %d\n", u.id.id);

							continue;
						}

						targetStage.preset.fs[i / 5][i % 5] = u.forms[levelData.evolution];
						targetStage.preset.levels[i / 5][i % 5] = new Level();
						targetStage.preset.levels[i / 5][i % 5].setLevel(levelData.level);
						targetStage.preset.levels[i / 5][i % 5].setPlusLevel(levelData.plusLevel);
						i++;
					}

					targetStage.preset.cannonType = slotData.get("cannon").getAsInt();
					JsonObject abilityData = presetObject.getAsJsonObject("ability").getAsJsonObject("data");
					for (String key : abilityData.keySet()) {
						int abilityIndex = CommonStatic.safeParseInt(key);
						JsonObject upgradeData = abilityData.getAsJsonObject(key);

						int realIndex;

						switch (abilityIndex) {
							case 0:
								realIndex = LV_CATK;
								break;
							case 1:
								realIndex = LV_CRG;
								break;
							case 2:
								realIndex = LV_RECH;
								break;
							case 3:
								realIndex = LV_WORK;
								break;
							case 4:
								realIndex = LV_WALT;
								break;
							case 5:
								realIndex = LV_BASE;
								break;
							case 6:
								realIndex = LV_RES;
								break;
							case 7:
								realIndex = LV_ACC;
								break;
							case 8:
								realIndex = LV_XP;
								break;
							default:
								if (abilityIndex != 9) {
									System.out.printf("W/MapColc::read - Undefined ability index %d found\n", abilityIndex);
								}
								continue;
						}
						int level = 0;
						if (upgradeData.has("level")) {
							level += upgradeData.get("level").getAsInt();
						}
						if (upgradeData.has("plus")) {
							level += upgradeData.get("plus").getAsInt();
						}
						if (level > MLV[realIndex]) {
							System.out.printf("W/MapColc::read - Provided level for ability index %d is out of range : %d > %d", realIndex, level, MLV[realIndex]);
							level = MLV[realIndex];
						}
						targetStage.preset.tech[realIndex] = level;
					}
					JsonObject cannonData = presetObject.getAsJsonObject("cannon").getAsJsonObject("data");
					for (String key : cannonData.keySet()) {
						int id = CommonStatic.safeParseInt(key);

						int realIndex;

						switch(id) {
							case 0:
								realIndex = BASE_H;
								break;
							case 1:
								realIndex = BASE_SLOW;
								break;
							case 2:
								realIndex = BASE_WALL;
								break;
							case 3:
								realIndex = BASE_STOP;
								break;
							case 4:
								realIndex = BASE_WATER;
								break;
							case 5:
								realIndex = BASE_GROUND;
								break;
							case 6:
								realIndex = BASE_BARRIER;
								break;
							case 7:
								realIndex = BASE_CURSE;
								break;
							default:
								System.out.printf("W/MapColc::read - Unknown cannon ID %d\n", id);
								continue;
						}
						targetStage.preset.bslv[realIndex] = cannonData.getAsJsonObject(key).get("level").getAsInt();
					}
					JsonObject treasureObject = presetObject.getAsJsonObject("treasure");
					JsonObject treasureDataObject = presetObject.getAsJsonObject("treasure").getAsJsonObject("data");

					ArrayList<BattlePreset.ActivatedTreasure> unlistedTreasure = new ArrayList<>(Arrays.asList(BattlePreset.ActivatedTreasure.values()));
					for (String key : treasureDataObject.keySet()) {
						int id = CommonStatic.safeParseInt(key);
						JsonArray countData = treasureDataObject.getAsJsonObject(key).getAsJsonArray("count");

						int[] count = new int[3];

						for (int j = 0; j < countData.size(); j++) {
							if (j >= count.length) {
								System.out.printf("W/MapColc::read - Treasure data index out of bound : Treasure ID = %d, Size = %d\n", id, countData.size());

								break;
							}

							count[j] = CommonStatic.safeParseInt(countData.get(j).getAsString());
						}

						BattlePreset.ActivatedTreasure activatedTreasure;

						switch (id) {
							case 0:
								activatedTreasure = BattlePreset.ActivatedTreasure.EOC1;
								break;
							case 1:
								activatedTreasure = BattlePreset.ActivatedTreasure.EOC2;
								break;
							case 2:
								activatedTreasure = BattlePreset.ActivatedTreasure.EOC3;
								break;
							case 3:
								activatedTreasure = BattlePreset.ActivatedTreasure.BASE;
								break;
							case 4:
								activatedTreasure = BattlePreset.ActivatedTreasure.ITF1;
								break;
							case 5:
								activatedTreasure = BattlePreset.ActivatedTreasure.ITF2;
								break;
							case 6:
								activatedTreasure = BattlePreset.ActivatedTreasure.ITF3;
								break;
							case 7:
								activatedTreasure = BattlePreset.ActivatedTreasure.COTC1;
								break;
							case 8:
								activatedTreasure = BattlePreset.ActivatedTreasure.COTC2;
								break;
							case 9:
								activatedTreasure = BattlePreset.ActivatedTreasure.COTC3;
								break;
							default:
								System.out.printf("W/MapColc::read - Unknown Treasure ID %d found\n", id);
								continue;
						}

						unlistedTreasure.remove(activatedTreasure);

						boolean activated;

						if (count[2] == 48 || id == 3) {
							activated = true;
						} else {
							if (count[2] != 0)
								System.out.printf("W/MapColc::read - Unexpected treasure count number %d : [ %d, %d, %d ]\n", count[2], count[0], count[1], count[2]);

							activated = false;
						}

						if (!activated)
							continue;
						updateTreasureData(activatedTreasure, targetStage.preset);
					}

					if (treasureObject.has("defaultData") && treasureObject.getAsJsonObject("defaultData").has("none")) {
						for (BattlePreset.ActivatedTreasure treasure : unlistedTreasure)
							updateTreasureData(treasure, targetStage.preset);
					}

					//validation
					for (int j = 0; j < MT.length; j++) {
						if (targetStage.preset.trea[j] > MT[j]) {
							System.out.printf("W/MapColc::read - Treasure value out of range : %d, %d\n", j, targetStage.preset.trea[j]);

							targetStage.preset.trea[j] = MT[j];
						}
					}

					if (targetStage.preset.alien > 600) {
						System.out.printf("W/MapColc::read - ItF crystal value out of range : %d\n", targetStage.preset.alien);

						targetStage.preset.alien = 600;
					}

					if (targetStage.preset.star > 1500) {
						System.out.printf("W/MapColc::read - CotC crystal value out of range : %d\n", targetStage.preset.star);

						targetStage.preset.star = 1500;
					}

					for (int j = 0; j < targetStage.preset.fruit.length; j++) {
						if (targetStage.preset.fruit[j] > 300) {
							System.out.printf("W/MapColc::read - Fruit treasure value out of range : %d, %d\n", j, targetStage.preset.fruit[j]);

							targetStage.preset.fruit[j] = 300;
						}
					}

					for (int j = 0; j < targetStage.preset.gods.length; j++) {
						if (targetStage.preset.gods[j] > 100) {
							System.out.printf("W/MapColc::read - God mask treasure value out of range : %d, %d\n", j, targetStage.preset.fruit[j]);

							targetStage.preset.gods[j] = 100;
						}
					}
				}
			}
		}

		public final int id;
		public final String name;

		private DefMapColc() {
			id = 3;
			UserProfile.getRegister(REG_MAPCOLC).put(Data.hex(id), this);
			name = "CH";
			String abbr = "./org/stage/CH/stageNormal/stageNormal";
			for(int j = 0; j < 3; j++) {
				if(j == 0) {
					for (int i = 0; i < 3; i++) {
						int I = i;
						add(i, id -> new StageMap(id, abbr + "0_" + I + "_Z.csv", 1)).names.put("EoC " + (i + 1) + " Zombie");
					}
				} else if(j == 1) {
					for (int i = 0; i < 3; i++) {
						int I = i;
						add(3 + i, id -> new StageMap(id, abbr + "1_" + I + ".csv", 2)).names.put("ItF " + (i + 1));
					}
				} else {
					for (int i = 0; i < 3; i++) {
						int I = i;
						add(6 + i, id -> new StageMap(id, abbr + "2_" + I + ".csv", 3)).names.put("CotC " + (i + 1));
					}
				}
			}

			add(9, id -> new StageMap(id, abbr + "0.csv", 1)).names.put("EoC 1-3");
			add(10, id -> new StageMap(id, abbr + "1_0_Z.csv", 2)).names.put("ItF 1 Zombie");
			add(11, id -> new StageMap(id, abbr + "2_2_Invasion.csv", 2)).names.put("CotC 3 Invasion");
			add(12, id -> new StageMap(id, abbr + "1_1_Z.csv", 2)).names.put("ItF 2 Zombie");
			add(13, id -> new StageMap(id, abbr + "1_2_Z.csv", 2)).names.put("ItF 3 Zombie");

			String akuOutbreak = "./org/stage/DM/";

			add(14, id -> new StageMap(id, akuOutbreak+"MSDDM/MapStageDataDM_000.csv", 0));

			add(15, id -> new StageMap(id, abbr + "2_0_Z.csv", 3)).names.put("CotC 1 Zombie");
			add(16, id -> new StageMap(id, abbr + "2_1_Z.csv", 3)).names.put("CotC 2 Zombie");

			VFile stz = VFile.get("./org/stage/CH/stageZ/");
			for (VFile vf : stz.list()) {
				String str = vf.getName();
				int id0, id1;
				try {
					id0 = Integer.parseInt(str.substring(6, 8));
					id1 = Integer.parseInt(str.substring(9, 11));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				if (id0 < 3)
					maps.get(id0).add(id1, id -> new Stage(id, vf, 0));
				else if (id0 == 4)
					maps.get(10).add(id1, id -> new Stage(id, vf, 0));
				else if (id0 == 5)
					maps.get(12).add(id1, id -> new Stage(id, vf, 0));
				else if (id0 == 6)
					maps.get(13).add(id1, id -> new Stage(id, vf, 0));
				else if (id0 == 7)
					maps.get(15).add(id1, id -> new Stage(id, vf, 0));
				else if (id0 == 8)
					maps.get(16).add(id1, id -> new Stage(id, vf, 0));
			}
			VFile stw = VFile.get("./org/stage/CH/stageW/");
			for (VFile vf : stw.list()) {
				String str = vf.getName();
				int id0, id1;
				try {
					id0 = Integer.parseInt(str.substring(6, 8));
					id1 = Integer.parseInt(str.substring(9, 11));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				maps.get(id0 - 1).add(id1, id -> new Stage(id, vf, 1));
			}
			VFile sts = VFile.get("./org/stage/CH/stageSpace/");
			for (VFile vf : sts.list()) {
				String str = vf.getName();
				if (str.length() > 20) {
					maps.get(11).add(0, id -> new Stage(id, vf, 0));
					continue;
				}
				int id0, id1;
				try {
					id0 = Integer.parseInt(str.substring(10, 12));
					id1 = Integer.parseInt(str.substring(13, 15));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				maps.get(id0 - 1).add(id1, id -> new Stage(id, vf, 1));
			}

			VFile st = VFile.get("./org/stage/CH/stage/");
			for (VFile vf : st.list()) {
				String str = vf.getName();
				int id0;
				try {
					id0 = Integer.parseInt(str.substring(5, 7));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				maps.get(9).add(id0, id -> new Stage(id, vf, 2));
			}
			maps.get(9).stars = new int[] { 100, 150, 400 };

			VFile sta = VFile.get(akuOutbreak+"StageDM/");
			for(VFile vf : sta.list()) {
				String str = vf.getName();
				int id0;
				try {
					id0 = Integer.parseInt(str.substring(11, 13));

					maps.get(14).add(id0, id -> new Stage(id, vf, 0));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private DefMapColc(String st, int ID, List<VFile> stage, VFile map) {
			name = st;
			id = ID;
			UserProfile.getRegister(REG_MAPCOLC).put(Data.hex(id), this);
			for (VFile m : map.list()) {
				String str = m.getName();
				int len = str.length();
				int id;
				try {
					id = Integer.parseInt(str.substring(len - 7, len - 4));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				add(id, ind -> new StageMap(ind, m.getData()));
			}

			for (VFile s : stage) {
				String str = s.getName();
				int id0, id1;

				String[] segments = str.replaceAll("stage[A-Z]+", "").replace(".csv", "").split("_");

				try {
					id0 = Integer.parseInt(segments[0]);
					id1 = Integer.parseInt(segments[1]);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				StageMap sm = maps.get(id0);
				sm.add(id1, id -> new Stage(id, s, 0));
			}
		}

		@Override
		public String getSID() {
			return Data.hex(id);
		}

		@Override
		public String toString() {
			String desp = MultiLangCont.get(this);
			if (desp != null && !desp.isEmpty())
				return desp + " (" + maps.size() + ")";
			return name + " (" + maps.size() + ")";
		}

		private static void updateTreasureData(BattlePreset.ActivatedTreasure treasure, BattlePreset preset) {
			switch (treasure) {
				case EOC1:
				case EOC2:
				case EOC3:
					preset.trea[T_WORK] += 100;
					preset.trea[T_WALT] += 100;
					preset.trea[T_RES]  += 100;
					preset.trea[T_XP1]  += 100;
					preset.trea[T_BASE] += 100;
					preset.trea[T_ACC]  += 100;
					preset.trea[T_DEF]  += 100;
					preset.trea[T_ATK]  += 100;
					preset.trea[T_CATK] += 100;
					preset.trea[T_RECH] += 100;
					break;
				case BASE:
					preset.baseHealthBoost = true;
					break;
				case ITF1:
				case ITF2:
				case ITF3:
					preset.alien 		  += 200;
					preset.trea[T_BASE]   += 100;
					preset.trea[T_RECH]   += 100;
					preset.trea[T_CATK]   += 100;
					preset.fruit[T_BLACK] += 100;
					preset.fruit[T_RED]   += 100;
					preset.fruit[T_FLOAT] += 100;
					preset.fruit[T_ANGEL] += 100;
					break;
				case COTC1:
				case COTC2:
				case COTC3:
					preset.star            += 500;
					preset.fruit[T_METAL]  += 100;
					preset.fruit[T_ZOMBIE] += 100;
					preset.fruit[T_ALIEN]  += 100;
					preset.trea[T_XP2]     += 100;
					preset.gods[treasure.ordinal() - BattlePreset.ActivatedTreasure.COTC1.ordinal()] += 100;
					break;
				default:
					System.out.printf("W/MapColc::read - Unknown Treasure ID %s found\n", treasure);
			}

			if (treasure != BattlePreset.ActivatedTreasure.BASE) {
				preset.activatedTreasures.add(treasure);
			}
		}
	}

	@JsonClass
	public static class PackMapColc extends MapColc {

		public final UserPack pack;
		@JsonField(generic = CustomStageInfo.class, defval = "isEmpty")
		public ArrayList<CustomStageInfo> si = new ArrayList<>();

		public PackMapColc(UserPack pack) {
			this.pack = pack;
			UserProfile.getRegister(REG_MAPCOLC).put(pack.getSID(), this);
		}

		@Override
		public String getSID() {
			return pack.getSID();
		}

		@Override
		public String toString() {
			String str = pack.desc.names.toString();
			if (str.isEmpty())
				return pack.desc.id;
			return str;
		}

		public boolean isEmpty() {
			return maps.isEmpty();
		}

		@Override
		public SaveData getSave(boolean force) {
			return force ? pack.save : pack.getSave();
		}

		@JsonDecoder.OnInjected
		public void onInjected() {//For useless leftovers from old packs
			si.removeIf(ci -> ci.st == null || ci.st.info != ci || ci.useless());
			si.sort((sti, ssti) -> {
				StageMap map = sti.st.getCont(), omap = ssti.st.getCont();
				if (map == omap)
					return sti.st.compareTo(ssti.st);
				if (map.unlockReq.contains(omap))
					return -1;
				else if (omap.unlockReq.contains(map))
					return 1;
				return 0;
			});
		}
	}

	public static class StItr implements Iterator<Stage>, Iterable<Stage> {

		private Iterator<MapColc> imc;
		private MapColc mc;
		private int ism, is;

		protected StItr() {
			imc = UserProfile.getRegister(REG_MAPCOLC, MapColc.class).values().iterator();
			mc = imc.next();
			ism = is = 0;
			validate();
		}

		@Override
		public boolean hasNext() {
			return imc != null;
		}

		@NotNull
		@Override
		public Iterator<Stage> iterator() {
			return this;
		}

		@Override
		public Stage next() {
			Stage ans = mc.maps.get(ism).list.get(is);
			is++;
			validate();
			return ans;
		}

		private void validate() {
			while (is >= mc.maps.get(ism).list.size()) {
				is = 0;
				ism++;
				while (ism >= mc.maps.size()) {
					ism = 0;

					if (!imc.hasNext()) {
						imc = null;
						return;
					}
					mc = imc.next();
				}
			}
		}
	}

	public static class ClipMapColc extends MapColc {

		protected ClipMapColc() {
			add(0, StageMap::new);
		}

		@Override
		public String getSID() {
			return "clipboard";
		}

		@Override
		public String toString() {
			return getSID();
		}

	}

	private static final String REG_MAPCOLC = "MapColc";

	@StaticPermitted
	private static final String[] strs = new String[] { "rc", "ec", "wc", "sc" };

	@ContGetter
	public static MapColc get(String id) {
		if(id.equals("clipboard"))
			return Stage.CLIPMC;

		return UserProfile.getRegister(REG_MAPCOLC, MapColc.class).get(id);
	}

	public static Iterable<Stage> getAllStage() {
		return new StItr();
	}

	public static Collection<MapColc> values() {
		return UserProfile.getRegister(REG_MAPCOLC, MapColc.class).values();
	}

	@JsonField
	public FixIndexMap<StageMap> maps = new FixIndexMap<>(StageMap.class);

	@Override
	public FixIndexMap<StageMap> getFIM() {
		return maps;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <R> R getList(Class cls, Reductor<R, FixIndexMap> func, R def) {
		return func.reduce(def, maps);
	}

	public SaveData getSave(boolean force) {
		return null;
	}

	public int getStageCount() {
		int stageTot = 0;
		for (StageMap smaps : maps)
			stageTot += smaps.list.size();
		return stageTot;
	}
}