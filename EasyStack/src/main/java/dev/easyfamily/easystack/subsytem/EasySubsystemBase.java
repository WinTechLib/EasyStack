package dev.easyfamily.easystack.subsytem;

import dev.easyfamily.easystack.Commands.EasyCommand;
import dev.easyfamily.easystack.Commands.EasyCommandScheduler;
import dev.easyfamily.easystack.Commands.InstantCommand;
import dev.easyfamily.easystack.Commands.RunCommand;
import dev.easyfamily.easystack.Commands.StartEndCommand;

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
