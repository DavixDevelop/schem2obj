package com.davixdevelop.schem2obj.blockmodels.json;

import java.util.List;
import java.util.Map;

/** JSON Template for a BlockModel
 * @author DavixDevelop
 */
public class BlockModelTemplate {
    public String parent;
    public Boolean ambientOcclusion;
    public Map<?, ?> textures;
    public List<Element> elements;

    public static class Element {
        public List<Number> from;
        public List<Number> to;
        public Boolean shade;
        public Map<?, ?> rotation;
        public Map<?, ?> faces;
    }
}
