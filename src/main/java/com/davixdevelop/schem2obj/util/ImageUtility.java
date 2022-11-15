package com.davixdevelop.schem2obj.util;

import com.davixdevelop.schem2obj.wavefront.material.IMaterial;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ImageUtility {
    public static boolean copyImageToFile(InputStream imageStream, String filePath){
        //InputStream assetStream = this.getClass().getClassLoader().getResourceAsStream("assets/" + asset);

        try {
            OutputStream outputStream = new FileOutputStream(filePath);

            try{
                byte[] buffer = new byte[1024];
                Integer readBytes = imageStream.read(buffer);

                while(readBytes != -1){
                    outputStream.write(buffer, 0, readBytes);
                    readBytes = imageStream.read(buffer);
                }

                outputStream.flush();
                outputStream.close();

                imageStream.close();

            }catch (Exception ex){
                LogUtility.Log("Could not read from assets");
                LogUtility.Log(ex.getMessage());
                return false;
            }


        }catch (FileNotFoundException ex){
            LogUtility.Log("Could not copy asset to file");
            LogUtility.Log(ex.getMessage());
            return false;
        }

        return true;
    }

    public static InputStream bufferedImageToInputStream(BufferedImage image){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }catch (Exception ex){
            LogUtility.Log("Could not convert BufferedImage to InputStream");
            return null;
        }
    }

    static HashMap<String, BufferedImage> LOADED_IMAGES = new HashMap<>();

    public static boolean hasAlpha(IMaterial material, ArrayList<Double[]> UV){
        try {
            if(!LOADED_IMAGES.containsKey(material.getDiffuseTexturePath())){
                LOADED_IMAGES.put(material.getDiffuseTexturePath(), ImageIO.read(material.getDiffuseImage()));
            }

            BufferedImage image = LOADED_IMAGES.get(material.getDiffuseTexturePath());
            boolean hasTransparency = (image.getTransparency() == 3);
            if(hasTransparency){
                Double uv_min_x = UV.stream().min(Comparator.comparing(v -> v[0])).get()[0]; //Min x
                Double uv_max_x = UV.stream().max(Comparator.comparing(v -> v[0])).get()[0]; //Max x
                Double uv_min_y = UV.stream().min(Comparator.comparing(v -> v[1])).get()[1]; //Min y
                Double uv_max_y = UV.stream().max(Comparator.comparing(v -> v[1])).get()[1]; //Max y

                int x_min = (int) Math.round(image.getWidth() * uv_min_x);
                int x_max = (int) Math.round(image.getWidth() * uv_max_x);

                int y_min = (int) Math.round((1.0 - uv_max_y) * image.getHeight());
                int y_max = (int) Math.round((1.0 - uv_min_y) * image.getHeight());

                for(int y = y_min ;y < y_max; y++){
                    for(int x = x_min ;x < x_max; x++){
                        int color = image.getRGB(x, y);
                        float alpha = (float)(color >> 24 & 255);
                        if(alpha != 255.0) {
                            return true;
                        }
                    }
                }
            }

        }catch (Exception ex){
            return false;
        }

        return false;
    }

    /**
     * Get the absolute path to the texture in a resource pack
     * @param resourcePath The path to the resource pack
     * @param texture The name of the texture, ex. blocks/dirt
     * @return
     */
    public static Path getExternalTexture(String resourcePath, String texture){
        return Paths.get(resourcePath, "assets","minecraft","textures",String.format("%s.png", texture));
    }

    /**
     * Extract the Roughness (Inverted Glossiness Red Channel), Metalness (Green Channel), Emission (Blue Channel) image from the specular texture
     * as SEUS stores Glossiness, Metalness and Emission in the the tree RGB channels in the specular texture
     * @param specularAsset
     * @return
     */
    public static BufferedImage[] extractPBRFromSpec(InputStream specularAsset){
        try{
            BufferedImage specularImage = ImageIO.read(specularAsset);

            BufferedImage[] RME = new BufferedImage[] {
                    new BufferedImage(specularImage.getWidth(), specularImage.getHeight(), BufferedImage.TYPE_INT_ARGB),
                    new BufferedImage(specularImage.getWidth(), specularImage.getHeight(), BufferedImage.TYPE_INT_ARGB),
                    new BufferedImage(specularImage.getWidth(), specularImage.getHeight(), BufferedImage.TYPE_INT_ARGB)
            };

            Color color = null;
            Color rougness = null;
            Color metalness = null;
            Color emission = null;

            for(int x = 0; x < specularImage.getWidth(); x++){
                for(int y = 0; y < specularImage.getHeight(); y++){
                    color = new Color(specularImage.getRGB(x, y));

                    //Roughness is the inverse of glossiness (255 - channel value)
                    rougness = new Color(Math.abs(255 - color.getRed()),Math.abs(255 - color.getRed()), Math.abs(255 - color.getRed()), color.getAlpha());
                    metalness = new Color(color.getGreen(), color.getGreen(), color.getGreen(), color.getAlpha());
                    emission = new Color(color.getBlue(), color.getBlue(), color.getBlue(), color.getAlpha());

                    RME[0].setRGB(x, y, rougness.getRGB());
                    RME[1].setRGB(x, y, metalness.getRGB());
                    RME[2].setRGB(x, y, emission.getRGB());

                }
            }

            specularAsset.close();

            return RME;

        }catch (Exception ex){
            LogUtility.Log("Failed to read specular texture");
            LogUtility.Log(ex.getMessage());
            return null;
        }

    }

    /**
     * Mask an image with the grayscale mask
     * @param image The original image
     * @param mask A grayscale image mask
     * @return An masked imaged where the original image transparency is based on the mask
     * White pixels aren't transparent, while black pixels are full on transparent
     */
    public static BufferedImage maskImage(InputStream image, BufferedImage mask,double mixFactor){
        try {
            BufferedImage originalImage = ImageIO.read(image);
            BufferedImage maskedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

            Color color = null;
            Color maskColor = null;

            Integer max_alpha = -1;
            Integer min_alpha = -1;
            Integer values = 0;
            boolean transparent = true;
            for(int y = 0; y < mask.getHeight(); y++){
                for(int x = 0; x < mask.getWidth(); x++){
                    int m = mask.getRGB(x , y);
                    maskColor = new Color(m);

                    if(max_alpha == -1){
                        max_alpha = maskColor.getRed();
                        min_alpha = maskColor.getRed();
                        values = maskColor.getRed();
                    }
                    if(maskColor.getRed() > max_alpha)
                        max_alpha = maskColor.getRed();
                    else if(maskColor.getRed() < min_alpha)
                        min_alpha = maskColor.getRed();

                    if(values != maskColor.getRed())
                        transparent = false;
                }
            }


            for(int x = 0; x < originalImage.getWidth(); x++){
                for(int y = 0; y < originalImage.getHeight(); y++){
                    int col = originalImage.getRGB(x, y);
                    color = new Color(col);

                    int m = mask.getRGB((x * mask.getWidth()) / originalImage.getWidth(), (y * mask.getHeight()) / originalImage.getHeight());
                    maskColor = new Color(m);

                    int alpha = maskColor.getRed();
                    int r = color.getRed();
                    int b = color.getBlue();
                    int g = color.getGreen();


                    if(mixFactor != 0.0)
                    {
                        if(alpha != 0 || min_alpha == 0 && !transparent) {
                            int a = (int) Math.round(255 * mixFactor);
                            if (alpha < a) {
                                alpha = a;
                            } else {
                                a = (int) Math.round((1 / mixFactor) * (alpha * mixFactor));
                                alpha = ColorUtility.clipRGB(a + alpha);
                            }
                        }
                    }


                    color = new Color(r, g, b, alpha);
                    maskedImage.setRGB(x, y, color.getRGB());
                }
            }

            image.close();
            return maskedImage;

        }catch (Exception ex){
            LogUtility.Log("Could not mask image");
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    public static BufferedImage colorImage(InputStream image, int color){

        float multiRed = (float)(color >> 16 & 255) / 255.0f;
        float multiGreen = (float)(color >> 8 & 255) / 255.0f;
        float multiBlue = (float) (color & 255) / 255.0f;

        try {
            BufferedImage bufferedImage = ImageIO.read(image);
            BufferedImage coloredImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());

            boolean hasAlpha = bufferedImage.getTransparency() == 3;

            for(int x = 0; x < bufferedImage.getWidth(); x++){
                for(int y = 0; y < bufferedImage.getHeight(); y++){
                    int c = bufferedImage.getRGB(x, y);

                    float alpha = 0.0f;

                    float r = (float)(c >> 16 & 255);
                    float g = (float) (c >> 8 & 255);
                    float b = (float) (c & 255);


                    if(hasAlpha)
                        alpha = (float)(c >> 24 & 255);

                    if(alpha != 0.0f){
                        r *= multiRed;
                        g *= multiGreen;
                        b *= multiBlue;

                    }

                    int new_color = (int)r << 16 | (int)g << 8 | (int)b | (int)alpha << 24;

                    coloredImage.setRGB(x, y, new_color);
                }
            }

            return coloredImage;

        }catch (Exception ex){
            LogUtility.Log("Failed to read input image");
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    public static BufferedImage overlayImages(InputStream image, BufferedImage overlay){
        try{
            BufferedImage orgImage = ImageIO.read(image);
            boolean hasOverlayAlpha = overlay.getTransparency() == 3;
            BufferedImage combinedImage = new BufferedImage(orgImage.getWidth(), orgImage.getHeight(), orgImage.getType());

            for(int x = 0; x < orgImage.getWidth(); x++){
                for(int y = 0; y < orgImage.getHeight(); y++){
                    final int color1 = orgImage.getRGB(x, y);
                    final int x1 = (x * overlay.getWidth()) / orgImage.getWidth();
                    final int y1 = (y * overlay.getHeight()) / orgImage.getHeight();
                    final int color2 = overlay.getRGB(x1, y1);

                    Color orginalColor = new Color(color1);
                    Color overlayColor = new Color(color2, hasOverlayAlpha);
                    if(overlayColor.getAlpha() != 0){
                        //int luminosity = ColorUtility.RGBtoLuminosity(orginalColor);

                        //orginalColor = new Color(Math.abs(overlayColor.getRed() - luminosity), Math.abs(overlayColor.getGreen() - luminosity), Math.abs(overlayColor.getBlue() - luminosity), orginalColor.getAlpha());
                        orginalColor = overlayColor;
                    }

                    combinedImage.setRGB(x, y, orginalColor.getRGB());
                }
            }

            return combinedImage;

        }catch (Exception ex){
            LogUtility.Log("Failed to combine images");
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    /**
     * Mask an image based on the alpha channel of the alpha image
     * @param originalImage The original image
     * @param alpha And image with an alpha
     * @return The original image with alpha channel from the alpha image
     */
    public static BufferedImage maskImageFromAlpha(BufferedImage originalImage, BufferedImage alpha){
        try {
            BufferedImage maskedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

            for(int y = 0; y < originalImage.getHeight(); y++){
                for(int x = 0; x < originalImage.getWidth(); x++){
                    final int orgColor = originalImage.getRGB(x, y);
                    final Color color = new Color(orgColor);

                    final int x1 = (x * alpha.getWidth()) / originalImage.getWidth();
                    final int y1 = (y * alpha.getHeight()) / originalImage.getHeight();
                    final int mask = alpha.getRGB(x1, y1);
                    final Color maskColor = new Color(mask, true);

                    final Color maskedColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), maskColor.getAlpha());

                    maskedImage.setRGB(x, y, maskedColor.getRGB());
                }
            }

            return maskedImage;


        }catch (Exception ex){
            LogUtility.Log("Could not mask image with alpha");
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    /**
     *
     * @param source
     * @param target
     * @return
     */
    public static BufferedImage colorMatch(BufferedImage source, BufferedImage target){
        Object[] lab_source = new Object[source.getWidth() * source.getHeight()];
        Object[] lab_target = new Object[target.getWidth() * target.getHeight()];

        //Convert source and target image to CIE LAB color space
        for(int y = 0; y < source.getHeight(); y++){
            for(int x = 0; x < source.getWidth(); x++){
                final int _color = source.getRGB(x, y);
                final Color color = new Color(_color, true);

                //Only convert if alpha is bigger than 0
                if(color.getAlpha() != 0){
                    double[] lab = new double[4];
                    ColorUtility.RGB2LAB(new int[]{color.getRed(), color.getGreen(), color.getBlue()}, lab);
                    lab[3] = color.getAlpha();

                    final int index = (y * target.getWidth()) + x;
                    lab_source[index]= lab;
                }
            }
        }

        for(int y = 0; y < target.getHeight(); y++){
            for(int x = 0; x < target.getWidth(); x++){
                final int _color = target.getRGB(x, y);
                final Color color = new Color(_color, true);

                //Only convert if alpha is bigger than 0
                if(color.getAlpha() != 0){
                    double[] lab = new double[4];
                    ColorUtility.RGB2LAB(new int[]{color.getRed(), color.getGreen(), color.getBlue()}, lab);
                    lab[3] = color.getAlpha();

                    final int index = (y * target.getWidth()) + x;
                    lab_target[index]= lab;
                }
            }
        }

        //Get the statistics (the mean and standard deviation for each channel) for the source and target image
        double[] stats = ImageStatistic(lab_source, source.getHeight(), source.getWidth());
        double L_MeanSource = stats[0];
        double L_StdSource = stats[1];
        double a_MeanSource = stats[2];
        double a_StdSource = stats[3];
        double b_MeanSource = stats[4];
        double b_StdSource = stats[5];

        stats = ImageStatistic(lab_target, target.getHeight(), target.getWidth());
        double L_MeanTarget = stats[0];
        double L_StdTarget = stats[1];
        double a_MeanTarget = stats[2];
        double a_StdTarget = stats[3];
        double b_MeanTarget = stats[4];
        double b_StdTarget = stats[5];

        //4. Clip each target channel by 0 - 255
        //5. Convert the channels from the CIELAB space to RGB
        //6. Save pixel color to target image
        for(int y = 0; y < target.getHeight(); y++) {
            for(int x = 0; x < target.getWidth(); x++){
                final int index = (y * target.getWidth()) + x;
                Object d = lab_target[index];

                Color finalColor = new Color(255, 255, 255, 0);

                if (d != null) {
                    double[] values = (double[]) d;
                    //1. Subtract mean for each channel in target
                    values[0] -= L_MeanTarget;
                    values[1] -= a_MeanTarget;
                    values[2] -= b_MeanTarget;

                    //2. Scale each channel by the standard deviations
                    values[0] *= L_StdTarget / L_StdSource;
                    values[1] *= a_StdTarget / a_StdSource;
                    values[2] *= b_StdTarget / b_StdSource;

                    //3. Add the channels with the source mean
                    values[0] += L_MeanSource;
                    values[1] += a_MeanSource;
                    values[2] += b_MeanSource;

                    int[] RGB = new int[3];
                    ColorUtility.LAB2RGB(values, RGB);
                    //ColorUtility.clipSRGB(RGB);

                    finalColor = new Color(RGB[0], RGB[1], RGB[2], (int)Math.round(values[3]));
                }

                //6
                target.setRGB(x, y, finalColor.getRGB());
            }
        }

        return target;

    }

    /**
     * Calculate the mean and standard deviation for the image data
     * @param data A flat map array of the image data, where each item is a 3 length double array consisting of values in any color space
     * @param height The height of the image
     * @param width The width og the image
     * @return Double array with the following items:
     *      Channel 1 mean
     *      Channel 1 standard deviation
     *      Channel 2 mean
     *      Channel 2 standard deviation
     *      Channel 3 mean
     *      Channel 4 standard deviation
     */
    private static double[] ImageStatistic(Object[] data, int height, int width){
        double[] stats = new double[6];

        //Mean deviation calculation
        double mean1 = 0.0;
        double mean2 = 0.0;
        double mean3 = 0.0;

        int total_size = 0;

        //Sum each channel
        for(Object d : data){
            if(d != null){
                double[] values = (double[])d;
                mean1 += values[0];
                mean2 += values[1];
                mean3 += values[2];

                total_size += 1;
            }
        }

        //Find the mean of each channel
        mean1 = mean1 / total_size;
        mean2 = mean2 / total_size;
        mean3 = mean3 / total_size;

        stats[0] = mean1;
        stats[2] = mean2;
        stats[4] = mean3;


        //Standard deviation calculation
        //The sum of squared differences between each channel value and the mean
        double standard_deviation1 = 0.0;
        double standard_deviation2 = 0.0;
        double standard_deviation3 = 0.0;

        for(Object d : data){
            if(d != null){
                double[] values = (double[])d;
                standard_deviation1 += Math.pow(values[0] - mean1, 2);
                standard_deviation2 += Math.pow(values[1] - mean2, 2);
                standard_deviation3 += Math.pow(values[2] - mean3, 2);
            }
        }

        //Divide the sum of squared differences between each channel value and the mean by the total count of the data
        stats[1] = standard_deviation1 / total_size - 1;
        stats[3] = standard_deviation2 / total_size - 1;
        stats[5] = standard_deviation3 / total_size - 1;

        return stats;
    }


}
