package com.davixdevelop.schem2obj.namespace;

import com.davixdevelop.schem2obj.schematic.EntityValues;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a Minecraft namespace, with a field for additional data
 * @author DavixDevelop
 */
public class Namespace {
    String id;
    String domain;
    String type;

    Map<Integer, BlockStateNamespace> blockStates;
    List<String> validTileEntityKeys;
    EntityValues defaultTileEntityValues;

    Integer defaultMetaID;

    Integer stockMetaID;
    EntityValues customData;

    DISPLAY_MODE displayMode;

    public Namespace(String id, String domain, String type, Map<Integer, BlockStateNamespace> blockStates, Integer defaultMetaID, List<String> validTileEntityKeys, EntityValues defaultTileEntityValues) {
        this.id = id;
        this.domain = domain;
        this.type = type;
        this.blockStates = blockStates;
        this.validTileEntityKeys = validTileEntityKeys;
        this.defaultMetaID = defaultMetaID;
        this.stockMetaID = defaultMetaID;

        this.defaultTileEntityValues = defaultTileEntityValues;

        displayMode = DISPLAY_MODE.BLOCK;
    }

    public String getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public String getType() {
        return type;
    }

    public BlockStateNamespace getDefaultBlockState() {
        if(blockStates.containsKey(defaultMetaID))
            return blockStates.get(defaultMetaID);
        else
            return blockStates.get(stockMetaID);
    }

    public void setDefaultBlockState(Integer defaultMetaID){
        this.defaultMetaID = defaultMetaID;
    }

    public void setCustomData(EntityValues customData) {
        this.customData = customData;
    }

    public EntityValues getCustomData() {
        return customData;
    }

    public Set<Integer> getMetaIDS(){
        return blockStates.keySet();
    }

    public List<String> getValidEntityKeys() {
        return validTileEntityKeys;
    }

    public EntityValues getDefaultEntityValues() {
        return defaultTileEntityValues;
    }

    /**
     * Return the resource path the namespace.
     * If the namespace has no blockstates, this is the type,
     * else return the name of the blockstate (variant).
     * @return The resource path to this namespace
     */
    public String getResource(){
        if(blockStates.keySet().isEmpty())
            return type;
        else
            if(blockStates.containsKey(defaultMetaID))
                return blockStates.get(defaultMetaID).name;
            else
                return blockStates.get(stockMetaID).name;
    }

    public DISPLAY_MODE getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DISPLAY_MODE displayMode) {
        this.displayMode = displayMode;
    }

    public Namespace duplicate(){
        Map<Integer, BlockStateNamespace> cloneBlockstates = new LinkedHashMap<>();
        for(Integer metaID : blockStates.keySet()){
            cloneBlockstates.put(metaID, blockStates.get(metaID).duplicate());
        }

        EntityValues defaultEntityTileValues = new EntityValues();
        if(!defaultTileEntityValues.isEmpty())
            defaultEntityTileValues = defaultTileEntityValues.duplicate();

        Namespace clone = new Namespace(id, domain, type, cloneBlockstates, defaultMetaID, new ArrayList<>(validTileEntityKeys), defaultEntityTileValues);
        clone.stockMetaID = stockMetaID;
        if(customData != null)
        {
            clone.customData = customData.duplicate();
        }

        clone.displayMode = DISPLAY_MODE.valueOf(displayMode.toString());

        return clone;
    }

    public enum DISPLAY_MODE{
        BLOCK,
        FIXED
    }
}
