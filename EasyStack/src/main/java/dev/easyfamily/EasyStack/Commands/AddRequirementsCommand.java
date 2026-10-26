package dev.easyfamily.EasyStack.Commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dev.easyfamily.EasyStack.subsytem.EasySubsystem;

public class AddRequirementsCommand extends WrapperCommand {
    private final Set<EasySubsystem> merged = new HashSet<>();

    public AddRequirementsCommand(EasyCommand inner, EasySubsystem... extra) {
        super(inner);
        merged.addAll(inner.getRequirements());
        merged.addAll(Arrays.asList(extra));
    }

    @Override
    public Set<EasySubsystem> getRequirements() {
        return merged;
    }
}
