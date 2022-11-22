package com.davixdevelop.schem2obj.materials;

import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private Double Ke = 0.0;
    private Double Ka = 0.2;
    private Double Ks;
    private Double Ns;
    private Integer illum;
    private Double Tf;

    private String name;

    private BufferedImage defaultDiffuseImage;
    private BufferedImage customDiffuseImage;

    private boolean transparency = false;

    public Material() {
        this.diffuseTexturePath = null;
        this.Ke = 0.0;
        this.Ka = 0.2;
    }

    /**
     * Create new Vanilla Material from diffuse file name
     * @param materialName The name of the material, ex: blocks/dirt, entity/blue-bed...
     * @param diffuseTexturePath The path to the texture, ex blocks/dirt, entity/bed/blue
     */
    public Material(String materialName, String diffuseTexturePath){
        setName(CubeModelUtility.textureName(materialName));
        //Set the relative path to the diffuse texture, ex entity/bed/blue.png -> entity/bed/blue
        setDiffuseTexturePath(diffuseTexturePath);
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
    public BufferedImage getDefaultDiffuseImage() {
        if(defaultDiffuseImage != null){
            return defaultDiffuseImage;
            //return ImageUtility.bufferedImageToInputStream(diffuseImage);
        }

        //Get the relative path to the diffuse texture, ex. textures/entity/bed/blue.png
        String diffusePath = ResourceLoader.getResourcePath("textures", getDiffuseTexturePath(), "png");

        try{
            InputStream assetStream = ResourceLoader.getResource(diffusePath);

            BufferedImage bufferedImage = ImageUtility.toBuffedImage(assetStream);

            //Only store the default diffuse image, if the the resource can be stored in memory
            if(defaultDiffuseImage == null && ResourceLoader.storeInMemory(diffusePath)){
                defaultDiffuseImage = bufferedImage;
            }

            return bufferedImage;

        }catch (Exception ex){
            LogUtility.Log(String.format("Could not find %s in resources", getDiffuseTexturePath()));
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    @Override
    public void setDefaultDiffuseImage(BufferedImage diffuseImage) {
        this.defaultDiffuseImage = diffuseImage;
    }

    @Override
    public BufferedImage getDiffuseImage() {
        if(customDiffuseImage != null)
            return customDiffuseImage;
        else
            return getDefaultDiffuseImage();
    }

    @Override
    public void setDiffuseImage(BufferedImage diffuseImage) {
        customDiffuseImage = diffuseImage;
    }

    @Override
    public Boolean storeDiffuseImage() {
        String diffusePath = ResourceLoader.getResourcePath("textures", getDiffuseTexturePath(), "png");
        return ResourceLoader.storeInMemory(diffusePath);
    }

    @Override
    public Double getEmissionStrength() {
        return Ke;
    }

    @Override
    public void setEmissionStrength(Double lightValue) {
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


        BufferedImage bufferedImage = getDiffuseImage();

        ImageUtility.copyImageToFile(bufferedImage, diffuseTextureOut.toFile().toString());

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

        defaultDiffuseImage = cloneMaterial.defaultDiffuseImage;
        customDiffuseImage = cloneMaterial.customDiffuseImage;
        diffuseTexturePath = cloneMaterial.diffuseTexturePath;
        name = cloneMaterial.name;

        transparency = cloneMaterial.transparency;

        Ke = cloneMaterial.Ke;
        Ka = cloneMaterial.Ka;
        Ks = cloneMaterial.Ks;
        Ns = cloneMaterial.Ns;
        illum = cloneMaterial.illum;
        Tf = cloneMaterial.Tf;
    }


}
