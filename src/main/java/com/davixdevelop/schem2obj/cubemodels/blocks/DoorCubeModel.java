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
                    namespace.getPosition("X"),
                    namespace.getPosition("Y") - 1,
                    namespace.getPosition("Z")
                    );
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
        }else{
            //Get the block up the lower part of the door
            Namespace upperAdjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                    namespace.getPosition("X"),
                    namespace.getPosition("Y") + 1,
                    namespace.getPosition("Z")
            );
            //Check if there is any block bellow
            if(upperAdjacentBlock != null){
                //Check if the block bellow is also door of the same variant
                if(upperAdjacentBlock.getType().equals(namespace.getType())){
                    //Check if the block up is the upper part of the door
                    if(upperAdjacentBlock.getDefaultBlockState().getData("half").equals("upper")){
                        //Set the hinge of the lower door to the same of the upper door
                        namespace.getDefaultBlockState().setData("hinge", upperAdjacentBlock.getDefaultBlockState().getData("hinge"));
                    }
                }
            }
        }

        if(namespace.getDefaultBlockState().getData("facing").equals("east") && namespace.getDefaultBlockState().getData("open").equals("true"))
            namespace.getDefaultBlockState().setData("hinge", "left");

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
