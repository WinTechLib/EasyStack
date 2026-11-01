package dev.easyfamily.EasyStack.Commands;

import dev.easyfamily.EasyStack.subsytem.EasySubsystem;

/** Liga (on) por X ms e depois desliga (off). Ex.: intake, solenoide, LED. */
public class PulseCommand extends TimeoutCommand {
    public PulseCommand(Runnable on, Runnable off, long millis, EasySubsystem... subsystems) {
        super(new StartEndCommand(on, off, subsystems), millis);
    }
}
