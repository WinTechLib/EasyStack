package dev.easyfamily.easystack.Commands;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import dev.easyfamily.easystack.subsytem.EasySubsystem;


public class AxisCommand extends EasyCommandBase {
    private final DoubleSupplier axis;
    private final double deadband;
    private final DoubleConsumer output;

    public AxisCommand(DoubleSupplier axis, double deadband, DoubleConsumer output, EasySubsystem... subsystems) {
        this.axis = axis;
        this.deadband = deadband;
        this.output = output;
        addRequirements(subsystems);
    }

    @Override
    public void execute() {
        double v = axis.getAsDouble();
        if (Math.abs(v) < deadband) {
            output.accept(0.0);
        } else {
            // reescala para começar em 0 logo após a deadband
            output.accept(Math.signum(v) * (Math.abs(v) - deadband) / (1.0 - deadband));
        }
    }

    @Override
    public void end(boolean interrupted) {
        output.accept(0.0);
    }
}
