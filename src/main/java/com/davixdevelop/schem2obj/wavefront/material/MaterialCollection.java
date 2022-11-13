package com.davixdevelop.schem2obj.wavefront.material;

import com.davixdevelop.schem2obj.util.Utility;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

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

    public void registerTexturePack(String format, String resourcePack){
        //Get the path to the textures folder of the resource pack
        Path resourcePackTexturesFolder = Paths.get(resourcePack, "assets","minecraft","textures");
        //Get the texture folders inside the textures folder of the resource pack
        File[] textureFolders = resourcePackTexturesFolder.toFile().listFiles();


        Matcher matcher = null;
        //Loop through the textureFolders
        for(File textureFolder : textureFolders){
            //ToDo: For now only scan for textures in blocks
            if(!textureFolder.getName().equals("blocks"))
                continue;

            File[] textures = textureFolder.listFiles();

            String textureFolderName = textureFolder.getName();

            //Loop through the textures
            for(File texture : textures){
                //Pattern to get texture name from png texture (example grass_n.png or grass.png -> grass)
                matcher = Utility.TEXTURE_NAME_FROM_FILE.matcher(texture.getName());
                if(matcher.find()){
                    String textureName = matcher.group(1);


                    String materialName = String.format("%s/%s",textureFolderName,textureName);
                    //Check if material (texture folder + / + texture name, example blocks/grass)
                    //Is not already in memory
                    if(!containsMaterial(materialName)){
                        //Create material depending on the format (SEUS, Specular, Vanilla)
                        IMaterial material = null;

                        switch (format){
                            case "SEUS":
                                material = new SEUSMaterial(resourcePack, materialName + ".png");
                        }

                        //Put material into memory, for later use
                        modifyOtherMaterials(material);
                        materials.put(materialName, material);
                    }
                }
            }
        }
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
                material.setEmissionStrength(1);
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
