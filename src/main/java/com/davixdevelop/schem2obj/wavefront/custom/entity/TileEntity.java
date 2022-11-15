package com.davixdevelop.schem2obj.wavefront.custom.entity;

import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontObject;

import java.util.ArrayList;

public class TileEntity extends WavefrontObject {
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        return false;
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new TileEntity();
        clone.copy(this);

        return clone;
    }
}
