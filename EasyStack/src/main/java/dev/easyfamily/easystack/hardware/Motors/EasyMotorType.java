package dev.easyfamily.easystack.hardware.Motors;

public enum EasyMotorType {

    GOBILDA_6000(28.0, 6000),
    GOBILDA_1620(103.8, 1620),
    GOBILDA_1150(145.1, 1150),
    GOBILDA_435(383.6, 435),
    GOBILDA_312(537.7, 312),
    GOBILDA_223(753.2, 223),
    GOBILDA_117(1425.1, 117),
    GOBILDA_84(1993.6, 84),
    GOBILDA_60(2786.2, 60),
    GOBILDA_43(3895.9, 43),
    GOBILDA_30(5281.1, 30),

    REV_CORE_HEX(288.0, 125),
    REV_HD_HEX_20(560.0, 300),
    REV_HD_HEX_40(1120.0, 150),

    CONFIG(0.0, 0);

    private final double cpr;
    private final double maxRPM;

    EasyMotorType(double cpr, double maxRPM) {
        this.cpr = cpr;
        this.maxRPM = maxRPM;
    }

    public double getCPR() {
        return cpr;
    }

    public double getMaxRPM() {
        return maxRPM;
    }

    public boolean isConfig() {
        return this == CONFIG;
    }
}