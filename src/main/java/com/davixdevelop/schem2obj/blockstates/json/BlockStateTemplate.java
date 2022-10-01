package com.davixdevelop.schem2obj.blockstates.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockStateTemplate {
    public Map<String, Object> variants;
    public List<Part> multipart;

    public class Part{
        public Map<String, Object> when;
        public Object apply;
    }

    public class Apply {
        public String model;
        public Number x;
        public Number y;
        public Boolean uvlock;
        public Number weight;
    }

}
