package synercys.util;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by cy on 3/14/2017.
 */
public class Umath {
    public static int getRandom(int min, int max) {
        Random rand = new Random();
        return rand.nextInt(max - min + 1) + min;
    }

    public static String nanoIntToMilliString(long inNum) {
        return String.valueOf((double)inNum/1000_000.0);
    }

    public static ArrayList<Long> integerFactorization(long inNum) {
        ArrayList<Long> factors = new ArrayList<>();
        long thisNum = inNum;

        long currentFactor = 2;
        long remainder;
        while (thisNum != 1) {
            remainder = thisNum % currentFactor;
            if (remainder == 0) {
                factors.add(currentFactor);
                thisNum = thisNum/currentFactor;

                // Use the same currentFactor to check again.
                continue;
            } else {
                // currentFactor is not a factor of inNum, thus continue next value.
                currentFactor++;
            }
        }
        return factors;
    }

    public static double getGeometricMean(ArrayList<Double> values) {
        double multiple = 1;
        for (double val : values) {
            multiple *= val;
        }
        return Math.pow(multiple, (double)(1/values.size()));
    }

    public static long gcd(long a, long b) {
        if (b==0) return a;
        return gcd(b,a%b);
    }

    public static long lcm(long a, long b) {
        return a * b / gcd(a, b);
    }

    public static int getPoisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= Math.random();
        } while (p > L);

        return k - 1;
    }
}
