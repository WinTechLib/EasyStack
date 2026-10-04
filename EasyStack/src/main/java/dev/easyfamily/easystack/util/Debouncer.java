package dev.easyfamily.easystack.util;

/** Só aceita a mudança de um boolean depois de ficar estável por X ms (anti-ruído de sensor/botão). */
public class Debouncer {
    public enum Type { RISING, FALLING, BOTH }

    private final long debounceNanos;
    private final Type type;
    private boolean baseline;
    private long prevNanos;

    public Debouncer(long debounceMillis) {
        this(debounceMillis, Type.RISING);
    }

    public Debouncer(long debounceMillis, Type type) {
        this.debounceNanos = debounceMillis * 1_000_000L;
        this.type = type;
        this.baseline = type == Type.FALLING;
        this.prevNanos = System.nanoTime();
    }

    public boolean calculate(boolean input) {
        if (input == baseline) {
            prevNanos = System.nanoTime();
        }
        if (System.nanoTime() - prevNanos >= debounceNanos) {
            if (type == Type.BOTH) {
                baseline = input;
                prevNanos = System.nanoTime();
            }
            return input;
        }
        return baseline;
    }
}
