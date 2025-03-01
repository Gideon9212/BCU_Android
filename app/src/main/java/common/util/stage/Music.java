package common.util.stage;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.IndexContainer.IndexCont;
import common.pack.IndexContainer.Indexable;
import common.pack.PackData;
import common.system.files.FileData;
import common.util.Data;
import org.jetbrains.annotations.NotNull;

@JsonClass
@IndexCont(PackData.class)
@JsonClass.JCGeneric(Identifier.class)
public class Music implements Indexable<PackData, Music>, Comparable<Music> {

	@JsonField
	@JsonClass.JCIdentifier
	public final Identifier<Music> id;
	@JsonField(defval = "0")
	public long loop;
	@JsonField(defval = "isEmpty")
	public String name = "";

	public FileData data;

	@JsonClass.JCConstructor
	@Deprecated
	public Music() {
		id = null;
	}

	public Music(Identifier<Music> id, FileData fd) {
		this.id = id;
		data = fd;
	}

	public Music(Identifier<Music> id, FileData fd, Music m) {
		this(id, fd);
		if (m != null) {
			loop = m.loop;
			name = m.name;
		}
	}

	@Override
	public Identifier<Music> getID() {
		return id;
	}

	@Override
	public String toString() {
		if (id != null) {
			if (!name.isEmpty())
				return name + " (" + Data.trio(id.id) + ".ogg - " + id.pack + ")";
			return Data.trio(id.id) + ".ogg - " + id.pack;
		} else
			return name;
	}

	@Override
	public int compareTo(@NotNull Music o) {
		if (id == null) {
			if (o.id == null)
				return 0;
			return -1;
		} else if (o.id == null)
			return 1;
		return id.compareTo(o.id);
	}

	public static boolean valid(String str) {
		if (str.length() != 7 || !str.contains(Data.trio(CommonStatic.parseIntN(str))))
			return false;
		return isMusic(str);
	}

	public static boolean isMusic(String str) {
		return str.endsWith(".ogg");
	}
}
