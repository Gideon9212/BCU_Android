package common.util.unit;

import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.entity.EEnemy;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.Source;
import common.pack.UserProfile;
import common.system.VImg;
import common.util.BattleObj;
import common.util.Data;
import common.util.unit.rand.EREnt;

import java.util.*;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class EneRand extends Data implements AbEnemy {

	public static final byte T_NL = 0, T_LL = 1;

	@JsonField(generic = EREnt.class, defval = "isEmpty")
	public final ArrayList<EREnt> list = new ArrayList<>();

	public final Map<StageBasis, ELock> map = new HashMap<>();

	@JsonField
	public int type = 0;

	public void updateCopy(StageBasis sb, Object o) {
		if (o != null)
			map.put(sb, (ELock) o);
	}

	protected EREnt getSelection(StageBasis sb, Object obj) {
		if (type != T_NL) {
			ELock l = map.get(sb);
			if (l == null)
				map.put(sb, l = type == T_LL ? new ELockLL() : new ELockGL());
			EREnt ae = l.get(obj);
			if (ae == null)
				l.put(obj, ae = selector(sb));
			return ae;
		}
		return selector(sb);
	}

	private EREnt selector(StageBasis sb) {
		int tot = 0;
		for (EREnt e : list)
			tot += e.share;
		if (tot > 0) {
			int r = sb.r.nextInt(tot);
			for (EREnt ent : list) {
				r -= ent.share;
				if (r < 0)
					return ent;
			}
		}
		return null;
	}

	@JsonClass.JCIdentifier
	@JsonField
	public final Identifier<AbEnemy> id;

	@JsonField
	public String name = "";
	public VImg icon = null;

	@JsonClass.JCConstructor
	public EneRand() {
		id = null;
	}

	public EneRand(Identifier<AbEnemy> ID) {
		id = ID;
	}

	public void fillPossible(Set<Enemy> se, Set<EneRand> sr) {
		sr.add(this);
		for (EREnt e : list) {
			AbEnemy ae = Identifier.get(e.ent);
			if (ae instanceof Enemy)
				se.add((Enemy) ae);
			if (ae instanceof EneRand) {
				EneRand er = (EneRand) ae;
				if (!sr.contains(er))
					er.fillPossible(se, sr);
			}
		}
	}

	@Override
	public EEnemy getEntity(StageBasis sb, Object obj, float mul, float mul2, int d0, int d1, int m) {
		return get(getSelection(sb, obj), sb, obj, mul, mul2, d0, d1, m);
	}

	@Override
	public VImg getIcon() {
		if (icon != null)
			return icon;
		return CommonStatic.getBCAssets().ico[0][0];
	}

	@Override
	public Identifier<AbEnemy> getID() {
		return id;
	}

	@Override
	public Set<Enemy> getPossible() {
		Set<Enemy> te = new TreeSet<>();
		fillPossible(te, new TreeSet<>());
		return te;
	}

	@Override
	public String toString() {
		return (CommonStatic.getFaves().enemies.contains(this) ? "❤" : "") + id.id + " - " + name + " (" + id.pack + ")";
	}

	private EEnemy get(EREnt x, StageBasis sb, Object obj, float mul, float mul2, int d0, int d1,
			int m) {
		return Identifier.getOr(x.ent, AbEnemy.class).getEntity(sb, obj, x.multi * mul / 100, x.multi * mul2 / 100, d0,
				d1, m);
	}

	public boolean contains(Identifier<AbEnemy> e, Identifier<AbEnemy> origin) {
		for(EREnt id : list) {
			Identifier<AbEnemy> i = id.ent;

			if(i == null)
				continue;

			if(origin.equals(i))
				continue;

			if(i.cls == EneRand.class) {
				EneRand rand = (EneRand) Identifier.get(i);

				if(rand != null && rand.contains(e, origin))
					return true;
			} else if(i.cls == Enemy.class) {
				if(i.pack.equals(e.pack) && i.id == e.id)
					return true;
			}
		}

		return false;
	}

	@JsonDecoder.OnInjected
	public void onInjected() {
		icon = UserProfile.getUserPack(id.pack).source.readImage(Source.BasePath.RAND + "/enemyDisplayIcons", id.id);
	}
}

interface ELock {

	EREnt get(Object obj);

	EREnt put(Object obj, EREnt ae);

}

class ELockGL extends BattleObj implements ELock {

	private EREnt ae;

	@Override
	public EREnt get(Object obj) {
		return ae;
	}

	@Override
	public EREnt put(Object obj, EREnt e) {
		EREnt pre = ae;
		ae = e;
		return pre;
	}

}

class ELockLL extends HashMap<Object, EREnt> implements ELock {

	private static final long serialVersionUID = 1L;

}