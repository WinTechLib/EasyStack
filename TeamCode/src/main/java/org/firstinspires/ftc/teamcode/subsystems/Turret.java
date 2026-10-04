package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.feedforward.FFCoefficients;
import dev.easyfamily.easystack.hardware.Servo.EzAbsoluteAnalogEncoder;
import dev.easyfamily.easystack.subsytem.EasySubsytem;
import dev.easyfamily.easystack.hardware.Servo.EzCRServo;
import dev.easyfamily.easystack.hardware.Servo.EzCRServoGroup;

public class Turret implements EasySubsytem {
    private EzCRServo Axon1;
    private EzCRServo Axon2;
    private EzCRServoGroup turretServos;
    private EzAbsoluteAnalogEncoder encoderAnalogo;
    private PIDCoefficients pid;
    private FFCoefficients ff;

    @Override
    public void init(HardwareMap hardwareMap) {
        pid = new PIDCoefficients(0, 0, 0);
        ff = new FFCoefficients(0, 0, 0);
        encoderAnalogo = new EzAbsoluteAnalogEncoder(hardwareMap, "AnalogEncoder");
        Axon1 = new EzCRServo(hardwareMap, "TurretCRServo1").setInverted(true);
        Axon2 = new EzCRServo(hardwareMap, "TurretCRSer vo2");
        turretServos = new EzCRServoGroup(Axon1, Axon2).setFeedforward(ff).setPositionalControl(encoderAnalogo, pid, 1, AngleUnit.DEGREES);
    }

    @Override
    public void loop() {
        turretServos.setTargetAngle(90);
    }
}
