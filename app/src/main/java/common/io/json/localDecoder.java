package common.io.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.CommonStatic;
import common.pack.Context;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * Behaves like jsondecoder for local methods that can't have annotations
 */
public class localDecoder {

    static class localField {
        enum GenType {
            SET, FILL, GEN
        }
        Class<?>[] alias = {};
        boolean block;
        GenType gen = GenType.SET;
        String generator = "";
        Class<?>[] generic = {};
        String tag = "";
        boolean usePool = false;
        boolean isNull = false;
        boolean decodeLast = false;

        public void setJProperties(JsonField jf) {
            if (jf == null) {
                isNull = true;
                return;
            }
            isNull = false;
            alias = jf.alias();
            block = jf.block() || jf.io() == JsonField.IOType.W;
            if (jf.gen() == JsonField.GenType.FILL)
                gen = GenType.FILL;
            else if (jf.gen() == JsonField.GenType.GEN)
                gen = GenType.GEN;
            else
                gen = GenType.SET;
            generator = jf.generator();
            generic = jf.generic();
            tag = jf.tag();
            decodeLast = jf.decodeLast();
        }

        static class Handler {
            public final List<Object> list = new ArrayList<>();
            public Handler(JsonArray jarr, Class<?> cls, localDecoder dec) throws Exception {
                int n = jarr.size();
                if (dec.locfld.generic.length == 1)
                    cls = dec.locfld.generic[0];
                for (int i = 0; i < n; i++)
                    list.add(dec.decode(jarr.get(i), cls));
            }
            public int add(Object o) {
                if (o == null)
                    return -1;
                for (int i = 0; i < list.size(); i++)
                    if (list.get(i) == o) // hard comparison
                        return i;
                list.add(o);
                return list.size() - 1;
            }
            public Object get(int i) {
                return i == -1 ? null : list.get(i);
            }
        }
    }

    private final localDecoder par;
    private final JsonObject jobj;
    private final Class<?> tarcls;
    private final JsonClass tarjcls;
    private final Object obj;
    private Field curfld;
    private final localField locfld = new localField();
    private final String name; //Only used for errors
    private int index = 0;

    /**
     * Creates a local, null-safe decoder to decode variables without annotations. Use any of the set functions before decoding to set up the "annotation"
     * @param json The jsonobject to decode
     * @param cls The class the decoded object belongs to
     * @param obj Object that acts as the parent for this object
     */
    public localDecoder(JsonElement json, Class<?> cls, Object obj) {
        this(json, cls, obj, "");
    }
    /**
     * Creates a local decoder to decode variables without annotations. Use any of the set functions before decoding to set up the "annotation"
     * @param json The jsonobject to decode
     * @param cls The class the decoded object belongs to
     * @param obj Object that acts as the parent for this object
     * @param name For debugging purposes
     */
    public localDecoder(JsonElement json, Class<?> cls, Object obj, String name) {
        par = null;
        jobj = json == null || json.isJsonNull() ? null : json.getAsJsonObject();
        tarcls = cls;
        tarjcls = null;
        this.name = name;
        this.obj = obj;
    }

    private localDecoder(localDecoder par, JsonObject json, Class<?> cls, Object obj, String name) throws Exception {
        this.par = par;
        jobj = json;
        tarcls = cls;
        tarjcls = cls.getAnnotation(JsonClass.class);
        this.name = name;
        this.obj = obj;
        decode(tarcls);
    }

    /**
     * Sets gen type to fill or gen. set isn't an option since it's the default one
     * @param gen Fill or Gen
     * @return this
     */
    public localDecoder setGen(boolean gen) {
        locfld.gen = gen ? localField.GenType.GEN : localField.GenType.FILL;
        return this;
    }

    public localDecoder setPool(boolean pool) {
        locfld.usePool = pool;
        return this;
    }

    public localDecoder setGeneric(Class<?>... cls) {
        locfld.generic = cls;
        return this;
    }

