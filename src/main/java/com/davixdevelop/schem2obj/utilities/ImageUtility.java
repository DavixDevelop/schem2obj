package com.davixdevelop.schem2obj.utilities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

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
                Utility.Log("Could not read from assets");
                Utility.Log(ex.getMessage());
                return false;
            }


        }catch (FileNotFoundException ex){
            Utility.Log("Could not copy asset to file");
            Utility.Log(ex.getMessage());
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
            Utility.Log("Could not convert BufferedImage to InputStream");
            return null;
        }
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
            Utility.Log("Failed to read specular texture");
            Utility.Log(ex.getMessage());
            return null;
        }

    }

    public static BufferedImage maskImage(InputStream image, BufferedImage mask){
        try {
            BufferedImage originalImage = ImageIO.read(image);
            BufferedImage maskedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

            Color color = null;
            Color maskColor = null;

            /*Integer max_mask = null;
            Integer min_mask = null;

            for(int x = 0; x < mask.getWidth(); x++) {
                for (int y = 0; y < mask.getHeight(); y++) {
                    int col = mask.getRGB(x, y);
                    maskColor = new Color(col);

                    if(max_mask == null){
                        max_mask = maskColor.getRed();
                        min_mask = maskColor.getRed();
                    }else{
                        if(maskColor.getRed() > max_mask)
                            max_mask = maskColor.getRed();

                        if(maskColor.getRed() < min_mask)
                            min_mask = maskColor.getRed();
                    }
                }
            }

            Integer mask_limit = max_mask - min_mask;*/

            for(int x = 0; x < originalImage.getWidth(); x++){
                for(int y = 0; y < originalImage.getHeight(); y++){
                    int col = originalImage.getRGB(x, y);

                    int m = mask.getRGB((x * mask.getWidth()) / originalImage.getWidth(), (y * mask.getHeight()) / originalImage.getHeight());
                    maskColor = new Color(m);

                    color = new Color(col);
                    color = new Color(color.getRed(), color.getGreen(), color.getBlue(), maskColor.getRed());
                    //Double perc = (maskColor.getRed() - min_mask) / mask_limit.doubleValue();
                    //color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) Math.round(perc * max_mask));
                    maskedImage.setRGB(x, y, color.getRGB());
                }
            }

            image.close();
            return maskedImage;

        }catch (Exception ex){
            Utility.Log("Could not mask image");
            Utility.Log(ex.getMessage());
            return null;
        }
    }

    public static BufferedImage colorImage(InputStream image, int color){
        Color overlayColor = new Color(color);
        try {
            BufferedImage bufferedImage = ImageIO.read(image);
            BufferedImage coloredImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());

            boolean hasAlpha = bufferedImage.getTransparency() == 3;

            for(int x = 0; x < bufferedImage.getWidth(); x++){
                for(int y = 0; y < bufferedImage.getHeight(); y++){
                    int c = bufferedImage.getRGB(x, y);
                    Color rgb = new Color(c, hasAlpha);

                    if(rgb.getAlpha() != 0){
                        int luminosity = RGBtoLuminosity(rgb);

                        rgb = new Color(Math.abs(overlayColor.getRed() - luminosity), Math.abs(overlayColor.getGreen() - luminosity), Math.abs(overlayColor.getBlue() - luminosity), rgb.getAlpha());

                    }
                    coloredImage.setRGB(x, y, rgb.getRGB());
                }
            }

            return coloredImage;

        }catch (Exception ex){
            Utility.Log("Failed to read input image");
            Utility.Log(ex.getMessage());
            return null;
        }
    }

    public static BufferedImage colorAndCombineImages(InputStream image, BufferedImage overlay){
        try{
            BufferedImage orgImage = ImageIO.read(image);
            boolean hasAlpha = overlay.getTransparency() == 3;
            BufferedImage combinedImage = new BufferedImage(orgImage.getWidth(), orgImage.getHeight(), orgImage.getType());

            for(int x = 0; x < orgImage.getWidth(); x++){
                for(int y = 0; y < orgImage.getHeight(); y++){
                    final int color1 = orgImage.getRGB(x, y);
                    final int x1 = (x * overlay.getWidth()) / orgImage.getWidth();
                    final int y1 = (y * overlay.getHeight()) / orgImage.getHeight();
                    final int color2 = overlay.getRGB(x1, y1);

                    Color orginalColor = new Color(color1);
                    Color overlayColor = new Color(color2, hasAlpha);
                    if(overlayColor.getAlpha() != 0){
                        int luminosity = RGBtoLuminosity(orginalColor);

                        orginalColor = new Color(Math.abs(overlayColor.getRed() - luminosity), Math.abs(overlayColor.getGreen() - luminosity), Math.abs(overlayColor.getBlue() - luminosity), orginalColor.getAlpha());
                    }

                    combinedImage.setRGB(x, y, orginalColor.getRGB());
                }
            }

            return combinedImage;

        }catch (Exception ex){
            Utility.Log("Failed to combine images");
            Utility.Log(ex.getMessage());
            return null;
        }
    }

    private static int RGBtoLuminosity(Color rgba){
        return (int) Math.round(100 * (((0.299*rgba.getRed()) + (0.587* rgba.getGreen()) + (0.114 * rgba.getBlue())) / 255));
    }
}
