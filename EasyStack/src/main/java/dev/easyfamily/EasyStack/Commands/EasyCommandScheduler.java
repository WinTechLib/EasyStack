package dev.easyfamily.EasyStack.Commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.easyfamily.EasyStack.subsytem.EasySubsystem;

/** Scheduler for EasyStack commands and subsystem requirements. */
public class EasyCommandScheduler {

    private static EasyCommandScheduler instance;

    private final Map<EasySubsystem, EasyCommand> defaultCommands = new LinkedHashMap<>();
    private final Set<EasyCommand> scheduledCommands = new LinkedHashSet<>();
    private final Map<EasySubsystem, EasyCommand> requirementMap = new LinkedHashMap<>();
    private final List<EasyTrigger> triggers = new ArrayList<>();

    private final List<EasyCommand> pendingSchedule = new ArrayList<>();
    private final List<EasyCommand> pendingCancel = new ArrayList<>();

    private boolean iterating = false;
    private boolean dispatching = false;
    private boolean processingPending = false;
    private boolean resetting = false;

    private EasyCommandScheduler() {
    }

    public static synchronized EasyCommandScheduler getInstance() {
        if (instance == null) {
            instance = new EasyCommandScheduler();
        }
        return instance;
    }

    public void addTrigger(EasyTrigger trigger) {
        if (trigger != null && !triggers.contains(trigger)) {
            triggers.add(trigger);
        }
    }

    public void setDefaultCommand(EasySubsystem subsystem, EasyCommand defaultCommand) {
        if (subsystem == null || defaultCommand == null) {
            throw new IllegalArgumentException("Subsystem and default command cannot be null.");
        }

        if (!defaultCommand.getRequirements().contains(subsystem)) {
            throw new IllegalArgumentException(
                    "Default command must require its subsystem."
            );
        }

        defaultCommands.put(subsystem, defaultCommand);
    }

    public void schedule(EasyCommand command) {
        if (command == null || resetting) {
            return;
        }

        if (isBusy()) {
            pendingCancel.remove(command);
            if (!scheduledCommands.contains(command)
                    && !pendingSchedule.contains(command)) {
                pendingSchedule.add(command);
            }
            return;
        }

        scheduleNow(command);
        flushPending();
    }

    private void scheduleNow(EasyCommand command) {
        if (command == null || scheduledCommands.contains(command)) {
            return;
        }

        boolean wasDispatching = dispatching;
        dispatching = true;
        try {
            // Interrupt commands that currently own any requested subsystem.
            for (EasySubsystem requirement : command.getRequirements()) {
                EasyCommand running = requirementMap.get(requirement);
                if (running != null && running != command) {
                    cancelNow(running);
                }
            }

            // Reserve requirements before initialize() to prevent re-entrant conflicts.
            scheduledCommands.add(command);
            for (EasySubsystem requirement : command.getRequirements()) {
                requirementMap.put(requirement, command);
            }

            try {
                command.initialize();
            } catch (RuntimeException exception) {
                scheduledCommands.remove(command);
                for (EasySubsystem requirement : command.getRequirements()) {
                    requirementMap.remove(requirement, command);
                }
                command.end(true);
                throw exception;
            }
        } finally {
            dispatching = wasDispatching;
        }
    }

    public void cancel(EasyCommand command) {
        if (command == null) {
            return;
        }

        if (isBusy()) {
            pendingSchedule.remove(command);
            if (!pendingCancel.contains(command)) {
                pendingCancel.add(command);
            }
            return;
        }

        cancelNow(command);
        flushPending();
    }

    private void cancelNow(EasyCommand command) {
        pendingSchedule.remove(command);

        if (!scheduledCommands.remove(command)) {
            return;
        }

        for (EasySubsystem requirement : command.getRequirements()) {
            requirementMap.remove(requirement, command);
        }

        boolean wasDispatching = dispatching;
        dispatching = true;
        try {
            command.end(true);
        } finally {
            dispatching = wasDispatching;
        }
    }

