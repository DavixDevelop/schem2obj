package com.davixdevelop.schem2obj.utilities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

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

                    //Roughness in the inverse of glossiness (255 - channel value)
                    rougness = new Color(255 - color.getRed(),255 - color.getRed(), 255 - color.getRed(), color.getAlpha());
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
}
