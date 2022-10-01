package com.davixdevelop.schem2obj.blockstates;

import com.davixdevelop.schem2obj.namespace.Namespace;

import java.io.InputStream;
import java.util.HashMap;

public class BlockStateCollection {
    //Key: block name
    public HashMap<String, BlockState> blockStates;

    public BlockStateCollection(){
        blockStates = new HashMap<>();
    }

    public BlockState getBlockState(Namespace namespace){
        //Check if block state is already in memory
        //Else read it from assets and at it to memory
        if(blockStates.containsKey(namespace.getName())){
            return blockStates.get(namespace.getName());
        }else {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(String.format("assets/minecraft/blockstates/%s.json", namespace.getName()));
            BlockState blockState = BlockState.readFromJson(inputStream);

            blockStates.put(namespace.getName(), blockState);

            return blockState;
        }
    }
}
