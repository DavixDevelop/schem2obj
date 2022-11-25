package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;

/**
 * A CubeModel for TileEntities
 *
 * @author DavixDevelop
 */
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
