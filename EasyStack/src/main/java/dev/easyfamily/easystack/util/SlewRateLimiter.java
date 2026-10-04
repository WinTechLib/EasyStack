package dev.easyfamily.easystack.util;

/** Limita a taxa de variação (unidades/s) de um valor. Ex.: rampa de potência do drive. */
public class SlewRateLimiter {
    private final double positiveRate;
    private final double negativeRate;
    private double previous;
    private long lastNanos;

    /** rate em unidades por segundo (usado para subir e descer). */
    public SlewRateLimiter(double rate) {
        this(rate, -rate, 0.0);
    }

    /** negativeRate deve ser negativo (ex.: -4.0). */
    public SlewRateLimiter(double positiveRate, double negativeRate, double initialValue) {
        this.positiveRate = positiveRate;
        this.negativeRate = negativeRate;
        this.previous = initialValue;
        this.lastNanos = System.nanoTime();
    }

    public double calculate(double input) {
        long now = System.nanoTime();
        double dt = (now - lastNanos) / 1e9;
        lastNanos = now;
        return calculate(input, dt);
    }

    public double calculate(double input, double dt) {
        double delta = input - previous;
        double maxUp = positiveRate * dt;
        double maxDown = negativeRate * dt;
        previous += Math.max(maxDown, Math.min(maxUp, delta));
        return previous;
    }

    public double getValue() { return previous; }

    public void reset(double value) {
        previous = value;
        lastNanos = System.nanoTime();
    }
}
