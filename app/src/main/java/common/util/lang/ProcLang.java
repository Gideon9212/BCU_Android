package common.util.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.CommonStatic;
import common.io.json.FieldOrder;
import common.io.json.JsonClass;
import common.io.json.JsonClass.RType;
import common.io.json.JsonClass.WType;
import common.io.json.JsonDecoder;
import common.io.json.JsonEncoder;
import common.io.json.JsonField;
import common.io.json.JsonField.IOType;
import common.pack.UserProfile;
import common.util.Data;
import common.util.Data.Proc;
import common.util.Data.Proc.IntType;
import common.util.lang.LocaleCenter.Binder;
import common.util.lang.LocaleCenter.DisplayItem;
import common.util.lang.LocaleCenter.ObjBinder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

@JsonClass(read = RType.MANUAL, write = WType.CLASS, generator = "gen", serializer = "ser")
public class ProcLang {

	@JsonClass(read = RType.FILL)
	public static class ItemLang {
		@JsonField
		public String abbr_name, full_name, tooltip, format;

		private final String name;
		private final Class<?> cls;
		private final LinkedHashMap<String, DisplayItem> map = new LinkedHashMap<>();

		private ItemLang(String name, Class<?> cls) {
			this.cls = cls;
			this.name = name;
		}

		public Binder get(String proc) {
			try {
				return new ObjBinder(map.get(proc), name + "." + proc, this::getBinder);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public String[] list() {
			String[] ans = new String[map.size()];
			int i = 0;
			for (Entry<String, DisplayItem> ent : map.entrySet())
				ans[i++] = ent.getKey();
			return ans;
		}

		@JsonField(tag = "class", io = IOType.R)
		public void readClass(JsonElement elem) {
			fill("", cls, elem == null ? null : elem.getAsJsonObject());
		}

		@JsonField(tag = "class", io = IOType.W)
		public JsonObject writeClass() {
			JsonObject ans = new JsonObject();
			fill(ans, "", cls);
			return ans;
		}

		private void fill(JsonObject ans, String pre, Class<?> c) {
			for (Field f : FieldOrder.getFields(c)) {
				if (IntType.class.isAssignableFrom(f.getType()))
					fill(ans, f.getName() + ".", f.getType());
				else
					ans.add(pre + f.getName(), JsonEncoder.encode(map.get(pre + f.getName())));
			}
		}

		private void fill(String pre, Class<?> c, JsonObject obj) {
			for (Field f : FieldOrder.getFields(c)) {
				if (IntType.class.isAssignableFrom(f.getType())) {
					fill(f.getName() + ".", f.getType(), obj);
				} else {
					JsonElement elem = obj == null ? null : obj.get(pre + f.getName());
					DisplayItem pf = elem == null ? new DisplayItem() : JsonDecoder.decode(elem, DisplayItem.class);
					map.put(pre + f.getName(), pf);
				}
			}
		}

		private Binder getBinder(String proc) {
			return ProcLang.get().get(name).get(proc);
		}

	}

	public static class ProcLangStore {

		private final ProcLang[] langs = new ProcLang[CommonStatic.Lang.Locale.values().length];

		private ProcLang getLang() {
			return getLang(CommonStatic.getConfig().langs[0].ordinal());
		}

		private ProcLang getLang(int lang) {
			if (langs[lang] == null)
				Data.err(() -> read(lang));
			return langs[lang];
		}

		private void setLang(ProcLang lang, int l) {
			langs[l] = lang;
		}

	}

	public static void clear() {
		UserProfile.setStatic("ProcLangStore", null);
	}

	public static ProcLang gen(JsonElement elem) throws Exception {
		ProcLang ans = new ProcLang();
		JsonObject obj = elem == null ? null : elem.getAsJsonObject();
		for (Field f : Proc.getDeclaredFields()) {
			String name = f.getName();
			ItemLang item = new ItemLang(name, f.getType());
			if (obj != null && obj.has(name))
				JsonDecoder.inject(obj.get(name), ItemLang.class, item);
			else
				item.readClass(null);
			ans.map.put(name, item);
		}
		return ans;
	}

	public static ProcLang get() {
		return store().getLang();
	}

	public static ProcLang getWithLang(int lang) {
		return store().getLang(lang);
	}

	private static void read(int lang) throws Exception {
		InputStream f = CommonStatic.ctx.getLangFile("proc.json");

		JsonElement elem = JsonParser.parseReader(new InputStreamReader(f, StandardCharsets.UTF_8));
		f.close();
		ProcLang proc = JsonDecoder.decode(elem, ProcLang.class);
		store().setLang(proc, lang);
	}

	private static ProcLangStore store() {
		return UserProfile.getStatic("ProcLangStore", ProcLangStore::new);
	}

	private final Map<String, ItemLang> map = new LinkedHashMap<>();

	private ProcLang() {
	}

	public ItemLang get(int i) {
		return get(Proc.getDeclaredFields()[i].getName());
	}

	public ItemLang get(String str) {
		return map.get(str);
	}

	public JsonObject ser() {
		JsonObject obj = new JsonObject();
		for (Field f : Proc.getDeclaredFields()) {
			String name = f.getName();
			obj.add(name, JsonEncoder.encode(map.get(name)));
		}
		return obj;
	}

}