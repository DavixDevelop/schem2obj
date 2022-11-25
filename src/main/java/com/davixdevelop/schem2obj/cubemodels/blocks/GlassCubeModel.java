package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.ArrayList;

/**
 * The CubeModel for the Glass block
 *
 * @author DavixDevelop
 */
public class GlassCubeModel extends BlockCubeModel {
    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        //Get BlockState for the glass block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace.getName());

        //Get the variant for the glass block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);

        VariantModels[] glassModel = new VariantModels[variants.size()];

        //Get the models for the glass
        for(int c = 0; c < variants.size(); c++)
            glassModel[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));


        //Modify the glass materials if they were not yet modified
        GlassPaneCubeModel.modifyGlassMaterials(glassModel, blockNamespace);

        //Generate the cube model for the glass pane variant
        fromVariantModel(blockNamespace.getName(), blockNamespace, glassModel);

        return true;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        //If the adjacent block is the same glass block type, check for collision
        return getName().equals(adjacent.getName());
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new GlassCubeModel();
        clone.copy(this);

        return clone;
    }
}
