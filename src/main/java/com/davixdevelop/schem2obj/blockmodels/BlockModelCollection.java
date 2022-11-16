package com.davixdevelop.schem2obj.blockmodels;

import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represent a collection of Block Model and their textures, associated with their Namespace
 * @author DavixDevelop
 */
public class BlockModelCollection {
    private static final Double[] BLOCK_ORIGIN = new Double[] {0.5, 0.5, 0.5};

    private HashMap<String, BlockModel> blocksModels;
    private HashMap<String, String> externalBlockModels;

    public BlockModelCollection(){
        blocksModels = new HashMap<>();
        externalBlockModels = new HashMap<>();
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

            InputStream modelStream = null;

            boolean readFromAssets = true;

            if(externalBlockModels.containsKey(modelName)) {
                try {
                    //Read from resource pack
                    modelStream = new FileInputStream(externalBlockModels.get(modelName));
                    readFromAssets = false;

                } catch (Exception ex) {
                    LogUtility.Log(String.format("Failed to read external BlockModel: %s.json (%s)", modelName, externalBlockModels.get(modelName)));
                    LogUtility.Log(ex.getMessage());
                }
            }

            //Read from assets
            if(readFromAssets)
                modelStream = this.getClass().getClassLoader().getResourceAsStream("assets/minecraft/model/" + modelName + ".json");

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



    /**
     * Parse through resource pack BlockModels, and add them to externalBlockModels, for it to be read later
     * @param resourcePack Path to resource pack
     */
    public void parseResourcePack(String resourcePack){
        File resourcePackBlockModelsFolder = Paths.get(resourcePack, "assets","minecraft","model").toFile();

        //Check if resource pack has a model folder
        if(resourcePackBlockModelsFolder.exists() && resourcePackBlockModelsFolder.isDirectory()){
            File[] modelsSubfolder = resourcePackBlockModelsFolder.listFiles();

            if(modelsSubfolder != null){
                for(File modelsFolder : modelsSubfolder){
                    if(modelsFolder.isDirectory()){
                        File[] blockModels = modelsFolder.listFiles();

                        if(blockModels != null)
                            for(File blockModel : blockModels){
                                if(blockModel.isFile() && blockModel.getName().endsWith(".json")) {
                                    externalBlockModels.put(modelsFolder.getName() + "/" + blockModel.getName().replace(".json", ""), blockModel.getPath());
                                }
                            }

                    }

                }
            }
        }
    }
}
