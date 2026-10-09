package dev.easyfamily.easystack.hardware.Motors;

import java.util.Objects;

import dev.easyfamily.easystack.controllable.EasyControllable;

public class EasyMotorGroup implements EasyControllable {

    private final EasyControllable leader;
    private final EasyControllable[] followers;

    public EasyMotorGroup(
            EasyControllable leader,
            EasyControllable... followers
    ) {
        this.leader = Objects.requireNonNull(leader, "The group leader cant be null");
        this.followers = followers == null ? new EasyControllable[0] : followers;
    }

    @Override
    public void setPower(double power) {
        leader.setPower(power);

        for (EasyControllable follower : followers) {
            follower.setPower(power);
        }
    }

    public EasyMotorGroup stop() {
        setPower(0);
        return this;
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

    public EasyControllable[] getFollowers() {
        return followers.clone();
    }
}