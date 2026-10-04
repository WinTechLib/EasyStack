package dev.easyfamily.easystack.Commands;

import java.util.Set;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

/** Base para decorators: delega tudo ao comando interno. */
public abstract class WrapperCommand implements EasyCommand {
    protected final EasyCommand inner;

    protected WrapperCommand(EasyCommand inner) {
        this.inner = inner;
    }

    @Override public void initialize() { inner.initialize(); }
    @Override public void execute() { inner.execute(); }
    @Override public void end(boolean interrupted) { inner.end(interrupted); }
    @Override public boolean isFinished() { return inner.isFinished(); }
    @Override public Set<EasySubsystem> getRequirements() { return inner.getRequirements(); }
}
