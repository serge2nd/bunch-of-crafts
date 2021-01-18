package ru.serge2nd.stream.util;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Arrays;

import static java.util.Arrays.stream;
import static java.util.stream.IntStream.empty;
import static java.util.stream.IntStream.of;
import static ru.serge2nd.stream.util.CollectingOptions.Internal.ALL;

/**
 * Collecting options represented by bits in an integer mask.
 */
public interface CollectingOptions {

    /** Indicates the result must be unmodifiable. */
    int UNMODIFIABLE = 1 << 4;

    /** Enables eliminating {@code null} elements. */
    int NON_NULL     = 1 << 7;

    /** Enables eliminating {@code null} values in the {@link java.util.Map}. */
    int NON_NULL_VAL = 1 << 10;

    static int[] allOptions() { return Arrays.copyOf(ALL, ALL.length); }

    class Internal {
        static int[] ALL = stream(CollectingOptions.class.getDeclaredFields())
            .flatMapToInt(f -> int.class == f.getType()
                ? of(getInt(f))
                : empty())
            .toArray();
        @SneakyThrows
        static int getInt(Field f) { return f.getInt(null); }
    }
}
