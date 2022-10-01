package com.davixdevelop.schem2obj.namespace;

import java.util.HashMap;

/**
 * Represents a Minecraft namespace, with a field for additional data
 * @author DavixDevelop
 */
public class Namespace {
    private String id;
    private String domain;
    private String name;
    public HashMap<String, String> data;

    private Double lightValue;

    public Namespace(String id, String domain, String name, HashMap<String, String> data,Double lightValue){
        this.id = id;
        this.domain = domain;
        this.name = name;
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

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    public Double getLightValue() {
        return lightValue;
    }
}
