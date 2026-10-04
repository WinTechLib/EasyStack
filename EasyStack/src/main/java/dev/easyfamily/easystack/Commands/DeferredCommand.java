package dev.easyfamily.easystack.Commands;

import java.util.Arrays;
import java.util.function.Supplier;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

/** Cria o comando só na hora de iniciar (útil quando o comando depende de valores do momento). */
public class DeferredCommand extends EasyCommandBase {
    private final Supplier<EasyCommand> supplier;
    private EasyCommand inner;

    public DeferredCommand(Supplier<EasyCommand> supplier, EasySubsystem... subsystems) {
        this.supplier = supplier;
        addRequirements(subsystems);
    }

    @Override
    public void initialize() {
        inner = supplier.get();
        if (!requirements.containsAll(inner.getRequirements())) {
            throw new IllegalArgumentException("O comando criado requer subsistemas não declarados no DeferredCommand: "
                    + Arrays.toString(inner.getRequirements().toArray()));
        }
        inner.initialize();
    }

    @Override public void execute() { if (inner != null) inner.execute(); }
    @Override public void end(boolean interrupted) { if (inner != null) inner.end(interrupted); }
    @Override public boolean isFinished() { return inner == null || inner.isFinished(); }
}
