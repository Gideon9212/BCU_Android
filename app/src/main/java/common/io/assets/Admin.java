package common.io.assets;

import common.CommonStatic;
import common.battle.BasisSet;
import common.io.Backup;
import common.io.PackLoader;
import common.io.PackLoader.ZipDesc;
import common.io.PackLoader.ZipDesc.FileDesc;
import common.io.assets.Admin.StaticPermitted;
import common.pack.Context;
import common.pack.PackData.PackDesc;
import common.pack.UserProfile;
import common.util.Data;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

@StaticPermitted
public class Admin {

	public static class AdminContext implements Context {

		@Override
		public boolean confirm(String str) {
			return true;
		}

		@Override
		public boolean confirmDelete(File f) {
			return true;
		}

		@Override
		public File getAssetFile(String string) {
			return new File("./assets/" + string);
		}

		@Override
		public File getAuxFile(String string) {
			return new File(string);
		}

		@Override
		public InputStream getLangFile(String file) {
			return Data.err(() -> new FileInputStream("./assets/lang/en/" + file));
		}

		@Override
		public File getUserFile(String string) {
			return new File("./user/" + string);
		}

		@Override
		public File getWorkspaceFile(String relativePath) {
			return new File("./workspace/" + relativePath);
		}

		@Override
		public File getBackupFile(String string) {
			return new File("./backups/"+string);
		}

		@NotNull
		@Override
		public File getBCUFolder() {
			return new File("./");
		}

		@NotNull
		@Override
		public File newFile(String path) {
			return new File(path);
		}

		@Override
		public String getAuthor() {
			return "";
		}

		@Override
		public void initProfile() {
			AssetLoader.load((d) -> {
			});
			UserProfile.getBCData().load((str) -> {
			}, (d) -> {
			});
			BasisSet.read();
			// UserProfile.loadPacks(); TODO
		}

		@Override
		public void noticeErr(Exception e, ErrType t, String str) {
			printErr(t, str);
			e.printStackTrace(t == ErrType.INFO ? System.out : System.err);
		}

		@Override
		public boolean preload(FileDesc desc) {
			return Admin.preload(desc);
		}

		@Override
		public void printErr(ErrType t, String str) {
			(t == ErrType.INFO ? System.out : System.err).println(str);
		}

		@Override
		public void loadProg(double d, String str) {
			System.out.println(str);
		}

		@Override
		public boolean restore(Backup b, Consumer<Double> prog) {
			return false;
		}

	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.FIELD })
	public @interface StaticPermitted {

		enum Type {
			FINAL, ENV, TEMP
		}

		Type value() default Type.FINAL;

	}

	private static final String[] ANIMFL = { ".imgcut", ".mamodel", ".maanim" };

	private static final String[] NONPRE = { "\\./org/img/../.....\\.png", "\\./org/enemy/.../..._.\\.png",
			"\\./org/unit/..././..._.\\.png", "\\./org/unit/..././udi..._.\\.png" };

	public static void main(String[] args) throws Exception {
		UserProfile.profile();
		CommonStatic.ctx = new AdminContext();
		ziphandler(args);
		// UpdateCheck.checkAsset(UpdateCheck.checkUpdate(), "pc");
		// testInternet();
		// AssetLoader.merge();
		// searchForStaticFields();
	}

	public static boolean preload(FileDesc fd) {
		if (fd.size < 1024)
			return true;
		if (fd.size > 65536)
			return false;
		for (String str : ANIMFL)
			if (fd.path.endsWith(str))
				return false;
		for (String str : NONPRE)
			if (fd.path.length() == str.length() - 2 && fd.path.matches(str))
				return false;
		return true;
	}

	public static List<ZipDesc> read() throws Exception {
		File folder = new File("./assets/assets/");
		List<ZipDesc> list = new ArrayList<>();
		for (File f : folder.listFiles())
			if (f.getName().endsWith(".asset.bcuzip")) {
				ZipDesc zd = PackLoader.readPack(f);
				list.add(zd);
			}
		return list;
	}

	public static void searchForStaticFields() throws ClassNotFoundException {
		File f = new File("./src/common");
		Queue<File> qfile = new ArrayDeque<>();
		Queue<Class<?>> qcls = new ArrayDeque<>();
		qfile.add(f);
		while (qfile.size() > 0) {
			File fi = qfile.poll();
			if (fi.isDirectory())
				for (File fx : fi.listFiles())
					qfile.add(fx);
			else if (fi.getName().endsWith(".java")) {
				String str = fi.toString();
				str = str.substring(6, str.length() - 5);
				str = str.replaceAll("/", ".");
				qcls.add(Class.forName(str));
			}
		}
		List<Class<?>> clist = new ArrayList<>();
		while (qcls.size() > 0) {
			Class<?> cls = qcls.poll();
			clist.add(cls);
			for (Class<?> ci : cls.getDeclaredClasses())
				qcls.add(ci);
		}
		List<Field> flist = new ArrayList<>();
		for (Class<?> cls : clist) {
			if (cls.isEnum())
				continue;
			if (cls.getAnnotation(StaticPermitted.class) != null)
				continue;
			for (Field field : cls.getDeclaredFields()) {
				if ((field.getModifiers() & Modifier.STATIC) == 0)
					continue;
				if ((field.getModifiers() & Modifier.FINAL) != 0) {
					if (field.getType().isPrimitive())
						continue;
					if (field.getType() == String.class)
						continue;
				}
				if (field.getAnnotation(StaticPermitted.class) != null)
					continue;
				flist.add(field);
			}
		}
		for (Field field : flist) {
			System.out.println(field);
		}
	}

	public static void write(String id, String ver, String desc, Consumer<Double> prog) throws Exception {
		File f = new File("./" + id);
		File dst = new File("./" + id + ".asset.bcuzip");
		Context.check(dst);
		PackDesc pd = new PackDesc("asset_" + id);
		pd.author = "PONOS";
		pd.BCU_VERSION = ver;
		pd.info.put(desc);
		PackLoader.writePack(dst, f, pd, "battlecatsultimate", prog);

	}

	public static void ziphandler(String... args) throws Exception {
		Consumer<Double> prog = (d) -> System.out.print("\b\b\b" + (int) (d * 100) + "%" + (d < 0.1 ? " " : ""));
		if (args.length > 3 && args[0].equals("encode")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 3; i < args.length; i++) {
				sb.append(args[i]);
				if (i < args.length - 1)
					sb.append(' ');
			}
			String desc = sb.toString();
			System.out.println("id: " + args[1]);
			System.out.println("version: " + args[2]);
			System.out.println("description: " + desc);
			System.out.print("0% ");
			write(args[1], args[2], desc, prog);
			System.out.println();
			return;
		}
		if (args.length > 1 && args[0].equals("decode")) {
			ZipDesc zip = PackLoader.readPack(new File(args[1]));
			System.out.println("id: " + zip.desc.id);
			System.out.println("version: " + zip.desc.BCU_VERSION);
			System.out.println("description: " + zip.desc.info);
			System.out.print("0% ");
			zip.unzip(path -> new File("./output/" + path), prog);
			System.out.println();
			return;
		}
		System.out.println("parameter: decode [id] - deocde a bcuzip");
		System.out.println("parameter: encode [id], [version], [description...]");
		System.out.println("\tid - 6 digit number, the name of the folder");
		System.out.println("\tversion - the minimum version of BCU Core, 0.5.0.0 is the minimum");
		System.out.println("\tenter any string for description, white space is allowed");

	}

}
