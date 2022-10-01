package com.davixdevelop.schem2obj.blockmodels.json;

import java.util.List;
import java.util.Map;

public class BlockModelTemplate {
    public String parent;
    public Boolean ambientocclusion;
    public Map textures;
    public List<Element> elements;

    public class Element {
        public List<Number> from;
        public List<Number> to;
        public Boolean shade;
        public Map rotation;
        public Map faces;
    }
}
