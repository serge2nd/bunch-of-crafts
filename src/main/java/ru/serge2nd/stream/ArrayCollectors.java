package ru.serge2nd.stream;

import lombok.NonNull;
import ru.serge2nd.stream.util.Accumulators;
import ru.serge2nd.stream.util.Collecting.IdentityFinish;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.stream.util.Collecting.noCombiner;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * A factory of lightweight collectors with array as result.
 * @see ru.serge2nd.stream
 */
public class ArrayCollectors {
    private ArrayCollectors() { throw errNotInstantiable(lookup().lookupClass()); }

    public static final int[] NO_INTS = new int[0];

    public static <E> Collector<E, ?, int[]> mapToInts(ToIntFunction<E> mapping, int len) {
        return new SuppliedAccumulator<>(()->newIntArray(len), mappingToIntAccumulator(mapping));
    }
    public static <E, F> Collector<E, ?, F[]> mapToArray(Function<E, F> mapping, Supplier<F[]> supplier) {
        return new SuppliedAccumulator<>(supplier, mappingAccumulator(mapping));
    }

    public static <E, F> BiConsumer<F[], E> mappingAccumulator(@NonNull Function<E, F> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {
            int i = idxHolder[0];
            if (i < a.length) {
                a[i] = mapping.apply(e);
                idxHolder[0] = i + 1;
            }
        };
    }
    public static <E> BiConsumer<int[], E> mappingToIntAccumulator(@NonNull ToIntFunction<E> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {
            int i = idxHolder[0];
            if (i < a.length) {
                a[i] = mapping.applyAsInt(e);
                idxHolder[0] = i + 1;
            }
        };
    }

    public static int[] newIntArray(int len) {
        return len > 0 ? new int[len] : NO_INTS;
    }

    static final class SuppliedAccumulator<E, R> extends Accumulators.SuppliedAccumulator<E, R, R> implements IdentityFinish<E, R> {
        @Override public BinaryOperator<R> combiner()                           { return noCombiner(); }
        SuppliedAccumulator(Supplier<R> supplier, BiConsumer<R, E> accumulator) { super(supplier, accumulator); }
    }
}
