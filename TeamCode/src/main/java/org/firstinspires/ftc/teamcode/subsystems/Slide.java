package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

public class Slide implements EasySubsystem {

    DcMotorEx slide_motor;


    @Override
    public void init(HardwareMap hardwareMap) {


        slide_motor = hardwareMap.get(DcMotorEx.class, "slide_motor");

    }

    @Override
    public void loop() {




    }
}
