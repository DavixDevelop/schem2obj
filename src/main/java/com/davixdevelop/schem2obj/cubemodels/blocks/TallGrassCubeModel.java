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

public class TallGrassCubeModel extends BlockCubeModel {

    static Set<String> TALL_GRASS_MATERIALS_MODIFIED = new HashSet<>();

    @Override
    public boolean fromNamespace(Namespace namespace) {

        if(namespace.getResource().equals("tall_grass") && !TALL_GRASS_MATERIALS_MODIFIED.contains("tallgrass")){
            colorTallGrassMaterial(namespace, "tallgrass");
        }else if(!TALL_GRASS_MATERIALS_MODIFIED.contains("fern")) {
            colorTallGrassMaterial(namespace, "fern");
        }

        return super.fromNamespace(namespace);
    }

    public void colorTallGrassMaterial(Namespace namespace, String texture){
        CubeModelUtility.generateOrGetMaterial("blocks/" + texture, namespace);
        IMaterial material = Constants.BLOCK_MATERIALS.getMaterial("blocks/" + texture);
        try{
            //Get the biome of the column
            Biome biome = Constants.LOADED_SCHEMATIC.getBiome(namespace.getPosition("X"), namespace.getPosition("Z"));

            BufferedImage bufferedImage = material.getDefaultDiffuseImage();
            bufferedImage = ImageUtility.colorImage(bufferedImage, biome.getFoliageColor());

            //Overlay texture with purple color if biome is swampland
            if(biome.getResource().contains("swampland"))
                bufferedImage = ImageUtility.colorColoredImage(bufferedImage, Constants.SWAMPLAND_PURPLE_OVERLAY);

            material.setDiffuseImage(bufferedImage);

            TALL_GRASS_MATERIALS_MODIFIED.add(texture);

        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
        }
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel cloneTallGrass = new TallGrassCubeModel();
        cloneTallGrass.copy(this);

        return cloneTallGrass;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getResource());

        return key;
    }
}
