package common.pack;

import common.pack.FixIndexList.FixIndexMap;

import java.lang.annotation.*;

public interface IndexContainer {

	interface Constructor<T extends R, R extends Indexable<?, R>> {

		T get(Identifier<R> id);

	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface ContGetter {
	}

	interface Indexable<R extends IndexContainer, T extends Indexable<R, T>> {

		@SuppressWarnings("unchecked")
		default R getCont() {
			return (R) getID().getCont();
		}

		Identifier<T> getID();

	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface IndexCont {

		Class<? extends IndexContainer> value();

	}

	interface Reductor<R, T> {

		R reduce(R r, T t);

	}

	interface SingleIC<T extends Indexable<?, T>> extends IndexContainer {

		default T add(Constructor<T, T> con) {
			int ind = getFIM().nextInd();
			T ans = con.get(getID(ind));
			getFIM().set(ind, ans);
			return ans;
		}

		default T add(int ind, Constructor<T, T> con) {
			T ans = con.get(getID(ind));
			getFIM().set(ind, ans);
			return ans;
		}

		FixIndexMap<T> getFIM();

		@SuppressWarnings("unchecked")
		@Override
		@Deprecated
		default <X extends R, R extends Indexable<?, R>> Identifier<R> getID(Class<X> cls, int ind) {
			return (Identifier<R>) getID(ind);
		}

		default Identifier<T> getID(int ind) {
			return new Identifier<>(getSID(), getFIM().cls, ind);
		}

		@Override
		@Deprecated
		@SuppressWarnings("rawtypes")
		default <R> R getList(Class cls, Reductor<R, FixIndexMap> func, R def) {
			return func.reduce(def, getFIM());
		}

		default Identifier<T> getNextID() {
			return new Identifier<>(getSID(), getFIM().cls, getFIM().nextInd());
		}

		@SuppressWarnings("unchecked")
		@Override
		@Deprecated
		default <X extends R, R extends Indexable<?, R>> Identifier<R> getNextID(Class<X> cls) {
			return (Identifier<R>) getNextID();
		}

	}

	default <T extends R, R extends Indexable<?, R>> T add(FixIndexMap<R> map, Constructor<T, R> con) {
		int ind = map.nextInd();
		T ans = con.get(getID(map.cls, ind));
		map.set(ind, ans);
		return ans;
	}

	default <T extends R, R extends Indexable<?, R>> T add(FixIndexMap<R> map, int ind, Constructor<T, R> con) {
		T ans = con.get(getID(map.cls, ind));
		map.set(ind, ans);
		return ans;
	}

	default <T extends R, R extends Indexable<?, R>> Identifier<R> getID(Class<T> cls, int ind) {
		return new Identifier<>(getSID(), cls, ind);
	}

	@SuppressWarnings("rawtypes")
	<R> R getList(Class cls, Reductor<R, FixIndexMap> func, R def);

	default <T extends R, R extends Indexable<?, R>> Identifier<R> getNextID(Class<T> cls) {
		int id = getList(cls, (r, l) -> l.nextInd(), 0);
		return new Identifier<>(getSID(), cls, id);
	}

	String getSID();

}