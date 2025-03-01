package common.pack;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.Data;
import common.util.pack.Background;
import common.util.stage.CastleImg;
import common.util.stage.Stage;
import common.util.unit.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class Identifier<T extends IndexContainer.Indexable<?, T>> implements Comparable<Identifier<?>>, Cloneable {

	public static final String DEF = "000000";

	@Nullable
	public static <T extends IndexContainer.Indexable<?, T>> T get(Identifier<T> id) {
		return id == null ? null : id.get();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@NotNull
	public static <T> T getOr(Identifier<?> id, Class<T> cls) {
		if (id != null) {
			IndexContainer ic = (IndexContainer) getContainer(id.cls, id.pack);
			if (ic != null) {
				Object ans = ic.getList(id.cls, (r, l) -> r == null ? l.getRaw(id.id) : r, null);
				if (ans != null)
					return (T) ans;
			}
		}

		if(cls == EneRand.class) {
			return (T) new Identifier(DEF, Enemy.class, 0).get();
		} else if (cls == UniRand.class) {
			return (T) new Identifier(DEF, Unit.class, 0).get();
		} else if (cls == Stage.class) {
			return (T) new Identifier(DEF + "/0", Stage.class, 0).get();
		} else {
			return (T) new Identifier(DEF, cls, 0).get();
		}
	}

	/**
	 * cls must be a class implementing Indexable. interfaces or other classes will
	 * go through fixer
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IndexContainer.Indexable<?, T>> Identifier<T> parseInt(int v, Class<? extends T> cls) {
		return parseIntRaw(v, cls);
	}

	public static <T extends IndexContainer.Indexable<?, T>> Identifier<T> rawParseInt(int v, Class<? extends T> cls) {
		return new Identifier<>(DEF, cls, v);
	}

	@Deprecated
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Identifier parseIntRaw(int v, Class<?> cls) {
		if (cls == AbEnemy.class) {
			if (v < 1000 || v % 1000 < 500)
				cls = Enemy.class;
			else {
				cls = EneRand.class;
				v -= 500;
			}
		}
		if (cls == null || cls.isInterface() || !IndexContainer.Indexable.class.isAssignableFrom(cls))
			cls = parse(v, cls);
		String pack = cls != CastleImg.class && v / 1000 == 0 ? DEF : Data.hex(v / 1000);
		int id = v % 1000;
		return new Identifier(pack, cls, id);
	}

	public static Class<?> parse(int val, Class<?> cls) {
		if (cls == Data.Proc.THEME.class)
			return Background.class;
		else if (cls == Unit.class)
			return cls;
		else
			return val % 1000 < 500 ? Enemy.class : EneRand.class;
	}

	private static Object getContainer(Class<?> cls, String str) {
		IndexContainer.IndexCont cont = null;
		Queue<Class<?>> q = new ArrayDeque<>();
		q.add(cls);
		while (q.size() > 0) {
			Class<?> ci = q.poll();
			if ((cont = ci.getAnnotation(IndexContainer.IndexCont.class)) != null)
				break;
			if (ci.getSuperclass() != null)
				q.add(ci.getSuperclass());
			Collections.addAll(q, ci.getInterfaces());
		}
		if (cont == null)
			return null;
		Method m = null;
		for (Method mi : cont.value().getMethods())
			if (mi.getAnnotation(IndexContainer.ContGetter.class) != null)
				m = mi;
		if (m == null)
			return null;
		Method fm = m;
		return Data.err(() -> fm.invoke(null, str));
	}

	public Class<? extends T> cls;
	@JsonField(defval = DEF)
	public String pack = DEF;
	public int id;

	@Deprecated
	public Identifier() {
		cls = null;
		id = 0;
	}

	public Identifier(String pack, Class<? extends T> cls, int id) {
		this.cls = cls;
		this.pack = pack;
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Identifier<T> clone() {
		return (Identifier<T>) Data.err(super::clone);
	}

	@Override
	public int compareTo(Identifier<?> identifier) {
		int val = pack.compareTo(identifier.pack);
		if (val != 0)
			return val;
		val = Integer.compare(cls.hashCode(), identifier.cls.hashCode());
		if (val != 0)
			return val;
		return Integer.compare(id, identifier.id);
	}

	public boolean equals(Identifier<T> o) {
		if (pack == null || o.pack == null)
			return false;

		if (cls == null || o.cls == null)
			return false;

		return pack.equals(o.pack) && id == o.id && o.cls == cls;
	}

	@JsonClass.JCGetter
	@SuppressWarnings("unchecked")
	public T get() {
		IndexContainer cont = getCont();
		if (cont == null)
			return null;

		return (T) cont.getList(cls, (r, l) -> r == null ? l.getRaw(id) : r, null);
	}

	/**
	 * Does get()'s function without the errors. Used mostly for getting stages
	 * @return get() result, or null if there was an error
	 */
	public T safeGet() {
		try {
			return get();
		} catch (Exception ignored) {
			return null;
		}
	}

	public IndexContainer getCont() {
		return (IndexContainer) getContainer(cls, pack);
	}

	public boolean isNull() {
		return id == -1;
	}

	@Override
	public String toString() {
		return pack + "/" + id;
	}

}