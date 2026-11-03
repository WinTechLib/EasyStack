package dev.easyfamily.EasyStack.controllable;

/**
 * Feedforward de posição: calcula a potência de feedforward a partir do estado
 * desejado do mecanismo. Todas as grandezas estão em unidades de encoder (ticks),
 * já no referencial lógico do {@code EasyMotor} (depois de inversões).
 *
 * <p>Como é uma interface funcional, pode ser passado como lambda:
 * <pre>
 * motor.setPositionFeedforward((pos, vel, acc) -> kG + kV * vel);
 * </pre>
 */
@FunctionalInterface
public interface EasyPositionFeedforward {

    /**
     * @param position     posição desejada, em ticks
     * @param velocity     velocidade desejada, em ticks/s (0 quando não há perfil de movimento)
     * @param acceleration aceleração desejada, em ticks/s² (0 quando não há perfil de movimento)
     * @return potência de feedforward (mesma escala da saída do controlador)
     */
    double calculate(double position, double velocity, double acceleration);
}