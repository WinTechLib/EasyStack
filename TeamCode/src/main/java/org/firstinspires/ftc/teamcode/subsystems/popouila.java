package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.opModes.EasyMecanumDrive;

import dev.easyfamily.EasyStack.control.EasyPIDCoefficients;
import dev.easyfamily.EasyStack.control.EasyPIDFCoefficients;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotorGroup;
import dev.easyfamily.EasyStack.hardware.Sensors.EasyRevColorSensor;
import dev.easyfamily.EasyStack.subsytem.EasySubsystemBase;
import dev.easyfamily.EasyStack.wpiLibControllers.EasyBangBangController;

public class popouila extends EasySubsystemBase {
    EasyMecanumDrive mecanumDrive;
    EasyRevColorSensor sensor;
    EasyMotor shooterRight, shooterLeft;
    EasyMotorGroup motorGroup;
    EasyPIDFCoefficients pidfCoefficients;


    @Override
    public void init(HardwareMap hardwareMap) {
    shooterRight = new EasyMotor(hardwareMap, "shooterRight");
        shooterLeft = new EasyMotor(hardwareMap, "shooterLeft");
        pidfCoefficients = new EasyPIDFCoefficients(0.9,0.8,0.5,0.9);
    motorGroup = new EasyMotorGroup(shooterRight, shooterLeft).setVelocityPIDF(pidfCoefficients).crossCoupled();
    }
    public void setRpm(double rpm){
        motorGroup.setRPM(rpm);
    }
}
