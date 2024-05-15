package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.biomes.Biome;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ColorUtility;
import com.davixdevelop.schem2obj.util.ImageUtility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * The CubeModel for the Leaves block
 *
 * @author DavixDevelop
 */
public class LeavesCubeModel extends BlockCubeModel {
    public static Set<String> MODIFIED_LEAVES_MATERIALS = new HashSet<>();

    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Get BlockState for the leaves block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getDefaultBlockState().getName());

        //Get the variant for the glass block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(namespace);

        VariantModels[] leavesModels = new VariantModels[variants.size()];

        //Get the models for the glass
        for(int c = 0; c < variants.size(); c++)
            leavesModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));


        //Modify the leaves materials if they were not yet modified
        modifyLeavesMaterials(leavesModels, namespace);

        //Generate the cube model for the leaves block variant
        fromVariantModel(namespace.getDefaultBlockState().getName(), namespace, leavesModels);

        return true;
    }

    public static void modifyLeavesMaterials(VariantModels[] leavesModel, Namespace blockNamespace){
        //Extract the texture the leaves use
        String leaves_texture = CubeModelUtility.modelsToMaterials(leavesModel, blockNamespace).entrySet().stream().findFirst().get().getValue().
                entrySet().stream().findFirst().get().getValue();
        if(!MODIFIED_LEAVES_MATERIALS.contains(leaves_texture)){

            IMaterial leaves_material = Constants.BLOCK_MATERIALS.getMaterial(leaves_texture);

            //Get the biome of the column
            Biome biome = Constants.LOADED_SCHEMATIC.getBiome(blockNamespace.getPosition("X"), blockNamespace.getPosition("Z"));

            int foliage_color = biome.getFoliageColor();

            if(blockNamespace.getResource().contains("spruce"))
                foliage_color = Constants.SPRUCE_LEAVES_COLOR;
            else if(blockNamespace.getResource().contains("birch"))
                foliage_color = Constants.BIRCH_LEAVES_COLOR;

            //Color the leaves with the foliage color
            BufferedImage leavesImage = ImageUtility.colorImage(leaves_material.getDefaultDiffuseImage(), foliage_color);

            //Overlay texture with purple color if biome is swampland
            if(biome.getResource().contains("swampland"))
                leavesImage = ImageUtility.colorColoredImage(leavesImage, Constants.SWAMPLAND_PURPLE_OVERLAY);

            leaves_material.setDiffuseImage(leavesImage);

            //Set the material options for leaves
            leaves_material.setSpecularHighlights(0.0);
            leaves_material.setSpecularColor(0.0);
            leaves_material.setIlluminationModel(2);

            leaves_material.setTransparency(true);

            MODIFIED_LEAVES_MATERIALS.add(leaves_texture);
        }
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new LeavesCubeModel();
        clone.copy(this);

        return clone;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getDefaultBlockState().getName());

        return key;
    }
}
