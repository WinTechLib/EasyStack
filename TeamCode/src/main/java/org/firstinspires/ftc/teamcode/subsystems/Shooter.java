package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

import dev.lagwave.wavestack.control.PIDCoefficients;
import dev.lagwave.wavestack.control.PIDController;
import dev.lagwave.wavestack.feedforward.FFCoefficients;
import dev.lagwave.wavestack.feedforward.FFController;
import dev.lagwave.wavestack.hardware.EasyMotor;
import dev.lagwave.wavestack.hardware.EasyMotorGroup;
import dev.lagwave.wavestack.subsytem.WaveSubsytem;
import dev.lagwave.wavestack.util.InterpLUT;

public class Shooter implements WaveSubsytem {

    private EasyMotor shooter_motor;
    private EasyMotor shooter_motor_left;
    private EasyMotorGroup shooterMotors;
    public static double targetVelocity;

    public boolean on = false;

    // Configuração do PID e FeedForward
    PIDCoefficients pidCoefficients = new PIDCoefficients(0, 0, 0);
    FFCoefficients ffCoefficients = new FFCoefficients(0,0,0);

    PIDController pidController = new PIDController(pidCoefficients);
    FFController ffController = new FFController(ffCoefficients);

    // Interpolação Linear
    InterpLUT shooterVelocity = new InterpLUT();

    public void toggle() {
        on = !on;
    }

    @Override
    public void init(HardwareMap hardwareMap) {
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

        double pid = pidController.calculate(targetVelocity, currentTicks);
        double ff = ffController.calculate(targetVelocity);

        if (on) {
            shooterMotors.setPower(pid + ff);
        } else {
            shooter_motor.setPower(0);
        }
    }

}
