package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.blockmodels.BlockModelCollection;
import com.davixdevelop.schem2obj.blockstates.BlockStateCollection;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.material.MaterialCollection;

import java.util.HashMap;

public class WavefrontCollection {

    public static final BlockModelCollection BLOCK_MODELS = new BlockModelCollection();
    public static final BlockStateCollection BLOCKS_STATES = new BlockStateCollection();
    public static final MaterialCollection BLOCK_MATERIALS = new MaterialCollection();

    private HashMap<Namespace, IWavefrontObject> wavefrontObjecs;

    public WavefrontCollection(){
        wavefrontObjecs = new HashMap<>();
    }

    public IWavefrontObject fromNamespace(Namespace blockNamespace){
        if(wavefrontObjecs.containsKey(blockNamespace))
            return wavefrontObjecs.get(blockNamespace);
        else{
            IWavefrontObject block = new BlockWavefrontObject();

            block.fromNamespace(blockNamespace);

            wavefrontObjecs.put(blockNamespace, block);

            return block;

        }
    }
}
