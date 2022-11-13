package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontCollection;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.util.HashMap;

public class IronBarsWavefrontObject extends BlockWavefrontObject implements IAdjacentCheck {
    //Map<key: %iron_bar:north=true|false,south=true|false,east=true|false,west=true|false, value: Iron Bars Wavefront Object>
    public static HashMap<String, IronBarsWavefrontObject> IRON_BARS_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        Namespace modifiedNamespace = blockNamespace.clone();

        WavefrontUtility.getAdjacentNamespace_NSWE(modifiedNamespace, this);

        String key = WavefrontUtility.getKey_NSWE(modifiedNamespace);

        //Check if variant is already in memory
        //Else generate it and store it
        if(IRON_BARS_VARIANTS.containsKey(key)){
            IWavefrontObject iron_bars_clone = IRON_BARS_VARIANTS.get(key).clone();
            copy(iron_bars_clone);

        }else{
            //Convert modified namespace to OBJ
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

        return WavefrontCollection.getType(adjacentBlock) instanceof BlockWavefrontObject;
    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        //If the adjacent block is also a fence, check for collision, else not
        return (adjacent instanceof IronBarsWavefrontObject);
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new IronBarsWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
