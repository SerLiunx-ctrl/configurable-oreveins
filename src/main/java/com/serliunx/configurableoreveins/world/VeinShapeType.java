package com.serliunx.configurableoreveins.world;

import java.util.Locale;

/**
 * 矿脉形状枚举.
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
 */
public enum VeinShapeType {

    ELLIPSOID {
        @Override
        public double normalizedDistance(int x, int y, int z, int radiusX, int radiusY, int radiusZ) {
            double nx = normalize(x, radiusX);
            double ny = normalize(y, radiusY);
            double nz = normalize(z, radiusZ);
            return Math.sqrt((nx * nx) + (ny * ny) + (nz * nz));
        }
    },

    SPHERE {
        @Override
        public double normalizedDistance(int x, int y, int z, int radiusX, int radiusY, int radiusZ) {
            int radius = Math.max(radiusX, Math.max(radiusY, radiusZ));
            double nx = normalize(x, radius);
            double ny = normalize(y, radius);
            double nz = normalize(z, radius);
            return Math.sqrt((nx * nx) + (ny * ny) + (nz * nz));
        }
    },

    BOX {
        @Override
        public double normalizedDistance(int x, int y, int z, int radiusX, int radiusY, int radiusZ) {
            return Math.max(
                    normalize(Math.abs(x), radiusX),
                    Math.max(normalize(Math.abs(y), radiusY), normalize(Math.abs(z), radiusZ)));
        }
    },

    WORM {
        @Override
        public double normalizedDistance(int x, int y, int z, int radiusX, int radiusY, int radiusZ) {
            return 0.0D;
        }
    };

    public abstract double normalizedDistance(int x, int y, int z, int radiusX, int radiusY, int radiusZ);

    /**
     * 根据配置字符串解析矿脉形状.
     */
    public static VeinShapeType fromConfig(String raw) {
        if (raw == null) {
            return ELLIPSOID;
        }

        try {
            return VeinShapeType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ELLIPSOID;
        }
    }

    private static double normalize(int value, int radius) {
        return radius <= 0 ? 0.0D : (double) value / (double) radius;
    }
}
