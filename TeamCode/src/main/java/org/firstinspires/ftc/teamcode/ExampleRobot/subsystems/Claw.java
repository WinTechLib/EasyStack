package org.firstinspires.ftc.teamcode.ExampleRobot.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.EasyStack.hardware.Servo.EasyServo;
import dev.easyfamily.EasyStack.subsytem.EasySubsystemBase;

public class Claw extends EasySubsystemBase {

    private EasyServo claw;

    private static final String OPEN = "open";
    private static final String CLOSED = "closed";

    @Override
    public void init(HardwareMap hardwareMap) {
        claw = new EasyServo(hardwareMap, "claw", 90.0)
                .addState(OPEN, 0.0)
                .addState(CLOSED, 1.0);
    }

    public void open() {
        ensureInitialized();
        claw.setState(OPEN);
    }

    public void close() {
        ensureInitialized();
        claw.setState(CLOSED);
    }

    public void setPosition(double position) {
        ensureInitialized();
        claw.setPosition(position);
    }

    public boolean isAtTarget() {
        ensureInitialized();
        return claw.isAtTarget();
    }

    public EasyServo getServo() {
        ensureInitialized();
        return claw;
    }

    @Override
    public void loop() {
        // Positional servo; no periodic update is required.
    }

    private void ensureInitialized() {
        if (claw == null) {
            throw new IllegalStateException("Claw has not been initialized.");
        }
    }
}
