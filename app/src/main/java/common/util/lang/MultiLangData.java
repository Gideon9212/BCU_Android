package common.util.lang;

import common.CommonStatic;
import common.CommonStatic.Lang;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.Data;
import org.jetbrains.annotations.NotNull;

import java.util.TreeMap;

@JsonClass(read = JsonClass.RType.FILL)
public class MultiLangData extends Data {
    @JsonField(generic = {Lang.Locale.class, String.class})
    private final TreeMap<Lang.Locale, String> dat = new TreeMap<>();

    @JsonClass.JCConstructor
    public MultiLangData() {
    }

    public boolean put(String data) {
        return put(langs()[0], data);
    }

    /**
     * Handles putting a name and removing duplicate/identical names in other languages.
     * @param lang The language index
     * @param data The string
     * @return True if the value in the given lang changed
     */
    public boolean put(Lang.Locale lang, String data) {
        if (data != null && !data.isEmpty() && (lang == langs()[0] || !toString().equals(data))) {
            String old = dat.put(lang, data);
            return old == null || !old.equals(data);
        }
        return dat.remove(lang) != null;
    }

    public void remove(Lang.Locale lang) {
        dat.remove(lang);
    }

    public String get(Lang.Locale lang) {
        String temp = dat.get(lang);
        if (temp != null)
            return temp;
        return toString();
    }

    public void replace(String olds, String news) {
        dat.replaceAll((k, v) -> v.replace(olds, news));
    }

    @NotNull
    @Override
    public String toString() {
        Lang.Locale[] langs = langs();

        String temp;
        for (Lang.Locale lang : langs)
            if (dat.containsKey(lang)) {
                temp = dat.get(lang);
                if (temp != null)
                    return temp;
            }
        return "";
    }

    public Lang.Locale getGrabbedLocale() {
        Lang.Locale[] langs = langs();

        String temp;
        for (Lang.Locale lang : langs)
            if (dat.containsKey(lang)) {
                temp = dat.get(lang);
                if (temp != null)
                    return lang;
            }
        return Lang.Locale.EN;
    }

    private static Lang.Locale[] langs() {
        return CommonStatic.getConfig().langs;
    }

    public void overwrite(MultiLangData ans) { //Replaces all values with the given MLD's values
        for (Lang.Locale lang : ans.dat.keySet())
            dat.put(lang, ans.dat.get(lang));
    }

    public boolean empty() {
        return dat.isEmpty();
    }
}