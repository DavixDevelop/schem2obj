package com.davixdevelop.schem2obj.namespace.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonBlocks {
    String ID;
    Map<Integer, JsonBlockState> blockStates;
    Integer defaultMetaID;
    List<String> entityTileKeys;
    Map<String, Object> defaultCustomData;

    String itemResource;
    String itemID;

    public String getID() {
        return ID;
    }

    public Map<Integer, JsonBlockState> getBlockStates() {
        return blockStates;
    }

    public void setBlockStates(Map<Integer, JsonBlockState> blockStates) {
        this.blockStates = blockStates;
    }

    public Integer getDefaultMetaID() {
        return defaultMetaID;
    }

    public List<String> getEntityTileKeys() {
        return entityTileKeys;
    }

    public Map<String, Object> getDefaultCustomData() {
        return defaultCustomData;
    }

    public JsonBlocks(String ID, Map<Integer, JsonBlockState> blockStates, Integer defaultMetaID, List<String> entityTileKeys, Map<String, Object> defaultCustomData){
        this.ID = ID;
        this.blockStates = blockStates;
        this.defaultMetaID = defaultMetaID;
        this.entityTileKeys = entityTileKeys;
        this.defaultCustomData = defaultCustomData;
    }

    public String getItemResource() {
        return itemResource;
    }

    public String getItemID() {
        return itemID;
    }
}
