package com.davixdevelop.schem2obj.util;

import java.awt.*;
import java.awt.color.ColorSpace;

public class ColorUtility {

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

        if(val < 0)
            return 0;

        return val;
    }
}
