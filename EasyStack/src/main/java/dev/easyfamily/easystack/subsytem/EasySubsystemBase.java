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

    /** Comando que está usando este subsistema agora (ou null). */
    public EasyCommand getCurrentCommand() {
        return EasyCommandScheduler.getInstance().getCurrentCommand(this);
    }

    /** Executa a ação a cada loop até ser cancelado. */
    public EasyCommand run(Runnable action) {
        return new RunCommand(action, this);
    }

    /** Executa a ação uma vez. */
    public EasyCommand runOnce(Runnable action) {
        return new InstantCommand(action, this);
    }

    /** start ao iniciar, end ao terminar/interromper. */
    public EasyCommand startEnd(Runnable start, Runnable end) {
        return new StartEndCommand(start, end, this);
    }

    /** Executa a ação a cada loop e roda end ao terminar/interromper. */
    public EasyCommand runEnd(Runnable action, Runnable end) {
        return new RunCommand(action, this).finallyDo(interrupted -> end.run());
    }
}
