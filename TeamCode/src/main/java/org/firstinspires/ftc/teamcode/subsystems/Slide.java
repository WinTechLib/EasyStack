package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.EasyStack.control.EasyPIDController;
import dev.easyfamily.EasyStack.control.wpiLibControllers.EasyElevatorFeedforward;
import dev.easyfamily.EasyStack.drivebase.drive.Drivetrain;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.subsytem.EasySubsystem;

public class Slide implements EasySubsystem {

    EasyMotor elevator;
    EasyElevatorFeedforward ff;
    EasyPIDController pid;
    Drivetrain drivetrain;
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
