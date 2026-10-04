package dev.easyfamily.easystack.Commands;

/** Espera X ms e então roda o comando. */
public class DelayedCommand extends SequentialCommandGroup {
    public DelayedCommand(long delayMillis, EasyCommand command) {
        super(new WaitCommand(delayMillis), command);
    }
}
