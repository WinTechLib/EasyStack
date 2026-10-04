package dev.easyfamily.easystack.Commands;

public class TimeoutCommand extends WrapperCommand {
    private final long millis;
    private long startNanos;

    public TimeoutCommand(EasyCommand inner, long millis) {
        super(inner);
        this.millis = millis;
    }

    @Override
    public void initialize() {
        startNanos = System.nanoTime();
        inner.initialize();
    }

    @Override
    public boolean isFinished() {
        return inner.isFinished() || (System.nanoTime() - startNanos) / 1_000_000L >= millis;
    }

    @Override
    public void end(boolean interrupted) {
        inner.end(interrupted || !inner.isFinished());
    }
}
