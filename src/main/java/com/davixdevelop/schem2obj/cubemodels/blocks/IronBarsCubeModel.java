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
 * The CubeModel for the Iron Bars block
 *
 * @author DavixDevelop
 */
public class IronBarsCubeModel extends BlockCubeModel implements IAdjacentCheck {
    @Override
    public boolean fromNamespace(Namespace namespace) {
        Namespace modifiedNamespace = namespace.duplicate();
        CubeModelUtility.getAdjacentNamespace_NSWE(modifiedNamespace, this);
        //Convert modified namespace to cube model
        super.baseConvert(modifiedNamespace);

        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getType());
        Namespace modifiedNamespace = namespace.duplicate();
        CubeModelUtility.getAdjacentNamespace_NSWE(modifiedNamespace, this);
        CubeModelUtility.getKey_NSWE(key, modifiedNamespace);

        return key;
    }

    @Override
    public boolean checkCollision(Namespace adjacentBlock, int y_index, String orientation){
        if(adjacentBlock.getType().contains("iron_bars") || adjacentBlock.getType().contains("glass_pane"))
            return true;

        if(adjacentBlock.getType().equals("glowstone") || adjacentBlock.getType().equals("sea_lantern"))
            return false;

        if(adjacentBlock.getDomain().equals("builtin"))
            return false;

        return CubeModelFactory.getType(adjacentBlock) instanceof BlockCubeModel;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        //If the adjacent block is also a fence, check for collision, else not
        return (adjacent instanceof IronBarsCubeModel);
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new IronBarsCubeModel();
        clone.copy(this);

        return clone;
    }
}
