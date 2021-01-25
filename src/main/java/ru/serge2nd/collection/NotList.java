package ru.serge2nd.collection;

import ru.serge2nd.type.Over;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Provides the same signatures as the {@link List} mutators (excluding inherited).
 * Intended to reduce duplicate code in various {@link List} implementations
 * extending certain {@link Collection} implementations that, in theirs turn, extend the same abstract collection class.
 * The mentioned abstract class can implement this interface to stub specific {@link List} methods early (on top of hierarchy).
 * @see Unmodifiable
 * @see List
 */
@Over(List.class)
public interface NotList<E> {
    @Over E       set(int index, E element);
    @Over void    add(int index, E element);
    @Over boolean addAll(int index, Collection<? extends E> c);
    @Over E       remove(int index);
    @Over void    replaceAll(UnaryOperator<E> operator);
    @Over void    sort(Comparator<? super E> c);
}