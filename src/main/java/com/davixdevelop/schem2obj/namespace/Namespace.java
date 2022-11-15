package com.davixdevelop.schem2obj.namespace;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a Minecraft namespace, with a field for additional data
 * @author DavixDevelop
 */
public class Namespace {
    private String id;
    private String domain;
    private String name;
    private String type;
    private HashMap<String, String> data;

    private Double lightValue;

    public Namespace(String id, String domain, String name, String type, HashMap<String, String> data,Double lightValue){
        this.id = id;
        this.domain = domain;
        this.name = name;
        this.type = type;
        this.data = data;
        this.lightValue = lightValue;
    }

    public String getId() {
        return id;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public String getData(String key) {
        return data.get(key);
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    public void setData(String key, String value) {this.data.put(key, value);}

    public Double getLightValue() {
        return lightValue;
    }

    public Namespace clone(){
        HashMap<String, String> cloneData = new HashMap<>();
        if(data != null || !data.isEmpty())
            cloneData = new HashMap<>(data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue)));

        return new Namespace(id, domain, name, type, cloneData, lightValue);
    }

    /**
     * Copy the a map of state to the namespace data
     * @param namespace The namespace to copy to
     * @param states A map of state to copy from
     */
    public static void copyStatesToNamespace(Namespace namespace, Map<String, String> states){
        for(String prop : states.keySet())
            namespace.getData().put(prop, states.get(prop));
    }
}
