package common.util.stage;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.IndexContainer.IndexCont;
import common.pack.IndexContainer.Indexable;
import common.pack.PackData;
import common.pack.Source;
import common.pack.UserProfile;
import common.system.VImg;
import common.system.files.VFile;

import java.util.Queue;

@IndexCont(CastleList.class)
@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class CastleImg implements Indexable<CastleList, CastleImg> {

	@JsonClass.JCIdentifier
	@JsonField
	public Identifier<CastleImg> id;
	@JsonField(defval = "0")
	public float boss_spawn = 0;
	@JsonField(defval = "1000")
	public int scale = 1000;
	@JsonField(defval = "0")
	public int center;
	public VImg img;

	public static void loadBossSpawns() {
		Queue<String> legendData = VFile.readLine("./org/data/enemyCastleDataLegend.csv");
		int index = 0;
		String str;
		while ((str = legendData.poll()) != null) {
			String[] data = str.split(",");
			int y = Integer.parseInt(data[0]);
			int z = Integer.parseInt(data[2]);
			if (y == -999) break;
			CastleImg castle = CastleList.getList("000000").get(index++);
			if (castle != null)
				castle.boss_spawn = CommonStatic.bossSpawnPoint(y, z);
		}
		index = 0;
		Queue<String> empireData = VFile.readLine("./org/data/enemyCastleData0.csv");
		while ((str = empireData.poll()) != null) {
			String[] data = str.split(",");
			int y = Integer.parseInt(data[0]);
			int z = Integer.parseInt(data[2]);
			if (y == -999) break;
			CastleImg castle = CastleList.getList("000001").get(index++);
			if (castle != null)
				castle.boss_spawn = CommonStatic.bossSpawnPoint(y, z);
		}
		index = 0;
		Queue<String> futureData = VFile.readLine("./org/data/enemyCastleData1.csv");
		while ((str = futureData.poll()) != null) {
			String[] data = str.split(",");
			int y = Integer.parseInt(data[0]);
			int z = Integer.parseInt(data[2]);
			if (y == -999) break;
			CastleImg castle = CastleList.getList("000002").get(index++);
			if (castle != null)
				castle.boss_spawn = CommonStatic.bossSpawnPoint(y, z);
		}
		index = 0;
		Queue<String> cosmoData = VFile.readLine("./org/data/enemyCastleData1.csv");
		while ((str = cosmoData.poll()) != null) {
			String[] data = str.split(",");
			int y = Integer.parseInt(data[0]);
			int z = Integer.parseInt(data[2]);
			if (y == -999) break;
			CastleImg castle = CastleList.getList("000003").get(index++);
			if (castle != null)
				castle.boss_spawn = CommonStatic.bossSpawnPoint(y, z);
		}
	}


	public CastleImg() {
	}

	public CastleImg(Identifier<CastleImg> id, VImg img) {
		this.id = id;
		this.img = img;
	}

	@Override
	public Identifier<CastleImg> getID() {
		return id;
	}

	@OnInjected
	public void onInjected() {
		PackData.UserPack pack = UserProfile.getUserPack(id.pack);

		if (pack == null)
			return;

		img = pack.source.readImage(Source.BasePath.CASTLE.toString(), id.id);

		if (UserProfile.isOlderPack(pack, "0.5.6.0")) {
			boss_spawn = 828.5f;
		}
	}

	@Override
	public String toString() {
		if(id == null)
			return super.toString();

		return id.toString();
	}
}
