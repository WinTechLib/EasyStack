package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.feedforward.FFCoefficients;
import dev.easyfamily.easystack.hardware.Servo.EasyAbsoluteAnalogEncoder;
import dev.easyfamily.easystack.subsytem.EasySubsystem;
import dev.easyfamily.easystack.hardware.Servo.EasyCRServo;
import dev.easyfamily.easystack.hardware.Servo.EasyCRServoGroup;

public class Turret implements EasySubsystem {
    private EasyCRServo Axon1;

    private EasyCRServo Axon2;

    private EasyCRServoGroup turretServos;

    private EasyAbsoluteAnalogEncoder encoderAnalogo;
    private PIDCoefficients pid;

    private FFCoefficients ff;


    @Override
    public void init(HardwareMap hardwareMap) {
        pid = new PIDCoefficients(0, 0, 0);
        ff = new FFCoefficients(0, 0, 0);
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
