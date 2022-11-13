package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontCollection;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.util.HashMap;

public class FenceWavefrontObject extends BlockWavefrontObject implements IAdjacentCheck {

    //Map<key: %fence_name:north=true|false,south=true|false,east=true|false,west=true|false, value: Fence Wavefront Object>
    public static HashMap<String, FenceWavefrontObject> FENCE_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        Namespace modifiedNamespace = blockNamespace.clone();

        WavefrontUtility.getAdjacentNamespace_NSWE(modifiedNamespace, this);

        String key = WavefrontUtility.getKey_NSWE(modifiedNamespace);

        //Check if variant is already in memory
        //Else generate it and store it
        if(FENCE_VARIANTS.containsKey(key)){
            IWavefrontObject fence_clone = FENCE_VARIANTS.get(key).clone();
            copy(fence_clone);

        }else{
            //Convert modified namespace to OBJ
            super.baseConvert(modifiedNamespace);
            //Store it in memory for later use
            FENCE_VARIANTS.put(key, this);
        }

        return false;
    }

    @Override
    public boolean checkCollision(Namespace adjacentBlock, int y_index, String orientation){
        if(adjacentBlock.getName().contains("fence") && !adjacentBlock.getName().contains("gate"))
            return true;

        if(adjacentBlock.getName().equals("glowstone") || adjacentBlock.getName().equals("sea_lantern"))
            return false;

        if(adjacentBlock.getDomain().equals("builtin"))
            return false;

        return WavefrontCollection.getType(adjacentBlock) instanceof BlockWavefrontObject;
    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        //If the adjacent block is also a fence, check for collision, else not
        return (adjacent instanceof FenceWavefrontObject);
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new FenceWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
