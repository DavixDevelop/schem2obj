package com.davixdevelop.schem2obj.blockmodels;

import com.davixdevelop.schem2obj.blockmodels.json.BlockModelTemplate;

public class BlockDisplay {
    public DisplayItem fixed;

    public static class DisplayItem {
        public Double[] rotation;
        public Double[] translation;
        public Double[] scale;
    }
}
