package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.biomes.Biome;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ImageUtility;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class VineCubeModel extends BlockCubeModel {

    //To mark if vine was colored based on the biomes foliage color
    private static boolean VINE_MATERIAL_COLORED;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Get BlockState for the leaves block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getDefaultBlockState().getName());

        //Get the variant for the vine block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(namespace);

        VariantModels[] vineModels = new VariantModels[variants.size()];

        //Get the model/models for the vine
        for(int c = 0; c < variants.size(); c++)
            vineModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));

        //Modify the vine material if it was not yet modified
        modifyVineMaterial(vineModels, namespace);

        //Generate the cube model for the vine block variant
        fromVariantModel(namespace.getDefaultBlockState().getName(), namespace, vineModels);

        return true;
    }

    public static void  modifyVineMaterial(VariantModels[] vineModels, Namespace blockNamespace){
        if(!VINE_MATERIAL_COLORED){
            String vine_texture = CubeModelUtility.modelsToMaterials(vineModels, blockNamespace).entrySet().stream().findFirst().get().getValue().
                    entrySet().stream().findFirst().get().getValue();

            IMaterial vine_material = Constants.BLOCK_MATERIALS.getMaterial(vine_texture);

            //Get the biome of the colum
            Biome biome = Constants.LOADED_SCHEMATIC.getBiome(blockNamespace.getPosition("X"), blockNamespace.getPosition("Z"));

            BufferedImage vineImage = ImageUtility.colorImage(vine_material.getDefaultDiffuseImage(), biome.getFoliageColor());

            //Overlay texture with purple color if biome is swampland
            if(biome.getResource().contains("swampland"))
                vineImage = ImageUtility.colorColoredImage(vineImage, Constants.SWAMPLAND_PURPLE_OVERLAY);

            vine_material.setDiffuseImage(vineImage);

            //Set the material options for vine
            vine_material.setSpecularHighlights(0.0);
            vine_material.setSpecularColor(0.0);
            vine_material.setIlluminationModel(2);

            vine_material.setTransparency(true);

            VINE_MATERIAL_COLORED = true;
        }
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new VineCubeModel();
        clone.copy(this);

        return clone;
    }
}
