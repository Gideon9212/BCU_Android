package common.util.unit;

import common.CommonStatic;
import common.battle.BasisLU;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.IndexContainer;
import common.pack.PackData;
import common.pack.UserProfile;
import common.system.files.VFile;
import common.util.Data;
import common.util.lang.MultiLangCont;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

@IndexContainer.IndexCont(PackData.class)
@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class Combo extends Data implements IndexContainer.Indexable<IndexContainer, Combo>, Comparable<Combo> {

	public static void readFile() {
		CommonStatic.BCAuxAssets aux = CommonStatic.getBCAssets();
		PackData.DefPack data = UserProfile.getBCData();
		Queue<String> qs = VFile.readLine("./org/data/NyancomboData.csv");
		int i = 0;
		for (String str : qs) {
			if (str.length() < 20)
				continue;
			String[] strs = str.trim().split(",");
			if (Integer.parseInt(strs[1]) <= 0)
				continue;
			Combo c = new Combo(Identifier.parseInt(i++, Combo.class), strs);
			data.combos.add(c);
		}

		qs = VFile.readLine("./org/data/NyancomboParam.tsv");
		for (i = 0; i < C_TOT; i++) {
			String[] strs = qs.poll().trim().split("\t");
			if (strs.length < 5)
				continue;
			for (int j = 0; j < 5; j++) {
				aux.values[i][j] = Integer.parseInt(strs[j]);
			}
		}
		qs = VFile.readLine("./org/data/NyancomboFilter.tsv");
		aux.filter = new int[qs.size()][];
		for (i = 0; i < aux.filter.length; i++) {
			String[] strs = qs.poll().trim().split("\t");
			aux.filter[i] = new int[strs.length];
			for (int j = 0; j < strs.length; j++)
				aux.filter[i][j] = Integer.parseInt(strs[j]);
		}
	}

	@JsonClass.JCIdentifier
	@JsonField
	public Identifier<Combo> id;

	@JsonField
	public int lv, type;

	@JsonField(alias = AbForm.AbFormJson.class)
	public Form[] forms;

	@JsonField(defval = "new combo")
	public String name = "new combo";

	@JsonClass.JCConstructor
	public Combo() {
		id = null;
	}

	protected Combo(Identifier<Combo> ID, String[] strs) {
		id = ID;
		name = strs[0];
		int n;
		for (n = 0; n < 5; n++)
			if (Integer.parseInt(strs[2 + n * 2]) == -1)
				break;
		forms = new Form[n];
		for (int i = 0; i < n; i++) {
			Identifier<AbUnit> u = Identifier.parseInt(Integer.parseInt(strs[2 + i * 2]), Unit.class);
			forms[i] = u.get().getForms()[Integer.parseInt(strs[3 + i * 2])];
		}
		type = Integer.parseInt(strs[12]);
		lv = Integer.parseInt(strs[13]);
	}

	public Combo(Identifier<Combo> ID, Combo c) {
		id = ID;
		name = c.name;
		lv = c.lv;
		type = c.type;
		forms = new Form[c.forms.length];
	}

	public Combo(Identifier<Combo> ID, Form f) {
		id = ID;
		lv = 0;
		type = 0;
		forms = new Form[]{f};
	}

	@Override
	public String toString() {
		return id.toString() + " - " + getName();
	}

	@Override
	public Identifier<Combo> getID() {
		return id;
	}

	public String getName() {
		String n = MultiLangCont.get(this);
		if (n != null && n.length() > 0)
			return n;
		else if (name != null && name.length() > 0)
			return name;
		else
			return null;
	}

	public void setType(int t) {
		for (BasisLU blu : BasisLU.allLus())
			if (blu.lu.coms.contains(this)) {
				blu.lu.inc[type] -= CommonStatic.getBCAssets().values[type][lv];
				blu.lu.inc[t] += CommonStatic.getBCAssets().values[t][lv];
			}
		type = t;
	}

	public void setLv(int l) {
		for (BasisLU blu : BasisLU.allLus())
			if (blu.lu.coms.contains(this)) {
				blu.lu.inc[type] -= CommonStatic.getBCAssets().values[type][lv];
				blu.lu.inc[type] += CommonStatic.getBCAssets().values[type][l];
			}
		lv = l;
	}

	public void addForm(Form f) {
		forms = Arrays.copyOf(forms, forms.length + 1);
		forms[forms.length - 1] = f;
		updateLUs();
	}

	public void removeForm(int index) {
		Form[] formSrc = new Form[forms.length - 1];
		for (int i = 0, j = 0; i < forms.length; i++) {
			if (i != index)
				formSrc[j++] = forms[i];
		}
		forms = formSrc;
		updateLUs();
	}

	public boolean containsForm(Form f) {
		for (Form cf : forms)
			if (f.unit == cf.unit && f.fid >= cf.fid)
				return true;
		return false;
	}

	public void unload() {
		for (BasisLU blu : BasisLU.allLus()) {
			blu.lu.coms.remove(this);
			blu.lu.inc[type] -= CommonStatic.getBCAssets().values[type][lv];
			for (Form frm : forms)
				for (int i = 0; i < 5; i++)
					if (blu.lu.fs[0][i] instanceof Form && blu.lu.fs[0][i].unit() == frm.unit && blu.lu.fs[0][i].getFid() >= frm.fid)
						blu.lu.loc[i]--;
		}
	}

	private void updateLUs() {
		for (BasisLU blu : BasisLU.allLus())
			blu.lu.renewCombo(this, true);
	}

	@JsonDecoder.OnInjected
	public void onInjected() {
		boolean broken = false;
		for (Form form : forms)
			if (form == null) {
				broken = true;
				break;
			}
		if(broken) {
			List<Form> f = new ArrayList<>();
			for (Form form : forms)
				if (form != null)
					f.add(form);
			forms = f.toArray(new Form[0]);
		}
	}

	@Override
	public int compareTo(@NotNull Combo c) {
		return id.compareTo(c.id);
	}
}