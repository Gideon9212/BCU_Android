package common.system;

import common.io.assets.Admin.StaticPermitted;

import java.util.HashMap;
import java.util.Map;

public interface BasedCopable<T, B> extends Cloneable, Copable<T> {

	@StaticPermitted(StaticPermitted.Type.TEMP)
	Map<Class<?>, Object> map = new HashMap<>();

	@Override
	@SuppressWarnings("unchecked")
	default T copy() {
		B base = (B) map.get(getClass());
		if (base == null)
			return null;
		return copy(base);
	}

	T copy(B b);

}
