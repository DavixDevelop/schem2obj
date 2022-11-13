package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockstates.AdjacentBlockState;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.util.HashMap;

public class StairsBlockWavefrontObject extends BlockWavefrontObject implements IAdjacentCheck {

    public static AdjacentBlockState ADJACENT_STAIRS_STATES = new AdjacentBlockState("assets/minecraft/stairs_states.json");

    //Map<key: %stair variant:facing=orientation (ex. east),half=top|bottom,shape=straight|outer(or inner)_left(or right), value: Stair Block Wavefront Object>
    public static HashMap<String, StairsBlockWavefrontObject> STAIRS_BLOCK_VARIANTS = new HashMap<>();

    private String half;
    private String facing;
    private String shape;

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        half = blockNamespace.getData().get("half");

        Namespace modifiedNamespace = blockNamespace.clone();

        //Get modified namespace depending on the adjacent block states
        WavefrontUtility.getAdjacentNamespace_AdjacentState(blockNamespace, modifiedNamespace, ADJACENT_STAIRS_STATES, this);

        shape = modifiedNamespace.getData().get("shape");
        facing = modifiedNamespace.getData().get("facing");

        //Create key for variant
        String key = getKey(modifiedNamespace);

        //Check if variant of stairs is already in memory
        if(STAIRS_BLOCK_VARIANTS.containsKey(key)){
            IWavefrontObject stairs_copy = STAIRS_BLOCK_VARIANTS.get(key).clone();
            copy(stairs_copy);
        }else{
            //Convert modified namespace to OBJ
            super.baseConvert(modifiedNamespace);
            //Store it in memory for later use
            STAIRS_BLOCK_VARIANTS.put(key, this);
        }

        return false;
    }

    @Override
    public boolean checkCollision(Namespace adjacentBlock, int y_index, String orientation){
        if(adjacentBlock.getName().contains("stairs")){
            if(half.equals(adjacentBlock.getData().get("half")))
                return true;
        }

        return false;
    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        //If the adjacent block is also a stair and is the same half, check for collision
        if(adjacent instanceof StairsBlockWavefrontObject){
            StairsBlockWavefrontObject adjacentStairs = (StairsBlockWavefrontObject) adjacent;
            if(half.equals(adjacentStairs.half)){
                if(shape.equals("straight") && adjacentStairs.shape.equals("straight") && ((facing.equals("north") && adjacentStairs.facing.equals("north")) || (facing.equals("south") && adjacentStairs.facing.equals("south"))))
                    return true;

            }
        }

        return false;
    }

    private String getKey(Namespace stairsNamespace){
        return String.format("%s:facing=%s,half=%s,shape=%s",
                stairsNamespace.getName(),
                stairsNamespace.getData().get("facing"),
                stairsNamespace.getData().get("half"),
                stairsNamespace.getData().get("shape"));
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new StairsBlockWavefrontObject();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(IWavefrontObject clone) {
        StairsBlockWavefrontObject stairClone = (StairsBlockWavefrontObject)clone;
        half = stairClone.half;
        facing = stairClone.facing;
        shape = stairClone.shape;

        super.copy(clone);
    }
}
