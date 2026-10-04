package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.lagwave.wavestack.control.PIDCoefficients;
import dev.lagwave.wavestack.feedforward.FFCoefficients;
import dev.lagwave.wavestack.hardware.Servo.EzAbsoluteAnalogEncoder;
import dev.lagwave.wavestack.hardware.Servo.EzCRServo;
import dev.lagwave.wavestack.hardware.Servo.EzCRServoGroup;
import dev.lagwave.wavestack.subsytem.WaveSubsytem;

public class Turret implements WaveSubsytem {
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
