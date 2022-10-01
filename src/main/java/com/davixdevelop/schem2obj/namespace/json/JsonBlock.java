package com.davixdevelop.schem2obj.namespace.json;

import java.util.Map;

public class JsonBlock {
    private int ID;
    private Map<String, JsonBlockState> variants;

    public int getID() {
        return ID;
    }

    public Map<String, JsonBlockState> getVariants() {
        return variants;
    }

    public class JsonBlockState{
        private String variantName;
        private Integer lightValue;
        private Map<String, String> properties;

        public String getVariantName() {
            return variantName;
        }

        public Integer getLightValue() {
            return lightValue;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }
}
