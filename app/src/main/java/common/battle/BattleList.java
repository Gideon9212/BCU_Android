package common.battle;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class BattleList<T extends Comparable<? super T>> implements Collection<T>, java.io.Serializable {
    static final long serialVersionUID = 1L;

    public class Itr implements Iterator<T> {
        private int ind = 0;
        @Override
        public boolean hasNext() {
            return ind < size;
        }
        @Override
        public T next() {
            return (T)arr[ind++];
        }
        @Override
        public void remove() {
            BattleList.this.remove(--ind);
        }
    }

    public Object[] arr;
    public int size = 0;

    public BattleList() {
        initCapacity(1);
    }

    public BattleList(int cap) {
        initCapacity(cap);
    }

    public void initCapacity(int num) {
        arr = new Object[num];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    public int indexOf(T t) {
        if (size == 0 || t == null || t.compareTo((T)arr[0]) < 0 || t.compareTo((T)arr[size-1]) > 0)
            return -1;
        //return recInd(t, 0, size - 1);
        for (int i = 0; i < size; i++)
            if (arr[i].equals(t))
                return i;
        return -1;
    }

    private int recInd(T t, int f, int l) {
        int mid = f + (l - f) / 2;
        if (arr[mid].equals(t))
            return mid;
        int c = t.compareTo((T)arr[mid]);
        if (c > 0)
            return recInd(t, mid + 1, l);
        else if (f < l)
            return recInd(t, f, mid - 1);
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf((T)o) != -1;
    }

    @Override
    public Itr iterator() {
        return new Itr();
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(arr, size);
    }

    @NotNull
    @Override
    public<R> R[] toArray(@NotNull R[] a) {
        return (R[]) Arrays.copyOf(arr, size, a.getClass());
    }

    public void sort() {
        Object[] narr = new Object[size];
        System.arraycopy(arr, 0, narr, 0, size);
        Arrays.sort(narr);
        System.arraycopy(narr, 0, arr, 0, size);
    }

    @Override
    public boolean add(T t) {
        if (t == null)
            return false;
        if (size == arr.length)
            arr = Arrays.copyOf(arr, arr.length * 2);

        arr[size++] = t;
        if (size > 1) {
            int pos = size - 2;
            while (pos >= 0 && t.compareTo(get(pos)) < 0) {
                T cur = get(pos);
                arr[pos] = t;
                arr[pos + 1] = cur;
                pos--;
            }
        }
        return true;
    }

    public T set(int ind, T t) {
        T prev = (T)arr[ind];
        remove(ind);
        add(t);
        return prev;
    }

    public T get(int ind) {
        return (T)arr[ind];
    }

    @Override
    public boolean remove(Object o) {
        int ind = indexOf((T)o);
        if (ind == -1)
            return false;
        remove(ind);
        return true;
    }

    public T remove(int ind) {
        if (ind >= size || ind < 0)
            throw new ArrayIndexOutOfBoundsException("Index:" + ind + ", Size:" + size);
        T val = (T)arr[ind];

        for (int i = ind; i < size - 1; i++)
            arr[i] = arr[i + 1];
        arr[--size] = null;
        return val;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean rem = false;
        for (T t : (Iterable<T>) c) rem |= remove(t);
        return rem;
    }

    @Override
    public boolean addAll(Collection c) {
        if (c.size() + size >= arr.length) {
            int mul = 1;
            while (++mul * arr.length <= c.size() + size);
            arr = Arrays.copyOf(arr, arr.length * mul);
        }
        boolean ch = false, unsorted = false;
        for (Object elem : c) {
            if (elem == null)
                continue;
            ch = true;
            unsorted |= size > 0 && ((Comparable<T>)elem).compareTo((T)arr[size - 1]) < 0;
            arr[size++] = elem;
        }
        if (unsorted)
            sort();
        return ch;
    }

    @Override
    public void clear() {
        while (!isEmpty())
            remove(0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof BattleList))
            return false;
        BattleList<?> c = (BattleList<?>)o;
        if (c.size() != size)
            return false;
        try {
            return containsAll(c);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int h = super.hashCode();
        for (T obj : this)
            if (obj != null)
                h += obj.hashCode();
        return h;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean s = false;
        for (int i = 0; i < size; i++)
            if (!c.contains(arr[i])) {
                remove(i);
                i--;
                s = true;
            }
        return s;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c)
            if (!contains(o))
                return false;
        return true;
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "[]";
        StringBuilder str = new StringBuilder("[");
        for (Object obj : this)
            str.append(obj.toString()).append(", ");
        str.replace(str.length() - 2, str.length() - 1, "]");
        return str.toString();
    }
}
