package dev.easyfamily.easystack.Commands;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import dev.easyfamily.easystack.subsytem.EasySubsystem;
public class ClosedLoopCommand extends EasyCommandBase {
    private final DoubleBinaryOperator controller;
    private final DoubleSupplier measurement, setpoint;
    private final DoubleConsumer output;
    private final BooleanSupplier atSetpoint;

    public ClosedLoopCommand(DoubleBinaryOperator controller, DoubleSupplier measurement, DoubleSupplier setpoint,
                             DoubleConsumer output, BooleanSupplier atSetpoint, EasySubsystem... subsystems) {
        this.controller = controller;
        this.measurement = measurement;
        this.setpoint = setpoint;
        this.output = output;
        this.atSetpoint = atSetpoint;
        addRequirements(subsystems);
    }
    public ClosedLoopCommand(DoubleBinaryOperator controller, DoubleSupplier measurement, DoubleSupplier setpoint,
                             DoubleConsumer output, double tolerance, EasySubsystem... subsystems) {
        this(controller, measurement, setpoint, output,
                () -> Math.abs(setpoint.getAsDouble() - measurement.getAsDouble()) <= tolerance, subsystems);
    }

    public ClosedLoopCommand(DoubleBinaryOperator controller, DoubleSupplier measurement, DoubleSupplier setpoint,
                             DoubleConsumer output, EasySubsystem... subsystems) {
        this(controller, measurement, setpoint, output, (BooleanSupplier) null, subsystems);
    }

    @Override
    public void execute() {
        output.accept(controller.applyAsDouble(measurement.getAsDouble(), setpoint.getAsDouble()));
    }

    @Override
    public boolean isFinished() {
        return atSetpoint != null && atSetpoint.getAsBoolean();
    }

    @Override
    public void end(boolean interrupted) {
        output.accept(0.0);
    }
}
