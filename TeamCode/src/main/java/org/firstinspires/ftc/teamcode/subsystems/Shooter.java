package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.lagwave.wavestack.control.PIDCoefficients;
import dev.lagwave.wavestack.control.PIDController;
import dev.lagwave.wavestack.feedforward.FFCoefficients;
import dev.lagwave.wavestack.feedforward.FFController;
import dev.lagwave.wavestack.hardware.EasyMotor;
import dev.lagwave.wavestack.hardware.EasyMotorGroup;
import dev.lagwave.wavestack.hardware.Servo.EzServo;
import dev.lagwave.wavestack.hardware.Servo.EzServoGroup;
import dev.lagwave.wavestack.subsytem.WaveSubsytem;
import dev.lagwave.wavestack.util.InterpLUT;

public class Shooter implements WaveSubsytem {

    private EasyMotor shooter_motor;
    private EasyMotor shooter_motor_left;
    private EasyMotorGroup shooterMotors;
    private EzServo BlockerServo;
    private EzServo AnguladorLeft, AnguladorRight;
    private EzServoGroup Anguladores;
    public static double targetVelocity;

    public boolean on = false;

    // Configuração do PID e FeedForward
    PIDCoefficients pidCoefficients = new PIDCoefficients(0, 0, 0);
    FFCoefficients ffCoefficients = new FFCoefficients(0,0,0);

    PIDController pidController = new PIDController(pidCoefficients);
    FFController ffController = new FFController(ffCoefficients);

    // Interpolação Linear
    InterpLUT shooterVelocity = new InterpLUT();
    InterpLUT PositionAng = new InterpLUT();

    public void toggle() {
        on = !on;
    }

    @Override
    public void init(HardwareMap hardwareMap) {
        BlockerServo = new EzServo(hardwareMap, "BlockerServo")
                .setLimits(0.3, 0.8)
                .addState("Closed", 1)

                .addState("Open", 0);
        AnguladorLeft = new EzServo(hardwareMap, "AnguladorLeft")
                .setInverted(true);
        AnguladorLeft = new EzServo(hardwareMap, "AnguladorLeft");

        Anguladores = new EzServoGroup(AnguladorLeft, AnguladorRight)
                .setCachingTolerance(0.05)
                .setLimits(0, 0.93);

        PositionAng.add(20, 0);
        PositionAng.add(40, 30);
        PositionAng.add(60, 40);
        PositionAng.add(80, 50);
        PositionAng.createLUT();




        shooter_motor = new EasyMotor(hardwareMap, "shooter_motor")
                .brakeMode()
                .reversed();

        shooter_motor_left = new EasyMotor(hardwareMap, "shooter_motor")
                .brakeMode()
                .reversed();

        shooterMotors = new EasyMotorGroup(shooter_motor, shooter_motor_left);

        shooterVelocity.add(20, 1000);
        shooterVelocity.add(40, 1200);
        shooterVelocity.add(60, 1300);
        shooterVelocity.add(80, 1400);
        shooterVelocity.createLUT();
    }

    @Override
    public void loop() {
        double distance = 0; // Váriavel da distância aqui

        double currentTicks = shooter_motor.getVelocity();
        targetVelocity = shooterVelocity.get(distance);
        double targetAngle = PositionAng.get(distance);
        double pid = pidController.calculate(targetVelocity, currentTicks);
        double ff = ffController.calculate(targetVelocity);
        Anguladores.setAngle(targetAngle, AngleUnit.DEGREES);
        if (on) {
            BlockerServo.setState("Open");
            shooterMotors.setPower(pid + ff);
        } else {
             BlockerServo.setState("Closed");
            shooter_motor.setPower(0);
        }
    }

}
