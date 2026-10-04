package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.easystack.subsytem.EasySubsytem;

public class Slide implements EasySubsytem {

    DcMotorEx slide_motor;


    @Override
    public void init(HardwareMap hardwareMap) {


        slide_motor = hardwareMap.get(DcMotorEx.class, "slide_motor");

    }

    @Override
    public void loop() {




    }
}
