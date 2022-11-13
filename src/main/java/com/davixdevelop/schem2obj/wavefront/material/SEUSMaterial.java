package com.davixdevelop.schem2obj.wavefront.material;

import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.Utility;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

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

    private String normalsTexture;
    private String specularTexture;

    private BufferedImage customDiffuse;

    //Factor to mix in "black" parts of the emission texture with the diffuse texture
    private double emissionMixFactor = 0.5;

    /**
     * Create new SEUS Material from diffuse texture name
     * @param resourcePath
     * @param textureFile
     */
    public SEUSMaterial(String resourcePath, String textureFile){
        this.resourcePath = resourcePath;
        Path textureFilePath = Paths.get(resourcePath, "assets","minecraft","textures",textureFile);

        setName(WavefrontUtility.textureName(textureFile).replace(".png",""));
        setDiffuseTexturePath(textureFile.replace(".png",""));
        setDiffuseTextureName(WavefrontUtility.textureName(textureFile).replace(".png",""));

        String textureParentFolderName = textureFilePath.getParent().toFile().getName();

        Path normalFilePath = Paths.get(textureFilePath.getParent().toFile().toString(), String.format("%s_n.png", getDiffuseTextureName()));
        if(normalFilePath.toFile().exists())
            normalsTexture = Paths.get(textureParentFolderName, String.format("%s_n", getDiffuseTextureName())).toString();

        Path specularFilePath = Paths.get(textureFilePath.getParent().toFile().toString(), String.format("%s_s.png", getDiffuseTextureName()));
        if(specularFilePath.toFile().exists())
            specularTexture = Paths.get(textureParentFolderName, String.format("%s_s", getDiffuseTextureName())).toString();

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
            String externalDiffuse = ImageUtility.getExternalTexture(resourcePath, getDiffuseTexturePath()).toFile().toString();

            try{
                InputStream externalAsset = new FileInputStream(externalDiffuse);

                return externalAsset;
            }catch (FileNotFoundException ex){
                Utility.Log("Could not find " + getDiffuseTexturePath() + ".png in resource pack");
                Utility.Log(ex.getMessage());
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
    public ArrayList<String> toMat(String textureFolder) {
        Path diffuseTextureOut = Paths.get(textureFolder, getDiffuseTextureName() + ".png");

        String textureFolderName = diffuseTextureOut.getParent().toFile().getName();



        /*if(getDiffuseImage() == null)
            return null;*/

        InputStream assetStream = getDiffuseImage();

        ImageUtility.copyImageToFile(assetStream, diffuseTextureOut.toFile().toString());

        /*boolean hasAlpha = false;

        try{
            InputStream diffuseStream = new FileInputStream(diffuseTextureOut.toFile().toString());
            hasAlpha = ImageUtility.hasAlpha(diffuseStream);
        }catch (Exception ex){
            Utility.Log(ex.getMessage());
        }*/

        boolean hasNormal = false;

        //Copy normal image to texture folder
        if(normalsTexture != null){
            String normalTextureFile = WavefrontUtility.textureName(normalsTexture);
            String normalTextureOut = Paths.get(textureFolder, normalTextureFile + ".png").toFile().toString();
            try{
                //Get path to normal texture in resource pack
                String externalNormal = ImageUtility.getExternalTexture(resourcePath, normalsTexture).toFile().toString();
                InputStream externalAsset = new FileInputStream(externalNormal);

                //Copy normal texture to output texture folder
                ImageUtility.copyImageToFile(externalAsset, normalTextureOut);

                hasNormal = true;


            }catch (FileNotFoundException ex){
                Utility.Log("Could not find normal texture" + getDiffuseTexturePath() + "_n.png in resource pack");
                Utility.Log(ex.getMessage());
                return null;
            }
        }

        boolean hasSpec = false;

        //Check if material has specular texture
        if(specularTexture != null){
            try{
                String specularTextureFile = WavefrontUtility.textureName(specularTexture);

                //Get path to normal texture in resource pack
                String externalNormal = ImageUtility.getExternalTexture(resourcePath, specularTexture).toFile().toString();
                InputStream externalAsset = new FileInputStream(externalNormal);

                //Get the Roughness, Mealiness and Emission image from the specular texture
                BufferedImage[] RME = ImageUtility.extractPBRFromSpec(externalAsset);

                if(RME != null){
                    String rawName = specularTextureFile.replace("_s","");

                    for(int c =0 ; c < 3; c++){
                        String texturePBRName = String.format("%s_%s",rawName, (c == 0) ? "r" : (c == 1) ? "m" : "e");

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
                Utility.Log("Failed get extract the Glossiness, Mealiness and Emission texture from the specular texture " + specularTexture);
                Utility.Log(ex.getMessage());
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
        matLines.add(String.format("map_Ka %s/%s.png", textureFolderName, getDiffuseTextureName()));
        matLines.add(String.format("map_Kd %s/%s.png", textureFolderName, getDiffuseTextureName()));
        if(hasTransparency())
            matLines.add(String.format("map_d %s/%s.png", textureFolderName, getDiffuseTextureName()));
        if(getEmissionStrength() > 0.0){
            if(!hasSpec)
                matLines.add(String.format("map_Ke %s/%s.png", textureFolderName, getDiffuseTextureName()));
            else
                matLines.add(String.format("map_Ke %s/%s_e.png", textureFolderName, WavefrontUtility.textureName(specularTexture).replace("_s","")));
        }

        //If material has normal define it
        if(hasNormal){
            matLines.add(String.format("map_Kn %s/%s.png", textureFolderName, WavefrontUtility.textureName(normalsTexture)));
            matLines.add(String.format("norm %s/%s.png", textureFolderName, WavefrontUtility.textureName(normalsTexture)));
            matLines.add(String.format("map_bump -bm 1.0 %s/%s.png", textureFolderName, WavefrontUtility.textureName(normalsTexture)));
        }

        if(hasSpec){
            matLines.add(String.format("map_Pr %s/%s_r.png", textureFolderName, WavefrontUtility.textureName(specularTexture).replace("_s","")));
            matLines.add(String.format("map_Pm %s/%s_m.png", textureFolderName, WavefrontUtility.textureName(specularTexture).replace("_s","")));
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
        normalsTexture = seusCopy.normalsTexture;
        specularTexture = seusCopy.specularTexture;
    }
}
