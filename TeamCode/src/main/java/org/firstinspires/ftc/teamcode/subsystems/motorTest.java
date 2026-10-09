package org.firstinspires.ftc.teamcode.subsystems;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import dev.easyfamily.easystack.control.EasyPIDFController;
import dev.easyfamily.easystack.hardware.Motors.EasyMotor;
import dev.easyfamily.easystack.hardware.Motors.EasyMotorType;
import dev.easyfamily.easystack.hardware.Servo.EasyServo;
@TeleOp(name = "teste")

public class motorTest extends LinearOpMode {
    EasyMotor motor;
    EasyPIDFController pidfController;
    @Override
    public void runOpMode() throws InterruptedException {
       double kp = 0.009;
        double ki = 0;
       double kd = 0;
       double targetRpm = 150;

        pidfController = new EasyPIDFController(kp, kd, ki, 0);
        motor = new EasyMotor(hardwareMap,"motor", EasyMotorType.GOBILDA_312);
        waitForStart();
        while(opModeIsActive()){
            double power =  pidfController.calculate(motor.getRPM(),targetRpm);
            motor.setPower(power);
            telemetry.addData("current Pos", motor.getCurrentPosition());
            telemetry.addData("rpm", motor.getRPM());
            telemetry.addData("error", pidfController.getError());
            telemetry.addData("rotations", motor.getRotations());

            telemetry.update();
        }
    }
}
