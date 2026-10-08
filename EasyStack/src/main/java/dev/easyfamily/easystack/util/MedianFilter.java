package dev.easyfamily.easystack.util;

import java.util.Arrays;
public class MedianFilter {
    private final double[] ring;
    private int index = 0;
    private int count = 0;

    public MedianFilter(int size) {
        if (size < 1) throw new IllegalArgumentException("size deve ser >= 1");
        ring = new double[size];
    }

    public double calculate(double input) {
        ring[index] = input;
        index = (index + 1) % ring.length;
        if (count < ring.length) count++;

        double[] tmp = Arrays.copyOf(ring, count);
        Arrays.sort(tmp);
        return count % 2 == 1 ? tmp[count / 2] : (tmp[count / 2 - 1] + tmp[count / 2]) / 2.0;
    }

    public void reset() {
        index = 0;
        count = 0;
    }
}
