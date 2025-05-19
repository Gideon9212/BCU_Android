package common.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.io.json.JsonClass;
import common.io.json.JsonClass.RType;
import common.io.json.JsonDecoder;
import common.io.json.JsonEncoder;
import common.io.json.JsonField;
import common.io.json.JsonField.IOType;
import common.pack.IndexContainer.Indexable;
import common.util.Data;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

@JsonClass(read = RType.FILL)
public class FixIndexList<T> extends Data {

	@JsonClass(read = RType.FILL)
	public static class FixIndexMap<T extends Indexable<?, ?>> extends FixIndexList<T> implements Iterable<T> {

		private class Itr implements Iterator<T> {

			private int ind = 0;

			@Override
			public boolean hasNext() {
				return ind < size;
			}

			@Override
			public T next() {
				return raw ? arr[ind++] : arr[order[ind++]];
			}
		}

		@JsonField
		private int[] order;
		public final Class<T> cls;
		public boolean raw = false;

		public FixIndexMap(Class<T> cls) {
			super(cls);
			this.cls = cls;
			order = new int[arr.length];
		}

		@Override
		public void clear() {
			super.clear();
			Arrays.fill(order, 0);
		}

		@Override
		public void expand(int size) {
			super.expand(size);
			if (size <= order.length)
				return;
			order = Arrays.copyOf(order, size);
		}

		@Override
		public List<T> getList() {
			List<T> ans = new ArrayList<>(size);
			for (int i = 0; i < size; i++)
				ans.add(arr[order[i]]);
			return ans;
		}

		@NotNull
		@Override
		public Iterator<T> iterator() {
			return new Itr();
		}

		public void reorder(int ori, int fin) {
			if (ori < fin) {
				int val = order[ori];
				System.arraycopy(order, ori + 1, order, ori, fin - ori);
				order[fin] = val;
			} else if (ori > fin) {
				int val = order[ori];
				if (ori - fin >= 0) System.arraycopy(order, fin, order, fin + 1, ori - fin);
				order[fin] = val;
			}
		}

		@Override
		public T get(int ind) {
			if(ind < 0 || ind >= order.length)
				return getRaw(ind);

			return arr[order[ind]];
		}

		@Override
		public int indexOf(T thing) {
			for (int i = 0; i < size; i++)
				if (arr[order[i]] != null && arr[order[i]].equals(thing))
					return order[i];
			return -1;
		}

		public T getRaw(int id) {
			if (id < arr.length) {
				if (id < 0)
					return null;
				if (arr[id] != null)
					return arr[id];
			}
			return getFromID(id);
		}

		public T getFromID(int id) {
			if (id >= 0 && id < size && arr[order[id]].getID().id == id)
				return arr[order[id]];

			for (int i = size - 1; i >= 0; i--)
				if (arr[order[i]].getID().id == id)
					return arr[order[i]];
			return null;
		}

		public void reset() {
			int ind = 0;
			for (int i = 0; i < arr.length; i++)
				if (arr[i] != null) {
					order[ind++] = i;
					if (ind == size)
						return;
				}
		}

		@Override
		public void set(int ind, T t) {
			while (arr.length <= ind)
				expand(arr.length * 2);
			boolean fill = arr[ind] == null && t != null;
			boolean dele = arr[ind] != null && t == null;
			super.set(ind, t);
			if (fill) {
				order[size - 1] = ind;
			}
			if (dele) {
				boolean rarr = false;
				for (int i = 0; i < size; i++) {
					if (!rarr && order[i] == ind)
						rarr = true;
					if (rarr)
						order[i] = order[i + 1];
				}
				order[size] = 0;
			}

		}

		@SuppressWarnings("unchecked")
		public T[] toArray() {
			return getList().toArray((T[]) Array.newInstance(cls, 0));
		}

		public List<T> getRawList() {
			return super.getList();
		}

		@SuppressWarnings("unchecked")
		public T[] toRawArray() {
			return getRawList().toArray((T[]) Array.newInstance(cls, 0));
		}
	}

	protected T[] arr;
	protected int size = 0;

	@SuppressWarnings("unchecked")
	public FixIndexList(Class<T> cls) {
		arr = (T[]) Array.newInstance(cls, 1);
	}

	public void add(T t) {
		int ind = nextInd();
		set(ind, t);
	}

	public void clear() {
		Arrays.fill(arr, null);
		size = 0;
	}

	public boolean contains(T t) {
		if (t == null)
			return false;
		for (T a : arr)
			if (t.equals(a))
				return true;
		return false;
	}

	public void expand(int size) {
		if (size <= arr.length)
			return;
		arr = Arrays.copyOf(arr, size);
	}

	public void forEach(BiConsumer<Integer, T> c) {
		for (int i = 0; i < arr.length; i++)
			if (arr[i] != null)
				c.accept(i, arr[i]);
	}

	public T get(int ind) {
		if (ind < 0 || ind >= arr.length)
			return null;
		return arr[ind];
	}

	/**
	 * Returns a list containing all of the contents of this FixIndexList
	 * Note that doing any changes to this list will not affect the FixIndexList
	 * @return New list containing all elements in this FixIndexList
	 */
	public List<T> getList() {
		List<T> ans = new ArrayList<>(size);
		for (T t : arr)
			if (t != null)
				ans.add(t);
		return ans;
	}

	public int indexOf(T tar) {
		for (int i = 0; i < arr.length; i++)
			if (arr[i] != null && arr[i].equals(tar))
				return i;
		return -1;
	}

	public int nextInd() {
		for (int i = 0; i < arr.length; i++)
			if (arr[i] == null)
				return i;
		expand(arr.length * 2);
		return arr.length / 2;
	}

	public void remove(T t) {
		if (t == null)
			return;
		for (int i = 0; i < arr.length; i++)
			if (arr[i] == t)
				set(i, null);
	}

	public void set(int ind, T t) {
		while (arr.length <= ind)
			expand(arr.length * 2);
		if (arr[ind] != null)
			size--;
		if (t != null)
			size++;
		arr[ind] = t;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	@JsonField(tag = "data", io = IOType.R)
	public void zgen(JsonElement e) {
		@SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>) arr.getClass().getComponentType();
		JsonArray jarr = e.getAsJsonArray();
		for (int i = 0; i < jarr.size(); i++) {
			JsonObject ji = jarr.get(i).getAsJsonObject();
			int ind = ji.get("ind").getAsInt();
			T val = JsonDecoder.decode(ji.get("val"), cls);
			this.set(ind, val);
		}

	}

	@JsonField(tag = "data", io = IOType.W)
	public JsonElement zser() {
		JsonArray data = new JsonArray();
		for (int i = 0; i < arr.length; i++)
			if (arr[i] != null) {
				JsonObject ent = new JsonObject();
				ent.addProperty("ind", i);
				ent.add("val", JsonEncoder.encode(arr[i]));
				data.add(ent);
			}
		return data;
	}
}
