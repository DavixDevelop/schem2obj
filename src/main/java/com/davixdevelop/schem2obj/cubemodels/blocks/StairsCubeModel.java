package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.blockstates.AdjacentBlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for all Stairs block variants
 *
 * @author DavixDevelop
 */
public class StairsCubeModel extends BlockCubeModel implements IAdjacentCheck {
    public static AdjacentBlockState ADJACENT_STAIRS_STATES = new AdjacentBlockState("assets/schem2obj/stairs_states.json");

    private String half;
    private String facing;
    private String shape;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Convert namespace to cube model
        super.baseConvert(namespace);
        return true;
    }

    @Override
    public boolean checkCollision(Namespace adjacentBlock, int y_index, String orientation){
        if(adjacentBlock.getType().contains("stairs")){
            return half.equals(adjacentBlock.getDefaultBlockState().getData("half"));
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

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        half = namespace.getDefaultBlockState().getData("half");
        //Get modified namespace depending on the adjacent block states
        CubeModelUtility.getAdjacentNamespace_AdjacentState(namespace, ADJACENT_STAIRS_STATES, this);
        shape = namespace.getDefaultBlockState().getData("shape");
        facing = namespace.getDefaultBlockState().getData("facing");

        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getDefaultBlockState().getName());
        key.put("facing", namespace.getDefaultBlockState().getData("facing"));
        key.put("half", namespace.getDefaultBlockState().getData("half"));
        key.put("shape", namespace.getDefaultBlockState().getData("shape"));

        return key;
    }

    @Override
    public ICubeModel duplicate() {
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
