package dev.easyfamily.easystack.Commands;

/**
 * Roda o comando pelo scheduler (fora do grupo) e espera ele terminar.
 * Não herda os requisitos: o grupo que contém o proxy não fica bloqueado pelos subsistemas dele.
 */
public class ProxyCommand extends EasyCommandBase {
    private final EasyCommand target;

    public ProxyCommand(EasyCommand target) {
        this.target = target;
    }

    @Override
    public void initialize() {
        target.schedule();
    }

    @Override
    public boolean isFinished() {
        return !target.isScheduled();
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) target.cancel();
    }
}
