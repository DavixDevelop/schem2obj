package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.cubemodels.CubeModelFactory;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.HashMap;

/**
 * The CubeModel for the Iron Bars block
 *
 * @author DavixDevelop
 */
public class IronBarsCubeModel extends BlockCubeModel implements IAdjacentCheck {
    //Map<key: %iron_bar:north=true|false,south=true|false,east=true|false,west=true|false, value: Iron Bars Cube Model>
    public static HashMap<String, IronBarsCubeModel> IRON_BARS_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        Namespace modifiedNamespace = blockNamespace.clone();

        CubeModelUtility.getAdjacentNamespace_NSWE(modifiedNamespace, this);

        String key = CubeModelUtility.getKey_NSWE(modifiedNamespace);

        //Check if variant is already in memory
        //Else generate it and store it
        if(IRON_BARS_VARIANTS.containsKey(key)){
            ICubeModel iron_bars_clone = IRON_BARS_VARIANTS.get(key).clone();
            copy(iron_bars_clone);

        }else{
            //Convert modified namespace to cube model
            super.baseConvert(modifiedNamespace);
            //Store it in memory for later use
            IRON_BARS_VARIANTS.put(key, this);
        }

        return false;
    }

    @Override
    public boolean checkCollision(Namespace adjacentBlock, int y_index, String orientation){
        if(adjacentBlock.getName().contains("iron_bars") || adjacentBlock.getName().contains("glass_pane"))
            return true;

        if(adjacentBlock.getName().equals("glowstone") || adjacentBlock.getName().equals("sea_lantern"))
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
    public ICubeModel clone() {
        ICubeModel clone = new IronBarsCubeModel();
        clone.copy(this);

        return clone;
    }
}
