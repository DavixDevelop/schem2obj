package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for the Glass block
 *
 * @author DavixDevelop
 */
public class GlassCubeModel extends BlockCubeModel {
    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Get BlockState for the glass block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getDefaultBlockState().getName());

        //Get the variant for the glass block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(namespace);

        VariantModels[] glassModel = new VariantModels[variants.size()];

        //Get the models for the glass
        for(int c = 0; c < variants.size(); c++)
            glassModel[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));


        //Modify the glass materials if they were not yet modified
        GlassPaneCubeModel.modifyGlassMaterials(glassModel, namespace);

        //Generate the cube model for the glass pane variant
        fromVariantModel(namespace.getDefaultBlockState().getName(), namespace, glassModel);

        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getDefaultBlockState().getName());

        return key;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        //If the adjacent block is the same glass block type, check for collision
        return getName().equals(adjacent.getName());
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new GlassCubeModel();
        clone.copy(this);

        return clone;
    }
}
