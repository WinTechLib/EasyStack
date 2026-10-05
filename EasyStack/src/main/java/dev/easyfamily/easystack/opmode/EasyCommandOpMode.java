package dev.easyfamily.easystack.opmode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import dev.easyfamily.easystack.Commands.EasyCommandScheduler;

/**
 * OpMode base: reseta o scheduler, chama initialize() (crie subsistemas, triggers e default commands),
 * faz init(hardwareMap) em todos, espera o start e roda o scheduler até parar.
 */
public abstract class   EasyCommandOpMode extends LinearOpMode {
    protected final EasyCommandScheduler scheduler = EasyCommandScheduler.getInstance();

    /** Crie subsistemas, bindings e comandos aqui. */
    public abstract void initialize();

    /** Opcional: roda uma vez logo após o start. */
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