    public localDecoder setAlias(Class<?>... cls) {
        locfld.alias = cls;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T decode() {
        return (T) CommonStatic.ctx.noticeErr(() -> decode(jobj, tarcls), Context.ErrType.CORRUPT, "Error decoding " + name);
    }

    private void decode(Class<?> cls) throws Exception {
        if (cls.getSuperclass().getAnnotation(JsonClass.class) != null)
            decode(cls.getSuperclass());
        JsonClass curjcls = cls.getAnnotation(JsonClass.class);
        if (curjcls == null)
            throw new JsonException(true, cls, "No JsonClass annotation");

        Field[] fs = FieldOrder.getDeclaredFields(cls);
        for (Field f : fs) {
            if (Modifier.isStatic(f.getModifiers()))
                continue;
            locfld.setJProperties(f.getAnnotation(JsonField.class));
            if (locfld.isNull && curjcls.noTag() == JsonClass.NoTag.LOAD)
                locfld.setJProperties(JsonField.DEF);
            if (locfld.isNull || locfld.block)
                continue;
            if (locfld.tag.length() == 0)
                locfld.tag = f.getName();
            if (!jobj.has(locfld.tag))
                continue;
            JsonElement elem = jobj.get(locfld.tag);
            f.setAccessible(true);
            curfld = f;
            try {
                f.set(obj, getInvoker().decode(elem, f.getType()));
            } catch (Exception e) {
                throw new JsonException(cls, e, f, elem);
            }
            curfld = null;
        }
        Method oni = null, lastin = null;
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getAnnotation(JsonDecoder.OnInjected.class) != null)
                if (oni == null)
                    oni = m;
                else
                    throw new JsonException(true, obj, "Duplicate OnInjected. " + oni + " already exists", m);
            if (m.getAnnotation(JsonDecoder.PostLoad.class) != null)
                if (lastin == null)
                    lastin = m; //Toggle whether to actually PostLoad may be added in the future
                else
                    throw new JsonException(true, obj, "Duplicate PostLoad. " + lastin + " already exists", m);
            locfld.setJProperties(m.getAnnotation(JsonField.class));
            if (locfld.isNull || locfld.block)
                continue;
            if (m.getParameterTypes().length != 1)
                throw new JsonException(true, obj, "Has " + m.getParameterTypes().length + " parameters, should be 1", m);
            if (locfld.tag.length() == 0)
                throw new JsonException(true, obj, m.getName() + " must have a tag", m);
            if (!jobj.has(locfld.tag))
                continue;
            JsonElement elem = jobj.get(locfld.tag);
            Class<?> ccls = m.getParameterTypes()[0];
            m.invoke(obj, getInvoker().decode(elem, ccls));
        }
        if (oni != null)
            if (oni.getParameterCount() == 0)
                oni.invoke(obj);
            else
                oni.invoke(obj, jobj);
        if (lastin != null)
            if (lastin.getParameterCount() == 0)
                lastin.invoke(obj);
            else
                lastin.invoke(obj, jobj);
    }

    private Object decode(JsonElement elem, Class<?> cls) throws Exception {
        if (elem == null || elem.isJsonNull())
            return null;
        if (JsonElement.class.isAssignableFrom(cls))
            return elem;
        JsonDecoder.Decoder dec = JsonDecoder.REGISTER.get(cls);
        if (dec != null)
            return dec.decode(elem);
        if (cls.isArray())
            return decodeArray(elem, cls);
        if (List.class.isAssignableFrom(cls))
            return decodeList(elem, cls);
        if (Map.class.isAssignableFrom(cls))
            return decodeMap(elem, cls);
        if (Set.class.isAssignableFrom(cls))
            return decodeSet(elem, cls);
        if (Enum.class.isAssignableFrom(cls))
            return decodeEnum(elem, cls);
        // alias
        if (locfld.alias.length > index) {
            Class<?> alias = locfld.alias[index];
            if (alias != cls && alias != void.class) {
                Object input = decode(elem, alias);
                for (Method m : alias.getDeclaredMethods())
                    if (m.getAnnotation(JsonClass.JCGetter.class) != null) {
                        Class<?> ret = m.getReturnType();
                        if (cls == ret || cls.isAssignableFrom(ret) || ret.isAssignableFrom(cls))
                            return m.invoke(input);
                    }
                throw new JsonException(true, locfld, "no JCGetter present: " + alias + "->" + cls);
            }
        }
        // fill existing object
        if (locfld.gen == localField.GenType.FILL) {
            Object val = curfld.get(obj);
            if (cls.getAnnotation(JsonClass.class) != null)
                return inject(elem.getAsJsonObject(), cls, val);
            return val;
        }
        // generator
        if (locfld.gen == localField.GenType.GEN) {
            Class<?> ccls = obj.getClass();
            // default generator
            if (locfld.generator.length() == 0) {
                Constructor<?> cst = null;
                for (Constructor<?> ci : cls.getDeclaredConstructors())
                    if (ci.getParameterTypes().length == 1 && ci.getParameterTypes()[0].isAssignableFrom(ccls))
                        cst = ci;
                if (cst == null)
                    throw new JsonException(true, locfld, "No constructor found in " + cls + " that takes " + ccls + " as parameter");
                Object val = cst.newInstance(obj);
                return inject(elem.getAsJsonObject(), cls, val);
            }
            // functional generator
            Method m = ccls.getMethod(locfld.generator, Class.class, JsonElement.class);
            return m.invoke(obj, cls, elem);
        }
        if (elem.isJsonObject() && elem.getAsJsonObject().has("_class"))
            cls = Class.forName(elem.getAsJsonObject().get("_class").getAsString());
        if (cls.getAnnotation(JsonClass.class) != null)
            return decodeObject(elem, cls);
        throw new JsonException(true, cls, "Not possible to generate");
    }

    protected List<Object> decodeList(JsonElement elem, Class<?> cls) throws Exception {
        if (locfld.generic.length != 1)
            throw new JsonException(true, elem, "In " + cls + ": Requires one class as generic provider");
        if (elem.isJsonNull())
            return null;
        @SuppressWarnings("unchecked")
        List<Object> val = (List<Object>) cls.newInstance();
        if (elem.isJsonObject() && locfld.usePool) {
            JsonArray pool = elem.getAsJsonObject().get("pool").getAsJsonArray();
            JsonArray data = elem.getAsJsonObject().get("data").getAsJsonArray();
            localField.Handler handler = new localField.Handler(pool, null, this);
            int n = data.size();
            for (int i = 0; i < n; i++)
                val.add(handler.get(data.get(i).getAsInt()));
            return val;
        }
        if (!elem.isJsonArray())
            throw new JsonException(true, elem, "Not an array");
        JsonArray jarr = elem.getAsJsonArray();
        int n = jarr.size();
        for (int i = 0; i < n; i++) {
            val.add(decode(jarr.get(i), locfld.generic[0]));
        }
        return val;
    }

    private Object getArray(Class<?> cls, int n) throws Exception {
        if (!locfld.isNull && locfld.gen == localField.GenType.FILL) {
            if (curfld == null || obj == null)
                throw new JsonException(true, locfld, "no enclosing object");
            return par.curfld.get(par.obj);
        } else
            return Array.newInstance(cls, n);
    }

    private Object decodeArray(JsonElement elem, Class<?> cls) throws Exception {
        Class<?> ccls = cls.getComponentType();
        if (elem.isJsonObject() && locfld.usePool) {
            JsonArray pool = elem.getAsJsonObject().get("pool").getAsJsonArray();
            JsonArray data = elem.getAsJsonObject().get("data").getAsJsonArray();
            localField.Handler handler = new localField.Handler(pool, ccls, this);
            int n = data.size();
            Object arr = getArray(ccls, n);
            for (int i = 0; i < n; i++)
                Array.set(arr, i, handler.get(data.get(i).getAsInt()));
            return arr;
        }
        if (!elem.isJsonArray())
            throw new JsonException(true, obj, "Element + " + elem + " on " + cls + " at " + ccls + " in " + this + " is not array");
        JsonArray jarr = elem.getAsJsonArray();
        int n = jarr.size();
        Object arr = getArray(ccls, n);
        for (int i = 0; i < n; i++)
            Array.set(arr, i, decode(jarr.get(i), ccls));
        return arr;
    }

    private Map<Object, Object> decodeMap(JsonElement elem, Class<?> cls) throws Exception {
        if (locfld.generic.length != 2)
            throw new JsonException(true, locfld, "generic data structure requires typeProvider tag");
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
            Object key = decode(obj.get("key"), locfld.generic[0]);
            index = 1;
            Object ent = decode(obj.get("val"), locfld.generic[1]);
            index = 0;

            if(key != null)
                val.put(key, ent);
        }
        return val;
    }

    private Object decodeObject(JsonElement elem, Class<?> cls) throws Exception {
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
                        return inject(elem.getAsJsonObject(), cls, val);
                    else
                        return val;
            }
            throw new JsonException(true, obj, "RType FILL requires GenType FILL or GEN, or implicit GenType FILL: " + cls + ", " + elem);
        } else if (jc.read() == JsonClass.RType.DATA)
            return inject(jobj, cls, null);
        else if (jc.read() == JsonClass.RType.MANUAL) {
            String func = jc.generator();
            if (func.length() == 0)
                throw new JsonException(true, cls, "no generate function");
            Method m = cls.getMethod(func, JsonElement.class);
            return m.invoke(null, jobj);
        } else
            throw new JsonException(true, cls, "Not possible to generate");
    }

    private Set<Object> decodeSet(JsonElement elem, Class<?> cls) throws Exception {
        if (locfld.generic.length != 1)
            throw new JsonException(true, locfld, "generic data structure requires typeProvider tag");
        if (elem.isJsonNull())
            return null;
        if (!elem.isJsonArray())
            throw new JsonException(true, elem, "this element is not array");
        JsonArray jarr = elem.getAsJsonArray();
        int n = jarr.size();
        @SuppressWarnings("unchecked")
        Set<Object> val = (Set<Object>) cls.getConstructor(int.class).newInstance(n);
        for (int i = 0; i < n; i++) {
            val.add(decode(jarr.get(i), locfld.generic[0]));
        }
        return val;
    }

    private static Object decodeEnum(JsonElement elem, Class<?> cls) throws IllegalArgumentException, JsonException {
        if (elem.isJsonNull())
            return null;
        Object[] constants = cls.getEnumConstants();
        String name = JsonDecoder.getString(elem);
        for (Object constant : constants)
            if (constant.toString().equals(name))
                return constant;
        throw new IllegalArgumentException("No constant with text " + elem.getAsString() + " found for " + cls);
    }

    private Object inject(JsonObject jobj, Class<?> cls, Object pre) throws Exception {
        return new localDecoder(this, jobj, cls, pre == null ? cls.newInstance() : pre, cls.getName()).obj;
    }

    private localDecoder getInvoker() {
        return tarjcls.bypass() ? par : this;
    }
}