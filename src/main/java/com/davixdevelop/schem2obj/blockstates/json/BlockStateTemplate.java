package com.davixdevelop.schem2obj.blockstates.json;

import java.util.List;
import java.util.Map;

/**
 * JSON Template for BlockState
 * @author DavixDevelop
 */
public class BlockStateTemplate {
    public Map<String, Object> variants;
    public List<Part> multipart;

    public static class Part{
        public Map<String, Object> when;
        public Object apply;
    }

    public static class Apply {
        public String model;
        public Number x;
        public Number y;
        public Boolean uvLock;
        public Number weight;
    }

}
