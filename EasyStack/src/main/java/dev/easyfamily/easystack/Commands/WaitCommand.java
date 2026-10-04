package dev.easyfamily.easystack.Commands;

public class WaitCommand extends EasyCommandBase {
    private final long millis;
    private long startNanos;

    public WaitCommand(long millis) {
        this.millis = millis;
    }

    @Override
    public void initialize() {
        startNanos = System.nanoTime();
    }

    @Override
    public boolean isFinished() {
        return (System.nanoTime() - startNanos) / 1_000_000L >= millis;
    }
}
