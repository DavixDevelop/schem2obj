package com.davixdevelop.schem2obj.blockmodels;

import com.davixdevelop.schem2obj.blockmodels.json.BlockModelTemplate;
import com.davixdevelop.schem2obj.utilities.ArrayUtility;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent a Minecraft Block Model, with methods for reading
 * reading the Block Model JSON Files
 * @author DavixDevelop
 *
 */
public class BlockModel {
    private String name;
    private String parent;
    private String rootParent;
    private Boolean ambientocclusion;
    private BlockTextures textures;
    private ArrayList<CubeElement> elements;

    public BlockModel(String name, String parent, Boolean ambientocclusion, BlockTextures textures, ArrayList<CubeElement> elements){
        this.name = name;
        this.parent = parent;
        this.ambientocclusion = ambientocclusion;
        this.textures = textures;
        this.elements = elements;
    }

    public String getName() { return name; }

    public String getParent() {
        return parent;
    }

    public String getRootParent() {
        return rootParent;
    }

    public Boolean getAmbientOcclusion() {
        return ambientocclusion;
    }

    public ArrayList<CubeElement> getElements() {
        return elements;
    }

    public BlockTextures getTextures() {
        return textures;
    }

    public void setElements(ArrayList<CubeElement> elements) {
        this.elements = elements;
    }

    public void setRootParent(String rootParent) {
        this.rootParent = rootParent;
    }

    /**
     * This methods reads a JSON Block Model file and returns a BlockModel
     * @param jsonInputStream The InputStream of the JSON Block Model File
     * @return the BlockModel of the JSON file
     */
    public static BlockModel readFromJson(InputStream jsonInputStream, String modelName){
        BlockModel model = null;

        //Get reader from stream
        Reader reader = new InputStreamReader(jsonInputStream);
        //Deserialize the json from the reader
        BlockModelTemplate rawModel = new Gson().fromJson(reader, BlockModelTemplate.class);

        HashMap<String, String> textureVariables = new HashMap<>();
        String particle = null;

        ArrayList<CubeElement> cubeElements = new ArrayList<>();

        if(rawModel.textures != null){
            for(Object key : rawModel.textures.keySet()){
                if(key.equals("particle")){
                    particle = (String)rawModel.textures.get(key);
                }else {
                    String texture = (String) rawModel.textures.get(key);
                    textureVariables.put((String) key, texture);
                }
            }
        }

        if(rawModel.elements != null) {
            for (BlockModelTemplate.Element element : rawModel.elements) {
                Double[] from = new Double[3];
                Double[] to = new Double[3];
                Boolean shade = (element.shade == null) ? false : element.shade;
                HashMap<String, CubeElement.CubeFace> cubeFaces = new HashMap<>();
                CubeElement.CubeRotation cubeRotation = null;

                //Get cube corner start position
                for(int c = 0; c < element.from.size(); c++)
                    from[c] = element.from.get(c).doubleValue();

                //The reason why we call flattenArray here, is to cap the values of the cube vertices to 1.0 instead of 16.0,
                //So that the cube's are the size of 1 meter or less, rather then 16 meters
                //ToDo: Modify the asset's to skip this part
                from = ArrayUtility.flattenArray(from, 16);

                //Get cube corner end position
                for(int c = 0; c < element.to.size(); c++)
                    to[c] = element.to.get(c).doubleValue();

                to = ArrayUtility.flattenArray(to, 16);

                if(element.rotation != null){
                    Double[] origin = null;
                    String axis = null;
                    Double angle = null;
                    Boolean rescale = null;

                    if(element.rotation.containsKey("origin")){
                        origin = new Double[3];
                        List<Number> rawOrigin = (List<Number>) element.rotation.get("origin");
                        for(int c = 0; c < rawOrigin.size(); c++)
                            origin[c] = rawOrigin.get(c).doubleValue();
                    }

                    if(element.rotation.containsKey("axis"))
                        axis = (String) element.rotation.get("axis");

                    if(element.rotation.containsKey("angle"))
                        angle = (Double) element.rotation.get("angle");

                    if(element.rotation.containsKey("rescale"))
                        rescale = (Boolean) element.rotation.get("rescale");

                    cubeRotation = new CubeElement.CubeRotation(origin, axis, angle, rescale);
                }

                if(element.faces != null){
                    for(Object face : element.faces.keySet()){
                        Double[] uv = null;
                        String texture = null;
                        String cullface = null;
                        Double rotation = null;
                        Double tintindex = null;

                        Map<String, Object> faceValue = (Map<String, Object>) element.faces.get(face);
                        if(faceValue.containsKey("uv")){
                            uv = new Double[4];
                            List<Number> rawUv = (List<Number>) faceValue.get("uv");
                            for(int c = 0; c < rawUv.size(); c++)
                                uv[c] = rawUv.get(c).doubleValue();

                            //The reason why we call flattenArrayHere, is to cap the values of the cube texture coords to 1.0 instead of 16.0,
                            //So that the cube face's uv's aren't bigger than the texture bounds
                            uv = ArrayUtility.flattenArray(uv, 16);
                        }

                        if(faceValue.containsKey("texture"))
                            texture = (String) faceValue.get("texture");

                        if(faceValue.containsKey("cullface"))
                            cullface = (String) faceValue.get("cullface");

                        if(faceValue.containsKey("rotation"))
                            rotation = ((Number) faceValue.get("rotation")).doubleValue();

                        if(faceValue.containsKey("tintindex"))
                            tintindex = ((Number) faceValue.get("tintindex")).doubleValue();

                        cubeFaces.put((String) face, new CubeElement.CubeFace(uv, texture, cullface, rotation, tintindex));
                    }
                }

                cubeElements.add(new CubeElement(from, to, shade, cubeRotation, cubeFaces));

            }
        }

        model = new BlockModel(modelName, rawModel.parent, rawModel.ambientocclusion, new BlockTextures(particle, textureVariables),cubeElements);
        return model;
    }

}
