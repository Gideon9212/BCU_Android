package common.battle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonEncoder;
import common.io.json.JsonField;
import common.io.json.JsonField.GenType;
import common.io.json.JsonField.IOType;
import common.pack.Context;
import common.pack.Context.ErrType;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.system.Copable;
import common.util.unit.AbUnit;
import common.util.unit.Form;
import common.util.unit.Level;
import common.util.unit.Unit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@JsonClass
public class BasisSet extends Basis implements Copable<BasisSet> {

	public static BasisSet current() {
		return UserProfile.getStatic("BasisSet_current", BasisSet::def);
	}

	public static BasisSet def() {
		listRaw();
		return UserProfile.getStatic("BasisSet_default", BasisSet::new);
	}

	public static List<BasisSet> list() {
		def();
		return listRaw();
	}

	public static BasisSet[] getBackupSet(JsonElement element) {
		JsonArray arr = element.getAsJsonObject().getAsJsonArray("list");

		BasisSet[] sets = new BasisSet[arr.size()];

		for(int i = 0; i < arr.size(); i++) {
			sets[i] = JsonDecoder.decode(arr.get(i), BasisSet.class);
		}

		return sets;
	}

	/**
	 * Synchronize lineup's orb data with stat change
	 * @param u Current unit, must be custom unit or it won't do anything
	 */
	public static void synchronizeOrb(Unit u) {
		//No need to change BC unit's orb status
		if (u == null || u.id.pack.equals(Identifier.DEF))
			return;

		for(BasisSet set : list()) {
			for(BasisLU lu : set.lb) {
				for(Identifier<AbUnit> id : lu.lu.map.keySet()) {
					if(id.equals(u.id)) {
						Level l = lu.lu.map.get(id);

						if(l.getOrbs() != null) {
							int[][] orb = l.getOrbs();

							ArrayList<int[]> filteredOrb = new ArrayList<>();

							boolean str = false;
							boolean mas = false;
							boolean res = false;

							for(Form f : u.forms) {
								int atk = (int) f.du.getProc().DMGINC.mult;
								int def = (int) f.du.getProc().DEFINC.mult;
								str |= (atk > 100 && atk < 300) || (def > 100 && def < 400);
								mas |= atk >= 300 && atk < 500;
								res |= def >= 400 && def < 600;
							}

							for(int[] o : orb) {
								if(conditionalOrb(o[ORB_TYPE])) {
									if(o[ORB_TYPE] == ORB_STRONG && str)
										filteredOrb.add(o);
									else if(o[ORB_TYPE] == ORB_MASSIVE && mas)
										filteredOrb.add(o);
									else if(o[ORB_TYPE] == ORB_RESISTANT && res)
										filteredOrb.add(o);
								} else
									filteredOrb.add(o);
							}

							if(filteredOrb.isEmpty()) {
								l.setOrbs(null);
							} else {
								int[][] newOrb = new int[filteredOrb.size()][];

								for(int i = 0; i < newOrb.length; i++)
									newOrb[i] = filteredOrb.get(i);
								l.setOrbs(newOrb);
							}
						}
					}
				}
			}
		}
	}

	public static void read() {
		def();
		File lvs = CommonStatic.ctx.getUserFile("./levels.json");
		if (lvs.exists())
			try (Reader r = new InputStreamReader(new FileInputStream(lvs), StandardCharsets.UTF_8)) {
				JsonElement je = JsonParser.parseReader(r);
				r.close();
				CommonStatic.Preflvs jlvs = CommonStatic.getPrefLvs();
				JsonDecoder.inject(je, CommonStatic.Preflvs.class, jlvs);

				jlvs.uni.keySet().removeIf(id -> id.safeGet() == null);
			} catch (Exception e) {
				CommonStatic.ctx.noticeErr(e, ErrType.WARN, "failed to read pref lvs");
			}

		File f = CommonStatic.ctx.getUserFile("./basis.json");
		if (f.exists())
			try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
				JsonElement je = JsonParser.parseReader(r);
				r.close();
				JsonElement jel = je.getAsJsonObject().get("list");
				JsonDecoder.decode(jel, BasisSet[].class);
				int cur = je.getAsJsonObject().get("current").getAsInt();
				setCurrent(list().get(cur));
			} catch (Exception e) {
				CommonStatic.ctx.noticeErr(e, ErrType.WARN, "failed to read basis data");
			}
	}

	public static void setCurrent(BasisSet cur) {
		UserProfile.setStatic("BasisSet_current", cur);
	}

	public static void write() {
		File target = CommonStatic.ctx.getUserFile("./basis.json");
		File temp = CommonStatic.ctx.getUserFile("./.temp.basis.json");
		try (Writer w = new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8)) {
			Context.check(temp);
			List<BasisSet> list = list();
			int cur = list.indexOf(current());
			BasisSet[] arr = new BasisSet[list.size() - 1];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = list.get(i + 1);
				for (BasisLU b : arr[i].lb)
					b.lu.removeDefs();
			}
			JsonObject ans = new JsonObject();
			ans.add("list", JsonEncoder.encode(arr));
			ans.addProperty("current", cur);
			w.write(ans.toString());
			w.flush();
			w.close();
			Context.delete(target);
			temp.renameTo(target);

			for (BasisSet bas : arr)
				for (BasisLU b : bas.lb)
					b.lu.reAddDefs();
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.ERROR, "failed to save basis data");
		}

	}

	private static boolean conditionalOrb(int type) {
		return type == ORB_STRONG || type == ORB_MASSIVE || type == ORB_RESISTANT;
	}

	private static List<BasisSet> listRaw() {
		return UserProfile.getStatic("BasisSet_list", ArrayList::new);
	}

	@JsonField(gen = GenType.FILL)
	private final Treasure t;

	@JsonField(generic = BasisLU.class, gen = GenType.GEN)
	public final ArrayList<BasisLU> lb = new ArrayList<>();

	public BasisLU sele;

	public BasisSet() {
		if (listRaw().size() == 0)
			name = "temporary";
		else
			name = "set " + listRaw().size();
		t = new Treasure(this);
		setCurrent(this);
		lb.add(sele = new BasisLU(this));
		listRaw().add(this);
	}

	public BasisSet(BasisSet ref) {
		name = "set " + list().size();
		list().add(this);
		t = new Treasure(this, ref.t);
		setCurrent(this);
		for (BasisLU blu : ref.lb)
			lb.add(sele = new BasisLU(this, blu));
	}

	public BasisLU add(int ind) {
		if (ind == -1 || ind >= lb.size())
			return add();
		lb.add(ind, sele = new BasisLU(this));
		return sele;
	}

	public BasisLU add() {
		lb.add(sele = new BasisLU(this));
		return sele;
	}

	@Override
	public BasisSet copy() {
		return new BasisSet(this);
	}

	public BasisLU copyCurrent() {
		lb.add(sele = new BasisLU(this, sele));
		return sele;
	}

	/**
	 * BasisSet are used in data display, so cannot be effected by combo
	 */
	@Override
	public int getInc(int type) {
		return 0;
	}

	public BasisLU remove() {
		lb.remove(sele);
		return sele = lb.get(0);
	}

	@Override
	public Treasure t() {
		return t;
	}

	@JsonField(tag = "sele", io = IOType.R)
	public void zgen(int ind) {
		sele = lb.get(ind);
	}

	@JsonField(tag = "sele", io = IOType.W)
	public int zser() {
		return lb.indexOf(sele);
	}
}
