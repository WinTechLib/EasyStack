package dev.easyfamily.easystack.Commands;

import java.util.function.DoubleConsumer;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

/** Interpola linearmente de "from" até "to" em X ms (ex.: varrer servo, rampa de potência). */
public class RampCommand extends EasyCommandBase {
    private final double from, to;
    private final long millis;
    private final DoubleConsumer output;
    private long startNanos;

    public RampCommand(double from, double to, long millis, DoubleConsumer output, EasySubsystem... subsystems) {
        this.from = from;
        this.to = to;
        this.millis = millis;
        this.output = output;
        addRequirements(subsystems);
    }

    private double fraction() {
        if (millis <= 0) return 1.0;
        double f = (System.nanoTime() - startNanos) / 1_000_000.0 / millis;
        return Math.min(1.0, Math.max(0.0, f));
    }

    @Override
    public void initialize() {
        startNanos = System.nanoTime();
        output.accept(from);
    }

    @Override
    public void execute() {
        output.accept(from + (to - from) * fraction());
    }

    @Override
    public boolean isFinished() {
        return fraction() >= 1.0;
    }

    @Override
    public void end(boolean interrupted) {
        if (!interrupted) output.accept(to);
    }
}
