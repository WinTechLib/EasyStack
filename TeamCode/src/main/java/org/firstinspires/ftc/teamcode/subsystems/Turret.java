package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.wintechlib.control.PIDCoefficients;
import dev.easyfamily.wintechlib.feedforward.FFCoefficients;
import dev.easyfamily.wintechlib.hardware.Servo.EasyAbsoluteAnalogEncoder;
import dev.easyfamily.wintechlib.hardware.Servo.EasyServo;
import dev.easyfamily.wintechlib.subsytem.EasySubsystem;
import dev.easyfamily.wintechlib.hardware.Servo.EasyCRServo;
import dev.easyfamily.wintechlib.hardware.Servo.EasyCRServoGroup;

public class Turret implements EasySubsystem {
    private EasyCRServo Axon1;

    private EasyCRServo Axon2;

    private EasyCRServoGroup turretServos;

    private EasyAbsoluteAnalogEncoder encoderAnalogo;
    private PIDCoefficients pid;

    private FFCoefficients ff;
    private EasyServo teste;


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
