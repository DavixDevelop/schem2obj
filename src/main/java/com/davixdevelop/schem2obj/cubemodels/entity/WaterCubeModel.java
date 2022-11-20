package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

public class WaterCubeModel extends LiquidCubeModel {
    public WaterCubeModel(){
        super("blocks/water_flow", "blocks/water_still", 32.0, 64.0);
        setName("Water");
    }

    @Override
    public boolean isLiquidAdjacent(Namespace adjacent) {
        return (adjacent.getName().equals("water")) ||
                (adjacent.getName().equals("flowing_water"));
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new WaterCubeModel();
        clone.copy(this);

        return clone;
    }
}
