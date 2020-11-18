package ru.serge2nd.stream;

import ru.serge2nd.stream.util.Accumulators.Accumulator;
import ru.serge2nd.stream.util.Collecting.NoFeatures;
import ru.serge2nd.stream.util.Collecting.IdentityFinish;
import ru.serge2nd.stream.util.CollectingOptions;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.unmodifiableMap;
import static ru.serge2nd.stream.util.CollectingOptions.*;
import static ru.serge2nd.stream.util.MapAccumulators.*;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * A factory of lightweight collectors with map as result.
 * @see ru.serge2nd.stream
 * @see CollectingOptions
 */
public class MapCollectors {
    private MapCollectors() { throw errNotInstantiable(lookup()); }

    //region Factory methods

    public static <E, K> Collector<E, ?, Map<K, E>> toMapKey(Function<E, K> keyMapper, int opts) {
        return forMap(keyAccumulator(keyMapper, opts), opts);
    }
    public static <E, V> Collector<E, ?, Map<E, V>> toMap(Function<E, V> valMapper, int opts) {
        return forMap(valueAccumulator(valMapper, opts), opts);
    }
    public static <E, K, V> Collector<E, ?, Map<K, V>> toMap(Function<E, K> keyMapper, Function<E, V> valMapper, int opts) {
        return forMap(keyValueAccumulator(keyMapper, valMapper, opts), opts);
    }
    public static <E, K, V> Collector<E, ?, Map<K, V>> toValueFromKeyMap(Function<E, K> keyMapper, Function<K, V> valMapper, int opts) {
        return forMap(valueFromKeyAccumulator(keyMapper, valMapper, opts), opts);
    }
    public static <E, K, V> Collector<E, ?, Map<K, V>> toKeyFromValueMap(Function<V, K> keyMapper, Function<E, V> valMapper, int opts) {
        return forMap(keyFromValueAccumulator(keyMapper, valMapper, opts), opts);
    }
    //endregion

    //region Public helpers

    public static <K, V, M extends Map<K, V>> M mergeUnique(M m1, M m2) {
        m2.forEach((k, v) -> putUnique(m1, k, v));
        return m1;
    }

    @SuppressWarnings("unchecked")
    public interface ToMap<E, K, V, M extends Map<K, V>, R> extends Collector<E, M, R> {
        @Override default Supplier<M>       supplier() { return () -> (M)new HashMap<>(); }
        @Override default BinaryOperator<M> combiner() { return MapCollectors::mergeUnique; }
    }
    //endregion

    //region Hidden implementations

    @SuppressWarnings("unchecked")
    static final class ToUnmodifiableMap<E, K, V, M extends Map<K, V>> extends Accumulator<E, M, M> implements ToMap<E, K, V, M, M>, NoFeatures<E, M, M> {
        @Override public Function<M, M> finisher()      { return m -> (M)unmodifiableMap(m); }
        ToUnmodifiableMap(BiConsumer<M, E> accumulator) { super(accumulator); }
    }
    static final class ToSimpleMap<E, K, V, M extends Map<K, V>> extends Accumulator<E, M, M> implements ToMap<E, K, V, M, M>, IdentityFinish<E, M> {
        ToSimpleMap(BiConsumer<M, E> accumulator) { super(accumulator); }
    }

    static <E, K, V> Collector<E, ?, Map<K, V>> forMap(BiConsumer<Map<K, V>, E> a, int opts) {
        return has(UNMODIFIABLE, opts) ? new ToUnmodifiableMap<>(a) : new ToSimpleMap<>(a);
    }
    //endregion
}


