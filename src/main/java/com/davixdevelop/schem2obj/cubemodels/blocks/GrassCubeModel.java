package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class GrassCubeModel extends BlockCubeModel {
    private static HashMap<BlockState.Variant, ICubeModel> RANDOM_VARIANTS = new HashMap<>();
    //To mark if grass_top and grass_side was colored based on the biome grass color
    //ToDo: Add option to choose biome colors
    private static boolean NORMAL_MATERIAL_COLORED;
    //To mark if grass_top and grass_side was colored to white and was sawed as snowy_grass_top and snowy_grass_side
    private static boolean SNOWY_MATERIAL_GENERATED;

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        Namespace grassNamespace = blockNamespace.clone();

        //Check if the above block is a snow layer
        Namespace aboveBlock = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX(), Constants.LOADED_SCHEMATIC.getPosY() + 1, Constants.LOADED_SCHEMATIC.getPosZ());
        if(aboveBlock != null){
            if(aboveBlock.getName().equals("snow_layer"))
                grassNamespace.setData("snowy", "true");
        }

        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(grassNamespace.getName());

        ArrayList<BlockState.Variant> variants = blockState.getVariants(grassNamespace);



        if(grassNamespace.getData().get("snowy").equals("false") && blockState.isRandomVariants()){
            //Color the regular grass material
            modifyRegularGrassMaterial(grassNamespace);

            if(!RANDOM_VARIANTS.containsKey(variants.get(0)))
            {
                createNormalVariant(grassNamespace, variants.get(0));
                RANDOM_VARIANTS.put(variants.get(0), this);
            }else{
                ICubeModel variantObject = RANDOM_VARIANTS.get(variants.get(0));
                super.copy(variantObject);
            }
        }else{
            //Color the snowy grass material
            modifySnowyGrassMaterial(grassNamespace);

            if(!RANDOM_VARIANTS.containsKey(variants.get(0))){
                createSnowyVariant(grassNamespace, variants.get(0));
                RANDOM_VARIANTS.put(variants.get(0), this);
            }else{
                ICubeModel snowyVariant = RANDOM_VARIANTS.get(variants.get(0));
                super.copy(snowyVariant);
            }
        }

        return false;
    }

    public void createSnowyVariant(Namespace blockNamespace, BlockState.Variant randomVariant){
        ArrayVector.MatrixRotation rotationY = null;
        if(randomVariant.getY() != null)
            rotationY = new ArrayVector.MatrixRotation(randomVariant.getY(), "Z");

        createGrassBlock("snowy_grass", "blocks/snowy_grass_top", "blocks/grass_side_snowed", blockNamespace, rotationY, randomVariant.getUvlock());
    }

    public void createNormalVariant(Namespace blockNamespace, BlockState.Variant randomVariant){
        ArrayVector.MatrixRotation rotationY = null;
        if(randomVariant.getY() != null)
            rotationY = new ArrayVector.MatrixRotation(randomVariant.getY(), "Z");

        createGrassBlock("grass", "blocks/grass_top", "blocks/grass_side", blockNamespace, rotationY, randomVariant.getUvlock());
    }

    public void createGrassBlock(String name, String top_texture, String side_texture, Namespace blockNamespace, ArrayVector.MatrixRotation rotationY, boolean uvLock){
        HashMap<String,String> modelsMaterials = new HashMap<>();
        CubeModelUtility.generateOrGetMaterial("blocks/dirt", blockNamespace);
        CubeModelUtility.generateOrGetMaterial(top_texture, blockNamespace);
        CubeModelUtility.generateOrGetMaterial(side_texture, blockNamespace);
        modelsMaterials.put("bottom", "blocks/dirt");
        modelsMaterials.put("top", top_texture);
        modelsMaterials.put("side", side_texture);

        HashMap<String, CubeElement.CubeFace> cubeFaces = new HashMap<>();
        cubeFaces.put("down", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#bottom", "down", null, null));
        cubeFaces.put("up", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#top", "up", null, null));
        cubeFaces.put("north", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#side", "north", null, null));
        cubeFaces.put("south", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#side", "south", null, null));
        cubeFaces.put("west", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#side", "west", null, null));
        cubeFaces.put("east", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#side", "east", null, null));

        CubeElement cube = new CubeElement(
                new Double[]{0.0,0.0,0.0},
                new Double[]{1.0,1.0,1.0},
                false,
                null,
                cubeFaces);

        //Convert cube to obj
        fromCubes(name, uvLock, null, rotationY, modelsMaterials, cube);
    }

    public void modifyRegularGrassMaterial(Namespace blockNamespace){
        //Color the grass top and sides with biome grass color
        if(!NORMAL_MATERIAL_COLORED){
            CubeModelUtility.generateOrGetMaterial("blocks/grass_top", blockNamespace);
            CubeModelUtility.generateOrGetMaterial("blocks/grass_side", blockNamespace);
            CubeModelUtility.generateOrGetMaterial("blocks/grass_side_overlay", blockNamespace);

            IMaterial grass_top = Constants.BLOCK_MATERIALS.getMaterial("blocks/grass_top");
            IMaterial grass_side = Constants.BLOCK_MATERIALS.getMaterial("blocks/grass_side");
            IMaterial grass_side_overlay = Constants.BLOCK_MATERIALS.getMaterial("blocks/grass_side_overlay");

            //Color the gray grass top with the biome grass color
            BufferedImage coloredTop = ImageUtility.colorImage(grass_top.getDefaultDiffuseImage(), Constants.BIOME_GRASS_COLOR);

            //Set grass top output diffuse image to colored top
            grass_top.setDiffuseImage(coloredTop);

            //Color the gray grass side overlay with the biome grass color
            BufferedImage colored_side_overlay = ImageUtility.colorImage(grass_side_overlay.getDefaultDiffuseImage(), Constants.BIOME_GRASS_COLOR);

            //Combine the grass side and colored overlay
            BufferedImage combinedOverlay = ImageUtility.overlayImage(grass_side.getDefaultDiffuseImage(),colored_side_overlay);
            grass_side.setDiffuseImage(combinedOverlay);

            grass_side.setSpecularHighlights(0.0);
            grass_side.setSpecularColor(0.0);
            grass_side.setIlluminationModel(2);

            grass_top.setSpecularHighlights(0.0);
            grass_top.setSpecularColor(0.0);
            grass_top.setIlluminationModel(2);

            Constants.BLOCK_MATERIALS.unsetUsedMaterial("blocks/grass_side_overlay");

            NORMAL_MATERIAL_COLORED = true;
        }
    }

    public void modifySnowyGrassMaterial(Namespace blockNamespace){
        //Color the grass top with biome snow color and create new copy of it
        if(!SNOWY_MATERIAL_GENERATED){
            CubeModelUtility.generateOrGetMaterial("blocks/grass_top", blockNamespace);
            IMaterial grass_top = Constants.BLOCK_MATERIALS.getMaterial("blocks/grass_top");


            Constants.BLOCK_MATERIALS.setMaterial("blocks/snowy_grass_top", grass_top.clone());

            CubeModelUtility.generateOrGetMaterial("blocks/grass_side_snowed", blockNamespace);
            IMaterial snowy_grass_side = Constants.BLOCK_MATERIALS.getMaterial("blocks/grass_side_snowed");

            IMaterial snowy_grass_top = Constants.BLOCK_MATERIALS.getMaterial("blocks/snowy_grass_top");
            snowy_grass_top.setName("snowy_grass_top");

            try {
                snowy_grass_top.setDiffuseImage(ImageUtility.colorImage(grass_top.getDefaultDiffuseImage(), Constants.SNOW_COLOR));
            }catch (Exception ex){
                LogUtility.Log("Could not create snowy grass texture");
                LogUtility.Log(ex.getMessage());
            }

            snowy_grass_top.setSpecularHighlights(0.0);
            snowy_grass_top.setSpecularColor(0.0);
            snowy_grass_top.setIlluminationModel(2);

            snowy_grass_side.setSpecularHighlights(0.0);
            snowy_grass_side.setSpecularColor(0.0);
            snowy_grass_side.setIlluminationModel(2);

            SNOWY_MATERIAL_GENERATED = true;
        }
    }
}
