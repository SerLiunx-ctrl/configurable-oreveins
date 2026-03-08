package com.serliunx.configurableoreveins.config.vein;

/**
 * 矿脉形状配置。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public class VeinShapeConfig {

    private String type = "ELLIPSOID";
    private int radiusX = 4;
    private int radiusY = 4;
    private int radiusZ = 4;
    private double sizeMultiplierMin = 1.0D;
    private double sizeMultiplierMax = 1.0D;
    private double irregularity = 0.0D;
    private int steps = 12;
    private double stepLength = 1.5D;

    public String getType() {
        return type == null ? "ELLIPSOID" : type;
    }

    public int getRadiusX() {
        return Math.max(1, radiusX);
    }

    public int getRadiusY() {
        return Math.max(1, radiusY);
    }

    public int getRadiusZ() {
        return Math.max(1, radiusZ);
    }

    public double getSizeMultiplierMin() {
        return clampPositive(sizeMultiplierMin, 1.0D);
    }

    public double getSizeMultiplierMax() {
        return Math.max(getSizeMultiplierMin(), clampPositive(sizeMultiplierMax, 1.0D));
    }

    public double getIrregularity() {
        return clamp(irregularity, 0.0D, 1.0D);
    }

    public int getSteps() {
        return Math.max(1, steps);
    }

    public double getStepLength() {
        return clampPositive(stepLength, 1.5D);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRadiusX(int radiusX) {
        this.radiusX = radiusX;
    }

    public void setRadiusY(int radiusY) {
        this.radiusY = radiusY;
    }

    public void setRadiusZ(int radiusZ) {
        this.radiusZ = radiusZ;
    }

    public void setSizeMultiplierMin(double sizeMultiplierMin) {
        this.sizeMultiplierMin = sizeMultiplierMin;
    }

    public void setSizeMultiplierMax(double sizeMultiplierMax) {
        this.sizeMultiplierMax = sizeMultiplierMax;
    }

    public void setIrregularity(double irregularity) {
        this.irregularity = irregularity;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setStepLength(double stepLength) {
        this.stepLength = stepLength;
    }

    private static double clampPositive(double value, double fallback) {
        return value <= 0.0D ? fallback : value;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
