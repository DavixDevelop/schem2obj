package com.davixdevelop.schem2obj.cubemodels.blocks;


import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.biomes.Biome;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ColorUtility;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DoublePlantCubeModel extends BlockCubeModel {

    static Set<String> DOUBLE_PLANT_MATERIALS_MODIFIED = new HashSet<>();
    @Override
    public boolean fromNamespace(Namespace namespace) {
        String variant = namespace.getDefaultBlockState().getName();
        if(variant.equals("double_grass") || variant.equals("double_fern")){
            String half = namespace.getDefaultBlockState().getData("half");
            String texture = variant.substring(7);
            String part = half.equals("lower") ? "bottom" : "top";
            if(!DOUBLE_PLANT_MATERIALS_MODIFIED.contains(part + texture)){
                modifyDoublePlantMaterial(part, texture, namespace);
            }
        }

        return super.fromNamespace(namespace);
    }

    public void modifyDoublePlantMaterial(String part, String texture, Namespace namespace){
        String materialName = String.format("blocks/double_plant_%s_%s", texture, part);
        CubeModelUtility.generateOrGetMaterial(materialName, namespace);
        IMaterial material = Constants.BLOCK_MATERIALS.getMaterial(materialName);
        try{
            //Get the biome of the column
            Biome biome = Constants.LOADED_SCHEMATIC.getBiome(namespace.getPosition("X"), namespace.getPosition("Z"));

            BufferedImage bufferedImage = material.getDefaultDiffuseImage();
            bufferedImage = ImageUtility.colorImage(bufferedImage, biome.getFoliageColor());

            //Overlay texture with purple color if biome is swampland
            if(biome.getResource().contains("swampland"))
                bufferedImage = ImageUtility.colorColoredImage(bufferedImage, Constants.SWAMPLAND_PURPLE_OVERLAY);

            material.setDiffuseImage(bufferedImage);

            DOUBLE_PLANT_MATERIALS_MODIFIED.add(part + texture);
        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
        }
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        if(namespace.getDefaultBlockState().getData("half").equals("upper")){
            if(namespace.getPosition("Y") - 1 >= 0) {
                //Check if lower block is of type double plant
                Namespace lowerBlock = Constants.LOADED_SCHEMATIC.getNamespace(namespace.getPosition("X"), namespace.getPosition("Y") - 1,namespace.getPosition("Z"));
                if(lowerBlock.getType().equals("double_plant"))
                    namespace.getDefaultBlockState().setName(lowerBlock.getDefaultBlockState().getName());
            }
        }else
        {
            if(namespace.getPosition("Y") + 1 < Constants.LOADED_SCHEMATIC.getHeight()){
                //Check if upper block is of type double plant
                Namespace upperBlock = Constants.LOADED_SCHEMATIC.getNamespace(namespace.getPosition("X"), namespace.getPosition("Y") + 1,namespace.getPosition("Z"));
                if(upperBlock.getType().equals("double_plant"))
                    namespace.getDefaultBlockState().setData("facing", upperBlock.getDefaultBlockState().getData("facing"));
            }
        }

        key.put("BlockName", namespace.getType());
        key.put("PlantVariant", namespace.getDefaultBlockState().getName());
        key.put("half", namespace.getDefaultBlockState().getData("half"));
        key.put("facing", namespace.getDefaultBlockState().getData("facing"));

        return key;
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clonePlant = new DoublePlantCubeModel();
        clonePlant.copy(this);

        return clonePlant;
    }
}
