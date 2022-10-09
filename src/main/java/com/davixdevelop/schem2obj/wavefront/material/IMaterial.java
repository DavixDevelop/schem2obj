package com.davixdevelop.schem2obj.wavefront.material;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;

public interface IMaterial {


    /**
     * Get Ke value (Emission Strength)
     * @return
     */
    double getEmissionStrength();
    void seEmissionStrength(double value);

    /**
     * Get Ka value (Ambient Color)
     * @return 3 length double array
     */
    Double getAmbientColor();

    /**
     * Set Ka value
     * @param value Ka exponent
     */
    void setAmbientColor(double value);

    /**
     * Get Ks value (Specular Color)
     */
    Double getSpecularColor();

    /**
     * Set Ks exponent
     * @param value Ks exponent
     */
    void setSpecularColor(double value);

    /**
     * Get Ns value (Specular Highlight)
     * @return double value
     */
    Double getSpecularHighlights();

    /**
     * Set Ns exponent
     * @param value Ns exponent
     */
    void setSpecularHighlights(double value);

    /**
     * Get illum value (Illumination Model)
     * @return
     */
    Integer getIlluminationModel();

    /**
     * Set illum value
     * @param value illumination model index
     */
    void setIlluminationModel(int value);

    /**
     * Get Tf value (Transmission filter)
     * @return Tf exponent
     */
    Double getTransmissionFilter();

    /**
     * Set Tf exponent
     * @param value Tf exponent
     */
    void setTransmissionFilter(double value);



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
