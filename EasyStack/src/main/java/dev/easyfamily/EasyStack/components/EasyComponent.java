package dev.easyfamily.EasyStack.components;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Base interface for EasyStack components.
 *
 * Components manage different parts of the OpMode lifecycle.
 */
public interface EasyComponent {

    default void init(HardwareMap hardwareMap) {}

    default void start() {}

    default void loop() {}

    default void stop() {}
}