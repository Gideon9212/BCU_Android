package common.io.json;

import com.google.gson.*;
import common.io.json.JsonClass.JCGeneric;
import common.io.json.JsonClass.JCIdentifier;
import common.pack.Identifier;
import common.util.Data;
import common.util.stage.CastleImg;
import common.util.stage.CastleList;
import common.util.stage.MapColc;
import common.util.stage.MapColc.PackMapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Dependency {

	private static class DependencyCheck {

		protected static void collect(Dependency set, Object obj, DependencyCheck par) throws Exception {
			if (obj == null)
				return;
			if (obj instanceof JsonElement)
				return;
			if (obj instanceof Number)
				return;
			if (obj instanceof Boolean)
				return;
			if (obj instanceof String)
				return;
			if (obj instanceof Class)
				return;
			if (obj instanceof Enum)
				return;
			if (obj instanceof Identifier) {
				set.add((Identifier<?>) obj);
				return;
			}
			Class<?> cls = obj.getClass();
			if (cls.isArray()) {
				int n = Array.getLength(obj);
				for (int i = 0; i < n; i++)
					collect(set, Array.get(obj, i), par);
				return;
			}
			if (cls.getAnnotation(JCGeneric.class) != null && par != null && par.curjfld.alias().length > par.index) {
				JCGeneric jcg = cls.getAnnotation(JCGeneric.class);
				Class<?> alias = par.curjfld.alias()[par.index];
				boolean found = false;
				for (Class<?> ala : jcg.value())
					if (ala == alias) {
						found = true;
						break;
					}
				if (!found)
					throw new JsonException(true, cls, alias + "Not present in JCGeneric");
				for (Field f : cls.getFields()) {
					JCIdentifier jcgw = f.getAnnotation(JCIdentifier.class);
					if (jcgw != null && f.getType() == alias) {
						collect(set, f.get(obj), par);
						return;
					}
				}
				Constructor<?> con = getFuncConstructor(alias, cls);
				collect(set, con.newInstance(obj), par);
				return;
			}
			if (par != null && par.curjfld != null) {
				JsonField jfield = par.curjfld;
				if (jfield.ser() == JsonField.SerType.FUNC) {
					if (jfield.serializer().length() == 0)
						throw new JsonException(false, jfield, "no serializer function");
					Method m = par.obj.getClass().getMethod(jfield.serializer(), cls);
					collect(set, m.invoke(par.obj, obj), null);
					return;
				} else if (jfield.ser() == JsonField.SerType.CLASS) {
					JsonClass cjc = cls.getAnnotation(JsonClass.class);
					if (cjc == null || cjc.serializer().length() == 0)
						throw new JsonException(false, jfield, "no serializer function");
					String func = cjc.serializer();
					Method m = cls.getMethod(func);
					collect(set, m.invoke(obj), null);
					return;
				}
			}
			if (obj instanceof Iterable) {
				for (Object o : (Iterable<?>) obj)
					collect(set, o, par);
				return;
			}
			JsonClass jc = cls.getAnnotation(JsonClass.class);
			if (jc != null)
				if (jc.write() == JsonClass.WType.DEF) {
					new DependencyCheck(set, par, obj);
					return;
				} else if (jc.write() == JsonClass.WType.CLASS) {
					if (jc.serializer().length() == 0)
						throw new JsonException(false, jc, "no serializer function");
					String func = jc.serializer();
					Method m = cls.getMethod(func);
					collect(set, m.invoke(obj), null);
					return;
				}
			if (obj instanceof Map) {
				for (Entry<?, ?> ent : ((Map<?, ?>) obj).entrySet()) {
					collect(set, ent.getKey(), par);
					collect(set, ent.getValue(), par);
				}
				return;
			}
			throw new JsonException(false, obj, obj.getClass() + " not defined");
		}

		/***
		 * Gets a valid constructor from a class, and in case no constructor is found with it, checks if there's a valid option using its interfaces or superclass
		 * @param alias The class intended to be constructed as
		 * @param cls The current class used as parameter for the constructor
		 * @return A valid constructor that takes the given class as parameter
		 * @throws JsonException If this class, and none of it's superclasses or interfaces has a JCGeneric constructor that uses the given class
		 */
		private static Constructor<?> getFuncConstructor(Class<?> alias, Class<?> cls) throws JsonException {
			try {
				return alias.getConstructor(cls);
			} catch (Exception e) {
				if (cls.getSuperclass() != null && cls.getSuperclass().getAnnotation(JCGeneric.class) != null) {
					JCGeneric jcg = cls.getSuperclass().getAnnotation(JCGeneric.class);
					for (Class<?> ala : jcg.value())
						if (ala == alias)
							return getFuncConstructor(alias, cls.getSuperclass());
				}
				for (Class<?> intf : cls.getInterfaces()) {
					if (intf.getAnnotation(JCGeneric.class) != null) {
						JCGeneric jcg = intf.getAnnotation(JCGeneric.class);
						for (Class<?> ala : jcg.value())
							if (ala == alias)
								return getFuncConstructor(alias, intf);
					}
				}
				throw new JsonException(false, cls, "No constructor using " + cls + " found");
			}
		}

		private final DependencyCheck par;
		private final Object obj;
		private final Dependency set;

		private JsonClass curjcls;
		private JsonField curjfld;
		private final int index = 0;

		private DependencyCheck(Dependency set, DependencyCheck parent, Object object) throws Exception {
			this.set = set;
			par = parent;
			obj = object;
			collect(obj.getClass());
		}

		private void collect(Class<?> cls) throws Exception {
			if (cls.getSuperclass().getAnnotation(JsonClass.class) != null)
				collect(cls.getSuperclass());
			curjcls = cls.getAnnotation(JsonClass.class);
			for (Field f : cls.getDeclaredFields())
				if (curjcls.noTag() == JsonClass.NoTag.LOAD || f.getAnnotation(JsonField.class) != null) {
					if (Modifier.isStatic(f.getModifiers()))
						continue;
					JsonField jf = f.getAnnotation(JsonField.class);
					if (jf == null)
						jf = JsonField.DEF;
					if (jf.block() || jf.io() == JsonField.IOType.R || jf.backCompat() == JsonField.CompatType.UPST)
						continue;
					f.setAccessible(true);
					curjfld = jf;
					Object val = f.get(obj);
					collect(set, val, getInvoker());
					curjfld = null;
				}
			for (Method m : cls.getDeclaredMethods())
				if (m.getAnnotation(JsonField.class) != null) {
					JsonField jf = m.getAnnotation(JsonField.class);
					if (jf.io() == JsonField.IOType.R || jf.backCompat() == JsonField.CompatType.UPST)
						continue;
					if (jf.io() == JsonField.IOType.RW)
						throw new JsonException(false, obj, "RW IOType", m);
					String tag = jf.tag();
					if (tag.length() == 0)
						throw new JsonException(false, obj, "No tag", m);
					curjfld = jf;
					collect(set, m.invoke(obj), getInvoker());
					curjfld = null;
				}
		}

		private DependencyCheck getInvoker() {
			return curjcls.bypass() ? par : this;
		}

	}

	public static Dependency collect(Object obj) {
		Dependency set = new Dependency();
		Data.err(() -> DependencyCheck.collect(set, obj, null));
		return set;
	}

	private final Map<Class<?>, Map<String, Set<Identifier<?>>>> map = new HashMap<>();

	public Map<Class<?>, Map<String, Set<Identifier<?>>>> getMap() {
		return map;
	}

	public Set<String> getPacks() {
		Set<String> ans = new TreeSet<>();
		for (Entry<Class<?>, Map<String, Set<Identifier<?>>>> ent : map.entrySet()) {
			Set<String> packs = ent.getValue().keySet();
			if (ent.getKey() == CastleImg.class) {
				for (String pack : packs) {
					boolean def = false;
					for (CastleList cl : CastleList.defset())
						if (cl.getSID().equals(pack)) {
							def = true;
							break;
						}
					if (!def)
						ans.add(pack);
				}

			} else if (ent.getKey() == Stage.class) {
				for (String pack : packs) {
					MapColc mc = StageMap.get(pack).getCont();
					if (mc instanceof PackMapColc)
						ans.add(mc.getSID());
				}
			} else if (ent.getKey() == StageMap.class) {
				for (String pack : packs) {
					MapColc mc = MapColc.get(pack);
					if (mc instanceof PackMapColc)
						ans.add(mc.getSID());
				}
			} else
				ans.addAll(packs);
		}
		return ans;
	}

	protected void add(Identifier<?> id) {
		Map<String, Set<Identifier<?>>> cont;
		if (map.containsKey(id.cls))
			cont = map.get(id.cls);
		else
			map.put(id.cls, cont = new TreeMap<>());
		Set<Identifier<?>> set;
		if (cont.containsKey(id.pack))
			set = cont.get(id.pack);
		else
			cont.put(id.pack, set = new TreeSet<>());
		set.add(id);
	}
}