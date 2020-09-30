package ru.serge2nd.stream;

import lombok.NonNull;
import ru.serge2nd.stream.util.CollectingOptions;
import ru.serge2nd.collection.Unmodifiable;
import ru.serge2nd.stream.util.Accumulators.Accumulator;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.stream.util.Accumulators.*;
import static ru.serge2nd.stream.util.Collecting.*;
import static ru.serge2nd.stream.util.CollectingOptions.*;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * A factory of lightweight mapping collectors.
 * @see ru.serge2nd.stream
 * @see CollectingOptions
 */
public class MappingCollectors {
    private MappingCollectors() { throw errNotInstantiable(lookup().lookupClass()); }

    //region Factory methods

    public static <E> Collector<E, ?, String> mapToStr(Function<E, ?> mapping, Supplier<StringJoiner> supplier, int opts) {
        return new ToString<>(supplier, mappingToStrAccumulator(mapping, opts));
    }
    public static <E, F> Collector<E, ?, Set<F>>  mapToSet(Function<E, F> mapping, int opts) {
        return forSet(mappingAccumulator(mapping, opts), opts);
    }
    public static <E, F> Collector<E, ?, List<F>> mapToList(Function<E, F> mapping, int opts) {
        return forList(mappingAccumulator(mapping, opts), opts);
    }

    public static <E> Collector<E, ?, String> aFlatMapToStr(Function<E, ? extends Object[]> mapping, Supplier<StringJoiner> supplier, int opts) {
        return new ToString<>(supplier, aFlatMappingToStrAccumulator(mapping, opts));
    }
    public static <E, F> Collector<E, ?, Set<F>> aFlatMapToSet(Function<E, F[]> mapping, int opts) {
        return forSet(aFlatMappingAccumulator(mapping, opts), opts);
    }
    public static <E, F> Collector<E, ?, List<F>> aFlatMapToList(Function<E, F[]> mapping, int opts) {
        return forList(aFlatMappingAccumulator(mapping, opts), opts);
    }
    //endregion

    //region Hidden implementations

    @SuppressWarnings("unchecked")
    static final class ToUnmodifiableSet<E, F, S extends Set<F>> extends Accumulator<E, S, S> implements ToSet<E, S, S>, Unordered<E, S, S> {
        @Override public Function<S, S> finisher() { return s -> (S)Unmodifiable.ofSet(s); }
        ToUnmodifiableSet(BiConsumer<S, E> a)      { super(a); }
    }
    @SuppressWarnings("unchecked")
    static final class ToUnmodifiableList<E, F, L extends List<F>> extends Accumulator<E, L, L> implements ToCollection<E, L, L>, NoFeatures<E, L, L> {
        @Override public Function<L, L> finisher() { return l -> (L)Unmodifiable.ofList(l); }
        ToUnmodifiableList(BiConsumer<L, E> a)     { super(a); }
    }
    static final class ToSimpleSet<E, F, S extends Set<F>> extends Accumulator<E, S, S> implements ToSet<E, S, S>, IdentityFinishUnordered<E, S> {
        ToSimpleSet(BiConsumer<S, E> a) { super(a); }
    }
    static final class ToSimpleList<E, F, L extends List<F>> extends Accumulator<E, L, L> implements ToCollection<E, L, L>, IdentityFinish<E, L> {
        ToSimpleList(BiConsumer<L, E> a) { super(a); }
    }
    static final class ToString<E> extends CommonCollectors.ToString<E> {
        @Override public BiConsumer<StringJoiner, E> accumulator() { return accumulator; }
        final BiConsumer<StringJoiner, E> accumulator;
        ToString(Supplier<StringJoiner> supplier, @NonNull BiConsumer<StringJoiner, E> accumulator) {
            super(supplier); this.accumulator = accumulator;
        }
    }

    static <E, F> Collector<E, ?, Set<F>> forSet(BiConsumer<Set<F>, E> a, int opts) {
        return has(UNMODIFIABLE, opts) ? new ToUnmodifiableSet<>(a) : new ToSimpleSet<>(a);
    }
    static <E, F> Collector<E, ?, List<F>> forList(BiConsumer<List<F>, E> a, int opts) {
        return has(UNMODIFIABLE, opts) ? new ToUnmodifiableList<>(a) : new ToSimpleList<>(a);
    }
    //endregion
}
