package common.io.assets;

import common.CommonStatic;
import common.io.PackLoader;
import common.io.PackLoader.ZipDesc;
import common.io.assets.Admin.StaticPermitted;
import common.io.assets.AssetLoader.AssetHeader.AssetEntry;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Context;
import common.pack.Context.ErrType;
import common.pack.PackData.PackDesc;
import common.system.files.FDFile;
import common.system.files.VFile;
import common.system.files.VFileRoot;
import common.util.Data;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

@StaticPermitted
public class AssetLoader {

	@JsonClass
	public static class AssetHeader {

		@JsonClass
		public static class AssetEntry {

			@JsonField
			public final PackDesc desc;

			@JsonField
			public final int size;

			public AssetEntry() {
				desc = null;
				size = 0;
			}

			private AssetEntry(File f) throws Exception {
				desc = PackLoader.readPack(f).desc;
				size = (int) f.length();
			}

			@Override
			public boolean equals(Object o) {
				return (o instanceof AssetEntry) && ((AssetEntry) o).desc.id.equals(desc.id);
			}

		}

		@JsonField(generic = AssetEntry.class)
		public final ArrayList<AssetEntry> list = new ArrayList<>();

		public int offset;

		public AssetHeader() {
		}

		private boolean add(File file) throws Exception {
			AssetEntry ent = new AssetEntry(file);
			if (list.contains(ent))
				return false;
			list.add(ent);
			return true;
		}

	}

	public static final String CORE_VER = "0.7.12.0";
	public static final byte FORK_VER = 13;

	private static final int LEN = 1024;

	public static void load(Consumer<Double> prog) {
		try {
			File folder = CommonStatic.ctx.getAssetFile("./assets/");
			TreeMap<String, File> map = new TreeMap<>();

			File[] fileList = folder.listFiles();
			if (fileList == null)
				return;

			for (File f : fileList)
				if (f.getName().endsWith(".assets.bcuzips"))
					map.put(f.getName(), f);
			int i = 0;
			TreeMap<String, ZipDesc> zips = new TreeMap<>();
			for (File f : map.values()) {
				int I = i;
				prog.accept(1.0 * (i++) / map.size());
				Consumer<Double> con = (d) -> prog.accept((I + d) / map.size());
				List<ZipDesc> list = PackLoader.readAssets(f, con);
				for (ZipDesc zip : list)
					if (Data.getVer(zip.desc.BCU_VERSION) <= Data.getVer(CORE_VER))
						zips.put(zip.desc.id, zip);
					else
						System.out.println(zip.desc.names + " requires core version " + zip.desc.BCU_VERSION + ". Current Core Version: " + CORE_VER);
			}
			for (ZipDesc zip : zips.values())
				VFile.getBCFileTree().merge(zip.tree);
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.FATAL, "failed to read asset");
		}
		try {
			File folder = new File(CommonStatic.ctx.getBCUFolder(), "./assets/custom");
			if (folder.exists()) {
				VFileRoot vr = new VFileRoot(".");
				add(vr, folder);
				VFile.getBCFileTree().merge(vr);
			}
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.FATAL, "failed to read custom asset");
		}
	}

	public static void removeTemp() {
		Set<String> prev = previewAssets();
		if (prev == null)
			return;
		try {
			File folder = CommonStatic.ctx.getAssetFile("./assets/");
			for (File f : folder.listFiles()) {
				if (!f.getName().startsWith("temp_"))
					continue;
				String fileName = f.getName().substring(5, f.getName().indexOf('.'));//5 is length of temp
				if (prev.contains("asset_" + fileName))
					Context.delete(f);
			}
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.WARN, "failed to remove unused assets");
		}
	}

	public static void merge() {
		try {
			File folder = CommonStatic.ctx.getAssetFile("./assets/");
			Map<String, Map<String, File>> map = new TreeMap<>();
			for (File f : folder.listFiles()) {
				if (!f.getName().endsWith(".asset.bcuzip"))
					continue;

				String fileName = f.getName().substring(0, f.getName().indexOf('.'));
				if(!CommonStatic.isInteger(fileName)) {
					Map<String, File> sub = map.computeIfAbsent(fileName.contains("fork_essentials") ? "fork_essentials"
							: fileName.startsWith("temp_") ? fileName : "custom", k -> new TreeMap<>());
					sub.put(fileName, f);
				} else {
					String pre = fileName.substring(0, 2);
					String name = fileName.substring(0, 6);
					Map<String, File> sub = map.computeIfAbsent(pre, k -> new TreeMap<>());
					sub.put(name, f);
				}
			}

			for (Entry<String, Map<String, File>> emain : map.entrySet()) {
				String targetName;

				if(!CommonStatic.isInteger(emain.getKey()))
					targetName = "./assets/" + emain.getKey() + ".assets.bcuzips";
				else
					targetName = "./assets/" + emain.getKey() + "xxxx.assets.bcuzips";

				File target = CommonStatic.ctx.getAssetFile(targetName);
				File dst = CommonStatic.ctx.getAssetFile("./assets/.assets.bcuzips.temp");
				Context.check(dst);
				FileOutputStream fos = new FileOutputStream(dst);
				AssetHeader header = new AssetHeader();
				FileInputStream fis = null;
				if (target.exists()) {
					fis = new FileInputStream(target);
					PackLoader.read(fis, header);
				}
				List<File> queue = new ArrayList<>();
				for (Entry<String, File> esub : emain.getValue().entrySet())
					if (header.add(esub.getValue()))
						queue.add(esub.getValue());
					else
						Context.delete(esub.getValue());
				PackLoader.write(fos, header);
				if (fis != null) {
					stream(fos, fis);
					fis.close();
				}
				for (File efile : queue) {
					fis = new FileInputStream(efile);
					stream(fos, fis);
					fis.close();
					Context.delete(efile);
				}
				fos.flush();
				fos.close();
				Context.delete(target);

				if (!dst.renameTo(target)) {
					System.out.println("W/AssetLoader::merge - Failed to rename the file : \n" +
							"\n" +
							"Target : " + target.getAbsolutePath() + "\n" +
							"Destination : " + dst.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.FATAL, "failed to merge asset");
		}
	}

	public static Set<String> previewAssets() {
		try {
			File folder = CommonStatic.ctx.getAssetFile("./assets/");
			Set<String> ans = new TreeSet<>();
			if (!folder.exists())
				return ans;

			File[] fileList = folder.listFiles();
			if (fileList == null)
				return ans;

			for (File f : fileList) {
				if (f.getName().endsWith(".assets.bcuzips")) {
					AssetHeader header = new AssetHeader();
					FileInputStream fis = new FileInputStream(f);
					PackLoader.read(fis, header);
					fis.close();
					for (AssetEntry ent : header.list)
						ans.add(ent.desc.id);
				} else if (f.getName().endsWith(".asset.bcuzip"))
					ans.add(PackLoader.readPack(f).desc.id);
			}
			return ans;
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.FATAL, "failed to preview asset");
			return null;
		}
	}

	private static void add(VFile vf, File f) {
		File[] fileList = f.listFiles();
		if (fileList == null)
			return;

		for (File fi : fileList)
			if (fi.isDirectory())
				add(new VFile(vf, fi.getName()), fi);
			else
				new VFile(vf, fi.getName(), new FDFile(fi));
	}

	private static void stream(OutputStream fos, InputStream fis) throws IOException {
		byte[] data = new byte[LEN];
		int len;
		do {
			len = fis.read(data);
			fos.write(data, 0, len);
		} while (len == LEN);
	}

}