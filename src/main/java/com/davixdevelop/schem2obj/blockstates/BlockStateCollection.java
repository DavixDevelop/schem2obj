package com.davixdevelop.schem2obj.blockstates;

import com.davixdevelop.schem2obj.namespace.Namespace;
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
    //Key: block name, value: Path to block state file in resource pack
    public HashMap<String, String> externalBlockStates;

    public BlockStateCollection(){
        blockStates = new HashMap<>();
        externalBlockStates = new HashMap<>();
    }

    /**
     * Get the block state
     * @param blockStateName The name of the block state
     * @return
     */
    public BlockState getBlockState(String blockStateName){
        //Check if block state is already in memory
        //Else read it from assets and at it to memory
        if(blockStates.containsKey(blockStateName)){
            return blockStates.get(blockStateName);
        }else {

            InputStream inputStream = null;

            boolean readFromAssets = true;

            if(externalBlockStates.containsKey(blockStateName)) {
                try {
                    inputStream = new FileInputStream(externalBlockStates.get(blockStateName));
                    readFromAssets = false;

                } catch (Exception ex) {
                    LogUtility.Log(String.format("Failed to read external BlockState: %s.json (%s)", blockStateName, externalBlockStates.get(blockStateName)));
                    LogUtility.Log(ex.getMessage());
                }
            }

            if(readFromAssets)
                inputStream = this.getClass().getClassLoader().getResourceAsStream(String.format("assets/minecraft/blockstates/%s.json", blockStateName));



            BlockState blockState = BlockState.readFromJson(inputStream);

            blockStates.put(blockStateName, blockState);

            return blockState;
        }
    }

    /**
     * Parse through resource pack BlockStates, and add them to externalBlockStates, for it to be read later
     * @param resourcePack Path to resource pack
     */
    public void parseResourcePack(String resourcePack){
        File resourcePackBlockStatesFolder = Paths.get(resourcePack, "assets","minecraft","blockstates").toFile();

        //Check if resource pack has a blockstates folder
        if(resourcePackBlockStatesFolder.exists() && resourcePackBlockStatesFolder.isDirectory()){
            File[] blockStates = resourcePackBlockStatesFolder.listFiles();

            if(blockStates != null){
                for(File blockState : blockStates){
                    if(blockState.isFile() && blockState.getName().endsWith(".json"))
                        externalBlockStates.put(blockState.getName().replace(".json",""), blockState.getPath());
                }
            }
        }
    }

}
