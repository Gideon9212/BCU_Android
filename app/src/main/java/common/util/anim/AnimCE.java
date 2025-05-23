package common.util.anim;

import common.CommonStatic.EditLink;
import common.io.InStream;
import common.io.OutStream;
import common.io.json.JsonClass;
import common.pack.PackData.UserPack;
import common.pack.Source;
import common.pack.Source.ResourceLocation;
import common.pack.Source.SourceAnimLoader;
import common.pack.Source.SourceAnimSaver;
import common.pack.Source.Workspace;
import common.pack.UserProfile;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.util.AnimGroup;
import common.util.Animable;
import common.util.pack.Soul;
import common.util.pack.bgeffect.BackgroundEffect;
import common.util.pack.bgeffect.CustomBGEffect;
import common.util.unit.Enemy;
import common.util.unit.Form;
import common.util.unit.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@JsonClass.JCGeneric(ResourceLocation.class)
public class AnimCE extends AnimCI {

	private static class History {

		protected final OutStream data;

		protected final String name;

		protected OutStream mms;

		protected History(String str, OutStream os) {
			name = str;
			data = os;
		}

	}

	private static final String REG_LOCAL_ANIM = "local_animation";

	public static String getAvailable(String string, Source.BasePath base) {
		ResourceLocation rl = new ResourceLocation(ResourceLocation.LOCAL, string, base);
		Workspace.validate(rl);
		return rl.id;
	}

	public static Map<String, AnimCE> map() {
		return UserProfile.getRegister(REG_LOCAL_ANIM);
	}

	private boolean saved = true;

	public EditLink link;

	public Stack<History> history = new Stack<>();
	public Stack<History> redo = new Stack<>();

	@NotNull
	public String group = "";

	public AnimCE(ResourceLocation resourceLocation) {
		super(new SourceAnimLoader(resourceLocation, null));
		id = resourceLocation;
		history("initial");
	}

	public AnimCE(ResourceLocation rl, AnimD<?, ?> ori) {
		super(new SourceAnimLoader(rl, null));
		id = rl;
		copyFrom(ori);

		if (id.pack.equals(ResourceLocation.LOCAL)) {
			map().put(id.id, this);
			AnimGroup.workspaceGroup.renewGroup();
		}
	}

	/**
	 * for conversion only
	 */
	@Deprecated
	public AnimCE(Source.SourceAnimLoader al) {
		super(al);
	}

	public void createNew() {
		loaded = true;
		partial = true;
		imgcut = new ImgCut();
		mamodel = new MaModel();
		if (loader.getName().base.equals(Source.BasePath.ANIM))
			anims = new MaAnim[AnimU.TYPEDEF.length];
		else if (loader.getName().base.equals(Source.BasePath.SOUL))
			anims = new MaAnim[1];
		else
			anims = new MaAnim[2];
		for (int i = 0; i < anims.length; i++)
			anims[i] = new MaAnim();
		parts = imgcut.cut(getNum());
		saved = false;
		save();
		history("initial");
	}

	public boolean undeleteableP(UserPack p) {
		if (types == AnimU.SOUL) {
			for (Soul s : p.souls.getList())
				if (s.anim == this)
					return true;
		} else if (types == AnimU.BGEFFECT) {
			for (BackgroundEffect bge : p.bgEffects.getList())
				if (bge instanceof CustomBGEffect && ((CustomBGEffect) bge).anim == this)
					return true;
		} else {
			for (Enemy e : p.enemies.getList())
				if (e.anim == this)
					return true;
			for (Unit u : p.units.getList())
				for (Form f : u.forms)
					if (f.anim == this)
						return true;
		}
		return false;
	}

	public boolean deletable() {
		for (UserPack p : UserProfile.getUserPacks())
			if (p.editable && (id.pack.equals(ResourceLocation.LOCAL) || p.desc.dependency.contains(id.pack) || id.pack.equals(p.getSID())) && undeleteableP(p))
				return false;
		return true;
	}

	public void delete() {
		map().remove(id.id);
		AnimGroup.workspaceGroup.renewGroup();
		if (!(id.pack.equals(ResourceLocation.LOCAL)))
			((Workspace)UserProfile.getUserPack(id.pack).source).unloadAnimation(this);
		new SourceAnimSaver(id, this).delete(true);
	}

	public String getUndo() {
		return history.peek().name;
	}

	public String getRedo() {
		if (redo.empty())
			return "nothing";
		return redo.peek().name;
	}

