package org.firstinspires.ftc.teamcode.subsystems;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.gamepad1;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.EasyStack.control.EasyPIDController;
import dev.easyfamily.EasyStack.control.EasyPIDCoefficients;
import dev.easyfamily.EasyStack.feedforward.EasyFFCoefficients;
import dev.easyfamily.EasyStack.feedforward.EasyFFController;
import dev.easyfamily.EasyStack.gamepad.EasyGamepad;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotorGroup;
import dev.easyfamily.EasyStack.hardware.Servo.EasyServo;
import dev.easyfamily.EasyStack.hardware.Servo.EasyServoGroup;
import dev.easyfamily.EasyStack.subsytem.EasySubsystem;
import dev.easyfamily.EasyStack.util.InterpLUT;

public class Shooter implements EasySubsystem {

    private EasyMotor shooter_motor;
    private EasyMotor shooter_motor_left;
    private EasyMotorGroup shooterMotors;
    private EasyServo BlockerServo;
    private EasyServo AnguladorLeft, AnguladorRight;
    private EasyServoGroup Anguladores;
    public static double targetVelocity;

    public boolean on = false;
    EasyPIDCoefficients pidCoefficients = new EasyPIDCoefficients(0, 0, 0);
    EasyFFCoefficients ffCoefficients = new EasyFFCoefficients(0, 0, 0);

    EasyPIDController pidController = new EasyPIDController(pidCoefficients);
    EasyFFController ffController = new EasyFFController(ffCoefficients);
    InterpLUT shooterVelocity = new InterpLUT();
    InterpLUT PositionAng = new InterpLUT();
    EasyGamepad gamepad;
    public void toggle() {
        on = !on;
    }

    @Override
    public void init(HardwareMap hardwareMap) {
        gamepad = new EasyGamepad(gamepad1).setADebounce(500);
        BlockerServo = new EasyServo(hardwareMap, "BlockerServo")
                .setLimits(0.3, 0.8)
                .addState("Closed", 1) //When .setState("Closed"), the position will go to 1
                .addState("Open", 0);  //When .setState("Open"), the position will go to 0

        AnguladorLeft = new EasyServo(hardwareMap, "AnguladorLeft", 360) // We create one EzServo
                .setInverted(true);
        AnguladorRight = new EasyServo(hardwareMap, "AnguladorRight", 360); // And another

        Anguladores = new EasyServoGroup(AnguladorLeft, AnguladorRight) //And now we put on the parameters the two EzServos to create a group.
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
        double distance = 0;
        double targetAngle = PositionAng.get(distance);
        Anguladores.setAngle(targetAngle, AngleUnit.DEGREES);

        if (gamepad.a()) {
            BlockerServo.setState("Open"); //0
        } else {
            BlockerServo.setState("Closed"); //1
        }

    }

}
