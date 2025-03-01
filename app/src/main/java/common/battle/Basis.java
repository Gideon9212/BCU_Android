package common.battle;

import common.io.json.JsonClass;
import common.io.json.JsonClass.RType;
import common.io.json.JsonField;
import common.util.Data;

@JsonClass(read = RType.FILL)
public abstract class Basis extends Data {

	@JsonField
	public String name;

	/**
	 * get combo effect data
	 */
	public abstract int getInc(int type);

	/**
	 * get Treasure object
	 */
	public abstract Treasure t();

	@Override
	public String toString() {
		return name;
	}

}
