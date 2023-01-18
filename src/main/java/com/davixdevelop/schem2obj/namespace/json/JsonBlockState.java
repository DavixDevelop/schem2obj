package com.davixdevelop.schem2obj.namespace.json;

import java.util.Map;

public class JsonBlockState {
    String blockStateName;
    Integer lightValue;
    Integer defaultColor;
    Map<String, String> properties;

    public JsonBlockState(String blockStateName, Integer lightValue, Integer defaultColor, Map<String, String> properties) {
        this.blockStateName = blockStateName;
        this.defaultColor = defaultColor;
        this.lightValue = lightValue;
        this.properties = properties;
    }

    public String getBlockStateName() {
        return blockStateName;
    }

    public Integer getLightValue() {
        return lightValue;
    }

    public Integer getDefaultColor() {
        return defaultColor;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
