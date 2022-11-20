package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.HashMap;

public class DoorCubeModel extends BlockCubeModel {
    //Map<key: %door_name:facing=north|east|south|west,half=lower|upper,hinge=left|right,open=true|false,powered=true|false, value: Door Cube Model>
    public static HashMap<String, DoorCubeModel> DOOR_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        Namespace modifiedNamespace = blockNamespace.clone();

        //Check if the block is the upper part of the door
        if(modifiedNamespace.getData().get("half").equals("upper")){
            //Get the block bellow the upper part of the door
            Namespace lowerAdjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                    Constants.LOADED_SCHEMATIC.getPosX(),
                    Constants.LOADED_SCHEMATIC.getPosY() - 1,
                    Constants.LOADED_SCHEMATIC.getPosZ());
            //Check if there is any block bellow
            if(lowerAdjacentBlock != null){
                //Chekc if the block bellow is also door of the same variant
                if(lowerAdjacentBlock.getName().equals(modifiedNamespace.getName())){
                    //Check if the block bellow is the lower part of the door
                    if(lowerAdjacentBlock.getData().get("half").equals("lower")){
                        //Set the facing of the upper door to the same of the lower door
                        modifiedNamespace.getData().put("facing", lowerAdjacentBlock.getData().get("facing"));
                        //Set the open of the upper door to the same of the lower door
                        modifiedNamespace.getData().put("open", lowerAdjacentBlock.getData().get("open"));
                    }
                }
            }
        }

        String key = getKey(modifiedNamespace);

        if(DOOR_VARIANTS.containsKey(key)){
            ICubeModel door_clone = DOOR_VARIANTS.get(key).clone();
            copy(door_clone);
        }else{
            super.baseConvert(modifiedNamespace);
            DOOR_VARIANTS.put(key, this);
        }

        return false;
    }

    public String getKey(Namespace blockNamespace){
        return String.format("%s:facing=%s,half=%s,hinge=%s,open=%s,powered=%s",
                blockNamespace.getName(),
                blockNamespace.getData().get("facing"),
                blockNamespace.getData().get("half"),
                blockNamespace.getData().get("hinge"),
                blockNamespace.getData().get("open"),
                blockNamespace.getData().get("powered"));
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        return false;
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new DoorCubeModel();
        clone.copy(this);

        return clone;
    }
}
