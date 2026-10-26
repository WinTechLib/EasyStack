package org.firstinspires.ftc.teamcode.subsystems;
import static org.firstinspires.ftc.teamcode.subsystems.constants.kd;
import static org.firstinspires.ftc.teamcode.subsystems.constants.kf;
import static org.firstinspires.ftc.teamcode.subsystems.constants.ki;
import static org.firstinspires.ftc.teamcode.subsystems.constants.kp;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

import dev.easyfamily.EasyStack.Camera.EasyCamera;
import dev.easyfamily.EasyStack.gamepad.EasyGamepad;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotorType;

@TeleOp(name = "teste")
public class motorTest extends LinearOpMode {
    EasyMotor motor;
    DcMotor motor1;
    EasyCamera camera;
    @Override
    public void runOpMode() throws InterruptedException {
        camera = new EasyCamera(hardwareMap, "A");
        waitForStart();
        while(opModeIsActive()){
            telemetry.update();
        }
    }
}
