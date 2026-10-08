package dev.easyfamily.easystack.util;
public class LowPassFilter {
    private final double timeConstant;
    private double value;
    private long lastNanos;
    private boolean initialized = false;
    public LowPassFilter(double timeConstant) {
        this.timeConstant = timeConstant;
    }
    public double calculate(double input) {
        long now = System.nanoTime();
        double dt = initialized ? (now - lastNanos) / 1e9 : 0.0;
        lastNanos = now;
        return calculate(input, dt);
    }
    public double calculate(double input, double dt) {
        if (!initialized) {
            value = input;
            initialized = true;
            lastNanos = System.nanoTime();
            return value;
        }
        double alpha = dt / (timeConstant + dt);
        value += alpha * (input - value);
        return value;
    }

    public double getValue() { return value; }

    public void reset() { initialized = false; }
}
