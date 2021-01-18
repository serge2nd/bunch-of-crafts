package ru.serge2nd.misc;

import lombok.NonNull;

import static java.lang.Integer.SIZE;
import static java.lang.Integer.bitCount;
import static java.lang.Integer.numberOfTrailingZeros;
import static java.util.Arrays.copyOf;

public final class BitsResolver<T> {
    private static final int H = SIZE - 1;
    private final int all;
    private final T[] vals;

    @SafeVarargs
    public BitsResolver(int all, @NonNull T... vals) {
        int nVars = 1 << bitCount(all);
        this.vals = vals.length > nVars ? copyOf(vals, nVars) : vals;
        this.all = all;
    }

    public T resolve(int bits) {
        final int all = this.all, maxIdx = vals.length - 1;
        bits &= all;

        int idx, i;
        for (idx = 0; bits != 0; bits = bits >>> i << i) {
            i = numberOfTrailingZeros(bits);
            int skipped = bitCount(all << (H - i++)) - 1;
            if (maxIdx < (idx |= (1 << skipped))) return null;
        }

        return vals[idx];
    }

    public static boolean has(int bits, int mask) { return (bits & mask) == bits; }
}
