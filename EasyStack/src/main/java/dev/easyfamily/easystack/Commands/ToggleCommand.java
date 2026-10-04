package dev.easyfamily.easystack.Commands;

import java.util.function.BooleanSupplier;

/** A cada vez que é agendado alterna entre os dois comandos (1ª vez = first). */
public class ToggleCommand extends ConditionalCommand {
    public ToggleCommand(EasyCommand first, EasyCommand second) {
        super(first, second, new BooleanSupplier() {
            private boolean state = false;

            @Override
            public boolean getAsBoolean() {
                state = !state;
                return state;
            }
        });
    }
}
