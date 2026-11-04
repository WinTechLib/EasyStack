package dev.easyfamily.EasyStack.Commands;

import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import dev.easyfamily.EasyStack.subsytem.EasySubsystem;

/**
 * Continuously maps an analog input to an output while scheduled.
 *
 * The input is clipped to [-1, 1]. Deadband is removed and the remaining
 * range is rescaled to preserve the full output range.
 */
public class AxisCommand extends EasyCommandBase {

    private final DoubleSupplier axis;
    private final DoubleConsumer output;
    private final double deadband;
    private final boolean zeroOutputOnEnd;

    /** Backwards-compatible constructor. The output is zeroed when the command ends. */
    public AxisCommand(
            DoubleSupplier axis,
            double deadband,
            DoubleConsumer output,
            EasySubsystem... subsystems
    ) {
        this(axis, deadband, output, true, subsystems);
    }

    /**
     * @param zeroOutputOnEnd true to send 0 when interrupted/ended; false to
     *                        preserve the last output (useful for positional servos).
     */
    public AxisCommand(
            DoubleSupplier axis,
            double deadband,
            DoubleConsumer output,
            boolean zeroOutputOnEnd,
            EasySubsystem... subsystems
    ) {
        this.axis = Objects.requireNonNull(axis, "axis");
        this.output = Objects.requireNonNull(output, "output");

        if (!Double.isFinite(deadband) || deadband < 0.0 || deadband >= 1.0) {
            throw new IllegalArgumentException("Deadband must be finite and in [0, 1).");
        }

        this.deadband = deadband;
        this.zeroOutputOnEnd = zeroOutputOnEnd;
        addRequirements(subsystems);
    }

    @Override
    public void execute() {
        double value = axis.getAsDouble();

        if (!Double.isFinite(value)) {
            value = 0.0;
        }

        value = Math.max(-1.0, Math.min(1.0, value));
        double magnitude = Math.abs(value);

        if (magnitude <= deadband) {
            output.accept(0.0);
            return;
        }

        double rescaled = (magnitude - deadband) / (1.0 - deadband);
        output.accept(Math.copySign(rescaled, value));
    }

    @Override
    public void end(boolean interrupted) {
        if (zeroOutputOnEnd) {
            output.accept(0.0);
        }
    }
}
