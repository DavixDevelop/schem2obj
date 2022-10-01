package com.davixdevelop.schem2obj.blockmodels;

import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.blockstates.BlockStateCollection;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.utilities.ArrayVector;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represent a collection of Block Model and their textures, associated with their Namespace
 * @author DavixDevelop
 */
public class BlockModelCollection {
    private static final Double[] BLOCK_ORIGIN = new Double[] {0.5, 0.5, 0.5};

    private HashMap<String, BlockModel> blocksModels;

    public BlockModelCollection(){
        blocksModels = new HashMap<String, BlockModel>();
    }

    /**
     * Internal Recursive method to get the block's models
     * and the block parents model
     * The last two parameters are constants, and should not be changed outside the method call
     * @param block The namespace of the block
     * @param blockState The BlockState of the Block
     * @param rootModels Set this to null, as this is used in the recursive call. rootModel keeps track of the original variant model names
     * @param rootIndex Set this to null, as this is used in the recursive calls. rootIndex keeps track of the current original variant model index
     * @return List of BlockModels
     */
    private BlockModel[] getBlockModelInternal(Namespace block, BlockState blockState, String[] rootModels, Integer rootIndex){
        BlockModel[] blockModels = null;

        ArrayList<String> modelNames = new ArrayList<>();
        modelNames.add(block.getName());

        if(!block.getDomain().equals("internal")){
            modelNames = new ArrayList<>();

            //Get the model name/names depending on the BlockState
            //Get the variant/variants for the block
            ArrayList<BlockState.Variant> variants = blockState.getVariants(block);
            for(BlockState.Variant variant : variants)
                modelNames.add(variant.getModel());

            if(rootModels == null){
                rootModels = new String[variants.size()];
                for(int c = 0; c < rootModels.length; c++)
                    rootModels[c] = modelNames.get(c);
            }
        }

        //Loop through the models the block uses, (as defined in blockstates)
        for(int m = 0; m < modelNames.size(); m++) {
            String modelName = modelNames.get(m);

            if(rootIndex == null)
                rootIndex = m;

            //Check if block was already read from assets
            if (blocksModels.containsKey(modelName)) {
                BlockModel item = blocksModels.get(modelName);
                //Add model item as first element in array, so that the requested block is the first element
                blockModels = new BlockModel[]{item};

                item.setRootParent(rootModels[rootIndex]);

                //Check if model has parent
                if (item.getParent() != null) {
                    //Recursive call to get model parent/parents
                    BlockModel[] parents = getBlockModelInternal(new Namespace(null, "internal", item.getParent().substring(6), null, 0.0),blockState,rootModels,rootIndex);



                    //Temp array to store block item + parents;
                    BlockModel[] tempItems = new BlockModel[1 + parents.length];
                    for (int c = 0; c < parents.length + 1; c++) {
                        //Add requested block model as first element
                        if (c == 0)
                            tempItems[0] = blockModels[0];
                        else
                            tempItems[c] = parents[c - 1];
                    }

                    //Finally assign the temp array items back to the original array
                    blockModels = tempItems;
                }

            } else {
                //Else read from class
                InputStream modelStream = this.getClass().getClassLoader().getResourceAsStream("assets/minecraft/model/block/" + modelName + ".json");
                BlockModel model = BlockModel.readFromJson(modelStream, modelName);

                //Store model in memory for later
                blocksModels.put(modelName, model);

                //Set the root parent model (first model in the "chain" of sub-model's)
                //This is done by only changing the rootIndex if the block domain is not internal
                //The block domain is only internal in recursive call.
                // This causes the rootIndex to only change in the first call to this method,
                // when getBlockModels call this method for the first time
                //This approach allows the blocks to use multiple root models, ex. fire uses multiple models
                model.setRootParent(rootModels[rootIndex]);

                blockModels = new BlockModel[]{model};

                if (model.getParent() != null) {
                    //Recursive call to get model parent/parents
                    BlockModel[] parents = getBlockModelInternal(new Namespace(modelName, "internal", model.getParent().substring(6), null, 0.0), blockState, rootModels, rootIndex);

                    //Temp array to store block item + parents;
                    BlockModel[] tempItems = new BlockModel[1 + parents.length];
                    for (int c = 0; c < parents.length + 1; c++) {
                        //Add requested block model as first element
                        if (c == 0)
                            tempItems[0] = blockModels[0];
                        else
                            tempItems[c] = parents[c - 1];
                    }

                    //Finally assign the temp array items back to the original array
                    blockModels = tempItems;
                }

            }

            //Only increase the rootIndex on the first call of getBlockModel (non recursive call)
            if(!block.getDomain().equals("internal"))
                rootIndex++;
        }

        return blockModels;
    }

    /**
     * Method to get the block's models
     * and the block parents model
     * @param block The namespace of the block
     * @param blockState The BlockState of the Block
     * @return List of BlockModels
     */
    public BlockModel[] getBlockModel(Namespace block,BlockState blockState){
        return getBlockModelInternal(block, blockState, null, null);
    }
}
