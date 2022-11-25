package com.davixdevelop.schem2obj.util;

import com.davixdevelop.schem2obj.materials.IMaterial;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ImageUtility {
    public static void copyImageToFile(BufferedImage bufferedImage, String filePath){
        //InputStream assetStream = this.getClass().getClassLoader().getResourceAsStream("assets/" + asset);

        try {
            InputStream imageStream = toInputStream(bufferedImage);

            copyImageStreamToFile(imageStream, filePath);
        }catch (Exception ex){
            LogUtility.Log(String.format("Could not read BufferedImage to InputStream for output image path: %s", filePath));
            LogUtility.Log(ex.getMessage());
        }

    }

    public static void copyImageStreamToFile(InputStream imageStream, String filePath){
        try {
            OutputStream outputStream = new FileOutputStream(filePath);

            try{


                byte[] buffer = new byte[1024];
                int readBytes = imageStream.read(buffer);

                while(readBytes != -1){
                    outputStream.write(buffer, 0, readBytes);
                    readBytes = imageStream.read(buffer);
                }

                outputStream.flush();
                outputStream.close();

                imageStream.close();

            }catch (Exception ex){
                LogUtility.Log(String.format("Could not read from assets for output file path: %s", filePath));
                LogUtility.Log(ex.getMessage());
            }


        }catch (FileNotFoundException ex){
            LogUtility.Log(String.format("Could not copy asset to output file path: %s", filePath));
            LogUtility.Log(ex.getMessage());
        }

    }

    public static InputStream toInputStream(BufferedImage image){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }catch (Exception ex){
            LogUtility.Log("Could not convert BufferedImage to InputStream");
            return null;
        }
    }

    public static BufferedImage toBuffedImage(InputStream inputStream){
        try {
            BufferedImage bufferedImage = ImageIO.read(inputStream);

            inputStream.close();

            return bufferedImage;
        }catch (IOException ex){
            LogUtility.Log("Could read image from input stream");
            LogUtility.Log(ex.getMessage());
        }
        return null;
    }

    static HashMap<String, BufferedImage> LOADED_ALPHA_IMAGES = new HashMap<>();

    public static boolean hasAlpha(IMaterial material, List<Double[]> UV){
        try {
            BufferedImage image;

            if(material.storeDiffuseImage())
                image = material.getDiffuseImage();
            else {
                if(!LOADED_ALPHA_IMAGES.containsKey(material.getName())){
                    BufferedImage diffuseImage = material.getDiffuseImage();

                    if(diffuseImage.getTransparency() != 3)
                        return false;

                    //Strip the R G B channels, to only store the alpha channel in memory
                    LOADED_ALPHA_IMAGES.put(material.getName(), extractAlphaChannel(diffuseImage));
                }

                image = LOADED_ALPHA_IMAGES.get(material.getName());
            }


            boolean hasTransparency = (image.getType() == BufferedImage.TYPE_BYTE_GRAY) || (image.getTransparency() == 3);
            if(hasTransparency){
                Double uv_min_x = UV.stream().min(Comparator.comparing(v -> v[0])).get()[0]; //Min x
                Double uv_max_x = UV.stream().max(Comparator.comparing(v -> v[0])).get()[0]; //Max x
                Double uv_min_y = UV.stream().min(Comparator.comparing(v -> v[1])).get()[1]; //Min y
                Double uv_max_y = UV.stream().max(Comparator.comparing(v -> v[1])).get()[1]; //Max y

                int x_min = (int) Math.round(image.getWidth() * uv_min_x);
                int x_max = (int) Math.round(image.getWidth() * uv_max_x);

                int y_min = (int) Math.round((1.0 - uv_max_y) * image.getHeight());
                int y_max = (int) Math.round((1.0 - uv_min_y) * image.getHeight());

                boolean isGrayscale = image.getType() == BufferedImage.TYPE_BYTE_GRAY;

                for(int y = y_min ;y < y_max; y++){
                    for(int x = x_min ;x < x_max; x++){
                        int color = image.getRGB(x, y);
                        float alpha = (isGrayscale) ? (float)(color >> 16 & 255) : (float)(color >> 24 & 255);
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

    public static BufferedImage extractAlphaChannel(BufferedImage bufferedImage){
            BufferedImage alphaGrayImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

            for(int y = 0; y < bufferedImage.getHeight(); y++){
                for(int x = 0; x < bufferedImage.getWidth(); x++){
                    int rawColor = bufferedImage.getRGB(x, y);

                    Color color = new Color(rawColor, true);
                    color = new Color(color.getAlpha(), color.getAlpha(), color.getAlpha());

                    alphaGrayImage.setRGB(x, y, color.getRGB());

                }
            }


            return alphaGrayImage;
    }

    /**
     * Extract the Roughness (Inverted Glossiness Red Channel), Metalness (Green Channel), Emission (Blue Channel) image from the specular texture
     * as SEUS stores Glossiness, Metalness and Emission in the the tree RGB channels in the specular texture
     * @param specularImage The specular texture of the material
     * @return A 3 length array of extracted PBR textures
     */
    public static BufferedImage[] extractPBRFromSpec(BufferedImage specularImage){
        try{

            BufferedImage[] RME = new BufferedImage[] {
                    new BufferedImage(specularImage.getWidth(), specularImage.getHeight(), BufferedImage.TYPE_INT_ARGB),
                    new BufferedImage(specularImage.getWidth(), specularImage.getHeight(), BufferedImage.TYPE_INT_ARGB),
                    new BufferedImage(specularImage.getWidth(), specularImage.getHeight(), BufferedImage.TYPE_INT_ARGB)
            };

            Color color;
            Color roughness;
            Color metalness;
            Color emission;

            for(int x = 0; x < specularImage.getWidth(); x++){
                for(int y = 0; y < specularImage.getHeight(); y++){
                    color = new Color(specularImage.getRGB(x, y));

                    //Roughness is the inverse of glossiness (255 - channel value)
                    roughness = new Color(Math.abs(255 - color.getRed()),Math.abs(255 - color.getRed()), Math.abs(255 - color.getRed()), color.getAlpha());
                    metalness = new Color(color.getGreen(), color.getGreen(), color.getGreen(), color.getAlpha());
                    emission = new Color(color.getBlue(), color.getBlue(), color.getBlue(), color.getAlpha());

                    RME[0].setRGB(x, y, roughness.getRGB());
                    RME[1].setRGB(x, y, metalness.getRGB());
                    RME[2].setRGB(x, y, emission.getRGB());

                }
            }

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
    public static BufferedImage maskImage(BufferedImage image, BufferedImage mask,double mixFactor){
        try {
            BufferedImage maskedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

            Color color;
            Color maskColor;

            int max_alpha = -1;
            int min_alpha = -1;
            int values = 0;
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


            for(int x = 0; x < image.getWidth(); x++){
                for(int y = 0; y < image.getHeight(); y++){
                    int col = image.getRGB(x, y);
                    color = new Color(col);

                    int m = mask.getRGB((x * mask.getWidth()) / image.getWidth(), (y * mask.getHeight()) / image.getHeight());
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

            return maskedImage;

        }catch (Exception ex){
            LogUtility.Log("Could not mask image");
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    public static BufferedImage maskImage(BufferedImage image, BufferedImage image2){
        try{
            boolean hasOverlayAlpha = image2.getTransparency() == 3;
            BufferedImage combinedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

            for(int x = 0; x < image.getWidth(); x++){
                for(int y = 0; y < image.getHeight(); y++){
                    final int color1 = image.getRGB(x, y);
                    final int x1 = (x * image2.getWidth()) / image.getWidth();
                    final int y1 = (y * image2.getHeight()) / image.getHeight();
                    final int color2 = image2.getRGB(x1, y1);

                    Color originalColor = new Color(color1);
                    Color maskColor = new Color(color2, hasOverlayAlpha);
                    if(maskColor.getAlpha() != 0){
                        combinedImage.setRGB(x, y, originalColor.getRGB());
                    }else
                    {
                        combinedImage.setRGB(x, y, maskColor.getRGB());
                    }


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
     * Color a image with the provide color
     * @param bufferedImage The diffuse image to color
     * @param color A Integer color
     * @return The colored image
     */
    public static BufferedImage colorImage(BufferedImage bufferedImage, int color, boolean ...isMask){

        float multiRed = (float)(color >> 16 & 255) / 255.0f;
        float multiGreen = (float)(color >> 8 & 255) / 255.0f;
        float multiBlue = (float) (color & 255) / 255.0f;

        boolean masked = false;

        if(isMask.length > 0)
            masked = isMask[0];

        try {
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
                        if(masked && r == 0.0f)
                            alpha = 0.0f;
                        else{
                            r *= multiRed;
                            g *= multiGreen;
                            b *= multiBlue;
                        }
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

    public static BufferedImage multiplyImage(BufferedImage image, BufferedImage multiplyImage){
        try{
            boolean hasMultiplyAlpha = multiplyImage.getTransparency() == 3;
            BufferedImage combinedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

            for(int x = 0; x < image.getWidth(); x++){
                for(int y = 0; y < image.getHeight(); y++){
                    final int color1 = image.getRGB(x, y);
                    final int x1 = (x * multiplyImage.getWidth()) / image.getWidth();
                    final int y1 = (y * multiplyImage.getHeight()) / image.getHeight();
                    final int color2 = multiplyImage.getRGB(x1, y1);

                    Color originalColor = new Color(color1);
                    Color multiplyColor = new Color(color2, hasMultiplyAlpha);

                    if(originalColor.getAlpha() != 0 && multiplyColor.getAlpha() != 0) {

                        int red = (originalColor.getRed() * multiplyColor.getRed()) / 255;
                        int green = (originalColor.getGreen() * multiplyColor.getGreen()) / 255;
                        int blue = (originalColor.getBlue() * multiplyColor.getBlue()) / 255;

                        originalColor = new Color(ColorUtility.clipRGB(Math.abs(red)), ColorUtility.clipRGB(Math.abs(green)), ColorUtility.clipRGB(Math.abs(blue)), originalColor.getAlpha());

                    }
                    combinedImage.setRGB(x, y, originalColor.getRGB());
                }
            }

            return combinedImage;

        }catch (Exception ex){
            LogUtility.Log("Failed to color image");
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    public static BufferedImage overlayImage(BufferedImage image, BufferedImage overlay){
        try{
            boolean hasOverlayAlpha = overlay.getTransparency() == 3;
            boolean hasImageAlpha = image.getTransparency() == 3;
            BufferedImage combinedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

            for(int x = 0; x < image.getWidth(); x++){
                for(int y = 0; y < image.getHeight(); y++){
                    final int color1 = image.getRGB(x, y);
                    final int x1 = (x * overlay.getWidth()) / image.getWidth();
                    final int y1 = (y * overlay.getHeight()) / image.getHeight();
                    final int color2 = overlay.getRGB(x1, y1);

                    Color originalColor = new Color(color1, hasImageAlpha);
                    Color overlayColor = new Color(color2, hasOverlayAlpha);
                    if(overlayColor.getAlpha() != 0){
                        originalColor = overlayColor;
                    }

                    combinedImage.setRGB(x, y, originalColor.getRGB());
                }
            }

            return combinedImage;

        }catch (Exception ex){
            LogUtility.Log("Failed to combine images");
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    public static BufferedImage upscaleImage(BufferedImage image, Integer xRes, Integer yRes){
        BufferedImage upscaleImage = new BufferedImage(xRes, yRes, image.getType());

        for(int x = 0; x < xRes; x++){
            for (int y = 0; y <yRes; y++){
                final int x1 = (x * image.getWidth()) / xRes;
                final int y1 = (y * image.getHeight()) / yRes;
                final int color = image.getRGB(x1, y1);

                upscaleImage.setRGB(x, y, color);
            }
        }

        return upscaleImage;
    }

    public static void insertIntoImage(BufferedImage image, BufferedImage image2, int xOffset, int yOffset){
        int local_x = 0;
        for(int x = xOffset; x < xOffset + image2.getWidth(); x++){
            int local_y = 0;
            for(int y = yOffset; y < yOffset + image2.getHeight(); y++){
                int color = image2.getRGB(local_x, local_y);
                image.setRGB(x, y, color);
                local_y += 1;
            }
            local_x += 1;
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

    public static boolean isTransparent(BufferedImage image){
        boolean hasAlpha = image.getTransparency() == 3;

        if(hasAlpha){
            for(int x = 0; x < image.getWidth(); x++){
                for(int y = 0; y < image.getHeight();y++){
                    int color = image.getRGB(x, y);
                    int alpha = (color >> 24 & 255);
                    if(alpha != 0)
                        return false;
                }
            }

            return true;
        }return false;
    }

    /**
     * Color match the target image with the source image
     * @param source The source image
     * @param target The target image
     * @return Color matched target image
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
        double[] stats = ImageStatistic(lab_source);
        double L_MeanSource = stats[0];
        double L_StdSource = stats[1];
        double a_MeanSource = stats[2];
        double a_StdSource = stats[3];
        double b_MeanSource = stats[4];
        double b_StdSource = stats[5];

        stats = ImageStatistic(lab_target);
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
     * @return Double array with the following items:
     *      Channel 1 mean
     *      Channel 1 standard deviation
     *      Channel 2 mean
     *      Channel 2 standard deviation
     *      Channel 3 mean
     *      Channel 4 standard deviation
     */
    private static double[] ImageStatistic(Object[] data){
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
