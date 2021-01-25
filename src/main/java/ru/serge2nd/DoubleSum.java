package ru.serge2nd;

import java.math.BigDecimal;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static java.math.BigDecimal.valueOf;

/**
 * Accumulating a sum of <code>double</code> values with compensation.
 * See {@link DoubleAlgs#kahanSum(double[], double)} and {@link DoubleAlgs#iterativeKBSum(double[], double, int)}
 * for examples of compensating summation.
 */
public class DoubleSum {
    @FunctionalInterface
    public interface CompensatingSumOp { double[] add(double[] sum, double x); }

    private final CompensatingSumOp op;
    private final double[] sum;

    public static DoubleSum of(double... xs) {
        return of(DoubleAlgs::kahanSum, 1, xs);
    }
    public static DoubleSum of(CompensatingSumOp op, int compensationOrder, double... xs) {
        DoubleSum s = new DoubleSum(0.0, op, compensationOrder);
        for (double x : xs) s.add(x); return s;
    }

    public DoubleSum(double x0) {
        this(x0, DoubleAlgs::kahanSum, 1);
    }
    public DoubleSum(double x0, CompensatingSumOp op, int compensationOrder) {
        if (op == null || compensationOrder < 0) throw new IllegalArgumentException("null op or negative compensation order");
        this.op = op;
        this.sum = new double[compensationOrder + 1];
        sum[0] = x0;
    }

    public DoubleSum add(double x) {
        op.add(sum, x); return this;
    }
    /** Get the raw result (perhaps with very basic compensation). */
    public double raw() {
        return sum[0];
    }
    /** Get the fast but not too precise result. */
    public double get() {
        double s = sum[0];
        for(int i = 1; i < sum.length; i++) s += sum[i];
        return fin(s);
    }
    /** Get the more precise result slower with as many {@link BigDecimal} allocations as the value of the compensation order. */
    public double fine() {
        if (sum.length == 1) return sum[0];
        BigDecimal s = valueOf(sum[0]);
        for(int i = 1; i < sum.length; i++) s = s.add(valueOf(sum[i]));
        return fin(s.doubleValue());
    }

    private double fin(double s) { return isNaN(s) && isInfinite(sum[0]) ? sum[0] : s; }
}
