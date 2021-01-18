package ru.serge2nd.stream;

import ru.serge2nd.stream.MappingCollectors.ToString;
import ru.serge2nd.stream.util.CollectingOptions;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.stream.util.Accumulators.filtering;
import static ru.serge2nd.stream.util.Accumulators.filteringToStr;
import static ru.serge2nd.stream.MappingCollectors.forList;
import static ru.serge2nd.stream.MappingCollectors.forSet;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * A factory of lightweight filtering collectors.
 * @see ru.serge2nd.stream
 * @see CollectingOptions
 */
public class FilteringCollectors {
    private FilteringCollectors() { throw errNotInstantiable(lookup()); }

    //region Factory methods

    public static <E> Collector<E, ?, String> filterToStr(Predicate<E> filter, Supplier<StringJoiner> supplier, int opts) {
        return new ToString<>(supplier, filteringToStr(filter, opts));
    }
    public static <E> Collector<E, ?, Set<E>> filterToSet(Predicate<E> filter, int opts)  {
        return forSet(filtering(filter, opts), opts);
    }
    public static <E> Collector<E, ?, List<E>> filterToList(Predicate<E> filter, int opts) {
        return forList(filtering(filter, opts), opts);
    }
    //endregion
}
