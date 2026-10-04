package dev.easyfamily.easystack.Commands;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

public interface EasyCommand {
    default void initialize() {}

    default void execute() {}

    default void end(boolean interrupted) {}

    default boolean isFinished() {
        return false;
    }

    Set<EasySubsystem> getRequirements();

    // ---------- Agendamento ----------
    default void schedule() { EasyCommandScheduler.getInstance().schedule(this); }

    default void cancel() { EasyCommandScheduler.getInstance().cancel(this); }

    default boolean isScheduled() { return EasyCommandScheduler.getInstance().isScheduled(this); }

    // ---------- Decorators ----------
    default EasyCommand withTimeout(long millis) { return new TimeoutCommand(this, millis); }

    default EasyCommand until(BooleanSupplier condition) { return new UntilCommand(this, condition); }

    default EasyCommand andThen(EasyCommand... next) {
        return new SequentialCommandGroup(CommandGroupBase.concat(new EasyCommand[]{this}, next));
    }

    default EasyCommand andThenRun(Runnable action) { return andThen(new InstantCommand(action)); }

    default EasyCommand beforeStarting(EasyCommand... before) {
        return new SequentialCommandGroup(CommandGroupBase.concat(before, new EasyCommand[]{this}));
    }

    default EasyCommand afterDelay(long millis) { return new DelayedCommand(millis, this); }

    default EasyCommand alongWith(EasyCommand... others) {
        return new ParallelCommandGroup(CommandGroupBase.concat(new EasyCommand[]{this}, others));
    }

    default EasyCommand raceWith(EasyCommand... others) {
        return new ParallelRaceGroup(CommandGroupBase.concat(new EasyCommand[]{this}, others));
    }

    /** Este comando é o "deadline": quando ele termina, os outros são encerrados. */
    default EasyCommand deadlineWith(EasyCommand... others) { return new ParallelDeadlineGroup(this, others); }

    default EasyCommand repeatedly() { return new RepeatCommand(this); }

    default EasyCommand repeat(int times) { return new RepeatCommand(this, times); }

    default EasyCommand perpetually() { return new PerpetualCommand(this); }

    default EasyCommand finallyDo(Consumer<Boolean> action) { return new FinallyCommand(this, action); }

    default EasyCommand onlyIf(BooleanSupplier condition) {
        return new ConditionalCommand(this, new NoOpCommand(), condition);
    }

    default EasyCommand unless(BooleanSupplier condition) {
        return new ConditionalCommand(new NoOpCommand(), this, condition);
    }

    default EasyCommand withRequirements(EasySubsystem... extra) { return new AddRequirementsCommand(this, extra); }

    default EasyCommand asProxy() { return new ProxyCommand(this); }
}
