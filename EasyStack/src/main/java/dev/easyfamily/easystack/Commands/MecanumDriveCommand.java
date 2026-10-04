package dev.easyfamily.easystack.Commands;

import java.util.function.DoubleSupplier;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

/** Drive mecanum robot-centric: calcula as 4 potências normalizadas e entrega para a saída. */
public class MecanumDriveCommand extends EasyCommandBase {
    public interface Output {
        void accept(double frontLeft, double frontRight, double backLeft, double backRight);
    }

    private final DoubleSupplier forward, strafe, turn;
    private final Output output;

    public MecanumDriveCommand(DoubleSupplier forward, DoubleSupplier strafe, DoubleSupplier turn,
                               Output output, EasySubsystem... subsystems) {
        this.forward = forward;
        this.strafe = strafe;
        this.turn = turn;
        this.output = output;
        addRequirements(subsystems);
    }

    @Override
    public void execute() {
        double f = forward.getAsDouble(), s = strafe.getAsDouble(), t = turn.getAsDouble();
        double fl = f + s + t, fr = f - s - t, bl = f - s + t, br = f + s - t;
        double max = Math.max(1.0, Math.max(Math.max(Math.abs(fl), Math.abs(fr)), Math.max(Math.abs(bl), Math.abs(br))));
        output.accept(fl / max, fr / max, bl / max, br / max);
    }

    @Override
    public void end(boolean interrupted) {
        output.accept(0, 0, 0, 0);
    }
}
