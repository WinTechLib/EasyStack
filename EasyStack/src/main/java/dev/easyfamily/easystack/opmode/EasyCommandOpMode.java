package dev.easyfamily.easystack.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import dev.easyfamily.easystack.Commands.EasyCommandScheduler;

public abstract class   EasyCommandOpMode extends LinearOpMode {
    protected final EasyCommandScheduler scheduler = EasyCommandScheduler.getInstance();
    public abstract void initialize();
    public void onStart() {}

    @Override
    public void runOpMode() {
        scheduler.reset();
        initialize();
        scheduler.init(hardwareMap);

        waitForStart();
        if (isStopRequested()) {
            scheduler.reset();
            return;
        }
        onStart();

        while (opModeIsActive() && !isStopRequested()) {
            scheduler.run();
            telemetry.update();
        }
        scheduler.reset();
    }
}
