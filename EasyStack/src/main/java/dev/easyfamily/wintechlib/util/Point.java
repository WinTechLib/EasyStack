package dev.easyfamily.wintechlib.util;

// Classe usada para definir pontos 2d na arena sem depender do
// vector2d do pedro
public final class Point {

    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }
}
//OK