    public void cancelAll() {
        if (isBusy()) {
            for (EasyCommand command : scheduledCommands) {
                if (!pendingCancel.contains(command)) {
                    pendingCancel.add(command);
                }
            }
            return;
        }

        for (EasyCommand command : new ArrayList<>(scheduledCommands)) {
            cancelNow(command);
        }

        flushPending();
    }

    public boolean isScheduled(EasyCommand command) {
        return scheduledCommands.contains(command) || pendingSchedule.contains(command);
    }

    public EasyCommand getCurrentCommand(EasySubsystem subsystem) {
        return requirementMap.get(subsystem);
    }

    public void run() {
        if (iterating || resetting) {
            return;
        }

        // Poll a snapshot so bindings cannot invalidate trigger iteration.
        for (EasyTrigger trigger : new ArrayList<>(triggers)) {
            trigger.poll();
        }

        iterating = true;
        try {
            Iterator<EasyCommand> iterator = scheduledCommands.iterator();

            while (iterator.hasNext()) {
                EasyCommand command = iterator.next();
                boolean finished;

                try {
                    command.execute();
                    finished = command.isFinished();
                } catch (RuntimeException exception) {
                    iterator.remove();
                    releaseRequirements(command);
                    invokeEnd(command, true);
                    throw exception;
                }

                if (finished) {
                    iterator.remove();
                    releaseRequirements(command);
                    invokeEnd(command, false);
                }
            }
        } finally {
            iterating = false;
        }

        flushPending();
        scheduleDefaults();
    }

    private void scheduleDefaults() {
        for (Map.Entry<EasySubsystem, EasyCommand> entry
                : new ArrayList<>(defaultCommands.entrySet())) {

            EasyCommand command = entry.getValue();
            boolean allRequirementsFree = true;

            for (EasySubsystem requirement : command.getRequirements()) {
                if (requirementMap.containsKey(requirement)) {
                    allRequirementsFree = false;
                    break;
                }
            }

            if (allRequirementsFree && !isScheduled(command)) {
                schedule(command);
            }
        }
    }


    private void invokeEnd(EasyCommand command, boolean interrupted) {
        boolean wasDispatching = dispatching;
        dispatching = true;
        try {
            command.end(interrupted);
        } finally {
            dispatching = wasDispatching;
        }
    }

    private void releaseRequirements(EasyCommand command) {
        for (EasySubsystem requirement : command.getRequirements()) {
            requirementMap.remove(requirement, command);
        }
    }

    private boolean isBusy() {
        return iterating || dispatching || processingPending;
    }

    private void flushPending() {
        if (isBusy() || resetting) {
            return;
        }

        processingPending = true;
        try {
            while (!pendingCancel.isEmpty() || !pendingSchedule.isEmpty()) {

                List<EasyCommand> cancels = new ArrayList<>(pendingCancel);
                pendingCancel.clear();

                for (EasyCommand command : cancels) {
                    cancelNow(command);
                }

                List<EasyCommand> schedules = new ArrayList<>(pendingSchedule);
                pendingSchedule.clear();

                for (EasyCommand command : schedules) {
                    scheduleNow(command);
                }
            }
        } finally {
            processingPending = false;
        }
    }

    /** Clears commands, defaults and triggers. Call before configuring an OpMode. */
    public void reset() {
        if (isBusy()) {
            throw new IllegalStateException("Cannot reset the scheduler while it is running.");
        }

        resetting = true;

        List<EasyCommand> toEnd = new ArrayList<>(scheduledCommands);
        scheduledCommands.clear();
        requirementMap.clear();
        pendingSchedule.clear();
        pendingCancel.clear();
        defaultCommands.clear();
        triggers.clear();

        try {
            for (EasyCommand command : toEnd) {
                command.end(true);
            }
        } finally {
            resetting = false;
        }
    }
}
