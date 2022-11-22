package com.davixdevelop.schem2obj.materials;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.materials.json.PackTemplate;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.LogUtility;
import com.google.gson.Gson;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MaterialCollection {
    private HashMap<String, IMaterial> materials;
    private Set<String> usedMaterials;

    public MaterialCollection(){
        materials = new HashMap<>();
        usedMaterials = new HashSet<>();
    }

    public IMaterial getMaterial(String name){
        return materials.get(name);
    }

    public void setMaterial(String name, IMaterial material){
        modifyOtherMaterials(material);
        materials.put(name, material);
        usedMaterials.add(name);
    }

    public boolean containsMaterial(String name){
        return materials.containsKey(name);
    }

    public void unsetMaterial(String name){
        usedMaterials.remove(name);
    }

    /**
     * Return the used materials names
     * @return A set of used materials
     */
    public Set<String> usedMaterials(){
        return usedMaterials;
    }

    /**
     * Parse through a list of textures file, create materials from them and store them in memory
     * @param textures A list of textures
     * @param textureFolderName The name of the texture folder, ex. blocks
     * @param format The format of the resource pack, ex. SEUS
     * @param resourcePack The path to the resource pack
     * @param entityName Optional name of the entity texture
     */
    public void parseTexturesFromPackFiles(File[] textures, String textureFolderName, String format, String resourcePack, String ...entityName){
        Matcher matcher = null;

        //Loop through the textures
        for(File texture : textures){

            //Skip files that end with _n or _s if format is SEUS to skip generating the same material 3 times (one for diffuse, normal and specular texture)
            if(format.equals("SEUS") && (texture.getName().endsWith("_n.png") || texture.getName().endsWith("_s.png")))
                continue;

            //Pattern to get texture name from png texture (example grass_n.png or grass.png -> grass)
            matcher = Constants.TEXTURE_NAME_FROM_FILE.matcher(texture.getName());
            if(matcher.find()){
                String textureName = matcher.group(1);

                //If texture is a block the material name is blocks/<texture name>
                //If texture is a entity the material name is entity/<texture name>-<entity name>
                String materialName = (entityName.length > 0) ? String.format("%s/%s-%s",textureFolderName,textureName, entityName[0]) :
                        String.format("%s/%s",textureFolderName,textureName);

                String textureFilePath = (entityName.length > 0) ? String.format("%s/%s/%s.png",textureFolderName,entityName[0],textureName) :
                        String.format("%s/%s.png",textureFolderName,textureName);

                //Create material depending on the format (SEUS, Specular, Vanilla)
                IMaterial material = null;

                switch (format){
                    case "SEUS":
                        material = new SEUSMaterial(materialName, textureFilePath, resourcePack);
                        break;
                    case "Vanilla":
                        material = new Material(materialName, textureFilePath, resourcePack);
                        break;
                }

                //Put material into memory, for later use
                modifyOtherMaterials(material);
                materials.put(materialName, material);

            }
        }
    }

    /**
     * Get material from a entry and store it in memory
     * @param compressedFile ZipFile to get the textures from
     * @param name Then name of the texture (ex. blue.png)
     * @param format The format of the material (SEUS, Vanilla, Specular...)
     * @param textureType The name of the folder the texture lies in (ex. assets/minecraft/textures/entity/bed -> entity )
     * @param folderPath The full path to the folder of the texture (ex. assets/minecraft/textures/entity/bed)
     * @param entityName Optional name of the entity (ex. bed)
     */
    public void parseMaterialFromEntry(ZipFile compressedFile, String name, String format, String textureType, String folderPath, String ...entityName){
        //Skip entries that end with _n or _s if format is SEUS to skip generating the same material 3 times (one for diffuse, normal and specular texture)
        if(format.equals("SEUS") && (name.endsWith("_n.png") || name.endsWith("_s.png")))
            return;

        //Pattern to get texture name from png texture (example grass_n.png or grass.png -> grass)
        Matcher matcher = Constants.TEXTURE_NAME_FROM_FILE.matcher(name);
        if (matcher.find()) {

            String textureName = matcher.group(1);

            //If texture is a block the material name is blocks/<texture name>
            //If texture is a entity the material name is entity/<texture name>-<entity name>
            String materialName = (entityName.length > 0) ? String.format("%s/%s-%s", textureType, textureName, entityName[0]) :
                    String.format("%s/%s", textureType, textureName);

            String textureFilePath = (entityName.length > 0) ? String.format("%s/%s/%s.png", textureType, entityName[0], textureName) :
                    String.format("%s/%s.png", textureType, textureName);

            //Check if material (texture folder + / + texture name, example blocks/grass)
            //Is not already in memory

            //Create material depending on the format (SEUS, Specular, Vanilla)
            IMaterial material = null;

            String diffuseImageName = String.format("%s/%s.png",folderPath, textureName);
            BufferedImage diffuseImage = null;

            try{
                //Get the diffuse image
                ZipEntry diffuseEntry = compressedFile.getEntry(diffuseImageName);
                InputStream diffuseInputStream = compressedFile.getInputStream(diffuseEntry);
                diffuseImage = ImageUtility.toBuffedImage(diffuseInputStream);

            }catch (Exception ex){
                LogUtility.Log("Failed to read diffuse texture");
                return;
            }


            switch (format) {
                case "SEUS":
                    try {
                        BufferedImage normalImage = null;
                        BufferedImage specularImage = null;

                        //Check if material has normal texture
                        String normalImageName = String.format("%s/%s_n.png", folderPath, textureName);
                        ZipEntry normalsEntry = compressedFile.getEntry(normalImageName);
                        if (normalsEntry != null){
                            InputStream normalInputStream = compressedFile.getInputStream(normalsEntry);
                            normalImage = ImageUtility.toBuffedImage(normalInputStream);
                        }

                        //Check if material has specular texture
                        String specularImageName = String.format("%s/%s_s.png", folderPath, textureName);
                        ZipEntry specularEntry = compressedFile.getEntry(specularImageName);
                        if (specularEntry != null) {
                            InputStream specularInputStream = compressedFile.getInputStream(specularEntry);
                            specularImage = ImageUtility.toBuffedImage(specularInputStream);
                        }

                        material = new SEUSMaterial(materialName, textureFilePath, diffuseImage, normalImage, specularImage);
                    } catch (Exception ex) {
                        LogUtility.Log(String.format("Failed to create SUES material from: %s", textureName));
                        LogUtility.Log(ex.getMessage());
                    }
                    break;
                case "Vanilla":
                    try {
                        material = new Material(materialName, textureFilePath, diffuseImage);

                    } catch (Exception ex) {
                        LogUtility.Log(String.format("Failed to create Vanilla material from: %s", textureName));
                        LogUtility.Log(ex.getMessage());
                    }
                    break;
            }

            //Put material into memory, for later use
            modifyOtherMaterials(material);
            materials.put(materialName, material);
        }
    }

    public PackTemplate getPackMeta(InputStream packMetaInputStream){
        try {
            //Get reader for pack.mcmeta
            Reader reader = new InputStreamReader(packMetaInputStream);
            //Deserialize json
            PackTemplate packMetaJson = new Gson().fromJson(reader, PackTemplate.class);

            reader.close();

            return packMetaJson;
        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
        }

        return null;
    }

    public static void modifyOtherMaterials(IMaterial material){

        material.setIlluminationModel(2);
        material.setSpecularHighlights(0.0);
        material.setSpecularColor(0.0);

        if(material.getName().contains("glazed")){
            material.setSpecularHighlights(204);
            material.setAmbientColor(0.16);
            return;
        }

        /*if(material.getName().contains("glass") ||
                material.getName().contains("leaves") ||
                material.setAl
                material.getName().equals("iron_bars"))
            material.setAlpha(true);*/

        switch (material.getName()){
            case "oak_planks":
            case "iron_block":
            case "redstone_block":
            case "brick":
            case "ice_packed":
            case "planks_spruce":
            case "planks_jungle":
            case "planks_birch":
            case "planks_acacia":
            case "planks_big_oak":
            case "stone_slab_top":
            case "stone_slab_side":
            case "door_iron_upper":
            case "door_iron_lower":
                material.setSpecularHighlights(127.5);
                material.setSpecularColor(0.1);
                break;
            case "gold_block":
            case "obsidian":
            case "lapis_block":
            case "emerald_block":
            case "quartz_block_bottom":
            case "quartz_block_side":
            case "quartz_block_top":
            case "quartz_block_chiseled_top":
            case "quartz_block_chiseled":
            case "quartz_block_lines":
            case "quartz_block_lines_top":
                material.setSpecularHighlights(204.0);
                material.setSpecularColor(0.16);
                break;
            case "diamond_block":
                material.setSpecularHighlights(229.5);
                material.setSpecularColor(0.18);
                break;
            case "ice":
                material.setSpecularHighlights(127.5);
                material.setSpecularColor(0.03);
                material.setIlluminationModel(4);
                material.setTransmissionFilter(0.387);
                break;
            case "clay":
                material.setSpecularHighlights(140.25);
                material.setSpecularColor(0.11);
                break;
            case "glowstone":
                material.setEmissionStrength(1.0);
                break;
            case "purpur_block":
            case "purpur_pillar":
            case "purpur_pillar_top":
                material.setSpecularHighlights(165.75);
                material.setSpecularColor(0.13);
                break;
            case "frosted_ice_0":
            case "frosted_ice_1":
            case "frosted_ice_2":
            case "frosted_ice_3":
                material.setSpecularHighlights(127.5);
                material.setSpecularColor(0.1);
                material.setIlluminationModel(4);
                material.setTransmissionFilter(0.387);
                break;
            case "slime":
                material.setSpecularHighlights(0);
                material.setAmbientColor(0.03);
                material.setIlluminationModel(4);
                material.setTransmissionFilter(0.3);
                break;
            case "stone_andesite_smooth":
            case "stone_diorite_smooth":
            case "stone_granite_smooth":
                material.setSpecularHighlights(216.75);
                material.setAmbientColor(0.17);
                break;
            case "fire_layer_0":
            case "fire_layer_1":
                material.setEmissionStrength(15.0 / 16.0);
            case "water_still":
                material.setSpecularHighlights(216.75);
                material.setSpecularColor(0.03);
                material.setIlluminationModel(4);
                material.setTransmissionFilter(0.465);
                material.setTransparency(true);
            case "water_flow":
                material.setSpecularHighlights(216.75);
                material.setSpecularColor(0.03);
                material.setIlluminationModel(4);
                material.setTransmissionFilter(0.465);
                material.setTransparency(true);



        }
    }
}
