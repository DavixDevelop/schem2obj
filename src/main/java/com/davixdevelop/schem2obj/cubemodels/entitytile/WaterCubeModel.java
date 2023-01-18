package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.biomes.Biome;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ImageUtility;

import java.awt.image.BufferedImage;

/**
 * The CubeModel for the Water entity
 *
 * @author DavixDevelop
 */
public class WaterCubeModel extends LiquidCubeModel {
    public WaterCubeModel(){
        super("blocks/water_flow", "blocks/water_still", 32.0, 64.0);
        setName("Water");
    }
    public static void modifyWaterMaterial(String materialPath, Namespace namespace){
        IMaterial material = Constants.BLOCK_MATERIALS.getMaterial(materialPath);

        //Get the biome of the column
        Biome biome = Constants.LOADED_SCHEMATIC.getBiome(namespace.getPosition("X"), namespace.getPosition("Z"));
        //Color the water texture with the water color multiplier
        BufferedImage waterTexture = ImageUtility.colorColoredImage(material.getDefaultDiffuseImage(), biome.getWaterColor());

        //Overlay texture with purple color if biome is swampland
        if(biome.getResource().contains("swampland"))
            waterTexture = ImageUtility.colorColoredImage(waterTexture, Constants.SWAMPLAND_PURPLE_OVERLAY);

        //Set colored water texture to water material
        material.setDiffuseImage(waterTexture);

    }

    @Override
    public boolean isLiquidAdjacent(Namespace adjacent) {
        return (adjacent.getType().equals("water")) ||
                (adjacent.getType().equals("flowing_water"));
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new WaterCubeModel();
        clone.copy(this);

        return clone;
    }
}
