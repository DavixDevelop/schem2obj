package com.davixdevelop.schem2obj.wavefront.custom.entity;

import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;

public class WaterWavefrontObject extends LiquidWavefrontObject {
    public WaterWavefrontObject(){
        super("blocks/water_flow", "blocks/water_still", 32.0, 64.0);
        setName("Water");
    }

    @Override
    public boolean isLiquidAdjacent(Namespace adjacent) {
        return (adjacent.getName().equals("water")) ||
                (adjacent.getName().equals("flowing_water"));
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new WaterWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
