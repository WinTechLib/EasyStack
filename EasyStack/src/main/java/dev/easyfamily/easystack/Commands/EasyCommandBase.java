package dev.easyfamily.easystack.Commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

public abstract class EasyCommandBase implements EasyCommand {
    protected Set<EasySubsystem> requirements = new HashSet<>();

    public void addRequirements(EasySubsystem... subsystems) {
        requirements.addAll(Arrays.asList(subsystems));
    }

    @Override
    public Set<EasySubsystem> getRequirements() {
        return requirements;
    }
}