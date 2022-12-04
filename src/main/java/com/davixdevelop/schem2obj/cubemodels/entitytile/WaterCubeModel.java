package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

/**
 * The CubeModel for the Water entity
 *
 * @author DavixDevelop
 */
public class WaterCubeModel extends LiquidCubeModel {
    public WaterCubeModel(){
        super("blocks/water_flow", "blocks/water_still", 32.0, 64.0);
        setName("Water");
    }

    @Override
    public boolean isLiquidAdjacent(Namespace adjacent) {
        return (adjacent.getType().equals("water")) ||
                (adjacent.getType().equals("flowing_water"));
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new WaterCubeModel();
        clone.copy(this);

        return clone;
    }
}
