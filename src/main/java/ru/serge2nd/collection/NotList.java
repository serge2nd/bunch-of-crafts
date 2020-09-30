package ru.serge2nd.collection;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Provides the same signatures as the {@link List} mutators (excluding inherited).
 * Created to reduce duplicate code in various {@link List} implementations
 * extending certain {@link Collection} implementations that, in its turn, extend the same abstract collection class.
 * The mentioned abstract class can implement this interface to stub specific {@link List} methods early (on top of hierarchy).
 * @see Unmodifiable
 * @see List
 */
interface NotList<E> /*extends List<E>*/ {
    /* @Override */ E       set(int index, E element);
    /* @Override */ void    add(int index, E element);
    /* @Override */ boolean addAll(int index, Collection<? extends E> c);
    /* @Override */ E       remove(int index);
    /* @Override */ void    replaceAll(UnaryOperator<E> operator);
    /* @Override */ void    sort(Comparator<? super E> c);
}