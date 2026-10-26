package dev.easyfamily.EasyStack.util;
public class SlewRateLimiter {
    private final double positiveRate;
    private final double negativeRate;
    private double previous;
    private long lastNanos;
    private boolean timerStarted = false;

    public SlewRateLimiter(double rate) {
        this(rate, rate, 0.0);
    }
    public SlewRateLimiter(double positiveRate, double negativeRate, double initialValue) {
        this.positiveRate = Math.abs(positiveRate);
        this.negativeRate = -Math.abs(negativeRate);
        this.previous = initialValue;
    }

    public double calculate(double input) {
        long now = System.nanoTime();
        double dt = timerStarted ? (now - lastNanos) / 1e9 : 0.0;
        lastNanos = now;
        timerStarted = true;
        return calculate(input, dt);
    }

    public double calculate(double input, double dt) {
        dt = Math.max(dt, 0.0);
        double delta = input - previous;
        double maxUp = positiveRate * dt;
        double maxDown = negativeRate * dt;
        previous += Math.max(maxDown, Math.min(maxUp, delta));
        return previous;
    }

    public double getValue() { return previous; }

    public void reset(double value) {
        previous = value;
        timerStarted = false;
    }
}