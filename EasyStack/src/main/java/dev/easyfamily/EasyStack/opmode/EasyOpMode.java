package dev.easyfamily.EasyStack.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.easyfamily.EasyStack.Commands.EasyCommandScheduler;
import dev.easyfamily.EasyStack.components.EasyComponent;

public abstract class EasyOpMode extends LinearOpMode {

    protected final EasyCommandScheduler scheduler =
            EasyCommandScheduler.getInstance();

    private final List<EasyComponent> components = new ArrayList<>();

    protected final void addComponents(EasyComponent... components) {

        for (EasyComponent component : components) {

            if (component != null && !this.components.contains(component)) {
                this.components.add(component);
            }
        }
    }

    public abstract void initialize();

    public void onStart() {}

    public abstract void onUpdate();

    public void onStop() {}

    private void initializeComponents(HardwareMap hardwareMap) {
        for (EasyComponent component : components) {
            component.init(hardwareMap);
        }
    }

    private void startComponents() {
        for (EasyComponent component : components) {
            component.start();
        }
    }

    private void updateComponents() {
        for (EasyComponent component : components) {
            component.loop();
        }
    }

    private void stopComponents() {
        for (int i = components.size() - 1; i >= 0; i--) {
            components.get(i).stop();
        }
    }

    @Override
    public final void runOpMode() {

        scheduler.reset();

        try {

            initialize();

            initializeComponents(hardwareMap);

            waitForStart();

            if (isStopRequested()) return;

            startComponents();

            onStart();

            while (opModeIsActive() && !isStopRequested()) {

                updateComponents();

                onUpdate();

                telemetry.update();
            }

        } finally {

            stopComponents();

            onStop();

            scheduler.reset();
        }
    }
}