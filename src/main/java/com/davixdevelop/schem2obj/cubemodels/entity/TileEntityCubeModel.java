package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;

public class TileEntityCubeModel extends CubeModel {
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        return false;
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new TileEntityCubeModel();
        clone.copy(this);

        return clone;
    }
}
