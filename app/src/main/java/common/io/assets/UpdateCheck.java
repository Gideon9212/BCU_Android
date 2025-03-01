package common.io.assets;

import com.google.gson.JsonElement;
import common.CommonStatic;
import common.io.WebFileIO;
import common.io.assets.UpdateCheck.UpdateJson.AssetJson;
import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonDecoder;
import common.pack.Context;
import common.pack.Context.ErrType;
import common.pack.UserProfile;
import common.util.Data;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class UpdateCheck {

	@JsonClass(noTag = NoTag.LOAD)
	public static class ContentJson {
		public String name, sha, download_url;
	}

	public static class Downloader {

		public final String[] url;
		public final File target;
		public final File temp;
		public final String desc;
		public final boolean direct;

		public Runnable post;

		public Downloader(File target, File temp, String desc, boolean direct, String... url) {
			this.url = url;
			this.target = target;
			this.temp = temp;
			this.desc = desc;
			this.direct = direct;
		}

		public void run(Consumer<Double> prog) throws Exception {
			if (temp.exists() && !temp.delete()) {
				System.out.println("W/UpdateCheck::Downloader - Failed to delete " +temp.getAbsolutePath());
			}
			boolean success = false;
			for (String u : url) {
				try {
					WebFileIO.download(u, temp, prog, direct);
					success = true;
					break;
				} catch (Exception e) {
					CommonStatic.ctx.noticeErr(e, ErrType.INFO, "failed to download " + u);
				}
			}
			if (!success)
				return;
			if (!target.getParentFile().exists())
				target.getParentFile().mkdirs();
			if (target.exists())
				target.delete();
			temp.renameTo(target);
			if (post != null) {
				post.run();
			}
		}

		@Override
		public String toString() {
			return desc;
		}

	}

	@JsonClass(noTag = NoTag.LOAD)
	public static class UpdateJson {

		@JsonClass(noTag = NoTag.LOAD)
		public static class AssetJson {
			public String id;
			public String ver;
			public String desc;
			public String type;
		}

		public AssetJson[] assets;
		public String[] pc_libs;
		public int music;

		private UpdateJson merge(UpdateJson other) {
			int ol = assets.length;
			assets = Arrays.copyOf(assets, ol + other.assets.length);
			System.arraycopy(other.assets, 0, assets, ol, other.assets.length);
			if (other.pc_libs != null) {
				ol = pc_libs.length;
				pc_libs = Arrays.copyOf(pc_libs, ol + other.pc_libs.length);
				System.arraycopy(other.pc_libs, 0, pc_libs, ol, other.pc_libs.length);
			}
			music = Math.max(music, other.music);
			return this;
		}
	}

	private static final String REG_REQLIB = "required_asset";

	static {
		addRequiredAssets("000001", "000002", "000003", "000004", "000005", "000006", "000007", "000008", "000009",
				"090900", "091000", "091001", "100000", "100002", "100100", "100102", "100103", "100104", "100200",
				"100201", "100203", "100204", "100300", "100303", "100304", "100400", "100401", "100403", "100500",
				"100502", "100503", "100504", "100505", "100506", "100507", "100508", "100509", "100600", "100603",
				"100700", "100701", "100800", "100802", "100803", "100804", "100900", "100902", "100904", "100905",
				"100906", "100907", "101000", "101002", "110000", "110002", "110100", "110101", "110200", "110300",
				"110400", "110403", "110500", "110503", "110504", "110505", "110506", "110600", "110603", "110604",
				"110700", "110703", "110800", "110900", "110903", "111000", "111003", "111005", "120000", "120100",
				"120200", "120203", "120300", "120400", "120500", "120503", "120600", "120700", "130000", "130100",
				"130200", "130300", "fork_essentials"
		);
	}

	public static final String REPO = "Blacksun420/sun-";
	public static final String UPSTREAM = "battlecatsultimate/";
	public static final String URL_UPDATE = "https://raw.githubusercontent.com/battlecatsultimate/bcu-page/master/api/updateInfo.json";
	public static final String URL_UPDATE_R = "https://raw.githubusercontent.com/" + REPO + "bcu-assets/master/assets/updateInfo.json";
	public static final String URL_LIB = "https://github.com/battlecatsultimate/bcu-assets/raw/master/BCU_lib/";
	public static final String URL_MUSIC = "https://github.com/battlecatsultimate/bcu-assets/raw/master/music/";
	public static final String URL_NEW = "https://github.com/battlecatsultimate/bcu-assets/raw/master/assets/";
	public static final String URL_LANG_CHECK = "https://api.github.com/repos/battlecatsultimate/bcu-assets/contents/lang";
	public static final String URL_MUSIC_CHECK = "https://api.github.com/repos/battlecatsultimate/bcu-assets/contents/music";
	public static final String URL_FONT = "https://github.com/battlecatsultimate/bcu-assets/raw/master/fonts/stage_font.otf";

	public static void addRequiredAssets(String... str) {
		Collections.addAll(UserProfile.getPool(REG_REQLIB), str);
	}

	public static List<Downloader> checkAsset(UpdateJson json, String... type) throws Exception {
		Set<String> local = AssetLoader.previewAssets();
		Set<String> req = new HashSet<>(UserProfile.getPool(REG_REQLIB));
		if(local != null)
			req.removeIf(id -> local.contains("asset_" + id));
		if (json == null && !req.isEmpty())
			throw new Exception("internet connection required: missing required libraries: " + req);
		List<Downloader> set = new ArrayList<>();
		if (json == null)
			return set;
		for (AssetJson aj : json.assets) {
			if (Data.getVer(aj.ver) > Data.getVer(AssetLoader.CORE_VER))
				continue;
			if (!aj.type.equals("core") && !contains(type, aj.type))
				continue;
			if (local != null && local.contains("asset_" + aj.id))
				continue;
			String url = URL_NEW + aj.id + ".asset.bcuzip";
			if (aj.id.contains("fork_essentials"))
				url = url.replace(UPSTREAM, REPO);
			File temp = CommonStatic.ctx.getAssetFile("./assets/.asset.bcuzip.temp");
			File target = CommonStatic.ctx.getAssetFile("./assets/" + aj.id + ".asset.bcuzip");
			set.add(new Downloader(target, temp, aj.desc, false, url));
		}
		return set;
	}

	private static boolean contains(String[] arr, String str) {
		for (String s : arr) {
			if (s.equals(str))
				return true;
		}

		return false;
	}

	public static Context.SupExc<List<Downloader>> checkLang(String[] files) {
		Map<String, String> local = CommonStatic.getDataMaps().localLangMap;
		File f = CommonStatic.ctx.getAssetFile("./lang");
		String path = f.getPath() + "/";
		return () -> {
			JsonElement je0 = WebFileIO.directRead(URL_LANG_CHECK);
			ContentJson[] cont = JsonDecoder.decode(je0, ContentJson[].class);
			JsonElement je1 = WebFileIO.directRead(URL_LANG_CHECK.replace(UPSTREAM, REPO));
			ContentJson[] fcont = JsonDecoder.decode(je1, ContentJson[].class);

			Map<String, ContentJson> map = new HashMap<>();
			List<Downloader> list = new ArrayList<>();
			for (ContentJson c : cont)
				map.put(c.name, c);
			for (ContentJson c : fcont)
				map.put(c.name, c);

			for (String str : files) {
				ContentJson cj = map.get(str.replace('/', '-'));
				if (cj == null)
					continue;
				File dst = new File(path + str);
				if (!dst.exists() || !cj.sha.equals(local.get(str))) {
					File tmp = new File(path + ".temp");
					String desc = "download language file " + str;
					Downloader d = new Downloader(dst, tmp, desc, true, cj.download_url);
					list.add(d);
					d.post = () -> local.put(str, cj.sha);
				}
			}
			return list;
		};
	}

	public static Downloader checkFont() { //If more fonts are added, it may need to be a list like the rest. For now it's like this because there is only one font
		File fonts = CommonStatic.ctx.getAssetFile("./fonts/stage_font.otf");

		if (!fonts.exists()) {
			File temp = CommonStatic.ctx.getAssetFile("./fonts/.otf.temp");
			return new Downloader(fonts, temp, "Stage Name Fonts", false, URL_FONT);
		}
		return null;
	}

	public static List<Downloader> checkNewMusic(int count) {
		if (count == -1)
			return null;

		try {
			File music = CommonStatic.ctx.getAssetFile("./music/");
			boolean[] exi = new boolean[count];
			if (music.exists())
				for (File m : music.listFiles())
					if (m.getName().length() == 7 && m.getName().endsWith(".ogg")) {
						Integer id = Data.ignore(() -> Integer.parseInt(m.getName().substring(0, 3)));
						if (id != null)
							exi[id] = id < count && id >= 0;
					}
			List<Downloader> ans = new ArrayList<>();
			for (int i = 0; i < count; i++)
				if (!exi[i]) {
					File target = CommonStatic.ctx.getAssetFile("./music/" + Data.trio(i) + ".ogg");
					File temp = CommonStatic.ctx.getAssetFile("./music/.ogg.temp");
					String url = URL_MUSIC + Data.trio(i) + ".ogg";
					if (i == 34)
						url = url.replace(UPSTREAM, REPO);
					ans.add(new Downloader(target, temp, "music " + Data.trio(i), false, url));
				}
			return ans;
		} catch (Exception e) {
			CommonStatic.ctx.printErr(ErrType.ERROR, "Failed music update check");
			e.printStackTrace();
			return null;
		}
	}

	public static Context.SupExc<List<Downloader>> checkMusic(int count) {
		return () -> {
			if (count == -1)
				return null;

			boolean[] exi = new boolean[count];
			File music = CommonStatic.ctx.getAssetFile("./music/");

			JsonElement je0 = WebFileIO.directRead(URL_MUSIC_CHECK);
			ContentJson[] contents = JsonDecoder.decode(je0, ContentJson[].class);
			JsonElement je1 = WebFileIO.directRead(URL_MUSIC_CHECK.replace(UPSTREAM, REPO));
			ContentJson[] fcont = JsonDecoder.decode(je1, ContentJson[].class);

			Map<String, ContentJson> map = new HashMap<>();
			for(ContentJson content : contents)
				map.put(content.name, content);
			for(ContentJson content : fcont)
				map.put(content.name, content);

			HashMap<Integer, String> local = CommonStatic.getDataMaps().localMusicMap;
			File[] musicList = music.listFiles();
			if (music.exists() && musicList != null)
				for (File m : musicList)
					if (m.getName().matches("\\d{3}\\.ogg") && m.getName().endsWith(".ogg")) {
						Integer id = Data.ignore(() -> Integer.parseInt(m.getName().substring(0, 3)));

						if (id != null && id < count && id >= 0)
							if(local.containsKey(id) && map.containsKey(m.getName()))
								exi[id] = local.get(id).equals(map.get(m.getName()).sha);
					}

			List<Downloader> ans = new ArrayList<>();
			for (int i = 0; i < count; i++)
				if (!exi[i]) {
					final int id = i;
					ContentJson content = map.get(Data.trio(id) + ".ogg");
					if(content == null)
						continue;

					File target = CommonStatic.ctx.getAssetFile("./music/" + Data.trio(i) + ".ogg");
					File temp = CommonStatic.ctx.getAssetFile("./music/.ogg.temp");
					String url = URL_MUSIC + Data.trio(i) + ".ogg";
					Downloader downloader = new Downloader(target, temp, "music " + Data.trio(i), false, url);
					downloader.post = () -> local.put(id, content.sha);
					ans.add(downloader);
				}
			return ans;
		};
	}

	public static List<Downloader> checkPCLibs(UpdateJson json) {
		File lib = new File(CommonStatic.ctx.getBCUFolder(), "./BCU_lib");
		List<Downloader> libs = new ArrayList<>();
		if (json != null) {
			Set<String> str = new HashSet<>();
			Collections.addAll(str, json.pc_libs);
			if (lib.exists()) {
				File[] libraryList = lib.listFiles();
				if (libraryList != null) {
					for (File f : libraryList)
						str.remove(f.getName());
				}
			}
			for (String s : str) {
				String url = URL_LIB + s;
				libs.add(new Downloader(new File(CommonStatic.ctx.getBCUFolder(), "./BCU_lib/" + s), new File(CommonStatic.ctx.getBCUFolder(), "./BCU_lib/.jar.temp"),
						"downloading BCU library " + s, false, url));
			}
		}
		return libs;
	}

	public static UpdateJson checkUpdate() throws Exception {
		JsonElement update = WebFileIO.read(URL_UPDATE);
		if (update == null)
			return null;

		UpdateJson uj = JsonDecoder.decode(update, UpdateJson.class);
		update = WebFileIO.read(URL_UPDATE_R);
		if (update == null)
			return uj;
		return uj.merge(JsonDecoder.decode(update, UpdateJson.class));
	}

}