package dev.lagwave.wavestack.hardware;

import dev.lagwave.wavestack.controllable.EasyControllable;

public class EasyMotorGroup implements EasyControllable {

    private final EasyControllable leader;
    private final EasyControllable[] followers;

    public EasyMotorGroup(
            EasyControllable leader,
            EasyControllable... followers
    ) {
        this.leader = leader;
        this.followers = followers;
    }

    @Override
    public void setPower(double power) {
        leader.setPower(power);

        for (EasyControllable follower : followers) {
            follower.setPower(power);
        }
    }

    @Override
    public double getPower() {
        return leader.getPower();
    }

    @Override
    public double getPosition() {
        return leader.getPosition();
    }

    @Override
    public double getVelocity() {
        return leader.getVelocity();
    }

    public EasyControllable getLeader() {
        return leader;
    }
}