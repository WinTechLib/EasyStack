package dev.easyfamily.easystack.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CommandGroupBase extends EasyCommandBase {
    protected final List<EasyCommand> commands = new ArrayList<>();

    /** exclusive = true para grupos paralelos (comandos não podem dividir subsistemas). */
    protected final void register(boolean exclusive, EasyCommand... cmds) {
        for (EasyCommand c : cmds) {
            if (commands.contains(c)) {
                throw new IllegalArgumentException("Comando duplicado no grupo.");
            }
            if (exclusive && !Collections.disjoint(requirements, c.getRequirements())) {
                throw new IllegalArgumentException("Comandos paralelos não podem compartilhar subsistemas.");
            }
            commands.add(c);
            requirements.addAll(c.getRequirements());
        }
    }

    public List<EasyCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    static EasyCommand[] concat(EasyCommand[] a, EasyCommand[] b) {
        EasyCommand[] out = new EasyCommand[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
