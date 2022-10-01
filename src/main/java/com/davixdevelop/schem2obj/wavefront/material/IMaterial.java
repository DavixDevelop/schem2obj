package com.davixdevelop.schem2obj.wavefront.material;

import java.io.InputStream;
import java.util.ArrayList;

public interface IMaterial {
    double getLightValue();
    void setLightValue(double value);
    String getName();
    String getTexture();
    void setTexture(String texturePath);
    String getTextureName();
    void setTextureName(String textureName);
    InputStream getDiffuseImage();
    void setDiffuseImage(InputStream diffuseImage);
    ArrayList<String> toMat(String textureFolder);
}
