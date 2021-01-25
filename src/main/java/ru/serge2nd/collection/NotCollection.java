package ru.serge2nd.collection;

import ru.serge2nd.type.Over;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * @see NotList
 */
@Over(Collection.class)
public interface NotCollection<E> {
    @Over boolean add(E e);
    @Over boolean addAll(Collection<? extends E> coll);
    @Over boolean remove(Object o);
    @Over boolean removeIf(Predicate<? super E> filter);
    @Over boolean removeAll(Collection<?> coll);
    @Over boolean retainAll(Collection<?> coll);
    @Over void    clear();
}
