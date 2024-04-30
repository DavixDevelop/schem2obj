package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemFrameCubeModel extends EntityCubeModel {

    @Override
    public boolean fromNamespace(Namespace namespace) {
        toCubeModel(namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        EntityValues entityValues = namespace.getCustomData();
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("EntityName", "item_frame");
        key.put("Facing", entityValues.getByte("Facing"));
        if(entityValues.containsKey("Item")){
            key.put("ItemRotation", entityValues.getByte("ItemRotation"));
            key.put("Item", entityValues.getEntityValues("Item"));
        }

        return key;
    }

    public void toCubeModel(Namespace namespace){
        EntityValues entityValues = namespace.getCustomData();

        List<Float> rotation = entityValues.getFloatList("Rotation");
        ArrayVector.MatrixRotation rotationY = null;

        if(rotation.get(0) > 0.0f)
            rotationY = new ArrayVector.MatrixRotation(rotation.get(0).doubleValue(), "Z");

        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("frame", "blocks/planks_birch");
        modelsMaterials.put("background", "blocks/itemframe_background");

        CubeModelUtility.generateOrGetMaterial("blocks/planks_birch", namespace);
        CubeModelUtility.generateOrGetMaterial("blocks/itemframe_background", namespace);

        BlockModel itemFrameModel = Constants.BLOCK_MODELS.getBlockModel("item_frame", "builtin").get(0);
        CubeElement[] itemFrameElements = itemFrameModel.getElements().toArray(new CubeElement[0]);

        fromCubes("item-frame", false, null, rotationY, modelsMaterials, itemFrameElements);

        //Check if frame contains a item
        if(entityValues.containsKey("Item")){
            try {
                //Get item model of item
                ICubeModel itemModel =  Constants.CUBE_MODEL_FACTORY.getItemModel(entityValues.getEntityValues("Item"), namespace).duplicate();

                //Rotate item model to match item frame
                if (rotationY != null) {
                    CubeModelUtility.rotateCubeModel(itemModel, new Double[]{0.0, 0.0, rotation.get(0).doubleValue()}, Constants.BLOCK_ORIGIN);
                }

                CubeModelUtility.scaleCubeModel(itemModel, new Double[]{0.5, 0.5, 0.5}, Constants.BLOCK_ORIGIN);

                int facing = entityValues.getByte("Facing").intValue();
                Double[] itemTranslation = new Double[]{
                        facing == 1 ? 0.4376 : facing == 3 ? -0.4376 : 0.0,
                        facing == 0 ? 0.4376 : facing == 2 ? -0.4376 : 0.0,
                        0.0
                };

                CubeModelUtility.translateCubeModel(itemModel, itemTranslation);

                double itemRotation = entityValues.getByte("ItemRotation").doubleValue();
                if (itemRotation > 0.0) {
                    CubeModelUtility.rotateCubeModel(itemModel, new Double[]{0.0, (360.0 / -8.0) * itemRotation, 0.0}, Constants.BLOCK_ORIGIN);
                }

                if (rotationY != null) {
                    CubeModelUtility.rotateCubeModel(itemModel, new Double[]{0.0, 0.0, rotation.get(0).doubleValue()}, Constants.BLOCK_ORIGIN);
                }


                //Add the item model to the item frame
                appendCubeModel(itemModel);
            }catch (Exception ex){
                LogUtility.Log("Error while generating Item model for " + entityValues.getEntityValues("Item").getString("id"));
                LogUtility.Log(ex.getMessage());
            }

        }

    }

    @Override
    public Double[] getOrigin() {
        return new Double[]{0.5, 0.03125, 0.5};
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new ItemFrameCubeModel();
        clone.copy(this);

        return clone;
    }
}
