package com.davixdevelop.schem2obj.blockmodels;

import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represent a collection of Block Models
 * @author DavixDevelop
 */
public class BlockModelCollection {

    ConcurrentMap<String, BlockModel> blocksModels;

    public BlockModelCollection(){
        blocksModels = new ConcurrentHashMap<>();
    }

    /**
     * Internal Recursive method to get the block's models
     * and the block parents model
     * The last two parameters are constants, and should not be changed outside the method call
     * @param models An list to populate with the block's models
     * @param modelName The name of the model. Ex. blocks/dirt
     */
    private void getBlockModelInternal(ArrayList<BlockModel> models, String modelName){

        //Check if block was already read from assets
        if (blocksModels.containsKey(modelName)) {
            BlockModel item = blocksModels.get(modelName).duplicate();
            //Add model item as first element in array, so that the requested block is the first element
            models.add(item);

            //item.setRootVariant(variant);

            //Check if model has parent and It's not builtin/entity as It doesn't exist
            if (item.getParent() != null && !item.getParent().equals("builtin/entity") && !item.getParent().equals("builtin/generated")) {
                //Recursive call to get model parent/parents
                getBlockModelInternal(models, item.getParent());
            }

        } else {
            //Else get block model from the Resources
            String modelPath = ResourceLoader.getResourcePath("models", modelName,"json");

            InputStream modelStream = ResourceLoader.getResource(modelPath);

            if(modelStream == null)
                return;

            //Read from stream
            BlockModel model = BlockModel.readFromJson(modelStream, modelName);

            //Store model in memory for later
            blocksModels.put(modelName, model.duplicate());

            //model.setRootVariant(variant);

            models.add(model);

            //Skip builtin/entity, as it doesn't exists
            if (model.getParent() != null && !model.getParent().equals("builtin/entity") && !model.getParent().equals("builtin/generated")) {
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
