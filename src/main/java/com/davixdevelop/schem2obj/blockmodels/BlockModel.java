package com.davixdevelop.schem2obj.blockmodels;

import com.davixdevelop.schem2obj.blockmodels.json.BlockModelTemplate;
import com.davixdevelop.schem2obj.util.ArrayUtility;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent a Minecraft Block Model, with methods for
 * reading the Block Model JSON Files and getting It's objects
 * @author DavixDevelop
 */
public class BlockModel {
    String name;
    String parent;
    Boolean ambientOcclusion;
    BlockTextures textures;
    ArrayList<CubeElement> elements;
    BlockDisplay.DisplayItem fixedDisplay;

    public BlockModel(String name, String parent, Boolean ambientOcclusion, BlockTextures textures, ArrayList<CubeElement> elements, BlockDisplay.DisplayItem fixedDisplay){
        this.name = name;
        this.parent = parent;
        this.ambientOcclusion = ambientOcclusion;
        this.textures = textures;
        this.elements = elements;
        this.fixedDisplay = fixedDisplay;
    }

    //public String getName() { return name; }

    public String getParent() {
        return parent;
    }

    public Boolean getAmbientOcclusion() {
        return ambientOcclusion;
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

    public BlockDisplay.DisplayItem getFixedDisplay() {
        return fixedDisplay;
    }

    /**
     * This methods reads a JSON Block Model file and returns a BlockModel
     * @param jsonInputStream The InputStream of the JSON Block Model File
     * @return the BlockModel of the JSON file
     */
    public static BlockModel readFromJson(InputStream jsonInputStream, String modelName){
        BlockModel model;

        //Get reader from stream
        Reader reader = new InputStreamReader(jsonInputStream);
        //Deserialize the json from the reader
        BlockModelTemplate rawModel = new Gson().fromJson(reader, BlockModelTemplate.class);

        HashMap<String, String> textureVariables = new HashMap<>();
        String particle = null;

        ArrayList<CubeElement> cubeElements = new ArrayList<>();
        BlockDisplay.DisplayItem fixedDisplay = null;

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
                Boolean shade = element.shade != null && element.shade;
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

                //The reason why we call flattenArray here, is to cap the values of the cube vertices to 1.0 instead of 16.0,
                //So that the cube's are the size of 1 meter or less, rather then 16 meters
                ArrayUtility.flattenArray(from, 16);
                ArrayUtility.flattenArray(to, 16);

                if(element.rotation != null){
                    Double[] origin = null;
                    List<String> axis = null;
                    List<Double> angle = null;
                    Boolean rescale = false;

                    if(element.rotation.containsKey("origin")){
                        origin = new Double[3];
                        List<Number> rawOrigin = (List<Number>) element.rotation.get("origin");
                        origin[0] = rawOrigin.get(0).doubleValue();
                        origin[1] = 16.0 - rawOrigin.get(2).doubleValue();
                        origin[2] = rawOrigin.get(1).doubleValue();
                        ArrayUtility.flattenArray(origin, 16);
                    }

                    if(element.rotation.containsKey("angle")) {
                        angle = new ArrayList<>();
                        Object rawAngle = element.rotation.get("angle");
                        if(rawAngle instanceof Double)
                            angle.add((Double) rawAngle);
                        else if(rawAngle instanceof String){
                            String[] angles = ((String)rawAngle).split("\\|");
                            for(int a = 0; a < angles.length; a++){
                                angle.add(Double.parseDouble(angles[a]));
                            }
                        }
                    }

                    if(angle != null) {
                        if (element.rotation.containsKey("axis")) {
                            axis = new ArrayList<>();
                            String[] axsis = element.rotation.get("axis").toString().toUpperCase().split("\\|");
                            for (int a = 0; a < axsis.length; a++) {
                                if (axsis[a].equals("Z")) {
                                    axis.add("Y");
                                    angle.set(a, angle.get(a) * -1.0);
                                } else if (axsis[a].equals("Y"))
                                    axis.add("Z");
                                else
                                    axis.add(axsis[a]);
                            }
                        }
                    }

                    if(element.rotation.containsKey("rescale"))
                        rescale = (Boolean) element.rotation.get("rescale");

                    cubeRotation = new CubeElement.CubeRotation(origin, (axis != null) ? axis.toArray(new String[]{}) : null, (angle != null) ? angle.toArray(new Double[]{}) : null, rescale);
                }

                if(element.faces != null){

                    //Double MaxUVValue = 16.0;

                    for(Object face : element.faces.keySet()){
                        Double[] uv = null;
                        String texture = null;
                        String cullFace = null;
                        Double rotation = null;
                        Double tintIndex = null;

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

                            //The reason why we call flattenArrayHere, is to cap the values of the cube texture cords to 1.0 instead of 16.0,
                            //So that the cube face's uv's aren't bigger than the texture bounds
                            ArrayUtility.flattenArray(uv, 16);
                        }

                        if(faceValue.containsKey("texture"))
                            texture = (String) faceValue.get("texture");

                        if(faceValue.containsKey("cullface"))
                            cullFace = (String) faceValue.get("cullface");

                        if(faceValue.containsKey("rotation"))
                            rotation = ((Number) faceValue.get("rotation")).doubleValue();

                        if(faceValue.containsKey("tintindex"))
                            tintIndex = ((Number) faceValue.get("tintindex")).doubleValue();

                        cubeFaces.put((String) face, new CubeElement.CubeFace(uv, texture, cullFace, rotation, tintIndex));
                    }

                }

                cubeElements.add(new CubeElement(from, to, shade, cubeRotation, cubeFaces));

            }
        }

        if(rawModel.display != null && rawModel.display.fixed != null){
            BlockDisplay.DisplayItem rawFixedDisplay = rawModel.display.fixed;
            fixedDisplay = new BlockDisplay.DisplayItem();
            if(rawFixedDisplay.rotation != null)
                fixedDisplay.rotation = new Double[]{rawFixedDisplay.rotation[0], rawFixedDisplay.rotation[2] * -1.0, rawFixedDisplay.rotation[1]};
            if(rawFixedDisplay.translation != null)
                fixedDisplay.translation = new Double[]{rawFixedDisplay.translation[0] / 16.0, rawFixedDisplay.translation[2] / 16.0, rawFixedDisplay.translation[1] / 16.0};
            if(rawFixedDisplay.scale != null)
                fixedDisplay.scale = new Double[]{rawFixedDisplay.scale[0], rawFixedDisplay.scale[2], rawFixedDisplay.scale[1]};
        }

        String parentModel = rawModel.parent;
        //Check if parent model path includes the namespace, ex. "minecraft:", and remove it
        if(parentModel != null && parentModel.contains(":"))
            parentModel = parentModel.substring(parentModel.indexOf(":") + 1);


        model = new BlockModel(modelName, parentModel, rawModel.ambientOcclusion, new BlockTextures(particle, textureVariables),cubeElements, fixedDisplay);
        return model;
    }


    public BlockModel duplicate(){
        BlockTextures cloneTextures = null;
        ArrayList<CubeElement> cloneElements = new ArrayList<>();
        if(textures != null)
            cloneTextures = textures.duplicate();
        if(elements != null)
        {
            for(CubeElement element : elements){
                cloneElements.add(element.duplicate());
            }
        }
        return new BlockModel(name, parent, ambientOcclusion, cloneTextures, cloneElements, fixedDisplay);
    }

}
