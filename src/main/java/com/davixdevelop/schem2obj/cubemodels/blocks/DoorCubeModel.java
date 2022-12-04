package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for the Door block
 *
 * @author DavixDevelop
 */
public class DoorCubeModel extends BlockCubeModel {
    @Override
    public boolean fromNamespace(Namespace namespace) {
        super.baseConvert(namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getDefaultBlockState().getName());


        //Check if the block is the upper part of the door
        if(namespace.getDefaultBlockState().getData("half").equals("upper")){
            //Get the block bellow the upper part of the door
            Namespace lowerAdjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                    Constants.LOADED_SCHEMATIC.getPosX(),
                    Constants.LOADED_SCHEMATIC.getPosY() - 1,
                    Constants.LOADED_SCHEMATIC.getPosZ());
            //Check if there is any block bellow
            if(lowerAdjacentBlock != null){
                //Check if the block bellow is also door of the same variant
                if(lowerAdjacentBlock.getType().equals(namespace.getType())){
                    //Check if the block bellow is the lower part of the door
                    if(lowerAdjacentBlock.getDefaultBlockState().getData("half").equals("lower")){
                        //Set the facing of the upper door to the same of the lower door
                        namespace.getDefaultBlockState().setData("facing", lowerAdjacentBlock.getDefaultBlockState().getData("facing"));
                        //Set the open of the upper door to the same of the lower door
                        namespace.getDefaultBlockState().setData("open", lowerAdjacentBlock.getDefaultBlockState().getData("open"));
                    }
                }
            }
        }

        key.put("facing", namespace.getDefaultBlockState().getData("facing"));
        key.put("half", namespace.getDefaultBlockState().getData("half"));
        key.put("hinge", namespace.getDefaultBlockState().getData("hinge"));
        key.put("open", namespace.getDefaultBlockState().getData("open"));
        key.put("powered", namespace.getDefaultBlockState().getData("powered"));

        return key;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        return false;
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new DoorCubeModel();
        clone.copy(this);

        return clone;
    }
}
