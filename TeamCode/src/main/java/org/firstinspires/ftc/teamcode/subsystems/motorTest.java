package org.firstinspires.ftc.teamcode.subsystems;
import static org.firstinspires.ftc.teamcode.subsystems.constants.kd;
import static org.firstinspires.ftc.teamcode.subsystems.constants.kf;
import static org.firstinspires.ftc.teamcode.subsystems.constants.ki;
import static org.firstinspires.ftc.teamcode.subsystems.constants.kp;
import static org.firstinspires.ftc.teamcode.subsystems.constants.target;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import dev.easyfamily.easystack.control.EasyPIDFController;
import dev.easyfamily.easystack.gamepad.EasyGamepad;
import dev.easyfamily.easystack.hardware.Motors.EasyMotor;
import dev.easyfamily.easystack.hardware.Motors.EasyMotorGroup;
import dev.easyfamily.easystack.hardware.Motors.EasyMotorType;
import dev.easyfamily.easystack.hardware.Servo.EasyServo;
@TeleOp(name = "teste")
public class motorTest extends LinearOpMode {
    EasyMotor motor;
    EasyGamepad gamepad;

    @Override
    public void runOpMode() throws InterruptedException {
        gamepad = new EasyGamepad(gamepad1);
        motor = new EasyMotor(hardwareMap,"motor", EasyMotorType.REV_HD_NAKED).stopAndResetEncoder().setPositionPIDF(kp, ki, kd, kf);
        waitForStart();
        while(opModeIsActive()){
            if(gamepad.aToggle()){motor.moveToDegrees(720);} else{motor.moveToDegrees(0);}

            motor.setPositionPIDF(kp, ki, kd, kf);
            telemetry.addData("current Pos", motor.getCurrentPosition());
            telemetry.addData("rpm", motor.getRPM());
            telemetry.addData("targetrpm",target);
            telemetry.addData("rotations", motor.getRotations());
            telemetry.addData("kp", kp);
            telemetry.addData("ki", ki);
            telemetry.addData("kd", kd);

            telemetry.update();
        }
    }
}
