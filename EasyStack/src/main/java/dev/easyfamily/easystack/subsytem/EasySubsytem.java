package dev.easyfamily.easystack.subsytem;

import com.qualcomm.robotcore.hardware.HardwareMap;

public interface EasySubsytem {

    void init(HardwareMap hardwareMap);

    void loop() ;
}