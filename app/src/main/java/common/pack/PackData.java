package common.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.CommonStatic;
import common.battle.Treasure;
import common.battle.data.CustomEntity;
import common.battle.data.Orb;
import common.battle.data.PCoin;
import common.io.assets.AssetLoader;
import common.io.json.Dependency;
import common.io.json.FieldOrder.Order;
import common.io.json.JsonClass;
import common.io.json.JsonClass.JCConstructor;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonClass.RType;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.io.json.JsonField.GenType;
import common.pack.FixIndexList.FixIndexMap;
import common.pack.Source.Workspace;
import common.system.VImg;
import common.system.files.FDFile;
import common.system.files.VFile;
import common.system.files.VFileRoot;
import common.util.Data;
import common.util.Res;
import common.util.anim.AnimCE;
import common.util.anim.AnimUD;
import common.util.lang.MultiLangData;
import common.util.pack.*;
import common.util.pack.bgeffect.BackgroundEffect;
import common.util.stage.*;
import common.util.stage.CastleList.PackCasList;
import common.util.stage.MapColc.DefMapColc;
import common.util.stage.MapColc.PackMapColc;
import common.util.unit.*;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

@JsonClass(read = RType.FILL, noTag = NoTag.LOAD)
public abstract class PackData implements IndexContainer {

	public static class DefPack extends PackData {

		public VFileRoot root = new VFileRoot(".");

		protected DefPack() {

		}

		@Override
		public String getSID() {
			return Identifier.DEF;
		}

		public void load(Consumer<String> progress, Consumer<Double> bar) {
			progress.accept("Loading basic Images");
			Res.readData();
			progress.accept("Loading traits");
			Trait.addBCTraits();
			progress.accept("Loading cannon Data");
			Treasure.readCannonCurveData();
			progress.accept("Loading enemies");
			loadEnemies(bar);
			progress.accept("Loading units");
			loadUnits(bar);
			progress.accept("Loading auxiliary data");
			Combo.readFile();
			PCoin.read();
			progress.accept("Loading effects");
			EffAnim.read();
			progress.accept("Loading backgrounds");
			Background.read(bar);
			BackgroundEffect.read();
			progress.accept("Loading cat castles");
			NyCastle.read();
			progress.accept("Loading souls");
			loadSoul();
			progress.accept("Loading stages");
			DefMapColc.read(bar);
			Enemy.regType();
			RandStage.read();
			loadCharaGroup();
			loadLimit();
			CastleImg.loadBossSpawns();
			progress.accept("Loading orbs");
			Orb.read();
			progress.accept("Loading musics");
			loadMusic();
			progress.accept("Processing data");
			this.traits.reset();
			this.enemies.reset();
			this.randEnemies.reset();
			this.units.reset();
			this.randUnits.reset();
			this.unitLevels.reset();
			this.groups.reset();
			this.lvrs.reset();
			this.bgs.reset();
			this.bgEffects.reset();
			this.musics.reset();
			this.combos.reset();
			for (CastleList cl : CastleList.map().values())
				cl.reset();
			for (MapColc mc : MapColc.values()) {
				mc.maps.reset();
				for (StageMap sm : mc.maps)
					sm.list.reset();
			}
		}

		@Override
		public String toString() {
			return "Default BC Data";
		}

		private void loadCharaGroup() {
			Queue<String> qs = VFile.readLine("./org/data/Charagroup.csv");
			qs.poll();
			for (String str : qs) {
				String[] strs = str.split(",");
				int id = CommonStatic.parseIntN(strs[0]);
				int type = CommonStatic.parseIntN(strs[2]);
				@SuppressWarnings("unchecked")
				Identifier<AbUnit>[] units = new Identifier[strs.length - 3];
				for (int i = 3; i < strs.length; i++)
					units[i - 3] = Identifier.parseInt(CommonStatic.parseIntN(strs[i]), Unit.class);
				groups.set(id, new CharaGroup(id, type, units));
			}
		}

