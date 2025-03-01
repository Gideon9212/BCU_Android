package common.io.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import common.io.assets.Admin.StaticPermitted;
import common.io.json.JsonClass.JCGetter;
import common.io.json.JsonField.GenType;
import common.io.json.JsonField.Handler;
import common.util.Data;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class JsonDecoder {

	public interface Decoder {

		Object decode(JsonElement elem) throws Exception;

	}

	@Documented
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface OnInjected {
	}

	@Documented
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface PostLoad {
	}

	@StaticPermitted
	public static final Map<Class<?>, Decoder> REGISTER = new HashMap<>();

	static {
		REGISTER.put(Boolean.TYPE, JsonDecoder::getBoolean);
		REGISTER.put(Boolean.class, JsonDecoder::getBoolean);
		REGISTER.put(Byte.TYPE, JsonDecoder::getByte);
		REGISTER.put(Byte.class, JsonDecoder::getByte);
		REGISTER.put(Short.TYPE, JsonDecoder::getShort);
		REGISTER.put(Short.class, JsonDecoder::getShort);
		REGISTER.put(Integer.TYPE, JsonDecoder::getInt);
		REGISTER.put(Integer.class, JsonDecoder::getInt);
		REGISTER.put(Long.TYPE, JsonDecoder::getLong);
		REGISTER.put(Long.class, JsonDecoder::getLong);
		REGISTER.put(Float.TYPE, JsonDecoder::getFloat);
		REGISTER.put(Float.class, JsonDecoder::getFloat);
		REGISTER.put(Double.TYPE, JsonDecoder::getDouble);
		REGISTER.put(Double.class, JsonDecoder::getDouble);
		REGISTER.put(String.class, JsonDecoder::getString);
		REGISTER.put(Class.class, (elem) -> Class.forName(getString(elem)));
	}

	@StaticPermitted(StaticPermitted.Type.TEMP)
	private static JsonDecoder current;
	private static final LinkedList<JsonDecoder> declast = new LinkedList<>();

	public static Object decode(JsonElement elem, Class<?> cls, JsonDecoder par) throws Exception {
		if (elem.isJsonNull())
			return null;
		if (JsonElement.class.isAssignableFrom(cls))
			return elem;
		Decoder dec = REGISTER.get(cls);
		if (dec != null)
			return dec.decode(elem);
		if (cls.isArray())
			return decodeArray(elem, cls, par);
		if (List.class.isAssignableFrom(cls))
			return decodeList(elem, cls, par);
		if (Map.class.isAssignableFrom(cls))
			return decodeMap(elem, cls, par);
		if (Set.class.isAssignableFrom(cls))
			return decodeSet(elem, cls, par);
		if (Enum.class.isAssignableFrom(cls))
			return decodeEnum(elem, cls);
		// alias
		if (par != null && par.curjfld.alias().length > par.index) {
			Class<?> alias = par.curjfld.alias()[par.index];
			if (alias != cls && alias != void.class) {
				Object input = decode(elem, alias, par);
				for (Method m : alias.getDeclaredMethods())
					if (m.getAnnotation(JCGetter.class) != null) {
						Class<?> ret = m.getReturnType();
						if (cls == ret || cls.isAssignableFrom(ret) || ret.isAssignableFrom(cls))
							return m.invoke(input);
					}
				throw new JsonException(true, cls, "no JCGetter present: " + alias + "->" + cls);
			}
		}
		// fill existing object
		if (par != null && par.curjfld.gen() == GenType.FILL) {
			Object val = par.curfld.get(par.obj);
			if (cls.getAnnotation(JsonClass.class) != null)
				return inject(par, elem.getAsJsonObject(), cls, val);
			return val;
		}
		// generator
		if (par != null && par.curjfld.gen() == GenType.GEN) {
			Class<?> ccls = par.obj.getClass();
			// default generator
			if (par.curjfld.generator().isEmpty()) {
				Constructor<?> cst = null;
				for (Constructor<?> ci : cls.getDeclaredConstructors())
					if (ci.getParameterTypes().length == 1 && ci.getParameterTypes()[0].isAssignableFrom(ccls))
						cst = ci;
				if (cst == null)
					throw new JsonException(true, cls, "no constructor found");
				Object val = cst.newInstance(par.obj);
				return inject(par, elem.getAsJsonObject(), cls, val);
			}
			// functional generator
			Method m = ccls.getMethod(par.curjfld.generator(), Class.class, JsonElement.class);
			return m.invoke(par.obj, cls, elem);
		}
		if (elem.isJsonObject() && elem.getAsJsonObject().has("_class"))
			cls = Class.forName(elem.getAsJsonObject().get("_class").getAsString());
		if (cls.getAnnotation(JsonClass.class) != null)
			return decodeObject(elem, cls, par);
		throw new JsonException(true, cls, "class not possible to generate");
	}

	@SuppressWarnings("unchecked")
	public static <T> T decode(JsonElement elem, Class<T> cls) {
		return (T) Data.err(() -> decode(elem, cls, null));
	}

	private static boolean getBoolean(JsonElement elem) throws JsonException {
		if (!elem.isJsonPrimitive() || !((JsonPrimitive) elem).isBoolean())
			throw new JsonException(true, elem, "this element is not boolean");
		return elem.getAsBoolean();
	}

	private static byte getByte(JsonElement elem) throws JsonException {
		if (!elem.isJsonPrimitive() || !((JsonPrimitive) elem).isNumber())
			throw new JsonException(true, elem, "this element is not number");
		return elem.getAsByte();
	}

	private static double getDouble(JsonElement elem) throws JsonException {
		if (!elem.isJsonPrimitive() || !((JsonPrimitive) elem).isNumber())
			throw new JsonException(true, elem, "this element is not number");
		return elem.getAsDouble();
	}

	private static float getFloat(JsonElement elem) throws JsonException {
		if (!elem.isJsonPrimitive() || !((JsonPrimitive) elem).isNumber())
			throw new JsonException(true, elem, "this element is not number");
		return elem.getAsFloat();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getGlobal(Class<T> cls) {
		return (T) getGlobal(current, cls);
	}

	private static int getInt(JsonElement elem) throws JsonException {
		if (!elem.isJsonPrimitive() || !((JsonPrimitive) elem).isNumber())
			throw new JsonException(true, elem, "this element is not number");
		return elem.getAsInt();
	}

	private static long getLong(JsonElement elem) throws JsonException {
		if (!elem.isJsonPrimitive() || !((JsonPrimitive) elem).isNumber())
			throw new JsonException(true, elem, "this element is not number");
		return elem.getAsLong();
	}

	private static short getShort(JsonElement elem) throws JsonException {
		if (!elem.isJsonPrimitive() || !((JsonPrimitive) elem).isNumber())
			throw new JsonException(true, elem, "this element is not number");
		return elem.getAsShort();
	}

	public static String getString(JsonElement elem) throws JsonException {
		if (elem.isJsonNull())
			return null;
		if (elem.isJsonArray()) {
			String ans = "";
			JsonArray arr = elem.getAsJsonArray();
			for (int i = 0; i < arr.size(); i++)
				ans += arr.get(i).getAsString();
			return ans;
		}
		if (!elem.isJsonPrimitive() || !((JsonPrimitive) elem).isString())
			throw new JsonException(true, elem, "this element is not string");
		return elem.getAsString();
	}

	@SuppressWarnings("unchecked")
	public static <T> T inject(JsonElement elem, Class<T> cls, T pre) throws Exception {
		return (T) inject(null, elem.getAsJsonObject(), cls, pre);
	}

	public static void finishLoading() {
		while (!declast.isEmpty()) {
			JsonDecoder dc = declast.get(0);
			Data.err(() -> dc.decodeFields(dc.tarcls, true));
			declast.remove(0);
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Object> decodeList(JsonElement elem, Class<?> cls, JsonDecoder par) throws Exception {
		if (par.curjfld == null || par.curjfld.generic().length != 1)
			throw new JsonException(true, par.curfld, "generic data structure requires typeProvider tag");
		if (elem.isJsonNull())
			return null;
		List<Object> val;
		if (elem.isJsonObject() && par.curjfld.usePool()) {
			JsonArray pool = elem.getAsJsonObject().get("pool").getAsJsonArray();
			JsonArray data = elem.getAsJsonObject().get("data").getAsJsonArray();
			Handler handler = new Handler(pool, null, par);
			int n = data.size();
			val = (List<Object>) cls.getConstructor(int.class).newInstance(n);
			for (int i = 0; i < n; i++)
				val.add(handler.get(data.get(i).getAsInt()));
			return val;
		}
		if (!elem.isJsonArray())
			throw new JsonException(true, elem, "this element is not array");
		JsonArray jarr = elem.getAsJsonArray();
		int n = jarr.size();
		val = (List<Object>) cls.getConstructor(int.class).newInstance(n);
		for (int i = 0; i < n; i++) {
			val.add(decode(jarr.get(i), par.curjfld.generic()[0], par));
		}
		return val;
	}

	private static Object decodeArray(JsonElement elem, Class<?> cls, JsonDecoder par) throws Exception {
		Class<?> ccls = cls.getComponentType();
		JsonField jf = par == null ? null : par.curjfld;
		if (elem.isJsonObject() && jf != null && jf.usePool()) {
			JsonArray pool = elem.getAsJsonObject().get("pool").getAsJsonArray();
			JsonArray data = elem.getAsJsonObject().get("data").getAsJsonArray();
			Handler handler = new Handler(pool, ccls, par);
			int n = data.size();
			Object arr = getArray(ccls, n, par);
			for (int i = 0; i < n; i++)
				Array.set(arr, i, handler.get(data.get(i).getAsInt()));
			return arr;
		}
		if (!elem.isJsonArray())
			throw new JsonException(true, cls, "Element + " + elem + " on " + cls + " at " + ccls + " in " + par + " is not array");
		JsonArray jarr = elem.getAsJsonArray();
		int n = jarr.size();
		Object arr = getArray(ccls, n, par);
		for (int i = 0; i < n; i++)
			Array.set(arr, i, decode(jarr.get(i), ccls, par));
		return arr;
	}

	private static Map<Object, Object> decodeMap(JsonElement elem, Class<?> cls, JsonDecoder par) throws Exception {
		if (par.curjfld == null || par.curjfld.generic().length != 2)
			throw new JsonException(true, par.curfld, "generic data structure requires typeProvider tag");
		if (elem.isJsonNull())
			return null;
		if (!elem.isJsonArray())
			throw new JsonException(true, elem, "this element is not array");

		JsonArray jarr = elem.getAsJsonArray();
		int n = jarr.size();

		@SuppressWarnings("unchecked")
		Map<Object, Object> val = (Map<Object, Object>) cls.newInstance();
		for (int i = 0; i < n; i++) {
			JsonObject obj = jarr.get(i).getAsJsonObject();
			Object key = decode(obj.get("key"), par.curjfld.generic()[0], par);
			par.index = 1;
			Object ent = decode(obj.get("val"), par.curjfld.generic()[1], par);
			par.index = 0;

			if(key != null)
				val.put(key, ent);
		}
		return val;

	}

	private static Object decodeObject(JsonElement elem, Class<?> cls, JsonDecoder par) throws Exception {
		if (elem.isJsonNull())
			return null;
		if (!elem.isJsonObject())
			throw new JsonException(true, elem, "this element is not object for " + cls);
		JsonObject jobj = elem.getAsJsonObject();
		JsonClass jc = cls.getAnnotation(JsonClass.class);
		if (jc.read() == JsonClass.RType.FILL) {
			if (par != null) {
				Object val = par.curfld.get(par.obj);
				if (val != null)
					if (cls.getAnnotation(JsonClass.class) != null)
						return inject(par, elem.getAsJsonObject(), cls, val);
					else
						return val;
			}
			throw new JsonException(true, cls, "RType FILL requires GenType FILL or GEN, or implicit GenType FILL: " + cls + ", " + elem);
		} else if (jc.read() == JsonClass.RType.DATA)
			return inject(par, jobj, cls, null);
		else if (jc.read() == JsonClass.RType.MANUAL) {
			String func = jc.generator();
			if (func.isEmpty())
				throw new JsonException(true, cls, "no generate function");
			Method m = cls.getMethod(func, JsonElement.class);
			return m.invoke(null, jobj);
		} else
			throw new JsonException(true, elem, "class not possible to generate");
	}

	private static Set<Object> decodeSet(JsonElement elem, Class<?> cls, JsonDecoder par) throws Exception {
		if (par.curjfld == null || par.curjfld.generic().length != 1)
			throw new JsonException(true, par.curfld, "generic data structure requires typeProvider tag");
		if (elem.isJsonNull())
			return null;
		if (!elem.isJsonArray())
			throw new JsonException(true, elem, "this element is not array");
		JsonArray jarr = elem.getAsJsonArray();
		int n = jarr.size();
		@SuppressWarnings("unchecked")
		Set<Object> val = (Set<Object>) cls.getConstructor(int.class).newInstance(n);
		for (int i = 0; i < n; i++) {
			val.add(decode(jarr.get(i), par.curjfld.generic()[0], par));
		}
		return val;
	}

	private static Object decodeEnum(JsonElement elem, Class<?> cls) throws IllegalArgumentException, JsonException {
		if (elem.isJsonNull())
			return null;
		Object[] constants = cls.getEnumConstants();
		if (elem.getAsJsonPrimitive().isNumber()) {
			int ord = elem.getAsInt();
			return constants[ord];
		}
		String name = getString(elem);
		for (Object constant : constants)
			if (constant.toString().equals(name))
				return constant;
		throw new IllegalArgumentException("No constant with text " + elem.getAsString() + " found for " + cls);
	}

	private static Object getArray(Class<?> cls, int n, JsonDecoder par) throws Exception {
		if (par != null && par.curjfld != null && par.curjfld.gen() == JsonField.GenType.FILL) {
			if (par.curfld == null || par.obj == null)
				throw new JsonException(true, par, "No enclosing object");
			return par.curfld.get(par.obj);
		} else
			return Array.newInstance(cls, n);
	}

	private static Object getGlobal(JsonDecoder par, Class<?> cls) {
		for (JsonDecoder dec = par; dec != null; dec = dec.par)
			if (cls.isInstance(dec.obj))
				return dec.obj;
		return null;
	}

	private static Object inject(JsonDecoder par, JsonObject jobj, Class<?> cls, Object pre) throws Exception {
		return new JsonDecoder(par, jobj, cls, pre == null ? cls.newInstance() : pre).obj;
	}

	private final JsonDecoder par;
	private final JsonObject jobj;
	private final Object obj;
	private final Class<?> tarcls;
	private final JsonClass tarjcls;
	private Class<?> curcls;
	private JsonClass curjcls;
	private Field curfld;
	protected JsonField curjfld;
	private int index = 0;

	private JsonDecoder(JsonDecoder parent, JsonObject json, Class<?> cls, Object pre) throws Exception {
		par = parent == null ? current : parent;
		jobj = json;
		obj = pre;
		tarcls = cls;
		tarjcls = cls.getAnnotation(JsonClass.class);
		current = getInvoker();
		decode(tarcls);
		current = par;
	}

	private void decode(Class<?> cls) throws Exception {
		if (cls.getSuperclass().getAnnotation(JsonClass.class) != null)
			decode(cls.getSuperclass());
		curcls = cls;
		curjcls = cls.getAnnotation(JsonClass.class);
		if (curjcls == null)
			throw new JsonException(true, obj, curcls + " has no annotation");
		decodeFields(cls, false);
		Method oni = null;
		for (Method m : cls.getDeclaredMethods()) {
			if (m.getAnnotation(OnInjected.class) != null)
				if (oni == null)
					oni = m;
				else
					throw new JsonException(true, obj, "Duplicate OnInjected", m);
			if (m.getAnnotation(PostLoad.class) != null)
				declast.add(this);

			curjfld = m.getAnnotation(JsonField.class);
			if (curjfld == null || curjfld.io() == JsonField.IOType.W)
				continue;
			if (curjfld.io() == JsonField.IOType.RW)
				throw new JsonException(true, obj, "RW IOtype", m);
			if (m.getParameterTypes().length != 1)
				throw new JsonException(true, obj, m.getParameterTypes().length + " parameters, should be 1", m);
			String tag = curjfld.tag();
			if (tag.isEmpty())
				throw new JsonException(true, obj, "Has no tag", m);
			if (!jobj.has(tag))
				continue;
			JsonElement elem = jobj.get(tag);
			Class<?> ccls = m.getParameterTypes()[0];
			m.invoke(obj, decode(elem, ccls, getInvoker()));
		}
		if (oni != null)
			try {
				if (oni.getParameterCount() == 0)
					oni.invoke(obj);
				else
					oni.invoke(obj, jobj);
			} catch (Exception e) {
				throw new JsonException(obj, e);
			}
	}

	public void decodeFields(Class<?> cls, boolean last) throws Exception {
		boolean ok = last;
		Field[] fs = FieldOrder.getDeclaredFields(cls);
		for (Field f : fs) {
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			curjfld = f.getAnnotation(JsonField.class);
			if (curjfld == null && curjcls.noTag() == JsonClass.NoTag.LOAD)
				curjfld = JsonField.DEF;
			if (curjfld == null || curjfld.block() || curjfld.io() == JsonField.IOType.W)
				continue;
			if (curjfld.decodeLast() != last) {
				if (!ok)
					ok = declast.add(this);
				continue;
			}
			String tag = curjfld.tag();
			if (tag.isEmpty())
				tag = f.getName();
			if (!jobj.has(tag)) {
				if (!curjfld.defval().isEmpty())
					defVal(obj, f, curjfld.defval());
				continue;
			}
			JsonElement elem = jobj.get(tag);
			f.setAccessible(true);
			curfld = f;
			try {
				f.set(obj, decode(elem, f.getType(), getInvoker()));
			} catch (Exception e) {
				throw new JsonException(obj, e, f, elem);
			}
			curfld = null;
		}
		if (last)
			for (Method m : cls.getDeclaredMethods())
				try {
					if (m.getAnnotation(PostLoad.class) != null)
						if (m.getParameterCount() == 0)
							m.invoke(obj);
						else
							m.invoke(obj, jobj);
				} catch (Exception e) {
					throw new JsonException(obj, e);
				}
	}

	private JsonDecoder getInvoker() {
		return tarjcls.bypass() ? par : this;
	}

	private void defVal(Object val, Field f, String str) {
		if (Modifier.isFinal(f.getModifiers()) || str.startsWith("this.") || str.equals("null") || str.equals("isEmpty"))
			return;
		try {
			f.setAccessible(true);
			if (f.getType() == byte.class)
				f.setByte(val, Byte.parseByte(str));
			else if (f.getType() == int.class)
				f.setInt(val, Integer.parseInt(str));
			else if (f.getType() == long.class)
				f.setLong(val, Long.parseLong(str));
			else if (f.getType() == float.class)
				f.setFloat(val, Float.parseFloat(str));
			else if (f.getType() == double.class)
				f.setDouble(val, Double.parseDouble(str));
			else if (f.getType() == boolean.class)
				f.setBoolean(val, Boolean.parseBoolean(str));
			else if (f.getType() == String.class)
				f.set(val, str);
			else if (str.contains(" ")) {
				Object nvar = f.get(val);
				Field nf = nvar.getClass().getField(str.substring(0, str.indexOf(" ")));
				defVal(nvar, nf, str.substring(str.indexOf(" ") + 1));
			}
		} catch (Exception e) {
			e.printStackTrace();//Nothing else
		}
	}
}