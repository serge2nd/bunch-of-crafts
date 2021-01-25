package ru.serge2nd;

import static java.lang.Math.abs;

public class DoubleAlgs {

    /**
     * Just increments {@code sum[0]} by {@code x}.
     * @param sum at least 1-element array (only 1st element is to change)
     * @param x value to add
     * @return the first arg
     */
    public static double[] plainSum(double[] sum, double x) { sum[0] += x; return sum; }

    /**
     * Performs one iteration of the <i>Kahan summation</i> to add {@code x} to the compensating sum.
     * @param sum at least 2-element array (only 1st and 2nd element are to change)
     * @param x value to add
     * @return the first arg
     * @see <a href="https://en.wikipedia.org/wiki/Kahan_summation_algorithm">Kahan summation algorithm</a>
     */
    public static double[] kahanSum(double[] sum, double x) {
        x -= sum[1];               // x with compensation
        double aSum = sum[0] + x;  // new sum
        double x_ = aSum - sum[0]; // high part of x

        sum[0] = aSum; sum[1] = x_ - x; // low part of x (negated)
        return sum;
    }

    /**
     * Same as {@link #iterativeKBSum(double[], double, int) iterativeKBSum(sum, x, 1)}
     */
    public static double[] neumaierSum(double[] sum, double x) { return iterativeKBSum(sum, x, 1); }

    /**
     * Same as {@link #iterativeKBSum(double[], double, int) iterativeKBSum(sum, x, 2)}
     */
    public static double[] kleinSum(double[] sum, double x) { return iterativeKBSum(sum, x, 2); }

    /**
     * TODO
     * @param sum array holding at least {@code order + 1} values (only these first values will be changed)
     * @param x value to add
     * @param order compensation order (number of compensating values)
     * @return the first arg
     * @see <a href="https://doi.org/10.1007%2Fs00607-005-0139-x">
     *     Klein (2006). "A generalized Kahan–Babuška-Summation-Algorithm". Computing. Springer-Verlag. 76 (3–4): 279–293</a>
     */
    public static double[] iterativeKBSum(double[] sum, double x, int order) {
        for (int i = 0; i < order; i++)
            x = compensationStep(sum, x, i);
        sum[order] += x; return sum;
    }

    public static double compensationStep(double[] sum, double x, int i) {
        double aSum = sum[i] + x;
        double c = abs(sum[i]) >= abs(x)
            ? (sum[i] - aSum) + x  // recover x part
            : (x - aSum) + sum[i]; // recover sum part
        sum[i] = aSum; return c;
    }
}
