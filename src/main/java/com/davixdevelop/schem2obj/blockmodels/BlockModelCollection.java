package com.davixdevelop.schem2obj.blockmodels;

import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;

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
        blocksModels = new HashMap<>();
    }

    /**
     * Internal Recursive method to get the block's models
     * and the block parents model
     * The last two parameters are constants, and should not be changed outside the method call
     * @param models An list to populate with the block's models
     * @param modelName The name of the model. Ex. blocks/dirt
     * @return List of BlockModels
     */
    private void getBlockModelInternal(ArrayList<BlockModel> models, String modelName){

        //Check if block was already read from assets
        if (blocksModels.containsKey(modelName)) {
            BlockModel item = blocksModels.get(modelName).clone();
            //Add model item as first element in array, so that the requested block is the first element
            models.add(item);

            //item.setRootVariant(variant);

            //Check if model has parent
            if (item.getParent() != null) {
                //Recursive call to get model parent/parents
                getBlockModelInternal(models, item.getParent());
            }

        } else {
            //Else get block model from the Resources
            String modelPath = ResourceLoader.getResourcePath("models", modelName,"json");

            InputStream modelStream = ResourceLoader.getResource(modelPath);

            //Read from stream
            BlockModel model = BlockModel.readFromJson(modelStream, modelName);

            //Store model in memory for later
            blocksModels.put(modelName, model.clone());

            //model.setRootVariant(variant);

            models.add(model);

            if (model.getParent() != null) {
                //Recursive call to get model parent/parents
                getBlockModelInternal(models, model.getParent());
            }

        }
    }

    /**
     * Method to get the block's models
     * and the block parents model
     * @param modelName The name of the model, ex. dirt
     * @param modelType Specify the type of model, ex. item or blocks. If not specified, It's block by default
     * @return List of BlockModels
     */
    public ArrayList<BlockModel> getBlockModel(String modelName, String ...modelType){
        ArrayList<BlockModel> models = new ArrayList<>();
        getBlockModelInternal(models, modelType.length > 0 ? modelType[0] + "/" + modelName : "block/" + modelName);

        return models;
    }
}
