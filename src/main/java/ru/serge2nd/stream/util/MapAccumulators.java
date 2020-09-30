package ru.serge2nd.stream.util;

import lombok.NonNull;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.stream.util.CollectingOptions.NON_NULL;
import static ru.serge2nd.stream.util.CollectingOptions.NON_NULL_VAL;
import static ru.serge2nd.stream.util.CollectingOptions.has;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * Accumulator functions for {@link Map} instances.
 * @see Collector#accumulator()
 */
public class MapAccumulators {
    private MapAccumulators() { throw errNotInstantiable(lookup().lookupClass()); }

    public static <E, K, M extends Map<K, E>> BiConsumer<M, E> keyAccumulator(@NonNull Function<E, K> mapping, int opts) {
        return has(NON_NULL, opts)
                ? (m, e) -> {K k = mapping.apply(e); if (k != null) putUnique(m, k, e);}
                : (m, e) -> putUnique(m, mapping.apply(e), e);
    }
    public static <E, V, M extends Map<E, V>> BiConsumer<M, E> valueAccumulator(@NonNull Function<E, V> mapping, int opts) {
        return has(NON_NULL_VAL, opts)
                ? (m, e) -> {V v = mapping.apply(e); if (v != null) putUnique(m, e, v);}
                : (m, e) -> putUnique(m, e, mapping.apply(e));
    }

    public static <E, K, V, M extends Map<K, V>> BiConsumer<M, E> valueFromKeyAccumulator(@NonNull Function<E, K> keyMapping, @NonNull Function<K, V> valMapping, int opts) {
        return has(NON_NULL_VAL, opts)
                ? (m, e) -> {K k = keyMapping.apply(e); V v; if (k != null && (v = valMapping.apply(k)) != null) putUnique(m, k, v);}
                : (m, e) -> {K k = keyMapping.apply(e); if (k != null) putUnique(m, k, valMapping.apply(k));};
    }
    public static <E, K, V, M extends Map<K, V>> BiConsumer<M, E> keyFromValueAccumulator(@NonNull Function<V, K> keyMapping, @NonNull Function<E, V> valMapping, int opts) {
        return has(NON_NULL, opts)
                ? (m, e) -> {V v = valMapping.apply(e); K k; if (v != null && (k = keyMapping.apply(v)) != null) putUnique(m, k, v);}
                : (m, e) -> {V v = valMapping.apply(e); if (v != null) putUnique(m, keyMapping.apply(v), v);};
    }

    public static <E, K, V, M extends Map<K, V>> BiConsumer<M, E> keyValueAccumulator(@NonNull Function<E, K> keyMapping, @NonNull Function<E, V> valMapping, int opts) {
        return has(NON_NULL | NON_NULL_VAL, opts)
                ? (m, e) -> {K k = keyMapping.apply(e); V v; if (k != null && (v = valMapping.apply(e)) != null) putUnique(m, k, v); }
                : has(NON_NULL, opts)     ? (m, e) -> {K k = keyMapping.apply(e); if (k != null) putUnique(m, k, valMapping.apply(e)); }
                : has(NON_NULL_VAL, opts) ? (m, e) -> {V v = valMapping.apply(e); if (v != null) putUnique(m, keyMapping.apply(e), v); }
                : (m, e) -> putUnique(m, keyMapping.apply(e), valMapping.apply(e));
    }

    public static <K, V> void putUnique(Map<K, V> m, K k, V v) {
        V old = m.putIfAbsent(k, v);
        if (old != null) throw duplicateKeyException(k, old, v);
    }

    public static IllegalStateException duplicateKeyException(Object k, Object old, Object v) {
        return new IllegalStateException(format(
                "duplicate key %s (attempted merging values %s and %s)", k, old, v));
    }
}
