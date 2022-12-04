package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;

public class EnderChestCubeMode extends ChestCubeModel {
    public EnderChestCubeMode(){
        super("ender", "ender_chest", false);
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new EnderChestCubeMode();
        clone.copy(this);

        return clone;
    }
}
