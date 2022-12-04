package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.BlockStateNamespace;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for most block that use a simple cube for It's model
 *
 * @author DavixDevelop
 */
public class BlockCubeModel extends CubeModel {
    public static HashMap<BlockState.Variant, ICubeModel> BLOCK_RANDOM_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace namespace) {

        //Get the BlockState for the block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getResource());

        VariantModels[] blockModels;

        EntityValues customData = namespace.getCustomData();
        //If block namespace custom data is not null and contains variant key, it means the block uses random variants,
        //so get the model from the random variant that was generated in getKey
        if(customData != null && customData.containsKey("variant")){
            BlockState.Variant variant = (BlockState.Variant) customData.get("variant");
            blockModels = new VariantModels[]{new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(variant.getModel()))};
        }else {
            //Get the variant/variants of the block
            ArrayList<BlockState.Variant> variants = blockState.getVariants(namespace);

            blockModels = new VariantModels[variants.size()];

            //Get the model/models the block uses based on the BlockState
            for(int c = 0; c < variants.size(); c++){
                blockModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));
            }
        }

        fromVariantModel(namespace.getResource(), namespace, blockModels);

        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();

        key.put("BlockName", namespace.getResource());

        if(!namespace.getMetaIDS().isEmpty()){
            for(String stateProperty : namespace.getDefaultBlockState().getData().keySet()){
                key.put(stateProperty, namespace.getDefaultBlockState().getData(stateProperty));
            }
        }

        //Get the BlockState for the block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState((String) key.get("BlockName"));

        //Check if block has random variants
        if(blockState.isRandomVariants()){
            //Get the variant/variants of the block
            ArrayList<BlockState.Variant> variants = blockState.getVariants(namespace);

            //Inject random variant into block namespace custom data
            EntityValues customData = new EntityValues();
            customData.put("variant", variants.get(0));

            namespace.setCustomData(customData);

            key.put("variant", variants.get(0));
        }

        return key;
    }

    /**
     * Generate cube model for block namespace without checking for random variants
     * @param namespace The block namespace for which to generate the cube model
     */
    public void baseConvert(Namespace namespace){

        //Get the BlockState for the block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getResource());

        //Get the variant/variants of the block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(namespace);

        VariantModels[] blockModels = new VariantModels[variants.size()];

        //Get the model/models the block uses based on the BlockState
        for(int c = 0; c < variants.size(); c++)
            blockModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));

        fromVariantModel(namespace.getResource(), namespace, blockModels);
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new BlockCubeModel();
        clone.copy(this);

        return clone;
    }
}
