package ru.serge2nd.collection;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;

/**
 * An unmodifiable wrapper for the specified list.
 * @see UnmodifiableCollection
 * @see Unmodifiable
 */
public abstract class UnmodifiableList<E> extends UnmodifiableCollection<E> implements List<E> {
    protected final List<E> list()                  { return (List<E>)collection; }
    public UnmodifiableList(List<? extends E> list) { super(list); }

    @Override @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public final boolean equals(Object o)          { return o == this || collection.equals(o); }
    @Override
    public final int     hashCode()                { return collection.hashCode(); }
    @Override
    public final E       get(int index)            { return list().get(index); }
    @Override
    public final int     indexOf(Object o)         { return list().indexOf(o); }
    @Override
    public final int     lastIndexOf(Object o)     { return list().lastIndexOf(o); }
    @Override
    public final List<E> subList(int from, int to) {
        if (from == to) return emptyList();
        return new UnmodifiableListImpl<>(list().subList(from, to));
    }

    @Override
    public final ListIterator<E> listIterator()      { return listIterator(0); }
    @Override
    public final ListIterator<E> listIterator(int i) { return new ListItr<>(list().listIterator(i)); }

    static class ListItr<E> extends Itr<E> implements ListIterator<E> {
        ListItr(ListIterator<E> it)    { super(it); }
        public boolean hasPrevious()   { return it().hasPrevious(); }
        public E       previous()      { return it().previous(); }
        public int     nextIndex()     { return it().nextIndex(); }
        public int     previousIndex() { return it().previousIndex(); }
        public void    set(E e)        { throw errNotModifiable(); }
        public void    add(E e)        { throw errNotModifiable(); }
        ListIterator<E> it() { return (ListIterator<E>)it; }
    }
}