		private void loadEnemies(Consumer<Double> bar) {
			int i = 0;
			Collection<VFile> list = VFile.get("./org/enemy/").list();
			Queue<String> qs = VFile.readLine("./org/data/t_unit.csv");
			qs.poll();
			qs.poll();

			Queue<String> fs = CommonStatic.getConfig().stat && VFile.get("./org/data/t_force.csv") != null ? VFile.readLine("./org/data/t_force.csv") : null;
			for (VFile p : list) {
				boolean force = fs != null && fs.peek() != null && fs.peek().endsWith(Data.trio(i));
				if (force)
					qs.poll();
				String[] strs = (force ? fs : qs).poll().split("//")[0].trim().split(",");
				int[] ints = new int[strs.length];
				for (int j = 0; j < strs.length; j++)
					ints[j] = Integer.parseInt(strs[j]);

				enemies.add(new Enemy(p, ints));
				bar.accept(1.0 * (++i) / list.size());
			}
		}

		private void loadLimit() {
			Queue<String> qs = VFile.readLine("./org/data/Stage_option.csv");
			qs.poll();
			for (String str : qs)
				new Limit.DefLimit(str.split(","));
		}

		private void loadMusic() {
			File dict = CommonStatic.ctx.getAssetFile("./music/");
			if (!dict.exists())
				return;
			for (File f : dict.listFiles()) {
				String str = f.getName();
				if (str.length() < 7 || !str.endsWith(".ogg"))
					continue;
				int id = CommonStatic.parseIntN(str.substring(0, str.indexOf(".")));
				if (id == -1)
					continue;
				musics.set(id, new Music(Identifier.parseInt(id, Music.class), new FDFile(f)));
			}
		}

		private void loadSoul() {
			String pre = "./org/battle/soul/";
			int soulNumber = 0;

			VFile soulFolder = VFile.get(pre);
			if(soulFolder == null)
				return;

			for(VFile vf : soulFolder.list())
				if(vf != null && vf.getData() == null && vf.name.matches("\\d{3}"))
					soulNumber = Math.max(soulNumber, CommonStatic.safeParseInt(vf.name));

			String mid = "/battle_";
			for (int i = 0; i <= soulNumber; i++) {
				String path = pre + Data.trio(i) + mid;
				AnimUD anim = new AnimUD(path, "soul_" + Data.trio(i), null, null);
				Identifier<Soul> identifier = new Identifier<>(Identifier.DEF, Soul.class, i);
				souls.add(new Soul(identifier, anim));
			}
			String dem = "demonsoul"; // TODO identify if anim is enemy or not in demon soul name in effect page
			CommonStatic.getBCAssets().demonSouls.add(new DemonSoul(0, new AnimUD(pre + dem + mid, "demonsoul_" + Data.duo(0), null, null), true));
			byte u = (byte)(VFile.get(pre + dem + mid + "demonsoul_01") == null ? 0 : 1);
			CommonStatic.getBCAssets().demonSouls.add(new DemonSoul(0, new AnimUD(pre + dem + mid, "demonsoul_" + Data.duo(u), null, null), u == 1));
		}

		private void loadUnits(Consumer<Double> bar) {
			int x = 0;
			Collection<VFile> list = VFile.get("./org/unit").list();
			Queue<String> qs = VFile.readLine("./org/data/unitbuy.csv");

			Queue<String> qt = VFile.readLine("./org/data/unitlevel.csv");
			FixIndexList<UnitLevel> l = unitLevels;
			int lastId = 0;
			for (VFile p : list) {
				int id = CommonStatic.parseIntN(p.getName());
				while (lastId++ < id) {//Passes through missing slots
					qs.poll();
					qt.poll();
					units.add(new Unit(lastId - 1));//Create a placeholder, because sure
				}
				String[] strs = qs.poll().split(",");

				Unit u = new Unit(p, new int[]{Integer.parseInt(strs[strs.length - 2]), Integer.parseInt(strs[strs.length - 1])});
				u.rarity = Integer.parseInt(strs[13]);
				u.max = Integer.parseInt(strs[50]);
				u.maxp = Integer.parseInt(strs[51]);
				u.info.fillBuy(strs);

				int tf =  Integer.parseInt(strs[20]);
				if (tf != -1)
					u.info.tfLevel = tf;

				tf = Integer.parseInt(strs[25]);
				if (tf != -1)
					u.info.tfLevel = tf;
				else if (u.info.tfLevel == -1)
					u.info.tfLevel = 20;

				//Handle zero form
				tf = Integer.parseInt(strs[26]);
				if (tf != -1)
					u.info.zeroLevel = tf;

				strs = qt.poll().split(",");
				int[] lv = new int[20];
				for (int i = 0; i < 20; i++)
					lv[i] = Integer.parseInt(strs[i]);
				UnitLevel ul = new UnitLevel(lv);
				if (!l.contains(ul)) {
					ul.id = new Identifier<>(Identifier.DEF, UnitLevel.class, l.size());
					l.add(ul);
				}
				int ind = l.indexOf(ul);
				u.lv = l.get(ind);
				l.get(ind).units.add(u);

				units.add(u);
				bar.accept(1.0 * (++x) / list.size());
			}
			CommonStatic.getBCAssets().defLv = l.get(2);
		}

	}

