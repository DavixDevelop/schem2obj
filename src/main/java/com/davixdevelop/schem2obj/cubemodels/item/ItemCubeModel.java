package com.davixdevelop.schem2obj.cubemodels.item;

import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;

public class ItemCubeModel extends CubeModel {
    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new ItemCubeModel();
        clone.copy(this);

        return clone;
    }
}
