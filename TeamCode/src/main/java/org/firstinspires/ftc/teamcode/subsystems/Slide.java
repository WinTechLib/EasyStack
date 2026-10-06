package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.easystack.control.EasyPIDController;
import dev.easyfamily.easystack.control.EasyPIDFController;
import dev.easyfamily.easystack.control.wpiLibControllers.EasyElevatorFeedforward;
import dev.easyfamily.easystack.hardware.Motors.EasyMotor;
import dev.easyfamily.easystack.subsytem.EasySubsystem;

public class Slide implements EasySubsystem {

    EasyMotor elevator;
    EasyElevatorFeedforward ff;
    EasyPIDController pid;

    @Override
    public void init(HardwareMap hardwareMap) {
        ff = new EasyElevatorFeedforward(0, 0, 0, 0);
        pid = new EasyPIDController(0, 0, 0);
        elevator = new EasyMotor(hardwareMap, "elevator")
                .reversed();
    }

    @Override
    public void loop() {




    }
}
