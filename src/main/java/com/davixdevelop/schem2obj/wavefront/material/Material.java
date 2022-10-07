package com.davixdevelop.schem2obj.wavefront.material;

import com.davixdevelop.schem2obj.utilities.ImageUtility;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;

/**Default Material that only uses the defuse texture
 * @author DavixDevelop
 */
public class Material implements IMaterial {
    private String diffuseTextureName;
    private String diffuseTexturePath;
    private double lightValue;
    private String name;

    private BufferedImage customDiffuse;

    public Material() {
        this.diffuseTextureName = null;
        this.diffuseTexturePath = null;
        this.lightValue = 0.0;
    }

    @Override
    public String getDiffuseTextureName() {
        return diffuseTextureName;
    }

    @Override
    public void setDiffuseTextureName(String textureName) {
        this.diffuseTextureName = textureName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDiffuseTexturePath() {
        return diffuseTexturePath;
    }

    @Override
    public void setDiffuseTexturePath(String texture) {
        this.diffuseTexturePath = texture;
    }

    @Override
    public InputStream getDiffuseImage() {
        if(customDiffuse != null){
            return ImageUtility.bufferedImageToInputStream(customDiffuse);
        }


        String assetMaterial = String.format("minecraft/textures/%s.png", getDiffuseTexturePath());

        InputStream assetStream = this.getClass().getClassLoader().getResourceAsStream("assets/" + assetMaterial);

        return assetStream;
    }

    @Override
    public void setDiffuseImage(BufferedImage diffuseImage) {
        customDiffuse = diffuseImage;
    }

    @Override
    public double getLightValue() {
        return lightValue;
    }

    @Override
    public void setLightValue(double lightValue) {
        this.lightValue = lightValue;
    }

    @Override
    public ArrayList<String> toMat(String textureFolder) {

        Path diffuseTextureOut = Paths.get(textureFolder, getDiffuseTextureName() + ".png");

        InputStream assetStream = getDiffuseImage();

        ImageUtility.copyImageToFile(assetStream, diffuseTextureOut.toFile().toString());

        //To store each line that defines a new material
        ArrayList<String> matLines = new ArrayList<>();

        matLines.add(String.format("newmtl %s", getName()));

        if(getLightValue() > 0.0){
            matLines.add(String.format(Locale.ROOT, "Ke %f %f %f",getLightValue(), getLightValue(), getLightValue()));
        }
        matLines.add(String.format("map_Ka %s/%s.png", diffuseTextureOut.getParent().toFile().getName(), getDiffuseTextureName()));
        matLines.add(String.format("map_Kd %s/%s.png", diffuseTextureOut.getParent().toFile().getName(), getDiffuseTextureName()));
        if(getName().contains("glass") || getName().contains("leaves") || getName().equals("slime"))
            matLines.add(String.format("map_d %s/%s.png", diffuseTextureOut.getParent().toFile().getName(), getDiffuseTextureName()));
        if(getLightValue() > 0.0){
            matLines.add(String.format("map_Ke %s/%s.png", diffuseTextureOut.getParent().toFile().getName(), getDiffuseTextureName()));
        }


        return matLines;
    }

    @Override
    public IMaterial clone() {
        Material material = new Material();
        material.copy(this);

        return material;
    }

    @Override
    public void copy(IMaterial clone) {
        Material cloneMaterial = (Material) clone;
        customDiffuse = cloneMaterial.customDiffuse;
        diffuseTextureName = cloneMaterial.diffuseTextureName;
        diffuseTexturePath = cloneMaterial.diffuseTexturePath;
        lightValue = cloneMaterial.lightValue;
        name = cloneMaterial.name;
    }


}
