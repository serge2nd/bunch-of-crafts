package ru.serge2nd.collection;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * An unmodifiable wrapper for the specified collection.
 * Note that further changes to the underlying collection reflect this one.<br>
 * All implemented methods are final though the class itself is not.
 * This allows to create your own unmodifiable containers extending this one
 * to facilitate implementation of {@link Collection}-based interfaces.
 * @see Unmodifiable
 */
public abstract class UnmodifiableCollection<E> extends Unmodifiable<E> implements Collection<E> {
    protected final Collection<E> collection;
    @SuppressWarnings("unchecked")
    public UnmodifiableCollection(Collection<? extends E> collection) {
        this.collection = (Collection<E>)collection;
    }

    //region Size & conversions

    @Override
    public final int      size()                      { return collection.size(); }
    @Override
    public final boolean  isEmpty()                   { return collection.isEmpty(); }
    @Override
    public final Object[] toArray()                   { return collection.toArray(); }
    @Override @SuppressWarnings("SuspiciousToArrayCall")
    public final <T> T[]  toArray(T[] a)              { return collection.toArray(a); }
    // @Override // since 11
    public final <T> T[]  toArray(IntFunction<T[]> g) { return toArray(g.apply(size())); }
    @Override
    public final String   toString()                  { return collection.toString(); }
    //endregion

    //region Value presence & iterables

    @Override
    public final boolean        contains(Object o)                  { return collection.contains(o); }
    @Override
    public final boolean        containsAll(Collection<?> coll)     { return collection.containsAll(coll); }
    @Override
    public final void           forEach(Consumer<? super E> action) { collection.forEach(action); }
    @Override
    public final Spliterator<E> spliterator()                       { return collection.spliterator(); }
    @Override
    public final Stream<E>      stream()                            { return collection.stream(); }
    @Override
    public final Stream<E>      parallelStream()                    { return collection.parallelStream(); }
    @Override
    public final Iterator<E>    iterator()                          { return new Itr<>(collection.iterator()); }
    //endregion

    @RequiredArgsConstructor
    static class Itr<E> implements Iterator<E> {
        final Iterator<E> it;
        public boolean hasNext() { return it.hasNext(); }
        public E       next()    { return it.next(); }
        public void    forEachRemaining(Consumer<? super E> action) { it.forEachRemaining(action); }
        public void    remove()  { throw errNotModifiable(); }
    }
}
