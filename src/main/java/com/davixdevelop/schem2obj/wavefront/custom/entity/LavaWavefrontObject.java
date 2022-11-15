package com.davixdevelop.schem2obj.wavefront.custom.entity;

import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;

public class LavaWavefrontObject extends LiquidWavefrontObject {

    public LavaWavefrontObject(){
        super("blocks/lava_flow", "blocks/lava_still",20.0, 32.0);
        setName("lava");
    }

    @Override
    public boolean isLiquidAdjacent(Namespace adjacent) {
        return (adjacent.getName().equals("lava")) ||
                (adjacent.getName().equals("flowing_lava"));
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new LavaWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
