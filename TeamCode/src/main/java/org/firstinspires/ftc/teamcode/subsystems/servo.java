package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import dev.easyfamily.easystack.hardware.Motors.EasyMotor;
import dev.easyfamily.easystack.hardware.Servo.EasyServo;

public class servo extends LinearOpMode {
    EasyMotor motor;
    @Override
    public void runOpMode() throws InterruptedException {
        motor = new EasyMotor(hardwareMap,"motor");
        waitForStart();
        while(opModeIsActive()){

            telemetry.addData("current Pos", motor.getCurrentPosition());
            telemetry.addData("rpm", motor.getRPM());//chupame el pene joao
        }


    }
}
