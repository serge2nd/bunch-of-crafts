package ru.serge2nd.collection;

import lombok.NonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;

/**
 * Stubbing implementations of the {@link Collection} and {@link List} mutators.
 * Also contains factory methods to create unmodifiable collections.
 */
@SuppressWarnings("EqualsWhichDoesntCheckParameterClass,RedundantUnmodifiable")
public abstract class Unmodifiable<E> implements Collection<E>, NotList<E> {

    //region Factory methods

    /**
     * Returns an unmodifiable list backed directly by the specified array
     * (further changes to the given array reflect this one).
     * @param elements array to be wrapped
     * @return an unmodifiable list backed directly by this array
     */
    @SafeVarargs
    public static <E> List<E> of(@NonNull E... elements) {
        if (elements.length == 0) return emptyList();
        return new UnmodifiableArrayListImpl<>(elements);
    }

    /**
     * Returns an unmodifiable list backed directly by the specified list
     * (further changes to the given list reflect this one).
     * @param list list to be wrapped
     * @return an unmodifiable list backed directly by this list.
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> ofList(@NonNull List<? extends E> list) {
        if (emptyList()        == list                 ||
            SINGLETON_LIST_CLS == list.getClass()      ||
            BASE_CLS.isAssignableFrom(list.getClass()) ||
            STD_BASE_CLS.isAssignableFrom(list.getClass()))
            return (List<E>)list;
        return new UnmodifiableListImpl<>(list);
    }

    /**
     * Returns an unmodifiable set backed directly by the specified set
     * (further changes to the given set reflect this one).
     * @param set set to be wrapped
     * @return an unmodifiable set backed directly by this set.
     */
    @SuppressWarnings("unchecked")
    public static <E> Set<E> ofSet(@NonNull Set<? extends E> set) {
        if (emptySet()          == set                ||
            SINGLETON_CLS       == set.getClass()     ||
            BASE_CLS.isAssignableFrom(set.getClass()) ||
            STD_BASE_CLS.isAssignableFrom(set.getClass()))
            return (Set<E>)set;
        return new UnmodifiableSetImpl<>(set);
    }

    /**
     * Returns an unmodifiable collection backed directly by the specified collection
     * (further changes to the given collection reflect this one).
     * @param c collection to be wrapped
     * @return an unmodifiable collection backed directly by this collection.
     */
    @SuppressWarnings("unchecked")
    public static <E> Collection<E> ofCollection(@NonNull Collection<? extends E> c) {
        if (c instanceof List) return ofList((List<E>)c);
        if (c instanceof Set) return ofSet((Set<E>)c);
        if (BASE_CLS.isAssignableFrom(c.getClass()) ||
            STD_BASE_CLS.isAssignableFrom(c.getClass()))
            return (Collection<E>)c;
        return new UnmodifiableCollectionImpl<>(c);
    }
    //endregion

    // region Collection mutators

    @Override
    public final boolean add(E e)                              { throw errNotModifiable(); }
    @Override
    public final boolean addAll(Collection<? extends E> coll)  { throw errNotModifiable(); }
    @Override
    public final boolean remove(Object o)                      { throw errNotModifiable(); }
    @Override
    public final boolean removeIf(Predicate<? super E> filter) { throw errNotModifiable(); }
    @Override
    public final boolean removeAll(Collection<?> coll)         { throw errNotModifiable(); }
    @Override
    public final boolean retainAll(Collection<?> coll)         { throw errNotModifiable(); }
    @Override
    public final void    clear()                               { throw errNotModifiable(); }
    // endregion

    //region List mutators

    @Override
    public final E       set(int index, E element)                    { throw errNotModifiable(); }
    @Override
    public final void    add(int index, E element)                    { throw errNotModifiable(); }
    @Override
    public final boolean addAll(int index, Collection<? extends E> c) { throw errNotModifiable(); }
    @Override
    public final E       remove(int index)                            { throw errNotModifiable(); }
    @Override
    public final void    replaceAll(UnaryOperator<E> operator)        { throw errNotModifiable(); }
    @Override
    public final void    sort(Comparator<? super E> c)                { throw errNotModifiable(); }
    //endregion

    static UnsupportedOperationException errNotModifiable() { return new UnsupportedOperationException("this collection not modifiable"); }

    static final class UnmodifiableArrayListImpl<E> extends UnmodifiableArrayList<E> {
        UnmodifiableArrayListImpl(E[] array) { super(array); }
    }
    static final class UnmodifiableListImpl<E> extends UnmodifiableList<E> {
        UnmodifiableListImpl(List<? extends E> list) { super(list); }
    }
    static final class UnmodifiableSetImpl<E> extends UnmodifiableCollection<E> implements Set<E> {
        UnmodifiableSetImpl(Set<? extends E> set) { super(set); }
        public boolean equals(Object obj) { return this == obj || collection.equals(obj); }
        public int     hashCode()         { return collection.hashCode(); }
    }
    static final class UnmodifiableCollectionImpl<E> extends UnmodifiableCollection<E> {
        UnmodifiableCollectionImpl(Collection<? extends E> collection) { super(collection); }
    }

    static final Class<?> BASE_CLS           = Unmodifiable.class;
    static final Class<?> STD_BASE_CLS       = unmodifiableCollection(emptySet()).getClass();
    static final Class<?> SINGLETON_CLS      = singleton(null).getClass();
    static final Class<?> SINGLETON_LIST_CLS = singletonList(null).getClass();
}
