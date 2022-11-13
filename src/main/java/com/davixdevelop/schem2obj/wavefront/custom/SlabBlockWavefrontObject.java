package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;

public class SlabBlockWavefrontObject extends BlockWavefrontObject {
    private String half = "";

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        half = blockNamespace.getData().get("half");

        //Convert block namespace to OBJ
        super.baseConvert(blockNamespace);

        return true;
    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        if(adjacent instanceof SlabBlockWavefrontObject){
            SlabBlockWavefrontObject adjSlab = (SlabBlockWavefrontObject) adjacent;
            return adjSlab.half.equals(half);
        }

        return true;
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new SlabBlockWavefrontObject();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(IWavefrontObject clone) {
        SlabBlockWavefrontObject slabClone = (SlabBlockWavefrontObject)clone;
        half = slabClone.half;
        super.copy(clone);
    }
}
