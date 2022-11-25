package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.cubemodels.CubeModelFactory;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

/**
 * The CubeModel for all slab block variants
 *
 * @author DavixDevelop
 */
public class SlabCubeModel extends BlockCubeModel{
    private String half = "";

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        half = blockNamespace.getData().get("half");

        //Convert block namespace to cube model
        super.baseConvert(blockNamespace);

        return true;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        if(adjacent instanceof SlabCubeModel){
            SlabCubeModel adjSlab = (SlabCubeModel) adjacent;
            return adjSlab.half.equals(half);
        }

        return !CubeModelFactory.isTranslucentOrNotFull(adjacent);
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new SlabCubeModel();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(ICubeModel clone) {
        SlabCubeModel slabClone = (SlabCubeModel) clone;
        half = slabClone.half;
        super.copy(clone);
    }
}
