package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;

import java.util.ArrayList;

public class GlassBlockWavefrontObject extends BlockWavefrontObject {
    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        //Get BlockState for the glass block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace);

        //Get the variant for the glass block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);

        ArrayList<VariantModels> glassModel = new ArrayList<>();

        //Get the models for the glass
        for(BlockState.Variant variant : variants)
            glassModel.add(new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(blockNamespace, variant)));


        //Modify the glass materials if they were not yet modified
        GlassPaneWavefrontObject.modifyGlassMaterials(glassModel, blockNamespace);

        //Generate the OBJ for the glass pane variant
        super.toObj(glassModel, blockNamespace);

        return true;
    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        //If the adjacent block is the same glass block type, check for collision
        return getName().equals(adjacent.getName());
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new GlassBlockWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
