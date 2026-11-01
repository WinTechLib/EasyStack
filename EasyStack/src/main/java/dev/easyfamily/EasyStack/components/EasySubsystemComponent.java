package dev.easyfamily.EasyStack.components;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.easyfamily.EasyStack.subsytem.EasySubsystem;

public class EasySubsystemComponent implements EasyComponent {

    private final List<EasySubsystem> subsystems = new ArrayList<>();

    public EasySubsystemComponent(EasySubsystem... subsystems) {
        for (EasySubsystem subsystem : subsystems) {
            if (subsystem != null && !this.subsystems.contains(subsystem)) {
                this.subsystems.add(subsystem);
            }
        }
    }

    @Override
    public void init(HardwareMap hardwareMap) {
        for (EasySubsystem subsystem : subsystems) {
            subsystem.init(hardwareMap);
        }
    }

    @Override
    public void loop() {
        for (EasySubsystem subsystem : subsystems) {
            subsystem.loop();
        }
    }

    @Override
    public void stop() {
        // Futuramente, podemos adicionar um lifecycle de encerramento
        // individual aos subsistemas.
    }

    public List<EasySubsystem> getSubsystems() {
        return new ArrayList<>(subsystems);
    }
}