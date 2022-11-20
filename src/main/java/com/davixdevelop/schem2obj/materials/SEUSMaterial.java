package com.davixdevelop.schem2obj.materials;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
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

/**
 * Represents a SEUS Material (PBR Material)
 * @author DavixDevelop
 */
public class SEUSMaterial extends Material {
    private String resourcePath;

    private boolean hasNormalsTexture;
    private boolean hasSpecularTexture;

    private BufferedImage customDiffuse;

    //Factor to mix in "black" parts of the emission texture with the diffuse texture
    private double emissionMixFactor = Constants.EMISSION_MIX_FACTOR;

    /**
     * Create new SEUS Material from diffuse texture file name
     * @param materialName The name of the material, ex: dirt, blue-bed...
     * @param textureFile The path to the texture, ex blocks/dirt.png, entity/bed/blue.png
     * @param resourcePath The path to the resource pack
     */
    public SEUSMaterial(String materialName, String textureFile, String resourcePath){
        this.resourcePath = resourcePath;
        Path textureFilePath = Paths.get(resourcePath, "assets","minecraft","textures",textureFile);

        setName(CubeModelUtility.textureName(materialName));

        //Set the relative path to the diffuse texture, ex entity/bed/blue.png -> entity/bed/blue
        setDiffuseTexturePath(textureFile.replace(".png",""));

        //Set the name of the normals texture. Ex dirt_n.png, or blue_n.png
        String normalsTextureName = String.format("%s_n.png", CubeModelUtility.textureName(getDiffuseTexturePath()));
        this.hasNormalsTexture = Paths.get(textureFilePath.getParent().toFile().toString(), normalsTextureName).toFile().exists();

        //Set the name of specular texture. Ex dirt_s.png, or blue_s.png
        String specularTextureName = String.format("%s_s.png", CubeModelUtility.textureName(getDiffuseTexturePath()));
        this.hasSpecularTexture = Paths.get(textureFilePath.getParent().toFile().toString(), specularTextureName).toFile().exists();

    }

    public SEUSMaterial(){}

    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    public InputStream getDiffuseImage() {
        if(customDiffuse != null)
            return ImageUtility.bufferedImageToInputStream(customDiffuse);
        else{
            //Get the path to the external diffuse texture, ex. <resourcePath>/assets/minecraft/textures/entity/bed/blue.png
            String externalDiffuse = ImageUtility.getExternalTexture(resourcePath, getDiffuseTexturePath()).toFile().toString();

            try{
                InputStream externalAsset = new FileInputStream(externalDiffuse);

                return externalAsset;
            }catch (FileNotFoundException ex){
                LogUtility.Log("Could not find " + getDiffuseTexturePath() + ".png in resource pack");
                LogUtility.Log(ex.getMessage());
                return null;
            }

        }
    }

    @Override
    public void setDiffuseImage(BufferedImage diffuseImage) {
        customDiffuse = diffuseImage;
    }

    public void setEmissionMixFactor(double emissionMixFactor) {
        this.emissionMixFactor = emissionMixFactor;
    }

