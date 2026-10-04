package org.firstinspires.ftc.teamcode.opModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Shooter;

import dev.easyfamily.easystack.gamepad.EasyGamepad;

@TeleOp(name = "Shooter")
public class teleOP extends OpMode {

    Shooter shooter = new Shooter();
    EasyGamepad gamepad;

    @Override
    public void init() {
        shooter.init(hardwareMap);
        gamepad = new EasyGamepad(gamepad1)
                .setADebounce(500);
    }

    @Override
    public void loop() {
        gamepad.update();
        shooter.loop();

        // Toggle do shooter
        if (gamepad.aToggle()) {
            shooter.toggle();
        }

    }
}
