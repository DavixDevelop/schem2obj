package com.davixdevelop.schem2obj.wavefront.custom.entity;

import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontObject;

import java.util.HashMap;

public class BedWavefrontObject extends TileEntity {

    //Map<key: %color:part:facing, value: Glass Pane Wavefront Object>
    public static HashMap<String, BedWavefrontObject> BED_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        int color = entityValues.getInteger("color");
        String facing = blockNamespace.getData("facing");
        String part = blockNamespace.getData("part");

        String key = getKey(color, part, facing);

        if(!BED_VARIANTS.containsKey(key)){
            toObj(color, part, facing);
        }else{
            IWavefrontObject variantObject = BED_VARIANTS.get(key);
            super.copy(variantObject);
        }

        return false;
    }

    private String getKey(int color, String part, String facing){
        return String.format("%d:%s:%s",color,part,facing);
    }

    public void toObj(int color, String part, String facing){


    }
}
