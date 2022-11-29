package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;

public class NormalChestCubeModel extends ChestCubeModel {
    public NormalChestCubeModel(){
        super("normal", "chest", true);
    }

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        return super.fromNamespace(blockNamespace, entityValues);
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new NormalChestCubeModel();
        clone.copy(this);

        return clone;
    }
}
