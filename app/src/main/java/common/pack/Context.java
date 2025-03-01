package common.pack;

import common.io.Backup;
import common.io.PackLoader.ZipDesc.FileDesc;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public interface Context {

	enum ErrType {
		FATAL, ERROR, WARN, INFO, NEW, CORRUPT, DEBUG
	}

	interface RunExc {

		void run() throws Exception;

	}

	interface SupExc<T> {

		T get() throws Exception;

	}

	static void check(boolean bool, String str, File f) throws IOException {
		if (!bool)
			throw new IOException("failed to " + str + " file " + f);
	}

	static void check(File f) throws IOException {
		if (!f.getParentFile().exists())
			check(f.getParentFile().mkdirs(), "create", f);
		if (!f.exists())
			check(f.createNewFile(), "create", f);
	}

	static void delete(File f) throws IOException {
		if (f == null || !f.exists())
			return;
		if (f.isDirectory())
			for (File i : f.listFiles())
				delete(i);
		check(f.delete(), "delete", f);
	}

	static boolean renameTo(File a, File b) {
		if (!b.getParentFile().exists())
			b.getParentFile().mkdirs();
		if (b.exists())
			b.delete();
		return a.renameTo(b);
	}

	static String validate(String str, char replace) {
		char[] chs = new char[] { '.', '/', '\\', ':', '*', '?', '"', '<', '>', '|' };
		for (char c : chs)
			str = str.replace(c, replace);
		return str;
	}

	boolean confirm(String str);

	boolean confirmDelete(File f);

	File getAssetFile(String string);

	File getAuxFile(String string);

	InputStream getLangFile(String file);

	File getUserFile(String string);

	File getWorkspaceFile(String relativePath);

	File getBackupFile(String string);

	@Nonnull
	File getBCUFolder();

	@Nonnull
	File newFile(String path);

	String getAuthor();

	void initProfile();

	default boolean noticeErr(Context.RunExc r, ErrType t, String str) {
		try {
			r.run();
			return true;
		} catch (Exception e) {
			noticeErr(e, t, str);
			return false;
		}
	}

	default boolean noticeErr(Context.RunExc r, ErrType t, String str, Runnable onFail) {
		try {
			r.run();
			return true;
		} catch (Exception e) {
			noticeErr(e, t, str);
			onFail.run();
			return false;
		}
	}

	default <T> T noticeErr(Context.SupExc<T> r, ErrType t, String str) {
		try {
			return r.get();
		} catch (Exception e) {
			noticeErr(e, t, str);
			return null;
		}
	}

	default <T> T noticeErr(Context.SupExc<T> r, ErrType t, String str, Runnable onFail) {
		try {
			return r.get();
		} catch (Exception e) {
			onFail.run();
			noticeErr(e, t, str);
			return null;
		}
	}

	void noticeErr(Exception e, ErrType t, String str);

	boolean preload(FileDesc desc);

	void printErr(ErrType t, String str);

	void loadProg(double d, String str);

	boolean restore(Backup b, Consumer<Double> prog);
}