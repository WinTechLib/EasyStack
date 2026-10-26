package dev.easyfamily.EasyStack.subsytem;

import dev.easyfamily.EasyStack.Commands.EasyCommand;
import dev.easyfamily.EasyStack.Commands.EasyCommandScheduler;
import dev.easyfamily.EasyStack.Commands.InstantCommand;
import dev.easyfamily.EasyStack.Commands.RunCommand;
import dev.easyfamily.EasyStack.Commands.StartEndCommand;

public abstract class EasySubsystemBase implements EasySubsystem {

    public EasySubsystemBase() {
        EasyCommandScheduler.getInstance().registerSubsystem(this);
    }

    public void setDefaultCommand(EasyCommand command) {
        EasyCommandScheduler.getInstance().setDefaultCommand(this, command);
    }

    public EasyCommand getCurrentCommand() {
        return EasyCommandScheduler.getInstance().getCurrentCommand(this);
    }
    public EasyCommand run(Runnable action) {
        return new RunCommand(action, this);
    }
    public EasyCommand runOnce(Runnable action) {
        return new InstantCommand(action, this);
    }
    public EasyCommand startEnd(Runnable start, Runnable end) {
        return new StartEndCommand(start, end, this);
    }
    public EasyCommand runEnd(Runnable action, Runnable end) {
        return new RunCommand(action, this).finallyDo(interrupted -> end.run());
    }
}
