package org.firstinspires.ftc.teamcode.subsystems;
import static org.firstinspires.ftc.teamcode.subsystems.constants.kd;
import static org.firstinspires.ftc.teamcode.subsystems.constants.kf;
import static org.firstinspires.ftc.teamcode.subsystems.constants.ki;
import static org.firstinspires.ftc.teamcode.subsystems.constants.kp;
import static org.firstinspires.ftc.teamcode.subsystems.constants.target;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import dev.easyfamily.wintechlib.gamepad.EasyGamepad;
import dev.easyfamily.wintechlib.hardware.Motors.EasyMotor;
import dev.easyfamily.wintechlib.hardware.Motors.EasyMotorType;

@TeleOp(name = "teste")
public class motorTest extends LinearOpMode {
    EasyMotor motor;
    EasyGamepad gamepad;
    @Override
    public void runOpMode() throws InterruptedException {
        gamepad = new EasyGamepad(gamepad1).setADebounce(500);
        motor = new EasyMotor(hardwareMap,"motor", EasyMotorType.REV_HD_HEX_40).stopAndResetEncoder().setPositionPIDF(kp, ki, kd, kf);
        waitForStart();
        while(opModeIsActive()){
            if(gamepad.a()){
                motor.moveToDegrees(90);
            }
            telemetry.update();
        }
    }
}
