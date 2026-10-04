package dev.easyfamily.easystack.Commands;

/** Agenda outros comandos no scheduler e termina na hora (não bloqueia nem requer subsistemas). */
public class ScheduleCommand extends EasyCommandBase {
    private final EasyCommand[] toSchedule;

    public ScheduleCommand(EasyCommand... toSchedule) {
        this.toSchedule = toSchedule;
    }

    @Override
    public void initialize() {
        for (EasyCommand c : toSchedule) c.schedule();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
