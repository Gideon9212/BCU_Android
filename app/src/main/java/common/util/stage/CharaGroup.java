package common.util.stage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.io.json.JsonClass;
import common.io.json.JsonClass.JCGeneric;
import common.io.json.JsonClass.JCIdentifier;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.IndexContainer.IndexCont;
import common.pack.IndexContainer.Indexable;
import common.pack.PackData;
import common.pack.PackData.UserPack;
import common.pack.SortedPackSet;
import common.util.Data;
import common.util.unit.AbForm;
import common.util.unit.AbUnit;
import common.util.unit.Form;
import common.util.unit.Unit;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

@IndexCont(PackData.class)
@JsonClass(noTag = NoTag.LOAD)
@JCGeneric(Identifier.class)
public class CharaGroup extends Data implements Indexable<PackData, CharaGroup>, Comparable<CharaGroup> {
	public String name = "";
	@JCIdentifier
	public final Identifier<CharaGroup> id;
	public int type = 0;

	@JsonField(generic = Form.class, alias = AbForm.AbFormJson.class, backCompat = JsonField.CompatType.FORK, defval = "isEmpty")
		public final SortedPackSet<Form> fset = new SortedPackSet<>();

	@JsonClass.JCConstructor
	public CharaGroup() {
		id = null;
	}

	public CharaGroup(Identifier<CharaGroup> id) {
		this.id = id;
	}

	public CharaGroup(CharaGroup cg) {
		this(cg.id, cg);
	}

	public CharaGroup(Identifier<CharaGroup> id, CharaGroup cg) {
		this(id);
		type = cg.type;
		fset.addAll(cg.fset);
	}

	public CharaGroup(int ID, int t, Identifier<AbUnit>[] units) {
		type = t;
		for (Identifier<AbUnit> uid : units) {
			AbUnit u = Identifier.get(uid);
			if (u != null)
				fset.addAll(Arrays.asList(u.getForms()));
		}
		id = Identifier.parseInt(ID, CharaGroup.class);
	}

	public boolean allow(Form f) {
		return type % 2 != 0 || (type == 0 && fset.contains(f)) || (type == 2 && !fset.contains(f));
	}

	public CharaGroup combine(CharaGroup cg) {
		CharaGroup ans = new CharaGroup(this);
		if (type == 0 && cg.type == 0)
			ans.fset.retainAll(cg.fset);
		else if (type == 0 && cg.type == 2)
			ans.fset.removeAll(cg.fset);
		else if (type == 2 && cg.type == 0) {
			ans.type = 0;
			ans.fset.addAll(cg.fset);
			ans.fset.removeAll(fset);
		} else if (type == 2 && cg.type == 2)
			ans.fset.addAll(cg.fset);
		return ans;
	}

	@Override
	public int compareTo(CharaGroup cg) {
		return id.compareTo(cg.id);
	}

	@Override
	public Identifier<CharaGroup> getID() {
		return id;
	}

	@Override
	public String toString() {
		return id + " - " + name;
	}

	public LinkedList<Object> used() {
		LinkedList<Object> objs = new LinkedList<>();
		UserPack mc = (UserPack) getCont();
		for (LvRestrict lr : mc.lvrs.getList())
			if (lr.cgl.containsKey(this))
				objs.add(lr);
		for (StageMap sm : mc.mc.maps) {
			for (Limit lim : sm.lim)
				if (lim.group == this)
					objs.add(lim);
			for (Stage st : sm.list)
				if (st.lim != null && st.lim.group == this)
					objs.add(st);
		}
		return objs;
	}

	@JsonDecoder.OnInjected
	public void onInjected(JsonObject jobj) {
		UserPack pack = (UserPack) getCont();
		if (pack.desc.FORK_VERSION < 1) {
			JsonArray jarr = jobj.get("set").getAsJsonArray();
			for (int i = 0; i < jarr.size(); i++) {
				String pacc = jarr.get(i).getAsJsonObject().get("pack").getAsString();
				int id = jarr.get(i).getAsJsonObject().get("id").getAsInt();
				Unit u = (Unit) new Identifier<>(pacc, Unit.class, id).get();
				if (u != null)
					Collections.addAll(fset, u.forms);
			}
		}
	}

	@JsonField(tag = "set", io = JsonField.IOType.W, alias = Identifier.class, backCompat = JsonField.CompatType.UPST)
	public SortedPackSet<Unit> getBGEff() {
		SortedPackSet<Unit> uset = new SortedPackSet<>();
		for (Form f : fset)
			uset.add(f.unit);
		return uset;
	}
}
