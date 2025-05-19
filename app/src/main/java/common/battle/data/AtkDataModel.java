package common.battle.data;

import com.google.gson.JsonObject;
import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonClass.RType;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.PackData;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.system.BasedCopable;
import common.util.Data;
import common.util.stage.Music;
import common.util.unit.Trait;

@JsonClass(read = RType.FILL, noTag = NoTag.LOAD)
public class AtkDataModel extends Data implements MaskAtk, BasedCopable<AtkDataModel, CustomEntity> {

	@JsonField(block = true)
	public final CustomEntity ce;
	@JsonField(backCompat = JsonField.CompatType.FORK)
	public String str = "";
	public int atk, ld0, ld1, move;
	@JsonField(defval = "1")
	public int targ = TCH_N, dire = 1;
	@JsonField(defval = "-1")
	public int count = -1;
	@JsonField(backCompat = JsonField.CompatType.FORK, defval = "1")
	public int pre = 1;
	@JsonField(defval = "true")
	public boolean range = true;
	@JsonField(backCompat = JsonField.CompatType.FORK)
	public int alt;
	@JsonField(generic = Trait.class, alias = Identifier.class, backCompat = JsonField.CompatType.FORK, defval = "isEmpty")
	public SortedPackSet<Trait> traits = new SortedPackSet<>();//Gives attacks their own typings

	@JsonField(generic = Identifier.class, defval = "isEmpty")
	public SortedPackSet<Identifier<Music>> audios = new SortedPackSet<>();//Gives custom audio to attacks

	@JsonField(defval = "isBlank")
	public Proc proc = Proc.blank();

	public AtkDataModel(CustomEntity ent) {
		ce = ent;
		checkAvail();
	}

	protected AtkDataModel(CustomEntity ene, AtkDataModel adm) {
		ce = ene;
		str = adm.str;
		checkAvail(adm.str);
		atk = adm.atk;
		pre = adm.pre;
		ld0 = adm.ld0;
		ld1 = adm.ld1;
		range = adm.range;
		traits = new SortedPackSet<>(adm.traits);
		traits.removeIf(t -> !(t.id.pack.equals(Identifier.DEF) || t.id.pack.equals(ce.getPack().getID().pack) || UserProfile.getUserPack(ce.getPack().getID().pack).desc.dependency.contains(t.id.pack)));
		dire = adm.dire;
		count = adm.count;
		targ = adm.targ;
		alt = adm.alt;
		move = adm.move;
		proc = adm.proc.clone();
		audios = new SortedPackSet<>(adm.audios);
	}

	protected AtkDataModel(CustomEntity ene, MaskEntity me, int i) {
		ce = ene;
		checkAvail("copied");
		MaskAtk am = me.getAtkModel(0, i);
		proc = am.getProc().clone();
		ld0 = am.getShortPoint();
		ld1 = am.getLongPoint();
		pre = am.getPre();
		atk = am.getAtk();
		range = am.isRange();
		dire = am.getDire();
		count = am.loopCount();
		targ = am.getTarget();
		alt = am.getAltAbi();
		move = am.getMove();
	}

	@Override
	public AtkDataModel clone() {
		return new AtkDataModel(ce, this);
	}

	@Override
	public AtkDataModel copy(CustomEntity nce) {
		return new AtkDataModel(nce, this);
	}

	@Override
	public int getAltAbi() {
		return alt;
	}

	@Override
	public int getAtk() {
		return atk;
	}

	@Override
	public int getPre() {
		return pre;
	}

	@Override
	public int getDire() {
		return dire;
	}

	@Override
	public int getLongPoint() {
		return ld1;
	}

	@Override
	public int getMove() {
		return move;
	}

	@Override
	public SortedPackSet<Trait> getATKTraits() { return traits; }

	@Override
	public Proc getProc() {
		if (ce.rep != this && ce.common)
			return ce.rep.getProc();
		return proc;
	}

	@Override
	public int getShortPoint() {
		return ld0;
	}

	@Override
	public int getTarget() {
		return targ;
	}

	@Override
	public boolean isRange() {
		return range;
	}

	@Override
	public int loopCount() {
		return count;
	}

	@Override
	public String toString() {
		int[] ind = indexOf();
		if (ind.length == 0)
			return str;
		String name = (ind[1] + 1) + " - " + str;
		int pre = 0;
		for (int i = 0; i <= ind[1]; i++) {
			pre += ce.getAtks(ind[0])[i].getPre();
			if (pre >= ce.getAnimLen(ind[0])) {
				name += " (out of range)";
				break;
			}
		}
		return name;
	}

