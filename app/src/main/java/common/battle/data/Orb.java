package common.battle.data;

import common.CommonStatic;
import common.CommonStatic.BCAuxAssets;
import common.pack.Identifier;
import common.system.VImg;
import common.system.files.VFile;
import common.util.Data;
import common.util.anim.ImgCut;
import common.util.unit.Form;
import common.util.unit.Unit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Orb extends Data {

	public static final byte[] orbTrait = {
			Data.TRAIT_RED, Data.TRAIT_FLOAT, Data.TRAIT_BLACK, Data.TRAIT_METAL, Data.TRAIT_ANGEL, Data.TRAIT_ALIEN,
			Data.TRAIT_ZOMBIE, Data.TRAIT_RELIC, Data.TRAIT_WHITE, Data.TRAIT_EVA, Data.TRAIT_WITCH, Data.TRAIT_DEMON
	};

	public static void read() {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		try {
			Queue<String> traitData = VFile.readLine("./org/data/equipment_attribute.csv");

			byte key = 0;

			for (String line : traitData) {
				if (line == null || line.startsWith("//") || line.isEmpty())
					continue;

				String[] strs = line.trim().split(",");

				int value = 0;

				for (int i = 0; i < strs.length; i++) {
					if(strs.length != orbTrait.length)
						continue;

					int t = CommonStatic.parseIntN(strs[i]);
					if (t == 1)
						value |= 1 << orbTrait[i];
				}

				aux.DATA.put(key, value);

				key++;
			}

			String data = new String(VFile.get("./org/data/equipmentlist.json").getData().getBytes(), StandardCharsets.UTF_8);

			JSONObject jdata = new JSONObject(data);
			JSONArray lists = jdata.getJSONArray("ID");

			for (int i = 0; i < lists.length(); i++) {
				if (!(lists.get(i) instanceof JSONObject))
					continue;

				JSONObject obj = (JSONObject) lists.get(i);
				byte trait = (byte)obj.getInt("attribute");
				byte type = (byte)obj.getInt("content");
				byte grade = (byte)obj.getInt("gradeID");

				Map<Integer, List<Byte>> orb;

				if(aux.ORB.containsKey(type))
					orb = aux.ORB.get(type);
				else
					orb = new TreeMap<>();

				List<Byte> grades;

				if(orb.containsKey(aux.DATA.get(trait)))
					grades = orb.get(aux.DATA.get(trait));
				else
					grades = new ArrayList<>();

				if(!grades.contains(grade))
					grades.add(grade);

				orb.put(aux.DATA.get(trait), grades);

				aux.ORB.put(type, orb);
			}

			Queue<String> units = VFile.readLine("./org/data/equipmentslot.csv");

			for (String line : units) {
				if (line == null || line.startsWith("//") || line.isEmpty()) {
					continue;
				}

				String[] strs = line.trim().split(",");

				if (strs.length != 2 && strs.length != 4) {
					continue;
				}

				int id = CommonStatic.parseIntN(strs[0]);
				int slots = CommonStatic.parseIntN(strs[1]);

				Unit u = (Unit) Identifier.parseInt(id, Unit.class).get();

				if (u == null || u.forms.length < 3)
					continue;
				Orb o = strs.length == 2 ? new Orb(slots) : new Orb(slots, new int[] { CommonStatic.parseIntN(strs[2]), CommonStatic.parseIntN(strs[3]) });
				for (int i = 2; i < u.forms.length; i++) {
					Form f = u.forms[i];
					if (f == null)
						break;
					f.orbs = o;
				}
			}

			String pre = "./org/page/orb/equipment_";
			VImg type = new VImg(pre + "effect.png");
			ImgCut it = ImgCut.newIns(pre + "effect.imgcut");
			aux.TYPES = it.cut(type.getImg());

			VImg trait = new VImg(pre + "attribute.png");
			ImgCut itr = ImgCut.newIns(pre + "attribute.imgcut");
			aux.TRAITS = itr.cut(trait.getImg());

			VImg grade = new VImg(pre + "grade.png");
			ImgCut ig = ImgCut.newIns(pre + "grade.imgcut");
			aux.GRADES = ig.cut(grade.getImg());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static int reverse(int value) {
		Map<Byte, Integer> DATA = CommonStatic.getBCAssets().DATA;
		for (byte n : DATA.keySet()) {
			int v = DATA.get(n);
			if (DATA.get(n) != null && v == value)
				return n;
		}
		return -1;
	}

	public static int traitToOrb(int trait) {
		for(int i = 0; i < orbTrait.length; i++) {
			if(orbTrait[i] == trait)
				return 1 << i;
		}
		return -1;
	}

	private final int slots;
	private final int[] limit;

	public Orb(int slots) {
		this.slots = slots;

		if(slots == -1)
			this.limit = null;
		else
			this.limit = new int[slots];
	}

	public Orb(int slots, int[] limit) {
		this.slots = slots;

		if(slots != limit.length) {
			System.out.println("W/Orb - Desynced number of slot and level limit data : " + slots + " -> " + Arrays.toString(limit));

			int[] temp = new int[slots];

			System.arraycopy(limit, 0, temp, 0, temp.length);

			this.limit = temp;
		} else
			this.limit = limit;
	}

	public int getAtk(int grade, int atk) {
		return ORB_ATK_MULTI[grade] * atk / 100;
	}

	public int getRes(int grade, int atk) {
		return (100-ORB_RES_MULTI[grade]) * atk / 100;
	}

	public int getSlots() {
		return slots;
	}

	public int[] getLimits() {
		return limit;
	}
}
