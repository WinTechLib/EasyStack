package org.firstinspires.ftc.teamcode.subsystems;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.gamepad1;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.wintechlib.control.EasyPIDController;
import dev.easyfamily.wintechlib.control.wpiLibControllers.EasyElevatorFeedforward;
import dev.easyfamily.wintechlib.drivebase.drive.Drivetrain;
import dev.easyfamily.wintechlib.hardware.Motors.EasyMotor;
import dev.easyfamily.wintechlib.hardware.Sensors.EasyRevIMU;
import dev.easyfamily.wintechlib.subsytem.EasySubsystem;

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
