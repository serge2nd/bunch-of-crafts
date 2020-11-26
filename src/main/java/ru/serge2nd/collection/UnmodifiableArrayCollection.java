package ru.serge2nd.collection;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An unmodifiable collection backed directly by an array.
 * Note that further changes to the underlying array reflect this one.<br>
 * All implemented methods are final though the class itself is not.
 * This allows to create your own unmodifiable containers extending this one
 * to facilitate implementation of {@link Collection}-based interfaces.
 * @see Unmodifiable
 */
@RequiredArgsConstructor
public abstract class UnmodifiableArrayCollection<E> extends Unmodifiable<E> {
    protected final E[] array;

    //region Size & conversions

    @Override
    public final int      size()                              { return array.length; }
    @Override
    public final boolean  isEmpty()                           { return array.length == 0; }
    @Override
    public final Object[] toArray()                           { return Arrays.copyOf(array, array.length); }
    @Override @SuppressWarnings("unchecked,SuspiciousSystemArraycopy")
    public final <T> T[]  toArray(@NonNull T[] a)             { if (a.length < array.length)
                                                                    return (T[])Arrays.copyOf(array, array.length, a.getClass());
                                                                System.arraycopy(array, 0, a, 0, array.length);
                                                                return a;
                                                              }
    // @Override // since 11
    public final <T> T[] toArray(@NonNull IntFunction<T[]> g) { return toArray(g.apply(array.length)); }
    @Override
    public final String  toString()                           { return Arrays.toString(array); }
    //endregion

    //region Value presence & iterables

    @Override
    public final boolean        contains(Object o)                           { for (E e : array) if (Objects.equals(o, e)) return true; return false; }
    @Override
    public final boolean        containsAll(@NonNull Collection<?> coll)     { for (Object o : coll) if (!contains(o)) return false; return true; }
    @Override
    public final void           forEach(@NonNull Consumer<? super E> action) { for (E e : array) action.accept(e); }
    @Override
    public final Spliterator<E> spliterator()                                { return Arrays.spliterator(array); }
    @Override
    public final Stream<E>      stream()                                     { return Arrays.stream(array); }
    @Override
    public final Stream<E>      parallelStream()                             { return StreamSupport.stream(spliterator(), true); }
    @Override
    public final Iterator<E>    iterator()                                   { return new Itr<>(array); }

    static NoSuchElementException errOutOfBounds(int i, int len) {
        return new NoSuchElementException("index " + i + " not in [0; " + len + ")");
    }
    //endregion

    @RequiredArgsConstructor
    static class Itr<E> implements Iterator<E> {
        final E[] array; int i = 0;
        public boolean hasNext() { return i < array.length; }
        public E       next()    { if (i < array.length) return array[i++]; throw errOutOfBounds(i, array.length); }
        public void    remove()  { throw errNotModifiable(); }
    }
}
