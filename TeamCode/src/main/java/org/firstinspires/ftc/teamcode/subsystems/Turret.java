package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.EasyStack.control.EasyPIDCoefficients;
import dev.easyfamily.EasyStack.feedforward.EasyFFCoefficients;
import dev.easyfamily.EasyStack.hardware.Servo.EasyAbsoluteAnalogEncoder;
import dev.easyfamily.EasyStack.hardware.Servo.EasyServo;
import dev.easyfamily.EasyStack.subsytem.EasySubsystem;
import dev.easyfamily.EasyStack.hardware.Servo.EasyCRServo;
import dev.easyfamily.EasyStack.hardware.Servo.EasyCRServoGroup;

public class Turret implements EasySubsystem {
    private EasyCRServo Axon1;

    private EasyCRServo Axon2;

    private EasyCRServoGroup turretServos;

    private EasyAbsoluteAnalogEncoder encoderAnalogo;
    private EasyPIDCoefficients pid;

    private EasyFFCoefficients ff;
    private EasyServo teste;


    @Override
    public void init(HardwareMap hardwareMap) {
        pid = new EasyPIDCoefficients(0, 0, 0);
        ff = new EasyFFCoefficients(0, 0, 0);
        encoderAnalogo = new EasyAbsoluteAnalogEncoder(hardwareMap, "AnalogEncoder");
        Axon1 = new EasyCRServo(hardwareMap, "TurretCRServo1").setInverted(true);
        Axon2 = new EasyCRServo(hardwareMap, "TurretCRServo2");
        turretServos = new EasyCRServoGroup(Axon1, Axon2).setFeedforward(ff).setPositionalControl(encoderAnalogo, pid, 1, AngleUnit.DEGREES);
    }

    @Override
    public void loop()   {
        turretServos.setTargetAngle(90);
    }
}
