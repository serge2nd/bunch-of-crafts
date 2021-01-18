package ru.serge2nd.stream.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.stream.util.CollectingOptions.NON_NULL;
import static ru.serge2nd.misc.BitsResolver.has;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * Accumulator functions for {@link Collection} and {@link StringJoiner} instances.
 * @see Collector#accumulator()
 */
public class Accumulators {
    private Accumulators() { throw errNotInstantiable(lookup()); }

    @SuppressWarnings("unchecked,rawtypes")
    public static <E, C extends Collection> BiConsumer<C, E> adding()        { return Collection::add; }
    @SuppressWarnings("unchecked,rawtypes")
    public static <E, C extends Collection> BiConsumer<C, E> addingNonNull() { return (c, e) -> {if (e != null) c.add(e);}; }

    //region Filtering

    public static <C extends Collection<E>, E> BiConsumer<C, E> filtering(@NonNull Predicate<E> filter, int opts) {
        return has(NON_NULL, opts)
            ? (c, e) -> {if (e != null && filter.test(e)) c.add(e);}
            : (c, e) -> {if (filter.test(e)) c.add(e);};
    }
    public static <E> BiConsumer<StringJoiner, E> filteringToStr(@NonNull Predicate<E> filter, int opts) {
        return has(NON_NULL, opts)
            ? (sj, e) -> {if (e != null && filter.test(e)) sj.add(String.valueOf(e));}
            : (sj, e) -> {if (filter.test(e)) sj.add(String.valueOf(e));};
    }
    //endregion

    //region Mapping

    public static <E, F, C extends Collection<F>> BiConsumer<C, E> mapping(@NonNull Function<E, F> mapping, int opts) {
        return has(NON_NULL, opts)
            ? (c, e) -> {F f = mapping.apply(e); if (f != null) c.add(f);}
            : (c, e) -> c.add(mapping.apply(e));
    }
    public static <E> BiConsumer<StringJoiner, E> mappingToStr(@NonNull Function<E, ?> mapping, int opts) {
        return has(NON_NULL, opts)
            ? (sj, e) -> {Object f = mapping.apply(e); if (f != null) sj.add(String.valueOf(f));}
            : (sj, e) -> sj.add(String.valueOf(mapping.apply(e)));
    }

    @SuppressWarnings("UseBulkOperation,ManualArrayToCollectionCopy")
    public static <E, F, C extends Collection<F>> BiConsumer<C, E> aFlatMapping(@NonNull Function<E, F[]> mapping, int opts) {
        return has(NON_NULL, opts)
            ? (c, e) -> {F[] a = mapping.apply(e); if (a != null) for (F f : a) if (f != null) c.add(f);}
            : (c, e) -> {F[] a = mapping.apply(e); if (a != null) for (F f : a) c.add(f);};
    }
    public static <E> BiConsumer<StringJoiner, E> aFlatMappingToStr(@NonNull Function<E, ? extends Object[]> mapping, int opts) {
        return has(NON_NULL, opts)
            ? (sj, e) -> {Object[] a = mapping.apply(e); if (a != null) for (Object f : a) if (f != null) sj.add(String.valueOf(f));}
            : (sj, e) -> {Object[] a = mapping.apply(e); if (a != null) for (Object f : a) sj.add(String.valueOf(f));};
    }
    //endregion

    public static abstract class SuppliedAccumulator<E, A, R> extends Accumulator<E, A, R> {
        @Override public final Supplier<A> supplier() { return supplier; }
        public SuppliedAccumulator(@NonNull Supplier<A> supplier, BiConsumer<A, E> accumulator) { super(accumulator); this.supplier = supplier; }
        final Supplier<A> supplier;
    }
    @RequiredArgsConstructor
    public static abstract class Accumulator<E, A, R> implements Collector<E, A, R> {
        @Override public final BiConsumer<A, E> accumulator() { return accumulator; }
        final @NonNull BiConsumer<A, E> accumulator;
    }
}
