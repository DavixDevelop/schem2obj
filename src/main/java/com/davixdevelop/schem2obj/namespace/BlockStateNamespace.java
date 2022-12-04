package com.davixdevelop.schem2obj.namespace;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockStateNamespace {
    Integer MetaID;
    String name;
    Double lightValue;
    Map<String, String> data;

    public BlockStateNamespace(Integer MetaID, String name, Double lightValue, Map<String, String> data){
        this.MetaID = MetaID;
        this.name = name;
        this.lightValue = lightValue;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public Double getLightValue() {
        return lightValue;
    }

    public String getData(String key) {
        return data.get(key);
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public void setData(String key, String value){this.data.put(key, value);}

    public BlockStateNamespace duplicate(){
        Map<String, String> cloneData = new LinkedHashMap<>();
        if(data != null)
            cloneData = new LinkedHashMap<>(data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return new BlockStateNamespace(MetaID , name, lightValue, cloneData);
    }

    public static void cloneData(BlockStateNamespace blockStateNamespace, Map<String, String> data){
        for(String prop : data.keySet())
            blockStateNamespace.data.put(prop, data.get(prop));
    }
}
