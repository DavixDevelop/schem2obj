package com.davixdevelop.schem2obj.blockstates;

import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;

public class BlockStateCollection {
    //Key: block name, value: BlockStateObject
    public HashMap<String, BlockState> blockStates;

    public BlockStateCollection(){
        blockStates = new HashMap<>();
    }

    /**
     * Get the block state
     * @param blockStateName The name of the block state
     * @return
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