	public void removeICline(int ind) {
		int[][] data = imgcut.cuts;
		String[] name = imgcut.strs;

		imgcut.cuts = new int[--imgcut.n][];
		imgcut.strs = new String[imgcut.n];

		for (int i = 0; i < ind; i++) {
			imgcut.cuts[i] = data[i];
			imgcut.strs[i] = name[i];
		}
		for (int i = ind + 1; i < data.length; i++) {
			imgcut.cuts[i - 1] = data[i];
			imgcut.strs[i - 1] = name[i];
		}

		for (int[] ints : anim.mamodel.parts)
			if (ints[2] > ind)
				ints[2]--;
		for (MaAnim ma : anim.anims)
			for (Part part : ma.parts)
				if (part.ints[1] == 2)
					for (int[] ints : part.moves)
						if (ints[1] > ind)
							ints[1]--;
		ICedited();
		unSave("imgcut remove line");
	}

	public void reloadAnimations() {
		partial = false;
		partial();
	}

	public void ICedited() {
		check();
		parts = imgcut.cut(getNum());
	}

	public void addMMline(int ind, int spr) {
		if (ind == 0)
			ind++;
		int[] inds = new int[mamodel.n];
		for (int i = 0; i < mamodel.n; i++)
			inds[i] = i < ind ? i : i + 1;
		reorderModel(inds);
		mamodel.n++;
		int[] move = new int[mamodel.n];
		for (int i = 0; i < mamodel.n; i++)
			move[i] = i < ind ? i : i - 1;
		mamodel.reorder(move);
		int[] newl = new int[14];
		newl[2] = Math.max(spr, 0);
		newl[8] = newl[9] = newl[11] = 1000;
		mamodel.parts[ind] = newl;
		unSave("mamodel add line");
	}

	public final void addAttack() {
		int ind = 2 + getAtkCount();
		MaAnim[] newMaAnim = new MaAnim[anims.length + 1];
		UType[] newUType = new UType[newMaAnim.length];

		for (int i = 0; i < newMaAnim.length; i++) {
			if (i == ind)
				i++;
			if (i < ind) {
				newMaAnim[i] = anims[i];
				newUType[i] = types[i];
			} else {
				newMaAnim[i] = anims[i - 1];
				newUType[i] = types[i - 1];
			}
		}
		anims = newMaAnim;
		types = newUType;
		anims[ind] = new MaAnim();
		types[ind] = new UType("attack" + (ind - 2), true);
	}

	public final void remAttack(int atk) {
		MaAnim[] newMaAnim = new MaAnim[anims.length - 1];
		UType[] newUType = new UType[newMaAnim.length];

		for (int i = 0; i < newMaAnim.length; i++) {
			if (i < atk) {
				newMaAnim[i] = anims[i];
				newUType[i] = types[i];
			} else {
				newMaAnim[i] = anims[i + 1];
				newUType[i] = types[i + 1];
			}
		}
		newUType[2] = TYPEDEF[2];
		for (int i = 3; i < newUType.length - 6; i++)
			newUType[i].changeName("attack" + (i - 2));
		anims = newMaAnim;
		types = newUType;
	}

	public boolean inPool() {
		return id.pack != null && id.pack.equals("_local");
	}

	public boolean isSaved() {
		return saved;
	}

	@Override
	public void load() {
		super.load();
		history("initial");

		validate();
	}

	public void localize() {
		check();
		map().remove(id.id);
		SourceAnimSaver saver = new SourceAnimSaver(id, this);
		for (UserPack pack : UserProfile.getUserPacks())
			if (pack.editable) {
				List<Animable<AnimU<?>, UType>> list = new ArrayList<>();
				if (id.base == Source.BasePath.ANIM) {
					for (Enemy e : pack.enemies)
						if (e.anim == this)
							list.add(e);
					for (Unit u : pack.units)
						for (Form f : u.forms)
							if (f.anim == this)
								list.add(f);
				} else if (id.base == Source.BasePath.SOUL) {
					for (Soul s : pack.souls)
						if (s.anim == this)
							list.add(s);
				} else
					for (BackgroundEffect bge : pack.bgEffects)
						if (bge instanceof CustomBGEffect && ((CustomBGEffect)bge).anim == this)
							list.add(((CustomBGEffect) bge).anim);
				if (list.isEmpty())
					continue;
				ResourceLocation rl = new ResourceLocation(pack.getSID(), id.id, id.base);
				Workspace.validate(rl);
				AnimCE tar = new AnimCE(rl, this);
				tar.parts = tar.imgcut.cut(tar.getNum());
				((Workspace)pack.source).addAnimation(tar);
				for (Animable<AnimU<?>, UType> a : list)
					a.anim = tar;
			}
		saver.delete(true);
		AnimGroup.workspaceGroup.renewGroup();
	}

