package org.firstinspires.ftc.teamcode.opModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import dev.easyfamily.EasyStack.drivebase.MecanumDrive;
import dev.easyfamily.EasyStack.gamepad.EasyGamepad;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;

@TeleOp(name = "Easy Mecanum Drive")
public class EasyMecanumDrive extends OpMode {

    private EasyGamepad driver;
    private MecanumDrive drive;

    @Override
    public void init() {

        EasyMotor frontLeft = new EasyMotor(hardwareMap, "frontLeft");

        EasyMotor frontRight = new EasyMotor(hardwareMap, "frontRight");

        EasyMotor backLeft = new EasyMotor(hardwareMap, "backLeft");

        EasyMotor backRight = new EasyMotor(hardwareMap, "backRight");

        drive = new MecanumDrive(frontLeft, frontRight, backLeft, backRight);

        EasyGamepad driver = new EasyGamepad(gamepad1);
    }

    @Override
    public void loop() {
        driver.update();

        // Robot code
    }

}
