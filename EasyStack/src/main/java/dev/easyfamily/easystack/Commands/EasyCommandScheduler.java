package dev.easyfamily.easystack.Commands;

import com.qualcomm.robotcore.hardware.HardwareMap;
import dev.easyfamily.easystack.subsytem.EasySubsystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EasyCommandScheduler {
    private static EasyCommandScheduler instance;

    private final Set<EasySubsystem> subsystems = new LinkedHashSet<>();
    private final Map<EasySubsystem, EasyCommand> defaultCommands = new LinkedHashMap<>();

    private final Set<EasyCommand> scheduledCommands = new LinkedHashSet<>();
    private final Map<EasySubsystem, EasyCommand> requirementMap = new LinkedHashMap<>();

    private final List<EasyTrigger> triggers = new ArrayList<>();

    // schedule/cancel chamados durante o loop de execute() ficam na fila (evita ConcurrentModification)
    private final List<EasyCommand> pendingSchedule = new ArrayList<>();
    private final List<EasyCommand> pendingCancel = new ArrayList<>();
    private boolean iterating = false;

    private EasyCommandScheduler() {}

    public static synchronized EasyCommandScheduler getInstance() {
        if (instance == null) {
            instance = new EasyCommandScheduler();
        }
        return instance;
    }

    public void registerSubsystem(EasySubsystem subsystem) {
        subsystems.add(subsystem);
    }

    public void addTrigger(EasyTrigger trigger) {
        triggers.add(trigger);
    }

    public void setDefaultCommand(EasySubsystem subsystem, EasyCommand defaultCommand) {
        if (!defaultCommand.getRequirements().contains(subsystem)) {
            throw new IllegalArgumentException("O comando por defecto deve requerer o subsistema.");
        }
        defaultCommands.put(subsystem, defaultCommand);
    }

    /** Inicializa todos os subsistemas registrados de uma vez. */
    public void init(HardwareMap hardwareMap) {
        for (EasySubsystem subsystem : subsystems) {
            subsystem.init(hardwareMap);
        }
    }

    public void schedule(EasyCommand command) {
        if (iterating) {
            if (!pendingSchedule.contains(command)) pendingSchedule.add(command);
            return;
        }
        if (scheduledCommands.contains(command)) return;

        for (EasySubsystem requirement : command.getRequirements()) {
            EasyCommand running = requirementMap.get(requirement);
            if (running != null) cancel(running);
        }

        command.initialize();
        scheduledCommands.add(command);
        for (EasySubsystem requirement : command.getRequirements()) {
            requirementMap.put(requirement, command);
        }
    }

    public void cancel(EasyCommand command) {
        if (iterating) {
            pendingCancel.add(command);
            return;
        }
        pendingSchedule.remove(command);
        if (!scheduledCommands.remove(command)) return;
        for (EasySubsystem req : command.getRequirements()) {
            requirementMap.remove(req, command);
        }
        command.end(true);
    }

    public void cancelAll() {
        for (EasyCommand command : new ArrayList<>(scheduledCommands)) {
            cancel(command);
        }
    }

    public boolean isScheduled(EasyCommand command) {
        return scheduledCommands.contains(command) || pendingSchedule.contains(command);
    }

    /** Comando que está usando o subsistema agora (ou null). */
    public EasyCommand getCurrentCommand(EasySubsystem subsystem) {
        return requirementMap.get(subsystem);
    }

    public void run() {
        // 0. Triggers (botões/condições -> agendam comandos)
        for (EasyTrigger trigger : triggers) {
            trigger.poll();
        }

        // 1. Loop de todos os subsistemas
        for (EasySubsystem subsystem : subsystems) {
            subsystem.loop();
        }

        // 2. Executa comandos programados
        iterating = true;
        try {
            Iterator<EasyCommand> iterator = scheduledCommands.iterator();
            while (iterator.hasNext()) {
                EasyCommand command = iterator.next();
                command.execute();

                if (command.isFinished()) {
                    iterator.remove();
                    for (EasySubsystem req : command.getRequirements()) {
                        requirementMap.remove(req, command);
                    }
                    command.end(false);
                }
            }
        } finally {
            iterating = false;
        }
        flushPending();

        // 3. Agenda comandos por padrão se o subsistema estiver ocioso
        for (Map.Entry<EasySubsystem, EasyCommand> entry : defaultCommands.entrySet()) {
            if (!requirementMap.containsKey(entry.getKey())) {
                schedule(entry.getValue());
            }
        }
    }

    private void flushPending() {
        List<EasyCommand> cancels = new ArrayList<>(pendingCancel);
        pendingCancel.clear();
        for (EasyCommand c : cancels) cancel(c);

        List<EasyCommand> schedules = new ArrayList<>(pendingSchedule);
        pendingSchedule.clear();
        for (EasyCommand c : schedules) schedule(c);
    }

    public void reset() {
        List<EasyCommand> toEnd = new ArrayList<>(scheduledCommands);
        scheduledCommands.clear();
        requirementMap.clear();
        pendingSchedule.clear();
        pendingCancel.clear();
        subsystems.clear();
        defaultCommands.clear();
        triggers.clear();
        for (EasyCommand command : toEnd) {
            command.end(true);
        }
    }
}
