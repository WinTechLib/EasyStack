package org.firstinspires.ftc.teamcode.subst;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.easystack.hardware.Motors.EasyMotor;
import dev.easyfamily.easystack.hardware.Servo.EasyServo;
import dev.easyfamily.easystack.subsytem.EasySubsystem;
import dev.easyfamily.easystack.util.InterpLUT;

public class Shooter implements EasySubsystem {

    public Pose pose;
    private EasyMotor shooter_motor;
    private EasyServo block;

    public InterpLUT shooterVelocity;
    public InterpLUT hoodAngle;

    public double currentVelocity = 0;
    public double targetVelocity = 0;
    public double distance;



    public void createFills() {
        shooterVelocity.add(10, 800);
        shooterVelocity.createLUT();

        hoodAngle.add(10, 0.5);
        hoodAngle.createLUT();
    }

    @Override
    public void init(HardwareMap hardwareMap) {
        shooter_motor = new EasyMotor(hardwareMap, "shooter_motor");
        block = new EasyServo(hardwareMap, "block_servo");

        createFills();
    }

    @Override
    public void loop() {
        currentVelocity = shooter_motor.getVelocity();

        targetVelocity = shooterVelocity.get(2);


    }
}