	@JsonClass(noTag = NoTag.LOAD)
	public static class PackDesc {
		public String BCU_VERSION;
		@JsonField(backCompat = JsonField.CompatType.FORK)
		public int FORK_VERSION = 0; // The same as BCU_VERSION, but for this fork exclusively
		public String id;
		public String author;

		@JsonField(generic = MultiLangData.class, gen = GenType.FILL, defval = "empty")
		public final MultiLangData names = new MultiLangData();
		@JsonField(generic = MultiLangData.class, gen = GenType.FILL, defval = "empty")
		public final MultiLangData info = new MultiLangData();

		public String creationDate;
		public String exportDate;
		@JsonField(defval = "1")
		public double version = 1.0;

		public boolean allowAnim = false;
		public byte[] parentPassword;
		@JsonField(generic = String.class, defval = "isEmpty")
		public SortedPackSet<String> dependency = new SortedPackSet<>();

		@JCConstructor
		@Deprecated
		public PackDesc() {
		}

		public PackDesc(String id) {
			BCU_VERSION = AssetLoader.CORE_VER;
			FORK_VERSION = AssetLoader.FORK_VER; //0 by default to differ Fork packs and non-fork packs
			this.id = id;
			DateFormat df = new SimpleDateFormat("MM dd yyyy HH:mm:ss");
			creationDate = df.format(new Date());
		}

		/**
		 * Null-safe way to get a string off the pack, used for sorting
		 * Currently only uses auto since there's no other unsafe string
		 * @return The requested string, or an empty string if it's null
		 */
		public String getAuthor() {
			if (author != null)
				return author;
			return "";
		}

		public long getTimestamp(String val) {
			String time;
			if (val.equals("cdate"))
				time = creationDate;
			else
				time = exportDate;
			if (time != null) {
				DateFormat df = new SimpleDateFormat("MM dd yyyy HH:mm:ss");
				try {
					Date d = df.parse(time);
					return d.getTime();
				} catch (Exception e) {
					System.out.println("Error parsing " + time + " in pack " + this);
					e.printStackTrace();
					return 0;
				}
			}
			return 0;
		}

		public int[] allVersionData() {
			return new int[]{Data.getVer(BCU_VERSION), FORK_VERSION};
		}

		@Override
		public String toString() {
			return names + " - " + id;
		}

		@Override
		public PackDesc clone() {
			PackDesc desc = new PackDesc(id);

			desc.author = author;
			desc.names.put(names.toString());
			desc.info.put(info.toString());
			desc.allowAnim = allowAnim;
			desc.parentPassword = parentPassword == null ? null : parentPassword.clone();

			return desc;
		}

		@JsonDecoder.OnInjected
		public void onInjected(JsonObject jobj) {
			if (jobj.has("name") && !jobj.get("name").isJsonNull())
				names.put(jobj.get("name").getAsString());
		}
	}

	@JsonClass(read = RType.FILL)
	public static class UserPack extends PackData implements Comparable<UserPack> {

		@JsonField
		@Order(0)
		public final PackDesc desc;

		@JsonField(gen = GenType.FILL, defval = "isEmpty")
		@Order(1)
		public PackCasList castles;

		@JsonField(gen = GenType.FILL, defval = "isEmpty")
		@Order(2)
		public PackMapColc mc;

		public Source source;

		public boolean editable;
		public boolean loaded = false;

