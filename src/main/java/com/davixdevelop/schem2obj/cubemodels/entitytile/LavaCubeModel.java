package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

/**
 * The CubeModel for the Lava entity
 *
 * @author DavixDevelop
 */
public class LavaCubeModel extends LiquidCubeModel{
    public LavaCubeModel(){
        super("blocks/lava_flow", "blocks/lava_still",20.0, 32.0);
        setName("lava");
    }

    @Override
    public boolean isLiquidAdjacent(Namespace adjacent) {
        return (adjacent.getName().equals("lava")) ||
                (adjacent.getName().equals("flowing_lava"));
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new LavaCubeModel();
        clone.copy(this);

        return clone;
    }
}
