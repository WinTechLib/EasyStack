package dev.easyfamily.easystack.Commands;

import java.util.function.DoubleSupplier;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

/** Perfil trapezoidal (aceleração/cruzeiro/desaceleração). Entrega posição e velocidade desejadas a cada loop. */
public class TrapezoidProfileCommand extends EasyCommandBase {
    public interface Output {
        void accept(double position, double velocity);
    }

    private final DoubleSupplier start, goal;
    private final double maxVel, maxAccel;
    private final Output output;

    private double s0, dir, dist, tAcc, tCruise, total, peak;
    private long startNanos;

    public TrapezoidProfileCommand(DoubleSupplier start, DoubleSupplier goal, double maxVel, double maxAccel,
                                   Output output, EasySubsystem... subsystems) {
        this.start = start;
        this.goal = goal;
        this.maxVel = maxVel;
        this.maxAccel = maxAccel;
        this.output = output;
        addRequirements(subsystems);
    }

    @Override
    public void initialize() {
        s0 = start.getAsDouble();
        double d = goal.getAsDouble() - s0;
        dir = Math.signum(d);
        dist = Math.abs(d);
        tAcc = maxVel / maxAccel;
        double accDist = 0.5 * maxAccel * tAcc * tAcc;
        if (2 * accDist > dist) { // triangular: não chega na velocidade máxima
            tAcc = Math.sqrt(dist / maxAccel);
            peak = maxAccel * tAcc;
            tCruise = 0;
        } else {
            peak = maxVel;
            tCruise = (dist - 2 * accDist) / maxVel;
        }
        total = 2 * tAcc + tCruise;
        startNanos = System.nanoTime();
    }

    private double elapsed() {
        return (System.nanoTime() - startNanos) / 1_000_000_000.0;
    }

    @Override
    public void execute() {
        double t = elapsed();
        double p, v;
        if (t >= total) {
            p = dist;
            v = 0;
        } else if (t < tAcc) {
            p = 0.5 * maxAccel * t * t;
            v = maxAccel * t;
        } else if (t < tAcc + tCruise) {
            p = 0.5 * maxAccel * tAcc * tAcc + peak * (t - tAcc);
            v = peak;
        } else {
            double r = total - t;
            p = dist - 0.5 * maxAccel * r * r;
            v = maxAccel * r;
        }
        output.accept(s0 + dir * p, dir * v);
    }

    @Override
    public boolean isFinished() {
        return elapsed() >= total;
    }
}
