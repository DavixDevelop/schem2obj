package com.davixdevelop.schem2obj.wavefront.material;

import com.davixdevelop.schem2obj.utilities.ImageUtility;
import com.davixdevelop.schem2obj.utilities.Utility;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Represents a SEUS Material (PBR Material)
 * @author DavixDevelop
 */
public class SEUSMaterial extends Material {
    private String resourcePath;

    private String normalsTexture;
    private String specularTexture;

    private BufferedImage customDiffuse;

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

    @Override
    public ArrayList<String> toMat(String textureFolder) {
        Path diffuseTextureOut = Paths.get(textureFolder, getDiffuseTextureName() + ".png");

        String textureFolderName = diffuseTextureOut.getParent().toFile().getName();

        InputStream assetStream = getDiffuseImage();

        if(assetStream == null)
            return null;

        ImageUtility.copyImageToFile(assetStream, diffuseTextureOut.toFile().toString());

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

        if(getLightValue() > 0.0){
            matLines.add(String.format("Ke %f %f %f",getLightValue() / 16, getLightValue() / 16, getLightValue() / 16));
        }
        matLines.add(String.format("map_Ka %s/%s.png", textureFolderName, getDiffuseTextureName()));
        matLines.add("interpolateMode NEAREST_MAGNIFICATION_TRILINEAR_MIPMAP_MINIFICATION");
        matLines.add(String.format("map_Kd %s/%s.png", textureFolderName, getDiffuseTextureName()));
        if(getName().contains("glass") || getName().contains("leaves"))
            matLines.add(String.format("map_d %s/%s.png", textureFolderName, getDiffuseTextureName()));
        if(getLightValue() > 0.0){
            if(!hasSpec)
                matLines.add(String.format("map_Ke %s/%s.png", textureFolderName, getDiffuseTextureName()));
            else
                matLines.add(String.format("map_Ke %s/%s_e.png", textureFolderName, WavefrontUtility.textureName(specularTexture).replace("_s","")));
        }

        //If material has normal define it
        if(hasNormal){
            matLines.add(String.format("map_bump -bm 1.0 %s/%s.png", textureFolderName, WavefrontUtility.textureName(normalsTexture)));
        }

        if(hasSpec){
            matLines.add(String.format("map_Pr %s/%s_r.png", textureFolderName, WavefrontUtility.textureName(specularTexture).replace("_s","")));
            matLines.add(String.format("map_Pm %s/%s_m.png", textureFolderName, WavefrontUtility.textureName(specularTexture).replace("_s","")));
        }


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
