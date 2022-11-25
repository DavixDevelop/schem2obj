package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.blockstates.AdjacentBlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.HashMap;

/**
 * The CubeModel for all Stairs block varaints
 *
 * @author DavixDevelop
 */
public class StairsCubeModel extends BlockCubeModel implements IAdjacentCheck {
    public static AdjacentBlockState ADJACENT_STAIRS_STATES = new AdjacentBlockState("assets/minecraft/stairs_states.json");

    //Map<key: %stair variant:facing=orientation (ex. east),half=top|bottom,shape=straight|outer(or inner)_left(or right), value: Stair Cube Model>
    public static HashMap<String, StairsCubeModel> STAIRS_BLOCK_VARIANTS = new HashMap<>();

    private String half;
    private String facing;
    private String shape;

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        half = blockNamespace.getData().get("half");

        Namespace modifiedNamespace = blockNamespace.clone();

        //Get modified namespace depending on the adjacent block states
        CubeModelUtility.getAdjacentNamespace_AdjacentState(blockNamespace, modifiedNamespace, ADJACENT_STAIRS_STATES, this);

        shape = modifiedNamespace.getData().get("shape");
        facing = modifiedNamespace.getData().get("facing");

        //Create key for variant
        String key = getKey(modifiedNamespace);

        //Check if variant of stairs is already in memory
        if(STAIRS_BLOCK_VARIANTS.containsKey(key)){
            ICubeModel stairs_copy = STAIRS_BLOCK_VARIANTS.get(key).clone();
            copy(stairs_copy);
        }else{
            //Convert modified namespace to cube model
            super.baseConvert(modifiedNamespace);
            //Store it in memory for later use
            STAIRS_BLOCK_VARIANTS.put(key, this);
        }

        return false;
    }

    @Override
    public boolean checkCollision(Namespace adjacentBlock, int y_index, String orientation){
        if(adjacentBlock.getName().contains("stairs")){
            return half.equals(adjacentBlock.getData().get("half"));
        }

        return false;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        //If the adjacent block is also a stair and is the same half, check for collision
        if(adjacent instanceof StairsCubeModel){
            StairsCubeModel adjacentStairs = (StairsCubeModel) adjacent;
            if(half.equals(adjacentStairs.half)){
                return shape.equals("straight") && adjacentStairs.shape.equals("straight") && ((facing.equals("north") && adjacentStairs.facing.equals("north")) || (facing.equals("south") && adjacentStairs.facing.equals("south")));

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
    public ICubeModel clone() {
        ICubeModel clone = new StairsCubeModel();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(ICubeModel clone) {
        StairsCubeModel stairClone = (StairsCubeModel) clone;
        half = stairClone.half;
        facing = stairClone.facing;
        shape = stairClone.shape;

        super.copy(clone);
    }
}
