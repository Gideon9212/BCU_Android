package common.util.lang;

import common.pack.Identifier;
import common.pack.UserProfile;
import common.util.Data;
import common.util.Data.Proc;
import common.util.Data.Proc.SUMMON;
import common.util.Data.Proc.POISON;
import common.util.Data.Proc.COUNTER;
import common.util.Data.Proc.REVIVE;
import common.util.Data.Proc.SPEED;
import common.util.Data.Proc.ProcItem;
import common.util.unit.Unit;
import org.jcodec.common.tools.MathUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Editors {

	public static class DispItem implements LocaleCenter.Displayable {

		private final ProcLang.ItemLang lang;
		private final Supplier<Proc.ProcItem> proc;
		private final Formatter.Context ctx;

		public DispItem(ProcLang.ItemLang lang, Supplier<Proc.ProcItem> proc, Formatter.Context ctx) {
			this.lang = lang;
			this.proc = proc;
			this.ctx = ctx;
		}

		@Override
		public String getName() {
			return lang.full_name;
		}

		@Override
		public String getTooltip() {
			String t0 = lang.tooltip == null ? null : lang.tooltip;
			Proc.ProcItem item = proc.get();
			String t1 = item == null ? null : Formatter.format(lang.format, item, ctx);
			if (t0 != null)
				t0 = "<p width = \"500\">" + t0 + "</p>";
			if (t1 != null)
				t1 = "<p width = \"500\">" + t1 + "</p>";
			if (t0 == null && t1 == null)
				return "";
			else if (t0 == null)
				return "<html>" + t1 + "</html>";
			else if (t1 == null)
				return "<html>" + t0 + "</html>";
			else
				return "<html>" + t0 + "<hr>" + t1 + "</html>";
		}

		@Override
		public void setName(String str) {
			lang.full_name = str;
		}

		@Override
		public void setTooltip(String str) {
			lang.tooltip = str;
		}

	}

	public static class EdiField {

		private final Field f0;
		private final Field f1;

		public Object obj;

		private EdiField(Field f) {
			f0 = f;
			f1 = null;
		}

		private EdiField(Field pri, Field sec) {
			f0 = sec;
			f1 = pri;
		}

		public Object get() {
			return Data.err(() -> f0.get(obj));
		}

		public boolean getBoolean() {
			return Data.err(() -> f0.getBoolean(obj));
		}

		public int getInt() {
			return Data.err(() -> f0.getInt(obj));
		}

		public float getFloat() {
			return Data.err(() -> f0.getFloat(obj));
		}

		public double getDouble() {
			return Data.err(() -> f0.getDouble(obj));
		}

		public Class<?> getType() {
			return f0.getType();
		}

		public Field getRaw() {
			return f0;
		}

		public void set(Object data) {
			Data.err(() -> f0.set(obj, data));
		}

		public void setBoolean(boolean data) {
			Data.err(() -> f0.setBoolean(obj, data));
		}

		public void setData(Object obj) {
			this.obj = f1 == null ? obj : obj == null ? null : Data.err(() -> f1.get(obj));
		}

		public void setInt(int data) {
			Data.err(() -> f0.setInt(obj, data));
		}

	}

	public static class EditControl<T extends ProcItem> {

		public final Class<T> cls;
		private final Consumer<T> regulator;
		private final Function<EditorGroup, Consumer<T>> visibilityReg;

		public EditControl(Class<T> cls, Consumer<T> func) {
			this(cls, func, null);
		}

		public EditControl(Class<T> cls, Consumer<T> func, Function<EditorGroup, Consumer<T>> vis) {
			this.cls = cls;
			regulator = func;
			visibilityReg = vis;
		}

		public EdiField getField(String f) {
			return Data.err(() -> {
				if (f.contains(".")) {
					String[] strs = f.split("\\.");
					Field f0 = cls.getField(strs[0]);
					Field f1 = f0.getType().getField(strs[1]);
					return new EdiField(f0, f1);
				} else
					return new EdiField(cls.getField(f));
			});
		}

		public final void update(EditorGroup par) {
			regulate(par.obj);
			par.setData(par.obj);
			if (par.callback != null)
				par.callback.run();
		}

		@SuppressWarnings("unchecked")
		protected void regulate(ProcItem obj) {
			regulator.accept((T)obj);
		}

		@SuppressWarnings("unchecked")
		protected void setVis(EditorGroup eg, ProcItem obj) {
			visibilityReg.apply(eg).accept((T)obj);
		}
	}

	public static abstract class Editor {

		public final EditorGroup par;
		public final EdiField field;
		public final String name;

		public Editor(EditorGroup par, EdiField field, String f) {
			this.par = par;
			this.field = field;
			this.name = f;
		}

		/**
		 * notify that the data changed
		 */
		protected abstract void setData();

		protected final void update() {
			par.ctrl.update(par);
		}
	}

	public static class EditorGroup {

		public final String proc; // Proc Title
		public final Class<?> cls; // ProcItem
		public final Editor[] list;
		public final EditControl<?> ctrl;
		public final Runnable callback;

		public ProcItem obj;

		public EditorGroup(String proc, boolean edit, Runnable cb) {
			this.proc = proc;
			this.cls = Data.err(() -> Proc.class.getDeclaredField(proc)).getType();
			this.callback = cb;
			ctrl = map().get(proc);
			ProcLang.ItemLang item = ProcLang.get().get(proc);
			String[] arr = item.list();
			list = new Editor[arr.length];
			for (int i = 0; i < arr.length; i++)
				list[i] = getEditor(ctrl, this, arr[i], edit);
		}

		public LocaleCenter.Binder getItem(Formatter.Context ctx) {
			ProcLang.ItemLang lang = ProcLang.get().get(proc);
			LocaleCenter.Displayable disp = new DispItem(lang, this::getProc, ctx);
			return new LocaleCenter.ObjBinder(disp, proc, (name) -> getItem(ctx));
		}

		public void setData(ProcItem obj) {
			this.obj = obj;
			for (Editor e : list)
				if (e != null)
					e.setData();
			updateVisibility();
		}

		private ProcItem getProc() {
			return obj;
		}

		public void updateVisibility() {
			if (ctrl.visibilityReg != null)
				ctrl.setVis(this, obj);
		}
	}

	public interface EditorSupplier {

		Editor getEditor(EditControl<?> ctrl, EditorGroup g, String field, boolean edit);

		void setEditorVisibility(Editor e, boolean b);

		boolean isEnemy();

	}

	public static boolean def = true;//True means normal editing, false means blessing editing

	static {
		EditControl<Proc.PROB> prob = new EditControl<>(Proc.PROB.class, t -> t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100)));

		EditControl<Proc.PT> pt = new EditControl<>(Proc.PT.class, t -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0)
				t.time = 0;
			else if (t.time == 0)
				t.time = 1;
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1));

		EditControl<Proc.IMU> imu = new EditControl<>(Proc.IMU.class, t -> {
			t.block = Math.min(t.block, 100);
			if (t.block == 100)
				t.mult = Math.min(0, t.mult);
			else
				t.mult = Math.min(t.mult, 100);
		});

		EditControl<Proc.IMUI> imui = new EditControl<>(Proc.IMUI.class, t -> {
			t.block = Math.min(t.block, 100);
			if (t.block == 100)
				t.mult = Math.min(0, t.mult);
			else
				t.mult = Math.min(t.mult, 100);
			if (def && !t.exists())
				t.pid.clear();
		}, eg -> t -> setComponentVisibility(eg, !def || t.mult != 0 || t.block != 0, 2));

		EditControl<Proc.IMUAD> imuad = new EditControl<>(Proc.IMUAD.class, t -> {
			t.block = Math.min(t.block, 100);
			if (t.block == 100)
				t.mult = Math.min(0, t.mult);
			t.mult = Math.min(t.mult, 100);
			if (t.mult == 0 && t.block == 0)
				t.focus = Proc.IMUAD.FOCUS.ALL;
		}, eg -> t -> setComponentVisibility(eg, !def || t.mult != 0 || t.block != 0, 2));

        map().put("KB", new EditControl<>(Proc.PTD.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.dis = t.time = 0;
			} else {
				if (t.dis == 0)
					t.dis = Data.KB_DIS[Data.INT_KB];
				if (t.time <= 0)
					t.time = Data.KB_TIME[Data.INT_KB];
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("STOP", new EditControl<>(Proc.PTM.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0)
				t.mult = t.time = 0;
			else {
				if (t.mult >= 100)
					t.mult = 0;
				if (t.time == 0)
					t.time = 1;
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("SLOW", pt);

		map().put("RAGE", pt);

		map().put("HYPNO", pt);

		map().put("CRIT", new EditControl<>(Proc.PM.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0)
				t.mult = 0;
			else if (t.mult == 0)
				t.mult = 200;
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("WAVE", new EditControl<>(Proc.WAVE.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			t.lv = MathUtil.clip(t.lv, def ? 1 : -125, 125);
			if (def && t.prob == 0) {
				t.lv = 0;
				t.hitless = false;
				t.pid.clear();
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("WEAK", new EditControl<>(Proc.PTMS.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.mult = t.time = 0;
				t.stackable = false;
			} else
				t.time = Math.max(t.time, 1);
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("LETHARGY", new EditControl<>(Proc.LETHARGY.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.mult = t.time = 0;
				t.stackable = t.percentage = false;
			} else {
				if (t.percentage)
					t.mult = Math.max(t.mult, -100);
				t.time = Math.max(t.time, 1);
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("BREAK", prob);

		map().put("WARP", new EditControl<>(Proc.WARP.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (def && t.prob == 0) {
				t.dis = t.dis_1 = t.time = 0;
			} else {
				int oDis = t.dis;
				t.dis = Math.min(t.dis, t.dis_1);
				t.dis_1 = Math.max(oDis, t.dis_1);
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("CURSE", pt);

		map().put("STRONG", new EditControl<>(Proc.STRONG.class, (t) -> {
			t.health = Math.max(def ? 0 : -100, Math.min(t.health, 99.9999999f));
			if (def && t.health == 0) {
				t.mult = 0;
				t.incremental = false;
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.health > 0, 1)));

		map().put("SPEEDUP", new EditControl<>(Proc.STRONG.class, (t) -> {
			t.health = Math.max(def ? 0 : -100, Math.min(t.health, 99.9999999f));
			if (def && t.health == 0) {
				t.mult = 0;
				t.incremental = false;
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.health > 0, 1)));

		map().put("LETHAL", prob);

		map().put("BURROW", new EditControl<>(Proc.BURROW.class, (t) -> {
			t.count = Math.max(t.count, -1);
			if (!def)
				return;
			if (t.count == 0)
				t.dis = 0;
			else
				t.dis = Math.max(t.dis, 1);
		}, eg -> t -> setComponentVisibility(eg, !def || t.count != 0, 1)));

		map().put("REVIVE", new EditControl<>(REVIVE.class, (t) -> {
			if (!def)
				return;
			t.count = Math.max(t.count, -1);
			if (t.count == 0) {
				t.health = 0;
				t.time = 0;
				t.imu_zkill = false;
				t.revive_others = false;
				t.dis_0 = t.dis_1 = 0;
				t.range_type = REVIVE.RANGE.ACTIVE;
				t.revive_non_zombie = false;
			} else {
				t.health = Math.max(t.health, 1);
				t.time = Math.max(t.time, 1);
				if (!t.revive_others) {
					t.dis_0 = t.dis_1 = 0;
					t.range_type = REVIVE.RANGE.ACTIVE;
					t.revive_non_zombie = false;
				}
			}
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.count != 0, 1);
			setComponentVisibility(eg, !def || t.revive_others, 3, 4, 5, 7);
		}));

		map().put("SNIPER", prob);

		map().put("TIME", new EditControl<>(Proc.TIME.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (t.prob != 0) {
				t.time = Math.max(1, t.time);
				t.intensity = Math.min(t.intensity, 100);
				if (t.intensity == 0)
					t.intensity = 100;
			} else {
				t.time = 0;
				t.intensity = 100;
			}
		}, eg -> t -> setComponentVisibility(eg, t.prob > 0, 1)));

		map().put("SEAL", pt);

		map().put("SUMMON", new EditControl<>(SUMMON.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (def && t.prob == 0) {
				t.dis = t.max_dis = 0;
				t.id = null;
				t.time = t.interval = 0;
				t.mult = t.form = t.amount = 1;
				t.anim_type = Proc.SUMMON_ANIM.NONE;
				t.pass_proc = 0;
				t.fix_buff = t.ignore_limit = t.on_hit = t.on_kill = false;
				t.min_layer = 0;
				t.max_layer = 9;
				t.same_health = false;
			} else {
				if (def) {
					t.time = Math.max(0, t.time);
					t.amount = Math.max(t.amount, 1);
					if (t.amount >= 2)
						t.interval = Math.max(0, t.interval);
					else
						t.interval = 0;
				}
				int temp = t.dis;
				t.dis = Math.min(temp, t.max_dis);
				t.max_dis = Math.max(temp, t.max_dis);
				temp = t.min_layer;
				t.min_layer = Math.min(temp, t.max_layer);
				t.max_layer = Math.max(temp, t.max_layer);

				EditorSupplier edi = UserProfile.getStatic("Editor_Supplier", () -> null);
				if ((!edi.isEnemy() && t.id == null) || (t.id != null && t.id.cls == Unit.class)) {
					Unit u = Identifier.getOr(t.id, Unit.class);
					t.form = MathUtil.clip(t.form, def ? 1 : -u.forms.length, u.forms.length);
					if (!def || !t.fix_buff)
						t.mult = MathUtil.clip(t.mult, -u.max - u.maxp, u.max + u.maxp);
					else
						t.mult = MathUtil.clip(t.mult, 1, u.max + u.maxp);
				} else if (def) {
					t.form = 1;
					t.mult = Math.max(1, t.mult);
				}
			}
		}, eg -> t -> {
			EditorSupplier edi = UserProfile.getStatic("Editor_Supplier", () -> null);
			setComponentVisibility(eg, !def || t.prob > 0, 1);
			setComponentVisibility(eg, !def || (t.prob > 0 && ((!edi.isEnemy() && t.id == null) || (t.id != null && t.id.cls == Unit.class))), 17, 18);
			setComponentVisibility(eg, !def || t.amount >= 2, 18);
		}));

		map().put("MOVEWAVE", new EditControl<>(Proc.MOVEWAVE.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.dis = t.itv = t.speed = t.time = t.width = 0;
				t.pid.clear();
			} else {
				t.width = Math.max(0, t.width);
				t.time = Math.max(1, t.time);
				t.itv = Math.max(1, t.itv);
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("THEME", new EditControl<>(Proc.THEME.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (t.prob == 0) {
				t.time = 0;
				t.id = null;
				t.mus = null;
				t.kill = false;
			} else
				t.time = Math.max(-1, t.time);
		}, eg -> t -> setComponentVisibility(eg, t.prob > 0, 1)));

		map().put("POISON", new EditControl<>(POISON.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.damage = t.itv = t.time = 0;
				t.damage_type = POISON.TYPE.BURN;
				t.unstackable = t.ignoreMetal = t.modifAffected = false;
			} else {
				t.time = Math.max(1, t.time);
				t.itv = Math.max(1, t.itv);
			}
		}, eg -> t -> setComponentVisibility(eg, t.prob > 0, 1)));

		map().put("BOSS", prob);

		map().put("CRITI", imu);

		map().put("SATK", new EditControl<>(Proc.PM.class, (t) -> {
			t.prob = Math.max(0, Math.min(t.prob, 100));
			if (def && t.prob == 0)
				t.mult = 0;
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("COUNTER", new EditControl<>(COUNTER.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (t.prob != 0) {
				int min = t.minRange;
				t.minRange = Math.min(min, t.maxRange);
				t.maxRange = Math.max(min, t.maxRange);
				if (def && t.useOwnDamage)
					t.maxDamage = 0;
			} else {
				t.damage = t.minRange = t.maxRange = t.maxDamage = t.procType = 0;
				t.counterWave = COUNTER.CWAVE.NONE;
				t.useOwnDamage = t.outRange = t.areaAttack = false;
			}
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.prob > 0, 1);
			setComponentVisibility(eg, !def || (t.prob > 0 && (t.areaAttack || !t.outRange)), 2, 4);
		}));

		map().put("IMUATK", new EditControl<>(Proc.IMUATK.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0)
				t.time = t.cd = 0;
			else {
				t.time = Math.max(t.time, 1);
				t.cd = Math.max(t.cd, 0);
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("DMGCUT", new EditControl<>(Proc.DMGCUT.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.dmg = 0;
				t.reduction = 0;
				t.traitIgnore = t.procs = t.magnif = false;
			} else
				t.dmg = Math.max(t.dmg,0);
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("DMGCAP", new EditControl<>(Proc.DMGCAP.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.dmg = 0;
				t.traitIgnore = t.nullify = t.procs = t.magnif = false;
			} else
				t.dmg = Math.max(t.dmg,0);
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("POIATK", new EditControl<>(Proc.PM.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (def && t.prob == 0)
				t.mult = 0;
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("VOLC", new EditControl<>(Proc.VOLC.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				t.time = (t.time / Data.VOLC_ITV) * Data.VOLC_ITV;
			else if (t.prob == 0) {
				t.dis_0 = t.dis_1 = t.time = 0;
				t.hitless = false;
				t.pid.clear();
			} else
				t.time = Math.max(1, t.time / Data.VOLC_ITV) * Data.VOLC_ITV;
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.prob > 0, 1);
			setComponentVisibility(eg, false, 6);//count field which is only used for deathsurge
		}));

		map().put("MINIVOLC", new EditControl<>(Proc.MINIVOLC.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				t.time = (t.time / Data.VOLC_ITV) * Data.VOLC_ITV;
			else if (t.prob == 0) {
				t.dis_0 = t.dis_1 = t.time = 0;
				t.mult = 20;
				t.hitless = false;
				t.pid.clear();
			} else {
				t.time = Math.max(1, t.time / Data.VOLC_ITV) * Data.VOLC_ITV;
				if(t.mult == 0)
					t.mult = 20;
			}
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.prob > 0, 1);
			setComponentVisibility(eg, false, 7);//count field which is only used for deathsurge
		}));

		map().put("ARMOR", new EditControl<>(Proc.PTMS.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.mult = t.time = 0;
				t.stackable = false;
			} else
				t.time = Math.max(1, t.time);
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("SPEED", new EditControl<>(SPEED.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.speed = t.time = 0;
				t.type = SPEED.TYPE.FIXED;
			} else
				t.time = Math.max(1, t.time);
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("MINIWAVE", new EditControl<>(Proc.MINIWAVE.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (def && t.prob == 0) {
				t.lv = 0;
				t.multi = 20;
				t.hitless = false;
				t.pid.clear();
			} else {
				t.lv = MathUtil.clip(t.lv, def ? 1 : -125, 125);
				if(def && t.multi == 0)
					t.multi = 20;
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("IMUKB", imu);

		map().put("IMUSTOP", imu);

		map().put("IMUSLOW", imu);

		map().put("IMUWAVE", imui);

		map().put("IMUWEAK", imuad);

		map().put("IMULETHARGY", imuad);

		map().put("IMUWARP", imu);

		map().put("IMUCURSE", imu);

		map().put("IMUPOIATK", imu);

		map().put("IMUVOLC", imui);

		map().put("IMUSUMMON", imu);

		map().put("IMUSEAL", imu);

		map().put("IMUMOVING", new EditControl<>(Proc.MOVEI.class, (t) -> {
			t.mult = Math.min(t.mult, 100);
			if (def && t.mult == 0)
				t.pid.clear();
		}, eg -> t -> setComponentVisibility(eg, !def || t.mult > 0, 1)));

		map().put("IMURAGE", imu);

		map().put("IMUHYPNO", imu);

		map().put("IMUCANNON", new EditControl<>(Proc.CANNI.class, (t) -> {
			t.mult = Math.min(t.mult, 100);
			if (t.mult != 0)
				t.type = MathUtil.clip(t.type, 1, 127);
			else
				t.type = 0;
		}, eg -> t -> setComponentVisibility(eg, !def || t.mult > 0, 1)));

		map().put("IMUPOI", imuad);

		map().put("IMUARMOR", imuad);

		map().put("IMUSPEED", imuad);

		map().put("BARRIER", new EditControl<>(Proc.BARRIER.class, (t) -> {
			if (!def)
				return;
			t.health = Math.max(0, t.health);
			if (t.health > 0) {
				t.regentime = Math.max(0, t.regentime);
				t.timeout = Math.max(0, t.timeout);
			} else {
				t.regentime = t.timeout = 0;
				t.magnif = false;
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.health > 0, 1)));

		map().put("DEMONSHIELD", new EditControl<>(Proc.DSHIELD.class, t -> {
			if (!def)
				return;
			t.hp = Math.max(0, t.hp);

			if(t.hp == 0)
				t.regen = 0;
			else
				t.regen = Math.max(0, t.regen);
		}, eg -> t -> setComponentVisibility(eg, !def || t.hp > 0, 1)));

		map().put("SHIELDBREAK", prob);

		map().put("DEATHSURGE", new EditControl<>(Proc.VOLC.class, t -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				t.time = (t.time / Data.VOLC_ITV) * Data.VOLC_ITV;
			else if (t.prob == 0) {
				t.dis_0 = t.dis_1 = t.time = 0;
				t.spawns = 1;
				t.pid.clear();
			} else {
				t.time = Math.max(1, t.time / Data.VOLC_ITV) * Data.VOLC_ITV;
				t.spawns = Math.max(1, t.spawns);
			}
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.prob > 0, 1);
			setComponentVisibility(eg, false, 4, 5);
			setComponentVisibility(eg, def && t.prob > 0, 6);
		}));

		map().put("MINIDEATHSURGE", new EditControl<>(Proc.MINIVOLC.class, t -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				t.time = (t.time / Data.VOLC_ITV) * Data.VOLC_ITV;
			else if (t.prob == 0) {
				t.dis_0 = t.dis_1 = t.time = 0;
				t.spawns = 1;
				t.mult = 20;
				t.pid.clear();
			} else {
				t.time = Math.max(1, t.time / Data.VOLC_ITV) * Data.VOLC_ITV;
				t.spawns = Math.max(1, t.spawns);
				if (t.mult == 0)
					t.mult = 20;
			}
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.prob > 0, 1);
			setComponentVisibility(eg, false, 5, 6);
			setComponentVisibility(eg, def && t.prob > 0, 7);
		}));

		map().put("REFUND", new EditControl<>(Proc.REFUND.class, t -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.mult = 0;
				t.count = 1;
			} else if (t.mult == 0) {
				t.count = 2;
				t.mult = 50;
			} else
				t.count = Math.max(t.count, 1);
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.prob > 0, 1, 2);
			setComponentVisibility(eg, def && t.prob > 0, 2);
		}));

		map().put("BOUNTY", new EditControl<>(Proc.MULT.class, t -> {}));
		map().put("ATKBASE", new EditControl<>(Proc.MULT.class, t -> {}));

		map().put("WORKERLV", new EditControl<>(Proc.WORKLV.class, t -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0)
				t.mult = 0;
			else if (t.mult == 0)
				t.mult = 1;
			else
				t.mult = MathUtil.clip(t.mult, -7, 7);
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("CDSETTER", new EditControl<>(Proc.CDSETTER.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (def && t.prob == 0) {
				t.amount = t.slot = t.type = 0;
			} else {
				t.slot = MathUtil.clip(t.slot, -1, 11);
				t.type = MathUtil.clip(t.type, 0, 2);
				if (t.type == 1)
					t.amount = MathUtil.clip(t.amount, def ? 0 : -100, 100);
				else if (def && t.type == 2)
					t.amount = Math.max(t.amount, 0);
				else if (def && t.amount == 0)
					t.amount = 1;
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("WEAKAURA", new EditControl<>(Proc.AURA.class, (t) -> {
			if (def && t.amult == 0 && t.dmult == 0 && t.smult == 0 && t.tmult == 0) {
				t.min_dis = t.max_dis = 0;
				t.trait = false;
			} else {
				t.tmult = Math.max(t.tmult, -100);
				int min = t.min_dis;
				t.min_dis = Math.min(min, t.max_dis);
				t.max_dis = Math.max(min, t.max_dis);
			}
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.exists(), 4, 7);
			setComponentVisibility(eg, false, 7);
		}));

		map().put("STRONGAURA", new EditControl<>(Proc.AURA.class, (t) -> {
			if (def && t.amult == 0 && t.dmult == 0 && t.smult == 0 && t.tmult == 0) {
				t.min_dis = t.max_dis = 0;
				t.trait = t.skip_self = false;
			} else {
				t.tmult = Math.max(t.tmult, 0);
				int min = t.min_dis;
				t.min_dis = Math.min(min, t.max_dis);
				t.max_dis = Math.max(min, t.max_dis);
				t.skip_self &= t.min_dis * t.max_dis <= 0;
			}
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.exists(), 4);
			setComponentVisibility(eg, !def || (t.exists() && t.min_dis * t.max_dis <= 0), 7);
		}));

		map().put("REMOTESHIELD", new EditControl<>(Proc.REMOTESHIELD.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (def && t.prob == 0) {
				t.minrange = t.maxrange = t.reduction = t.block = 0;
				t.traitCon = t.procs = t.waves = false;
			} else {
				int min = t.minrange;
				t.minrange = Math.min(min, t.maxrange);
				t.maxrange = Math.max(min, t.maxrange);
			}
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("BSTHUNT", new EditControl<>(Proc.BSTHUNT.class, (t) -> {
			if (t.active || !def) {
				t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
				if (!def)
					return;
				if (t.prob == 0)
					t.time = 0;
				else
					t.time = Math.max(1, t.time);
			} else
				t.prob = t.time = 0;
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.active, 1);
			setComponentVisibility(eg, !def || t.prob > 0, 2);
		}));

		map().put("AI", new EditControl<>(Proc.AI.class, (t) -> {
			if (!def)
				return;
			t.retreatDist = Math.max(0, t.retreatDist);
			if (t.retreatDist == 0 && !t.danger)
				t.retreatSpeed = 0;
		}, eg -> t -> setComponentVisibility(eg, !def || t.retreatDist > 0 || t.danger, 1, 2)));

		map().put("DEMONVOLC", new EditControl<>(Proc.PM.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0)
				t.mult = 0;
			else if (t.mult == 0)
				t.mult = 100;
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("DMGINC", new EditControl<>(Proc.STATINC.class, (t) -> {
			if (def && t.mult == 100)
				t.mult = 0;
		}));

		map().put("DEFINC", new EditControl<>(Proc.STATINC.class, (t) -> {
			if (def && t.mult == 100)
				t.mult = 0;
		}));

		map().put("RANGESHIELD", new EditControl<>(Proc.RANGESHIELD.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (!def)
				return;
			if (t.prob == 0) {
				t.mult = 0;
				t.range = false;
			} else if (t.mult == 0)
				t.mult = 1;
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("SPIRIT", new EditControl<>(Proc.SPIRIT.class, (t) -> {
			if (t.id == null) {
				t.cd0 = t.cd1 = t.amount = t.summonerCd = t.moneyCost = t.form = 0;
				t.animType = Proc.SUMMON_ANIM.NONE;
				t.inv = false;
			} else {
				t.amount = Math.max(t.amount, 1);
				t.cd0 = Math.max(t.cd0, 15);
				t.cd1 = Math.max(t.cd1, 15);

				Unit u = Identifier.getOr(t.id, Unit.class);
				t.form = MathUtil.clip(t.form, 1, u.forms.length);
				if (u.forms[t.form - 1].anim.getAtkCount() == 0) { //BC spirit
					t.animType = Proc.SUMMON_ANIM.NONE;
					t.inv = true;
				}
			}
		}, eg -> t -> {
			if (!def) {//lol
				setComponentVisibility(eg, false, 0);
				return;
			}
			setComponentVisibility(eg, t.id != null, 1);
			if (t.id == null)
				return;
			Unit u = Identifier.getOr(t.id, Unit.class);
			setComponentVisibility(eg, t.amount > 1, 2, 3);
			setComponentVisibility(eg, u.forms[t.form - 1].anim.getAtkCount() > 0, 6);
			setComponentVisibility(eg, u.forms.length > 1, 7, 8);
		}));

		map().put("METALKILL", new EditControl<>(Proc.MULT.class, (t) -> {
			if (def && t.mult == 100)
				t.mult = 0;
		}));

		map().put("BLAST", new EditControl<>(Proc.BLAST.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (t.prob != 0) {
				int d0 = t.dis_0;
				t.dis_0 = Math.min(d0, t.dis_1);
				t.dis_1 = Math.max(d0, t.dis_1);
				if (!def)
					return;
				if (t.lv == 0) {
					t.lv = 3;
					t.reduction = 30;
				} else
					t.lv = Math.max(1, t.lv);
				t.reduction = Math.min(t.reduction, 100f / t.lv);
			} else if (def) {
				t.dis_0 = t.dis_1 = 0;
				t.lv = 3;
				t.reduction = 30;
			}
		}, eg -> t -> {
			setComponentVisibility(eg, !def || t.prob > 0, 1);
			setComponentVisibility(eg, !def || (t.prob > 0 && t.lv > 1), 4, 5);
		}));

		map().put("IMUBLAST", imui);

		map().put("DRAIN", new EditControl<>(Proc.PM.class, (t) -> {
			t.prob = Math.max(def ? 0 : -100, Math.min(t.prob, 100));
			if (def && t.prob == 0)
				t.mult = 0;
		}, eg -> t -> setComponentVisibility(eg, !def || t.prob > 0, 1)));

		map().put("BLESSING", new EditControl<>(Proc.BLESSING.class, (t) -> {
			t.prob = Math.max(0, Math.min(t.prob, 100));
			if (t.prob == 0) {
				t.time = t.abis = 0;
				t.stackable = false;
				t.procs = null;
				t.traits.clear();
			}
		}, eg -> t -> setComponentVisibility(eg, t.prob > 0, 1)));
	}

	private static void setComponentVisibility(EditorGroup egg, boolean boo, int... fields) {
		EditorSupplier edi = UserProfile.getStatic("Editor_Supplier", () -> null);
		if (fields.length > 2) {
			for (int field : fields)
				if (egg.list[field] != null)
					edi.setEditorVisibility(egg.list[field], boo);
		} else {
			int l1 = fields.length == 2 ? fields[1] : egg.list.length;
			for (int i = fields[0]; i < l1; i++)
				if (egg.list[i] != null)
					edi.setEditorVisibility(egg.list[i], boo);
		}
	}

	public static void setEditorSupplier(EditorSupplier sup) {
		UserProfile.setStatic("Editor_Supplier", sup);
	}

	private static Editor getEditor(EditControl<?> ctrl, EditorGroup g, String field, boolean edit) {
		EditorSupplier edi = UserProfile.getStatic("Editor_Supplier", () -> null);
		return edi.getEditor(ctrl, g, field, edit);
	}

	@SuppressWarnings("rawtypes")
	private static Map<String, EditControl> map() {
		return UserProfile.getRegister("Editor_EditControl");
	}

}