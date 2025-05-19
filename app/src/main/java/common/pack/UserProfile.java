package common.pack;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import common.CommonStatic;
import common.io.PackLoader;
import common.io.PackLoader.ZipDesc;
import common.io.assets.Admin.StaticPermitted;
import common.io.assets.AssetLoader;
import common.io.json.JsonDecoder;
import common.pack.Context.ErrType;
import common.pack.PackData.DefPack;
import common.pack.PackData.PackDesc;
import common.pack.PackData.UserPack;
import common.pack.Source.Workspace;
import common.pack.Source.ZipSource;
import common.util.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class UserProfile {

	private static final String REG_POOL = "_pools";
	private static final String REG_STATIC = "_statics";
	protected static final String CURRENT_PACK = "_current_pack";

	@StaticPermitted(StaticPermitted.Type.ENV)
	private static UserProfile profile = null;

	/**
	 * get all available items for a pack, except castle
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> List<T> getAll(String pack, Class<T> cls) {
		List<PackData> list = new ArrayList<>();
		list.add(profile().def);
		if (pack != null && !pack.equals(Identifier.DEF)) {
			UserPack userpack = profile().packmap.get(pack);
			list.add(userpack);
			for (String dep : userpack.desc.dependency) {
				PackData p = getPack(dep);

				if(p != null)
					list.add(p);
			}
		}
		List ans = new ArrayList<>();
		for (PackData data : list) {
			if(data == null)
				continue;

			data.getList(cls, (r, l) -> ans.addAll(l.getList()), null);
		}
		return ans;
	}

	/**
	 * get all packs, including default pack
	 */
	public static Collection<PackData> getAllPacks() {
		List<PackData> ans = new ArrayList<>();
		ans.add(getBCData());
		ans.addAll(getUserPacks());
		return ans;
	}

	public static DefPack getBCData() {
		return profile().def;
	}

	/**
	 * get a PackData from a String
	 */
	public static PackData getPack(String str) {
		if (str.equals(Identifier.DEF))
			return getBCData();
		return getUserPack(str);
	}

	public static boolean isOlderPack(UserPack pack, String ver) {
		int thisVersion = Data.getVer(ver);
		int thatVersion = Data.getVer(pack.desc.BCU_VERSION);

		return thatVersion < thisVersion;
	}

	/**
	 * get a set registered in the Registrar
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Set<T> getPool(String id) {
		Map<String, Set> pool = getRegister(REG_POOL);
		return (Set<T>) pool.computeIfAbsent(id, k -> new LinkedHashSet<>());
	}

	/**
	 * get a map registered in the Registrar
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> getRegister(String id) {
		Map<String, T> ans = (Map<String, T>) profile().registers.get(id);
		if (ans == null)
			profile().registers.put(id, ans = new LinkedHashMap<>());
		return ans;
	}

	/**
	 * get a map registered in the Registrar
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> getRegister(String id, Class<T> cls) {
		Map<String, T> ans = (Map<String, T>) profile().registers.get(id);
		if (ans == null)
			profile().registers.put(id, ans = new LinkedHashMap<>());
		return ans;
	}

	/**
	 * get a variable registered in the Registrar
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getStatic(String id, Supplier<T> def) {
		Map<String, Object> pool = getRegister(REG_STATIC);
		T ans = (T) pool.get(id);
		if (ans == null)
			pool.put(id, ans = def.get());
		return ans;
	}

	/**
	 * get a UserPack from a String
	 */
	public static UserPack getUserPack(String str) {
		UserProfile profile = profile();
		if (profile.pending != null && profile.pending.containsKey(str))
			return profile.pending.get(str);
		if (profile.packmap.containsKey(str))
			return profile.packmap.get(str);
		return profile.skipped.get(str);
	}

	/**
	 * get all UserPack
	 */
	public static Collection<UserPack> getUserPacks() {
		return profile().packmap.values();
	}

	public static void loadPacks(boolean loadWorkspace) {
		UserProfile profile = profile();
		if (profile.pending == null)
			profile.pending = new HashMap<>();

		File packs = CommonStatic.ctx.getAuxFile("./packs");
		File workspace = CommonStatic.ctx.getWorkspaceFile(".");
		if (packs.exists()) {
			for (File f : packs.listFiles())
				if (f.getName().endsWith(".pack.bcuzip") || f.getName().endsWith(".userpack")) {
					UserPack pack = CommonStatic.ctx.noticeErr(() -> readZipPack(f), ErrType.WARN,
							"failed to load external pack " + f, () -> setStatic(CURRENT_PACK, null));

					if (pack != null) {
						UserPack p = (CommonStatic.getConfig().skipLoad.contains(pack.desc.id) ? profile.skipped : profile.pending).put(pack.desc.id, pack);
						if (p != null)
							CommonStatic.ctx.printErr(ErrType.WARN, ((ZipSource) p.source).getPackFile().getName()
									+ " has same ID with " + ((ZipSource) pack.source).getPackFile().getName());
					}
				}
		} else
			packs.mkdir();

		if (loadWorkspace && workspace.exists())
			for (File f : workspace.listFiles())
				if (f.isDirectory()) {
					File main = CommonStatic.ctx.getWorkspaceFile("./" + f.getName() + "/pack.json");
					if (!main.exists() || main.length() == 0)
						continue;
					File auto = CommonStatic.ctx.getWorkspaceFile("./_autosave/pack_" + f.getName() + ".json");
					File FF;
					if (auto.exists()) {
						if (CommonStatic.ctx.confirm("Autosave found for " + f.getName() + ". Load this autosave?"))
							FF = auto;
						else {
							FF = main;
							if (CommonStatic.ctx.confirmDelete(auto))
								try {
									Context.delete(auto);
								} catch (Exception e) {
									CommonStatic.ctx.printErr(ErrType.WARN, "Failed to delete " + auto.getName());
								}
						}
					} else
						FF = main;

					UserPack pack = CommonStatic.ctx.noticeErr(() -> readJsonPack(FF), ErrType.WARN,
							"failed to load workspace pack " + f);
					if (pack != null)
						(CommonStatic.getConfig().skipLoad.contains(pack.desc.id) ? profile.skipped : profile.pending).put(pack.desc.id, pack);
				}
		Set<UserPack> queue = new HashSet<>(profile.pending.values());
		profile.df = 0;
		while (queue.removeIf(profile::add));

		profile.pending = null;
		profile.packlist.addAll(profile.failed);
		CommonStatic.getConfig().excludeCombo.removeIf(k -> !(profile.packmap.containsKey(k) || profile.skipped.containsKey(k)) || profile.packmap.get(k).combos.isEmpty());
		CommonStatic.getConfig().skipLoad.removeIf(k -> !(profile.packmap.containsKey(k) || profile.skipped.containsKey(k)));

		for (PackData.UserPack pk : queue)
			checkMissingParents(pk);
		loadSaveData();
	}

	public static void reloadExternalPacks() {
		UserProfile profile = profile();
		profile.packlist.removeIf(p -> {
			if(!p.editable) {
				p.unregister();
				profile.packmap.remove(p.desc.id);
			}
			return !p.editable;
		});
		profile.skipped.values().removeIf(p -> !p.editable);
		profile.failed.removeIf(p -> !p.editable);
		loadPacks(false);
	}

	public static UserPack addExternalPack(File f) {
		if (!f.getName().endsWith(".pack.bcuzip") && !f.getName().endsWith(".userpack"))
			return null;
		UserPack pack = CommonStatic.ctx.noticeErr(() -> readZipPack(f), ErrType.WARN,
				"failed to load external pack " + f, () -> setStatic(CURRENT_PACK, null));
		if (pack == null)
			return null;
		UserPack p = getUserPack(pack.desc.id);
		if (p != null) {
			CommonStatic.ctx.printErr(ErrType.WARN, ((ZipSource) p.source).getPackFile().getName()
					+ " has same ID with " + ((ZipSource) pack.source).getPackFile().getName());
			return null;
		}
		if (!profile().add(pack)) {
			checkMissingParents(pack);
			return null;
		}
		return pack;
	}
	public static void loadPacks(List<UserPack> packs) {
		if (profile.pending == null)
			profile.pending = new HashMap<>();
		for (UserPack p : packs)
			profile.pending.put(p.desc.id, p);
		Set<UserPack> queue = new HashSet<>(profile.pending.values());
		profile.df = 0;
		while (queue.removeIf(profile::add));

		packs.removeAll(queue);
		profile.skipped.values().removeAll(packs);

		profile.pending = null;
		for (UserPack p : queue)
			checkMissingParents(p);
	}

	public static UserPack initJsonPack(String id) throws Exception {
		File f = CommonStatic.ctx.getWorkspaceFile("./" + id + "/pack.json");
		File folder = f.getParentFile();
		if (folder.exists()) {
			if (!CommonStatic.ctx.confirmDelete(f))
				return null;
			Context.delete(f);
		}
		folder.mkdirs();
		Context.check(f.createNewFile(), "create", f);
		UserPack p = new UserPack(id);
		profile().packmap.put(id, p);
		return p;
	}

	public static void checkMissingParents(UserPack pk) {
		SortedPackSet<String> deps = new SortedPackSet<>(pk.preGetDependencies());
		deps.removeIf(profile.packmap::containsKey);
		if (!deps.isEmpty())
			CommonStatic.ctx.printErr(ErrType.WARN, pk.desc.names + " (" + pk.desc.id + ")"
					+ " requires parent packs you don't have, which are: " + deps);
	}

	public static UserProfile profile() {
		if (profile == null) {
			profile = new UserProfile();
			if (CommonStatic.ctx != null)
				CommonStatic.ctx.initProfile();
		}
		return profile;
	}

	public static UserPack readJsonPack(File f) throws Exception {
		File folder = f.getName().length() == 9 ? f.getParentFile() : new File(f.getParentFile().getPath() + "/" + f.getName().substring(5, f.getName().indexOf("."))); //pack.json : pack_ID.json
		Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
		JsonElement elem = JsonParser.parseReader(r);
		r.close();
		PackDesc desc = JsonDecoder.decode(elem.getAsJsonObject().get("desc"), PackDesc.class);

		if (Data.getVer(desc.BCU_VERSION) > Data.getVer(AssetLoader.CORE_VER)) {
			CommonStatic.ctx.printErr(ErrType.WARN, "Pack " + f.getName() + " core version (" + desc.BCU_VERSION
					+ ") is higher than BCU core version (" + AssetLoader.CORE_VER + ")");
		}

		return new UserPack(new Workspace(folder.getName()), desc, elem);
	}

	public static UserPack readBackupPack(String content, String id) {
		JsonElement elem = JsonParser.parseString(content);
		PackDesc desc = JsonDecoder.decode(elem.getAsJsonObject().get("desc"), PackDesc.class);

		return new UserPack(new Workspace(id), desc, elem);
	}

	public static UserPack readZipPack(File f) throws Exception {
		ZipDesc zip = PackLoader.readPack(f);

		if (Data.getVer(zip.desc.BCU_VERSION) > Data.getVer(AssetLoader.CORE_VER)) {
			CommonStatic.ctx.printErr(ErrType.WARN, "Pack " + f.getName() + " core version (" + zip.desc.BCU_VERSION
					+ ") is higher than BCU core version (" + AssetLoader.CORE_VER + ")");
		}

		Reader r = new InputStreamReader(zip.readFile("./pack.json"), StandardCharsets.UTF_8);

		ZipSource zs = new ZipSource(zip);

		setStatic(CURRENT_PACK, zs);

		JsonElement elem = JsonParser.parseReader(r);

		UserPack data = new UserPack(zs, zip.desc, elem);
		r.close();

		setStatic(CURRENT_PACK, null);

		return data;
	}

	public static void loadSaveData() {
		File datas = CommonStatic.ctx.getAuxFile("./saves");
		if (datas.exists()) {
			for (File f : datas.listFiles())
				if (f.getName().endsWith(".packsave"))
					try {
						loadData(f);
					} catch (Exception e) {
						CommonStatic.ctx.printErr(ErrType.ERROR, "Failed to load " + f.getName());
						e.printStackTrace();
					}
		} else {
			datas.mkdir();
		}
	}

	public static void loadData(File f) throws Exception {
		InputStreamReader isr = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
		JsonElement elem = JsonParser.parseReader(isr);
		String id = elem.getAsJsonObject().get("pack").getAsString();
		UserPack pk = UserProfile.getUserPack(id);
		if (pk == null || profile.skipped.containsKey(id)) {
			if (!profile.skipped.containsKey(id))
				CommonStatic.ctx.printErr(ErrType.WARN, "Save data found for " + id + ", but said pack isn't found. File: " + f.getName());
			isr.close();
			return;
		}
		try {
			pk.save = JsonDecoder.inject(elem, SaveData.class, pk.save);
		} catch (Exception e) {
			CommonStatic.ctx.printErr(ErrType.ERROR, "Failed to load data for " + pk.desc.names);
			e.printStackTrace();
		}

		isr.close();
	}

	public static void setStatic(String id, Object val) {
		getRegister(REG_STATIC).put(id, val);
	}

	public static void unloadPack(UserPack pack) {
		pack.unregister();

		profile().packmap.remove(pack.getSID());
		profile().packlist.remove(pack);
		profile().failed.remove(pack);
	}

	public static void unloadAllUserPacks() {
		for (UserPack pack : getUserPacks())
			pack.unregister();

		profile().packmap.clear();
		profile().packlist.clear();
		profile().failed.clear();
	}

	/**
	 * Unregister object from registers
	 *
	 * @param id ID of registered object
	 */
	public static void unregister(String id) {
		profile().registers.remove(id);
	}

	public final DefPack def = new DefPack();
	public final Map<String, UserPack> packmap = new HashMap<>();

	public final Set<UserPack> packlist = new HashSet<>();

	public final Set<UserPack> failed = new HashSet<>();

	private final Map<String, Map<String, ?>> registers = new HashMap<>();

	public Map<String, UserPack> pending = new HashMap<>();
	public Map<String, UserPack> skipped = new HashMap<>();
	public int df = 0;

	private UserProfile() {
	}

	/**
	 * return true if the pack is attempted to load and should be removed from the
	 * loading queue
	 */
	private boolean add(UserPack pack) {
		packlist.add(pack);
		SortedPackSet<String> deps = pack.editable ? pack.desc.dependency : pack.preGetDependencies();
		if (!canAdd(deps))
			return false;
		double siz = pending == null ? 0.5 : 1f * df++ / pending.size();
		CommonStatic.ctx.loadProg(siz, "Reading " + (pack.desc.names.toString().isEmpty() ? pack.desc.id : pack.desc.names.toString()) + " data...");
		if (CommonStatic.ctx.noticeErr(pack::load, ErrType.WARN, "failed to load pack " + pack.desc, () -> setStatic(CURRENT_PACK, null))) {
			packmap.put(pack.desc.id, pack);
		} else
			failed.add(pack);

		return true;
	}

	private boolean canAdd(SortedPackSet<String> deps) {
		for (String dep : deps)
			if (!packmap.containsKey(dep))
				return false;
		return true;
	}

}