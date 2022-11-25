package com.davixdevelop.schem2obj.util;

import java.awt.color.ColorSpace;

/**
 * Color space for the CIE Lab color space
 * Copied from: https://stackoverflow.com/questions/4593469/java-how-to-convert-rgb-color-to-cie-lab
 * @author finnw (https://stackoverflow.com/users/12048/finnw)
 */
public class CIELab extends ColorSpace {

    public static CIELab getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public float[] fromCIEXYZ(float[] colorValue) {
        double l = f(colorValue[1]);
        double L = 116.0 * l - 16.0;
        double a = 500.0 * (f(colorValue[0]) - l);
        double b = 200.0 * (l - f(colorValue[2]));
        return new float[] {(float) L, (float) a, (float) b};
    }

    @Override
    public float[] fromRGB(float[] rgbValue) {
        float[] xyz = CIEXYZ.fromRGB(rgbValue);
        return fromCIEXYZ(xyz);
    }

    @Override
    public float getMaxValue(int component) {
        return 128f;
    }

    @Override
    public float getMinValue(int component) {
        return (component == 0)? 0f: -128f;
    }

    @Override
    public String getName(int idx) {
        return String.valueOf("Lab".charAt(idx));
    }

    @Override
    public float[] toCIEXYZ(float[] colorValue) {
        double i = (colorValue[0] + 16.0) * (1.0 / 116.0);
        double X = fInv(i + colorValue[1] * (1.0 / 500.0));
        double Y = fInv(i);
        double Z = fInv(i - colorValue[2] * (1.0 / 200.0));
        return new float[] {(float) X, (float) Y, (float) Z};
    }

    @Override
    public float[] toRGB(float[] colorValue) {
        float[] xyz = toCIEXYZ(colorValue);
        return CIEXYZ.toRGB(xyz);
    }

    CIELab() {
        super(ColorSpace.TYPE_Lab, 3);
    }

    private static double f(double x) {
        if (x > 216.0 / 24389.0) {
            return Math.cbrt(x);
        } else {
            return (841.0 / 108.0) * x + N;
        }
    }

    private static double fInv(double x) {
        if (x > 6.0 / 29.0) {
            return x*x*x;
        } else {
            return (108.0 / 841.0) * (x - N);
        }
    }

    private Object readResolve() {
        return getInstance();
    }

    private static class Holder {
        static final CIELab INSTANCE = new CIELab();
    }

    private static final long serialVersionUID = 5027741380892134289L;

    private static final ColorSpace CIEXYZ =
            ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

    private static final double N = 4.0 / 29.0;

}