	public void merge(AnimCE a, int x, int y) {
		ImgCut ic0 = imgcut;
		ImgCut ic1 = a.imgcut;
		int icn = ic0.n;
		ic0.n += ic1.n;
		ic0.cuts = Arrays.copyOf(ic0.cuts, ic0.n);
		for (int i = 0; i < icn; i++)
			ic0.cuts[i] = ic0.cuts[i].clone();
		ic0.strs = Arrays.copyOf(ic0.strs, ic0.n);
		for (int i = 0; i < ic1.n; i++) {
			int[] data = ic0.cuts[i + icn] = ic1.cuts[i].clone();
			data[0] += x;
			data[1] += y;
			ic0.strs[i + icn] = ic1.strs[i];
		}

		MaModel mm0 = mamodel;
		MaModel mm1 = a.mamodel;
		int mmn = mm0.n;
		mm0.n += mm1.n;
		mm0.parts = Arrays.copyOf(mm0.parts, mm0.n);
		for (int i = 0; i < mmn; i++)
			mm0.parts[i] = mm0.parts[i].clone();
		mm0.strs0 = Arrays.copyOf(mm0.strs0, mm0.n);
		int[] fir = mm0.parts[0];
		for (int i = 0; i < mm1.n; i++) {
			int[] data = mm0.parts[i + mmn] = mm1.parts[i].clone();
			if (data[0] != -1)
				data[0] += mmn;
			else {
				data[0] = 0;
				data[8] = data[8] * 1000 / fir[8];
				data[9] = data[9] * 1000 / fir[9];
				data[4] = data[6] * data[8] / 1000 - fir[6];
				data[5] = data[7] * data[9] / 1000 - fir[7];
			}
			data[2] += icn;
			mm0.strs0[i + mmn] = mm1.strs0[i];
		}

		for (int i = 0; i < Math.min(anims.length, a.anims.length); i++) {
			MaAnim ma0 = anims[i];
			MaAnim ma1 = a.anims[i];
			int man = ma0.n;
			ma0.n += ma1.n;
			ma0.parts = Arrays.copyOf(ma0.parts, ma0.n);
			for (int j = 0; j < man; j++)
				ma0.parts[j] = ma0.parts[j].clone();
			for (int j = 0; j < ma1.n; j++) {
				Part p = ma0.parts[j + man] = ma1.parts[j].clone();
				p.ints[0] += mmn;
				if (p.ints[1] == 2)
					for (int[] data : p.moves)
						data[1] += icn;
			}
		}
	}

	public void reloImg() {
		setNum(loader.loader.getNum());
		ICedited();
	}

	public void renameTo(String str) {
		check();
		if (id.pack.equals(ResourceLocation.LOCAL))
			map().remove(id.id);
		SourceAnimSaver saver = new SourceAnimSaver(id, this);
		saver.delete(false);
		id.id = str;
		Workspace.validate(id);
		if (id.pack.equals(ResourceLocation.LOCAL))
			map().put(id.id, this);
		AnimGroup.workspaceGroup.renewGroup();
		saver.saveAll();
		reloImg();
		unSave("rename (not applicable for undo)");
	}

	public void resize(double d) {
		for (int[] l : imgcut.cuts)
			for (int i = 0; i < l.length; i++)
				l[i] *= d;
		mamodel.parts[0][8] /= d;
		mamodel.parts[0][9] /= d;
		for (int[] l : mamodel.parts) {
			l[4] *= d;
			l[5] *= d;
			l[6] *= d;
			l[7] *= d;
		}
		for (MaAnim ma : anims)
			for (Part p : ma.parts)
				if (p.ints[1] >= 4 && p.ints[1] <= 7)
					for (int[] x : p.moves)
						x[1] *= d;
		unSave("resize");
	}

	public void restore(History hist) {
		InStream is = hist.data.translate();
		imgcut.restore(is);
		ICedited();
		mamodel.restore(is);
		int n = is.nextInt();
		anims = new MaAnim[n];
		for (int i = 0; i < n; i++) {
			anims[i] = new MaAnim();
			anims[i].restore(is);
		}
		is = hist.mms.translate();
		n = is.nextInt();
		for (int i = 0; i < n; i++) {
			int ind = is.nextInt();
			int val = is.nextInt();
			if (ind >= 0 && ind < mamodel.n)
				mamodel.status.put(mamodel.parts[ind], val);
		}
		saved = false;
	}

	public void undo() {
		redo.push(history.pop());
		restore(history.peek());
	}

	public void redo() {
		History hist = redo.pop();
		restore(hist);
		history.push(hist);
	}

	@Override
	public void revert() {
		super.revert();
		unSave("revert");
	}

