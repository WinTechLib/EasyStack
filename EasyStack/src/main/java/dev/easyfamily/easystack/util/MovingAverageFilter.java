package dev.easyfamily.easystack.util;
public class MovingAverageFilter {
    private final double[] buffer;
    private int index = 0;
    private int count = 0;
    private double sum = 0.0;

    public MovingAverageFilter(int windowSize) {
        if (windowSize < 1) throw new IllegalArgumentException("windowSize deve ser >= 1");
        buffer = new double[windowSize];
    }

    public double calculate(double input) {
        sum -= buffer[index];
        buffer[index] = input;
        sum += input;
        index = (index + 1) % buffer.length;
        if (count < buffer.length) count++;
        return sum / count;
    }

    public void reset() {
        java.util.Arrays.fill(buffer, 0.0);
        index = 0;
        count = 0;
        sum = 0.0;
    }
}
