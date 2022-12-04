package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.cubemodels.CubeModelFactory;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for all slab block variants
 *
 * @author DavixDevelop
 */
public class SlabCubeModel extends BlockCubeModel{
    private String half = "";

    @Override
    public boolean fromNamespace(Namespace namespace) {
        half = namespace.getDefaultBlockState().getData("half");

        //Convert block namespace to cube model
        super.baseConvert(namespace);

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
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getDefaultBlockState().getName());
        key.put("half", namespace.getDefaultBlockState().getData("half"));

        return key;
    }

    @Override
    public ICubeModel duplicate() {
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
