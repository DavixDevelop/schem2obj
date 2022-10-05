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
}
