package ru.serge2nd.stream.util;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Objects;

import static java.lang.Integer.SIZE;
import static java.lang.Integer.bitCount;
import static java.lang.Integer.numberOfTrailingZeros;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.stream;
import static ru.serge2nd.stream.util.CollectingOptions.Holder.ALL;

/**
 * Collecting options represented by bits in an integer mask.
 */
public interface CollectingOptions {
    class Holder { static int[] ALL = stream(CollectingOptions.class.getDeclaredFields())
            .map(f -> {
                try {if (int.class == f.getType())
                    return f.getInt(null);
                } catch(IllegalAccessException e) {/* NO-OP */}
                return null; })
            .filter(Objects::nonNull)
            .mapToInt(i -> i)
            .toArray(); }

    /** Indicates the result must be unmodifiable. */
    int UNMODIFIABLE = 1;

    /** Enables dropping out {@code null} elements. */
    int NON_NULL     = 1 << 1;

    /** Enables dropping out {@code null} values in the {@link java.util.Map}. */
    int NON_NULL_VAL = 1 << 2;

    static int[] collectingOptions() { return Arrays.copyOf(ALL, ALL.length); }

    static boolean has(int opts, int mask) { return (opts & mask) == opts; }

    final class OptionsResolver<T> {
        static final int H = SIZE - 1;
        final int availableOpts;
        final T[] vals;

        @SafeVarargs
        public OptionsResolver(int availableOpts, @NonNull T... vals) {
            int nVars = 1 << bitCount(availableOpts);
            this.vals = vals.length > nVars ? copyOf(vals, nVars) : vals;
            this.availableOpts = availableOpts;
        }

        public T resolve(int opts) {
            final int available = availableOpts, max = vals.length - 1;
            opts &= available;

            int idx, i;
            for (idx = 0; opts != 0; opts = opts >>> i << i) {
                i = numberOfTrailingZeros(opts);
                int skipped = bitCount(available << (H - i++)) - 1;
                if (max < (idx |= (1 << skipped))) return null;
            }

            return vals[idx];
        }
    }
}
