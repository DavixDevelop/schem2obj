package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ImageUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LeavesCubeModel extends BlockCubeModel {
    public static Set<String> MODIFIED_LEAVES_MATERIALS = new HashSet<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        //Get BlockState for the leaves block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace.getName());

        //Get the variant for the glass block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);

        VariantModels[] leavesModels = new VariantModels[variants.size()];

        //Get the models for the glass
        for(int c = 0; c < variants.size(); c++)
            leavesModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));


        //Modify the leaves materials if they were not yet modified
        modifyLeavesMaterials(leavesModels, blockNamespace);

        //Generate the cube model for the leaves block variant
        fromVariantModel(blockNamespace.getName(), blockNamespace, leavesModels);

        return true;
    }

    public static void modifyLeavesMaterials(VariantModels[] leavesModel, Namespace blockNamespace){
        //Extract the texture the leaves use
        String leaves_texture = CubeModelUtility.modelsToMaterials(leavesModel, blockNamespace).entrySet().stream().findFirst().get().getValue().
                entrySet().stream().findFirst().get().getValue();
        if(!MODIFIED_LEAVES_MATERIALS.contains(leaves_texture)){

            IMaterial leaves_material = Constants.BLOCK_MATERIALS.getMaterial(leaves_texture);

            //Color the leaves with the foliage color
            leaves_material.setDiffuseImage(ImageUtility.colorImage(leaves_material.getDefaultDiffuseImage(), Constants.BIOME_FOLIAGE_COLOR));

            //Set the material options for leaves
            leaves_material.setSpecularHighlights(0.0);
            leaves_material.setSpecularColor(0.0);
            leaves_material.setIlluminationModel(2);

            leaves_material.setTransparency(true);

            MODIFIED_LEAVES_MATERIALS.add(leaves_texture);
        }
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new LeavesCubeModel();
        clone.copy(this);

        return clone;
    }
}
