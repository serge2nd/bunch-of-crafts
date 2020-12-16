package ru.serge2nd.collection;

import ru.serge2nd.type.Overrides;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Provides the same signatures as the {@link List} mutators (excluding inherited).
 * Created to reduce duplicate code in various {@link List} implementations
 * extending certain {@link Collection} implementations that, in theirs turn, extend the same abstract collection class.
 * The mentioned abstract class can implement this interface to stub specific {@link List} methods early (on top of hierarchy).
 * @see Unmodifiable
 * @see List
 */
@Overrides(List.class)
interface NotList<E> {
    @Overrides E       set(int index, E element);
    @Overrides void    add(int index, E element);
    @Overrides boolean addAll(int index, Collection<? extends E> c);
    @Overrides E       remove(int index);
    @Overrides void    replaceAll(UnaryOperator<E> operator);
    @Overrides void    sort(Comparator<? super E> c);
}