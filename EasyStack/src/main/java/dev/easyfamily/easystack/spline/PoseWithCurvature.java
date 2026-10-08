package dev.easyfamily.easystack.spline;

import dev.easyfamily.easystack.geometry.Pose2d;
public class    PoseWithCurvature {
    public Pose2d pose;
    public double curvature;

    public PoseWithCurvature(Pose2d pose, double curvature) {
        this.pose = pose;
        this.curvature = curvature;
    }

    @Override
    public String toString() {
        return "PoseWithCurvature(" + pose + ", k=" + curvature + ")";
    }
}
