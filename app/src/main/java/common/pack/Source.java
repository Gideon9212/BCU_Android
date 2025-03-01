package common.pack;

import common.CommonStatic;
import common.io.PackLoader;
import common.io.PackLoader.ZipDesc;
import common.io.assets.Admin.StaticPermitted;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonEncoder;
import common.io.json.JsonField;
import common.pack.Context.ErrType;
import common.pack.PackData.UserPack;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.system.files.FDFile;
import common.system.files.FileData;
import common.system.files.VFile;
import common.util.Data;
import common.util.anim.*;
import common.util.pack.Background;
import common.util.pack.Soul;
import common.util.pack.bgeffect.BackgroundEffect;
import common.util.pack.bgeffect.CustomBGEffect;
import common.util.stage.*;
import common.util.unit.Enemy;
import common.util.unit.Form;
import common.util.unit.Trait;
import common.util.unit.Unit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public abstract class Source {

	public static boolean warn = true;

	public interface SourceLoader {

		FileData loadFile(BasePath base, ResourceLocation id, String str);
	}

	@JsonClass
	public static class ResourceLocation {

		public static final String LOCAL = "_local";

		@JsonField
		public String pack, id;

		@JsonField
		public BasePath base;

		@JsonClass.JCConstructor
		public ResourceLocation() {
		}

		public ResourceLocation(String pack, String id) {
			this.pack = pack;
			this.id = id;
		}

		public ResourceLocation(String pack, String id, BasePath base) {
			this.pack = pack;
			this.id = id;
			this.base = base == null ? BasePath.ANIM : base;
		}

		public void setBase(BasePath b) {
			base = b;
		}

		@JsonClass.JCGetter
		public AnimU<? extends AnimU.ImageKeeper> getAnim() {
			if (pack.equals(LOCAL))
				return AnimCE.map().get(id);
			if (pack.equals(Identifier.DEF)) {
				int ind = CommonStatic.parseIntN(id);
				if (id.charAt(4) == 'e')
					return UserProfile.getBCData().enemies.arr[ind].anim;
				if (id.charAt(4) == 'm')
					return new AnimUD("./org/img/m/" + Data.trio(ind) + "/", id, "edi" + id + Data.duo(Math.min(1, ind)) + ".png", "uni" + id + Data.duo(Math.min(1, ind)) + ".png");
				byte f = 0;
				for (byte i = 1; i < 3; i++)
					if (id.charAt(4) == Data.SUFX[i]) {
						f = i;
						break;
					}
				return UserProfile.getBCData().units.arr[ind].forms[f].anim;
			}

			return UserProfile.getUserPack(pack).source.loadAnimation(id, base);
		}

		public String getPath() {
			return "./" + pack + "/" + base + "/" + id;
		}

		@JsonClass.JCGetter
		public Replay getReplay() {
			if (pack.equals(LOCAL))
				return Replay.getMap().get(id);
			Source s = UserProfile.getUserPack(pack).source;
			String path = "./" + BasePath.REPLAY + "/" + id + ".replay";
			return Data.err(() -> Replay.read(s.getFileData(path).getStream()));
		}

		@Override
		public String toString() {
			return pack + "/" + id;
		}

		@JsonDecoder.OnInjected
		public void onInjectSource() {
			Object zip = UserProfile.getStatic(UserProfile.CURRENT_PACK, () -> null);

			if (this.pack.equals(LOCAL)) {
				if (zip instanceof ZipSource) {
					this.pack = ((ZipSource) zip).id;
					this.id = "_mapped_" + this.id;
				} else return;
			}
			AnimU<?> anim = getAnim();

			if (!(anim instanceof AnimCE))
				return;
			UserPack p = (UserPack) UserPack.getPack(zip instanceof Workspace ? ((Workspace) zip).id : pack);

			if (UserProfile.isOlderPack(p, "0.6.9.1"))
				this.base = BasePath.ANIM;
			if (UserProfile.isOlderPack(p, "0.7.8.0")) {
				anim.anims = anim.loader.getMA();
				for (MaAnim maanim : anim.anims)
					for (Part line : maanim.parts)
						if (line.ints[1] == 8)
							line.ints[1] = 53;
				((AnimCE)anim).unSave("scale to new scale");
				anim.unload();
			}
		}

	}

	@StaticPermitted
	public static class SourceAnimLoader {

		public static final String IC = "imgcut.txt", MM = "mamodel.txt";
		public static final String[] MA_SOUL = { "maanim_soul.txt" };
		public static final String[] MA_BACKGROUND = { "maanim_background.txt", "maanim_foreground.txt" };
		public static final String SP = "sprite.png";
		public static final String EDI = "icon_display.png", UNI = "icon_deploy.png", ICN = "icon_preview.png";

		private final ResourceLocation id;
		private final Source.SourceLoader loader;

		public SourceAnimLoader(ResourceLocation id, Source.SourceLoader loader) {
			this.id = id;
			this.loader = loader == null ? Workspace::loadAnimFile : loader;
		}

		private AnimU.UType[] getBaseUT() {
			if (id.base.equals(BasePath.ANIM))
				return AnimU.TYPEDEF;
			else if (id.base.equals(BasePath.SOUL))
				return AnimU.SOUL;
			else
				return AnimU.BGEFFECT;
		}

		public VImg getIcon(String path) {
			FileData icn = loader.loadFile(id.base, id, path);
			if (icn == null)
				return null;
			return new VImg(FakeImage.read(icn));
		}

		public ImgCut getIC() {
			FileData fd = loader.loadFile(id.base, id, IC);
			if (fd == null && warn)
				CommonStatic.ctx.printErr(ErrType.WARN, "Corrupted imgcut found for " + id);
			return ImgCut.newIns(fd);
		}

		public MaAnim[] getMA() {
			boolean old = !id.pack.equals("_local") && UserProfile.isOlderPack(UserProfile.getUserPack(id.pack), "0.7.8.0");
			ArrayList<MaAnim> ans = new ArrayList<>();
			for (int i = 0; i < getBaseUT().length; i++)
				ans.add(MaAnim.newIns(loader.loadFile(id.base, id, "maanim_" + getBaseUT()[i].toString() + ".txt"), old));

			int i = 1;
			FileData extra = loader.loadFile(id.base, id, "maanim_attack1.txt");
			while (extra != null) {
				ans.add(i + 2, MaAnim.newIns(extra, old));
				i++;
				extra = loader.loadFile(id.base, id, "maanim_attack" + i + ".txt");
			}

			return ans.toArray(new MaAnim[0]);
		}

		public MaModel getMM() {
			FileData fd = loader.loadFile(id.base, id, MM);
			if (fd == null && warn)
				CommonStatic.ctx.printErr(ErrType.WARN, "Corrupted mamodel found for " + id);
			return MaModel.newIns(fd);
		}

		public ResourceLocation getName() {
			return id;
		}

		public FakeImage getNum() {
			return FakeImage.read(loader.loadFile(id.base, id, SP));
		}

		public int getStatus() {
			return id.pack.equals("_local") ? 0 : 1;
		}
	}

	public static class SourceAnimSaver {

		private final ResourceLocation id;
		private final AnimCI anim;

		public SourceAnimSaver(ResourceLocation name, AnimCI animCI) {
			this.id = name;
			this.anim = animCI;
		}

		/**
		 * Delete animation
		 * @param unload If this variable is true, it means that this method is called for completely deleting process
		 */
		public void delete(boolean unload) {
			if(unload)
				anim.unload();

			CommonStatic.ctx.noticeErr(
					() -> Context.delete(CommonStatic.ctx.getWorkspaceFile("./" + id.pack + "/" + id.base + "/" + id.id)),
					ErrType.ERROR, "failed to delete animation: " + id);
		}

		public void saveAll() {
			saveData();
			saveImgs();
		}

		public void saveData() {
			try {
				write("imgcut.txt", anim.imgcut::write);
				write("mamodel.txt", anim.mamodel::write);
				if (id.base.equals(BasePath.ANIM)) {
					for (int i = 0; i < anim.anims.length; i++) {
						if ((JsonEncoder.backCompat && i < 4) || anim.anims[i].parts.length > 0)
							write("maanim_" + anim.types[i].toString() + ".txt", anim.anims[i]::write);
						else
							dispose("maanim_" + anim.types[i].toString() + ".txt");
					}
				} else if (id.base.equals(BasePath.SOUL))
					write(SourceAnimLoader.MA_SOUL[0], anim.anims[0]::write);
				else {
					for (int i = 0; i < 2; i++)
						if (anim.anims[i].parts.length > 0)
							write(SourceAnimLoader.MA_BACKGROUND[i], anim.anims[i]::write);
						else
							dispose(SourceAnimLoader.MA_BACKGROUND[i]);
				}
			} catch (IOException e) {
				CommonStatic.ctx.noticeErr(e, ErrType.ERROR, "Error during saving animation data: " + anim);
			}
		}

		public void saveIconDeploy() {
			if (anim.getUni() != null && anim.getUni() != CommonStatic.getBCAssets().slot[0] && id.base.equals(BasePath.ANIM))
				CommonStatic.ctx.noticeErr(() -> write(SourceAnimLoader.UNI, anim.getUni().getImg()),ErrType.ERROR,"Error during saving deploy icon: " + id);
		}
		public void saveIconDisplay() {
			if (anim.getEdi() != null)
				CommonStatic.ctx.noticeErr(() -> write(SourceAnimLoader.EDI, anim.getEdi().getImg()),ErrType.ERROR,"Error during saving display icon: " + id);
		}
		public void saveIconPreview() {
			if (anim.loader.getPreviewIcon() != null && id.base.equals(BasePath.ANIM))
				CommonStatic.ctx.noticeErr(() -> write(SourceAnimLoader.ICN, anim.loader.getPreviewIcon().getImg()), ErrType.ERROR,"Error during saving preview icon: " + id);
		}

		public void saveImgs() {
			saveSprite();
			saveIconDisplay();
			saveIconDeploy();
			saveIconPreview();
		}

		public void saveSprite() {
			CommonStatic.ctx.noticeErr(() -> write("sprite.png", anim.getNum()), ErrType.ERROR,
					"Error during saving sprite sheet: " + id);
		}

		private void write(String type, Consumer<PrintStream> con) throws IOException {
			File f = CommonStatic.ctx.getWorkspaceFile(id.getPath() + "/" + type);
			Context.check(f);
			PrintStream ps = new PrintStream(f, StandardCharsets.UTF_8.toString());
			con.accept(ps);
			ps.flush();
			ps.close();
		}

		private void dispose(String type) throws IOException {
			Context.delete(CommonStatic.ctx.getWorkspaceFile(id.getPath() + "/" + type));
		}

		private void write(String type, FakeImage img) throws IOException {
			File f = CommonStatic.ctx.getWorkspaceFile(id.getPath() + "/" + type);
			Context.check(f);
			Context.check(FakeImage.write(img, "PNG", f), "save", f);
		}

	}

	public static class Workspace extends Source {

		public static void loadAnimations(String id) {
			if (id == null)
				id = ResourceLocation.LOCAL;
			loadAnims(id, BasePath.ANIM);
			loadAnims(id, BasePath.SOUL);
			loadAnims(id, BasePath.BGEffect);
		}

		public static void loadAnims(String id, BasePath path) {
			File folder = CommonStatic.ctx.getWorkspaceFile("./" + id + "/" + path + "/");
			if (folder.exists() && folder.isDirectory()) {
				File[] animFiles = folder.listFiles();
				Arrays.sort(animFiles);
				for (File f : animFiles) {
					String sprite = "./" + id + "/" + path + "/" + f.getName() + "/sprite.png";
					if (f.isDirectory() && CommonStatic.ctx.getWorkspaceFile(sprite).exists()) {
						ResourceLocation rl = new ResourceLocation(id, f.getName(), path);
						AnimCE anim = new AnimCE(rl);
						AnimCE.map().put(f.getName(), anim);
					} else if (f.isDirectory())
						CommonStatic.ctx.printErr(ErrType.WARN, "Corrupted Sprite found in " + sprite);
				}
			}
		}

		public static void saveWorkspace(boolean auto) {
			AnimCE.map().values().forEach(auto ? AnimCE::autosave : AnimCE::save);
			for (UserPack up : UserProfile.getUserPacks()) {
				if (up.source instanceof Workspace) {
					if (!auto) {
						up.source.getAnims(BasePath.ANIM).forEach(a -> ((AnimCE) a).save());
						up.source.getAnims(BasePath.SOUL).forEach(a -> ((AnimCE) a).save());
						up.source.getAnims(BasePath.BGEffect).forEach(a -> ((AnimCE) a).save());
					}
					CommonStatic.ctx.noticeErr(() -> ((Workspace) up.source).save(up, auto), ErrType.WARN,
							"failed to save pack " + up.desc.names);
				}
				if (up.save != null && !up.save.cSt.isEmpty())
					CommonStatic.ctx.noticeErr(() -> saveData(up), ErrType.WARN, "failed to save data for " + up.desc.names);
			}
			for (Replay r : Replay.getMap().values())
				if (r.unsaved)
					r.write();
		}

		public static void saveData(UserPack up) throws Exception {
			File f = CommonStatic.ctx.getAuxFile("./saves/" + up.desc.id + ".packsave");
			Context.check(f);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
			fw.write(JsonEncoder.encode(up.save).toString());
			fw.close();
		}

		public static void validate(ResourceLocation rl) {
			String id = rl.id;
			int num = 0;
			while (CommonStatic.ctx.getWorkspaceFile(rl.getPath()).exists())
				rl.id = id + "_" + (num++);
		}

		public static String validateString(String str) {
			if (str == null || str.length() == 0)
				str = "no_name";
			str = str.replaceAll("[^0-9a-z_]", "_");
			if (str.charAt(0) < '0')
				str = "a_" + str;
			return str;
		}

		public static String validateWorkspace(String str) {
			String id = validateString(str);
			int num = 0;
			while (CommonStatic.ctx.getWorkspaceFile("./" + str).exists())
				str = id + "_" + (num++);
			return id;
		}

		public static String generatePackID() {
			String format = "abcdefghijklmnopqrstuvwxyz0123456789_ ";
			Random random = new Random();

			StringBuilder result = new StringBuilder();
			int tott = 7 + random.nextInt(24);

			while (result.length() < tott) {
				char ch = format.charAt(random.nextInt(format.length()));
				result.append(ch);
			}
			return result.toString();
		}

		private static FileData loadAnimFile(BasePath base, ResourceLocation id, String str) {
			String path = "./" + id.pack + "/" + (base == null ? BasePath.ANIM : base.toString()) + "/" + id.id + "/" + str;
			File f = CommonStatic.ctx.getWorkspaceFile(path);
			if (!f.exists())
				return null;

			try {
				File realFile = new File(f.getCanonicalPath()).getParentFile();
				if (realFile != null && !realFile.getName().equals(id.id))
					return null;
			} catch (Exception ignored) {
			}
			return new FDFile(f);
		}

		public Workspace(String id) {
			super(id);
		}

		@Override
		public void delete() {
			try {
				Context.delete(getFile(""));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void export(UserPack pack, String password, String parentPassword, boolean backComp, Consumer<Double> prog) throws Exception {
			JsonEncoder.backCompat = backComp;
			HashSet<AnimCE> anims = new HashSet<>();

			for (Enemy e : pack.enemies)
				if (e.anim instanceof AnimCE)
					addAnim(pack, (AnimCE)e.anim, anims);
				else if (backComp && e.anim instanceof AnimUD)
					CommonStatic.ctx.printErr(ErrType.WARN, "Animation for " + e + " uses BC animation, so pack won't work outside of fork BCU builds");
			for (Unit u : pack.units)
				for (Form f : u.forms)
					if (f.anim instanceof AnimCE)
						addAnim(pack, (AnimCE)f.anim, anims);
					else if (backComp && f.anim instanceof AnimUD)
						CommonStatic.ctx.printErr(ErrType.WARN, "Animation for " + f + " uses BC animation, so pack won't work outside of fork BCU builds");
			for (Soul s : pack.souls)
				addAnim(pack, (AnimCE)s.anim, anims);
			for (BackgroundEffect bge : pack.bgEffects)
				if (bge instanceof CustomBGEffect)
					addAnim(pack, (AnimCE)((CustomBGEffect)bge).anim, anims);
			for (StageMap sm : pack.mc.maps)
				for (Stage st : sm.list)
					for (Replay rep : st.recd)
						if (rep != null && rep.rl.pack.startsWith(".temp_"))
							rep.rl.pack = rep.rl.pack.substring(6);

			if (backComp)
				for (HashSet<AnimCI> aCS : this.anims) {
					if (aCS == null)
						continue;
					for (AnimCI anim : aCS)
						for (MaAnim ma : anim.anims)
							for (Part p : ma.parts)
								p.ints = Arrays.copyOf(p.ints, 5);
				}
			save(pack, false);
			String star = id.startsWith(".temp_") ? "./packs/" : "./exports/";
			String end = backComp ? ".pack.bcuzip" : ".userpack";
			File tar = CommonStatic.ctx.getAuxFile(star + Context.validate(pack.toString(), '-') + (pack.toString().equals(pack.getSID()) ? "" : "(" + pack.getSID() + ")") + end);
			File dst = CommonStatic.ctx.getAuxFile(star + end + ".temp");
			File src = CommonStatic.ctx.getWorkspaceFile("./" + id);
			if (tar.exists())
				Context.delete(tar);
			Context.check(dst);

			PackData.PackDesc desc = pack.desc.clone();
			desc.parentPassword = parentPassword != null ? PackLoader.getMD5(parentPassword.getBytes(StandardCharsets.UTF_8), 16) : null;
			DateFormat df = new SimpleDateFormat("MM dd yyyy HH:mm:ss");
			desc.exportDate = df.format(new Date());

			PackLoader.writePack(dst, src, desc, password, prog);
			Context.renameTo(dst, tar);

			for(AnimCE anim : anims) {
				anim.id.pack = ResourceLocation.LOCAL;
				anim.id.id = anim.id.id.replaceAll("^_mapped_", "");
				if (backComp) {
					for (MaAnim ma : anim.anims)
						for (Part p : ma.parts)
							p.ints = Arrays.copyOf(p.ints, 3);
					new SourceAnimSaver(anim.id, anim).saveData();
				}
			}
			if (backComp)
				for (HashSet<AnimCI> aCS : this.anims) {
					if (aCS == null)
						continue;
					for (AnimCI anim : aCS) {
						for (MaAnim ma : anim.anims)
							for (Part p : ma.parts)
								p.ints = Arrays.copyOf(p.ints, 3);
						new SourceAnimSaver(anim.id, anim).saveData();
					}
				}
			JsonEncoder.backCompat = false;
			save(pack, false);
		}

		private void addAnim(UserPack pack, AnimCE anim, HashSet<AnimCE> anims) {
			if (JsonEncoder.backCompat)
				for (MaAnim ma : anim.anims)
					for (Part p : ma.parts)
						p.ints = Arrays.copyOf(p.ints, 5);

			if (anim.id.pack.equals(ResourceLocation.LOCAL)) {
				if(!anims.add(anim)) {
					anim.id.pack = ResourceLocation.LOCAL;
					anim.id.id = anim.id.id.replaceAll("^_mapped_", "");
				}
				new SourceAnimSaver(new ResourceLocation(pack.getSID(), "_mapped_"+anim.id.id, anim.id.base), anim).saveAll();

				anim.id.pack = pack.getSID();
				anim.id.id = "_mapped_"+anim.id.id;
			}
			if (anim.id.pack.startsWith(".temp_"))
				anim.id.pack = anim.id.pack.substring(6);
		}

		public File getBGFile(Identifier<Background> id) {
			return getFile("./" + BasePath.BG + "/" + Data.trio(id.id) + ".png");
		}

		public File getCasFile(Identifier<CastleImg> id) {
			return getFile("./" + BasePath.CASTLE + "/" + Data.trio(id.id) + ".png");
		}

		public File getMusFile(Identifier<Music> id) {
			return getFile("./" + BasePath.MUSIC + "/" + Data.trio(id.id) + ".ogg");
		}

		public File getTraitIconFile(Identifier<Trait> id) {
			return getFile("./" + BasePath.TRAIT + "/" + Data.trio(id.id) + ".png");
		}

		public File getRandIconFile(String type, Identifier<?> id) { //id must be either AbEnemy or Abunit
			return getFile("./" + BasePath.RAND + "/" + type + "/" + Data.trio(id.id) + ".png");
		}

		@Override
		public FileData getFileData(String string) {
			return new FDFile(getFile(string));
		}

		@Override
		public String[] listFile(String path) {
			return getFile(path).list();
		}

		@Override
		public HashSet<AnimCI> getAnims(BasePath path) {
			byte ind = (byte)(path.equals(BasePath.ANIM) ? 0 : path.equals(BasePath.SOUL) ? 1 : path.equals(BasePath.BGEffect) ? 2 : -1);
			if (anims[ind] != null)
				return anims[ind];

			File folder = CommonStatic.ctx.getWorkspaceFile("./" + id + "/" + path + "/");
			if (folder.exists() && folder.isDirectory()) {
				File[] animFiles = folder.listFiles();
				anims[ind] = new LinkedHashSet<>(animFiles.length);
				Arrays.sort(animFiles);

				for (File f : animFiles) {
					String sprite = "./" + id + "/" + path + "/" + f.getName() + "/sprite.png";
					if (f.isDirectory() && CommonStatic.ctx.getWorkspaceFile(sprite).exists()) {
						anims[ind].add(new AnimCE(new ResourceLocation(id, f.getName(), path)));
					}
				}
				return anims[ind];
			}
			return new HashSet<>(1);
		}

		@Override
		public AnimCE loadAnimation(String name, BasePath base) {
			for (AnimCI anim : getAnims(base == null ? base = BasePath.ANIM : base))
				if (name.equals(anim.id.id) && anim.id.base.equals(base))
					return (AnimCE) anim;
			return new AnimCE(new ResourceLocation(id, name, base));
		}

		public void addAnimation(AnimCE anim) {
			anims[anim.id.base.equals(BasePath.ANIM) ? 0 : anim.id.base.equals(BasePath.SOUL) ? 1 :
					anim.id.base.equals(BasePath.BGEffect) ? 2 : -1].add(anim);
		}

		public void unloadAnimation(AnimCE anim) {
			anims[anim.id.base.equals(BasePath.ANIM) ? 0 : anim.id.base.equals(BasePath.SOUL) ? 1 :
					anim.id.base.equals(BasePath.BGEffect) ? 2 : -1].remove(anim);
		}

		@Override
		public VImg readImage(String path, int ind) {
			return vimg(VFile.getFile(getFile(path + "/" + Data.trio(ind) + ".png")));
		}

		@Override
		public VImg readImage(String path) {
			return vimg(VFile.getFile(getFile(path + ".png")));
		}

		public InputStream streamFile(String path) throws IOException {
			return new FileInputStream(getFile(path));
		}

		public OutputStream writeFile(String path) throws IOException {
			File f = getFile(path);
			Context.check(f);
			return new FileOutputStream(f);
		}

		protected void save(UserPack up, boolean auto) throws IOException {
			File f = auto ? CommonStatic.ctx.getWorkspaceFile("./_autosave/pack_" + id + ".json") : getFile("pack.json");
			Context.check(f);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
			fw.write(JsonEncoder.encode(up).toString());
			fw.flush();
			fw.close();
		}

		private File getFile(String path) {
			return CommonStatic.ctx.getWorkspaceFile("./" + id + "/" + path);
		}
	}

	public static class ZipSource extends Source {

		public final ZipDesc zip;

		public ZipSource(ZipDesc desc) {
			super(desc.desc.id);
			zip = desc;
		}

		@Override
		public void delete() {
			zip.delete();
		}

		@Override
		public FileData getFileData(String string) {
			return zip.tree.find(string).getData();
		}

		public File getPackFile() {
			return zip.getZipFile();
		}

		@Override
		public String[] listFile(String path) {
			VFile dir = zip.tree.find(path);
			if (dir == null)
				return null;
			Collection<VFile> col = dir.list();
			if (col == null)
				return null;
			String[] ans = new String[col.size()];
			int i = 0;
			for (VFile vf : col) {
				ans[i++] = vf.name;
			}
			return ans;
		}

		@Override
		public HashSet<AnimCI> getAnims(BasePath path) {
			byte ind = (byte)(path.equals(BasePath.ANIM) ? 0 : path.equals(BasePath.SOUL) ? 1 : path.equals(BasePath.BGEffect) ? 2 : -1);
			if (anims[ind] != null)
				return anims[ind];

			VFile folder = zip.tree.find( "./" + path + "/");
			if (folder != null && folder.countSubDire() > 0) {
				Collection<VFile> animFiles = folder.list();
				anims[ind] = new HashSet<>(animFiles.size());

				for (VFile f : animFiles) {
					String sprite = "./" + path + "/" + f.getName() + "/sprite.png";
					if (f.countSubDire() > 0 && zip.tree.find(sprite) != null) {
						anims[ind].add(new AnimCI(new SourceAnimLoader(new ResourceLocation(id, f.getName(), path), this::loadAnimationFile)));
					}
				}
				return anims[ind];
			}
			return new HashSet<>(1);
		}

		@Override
		public AnimCI loadAnimation(String name, BasePath base) {
			for (AnimCI anim : getAnims(base == null ? base = BasePath.ANIM : base))
				if (name.equals(anim.id.id) && anim.id.base.equals(base))
					return anim;
			return new AnimCI(new SourceAnimLoader(new ResourceLocation(id, name, base), this::loadAnimationFile));
		}

		@Override
		public VImg readImage(String path, int ind) {
			String fullPath = path.startsWith("./") ? path + "/" + Data.trio(ind) + ".png" : "./" + path + "/" + Data.trio(ind) + ".png";
			return Source.vimg(zip.tree.find(fullPath));
		}

		@Override
		public VImg readImage(String path) {
			String fullPath = path.startsWith("./") ? path + ".png" : "./" + path + ".png";
			return Source.vimg(zip.tree.find(fullPath));
		}

		public Workspace unzip(String password, Consumer<Double> prog) throws Exception {
			if (!zip.match(PackLoader.getMD5(password.getBytes(StandardCharsets.UTF_8), 16)))
				return null;
			File f = CommonStatic.ctx.getWorkspaceFile("./" + id + "/pack.json");
			File folder = f.getParentFile();
			if (folder.exists()) {
				if (!CommonStatic.ctx.confirmDelete(f))
					return null;
				Context.delete(f);
			}
			if (!folder.exists())
				Context.check(folder.mkdirs(), "create", folder);
			if (!f.exists())
				Context.check(f.createNewFile(), "create", f);
			Workspace ans = new Workspace(id);
			zip.unzip(id -> {
				File file = ans.getFile(id);
				Context.check(file);
				return file;
			}, prog);
			return ans;
		}

		private FileData loadAnimationFile(BasePath base, ResourceLocation id, String path) {
			VFile vf = zip.tree.find("./" + base.toString() + "/" + id.id + "/" + path);
			return vf == null ? null : vf.getData();
		}
	}

	public enum BasePath {
		ANIM("animations"),
		BG("backgrounds"),
		CASTLE("castles"),
		MUSIC("musics"),
		REPLAY("replays"),
		SOUL("souls"),
		TRAIT("traitIcons"),
		RAND("randIcons"),
		BGEffect("backgroundeffects");

		private final String path;

		BasePath(String str) {
			path = str;
		}

		public String toString() {
			return path;
		}
	}

	public final String id;
	protected final HashSet<AnimCI>[] anims = new HashSet[3];

	public Source(String id) {
		this.id = id;
	}

	public abstract void delete();

	public abstract FileData getFileData(String string);

	public abstract String[] listFile(String path);
	public abstract HashSet<AnimCI> getAnims(BasePath path);

	public abstract AnimCI loadAnimation(String name, BasePath base);

	/**
	 * read images from file. Use it
	 */
	public abstract VImg readImage(String path, int ind);
	public abstract VImg readImage(String path);

	private static VImg vimg(VFile vf) {
		if(vf == null)
			return null;
		return new VImg(vf);
	}
}