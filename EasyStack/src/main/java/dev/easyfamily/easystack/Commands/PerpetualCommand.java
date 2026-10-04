package dev.easyfamily.easystack.Commands;

/** Ignora o isFinished() do comando interno: roda até ser cancelado. */
public class PerpetualCommand extends WrapperCommand {
    public PerpetualCommand(EasyCommand inner) {
        super(inner);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
