package dev.easyfamily.EasyStack.subsytem;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

public interface EasySubsystem {

    void init(HardwareMap hardwareMap);
    default void loop() {}

    default String getName() {
        return getClass().getSimpleName();
    }
}
