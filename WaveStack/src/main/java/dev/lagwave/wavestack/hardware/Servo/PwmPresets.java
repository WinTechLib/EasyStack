package dev.lagwave.wavestack.hardware.Servo;

import com.qualcomm.robotcore.hardware.PwmControl.PwmRange;

// *DISCLAIMER*: We do NOT responsabilize for any broken servos (xd)

public final class PwmPresets {
    private PwmPresets() {}


    public static final PwmRange SDK_DEFAULT = new PwmRange(600, 2400);

    public static final PwmRange FULL_RANGE = new PwmRange(500, 2500);
}