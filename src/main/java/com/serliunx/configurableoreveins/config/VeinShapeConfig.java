package com.serliunx.configurableoreveins.config;

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

    /**
     * 获取 Type。
     *
     * @return 处理结果。
    */
    public String getType() {
        return type == null ? "ELLIPSOID" : type;
    }

    /**
     * 获取 RadiusX。
     *
     * @return 处理结果。
    */
    public int getRadiusX() {
        return Math.max(1, radiusX);
    }

    /**
     * 获取 RadiusY。
     *
     * @return 处理结果。
    */
    public int getRadiusY() {
        return Math.max(1, radiusY);
    }

    /**
     * 获取 RadiusZ。
     *
     * @return 处理结果。
    */
    public int getRadiusZ() {
        return Math.max(1, radiusZ);
    }

    /**
     * 获取 SizeMultiplierMin。
     *
     * @return 处理结果。
    */
    public double getSizeMultiplierMin() {
        return clampPositive(sizeMultiplierMin, 1.0D);
    }

    /**
     * 获取 SizeMultiplierMax。
     *
     * @return 处理结果。
    */
    public double getSizeMultiplierMax() {
        return Math.max(getSizeMultiplierMin(), clampPositive(sizeMultiplierMax, 1.0D));
    }

    /**
     * 获取 Irregularity。
     *
     * @return 处理结果。
    */
    public double getIrregularity() {
        return clamp(irregularity, 0.0D, 1.0D);
    }

    /**
     * 获取 Steps。
     *
     * @return 处理结果。
    */
    public int getSteps() {
        return Math.max(1, steps);
    }

    /**
     * 获取 StepLength。
     *
     * @return 处理结果。
    */
    public double getStepLength() {
        return clampPositive(stepLength, 1.5D);
    }

    /**
     * 设置 Type。
     *
     * @param type 参数 type。
    */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 设置 RadiusX。
     *
     * @param radiusX 参数 radiusX。
    */
    public void setRadiusX(int radiusX) {
        this.radiusX = radiusX;
    }

    /**
     * 设置 RadiusY。
     *
     * @param radiusY 参数 radiusY。
    */
    public void setRadiusY(int radiusY) {
        this.radiusY = radiusY;
    }

    /**
     * 设置 RadiusZ。
     *
     * @param radiusZ 参数 radiusZ。
    */
    public void setRadiusZ(int radiusZ) {
        this.radiusZ = radiusZ;
    }

    /**
     * 设置 SizeMultiplierMin。
     *
     * @param sizeMultiplierMin 参数 sizeMultiplierMin。
    */
    public void setSizeMultiplierMin(double sizeMultiplierMin) {
        this.sizeMultiplierMin = sizeMultiplierMin;
    }

    /**
     * 设置 SizeMultiplierMax。
     *
     * @param sizeMultiplierMax 参数 sizeMultiplierMax。
    */
    public void setSizeMultiplierMax(double sizeMultiplierMax) {
        this.sizeMultiplierMax = sizeMultiplierMax;
    }

    /**
     * 设置 Irregularity。
     *
     * @param irregularity 参数 irregularity。
    */
    public void setIrregularity(double irregularity) {
        this.irregularity = irregularity;
    }

    /**
     * 设置 Steps。
     *
     * @param steps 参数 steps。
    */
    public void setSteps(int steps) {
        this.steps = steps;
    }

    /**
     * 设置 StepLength。
     *
     * @param stepLength 参数 stepLength。
    */
    public void setStepLength(double stepLength) {
        this.stepLength = stepLength;
    }

    /**
     * 执行 clampPositive 逻辑。
     *
     * @param value 参数 value。
     * @param fallback 参数 fallback。
     * @return 处理结果。
    */
    private static double clampPositive(double value, double fallback) {
        return value <= 0.0D ? fallback : value;
    }

    /**
     * 执行 clamp 逻辑。
     *
     * @param value 参数 value。
     * @param min 参数 min。
     * @param max 参数 max。
     * @return 处理结果。
    */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