		private JsonElement elem;
		public VImg icon, banner;
		public SaveData save;
		@JsonField(generic = { Unit.class, Integer.class }, alias = Identifier.class, decodeLast = true, backCompat = JsonField.CompatType.FORK, defval = "isEmpty")
		public final TreeMap<AbUnit, Integer> defULK = new TreeMap<>();//Units unlocked from the get-go, for progression
		@JsonField(generic = String.class, backCompat = JsonField.CompatType.FORK, defval = "isEmpty")
		public final SortedPackSet<String> syncPar = new SortedPackSet<>();

		public UserPack(Source s, PackDesc desc, JsonElement elem) {
			this.desc = desc;
			this.elem = elem;
			source = s;
			editable = source instanceof Workspace;
			castles = new PackCasList(this);
			mc = new PackMapColc(this);
		}

		/**
		 * for generating new pack only
		 */
		public UserPack(String id) {
			desc = new PackDesc(id);
			source = new Workspace(id);
			castles = new PackCasList(this);
			mc = new PackMapColc(this);
			editable = true;
			loaded = true;
		}

		public SaveData getSave() {
			return CommonStatic.getConfig().prog ? save : null;
		}

		public void delete() {
			unregister();
			UserProfile.profile().packmap.remove(getSID());
			source.delete();
		}

		public List<String> foreignList(String id) {
			List<String> list = new ArrayList<>();
			Dependency dep = Dependency.collect(this);
			if (dep.getPacks().contains(id))
				for (Entry<Class<?>, Map<String, Set<Identifier<?>>>> ent : dep.getMap().entrySet()) {
					Map<String, Set<Identifier<?>>> map = ent.getValue();
					if (map.containsKey(id) && map.get(id).size() > 0) {
						list.add(ent.getKey().getSimpleName() + ":");
						for (Identifier<?> identifier : map.get(id))
							list.add("\t" + identifier.get().toString());
					}
				}
			return list;
		}

		public List<Replay> getReplays() {
			List<Replay> ans = new ArrayList<>();
			for (StageMap sm : mc.maps)
				for (Stage st : sm.list)
					ans.addAll(st.recd);
			return ans;
		}

		@Override
		public String getSID() {
			return desc.id;
		}

		public void loadMusics() {
			String[] path = source.listFile("./musics");

			HashMap<Integer, Music> musMap = new HashMap<>();
			for (Music m : musics) {
				if (m.id == null)
					continue;
				musMap.put(m.id.id, m);
			}
			musics.clear();
			if (path != null)
				for (String str : path)
					if (Music.valid(str)) {
						int ind = Integer.parseInt(str.substring(0, 3));
						add(musics, ind, id -> new Music(id, source.getFileData("./musics/" + str), musMap.get(ind)));
					}
			musics.reset();
		}

		public boolean relyOn(String id) {
			Dependency dep = Dependency.collect(this);
			return dep.getPacks().contains(id);
		}

		@Override
		public String toString() {
			return desc.names.toString().isEmpty() ? desc.id : desc.names.toString();
		}

		public void unregister() {
			UserProfile.unregister(getSID());
		}

		public SortedPackSet<String> preGetDependencies() {
			if (!desc.dependency.isEmpty())
				return desc.dependency;
			JsonElement dobj = elem.getAsJsonObject().getAsJsonObject("desc").get("dependency");
			SortedPackSet<String> deps = new SortedPackSet<>();
			if (dobj == null)
				return deps;
			JsonArray jarr = dobj.getAsJsonArray();
			for (int i = 0; i < jarr.size(); i++)
				deps.add(jarr.get(i).getAsString());
			desc.dependency = deps;
			return deps;
		}

		public void load() throws Exception {
			UserProfile.setStatic(UserProfile.CURRENT_PACK, source);
			JsonDecoder.inject(elem, UserPack.class, this);
			JsonDecoder.finishLoading();
			elem = null;
			loaded = true;
			loadMusics();
			UserProfile.setStatic(UserProfile.CURRENT_PACK, null);

			if(source instanceof Source.ZipSource && ((Source.ZipSource) source).zip.desc.parentPassword != null)
				desc.parentPassword = ((Source.ZipSource) source).zip.desc.parentPassword.clone();

			icon = source.readImage("icon");
			banner = source.readImage("banner");
			//Since it succeeded to load all data, update Core version of this workspace pack
			if(editable) {
				desc.BCU_VERSION = AssetLoader.CORE_VER;
				desc.FORK_VERSION = AssetLoader.FORK_VER;
				if (desc.creationDate == null) {
					DateFormat df = new SimpleDateFormat("MM dd yyyy HH:mm:ss");
					desc.creationDate = df.format(new Date());
				}
			}
			if (desc.FORK_VERSION >= 8)
				save = new SaveData(this);
		}

