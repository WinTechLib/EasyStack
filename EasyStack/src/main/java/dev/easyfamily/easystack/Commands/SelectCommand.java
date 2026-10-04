package dev.easyfamily.easystack.Commands;

import java.util.Map;
import java.util.function.Supplier;

/** Escolhe um comando do mapa de acordo com a chave no momento do início (ex.: enum de estado). */
public class SelectCommand<K> extends EasyCommandBase {
    private final Map<K, EasyCommand> commands;
    private final Supplier<K> selector;
    private EasyCommand selected;

    public SelectCommand(Map<K, EasyCommand> commands, Supplier<K> selector) {
        this.commands = commands;
        this.selector = selector;
        for (EasyCommand c : commands.values()) requirements.addAll(c.getRequirements());
    }

    @Override
    public void initialize() {
        selected = commands.get(selector.get());
        if (selected == null) selected = new NoOpCommand();
        selected.initialize();
    }

    @Override public void execute() { if (selected != null) selected.execute(); }
    @Override public void end(boolean interrupted) { if (selected != null) selected.end(interrupted); }
    @Override public boolean isFinished() { return selected == null || selected.isFinished(); }
}