	public void autosave() {
		if (loaded && !isSaved())
			new SourceAnimSaver(new ResourceLocation("_autosave", id.id, id.base == null ? Source.BasePath.ANIM : id.base), this).saveData();
	}

	public void save() {
		if (!loaded || isSaved())
			return;
		saved = true;
		new SourceAnimSaver(id, this).saveAll();
	}

	public void saveIcon() {
		new SourceAnimSaver(id, this).saveIconDisplay();
	}

	public void saveImg() {
		new SourceAnimSaver(id, this).saveSprite();
	}

	public void saveUni() {
		new SourceAnimSaver(id, this).saveIconDeploy();
	}

	public void savePreview() {
		new SourceAnimSaver(id, this).saveIconPreview();
	}

	public void setEdi(VImg uni) {
		loader.setEdi(uni);
	}

	public void setNum(FakeImage fimg) {
		loader.setNum(fimg);
		if (loaded)
			ICedited();
	}

	public void setUni(VImg uni) {
		loader.setUni(uni);
	}

	public void setPreview(VImg uni) {
		loader.setPreview(uni);
	}

	public void unSave(String str) {
		saved = false;
		history(str);
		if (link != null)
			link.review();
	}

	public void updateStatus() {
		partial();
		OutStream mms = OutStream.getIns();
		mms.writeInt(mamodel.status.size());
		mamodel.status.forEach((d, s) -> {
			int ind = -1;
			for (int i = 0; i < mamodel.n; i++)
				if (mamodel.parts[i] == d)
					ind = i;
			mms.writeInt(ind);
			mms.writeInt(s);
		});
		mms.terminate();
		history.peek().mms = mms;
	}

	@Override
	public void partial() {
		super.partial();
		standardize();
	}

	private void copyFrom(AnimD<?, ?> ori) {
		loaded = true;
		partial = true;

		boolean isAnim = id.base.equals(Source.BasePath.ANIM);
		imgcut = ori.imgcut.clone();
		mamodel = ori.mamodel.clone();
		if (mamodel.confs.length < 1)
			mamodel.confs = new int[2][6];

		anims = new MaAnim[Math.max(ori.types.length, TYPEDEF.length)];
		for (int i = 0; i < anims.length; i++)
			if (i < ori.anims.length && ori.anims[i] != null)
				anims[i] = ori.anims[i].clone();
			else
				anims[i] = new MaAnim();

		loader.setNum(ori.getNum().cloneImage());
		types = isAnim ? ori.types.length > TYPEDEF.length ? (UType[])ori.types : TYPEDEF : ori.types.length == 2 ? BGEFFECT : SOUL;
		parts = imgcut.cut(ori.getNum());
		if (ori instanceof AnimU<?>) {
			AnimU<?> au = (AnimU<?>) ori;
			setEdi(au.getEdi());
			setUni(au.getUni());
			setPreview(au.getPreviewIcon());
		}
		standardize();
		saved = false;
		save();
		history("initial");
	}

	private void history(String str) {
		if (!history.empty() && str.equals("initial"))
			return;

		partial();
		OutStream os = OutStream.getAnimIns();
		imgcut.write(os);
		mamodel.write(os);
		os.writeInt(anims.length);
		for (MaAnim ma : anims)
			ma.write(os);
		os.terminate();
		History h = new History(str, os);
		history.push(h);
		redo.clear();
		updateStatus();
	}

	private void standardize() {
		if (mamodel.parts.length == 0 || mamodel.confs.length == 0)
			return;
		int[] dat = mamodel.parts[0];
		int[] con = mamodel.confs[0];
		dat[6] -= con[2];
		dat[7] -= con[3];
		con[2] = con[3] = 0;

		int[] std = mamodel.ints;
		for (int[] data : mamodel.parts) {
			data[8] = data[8] * 1000 / std[0];
			data[9] = data[9] * 1000 / std[0];
			data[10] = data[10] * 3600 / std[1];
			data[11] = data[11] * 1000 / std[2];
		}
		for (MaAnim ma : anims)
			for (Part p : ma.parts) {
				if (p.ints[1] >= 8 && p.ints[1] <= 10)
					for (int[] data : p.moves)
						data[1] = data[1] * 1000 / std[0];
				if (p.ints[1] == 11)
					for (int[] data : p.moves)
						data[1] = data[1] * 3600 / std[1];
				if (p.ints[1] == 12)
					for (int[] data : p.moves)
						data[1] = data[1] * 1000 / std[2];
			}
		std[0] = 1000;
		std[1] = 3600;
		std[2] = 1000;
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof AnimCE) {
			return this.id.id.equals(((AnimCE) that).id.id);
		} else {
			return false;
		}
	}
}