		public void animChanged(AnimCE anim, int del) {
			for (Enemy e : enemies)
				if (e.anim == anim)
					((CustomEntity)e.de).animChanged(del);
		}

		@Override
		public List<Enemy> getEnemies(boolean ignore) {
			if (ignore || getSave() == null)
				return super.getEnemies(true);
			List<Enemy> l = new ArrayList<>();
			for (Enemy e : super.getEnemies(true))
				if (e.filter != 3 && save.encountered(e))
					l.add(e);
			return l;
		}

		@Override
		public int compareTo(UserPack pk) {
			return toString().compareTo(pk.toString());
		}

		@Override
		@SuppressWarnings({ "rawtypes" })
		public <R> R getList(Class cls, Reductor<R, FixIndexMap> func, R def) {
			if (cls != CastleImg.class)
				def = super.getList(cls, func, def);
			else
				def = func.reduce(def, castles);
			return def;
		}
	}

	@ContGetter
	public static PackData getPack(String str) {
		return UserProfile.getPack(str);
	}

	@Order(0)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<Trait> traits = new FixIndexMap<>(Trait.class);
	@Order(1)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<Enemy> enemies = new FixIndexMap<>(Enemy.class);
	@Order(2)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<EneRand> randEnemies = new FixIndexMap<>(EneRand.class);
	@Order(3)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<UnitLevel> unitLevels = new FixIndexMap<>(UnitLevel.class);
	@Order(4)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<Unit> units = new FixIndexMap<>(Unit.class);
	@Order(5)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<UniRand> randUnits = new FixIndexMap<>(UniRand.class);
	@Order(6)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<Soul> souls = new FixIndexMap<>(Soul.class);
	@Order(7)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<Background> bgs = new FixIndexMap<>(Background.class);
	@Order(8)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<BackgroundEffect> bgEffects = new FixIndexMap<>(BackgroundEffect.class);
	@Order(9)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<CharaGroup> groups = new FixIndexMap<>(CharaGroup.class);
	@Order(10)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<LvRestrict> lvrs = new FixIndexMap<>(LvRestrict.class);
	@Order(11)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<Music> musics = new FixIndexMap<>(Music.class);
	@Order(12)
	@JsonField(defval = "isEmpty")
	public final FixIndexMap<Combo> combos = new FixIndexMap<>(Combo.class);

	public List<Enemy> getEnemies(boolean ignore) {
		return enemies.getList();
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public <R> R getList(Class cls, Reductor<R, FixIndexMap> func, R def) {
		if (cls == Trait.class)
			return func.reduce(def, traits);
		else if (UniRand.class.isAssignableFrom(cls))
			def = func.reduce(def, randUnits);
		else if (AbUnit.class.isAssignableFrom(cls))
			def = func.reduce(def, units);
		else if (cls == UnitLevel.class)
			def = func.reduce(def, unitLevels);
		else if (EneRand.class.isAssignableFrom(cls))
			def = func.reduce(def, randEnemies);
		else if (AbEnemy.class.isAssignableFrom(cls))
			def = func.reduce(def, enemies);
		else if (cls == Background.class)
			def = func.reduce(def, bgs);
		else if (cls == BackgroundEffect.class)
			def = func.reduce(def, bgEffects);
		else if (cls == Soul.class)
			def = func.reduce(def, souls);
		else if (cls == Music.class)
			def = func.reduce(def, musics);
		else if (cls == CharaGroup.class)
			def = func.reduce(def, groups);
		else if (cls == LvRestrict.class)
			def = func.reduce(def, lvrs);
		else if (cls == Combo.class)
			def = func.reduce(def, combos);
		return def;
	}
}