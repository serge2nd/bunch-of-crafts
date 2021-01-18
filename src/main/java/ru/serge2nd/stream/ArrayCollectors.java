package ru.serge2nd.stream;

import ru.serge2nd.function.Functions.*;
import ru.serge2nd.stream.util.Accumulators;
import ru.serge2nd.stream.util.Collecting.IdentityFinish;

import java.util.function.*;
import java.util.stream.Collector;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.stream.util.ArrayAccumulators.*;
import static ru.serge2nd.stream.util.Collecting.noCombiner;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * A factory of lightweight collectors with an array as result.
 * @see ru.serge2nd.stream
 */
public class ArrayCollectors {
    private ArrayCollectors() { throw errNotInstantiable(lookup()); }

    public static <E, F> Collector<E, ?, F[]> mapToArray(Function<E, F> mapping, Supplier<F[]> supplier) {
        return new SuppliedAccumulator<>(supplier, mapping(mapping));
    }
    public static <E> Collector<E, ?, boolean[]> mapToBools(ToBoolFunction<E> mapping, int len) {
        return new SuppliedAccumulator<>(()->newBooleans(len), mappingToBool(mapping));
    }
    public static <E> Collector<E, ?, char[]> mapToChars(ToCharFunction<E> mapping, int len) {
        return new SuppliedAccumulator<>(()->newChars(len), mappingToChar(mapping));
    }
    public static <E> Collector<E, ?, byte[]> mapToBytes(ToByteFunction<E> mapping, int len) {
        return new SuppliedAccumulator<>(()->newBytes(len), mappingToByte(mapping));
    }
    public static <E> Collector<E, ?, short[]> mapToShorts(ToShortFunction<E> mapping, int len) {
        return new SuppliedAccumulator<>(()->newShorts(len), mappingToShort(mapping));
    }
    public static <E> Collector<E, ?, int[]> mapToInts(ToIntFunction<E> mapping, int len) {
        return new SuppliedAccumulator<>(()->newInts(len), mappingToInt(mapping));
    }
    public static <E> Collector<E, ?, long[]> mapToLongs(ToLongFunction<E> mapping, int len) {
        return new SuppliedAccumulator<>(()->newLongs(len), mappingToLong(mapping));
    }
    public static <E> Collector<E, ?, float[]> mapToFloats(ToFloatFunction<E> mapping, int len) {
        return new SuppliedAccumulator<>(()->newFloats(len), mappingToFloat(mapping));
    }
    public static <E> Collector<E, ?, double[]> mapToDoubles(ToDoubleFunction<E> mapping, int len) {
        return new SuppliedAccumulator<>(()->newDoubles(len), mappingToDouble(mapping));
    }

    public static boolean[] newBooleans(int len) { return len > 0 ? new boolean[len] : NO_BOOLS; }
    public static char[]    newChars(int len)    { return len > 0 ? new char[len] : NO_CHARS; }
    public static byte[]    newBytes(int len)    { return len > 0 ? new byte[len] : NO_BYTES; }
    public static short[]   newShorts(int len)   { return len > 0 ? new short[len] : NO_SHORTS; }
    public static int[]     newInts(int len)     { return len > 0 ? new int[len] : NO_INTS; }
    public static long[]    newLongs(int len)    { return len > 0 ? new long[len] : NO_LONGS; }
    public static float[]   newFloats(int len)   { return len > 0 ? new float[len] : NO_FLOATS; }
    public static double[]  newDoubles(int len)  { return len > 0 ? new double[len] : NO_DOUBLES; }

    public static final boolean[] NO_BOOLS   = new boolean[0];
    public static final char[]    NO_CHARS   = new char[0];
    public static final byte[]    NO_BYTES   = new byte[0];
    public static final short[]   NO_SHORTS  = new short[0];
    public static final int[]     NO_INTS    = new int[0];
    public static final long[]    NO_LONGS   = new long[0];
    public static final float[]   NO_FLOATS  = new float[0];
    public static final double[]  NO_DOUBLES = new double[0];

    static final class SuppliedAccumulator<E, R> extends Accumulators.SuppliedAccumulator<E, R, R> implements IdentityFinish<E, R> {
        @Override public BinaryOperator<R> combiner()                           { return noCombiner(); }
        SuppliedAccumulator(Supplier<R> supplier, BiConsumer<R, E> accumulator) { super(supplier, accumulator); }
    }
}
