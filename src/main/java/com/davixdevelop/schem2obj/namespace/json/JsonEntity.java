package com.davixdevelop.schem2obj.namespace.json;

public class JsonEntity {
    String resourcePath;
    String ID;

    public JsonEntity(String resourcePath, String ID){
        this.resourcePath = resourcePath;
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
