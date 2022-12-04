package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;

public class EntityCubeModel extends CubeModel{
    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new EntityCubeModel();
        clone.copy(this);

        return clone;
    }


    public Double[] getOrigin() {
        return new Double[0];
    }
}
