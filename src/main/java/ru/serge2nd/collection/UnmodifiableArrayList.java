package ru.serge2nd.collection;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.RandomAccess;

import static java.util.Arrays.deepEquals;
import static java.util.Collections.emptyList;

/**
 * An unmodifiable list backed directly by an array.<br>
 * <b>Note:</b> the {@link #subList(int, int)} method does not fully meet the contract of the {@link List#subList(int, int)}.
 * @see UnmodifiableArrayCollection
 * @see Unmodifiable
 */
public abstract class UnmodifiableArrayList<E> extends UnmodifiableArrayCollection<E> implements List<E>, RandomAccess {
    public UnmodifiableArrayList(E[] array) { super(array); }

    /**
     * <b>Note:</b> this implementation returns the copy of the range instead of a view
     * violating the contract of the {@link List#subList(int, int)} in this sense.
     * @see List#subList(int, int) List.subList() <i>(the contract not fully matched)</i>
     */
    @Override
    public final List<E> subList(int from, int to) {
        if (checkClosedRange(from) == checkClosedRange(to)) return emptyList();
        return new UnmodifiableArrayListImpl<>(Arrays.copyOfRange(array, from, to));
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof List)) return false;
        if (o instanceof UnmodifiableArrayList) return deepEquals(((UnmodifiableArrayList<?>)o).array, array);

        List<?> other = (List<?>)o;
        if (other.size() != array.length) return false;

        int i = 0; for (Object otherElem : other)
            if (!Objects.equals(otherElem, array[i++]))
                return false;
        return true;
    }
    @Override
    public final int hashCode()            { return Arrays.hashCode(array); }
    @Override
    public final E   get(int i)            { return array[checkRange(i)]; }
    @Override
    public final int indexOf(Object o)     { for (int i = 0; i < array.length; i++) if (Objects.equals(o, array[i])) return i; return -1; }
    @Override
    public final int lastIndexOf(Object o) { for (int i = array.length - 1; i >= 0; i--) if (Objects.equals(o, array[i])) return i; return -1; }

    @Override
    public final ListIterator<E> listIterator()      { return listIterator(0); }
    @Override
    public final ListIterator<E> listIterator(int i) { return new ListItr<>(array, checkClosedRange(i)); }

    int checkRange(int i)       { if (0 > i || i >= array.length) throw errOutOfBounds(i, array.length); return i; }
    int checkClosedRange(int i) { if (0 > i || i > array.length) throw errOutOfBounds(i, array.length); return i; }

    static class ListItr<E> extends Itr<E> implements ListIterator<E> {
        ListItr(E[] array, int i)      { super(array); this.i = i; }
        public boolean hasPrevious()   { return i > 0; }
        public E       previous()      { if (i > 0) return array[--i]; throw errOutOfBounds(i - 1, array.length); }
        public int     nextIndex()     { return i; }
        public int     previousIndex() { return i - 1; }
        public void    set(E e)        { throw errNotModifiable(); }
        public void    add(E e)        { throw errNotModifiable(); }
    }
}
