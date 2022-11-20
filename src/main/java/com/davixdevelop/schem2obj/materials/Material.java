package com.davixdevelop.schem2obj.materials;

import com.davixdevelop.schem2obj.util.ImageUtility;

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
    //private String diffuseTextureName;
    private String diffuseTexturePath;

    private Double Ke;
    private Double Ka;
    private Double Ks;
    private Double Ns;
    private Integer illum;
    private Double Tf;

    private String name;

    private BufferedImage customDiffuse;

    private boolean transparency = false;

    public Material() {
        this.diffuseTexturePath = null;
        this.Ke = 0.0;
        this.Ka = 0.2;
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

        //Get the path to the diffuse texture, ex. minecraft/textures/entity/bed/blue.png
        String assetMaterial = String.format("minecraft/textures/%s.png", getDiffuseTexturePath());

        InputStream assetStream = this.getClass().getClassLoader().getResourceAsStream("assets/" + assetMaterial);

        return assetStream;
    }

    @Override
    public void setDiffuseImage(BufferedImage diffuseImage) {
        customDiffuse = diffuseImage;
    }

    @Override
    public double getEmissionStrength() {
        return Ke;
    }

    @Override
    public void setEmissionStrength(double lightValue) {
        this.Ke = lightValue;
    }

    @Override
    public Double getAmbientColor() {
        return Ka;
    }

    @Override
    public void setAmbientColor(double value) {
        Ka = value;
    }

    @Override
    public Double getSpecularColor() {
        return Ks;
    }

    @Override
    public void setSpecularColor(double value) {
        Ks = value;
    }

    @Override
    public Double getSpecularHighlights() {
        return Ns;
    }

    @Override
    public void setSpecularHighlights(double value) {
        Ns = value;
    }

    @Override
    public Integer getIlluminationModel() {
        return illum;
    }

    @Override
    public void setIlluminationModel(int value) {
        illum = value;
    }

    @Override
    public Double getTransmissionFilter() {
        return Tf;
    }

    @Override
    public void setTransmissionFilter(double value) {
        Tf = value;
    }

    @Override
    public ArrayList<String> toMTL(String textureFolder) {

        Path diffuseTextureOut = Paths.get(textureFolder, getName() + ".png");

        String textureFolderName = diffuseTextureOut.getParent().toFile().getName();


        InputStream assetStream = getDiffuseImage();

        ImageUtility.copyImageToFile(assetStream, diffuseTextureOut.toFile().toString());

        //To store each line that defines a new material
        ArrayList<String> matLines = new ArrayList<>();

        matLines.add(String.format("newmtl %s", getName()));

        if(Ns != null)
            matLines.add(String.format(Locale.ROOT, "Ns %f", Ns));

        if(Ka != null)
            matLines.add(String.format(Locale.ROOT, "Ka %f %f %f", Ka, Ka, Ka));

        matLines.add("Kd 1 1 1");

        if(Ks != null)
            matLines.add(String.format(Locale.ROOT, "Ks %f %f %f", Ks, Ks, Ks));

        if(getEmissionStrength() > 0.0){
            matLines.add(String.format(Locale.ROOT, "Ke %f %f %f", getEmissionStrength(), getEmissionStrength(), getEmissionStrength()));
        }
        matLines.add(String.format("map_Ka %s/%s.png", textureFolderName, getName()));
        matLines.add(String.format("map_Kd %s/%s.png", textureFolderName, getName()));
        if(transparency)
            matLines.add(String.format("map_d %s/%s.png", textureFolderName, getName()));
        if(getEmissionStrength() > 0.0){
            matLines.add(String.format("map_Ke %s/%s.png", textureFolderName, getName()));
        }

        if(illum != null)
            matLines.add(String.format(Locale.ROOT, "illum %d", illum));

        if(Tf != null)
            matLines.add(String.format(Locale.ROOT, "Tf %f %f %f", Tf, Tf, Tf));



        return matLines;
    }

    @Override
    public void setTransparency(boolean transparency) {
        this.transparency = transparency;
    }

    @Override
    public boolean hasTransparency() {
        return transparency;
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
        diffuseTexturePath = cloneMaterial.diffuseTexturePath;
        name = cloneMaterial.name;

        Ke = cloneMaterial.Ke;
        Ka = cloneMaterial.Ka;
        Ks = cloneMaterial.Ks;
        Ns = cloneMaterial.Ns;
        illum = cloneMaterial.illum;
        Tf = cloneMaterial.Tf;
    }


}
