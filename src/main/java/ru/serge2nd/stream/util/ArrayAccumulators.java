package ru.serge2nd.stream.util;

import lombok.NonNull;
import ru.serge2nd.function.Functions.*;
import java.util.function.*;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

public class ArrayAccumulators {
    private ArrayAccumulators() { throw errNotInstantiable(lookup()); }

    public static <E, F> BiConsumer<F[], E> mapping(@NonNull Function<E, F> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {int i = idxHolder[0]; if (i < a.length) { a[i] = mapping.apply(e); idxHolder[0] = i + 1; }};
    }
    public static <E> BiConsumer<boolean[], E> mappingToBool(@NonNull ToBoolFunction<E> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {int i = idxHolder[0]; if (i < a.length) { a[i] = mapping.asBoolean(e); idxHolder[0] = i + 1; }};
    }
    public static <E> BiConsumer<char[], E> mappingToChar(@NonNull ToCharFunction<E> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {int i = idxHolder[0]; if (i < a.length) { a[i] = mapping.asChar(e); idxHolder[0] = i + 1; }};
    }
    public static <E> BiConsumer<byte[], E> mappingToByte(@NonNull ToByteFunction<E> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {int i = idxHolder[0]; if (i < a.length) { a[i] = mapping.asByte(e); idxHolder[0] = i + 1; }};
    }
    public static <E> BiConsumer<short[], E> mappingToShort(@NonNull ToShortFunction<E> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {int i = idxHolder[0]; if (i < a.length) { a[i] = mapping.asShort(e); idxHolder[0] = i + 1; }};
    }
    public static <E> BiConsumer<int[], E> mappingToInt(@NonNull ToIntFunction<E> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {int i = idxHolder[0]; if (i < a.length) { a[i] = mapping.applyAsInt(e); idxHolder[0] = i + 1; }};
    }
    public static <E> BiConsumer<long[], E> mappingToLong(@NonNull ToLongFunction<E> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {int i = idxHolder[0]; if (i < a.length) { a[i] = mapping.applyAsLong(e); idxHolder[0] = i + 1; }};
    }
    public static <E> BiConsumer<float[], E> mappingToFloat(@NonNull ToFloatFunction<E> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {int i = idxHolder[0]; if (i < a.length) { a[i] = mapping.asFloat(e); idxHolder[0] = i + 1; }};
    }
    public static <E> BiConsumer<double[], E> mappingToDouble(@NonNull ToDoubleFunction<E> mapping) {
        int[] idxHolder = new int[] {0};
        return (a, e) -> {int i = idxHolder[0]; if (i < a.length) { a[i] = mapping.applyAsDouble(e); idxHolder[0] = i + 1; }};
    }
}
