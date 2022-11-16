package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.*;

public class BlockWavefrontObject extends WavefrontObject {

    private static HashMap<BlockState.Variant, IWavefrontObject> BLOCK_RANDOM_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        //Get the BlockState for the block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace.getName());

        //Get the variant/variants of the block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);


        ArrayList<VariantModels> blockModels = new ArrayList<>();

        //Get the model/models the block uses based on the BlockState
        for(BlockState.Variant variant : variants)
            blockModels.add(new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(variant.getModel())));

        if(blockState.isRandomVariants()){
            //Check if random variant is not in BLOCK_RANDOM_VARIANTS, generate it and store it
            //Else get a copy of the singleton
            if(!BLOCK_RANDOM_VARIANTS.containsKey(variants.get(0))){
                toObj(blockModels, blockNamespace);
                BLOCK_RANDOM_VARIANTS.put(variants.get(0), this);

            }else{
                IWavefrontObject copy = BLOCK_RANDOM_VARIANTS.get(variants.get(0)).clone();
                super.copy(copy);
            }

            return false;
        }

        toObj(blockModels, blockNamespace);

        return true;

    }

    /**
     * Generate OBJ for block namespace without checking for random variants
     * @param blockNamespace The block namespace for which to generate the OBJ
     */
    public void baseConvert(Namespace blockNamespace){
        //Get the BlockState for the block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace.getName());

        //Get the variant/variants of the block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);


        ArrayList<VariantModels> blockModels = new ArrayList<>();

        //Get the model/models the block uses based on the BlockState
        for(BlockState.Variant variant : variants)
            blockModels.add(new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(variant.getModel())));

        toObj(blockModels,blockNamespace);
    }

    /**
     * Convert block models to wavefront object.
     * The indexes in the object are treated as if the object is the first one in the file
     * @param blockModels The variant block models to convert
     * @param blockNamespace The namespace of the block
     * @return the block wavefront object
     */
    public void toObj(ArrayList<VariantModels> blockModels, Namespace blockNamespace){
        super.toObj(blockNamespace.getName(), blockModels, blockNamespace);
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new BlockWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
