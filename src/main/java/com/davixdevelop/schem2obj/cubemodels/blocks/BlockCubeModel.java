package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The CubeModel for most block that use a simple cube for It's model
 *
 * @author DavixDevelop
 */
public class BlockCubeModel extends CubeModel {
    public static HashMap<BlockState.Variant, ICubeModel> BLOCK_RANDOM_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        //Get the BlockState for the block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace.getName());

        //Get the variant/variants of the block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);


        VariantModels[] blockModels = new VariantModels[variants.size()];

        //Get the model/models the block uses based on the BlockState
        for(int c = 0; c < variants.size(); c++){
            blockModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));
        }

        if(blockState.isRandomVariants()){
            //Check if random variant is not in BLOCK_RANDOM_VARIANTS, generate it and store it
            //Else get a copy of the singleton
            if(!BLOCK_RANDOM_VARIANTS.containsKey(variants.get(0))){
                fromVariantModel(blockNamespace.getName(), blockNamespace, blockModels);
                BLOCK_RANDOM_VARIANTS.put(variants.get(0), this);

            }else{
                ICubeModel copy = BLOCK_RANDOM_VARIANTS.get(variants.get(0)).clone();
                super.copy(copy);
            }

            return false;
        }

        fromVariantModel(blockNamespace.getName(), blockNamespace, blockModels);

        return true;
    }

    /**
     * Generate cube model for block namespace without checking for random variants
     * @param blockNamespace The block namespace for which to generate the cube model
     */
    public void baseConvert(Namespace blockNamespace){
        //Get the BlockState for the block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace.getName());

        //Get the variant/variants of the block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);

        VariantModels[] blockModels = new VariantModels[variants.size()];

        //Get the model/models the block uses based on the BlockState
        for(int c = 0; c < variants.size(); c++)
            blockModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));

        fromVariantModel(blockNamespace.getName(), blockNamespace, blockModels);
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new BlockCubeModel();
        clone.copy(this);

        return clone;
    }
}