    @Override
    public ArrayList<String> toMTL(String textureFolder) {
        //Set the path to the output diffuse texture
        Path diffuseTextureOut = Paths.get(textureFolder, getName() + ".png");

        String textureFolderName = diffuseTextureOut.getParent().toFile().getName();

        InputStream assetStream = getDiffuseImage();

        ImageUtility.copyImageToFile(assetStream, diffuseTextureOut.toFile().toString());

        boolean hasNormal = false;

        //Copy normal image to texture folder
        if(hasNormalsTexture){
            //Set the path to the output normals file -> ex <textureFolder>/blue-bed_n.png
            String normalTextureOut = Paths.get(textureFolder, String.format("%s_n.png",getName())).toFile().toString();

            try{
                //Get path to normal texture in resource pack. Ex. <resourcePack>/assets/minecraft/textures/entity/bed/blue_n.png
                String externalNormal = ImageUtility.getExternalTexture(resourcePath, String.format("%s_n", getDiffuseTexturePath())).toFile().toString();
                InputStream externalAsset = new FileInputStream(externalNormal);

                //Copy normal texture to output texture folder
                ImageUtility.copyImageToFile(externalAsset, normalTextureOut);

                hasNormal = true;


            }catch (FileNotFoundException ex){
                LogUtility.Log("Could not find normal texture" + getDiffuseTexturePath() + "_n.png in resource pack");
                LogUtility.Log(ex.getMessage());
                return null;
            }
        }

        boolean hasSpec = false;

        //Check if material has specular texture
        if(hasSpecularTexture){
            try{

                //Get path to specular texture in resource pack. Ex. <resourcePack>/assets/minecraft/textures/entity/bed/blue_s.png
                String externalSpecular = ImageUtility.getExternalTexture(resourcePath, String.format("%s_s", getDiffuseTexturePath())).toFile().toString();
                InputStream externalAsset = new FileInputStream(externalSpecular);

                //Get the Roughness, Mealiness and Emission image from the specular texture
                BufferedImage[] RME = ImageUtility.extractPBRFromSpec(externalAsset);

                if(RME != null){
                    for(int c =0 ; c < 3; c++){
                        String texturePBRName = String.format("%s_%s",getName(), (c == 0) ? "r" : (c == 1) ? "m" : "e");

                        if(c == 2){
                            RME[c] = ImageUtility.maskImage(getDiffuseImage(), RME[c], emissionMixFactor);
                        }

                        //Path to output texture
                        String textureOut = Paths.get(textureFolder, texturePBRName + ".png").toFile().toString();

                        //Convert buffer image to input stream
                        assetStream = ImageUtility.bufferedImageToInputStream(RME[c]);

                        //Copy input stream to output texture folder
                        ImageUtility.copyImageToFile(assetStream, textureOut);
                    }

                    hasSpec = true;
                }

            }catch (Exception ex){
                LogUtility.Log("Failed get extract the Glossiness, Mealiness and Emission texture from the specular texture " + hasSpecularTexture);
                LogUtility.Log(ex.getMessage());
            }

        }

        //To store each line that defines a new material
        ArrayList<String> matLines = new ArrayList<>();

        matLines.add(String.format("newmtl %s", getName()));

        if(getSpecularHighlights() != null)
            matLines.add(String.format(Locale.ROOT, "Ns %f", getSpecularHighlights()));

        matLines.add("Kd 1 1 1");

        if(getSpecularColor() != null)
            matLines.add(String.format(Locale.ROOT, "Ks %f %f %f", getSpecularColor(), getSpecularColor(), getSpecularColor()));


        if(getEmissionStrength() > 0.0){
            matLines.add(String.format(Locale.ROOT, "Ke %f %f %f", getEmissionStrength(), getEmissionStrength(), getEmissionStrength()));
        }
        matLines.add(String.format("map_Ka %s/%s.png", textureFolderName, getName()));
        matLines.add(String.format("map_Kd %s/%s.png", textureFolderName, getName()));
        if(hasTransparency())
            matLines.add(String.format("map_d %s/%s.png", textureFolderName, getName()));
        if(getEmissionStrength() > 0.0){
            if(!hasSpec)
                matLines.add(String.format("map_Ke %s/%s.png", textureFolderName, getName()));
            else
                matLines.add(String.format("map_Ke %s/%s_e.png", textureFolderName, getName()));
        }

        //If material has normal define it
        if(hasNormal){
            matLines.add(String.format("map_Kn %s/%s_n.png", textureFolderName, getName()));
            matLines.add(String.format("norm %s/%s_n.png", textureFolderName, getName()));
            matLines.add(String.format("map_bump -bm 1.0 %s/%s_.png", textureFolderName, getName()));
        }

        if(hasSpec){
            matLines.add(String.format("map_Pr %s/%s_r.png", textureFolderName, getName()));
            matLines.add(String.format("map_Pm %s/%s_m.png", textureFolderName, getName()));
        }

        if(getIlluminationModel() != null)
            matLines.add(String.format(Locale.ROOT, "illum %d", getIlluminationModel()));

        if(getTransmissionFilter() != null)
            matLines.add(String.format(Locale.ROOT, "Tf %f %f %f", getTransmissionFilter(), getTransmissionFilter(), getTransmissionFilter()));


        return matLines;
    }

    @Override
    public IMaterial clone() {
        SEUSMaterial seusMaterial = new SEUSMaterial();
        seusMaterial.copy(this);

        return seusMaterial;
    }

    @Override
    public void copy(IMaterial clone) {
        super.copy(clone);
        SEUSMaterial seusCopy = (SEUSMaterial)clone;
        customDiffuse = seusCopy.customDiffuse;
        resourcePath = seusCopy.resourcePath;
        hasNormalsTexture = seusCopy.hasNormalsTexture;
        hasSpecularTexture = seusCopy.hasSpecularTexture;
    }
}
