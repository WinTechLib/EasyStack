package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.lagwave.wavestack.subsytem.WaveSubsytem;

public class Slide implements WaveSubsytem {

    DcMotorEx slide_motor;

    @Override
    public void init(HardwareMap hardwareMap) {
        slide_motor = hardwareMap.get(DcMotorEx.class, "slide_motor");

    }

    @Override
    public void loop() {

    }
}
