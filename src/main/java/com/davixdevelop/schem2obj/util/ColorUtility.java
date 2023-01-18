package com.davixdevelop.schem2obj.util;

import java.awt.*;

public class ColorUtility {

    public static Double RGBtoLuminosity(Color color){
        return  (0.2126 * color.getRed()) + (0.7152 * color.getGreen()) + (0.0722 * color.getBlue());
    }

    public static void RGB2LAB(int[] rgb, double[] out){
        float[] lab = CIELab.getInstance().fromRGB(new float[]{rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f});

        for(int l = 0; l < 3; l++)
            out[l] = lab[l];
    }

    public static void LAB2RGB(double[] lab, int[] out){
        //float[] xyz = ColorSpace.getInstance(ColorSpace.TYPE_Lab).toCIEXYZ(new float[]{(float) lab[0], (float) lab[1], (float) lab[2]});
        float[] rgb = CIELab.getInstance().toRGB(new float[]{(float) lab[0], (float) lab[1], (float) lab[2]});

        for(int l = 0; l < 3; l++)
            out[l] = Math.round(rgb[l] * 255);

    }

    public static int clipRGB(int val){
        if(val > 255)
            return 255;

        return Math.max(val, 0);

    }

    public static Color multiplyColor(Color color1, Color color2){
        int red = (color1.getRed() * color2.getRed()) / 255;
        int green = (color1.getGreen() * color2.getGreen()) / 255;
        int blue = (color1.getBlue() * color2.getBlue()) / 255;
        return new Color(clipRGB(red), clipRGB(green), clipRGB(blue));
    }
}
