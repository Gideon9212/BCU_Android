package common.system.files;

import common.CommonStatic;
import common.io.assets.AssetLoader;
import common.pack.UserProfile;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

public class VFile implements Comparable<VFile> {

	public static VFile get(String str) {
		VFile trial = getBCFileTree().find(str);

		if (trial == null && getBCFileTree().list().isEmpty()) {
			//Are we sure? Let's just re-load whole path
			AssetLoader.load(p -> {});
		} else {
			return trial;
		}

		//Once reload is done, try to get path again
		return getBCFileTree().find(str);
	}

	public static VFileRoot getBCFileTree() {
		return UserProfile.getBCData().root;
	}

	public static VFile getFile(File f) {
		if (!f.exists())
			return null;
		return new VFile(null, f.getName(), new FDFile(f));
	}

	@Deprecated
	public static VFile getFile(String path) {
		if (path.startsWith("./org/"))
			return getBCFileTree().find(path);
		if (path.startsWith("./workspace/")) {
			File f = CommonStatic.def.route(path);
			if (!f.exists())
				return null;
			return new VFile(null, f.getName(), new FDFile(f));
		}
		return null;
	}

	public static Queue<String> readLine(String str) {
		return get(str).getData().readLine();
	}

	public String name;

	protected VFile parent;

	private final Map<String, VFile> subs;

	private FileData data;

	public int mark;

	/**
	 * constructor for directory
	 */
	public VFile(VFile par, String str) {
		parent = par;
		name = str;
		subs = new TreeMap<>();
		data = null;
		if (parent != null)
			parent.subs.put(name, this);
	}

	/**
	 * constructor for data file
	 */
	public VFile(VFile par, String str, FileData fd) {
		parent = par;
		name = str;
		subs = null;
		data = fd;
		if (parent != null)
			parent.subs.put(name, this);
	}

	/**
	 * constructor for root directory
	 */
	protected VFile(String str) {
		this(null, str);
	}

	@Override
	public int compareTo(VFile o) {
		return name.compareTo(o.name);
	}

	public boolean containsSubDire(String dir) {
		return subs.containsKey(dir);
	}

	public int countSubDire() {
		int ans = 0;
		for (VFile f : subs.values())
			if (f.subs != null)
				ans++;
		return ans;
	}

	public void delete() {
		parent.subs.remove(name);
	}

	public FileData getData() {
		return data;
	}

	public String getName() {
		return name;
	}

	public VFile getParent() {
		return parent;
	}

	public String getPath() {
		if (parent != null)
			return parent.getPath() + "/" + name;
		return name;
	}

	public Collection<VFile> list() {
		return subs == null ? null : subs.values();
	}

	public void merge(VFile f) throws Exception {
		if (subs == null || f.subs == null)
			throw new Exception("merge can only happen for folders");
		for (VFile fi : f.subs.values()) {
			fi.parent = this;
			if (fi.subs == null || !subs.containsKey(fi.name))
				subs.put(fi.name, fi);
			else
				subs.get(fi.name).merge(fi);
		}
	}

	public void replace(FileData t) {
		delete();
		new VFile(parent, name, t);
	}

	public void setData(FileData fd) {
		data = fd;
	}

	@Override
	public String toString() {
		return name;
	}

}