package com.davixdevelop.schem2obj.namespace;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.davixdevelop.schem2obj.namespace.json.JsonBlock;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class BlockMapping {
    private HashMap<String, Namespace> blockMapping;

    public BlockMapping(){
        blockMapping = new HashMap<String, Namespace>();

        Gson gson = new Gson();

        //Get the input stream of blocks.json
        //blocks.json was generated using ExtractBlocks (https://github.com/DavixDevelop/ExtractBlocks)
        InputStream blocksStream = this.getClass().getClassLoader().getResourceAsStream("assets/minecraft/blocks.json");

        //Get reader for input stream
        Reader reader = new InputStreamReader(blocksStream);
        //Deserialize the JSON to a Map
        Map<String, Object> blocks = gson.fromJson(reader, Map.class);

        for(String block : blocks.keySet()){
            //Convert each map value back to JSON
            JsonElement rawJsonBlock = gson.toJsonTree(blocks.get(block));
            JsonBlock jsonBlock = gson.fromJson(rawJsonBlock, JsonBlock.class);

            //Get the block variants
            Map<String, JsonBlock.JsonBlockState> variants = jsonBlock.getVariants();

            //Read the block domain
            String blockDomain = block.substring(0, block.indexOf(":"));

            for(String meta : variants.keySet()){
                JsonBlock.JsonBlockState blockState = variants.get(meta);

                //Construct the meta id from the blockID:meta
                String MetaID = String.format("%d:%s",jsonBlock.getID() ,meta);
                HashMap<String, String> props = new HashMap<>();

                Map<String, String> blockProps = blockState.getProperties();
                //Copy block state properties to props
                for(String propName : blockProps.keySet()){
                    //ToDo: Find prop's in blocks.json that are obsolete (not in any blockstate)
                    //Ignore legacy_data prop
                    if(propName.equals("legacy_data"))
                        continue;
                    props.put(propName, blockProps.get(propName));
                }
                blockMapping.put(MetaID, new Namespace(MetaID, blockDomain,blockState.getVariantName(),props,blockState.getLightValue().doubleValue()));
            }
        }
    }

    /**
     * Get Namespace from Block ID:Meta
     * @param ID The ID:Meta of the requested block
     * @return the Namespace of the Block
     */
    public Namespace getBlockNamespace(String ID){
        return blockMapping.get(ID);
    }
}
