package dev.easyfamily.easystack.Commands;

import java.util.function.DoubleSupplier;

import dev.easyfamily.easystack.subsytem.EasySubsystem;
import dev.easyfamily.easystack.trajectory.TrapezoidProfile;

/** Perfil trapezoidal: entrega posição e velocidade desejadas a cada loop até chegar no goal. */
public class TrapezoidProfileCommand extends EasyCommandBase {
    public interface Output {
        void accept(double position, double velocity);
    }

    private final DoubleSupplier start, goal;
    private final TrapezoidProfile.Constraints constraints;
    private final Output output;

    private TrapezoidProfile profile;
    private long startNanos;

    public TrapezoidProfileCommand(DoubleSupplier start, DoubleSupplier goal, double maxVel, double maxAccel,
                                   Output output, EasySubsystem... subsystems) {
        this.start = start;
        this.goal = goal;
        this.constraints = new TrapezoidProfile.Constraints(maxVel, maxAccel);
        this.output = output;
        addRequirements(subsystems);
    }

    @Override
    public void initialize() {
        profile = new TrapezoidProfile(constraints,
                new TrapezoidProfile.State(goal.getAsDouble(), 0.0),
                new TrapezoidProfile.State(start.getAsDouble(), 0.0));
        startNanos = System.nanoTime();
    }

    private double elapsed() {
        return (System.nanoTime() - startNanos) / 1_000_000_000.0;
    }

    @Override
    public void execute() {
        TrapezoidProfile.State s = profile.calculate(elapsed());
        output.accept(s.position, s.velocity);
    }

    @Override
    public boolean isFinished() {
        return profile != null && profile.isFinished(elapsed());
    }
}
