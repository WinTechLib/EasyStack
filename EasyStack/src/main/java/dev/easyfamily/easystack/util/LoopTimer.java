package dev.easyfamily.easystack.util;

/** Mede o tempo de loop. Chame tick() uma vez por iteração e mostre getHz() na telemetry. */
public class LoopTimer {
    private long lastNanos = 0;
    private double dt = 0.0;
    private double avgDt = 0.0;
    private final double smoothing;

    public LoopTimer() {
        this(0.1);
    }

    /** smoothing em (0, 1]: peso da amostra nova na média (menor = mais suave). */
    public LoopTimer(double smoothing) {
        this.smoothing = smoothing;
    }

    public void tick() {
        long now = System.nanoTime();
        if (lastNanos != 0) {
            dt = (now - lastNanos) / 1e9;
            avgDt = avgDt == 0.0 ? dt : avgDt + smoothing * (dt - avgDt);
        }
        lastNanos = now;
    }

    /** Duração da última iteração (s). */
    public double getDt() { return dt; }

    /** Frequência média (Hz). */
    public double getHz() { return avgDt > 0 ? 1.0 / avgDt : 0.0; }

    public double getAverageMillis() { return avgDt * 1000.0; }
}
