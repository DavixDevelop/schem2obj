package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.utilities.ImageUtility;
import com.davixdevelop.schem2obj.utilities.Utility;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontCollection;
import com.davixdevelop.schem2obj.wavefront.material.IMaterial;
import com.davixdevelop.schem2obj.wavefront.material.Material;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class GrassBlockWavefrontObject extends BlockWavefrontObject {

    private static HashMap<BlockState.Variant, IWavefrontObject> RANDOM_VARIANTS = new HashMap<>();
    //To mark if grass_top and grass_side was colored based on the biome grass color
    //ToDo: Add option to choose biome colors
    private static boolean NORMAL_MATERIAL_COLORED;
    //To mark if grass_top and grass_side was colored to white and was sawed as snowy_grass_top and snowy_grass_side
    private static boolean SNOWY_MATERIAL_GENERATED;

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace);

        //Get the model/models the block uses based on the BlockState
        BlockModel[] blockModels = Constants.BLOCK_MODELS.getBlockModel(blockNamespace, blockState);

        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);

        //Color the grass top with biome snow color and create new copy of it
        if(!SNOWY_MATERIAL_GENERATED){
            IMaterial grass_top = Constants.BLOCK_MATERIALS.getMaterial("blocks/grass_top");


            Constants.BLOCK_MATERIALS.setMaterial("blocks/snowy_grass_top", grass_top.clone());

            IMaterial snowy_grass_top = Constants.BLOCK_MATERIALS.getMaterial("blocks/snowy_grass_top");
            snowy_grass_top.setName("snowy_grass_top");
            snowy_grass_top.setDiffuseTextureName("snowy_grass_top");
            snowy_grass_top.setDiffuseImage(ImageUtility.colorImage(grass_top.getDiffuseImage(), Constants.SNOW_COLOR));
        }

        //Color the grass top and sides with biome grass color
        if(!NORMAL_MATERIAL_COLORED){
            IMaterial grass_top = Constants.BLOCK_MATERIALS.getMaterial("blocks/grass_top");
            IMaterial grass_side = Constants.BLOCK_MATERIALS.getMaterial("blocks/grass_side");
            IMaterial grass_side_overlay = Constants.BLOCK_MATERIALS.getMaterial("blocks/grass_side_overlay");

            //Color the gray grass top with the biome grass color
            BufferedImage coloredTop = ImageUtility.colorImage(grass_top.getDiffuseImage(), Constants.BIOME_GRASS_COLOR);

            //Set grass top diffuse image to colored top
            grass_top.setDiffuseImage(coloredTop);

            //Color the gray grass side overlay with the biome grass color
            BufferedImage colored_side_overlay = ImageUtility.colorImage(grass_side_overlay.getDiffuseImage(), Constants.BIOME_GRASS_COLOR);

            //Combine the grass side and colored overlay
            BufferedImage combinedOverlay = ImageUtility.colorAndCombineImages(grass_side.getDiffuseImage(),colored_side_overlay);
            grass_side.setDiffuseImage(combinedOverlay);
        }

        if(blockNamespace.getData().get("snowy").equals("false") && blockState.isRandomVariants()){
            if(!RANDOM_VARIANTS.containsKey(variants.get(0)))
            {
                toObj(blockModels, variants, blockNamespace);
                getMaterialFaces().remove("blocks/grass_side_overlay");

                for(String orientation : getBoundingFaces().keySet()){
                    HashMap<String, ArrayList<Integer>> materialFaces = getBoundingFaces().get(orientation);
                    if(materialFaces.containsKey("blocks/grass_side_overlay"))
                        materialFaces.remove("blocks/grass_side_overlay");
                }

                RANDOM_VARIANTS.put(variants.get(0), this);
            }else{
                IWavefrontObject variantObject = RANDOM_VARIANTS.get(variants.get(0));
                super.copy(variantObject);
            }

            return false;
        }

        toObj(blockModels, variants,blockNamespace);

        ArrayList<ArrayList<Integer[]>> topFaces = getMaterialFaces().get("grass_top");
        getMaterialFaces().remove("grass_top");
        getMaterialFaces().put("snowy_grass_top",topFaces);

        for(String orientation : getBoundingFaces().keySet()){
            HashMap<String, ArrayList<Integer>> materialFaces = getBoundingFaces().get(orientation);
            if(materialFaces.containsKey("grass_top")){
                ArrayList<Integer> faceIndexes = materialFaces.get("grass_top");
                materialFaces.remove("grass_top");
                materialFaces.put("snowy_grass_top", faceIndexes);
            }
        }

        return true;
    }
}
