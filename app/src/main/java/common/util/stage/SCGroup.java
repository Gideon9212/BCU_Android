package common.util.stage;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.system.BasedCopable;
import common.util.Data;

import java.util.Arrays;

@JsonClass
public class SCGroup extends Data implements BasedCopable<SCGroup, Integer> {

	@JsonField
	public final int id;
	@JsonField
	private final int[] max = { -1, -1, -1, -1 };

	@JsonClass.JCConstructor
	public SCGroup() {
		id = -1;
	}

	public SCGroup(int ID, int... ns) {
		id = ID;
		System.arraycopy(ns, 0, max, 0, ns.length);
	}

	@Override
	public SCGroup copy(Integer id) {
		return new SCGroup(id, max);
	}

	public int getMax(int star) {
		if (max[star] == -1 && star > 0)
			return getMax(star - 1);
		return max[star];
	}

	public void setMax(int val, int star) {
		if (star == -1) {
			Arrays.fill(max, val);
			return;
		}
		max[star] = val;
		if (star > 0 && max[star - 1] < 0)
			setMax(val, star - 1);
	}

	@Override
	public String toString() {
		String str = trio(id) + " - " + max[0];
		String temp = "";
		for (int i = 1; i < 4; i++)
			if (max[i] == -1)
				return str;
			else if (max[i] == max[i - 1])
				temp += "," + max[i];
			else {
				str += temp + "," + max[i];
				temp = "";
			}
		return str;
	}

}
