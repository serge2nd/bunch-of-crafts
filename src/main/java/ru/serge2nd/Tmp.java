package ru.serge2nd;

import java.math.BigDecimal;
import java.util.Random;

import static java.lang.Math.abs;
import static java.math.BigDecimal.valueOf;

public class Tmp {

    public static void main(String[] args) {
        Random rnd = new Random(79);

        double sum = 0.0;
        float plainSum = 0.0f;
        float[] kahanSum = new float[2];
        float[] neumaierSum = new float[2];
        float[] kleinSum = new float[3];
        float[] itKB3Sum = new float[4];
        for (int i = 0; i < 50_000_000; i++) {
            float x = rnd.nextFloat();
            sum += x;
            plainSum += x;
            kahanSum(kahanSum, x);
            neumaierSum(neumaierSum, x);
            kleinSum(kleinSum, x);
            iterativeKBSum(itKB3Sum, x, 3);
        }

        System.out.println("exact: " + Double.toHexString(sum));
        System.out.println("plain: " + Float.toHexString(plainSum));
        System.out.println("kahan: " + Float.toHexString(kahanSum[0] + kahanSum[1]));
        System.out.println("neuma: " + Float.toHexString(neumaierSum[0] + neumaierSum[1]));
        System.out.println("klein: " + Double.toHexString((double)kleinSum[0] + kleinSum[1] + kleinSum[2]));
        System.out.println("itKB3: " + Double.toHexString(valueOf(itKB3Sum[0]).add(valueOf(itKB3Sum[1]).add(valueOf(itKB3Sum[2])).add(valueOf(itKB3Sum[3]))).doubleValue()));
    }

    public static float[] kahanSum(float[] sum, float x) {
        x -= sum[1];
        float aSum = sum[0] + x;
        float x_ = aSum - sum[0];

        sum[0] = aSum; sum[1] = x_ - x;
        return sum;
    }

    public static float[] neumaierSum(float[] sum, float x) {
        return iterativeKBSum(sum, x, 1);
    }

    public static float[] kleinSum(float[] sum, float x) {
        return iterativeKBSum(sum, x, 2);
    }

    public static float[] iterativeKBSum(float[] sum, float x, int order) {
        for (int i = 0; i < order; i++)
            x = compensation(sum, x, i);
        sum[order] += x;
        return sum;
    }

    public static float compensation(float[] sum, float x, int i) {
        float aSum = sum[i] + x;
        float c = abs(sum[i]) >= abs(x)
            ? (sum[i] - aSum) + x
            : (x - aSum) + sum[i];
        sum[i] = aSum;
        return c;
    }
}
