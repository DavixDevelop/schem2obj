package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.cubemodels.CubeModelFactory;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel of the Cobblestone Wall
 *
 * @author DavixDevelop
 */
public class CobblestoneWallCubeModel extends BlockCubeModel implements IAdjacentCheck {
    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Convert namespace to cube model
        super.baseConvert(namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();


        CubeModelUtility.getAdjacentNamespace_NSWE(namespace, this);
        CubeModelUtility.getKey_NSWE(key, namespace);

        if(namespace.getDefaultBlockState().getName().equals("mossy_cobblestone")) {
            key.put("BlockName", "mossy_cobblestone_wall");
            namespace.getDefaultBlockState().setName("mossy_cobblestone_wall");
        }else {
            key.put("BlockName", namespace.getType());
            namespace.getDefaultBlockState().setName(namespace.getType());
        }

        namespace.getDefaultBlockState().setData("up", "true");

        return key;
    }

    @Override
    public boolean checkCollision(Namespace adjacent, int y_index, String orientation) {
        if(adjacent.getType().equals("cobblestone_wall")){
            return true;
        }

        if(adjacent.getType().equals("glowstone") || adjacent.getType().equals("sea_lantern") || adjacent.getType().equals("beacon"))
            return false;

        if(adjacent.getType().contains("fence_gate")){
            String facing = adjacent.getDefaultBlockState().getData("facing");

            switch (orientation){
                case "north":
                case "south":
                    return facing.equals("west") || facing.equals("east");
                case "east":
                case "west":
                    return facing.equals("south") || facing.equals("north");
            }

            return false;

        }

        if(CubeModelFactory.isTranslucentOrNotFull(adjacent.getType()))
            return false;

        return !CubeModelFactory.isTranslucentOrNotFull(CubeModelFactory.getType(adjacent));
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        //If the adjacent block is also a cobblestone wall, check for collision, else not
        return (adjacent instanceof CobblestoneWallCubeModel);
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new CobblestoneWallCubeModel();
        clone.copy(this);

        return clone;
    }
}
