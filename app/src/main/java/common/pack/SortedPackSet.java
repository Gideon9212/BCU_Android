package common.pack;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class SortedPackSet<T extends Comparable<? super T>> implements Set<T>, Cloneable, java.io.Serializable {
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
            SortedPackSet.this.remove(--ind);
        }
    }

    private Object[] arr;
    private int size = 0;
    private Comparator<T> comp = null;

    public SortedPackSet() {
        this(1);
    }

    public SortedPackSet(int siz) {
        arr = new Object[Math.max(1, siz)];
    }

    public SortedPackSet(T t) {
        arr = new Object[]{t};
    }

    public SortedPackSet(Collection<T> col) {
        arr = new Object[Math.max(col.size(), 1)];
        addAll(col);
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
        if (size == 0 || t == null || compareCheck(t, (T)arr[0]) < 0 || compareCheck(t, (T)arr[size-1]) > 0)
            return -1;
        return recInd(t, 0, size - 1);
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

    @SuppressWarnings("rawtypes")
    public void sort() {
        Object[] narr = new Object[size];
        System.arraycopy(arr, 0, narr, 0, size);
        Arrays.sort(narr, (Comparator)comp);
        System.arraycopy(narr, 0, arr, 0, size);
    }

    private int compareCheck(T o1, T o2) {
        if (comp != null)
            return comp.compare(o1, o2);
        return o1.compareTo(o2);
    }

    public void setComp(Comparator<T> c) {
        comp = c;
        sort();
    }

    @Override
    public boolean add(T t) {
        if (t == null || contains(t))
            return false;
        if (size == arr.length)
            arr = Arrays.copyOf(arr, arr.length * 2);

        arr[size++] = t;
        if (size > 1) {
            int pos = size - 2;
            while (pos >= 0 && compareCheck(t, get(pos)) < 0) {
                T cur = get(pos);
                arr[pos] = t;
                arr[pos + 1] = cur;
                pos--;
            }
        }

        return true;
    }

    public void set(int ind, T t) {
        remove(ind);
        add(t);
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
            if (elem == null || contains(elem))
                continue;
            ch = true;
            unsorted |= size > 0 && compareCheck((T) elem,(T) arr[size - 1]) < 0;
            arr[size++] = elem;
        }
        if (unsorted)
            sort();
        return ch;
    }

    /**
     * Adds all objects from a given collection that pass a specific predicate to this set
     * @param c The collection
     * @param filter The condition to test all the elements with
     * @return True if anything got added to the list
     */
    public boolean addIf(Collection<T> c, Predicate<? super T> filter) {
        if (c.size() + size >= arr.length) {
            int mul = 1;
            while (++mul * arr.length <= c.size() + size);
            arr = Arrays.copyOf(arr, arr.length * mul);
        }
        boolean res = false, unsorted = false;
        for (T elem : c) {
            if (elem == null || contains(elem) || !filter.test(elem))
                continue;
            res = true;
            unsorted |= size > 0 && compareCheck(elem,(T) arr[size - 1]) < 0;
            arr[size++] = elem;
        }
        if (unsorted)
            sort();
        return res;
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
        if (!(o instanceof Set))
            return false;
        Set<?> c = (Set<?>) o;
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

    /**
     * Returns a list containing only the elements these 2 lists both contain
     * @param col The other list
     * @return A list containing only the elements these 2 lists both contain
     */
    public SortedPackSet<T> inCommon(Collection<T> col) {
        SortedPackSet<T> np = new SortedPackSet<>();
        for (T item : col) {
            if (contains(item))
                np.add(item);
            if (np.size() == size)
                break;
        }
        return np;
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
    public SortedPackSet<T> clone() {
        return new SortedPackSet<>(this);
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
