package ru.serge2nd.collection;

import ru.serge2nd.type.Overrides;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * @see NotList
 */
@Overrides(Collection.class)
public interface NotCollection<E> {
    @Overrides boolean add(E e);
    @Overrides boolean addAll(Collection<? extends E> coll);
    @Overrides boolean remove(Object o);
    @Overrides boolean removeIf(Predicate<? super E> filter);
    @Overrides boolean removeAll(Collection<?> coll);
    @Overrides boolean retainAll(Collection<?> coll);
    @Overrides void    clear();
}
