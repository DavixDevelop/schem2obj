package com.davixdevelop.schem2obj.blockmodels;

import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.namespace.Namespace;

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
     * @param models An list to populate with the block's models
     * @param block The namespace of the block
     * @return List of BlockModels
     */
    private void getBlockModelInternal(ArrayList<BlockModel> models, Namespace block, BlockState.Variant variant){

        String modelName = block.getName();

        if(!block.getDomain().equals("internal"))
            modelName = variant.getModel();

        //Check if block was already read from assets
        if (blocksModels.containsKey(modelName)) {
            BlockModel item = blocksModels.get(modelName).clone();
            //Add model item as first element in array, so that the requested block is the first element
            models.add(item);

            //item.setRootVariant(variant);

            //Check if model has parent
            if (item.getParent() != null) {
                //Recursive call to get model parent/parents
                getBlockModelInternal(models, new Namespace(null, "internal", item.getParent().substring(6), null, 0.0), variant);
            }

        } else {
            //Else read from class
            InputStream modelStream = this.getClass().getClassLoader().getResourceAsStream("assets/minecraft/model/block/" + modelName + ".json");
            BlockModel model = BlockModel.readFromJson(modelStream, modelName);

            //Store model in memory for later
            blocksModels.put(modelName, model.clone());

            //model.setRootVariant(variant);

            models.add(model);

            if (model.getParent() != null) {
                //Recursive call to get model parent/parents
                getBlockModelInternal(models, new Namespace(modelName, "internal", model.getParent().substring(6), null, 0.0), variant);
            }

        }
    }

    /**
     * Method to get the block's models
     * and the block parents model
     * @param block The namespace of the block
     * @return List of BlockModels
     */
    public ArrayList<BlockModel> getBlockModel(Namespace block, BlockState.Variant variant){
        ArrayList<BlockModel> models = new ArrayList<>();
        getBlockModelInternal(models, block, variant);

        return models;
    }
}
