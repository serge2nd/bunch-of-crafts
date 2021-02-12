package ru.serge2nd.misc;

import lombok.NonNull;

import static java.lang.Integer.SIZE;
import static java.lang.Integer.bitCount;
import static java.lang.Integer.numberOfTrailingZeros;
import static java.util.Arrays.copyOf;

/**
 * Example:
 * <pre>
 *     int $1ST      = 1 << 5;
 *     int $2ND      = 1 << 10;
 *     int $3RD      = 1 << 20;
 *     int TRASH_BIT = 1 << 15;
 *     int ALL = $1ST | $2ND | $3RD;
 *
 *     BitsResolver<String> r = new BitsResolver<>(ALL,
 *             "xxx", "xxv", "xvx", "xvv",
 *             "vxx", "vxv", "vvx");
 *
 *     assert "xxx".equals(r.resolve(0));
 *     assert "xxv".equals(r.resolve($1ST));
 *     assert "xvx".equals(r.resolve($2ND | TRASH_BIT));
 *     assert "xvv".equals(r.resolve($1ST | $2ND));
 *
 *     assert "vxx".equals(r.resolve($3RD | TRASH_BIT));
 *     assert "vxv".equals(r.resolve($1ST | $3RD));
 *     assert "vvx".equals(r.resolve($2ND | $3RD | TRASH_BIT));
 *     assert null == r.resolve($1ST |$2ND | $3RD);
 * </pre>
 */
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