	@Override
	public String getName() {
		return str;
	}

	public void checkAvail() {
		checkAvail(str);
	}

	public void checkAvail(String str) {
		if (ce.hits == null || ce.hits.isEmpty())
			return;
		int[] atkInd = indexOf();
		if (atkInd.length == 0)
			return;

		for (AtkDataModel adm : ce.hits.get(atkInd[0]))
			if (adm.str.equals(str))
				str += "'";
		this.str = str;
	}

	public int[] indexOf() {
		for (int j = 0;j < ce.hits.size(); j++) {
			if (ce.getAtks(j) == null)
				continue;
			for (int i = 0; i < ce.getAtks(j).length; i++)
				if (ce.getAtks(j)[i] == this)
					return new int[]{j, i};
		}
		return new int[0];
	}

	@Override
	public boolean isLD() {
		return (ld0 > 0 || ld1 < 0) && !isOmni();
	}

	@Override
	public boolean isOmni() {
		return ld0 * ld1 < 0 || (ld0 == 0 && ld1 > 0) || (ld0 < 0 && ld1 == 0);
	}

	@Override
	public Identifier<Music> getAudio() {
		if (audios.isEmpty())
			return null;
		return audios.get(audios.size() == 1 ? 0 : (int)(Math.random() * audios.size()));
	}

	@SuppressWarnings("unchecked")
	@JsonDecoder.OnInjected
	public void onInjected(JsonObject jobj) {
		if (proc == null)
			proc = Proc.blank();
		if (jobj.has("specialTrait")) {
			boolean spTrait = jobj.get("specialTrait").getAsBoolean();
			if ((ce instanceof CustomUnit && spTrait && dire == -1) || (ce instanceof CustomEnemy && ((spTrait && dire == 1) || (!spTrait && dire == -1))))
				traits.addAll(ce.traits);
		}
		if (jobj.has("audio"))
			audios.add((Identifier<Music>)JsonDecoder.decode(jobj.get("audio"), Identifier.class));
		if (jobj.has("audio1"))
			audios.add((Identifier<Music>)JsonDecoder.decode(jobj.get("audio1"), Identifier.class));
		if (proc.WARP.dis_1 < proc.WARP.dis)
			proc.WARP.dis_1 = proc.WARP.dis;
	}

	@JsonDecoder.PostLoad
	public void postLoad(JsonObject jobj) {
		if (jobj.has("specialTrait")) {
			boolean spTrait = jobj.get("specialTrait").getAsBoolean();
			if (ce instanceof CustomEnemy && (spTrait && dire == -1)) {
				traits.addAll(UserProfile.getBCData().traits.getList().subList(TRAIT_RED, TRAIT_BARON));
				PackData.UserPack p = (PackData.UserPack)ce.getPack().getPack();
				traits.addAll(p.traits.getList());
				for (String dep : p.desc.dependency)
					traits.addAll(UserProfile.getUserPack(dep).traits.getList());
			}
		}
		if (proc.WARP.prob > 0 && UserProfile.isOlderPack((PackData.UserPack)ce.getPack().getPack(), "0.7.4.2"))
			proc.WARP.dis_1 = proc.WARP.dis;
	}

	@JsonField(tag = "str", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public String Ustr() {
		return str.startsWith("combo") && pre == 0 ? str.replace("combo", "[c]") : str;
	}

	@JsonField(tag = "pre", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public int Upre() {
		return str.startsWith("combo") && pre == 0 ? 1 : pre;
	}

	@JsonField(tag = "alt", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public int Ualt() {
		int nabi = 0;
		int abiSub = 3;
		for (int i = 0; i < ABI_TOT; i++) {
			if (i == 2)
				abiSub++;
			else if (i == 11)
				abiSub += 2;
			if (((alt >> i) & 1) > 0)
				nabi |= 1 << i + abiSub;
		}
		return nabi;
	}

	@JsonField(tag = "specialTrait", io = JsonField.IOType.W, backCompat = JsonField.CompatType.UPST)
	public boolean spTr() {
		if (dire == 0 || traits.isEmpty())
			return false;
		boolean ig = dire == (ce instanceof CustomEnemy ? -1 : 1);

		if (!traits.containsAll(UserProfile.getBCData().traits.getList()))
			return !ig;
		PackData.UserPack p = (PackData.UserPack)ce.getPack().getPack();
		if (!traits.containsAll(p.traits.getList()))
			return !ig;
		for (String dep : p.desc.dependency)
			if (!traits.containsAll(UserProfile.getUserPack(dep).traits.getList()))
				return !ig;
		return ig;
	}
}
