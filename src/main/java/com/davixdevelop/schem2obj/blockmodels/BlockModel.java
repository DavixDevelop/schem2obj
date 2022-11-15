package com.davixdevelop.schem2obj.blockmodels;

import com.davixdevelop.schem2obj.blockmodels.json.BlockModelTemplate;
import com.davixdevelop.schem2obj.util.ArrayUtility;
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

    //public String getName() { return name; }

    public String getParent() {
        return parent;
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

                //Get cube corner start and end position
                //As MC uses the y axis as the z axis, swap them, and the +z axis is the -y axis,
                //"move" the cube
                List<Number> MC_From = element.from;
                List<Number> MC_To = element.to;

                from[0] = MC_From.get(0).doubleValue();
                from[1] = 16.0 - MC_To.get(2).doubleValue();
                from[2] = MC_From.get(1).doubleValue();

                to[0] = MC_To.get(0).doubleValue();
                to[1] = 16.0 - MC_From.get(2).doubleValue();
                to[2] = MC_To.get(1).doubleValue();

                /*for(int c = 0; c < element.from.size(); c++)
                    from[(c == 1) ? 2 : (c == 2) ? 1 : c] = element.from.get(c).doubleValue();

                for(int c = 0; c < element.to.size(); c++)
                    to[(c == 1) ? 2 : (c == 2) ? 1 : c] = element.to.get(c).doubleValue();*/

                //The reason why we call flattenArray here, is to cap the values of the cube vertices to 1.0 instead of 16.0,
                //So that the cube's are the size of 1 meter or less, rather then 16 meters
                //ToDo: Modify the asset's to skip this part
                from = ArrayUtility.flattenArray(from, 16);
                to = ArrayUtility.flattenArray(to, 16);

                if(element.rotation != null){
                    Double[] origin = null;
                    String axis = null;
                    Double angle = null;
                    Boolean rescale = false;

                    if(element.rotation.containsKey("origin")){
                        origin = new Double[3];
                        List<Number> rawOrigin = (List<Number>) element.rotation.get("origin");
                        origin[0] = rawOrigin.get(0).doubleValue();
                        origin[1] = 16.0 - rawOrigin.get(2).doubleValue();
                        origin[2] = rawOrigin.get(1).doubleValue();
                        origin = ArrayUtility.flattenArray(origin, 16);
                    }

                    if(element.rotation.containsKey("angle"))
                        angle = (Double) element.rotation.get("angle");

                    if(element.rotation.containsKey("axis")) {
                        axis = element.rotation.get("axis").toString().toUpperCase();
                        if(axis.equals("Z")) {
                            axis = "Y";
                            angle *= -1;
                        }
                        else if(axis.equals("Y"))
                            axis = "Z";
                    }

                    if(element.rotation.containsKey("rescale"))
                        rescale = (Boolean) element.rotation.get("rescale");

                    cubeRotation = new CubeElement.CubeRotation(origin, axis, angle, rescale);
                }

                if(element.faces != null){

                    //Double MaxUVValue = 16.0;

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

                            //As Minecraft UV axis is top-left (0,0) to bottom-right (16,16),
                            //convert it to bottom-left to top-right
                            uv[0] = rawUv.get(0).doubleValue(); //x1
                            uv[1] = 16.0 - rawUv.get(3).doubleValue(); //16 - y2
                            uv[2] = rawUv.get(2).doubleValue(); //x2
                            uv[3] = 16.0 - rawUv.get(1).doubleValue(); //16 - y2

                            /*for(int c = 0; c < rawUv.size(); c++){
                                uv[c] = rawUv.get(c).doubleValue();

                                //Find max UV Value
                                //if(uv[c] > MaxUVValue)
                                //    MaxUVValue = uv[c];
                            }*/

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

                    /*if(MaxUVValue / 16 > 1.0){
                        Integer maxVal = ((Double)(MaxUVValue / 16.0)).intValue();
                        MaxUVValue = maxVal.doubleValue() * 16.0;
                    }*/

                    /*
                    //Convert faces uv's from top left, bottom right to bottom left, top right that obj uses
                    for(String face : cubeFaces.keySet()){
                        CubeElement.CubeFace cubeFace = cubeFaces.get(face);
                        Double[] uv = cubeFace.getUv();
                        if(uv != null){
                            double bottom = uv[3];
                            double top = uv[1];
                            uv[1] = MaxUVValue - bottom;
                            uv[3] = MaxUVValue - top;


                            cubeFace.setUv(uv);
                        }
                    }*/
                }

                cubeElements.add(new CubeElement(from, to, shade, cubeRotation, cubeFaces));

            }
        }

        model = new BlockModel(modelName, rawModel.parent, rawModel.ambientocclusion, new BlockTextures(particle, textureVariables),cubeElements);
        return model;
    }

    public BlockModel clone(){
        BlockTextures cloneTextures = null;
        ArrayList<CubeElement> cloneElements = new ArrayList<>();
        if(textures != null)
            cloneTextures = textures.clone();
        if(elements != null)
            cloneElements = new ArrayList<>(elements);
        return new BlockModel(name, parent, ambientocclusion, cloneTextures, cloneElements);
    }

}
