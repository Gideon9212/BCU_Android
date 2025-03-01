package common.util;

import common.CommonStatic;
import common.battle.BattleField;
import common.io.BCUException;
import common.io.assets.Admin.StaticPermitted;
import common.pack.Context.ErrType;
import common.util.pack.bgeffect.BackgroundEffect;
import common.util.stage.Revival;
import common.util.unit.EneRand;
import common.util.unit.Enemy;
import common.util.unit.Trait;
import common.util.unit.UniRand;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * this class enables copy of an interconnected system. <br>
 * <br>
 * capable to copy: <br>
 * 1. Primary field <br>
 * 2. String field <br>
 * 3. Copible field <br>
 * 4. Array field of type 1~4 <br>
 * 5. Cloneable Collection and Map field with generic type of 1~4<br>
 * note: Collections are not Hashed <br>
 * exclusion:<br>
 * EAnimI (override)<br>
 * EneRand (update map reference) <br>
 */
@StaticPermitted(StaticPermitted.Type.TEMP)
public strictfp class BattleObj extends ImgCore implements Cloneable {

	public static final String NONC = "NONC_";

	private static final Class<?>[] EXCLUDE = { Number.class, String.class, Boolean.class, BattleStatic.class, Trait.class, Enum.class, BackgroundEffect.class, BattleField.class, EneRand.class, UniRand.class, Enemy.class, Revival.class };

	private static final Set<Class<?>> OLD = new HashSet<>();
	private static final Set<Class<?>> UNCHECKED = new HashSet<>();
	private static final Map<Integer, Object> ARRMAP = new HashMap<>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static Object hardCopy(Object obj) {
		if (obj == null)
			return null;
		Class<?> c = obj.getClass();
		if (c.isPrimitive())
			return obj;
		for (Class<?> cls : EXCLUDE)
			if (cls.isAssignableFrom(c))
				return obj;
		if (obj instanceof BattleObj)
			return ((BattleObj) obj).sysCopy();
		if (ARRMAP.containsKey(obj.hashCode()))
			return ARRMAP.get(obj.hashCode());
		if (obj.getClass().isArray()) {
			Object ans = Array.newInstance(c.getComponentType(), Array.getLength(obj));
			for (int i = 0; i < Array.getLength(ans); i++)
				Array.set(ans, i, hardCopy(Array.get(obj, i)));
			ARRMAP.put(obj.hashCode(), ans);
			return ans;
		}
		if (Collection.class.isAssignableFrom(c)) {
			Collection f2 = (Collection) obj;
			Collection f3 = null;
			try {
				f3 = f2.getClass().getConstructor().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (f3 != null)
				for (Object o : f2)
					f3.add(hardCopy(o));
			return f3;
		}
		if (Map.class.isAssignableFrom(c)) {
			Map f2 = (Map) obj;
			Map f3 = null;
			try {
				f3 = f2.getClass().getConstructor().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map f4 = f3;
			if (f4 != null)
				f2.forEach((k, v) -> f4.put(hardCopy(k), hardCopy(v)));
			return f3;
		}
		throw new BCUException("cannot copy class " + obj.getClass());
	}

	private static boolean checkField(Class<?> tc) {
		if (tc.isPrimitive())
			return true;
		boolean b0 = BattleObj.class.isAssignableFrom(tc);
		boolean b1 = BattleStatic.class.isAssignableFrom(tc);
		if (b0 && b1)
			return false;
		if (b0 || b1)
			return true;
		for (Class<?> cls : EXCLUDE)
			if (cls.isAssignableFrom(tc))
				return true;
		if (tc.isArray())
			return checkField(tc.getComponentType());
		return false;
	}

	@SuppressWarnings("unchecked")
	private static List<Field> getField(Class<? extends BattleObj> cls) {
		List<Field> fl = new ArrayList<>();
		Field[] fs = cls.getDeclaredFields();
		for (Field f : fs)
			if (!Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(true);
				fl.add(f);
			}
		Class<? extends BattleObj> sc = null;
		if (BattleObj.class.isAssignableFrom(cls) && BattleObj.class != cls.getSuperclass())
			sc = (Class<? extends BattleObj>) cls.getSuperclass();
		if (sc != null)
			fl.addAll(getField(sc));
		return fl;
	}

	protected BattleObj copy = null;

	@Override
	public final BattleObj clone() {
		BattleObj c = sysCopy();
		terminate();
		ARRMAP.clear();
		UNCHECKED.removeAll(OLD);
		for (Class<?> cls : UNCHECKED)
			CommonStatic.ctx.printErr(ErrType.WARN, "Unchecked Class in Battle: " + cls);
		OLD.addAll(UNCHECKED);
		UNCHECKED.clear();
		return c;
	}

	/**
	 * BattleStatic also has this method but different return type
	 */
	public final int conflict() {
		return 0;
	}

	/**
	 * override this method to make your own copy mechanics if you don't want all
	 * your fields copied <br>
	 * <br>
	 * this method is called to copy object's references to other objects
	 */
	protected void performDeepCopy() {
		List<Field> lf = getField(getClass());
		check(lf);
		for (Field f : lf) {
			if (f.getName().startsWith(NONC))
				continue;
			try {
				f.setAccessible(true);
				f.set(copy, hardCopy(f.get(this)));
			} catch (Exception e3) {
				System.out.println("failed to copy class " + getClass() + " at field " + f);
				e3.printStackTrace();
			}
		}
	}

	/**
	 * override this method to flush all objects used<br>
	 * <br>
	 * this method is called recursively to flush all resources used
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void terminate() {
		if (copy == null)
			return;
		BattleObj temp = copy;
		copy = null;
		if (temp != null)
			temp.terminate();
		List<Field> lf = getField(getClass());
		for (Field f : lf) {
			f.setAccessible(true);
			Class<?> tc = f.getType();

			if (BattleObj.class.isAssignableFrom(tc)) {
				BattleObj f2 = null;
				try {
					f2 = (BattleObj) f.get(this);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				if (f2 != null)
					f2.terminate();
			}
			if (tc.isArray() && BattleObj.class.isAssignableFrom(tc.getComponentType())) {
				BattleObj[] f2 = null;
				try {
					f2 = (BattleObj[]) f.get(this);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				if (f2 != null)
					for (BattleObj c : f2)
						if (c != null)
							c.terminate();
			}
			if (Collection.class.isAssignableFrom(tc)) {
				if (f.getName().equals(NONC))
					continue;
				Collection f2 = null;
				try {
					f2 = (Collection) f.get(this);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				if (f2 != null)
					for (Object c : f2)
						if (c != null && c instanceof BattleObj)
							((BattleObj) c).terminate();
			}
			if (Map.class.isAssignableFrom(tc)) {
				if (f.getName().equals(NONC))
					continue;
				Map f2 = null;
				try {
					f2 = (Map) f.get(this);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				if (f2 != null)
					f2.forEach((a, b) -> {
						if (a != null && a instanceof BattleObj)
							((BattleObj) a).terminate();
						if (b != null && b instanceof BattleObj)
							((BattleObj) b).terminate();
					});
			}
		}
	}

	/**
	 * this method is called to check that there isn't any unintended class
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void check(List<Field> lf) {
		for (Field f : lf) {
			Object obj = null;
			try {
				obj = f.get(this);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}
			if (obj == null)
				continue;
			Class<?> tc = obj.getClass();
			if (checkField(tc))
				continue;
			if (Collection.class.isAssignableFrom(tc)) {
				Collection f2 = (Collection) obj;
				for (Object o : f2)
					if (!checkField(o.getClass()))
						UNCHECKED.add(o.getClass());
				continue;
			}
			if (Map.class.isAssignableFrom(tc)) {
				Map f2 = (Map) obj;
				f2.forEach((a, b) -> {
					if (!checkField(a.getClass()))
						UNCHECKED.add(a.getClass());
					if (!checkField(b.getClass()))
						UNCHECKED.add(b.getClass());
				});
				continue;
			}
			UNCHECKED.add(tc);
		}
	}

	/**
	 * make a copy of this object during systematic clone process
	 */
	private BattleObj sysCopy() {
		if (copy != null)
			return copy;
		try {
			// copy primary types
			copy = (BattleObj) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		copy.copy = this;
		performDeepCopy();
		return copy;
	}

}
