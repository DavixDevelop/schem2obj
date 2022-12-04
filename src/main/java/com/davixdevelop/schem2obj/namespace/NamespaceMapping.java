package com.davixdevelop.schem2obj.namespace;

import com.davixdevelop.schem2obj.namespace.json.JsonBlockState;
import com.davixdevelop.schem2obj.namespace.json.JsonBlocks;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NamespaceMapping {
    Map<String, Namespace> namespaceMap;
    Map<String, String> itemMap;
    Map<String, String> idMapping;
    Map<String, String> itemMapping;

    public NamespaceMapping(){
        namespaceMap = new LinkedHashMap<>();
        itemMap = new HashMap<>();
        idMapping = new LinkedHashMap<>();
        itemMapping = new LinkedHashMap<>();


        Gson gson = new Gson();

        //Get the input stream of items.json
        //items.json was generated using ExtractBlocks (https://github.com/DavixDevelop/ExtractBlocks)
        InputStream itemsStream = this.getClass().getClassLoader().getResourceAsStream("assets/minecraft/items.json");

        if(itemsStream != null){
            //Get reader for input stream
            Reader reader = new InputStreamReader(itemsStream);
            //Deserialize the JSON to Map
            Map<String, JsonBlocks> items = gson.fromJson(reader, new TypeToken<Map<String, JsonBlocks>>(){}.getType());

            for(String resource : items.keySet()){
                JsonBlocks item = items.get(resource);

                String domain = resource.substring(0, resource.indexOf(":"));
                String path = resource.substring(resource.indexOf(":") + 1);

                Map<Integer, JsonBlockState> jsonBlockStateMap = item.getBlockStates();
                Map<Integer, BlockStateNamespace> blockStateNamespaceMap = new LinkedHashMap<>();
                for(Integer metaID : jsonBlockStateMap.keySet()){
                    JsonBlockState jsonBlockState = jsonBlockStateMap.get(metaID);
                    blockStateNamespaceMap.put(metaID, new BlockStateNamespace(metaID, jsonBlockState.getBlockStateName(), jsonBlockState.getLightValue().doubleValue(), jsonBlockState.getProperties()));
                }

                EntityValues defaultCustomData = new EntityValues();
                if(!item.getDefaultCustomData().isEmpty()){
                    defaultCustomData.parseMap(item.getDefaultCustomData());
                }

                namespaceMap.put(resource, new Namespace(item.getID(), domain, path, blockStateNamespaceMap, item.getDefaultMetaID(), item.getEntityTileKeys(), defaultCustomData));
                idMapping.put(item.getID(), resource);

                if(item.getItemResource() != null && item.getItemID() != null) {
                    if (!itemMap.containsKey(item.getItemID()))
                        itemMap.put(item.getItemID(), item.getItemResource());
                }

                if(item.getItemResource() != null) {
                    if (!itemMapping.containsKey(item.getItemResource()))
                        itemMapping.put(item.getItemResource(), resource);
                }

            }
        }
    }

    /**
     * Get Namespace from Block ID:Meta
     * @param ID The ID of the requested block
     * @param metaID The Meta of the requested block
     * @return the Namespace of the Block
     */
    public Namespace getBlockNamespace(String ID, Integer metaID){
        if(idMapping.containsKey(ID)){
            String resource = idMapping.get(ID);
            Namespace namespace = namespaceMap.get(resource).duplicate();
            namespace.setDefaultBlockState(metaID);

            return namespace;
        }

        return null;
    }

    public Namespace getNamespace(String resource){
        if(namespaceMap.containsKey(resource))
            return namespaceMap.get(resource).duplicate();
        else if(resource.contains(":")){
            //ToDo: Remove the "builtin" domain from the namespace mapping once all entity tiles are supported
            String resourcePath = resource.substring(resource.indexOf(":") + 1);
            String builtinPath = String.format("builtin:%s", resourcePath);
            if(namespaceMap.containsKey(builtinPath))
                return namespaceMap.get(builtinPath);
        }

        if(itemMapping.containsKey(resource))
            return namespaceMap.get(itemMapping.get(resource)).duplicate();

        if(idMapping.containsKey(resource))
            return namespaceMap.get(idMapping.get(resource)).duplicate();

        if(itemMap.containsKey(resource))
            return namespaceMap.get(itemMapping.get(itemMap.get(resource))).duplicate();

        return null;
    }
}
