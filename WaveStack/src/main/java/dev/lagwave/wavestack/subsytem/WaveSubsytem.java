package dev.lagwave.wavestack.subsytem;

import com.qualcomm.robotcore.hardware.HardwareMap;

public interface WaveSubsytem {

    void init(HardwareMap hardwareMap);

    void loop() ;
}
