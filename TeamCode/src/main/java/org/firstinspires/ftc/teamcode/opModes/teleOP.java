package org.firstinspires.ftc.teamcode.opModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Shooter;

@TeleOp(name = "Shooter")
public class teleOP extends OpMode {

    Shooter shooter = new Shooter();
    private boolean lastA = false;

    @Override
    public void init() {
        shooter.init(hardwareMap);
    }

    @Override
    public void loop() {
        // Toggle do shooter
        if (gamepad1.a && !lastA) {
            shooter.toggle();
        }

        lastA = gamepad1.a;
        shooter.loop();
    }
}
