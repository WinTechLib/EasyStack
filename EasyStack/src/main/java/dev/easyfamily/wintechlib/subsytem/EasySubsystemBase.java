package dev.easyfamily.wintechlib.subsytem;

import dev.easyfamily.wintechlib.Commands.EasyCommand;
import dev.easyfamily.wintechlib.Commands.EasyCommandScheduler;
import dev.easyfamily.wintechlib.Commands.InstantCommand;
import dev.easyfamily.wintechlib.Commands.RunCommand;
import dev.easyfamily.wintechlib.Commands.StartEndCommand;

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
