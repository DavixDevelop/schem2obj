package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.blockmodels.BlockModelCollection;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.blockstates.BlockStateCollection;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.custom.GlassBlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.custom.GlassPaneWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.custom.GrassBlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.custom.MagmaBlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.material.MaterialCollection;

import java.util.HashMap;

public class WavefrontCollection {

    private HashMap<Namespace, IWavefrontObject> wavefrontObjecs;

    public WavefrontCollection(){
        wavefrontObjecs = new HashMap<>();
    }

    public IWavefrontObject fromNamespace(Namespace blockNamespace){
        if(wavefrontObjecs.containsKey(blockNamespace))
            return wavefrontObjecs.get(blockNamespace).clone();
        else{
            IWavefrontObject block = getType(blockNamespace);

            //Only store object in memory if to does not have random variants (multiple variants in "variants" field)
            //If it does recreate it every time
            if(block.fromNamespace(blockNamespace))
                wavefrontObjecs.put(blockNamespace, block);

            return block.clone();

        }
    }

    public static IWavefrontObject getType(Namespace blockNamespace){

        if(blockNamespace.getName().contains("glass_pane"))
            return new GlassPaneWavefrontObject();

        if(blockNamespace.getName().contains("glass"))
            return new GlassBlockWavefrontObject();

        switch (blockNamespace.getName()){
            case "grass":
                return new GrassBlockWavefrontObject();
            case "magma":
                return  new MagmaBlockWavefrontObject();
            default:
                return new BlockWavefrontObject();
        }
    }
}
