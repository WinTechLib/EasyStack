package dev.easyfamily.easystack.drivebase.swerve.coaxial;

import dev.easyfamily.easystack.drivebase.drive.DrivePowers;
import dev.easyfamily.easystack.drivebase.drive.RobotDrive;
import dev.easyfamily.easystack.drivebase.swerve.ChassisSpeed;
import dev.easyfamily.easystack.geometry.Vector2d;

public class SwerveDrivetrain extends RobotDrive {

    private final SwerveModule[] modules = new SwerveModule[4];

    // Distância centro-a-centro entre os módulos, esquerda-direita.
    private final double trackWidth;

    // Distância centro-a-centro entre os módulos, frente-trás.
    private final double wheelBase;

    // Velocidade máxima do robô em unidades/segundo.
    private final double maxSpeed;

    // Velocidade angular máxima em rad/s.
    private final double maxAngularSpeed;

    private ChassisSpeed targetVelocity = new ChassisSpeed();

    /**
     * Ordem dos módulos:
     *
     * 0 = Front Right
     * 1 = Front Left
     * 2 = Back Left
     * 3 = Back Right
     */
    public SwerveDrivetrain(
            double trackWidth,
            double wheelBase,
            double maxSpeed,
            SwerveModule[] modules
    ) {
        if (modules == null || modules.length != 4) {
            throw new IllegalArgumentException(
                    "SwerveDrivetrain precisa de exatamente 4 módulos"
            );
        }

        for (int i = 0; i < modules.length; i++) {
            if (modules[i] == null) {
                throw new IllegalArgumentException(
                        "SwerveModule no índice " + i + " não pode ser null"
                );
            }
        }

        if (trackWidth <= 0 || wheelBase <= 0 || maxSpeed <= 0) {
            throw new IllegalArgumentException(
                    "trackWidth, wheelBase e maxSpeed devem ser positivos"
            );
        }

        this.trackWidth = trackWidth;
        this.wheelBase = wheelBase;
        this.maxSpeed = maxSpeed;

        /*
         * A velocidade angular máxima ocorre quando o robô
         * gira em torno do próprio centro.
         *
         * maxAngularSpeed =
         * maxSpeed / distância do centro até o módulo.
         */
        this.maxAngularSpeed =
                maxSpeed
                        / Math.hypot(
                        trackWidth / 2.0,
                        wheelBase / 2.0
                );

        System.arraycopy(
                modules,
                0,
                this.modules,
                0,
                4
        );
    }

    /**
     * Construtor conveniente.
     *
     * Ordem:
     * Front Right
     * Front Left
     * Back Left
     * Back Right
     */
    public SwerveDrivetrain(
            double trackWidth,
            double wheelBase,
            double maxSpeed,
            SwerveModule frontRight,
            SwerveModule frontLeft,
            SwerveModule backLeft,
            SwerveModule backRight
    ) {
        this(
                trackWidth,
                wheelBase,
                maxSpeed,
                new SwerveModule[]{
                        frontRight,
                        frontLeft,
                        backLeft,
                        backRight
                }
        );
    }

    @Override
    public SwerveDrivetrain setMaxOutput(double maxOutput) {
        super.setMaxOutput(maxOutput);
        return this;
    }

    /**
     * Define a velocidade desejada do robô em robot-centric.
     *
     * vx = velocidade para frente
     * vy = velocidade para a esquerda
     * omega = velocidade angular em rad/s
     */
    public void setTargetVelocity(ChassisSpeed targetVelocity) {

        if (targetVelocity == null) {
            throw new IllegalArgumentException(
                    "targetVelocity não pode ser null"
            );
        }

        double maxScaleOverOutput = 1.0;

        double maxAllowedLinearSpeed =
                maxSpeed * maxOutput;

        double maxAllowedAngularSpeed =
                maxAngularSpeed * maxOutput;

        if (maxAllowedLinearSpeed > 0) {

            maxScaleOverOutput = Math.max(
                    maxScaleOverOutput,
                    Math.abs(targetVelocity.getVx())
                            / maxAllowedLinearSpeed
            );

            maxScaleOverOutput = Math.max(
                    maxScaleOverOutput,
                    Math.abs(targetVelocity.getVy())
                            / maxAllowedLinearSpeed
            );
        }

        if (maxAllowedAngularSpeed > 0) {

            maxScaleOverOutput = Math.max(
                    maxScaleOverOutput,
                    Math.abs(targetVelocity.getOmega())
                            / maxAllowedAngularSpeed
            );
        }

        this.targetVelocity = new ChassisSpeed(
                targetVelocity.getVx()
                        / maxScaleOverOutput,

                targetVelocity.getVy()
                        / maxScaleOverOutput,

                targetVelocity.getOmega()
                        / maxScaleOverOutput
        );
    }

    /**
     * Atualiza todos os módulos para seguir
     * a velocidade alvo atual.
     */
    public Vector2d[] update() {

        Vector2d[] moduleVelocities =
                new Vector2d[modules.length];

        /*
         * Calcula a velocidade desejada de cada roda.
         */
        for (int i = 0; i < modules.length; i++) {

            moduleVelocities[i] =
                    modules[i]
                            .calculateVectorRobotCentric(
                                    targetVelocity
                            );
        }

        /*
         * Encontra a maior velocidade entre os módulos.
         */
        double maxModuleSpeed = 0.0;

        for (Vector2d velocity : moduleVelocities) {
            maxModuleSpeed =
                    Math.max(
                            maxModuleSpeed,
                            velocity.norm()
                    );
        }

        /*
         * Se algum módulo ultrapassar maxSpeed,
         * reduzimos todos proporcionalmente.
         */
        if (maxModuleSpeed > maxSpeed) {

            double scale =
                    maxSpeed / maxModuleSpeed;

            for (int i = 0;
                 i < moduleVelocities.length;
                 i++) {

                moduleVelocities[i] =
                        moduleVelocities[i]
                                .times(scale);
            }
        }

        /*
         * Aplica as velocidades aos módulos.
         */
        for (int i = 0; i < modules.length; i++) {

            modules[i]
                    .updateModuleWithVelocity(
                            moduleVelocities[i]
                    );
        }

        return moduleVelocities;
    }

    /**
     * Define e aplica imediatamente uma velocidade
     * robot-centric.
     */
    public void updateWithTargetVelocity(
            ChassisSpeed targetVelocity
    ) {
        setTargetVelocity(targetVelocity);
        update();
    }

    /**
     * Coloca as rodas em X para resistir a empurrões.
     */
    public void updateWithXLock() {

        for (int i = 0; i < modules.length; i++) {

            double angle =
                    -Math.PI / 4.0
                            + Math.PI / 2.0 * i;

            modules[i].updateModuleWithVelocity(
                    Vector2d.fromPolar(
                            0.0001,
                            angle
                    )
            );
        }
    }

    @Override
    public void drive(DrivePowers powers) {

        updateWithTargetVelocity(
                new ChassisSpeed(
                        powers.getForward(),
                        powers.getStrafe(),
                        powers.getTurn()
                )
        );
    }

    @Override
    public void stop() {

        for (SwerveModule module : modules) {
            module.stop();
        }
    }

    public SwerveModule[] getModules() {
        return modules.clone();
    }

    public SwerveModule getModule(int index) {
        return modules[index];
    }

    public ChassisSpeed getTargetVelocity() {
        return targetVelocity;
    }

    public double getTrackWidth() {
        return trackWidth;
    }

    public double getWheelBase() {
        return wheelBase;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getMaxAngularSpeed() {
        return maxAngularSpeed;
    }
}