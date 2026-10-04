package dev.easyfamily.easystack.Commands;

import java.util.function.Supplier;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/** Mostra um valor na telemetry a cada loop (roda até ser cancelado). */
public class TelemetryCommand extends EasyCommandBase {
    private final Telemetry telemetry;
    private final String caption;
    private final Supplier<?> value;
    private final boolean update;

    public TelemetryCommand(Telemetry telemetry, String caption, Supplier<?> value, boolean update) {
        this.telemetry = telemetry;
        this.caption = caption;
        this.value = value;
        this.update = update;
    }

    public TelemetryCommand(Telemetry telemetry, String caption, Supplier<?> value) {
        this(telemetry, caption, value, false);
    }

    @Override
    public void execute() {
        telemetry.addData(caption, value.get());
        if (update) telemetry.update();
    }
}
