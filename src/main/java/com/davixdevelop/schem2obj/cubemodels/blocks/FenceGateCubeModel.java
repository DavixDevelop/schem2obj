package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.cubemodels.IModifyNamespace;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.Map;

/**
 * The CubeModel of the Fence Gate Block
 *
 * @author DavixDevelop
 */
public class FenceGateCubeModel extends BlockCubeModel implements IAdjacentCheck, IModifyNamespace {

    String facing;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Convert namespace to cube model
        super.baseConvert(namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = super.getKey(namespace);
        facing = namespace.getDefaultBlockState().getData("facing");

        CubeModelUtility.getAdjacentNamespace_NSWE(namespace, this, this);

        return key;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        return (adjacent instanceof FenceGateCubeModel);
    }

    @Override
    public boolean checkCollision(Namespace adjacent, int y_index, String orientation) {
        if(adjacent.getType().equals("cobblestone_wall")){
            switch (orientation){
                case "north":
                case "south":
                    return facing.equals("west") || facing.equals("east");
                case "east":
                case "west":
                    return facing.equals("south") || facing.equals("north");
            }
        }

        return false;
    }

    @Override
    public void modifyNamespace(Namespace namespace) {
        namespace.getDefaultBlockState().setData("in_wall", "true");
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new FenceGateCubeModel();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(ICubeModel clone) {
        FenceGateCubeModel fenceGateCopy = (FenceGateCubeModel)clone;
        facing = fenceGateCopy.facing;

        super.copy(clone);
    }
}
