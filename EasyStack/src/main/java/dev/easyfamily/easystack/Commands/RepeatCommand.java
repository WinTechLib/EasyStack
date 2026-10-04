package dev.easyfamily.easystack.Commands;

/** Repete o comando N vezes (ou para sempre se usar o construtor sem times). */
public class RepeatCommand extends EasyCommandBase {
    private final EasyCommand inner;
    private final int times;
    private int count;

    public RepeatCommand(EasyCommand inner) {
        this(inner, -1);
    }

    public RepeatCommand(EasyCommand inner, int times) {
        this.inner = inner;
        this.times = times;
        requirements.addAll(inner.getRequirements());
    }

    @Override
    public void initialize() {
        count = 0;
        inner.initialize();
    }

    @Override
    public void execute() {
        if (isFinished()) return;
        inner.execute();
        if (inner.isFinished()) {
            inner.end(false);
            count++;
            if (!isFinished()) inner.initialize();
        }
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) inner.end(true);
    }

    @Override
    public boolean isFinished() {
        return times > 0 && count >= times;
    }
}
