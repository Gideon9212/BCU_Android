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
import common.util.stage.CharaGroup;
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
		if (qs == null) {
			System.out.println("W/Combo::readFile - \"./org/data/NyancomboData.csv\" file hasn't been found");
			return;
		}

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
			for (int j = 0; j < 5; j++)
				aux.values[i][j] = Integer.parseInt(strs[j]);
		}
		aux.values[C_IMUWAVE] = new int[]{10, 30, 60, 100,-50};
		aux.values[C_DISCOUNT][4] = -10;
		qs = VFile.readLine("./org/data/NyancomboFilter.tsv");
		aux.filter = new int[qs.size()][];
		for (i = 0; i < aux.filter.length; i++) {
			String[] strs = qs.poll().trim().split("\t");
			aux.filter[i] = new int[strs.length];
			for (int j = 0; j < strs.length; j++)
				aux.filter[i][j] = Integer.parseInt(strs[j]);
		}
	}

	public static boolean unrestrictable(int type) {
		return (type >= C_C_INI && type <= C_RESP && type != C_RESP);//No point in restricting money/base combos
	}

	@JsonClass.JCIdentifier
	@JsonField
	public Identifier<Combo> id;

	@JsonField
	public int lv, type;

	@JsonField(alias = Identifier.class)
	public CharaGroup group;

	@JsonField(alias = AbForm.AbFormJson.class)
	public Form[] forms;

	@JsonField(gen = JsonField.GenType.GEN, defval = "this.allForms")
	public byte[] formRestriction;//0 = This form or higher only (Default), 1 = this form or lower only, 2 = exclusively this form

	@JsonField(defval = "1")
	public byte row = 1;//0 = Any row, 1 = 1st row only (Default), 2 = 2nd row only, 3 = Any but all must be on the same row

	@JsonField(defval = "new combo")
	public String name = "new combo";

	@JsonClass.JCConstructor
	public Combo() {
		id = null;
	}

	protected Combo(Identifier<Combo> ID, String[] strs) {
		id = ID;
		name = strs[0];
		if (Integer.parseInt(strs[2]) >= 0)
			group = Identifier.parseInt(Integer.parseInt(strs[2]), CharaGroup.class).get();
		int n;
		for (n = 0; n < 5; n++)
			if (Integer.parseInt(strs[3 + n * 2]) == -1)
				break;
		forms = new Form[n];
		formRestriction = new byte[n];
		for (int i = 0; i < n; i++) {
			Identifier<AbUnit> u = Identifier.parseInt(Integer.parseInt(strs[3 + i * 2]), Unit.class);
			forms[i] = u.get().getForms()[Integer.parseInt(strs[4 + i * 2])];
		}
		type = Integer.parseInt(strs[13]);
		lv = Integer.parseInt(strs[14]);
	}

	public Combo(Identifier<Combo> ID, Combo c) {
		id = ID;
		name = c.name;
		lv = c.lv;
		type = c.type;
		forms = new Form[c.forms.length];
		formRestriction = c.formRestriction.clone();
		group = c.group;
		row = c.row;
	}

	public Combo(Identifier<Combo> ID, Form f) {
		id = ID;
		lv = 0;
		type = 0;
		forms = new Form[]{f};
		formRestriction = new byte[1];
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
		if (n != null && !n.isEmpty())
			return n;
		else if (name != null && !name.isEmpty())
			return name;
		else
			return null;
	}


	public void setType(int t) {
		for (BasisLU blu : BasisLU.allLus())
			if (blu.lu.coms.contains(this)) {
				blu.lu.getCombosFor(group, false).inc[type] -= CommonStatic.getBCAssets().values[type][lv];
				blu.lu.getCombosFor(group, false).inc[t] += CommonStatic.getBCAssets().values[t][lv];
			}
		type = t;
		if (group != null && unrestrictable(t))
			setGroup(null);
	}

	public void setLv(int l) {
		for (BasisLU blu : BasisLU.allLus())
			if (blu.lu.coms.contains(this)) {
				blu.lu.getCombosFor(group, false).inc[type] -= CommonStatic.getBCAssets().values[type][lv];
				blu.lu.getCombosFor(group, false).inc[type] += CommonStatic.getBCAssets().values[type][l];
			}
		lv = l;
	}

	public void setGroup(CharaGroup ncg) {
		CharaGroup old = group;
		group = ncg;
		for (BasisLU blu : BasisLU.allLus())
			if (blu.lu.coms.contains(this)) {
				blu.lu.getCombosFor(old, false).inc[type] -= CommonStatic.getBCAssets().values[type][lv];
				blu.lu.getCombosFor(ncg, true).inc[type] += CommonStatic.getBCAssets().values[type][lv];
				blu.lu.validateIncs();
			}
	}

	public void addForm(Form f) {
		forms = Arrays.copyOf(forms, forms.length + 1);
		formRestriction = Arrays.copyOf(formRestriction, forms.length);
		forms[forms.length - 1] = f;
		updateLUs();
	}

	public void removeForm(int index) {
		Form[] formSrc = new Form[forms.length - 1];
		byte[] lims = new byte[formSrc.length];
		for (int i = 0, j = 0; i < forms.length; i++) {
			if (i == index)
				continue;
			lims[j] = formRestriction[i];
			formSrc[j++] = forms[i];
		}
		forms = formSrc;
		formRestriction = lims;
		updateLUs();
	}

	public boolean containsForm(Form f) {
		for (byte i = 0; i < forms.length; i++)
			if (f.unit == forms[i].unit && correctForm(i, f.fid))
				return true;
		return false;
	}
	public boolean correctForm(int ind, int fid) {
		byte res = formRestriction[ind];
		return fid == forms[ind].fid || (res == 0 && fid > forms[ind].fid) || (res == 1 && fid < forms[ind].fid);
	}

	public void unload() {
		for (BasisLU blu : BasisLU.allLus())
			blu.lu.removeCombo(this);
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
		if (lv == 5)
			lv = 3;
		if (formRestriction == null || formRestriction.length != forms.length)
			formRestriction = new byte[forms.length];
	}

	@JsonDecoder.PostLoad
	public void postLoad() {
		PackData.UserPack pk = UserProfile.getUserPack(id.pack);
		if (pk.desc.FORK_VERSION < 13 && type == C_IMUWAVE)
			lv = 3;
	}

	@Override
	public int compareTo(@NotNull Combo c) {
		return id.compareTo(c.id);
	}

	public boolean allForms() {
		for (byte r : formRestriction)
			if (r > 0)
				return false;
		return true;
	}
}