package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.cubemodels.CubeModelFactory;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for the Fence block
 *
 * @author DavixDevelop
 */
public class FenceCubeModel extends BlockCubeModel implements IAdjacentCheck {
    //Map<key: %fence_name:north=true|false,south=true|false,east=true|false,west=true|false, value: Fence Cube Model>
    public static HashMap<String, FenceCubeModel> FENCE_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Convert namespace to cube model
        super.baseConvert(namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getDefaultBlockState().getName());
        CubeModelUtility.getAdjacentNamespace_NSWE(namespace, this);
        CubeModelUtility.getKey_NSWE(key, namespace);

        return key;
    }

    @Override
    public boolean checkCollision(Namespace adjacentBlock, int y_index, String orientation){
        if(adjacentBlock.getType().contains("fence") && !adjacentBlock.getType().contains("gate"))
            return true;

        if(adjacentBlock.getType().equals("glowstone") || adjacentBlock.getType().equals("sea_lantern"))
            return false;

        if(adjacentBlock.getDomain().equals("builtin"))
            return false;

        if(CubeModelFactory.isTranslucentOrNotFull(adjacentBlock.getType()))
            return false;

        return !CubeModelFactory.isTranslucentOrNotFull(CubeModelFactory.getType(adjacentBlock));
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        //If the adjacent block is also a fence, check for collision, else not
        return (adjacent instanceof FenceCubeModel);
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new FenceCubeModel();
        clone.copy(this);

        return clone;
    }
}
