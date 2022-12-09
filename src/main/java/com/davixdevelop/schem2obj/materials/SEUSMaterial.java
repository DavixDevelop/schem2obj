package com.davixdevelop.schem2obj.materials;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.awt.image.BufferedImage;
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
    private boolean hasNormalsTexture;
    private BufferedImage normalsImage;
    private boolean hasSpecularTexture;
    private BufferedImage specularImage;

    //Factor to mix in "black" parts of the emission texture with the diffuse texture
    private double emissionMixFactor = Constants.EMISSION_MIX_FACTOR;

    /**
     * Create new SEUS Material from diffuse texture file name
     * @param materialPath The path of the material, ex: blocks/dirt, entity/blue-bed...
     * @param diffuseTexturePath The relative path to the texture, ex blocks/dirt, entity/bed/blue
     */
    public SEUSMaterial(String materialPath, String diffuseTexturePath){
        setName(CubeModelUtility.textureName(materialPath));

        //Set the relative path to the diffuse texture, ex entity/bed/blue
        setDiffuseTexturePath(diffuseTexturePath);

        //Set the path name of the normals texture. Ex textures/blocks/dirt_n.png, or textures/entity/bed/blue_n.png
        String normalsTexturePath = ResourceLoader.getResourcePath("textures", String.format("%s_n", getDiffuseTexturePath()), "png");
        this.hasNormalsTexture = ResourceLoader.resourceExists(normalsTexturePath);

        //Set the path name of the specular texture. Ex textures/blocks/dirt_n.png, or textures/entity/bed/blue_n.png
        String specularTexturePath = ResourceLoader.getResourcePath("textures", String.format("%s_s", getDiffuseTexturePath()), "png");
        this.hasSpecularTexture = ResourceLoader.resourceExists(specularTexturePath);
    }

    public SEUSMaterial(){}

    public void setEmissionMixFactor(double emissionMixFactor) {
        this.emissionMixFactor = emissionMixFactor;
    }

    public BufferedImage getNormalsImage() {
        if (normalsImage != null){
            return normalsImage;
            //return ImageUtility.bufferedImageToInputStream(diffuseImage);
        }



        //Get the relative path to the normals texture, ex. textures/entity/bed/blue_n.png
        String normalsTexturePath = ResourceLoader.getResourcePath("textures", String.format("%s_n", getDiffuseTexturePath()), "png");

        try{
            InputStream assetStream = ResourceLoader.getResource(normalsTexturePath);

            BufferedImage bufferedImage = ImageUtility.toBuffedImage(assetStream);

            //Only store the normals image, if the the resource can be stored in memory
            if(normalsImage == null && ResourceLoader.storeInMemory(normalsTexturePath))
                normalsImage = bufferedImage;

            return bufferedImage;

        }catch (Exception ex){
            LogUtility.Log(String.format("Could not find %s in resources", normalsTexturePath));
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    public BufferedImage getSpecularImage() {
        if (specularImage != null){
            return specularImage;
            //return ImageUtility.bufferedImageToInputStream(diffuseImage);
        }

        //Get the relative path to the specular texture, ex. textures/entity/bed/blue_s.png
        String specularTexturePath = ResourceLoader.getResourcePath("textures", String.format("%s_s", getDiffuseTexturePath()), "png");

        try{
            InputStream assetStream = ResourceLoader.getResource(specularTexturePath);

            BufferedImage bufferedImage = ImageUtility.toBuffedImage(assetStream);

            //Only store the specular image, if the the resource can be stored in memory
            if(specularImage == null && ResourceLoader.storeInMemory(specularTexturePath))
                specularImage = bufferedImage;

            return bufferedImage;

        }catch (Exception ex){
            LogUtility.Log(String.format("Could not find %s in resources", specularTexturePath));
            LogUtility.Log(ex.getMessage());
            return null;
        }
    }

    @Override
    public ArrayList<String> toMTL(String textureFolder) {
        //Set the path to the output diffuse texture
        Path diffuseTextureOut = Paths.get(textureFolder, getName() + ".png");

        String textureFolderName = diffuseTextureOut.getParent().toFile().getName();

        BufferedImage bufferedImage = getDiffuseImage();

        ImageUtility.copyImageToFile(bufferedImage, diffuseTextureOut.toFile().toString());

        boolean hasNormal = false;

        //Copy normal image to texture folder
        if(hasNormalsTexture){
            //Set the path to the output normals file -> ex <textureFolder>/blue-bed_n.png
            String normalTextureOut = Paths.get(textureFolder, String.format("%s_n.png",getName())).toFile().toString();

            try{
                //Get normal image resource pack. Ex. <resourcePack>/assets/minecraft/textures/entity/bed/blue_n.png
                BufferedImage normalsImage = getNormalsImage();

                //Copy normal texture to output texture folder
                ImageUtility.copyImageToFile(normalsImage, normalTextureOut);
                hasNormal = true;


            }catch (Exception ex){
                LogUtility.Log("Could not find normal texture" + getDiffuseTexturePath() + "_n.png in resource pack");
                LogUtility.Log(ex.getMessage());
                return null;
            }
        }

        boolean hasSpec = false;

        //Check if material has specular texture
        if(hasSpecularTexture){
            try{
                //Get specular image in resource pack. Ex. <resourcePack>/assets/minecraft/textures/entity/bed/blue_s.png
                BufferedImage specularImage = getSpecularImage();

                //Get the Roughness, Mealiness and Emission image from the specular texture
                BufferedImage[] RME = ImageUtility.extractPBRFromSpec(specularImage);

                if(RME != null){
                    for(int c =0 ; c < 3; c++){
                        String texturePBRName = String.format("%s_%s",getName(), (c == 0) ? "r" : (c == 1) ? "m" : "e");

                        if(c == 2){
                            RME[c] = ImageUtility.maskImage(getDiffuseImage(), RME[c], emissionMixFactor);
                        }

                        //Path to output texture
                        String textureOut = Paths.get(textureFolder, texturePBRName + ".png").toFile().toString();

                        //Copy buffered image to output texture folder
                        ImageUtility.copyImageToFile(RME[c], textureOut);
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
            matLines.add(String.format("map_bump -bm 1.0 %s/%s_n.png", textureFolderName, getName()));
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
    public IMaterial duplicate() {
        SEUSMaterial seusMaterial = new SEUSMaterial();
        seusMaterial.copy(this);

        return seusMaterial;
    }

    @Override
    public void copy(IMaterial clone) {
        super.copy(clone);
        SEUSMaterial seusCopy = (SEUSMaterial)clone;
        normalsImage = seusCopy.normalsImage;
        hasNormalsTexture = seusCopy.hasNormalsTexture;
        hasSpecularTexture = seusCopy.hasSpecularTexture;
        specularImage = seusCopy.specularImage;
        emissionMixFactor = seusCopy.emissionMixFactor;
    }
}
