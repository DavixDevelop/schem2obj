package com.davixdevelop.schem2obj.blockstates;

import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represent a collection of Block States
 * @author DavixDevelop
 */
public class BlockStateCollection {
    //Key: block name, value: BlockStateObject
    public ConcurrentMap<String, BlockState> blockStates;

    public BlockStateCollection(){
        blockStates = new ConcurrentHashMap<>();
    }

    /**
     * Get the block state
     * @param blockStateName The name of the block state
     * @return the BlockState
     */
    public BlockState getBlockState(String blockStateName){
        //Check if block state is already in memory
        //Else read it from Resources and add it to memory
        if(blockStates.containsKey(blockStateName)){
            return blockStates.get(blockStateName);
        }else {
            String blockStatePath = ResourceLoader.getResourcePath("blockstates", blockStateName, "json");

            InputStream inputStream = ResourceLoader.getResource(blockStatePath);
            //this.getClass().getClassLoader().getResourceAsStream(String.format("assets/minecraft/blockstates/%s.json", blockStateName));
            BlockState blockState = BlockState.readFromJson(inputStream);

            blockStates.put(blockStateName, blockState);

            return blockState;
        }
    }

}
