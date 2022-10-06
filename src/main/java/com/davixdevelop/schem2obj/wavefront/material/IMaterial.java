package com.davixdevelop.schem2obj.wavefront.material;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;

public interface IMaterial {
    double getLightValue();
    void setLightValue(double value);
    String getName();
    void setName(String name);
    String getDiffuseTexturePath();
    void setDiffuseTexturePath(String texturePath);
    String getDiffuseTextureName();
    void setDiffuseTextureName(String textureName);
    InputStream getDiffuseImage();
    void setDiffuseImage(BufferedImage diffuseImage);
    ArrayList<String> toMat(String textureFolder);

    /**
     * Return a deep copy of the material
     * @return The deep copy of the material
     */
    IMaterial clone();
    /**
     * Create deep copy from clone
     * @param clone The cloned material
     */
    void copy(IMaterial clone);
}
