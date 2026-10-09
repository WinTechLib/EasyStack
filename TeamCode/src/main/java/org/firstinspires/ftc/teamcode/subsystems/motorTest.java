package org.firstinspires.ftc.teamcode.subsystems;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import dev.easyfamily.easystack.control.EasyPIDFController;
import dev.easyfamily.easystack.hardware.Motors.EasyMotor;
import dev.easyfamily.easystack.hardware.Motors.EasyMotorGroup;
import dev.easyfamily.easystack.hardware.Motors.EasyMotorType;
import dev.easyfamily.easystack.hardware.Servo.EasyServo;
@TeleOp(name = "teste")

public class motorTest extends LinearOpMode {
    EasyMotor motor, motor1;
    EasyMotorGroup motores;
    @Override
    public void runOpMode() throws InterruptedException {
        motor = new EasyMotor(hardwareMap,"motor", EasyMotorType.GOBILDA_312)
                .stopAndResetEncoder();
        motor1 = new EasyMotor(hardwareMap, "motor1", EasyMotorType.GOBILDA_312);
        motores = new EasyMotorGroup(motor, motor1).crossCoupled(.5);
        waitForStart();
        while(opModeIsActive()){
            motor.setRPM(3500);
            telemetry.addData("current Pos", motor.getCurrentPosition());
            telemetry.addData("rpm", motor.getRPM());
            telemetry.addData("rotations", motor.getRotations());

            telemetry.update();
        }
    }
}
