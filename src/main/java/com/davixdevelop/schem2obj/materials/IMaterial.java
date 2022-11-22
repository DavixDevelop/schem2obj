package com.davixdevelop.schem2obj.materials;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;

public interface IMaterial {


    /**
     * Get Ke value (Emission Strength)
     * @return
     */
    Double getEmissionStrength();
    void setEmissionStrength(Double value);

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

    /**
     *
     * @return Path to the diffuse texture file
     */
    String getDiffuseTexturePath();

    /**
     * Set the path to the diffuse texture file
     * @param texturePath Relative path to the diffuse texture file (ex. blocks/dirt)
     */
    void setDiffuseTexturePath(String texturePath);

    /**
     * Get the default unmodified diffuse image
     * @return Default unmodified diffuse image
     */
    BufferedImage getDefaultDiffuseImage();

    /**
     * Set the default unmodified diffuse image
     * @param diffuseImage The default unmodified diffuse image
     */
    void setDefaultDiffuseImage(BufferedImage diffuseImage);

    /**
     * Get the output diffuse image (ready for export)
     * @return The output diffuse image (ready for export)
     */
    BufferedImage getDiffuseImage();

    /**
     * Set the output diffuse image (ready for export)
     * @param diffuseImage The output diffuse image (ready for export)
     */
    void setDiffuseImage(BufferedImage diffuseImage);

    /**
     * Checks if the diffuse image can stored in memory.
     * If not it means the diffuse image can't be stored in memory with other diffuse images
     * Ex. a compressed resource pack can have all diffuse images loaded into memory at once,
     * while a un zipped resource pack can't have all diffuse images loaded into memory,
     * to avoid the memory filling up (ex. a x512 resource pack needs to be unzipped)
     * @return True if it can be stored in memory, else not
     */
    Boolean storeDiffuseImage();

    ArrayList<String> toMTL(String textureFolder);

    void setTransparency(boolean transparency);
    boolean hasTransparency();

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
