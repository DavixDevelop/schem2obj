package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayUtility;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.wavefront.custom.GlassBlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.custom.GlassPaneWavefrontObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WavefrontObject implements IWavefrontObject {
    private String name;
    private ArrayList<Double[]> v;
    private ArrayList<Double[]> vt;
    private ArrayList<Double[]> vn;
    //Key: materials used Value: List of faces, and It's indices
    private HashMap<String, ArrayList<ArrayList<Integer[]>>> f;
    //Map<Facing (Orientation):String, Map<MaterialName:String, List<FaceIndex:Integer>>>
    private HashMap<String, HashMap<String, ArrayList<Integer>>> facing;

    public WavefrontObject(){
        v = new ArrayList<>();
        vt = new ArrayList<>();
        vn = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Double[]> getVertices() {
        return v;
    }

    public void setVertices(ArrayList<Double[]> v) {
        this.v = v;
    }

    public ArrayList<Double[]> getVertexNormals() {
        return vn;
    }

    public void setVertexNormals(ArrayList<Double[]> vn) {
        this.vn = vn;
    }

    public ArrayList<Double[]> getTextureCoordinates() {
        return vt;
    }

    public void setTextureCoordinates(ArrayList<Double[]> vt) {
        this.vt = vt;
    }

    public HashMap<String, ArrayList<ArrayList<Integer[]>>> getMaterialFaces() {
        return f;
    }

    public void setMaterialFaces(HashMap<String, ArrayList<ArrayList<Integer[]>>> f) {
        this.f = f;
    }

    @Override
    public boolean fromNamespace(Namespace blockNamespace) { return false; }

    @Override
    public HashMap<String, HashMap<String, ArrayList<Integer>>> getBoundingFaces() {
        return facing;
    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        //If the adjacent block is glass block or glass pane don't check for collision
        return !WavefrontCollection.isTranslucentOrNotFull(adjacent);
    }

    /**
     * Set the bounding faces of the object
     * @param facing Map<Facing (Orientation):String, Map<MaterialName:String, List<FaceIndex:Integer>>>
     */
    public void setBoundingFaces(HashMap<String, HashMap<String, ArrayList<Integer>>> facing) {
        this.facing = facing;
    }

    /**
     * Convert block models to wavefront object.
     * The indexes in the object are treated as if the object is the first one in the file
     * @param blockModels The variant block models to convert
     * @param blockNamespace The namespace of the block
     * @return the block wavefront object
     */
    public void toObj(String name, ArrayList<VariantModels> blockModels, Namespace blockNamespace){
        //Set the name of the object
        setName(name);

        //Each item is an array with the following values [vx, vy, vz]
        HashedDoubleList vertices = new HashedDoubleList();
        ArrayList<Double[]> normalsArray = new ArrayList<>();
        HashedDoubleList textureCoordinates = new HashedDoubleList();
        //Map of materialName and It's faces, where each face consists of an list of array indices
        //Each indice consists of the vertex index, texture coordinate index and vertex normal index
        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = new HashMap<>();

        //A map that keeps track of what faces (indexes) bounds the block bounding box on that specific orientation
        //Map<Facing (Orientation):String, Map<MaterialName:String, List<FaceIndex:Integer>>>
        HashMap<String, HashMap<String, ArrayList<Integer>>> boundingFaces = new HashMap<>();

        //Extract the default materials from the textures that the models use
        HashMap<String, HashMap<String, String>> modelsMaterials = WavefrontUtility.texturesToMaterials(blockModels, blockNamespace);

        for(VariantModels variantModels : blockModels) {

            //Minecraft usually parses the block models from bottom to top (ex. from block -> dirt)
            //This approach overrides the generated elements, if the current model has a parents and elements
            //In other words if the current model has a parent and elements, ignore the previous generated elements and
            //Use the current model elements as the block elements
            //To emulate this approach when parsing the models from top to bottom (ex. from dirt -> block),
            //to skip generating elements that would be discarded (written over) later, we need to
            //check if the variant elements were already generated (mark it), and the current model has an parent and elements
            //If it does, skip the current model, else generate the elements.
            boolean generatedElements = false;

            for (BlockModel model : variantModels.getModels()) {
                ArrayList<CubeElement> elements = model.getElements();
                BlockState.Variant variant = variantModels.getVariant();

                if (!elements.isEmpty()) {

                    //Ignore the model (m) elements, if the model has elements and a parent and the elements were already set
                    if (generatedElements && !elements.isEmpty() && model.getParent() != null)
                        continue;

                    //Each element represents a cube
                    for (CubeElement element : elements) {
                        boolean uvLock = false;
                        ArrayVector.MatrixRotation rotationX = null;
                        ArrayVector.MatrixRotation rotationY = null;

                        if (variant != null) {
                            uvLock = variantModels.getVariant().getUvlock();

                            //Check if variant model should be rotated
                            if (variant.getX() != null)
                                rotationX = new ArrayVector.MatrixRotation(variant.getX(), "X");

                            if (variant.getY() != null)
                                rotationY = new ArrayVector.MatrixRotation(variant.getY(), "Z");
                        }


                        //Convert the cube to obj
                        WavefrontUtility.convertCubeToWavefront(element, uvLock, rotationX, rotationY, vertices, textureCoordinates, faces, boundingFaces, modelsMaterials.get(variant.getModel()));
                    }

                    //Mark that the variant has generated elements
                    generatedElements = true;
                }
            }
        }

        //Create normals for object
        WavefrontUtility.createNormals(normalsArray, vertices, faces);

        //Get vertex list
        ArrayList<Double[]> verticesArray = vertices.toList();

        //Normalize vertex normals
        WavefrontUtility.normalizeNormals(normalsArray);

        setVertices(verticesArray);
        setVertexNormals(normalsArray);
        setTextureCoordinates(textureCoordinates.toList());
        setMaterialFaces(faces);
        setBoundingFaces(boundingFaces);
    }

    /**
     * Create the obj from one or more cubes
     * @param name The name of the object
     * @param uvLock Set to true, to keep the uv's in place, and just rotate the cube
     * @param rotationX RotationMatrix around the X axis
     * @param rotationY R0tationMatrix around the Z axis
     * @param modelsMaterials A map of texture variables and the material it's set to, ex key: #north, value: block/dirt
     * @param cubeElements Cube element/element's to create a obj from
     */
    public void createObjFromCube(String name, Boolean uvLock, ArrayVector.MatrixRotation rotationX, ArrayVector.MatrixRotation rotationY,HashMap<String,String> modelsMaterials, CubeElement ...cubeElements){
        setName(name);

        //Each item is an array with the following values [vx, vy, vz]
        HashedDoubleList vertices = new HashedDoubleList();
        HashedDoubleList textureCoordinates = new HashedDoubleList();
        ArrayList<Double[]> normalsArray = new ArrayList<>();
        //Map of materialName and It's faces, where each face consists of an list of array indices
        //Each indice consists of the vertex index, texture coordinate index and vertex normal index
        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = new HashMap<>();

        //A map that keeps track of what faces (indexes) bounds the block bounding box on that specific orientation
        //Map<Facing (Orientation):String, Map<MaterialName:String, List<FaceIndex:Integer>>>
        HashMap<String, HashMap<String, ArrayList<Integer>>> boundingFaces = new HashMap<>();

        //Convert the cubes to wavefront
        for(CubeElement cube : cubeElements) {
            WavefrontUtility.convertCubeToWavefront(cube, uvLock, rotationX, rotationY, vertices, textureCoordinates, faces, boundingFaces, modelsMaterials);
        }

        //Create normals for the object
        WavefrontUtility.createNormals(normalsArray, vertices, faces);
        //Get vertex list
        ArrayList<Double[]> verticesArray = vertices.toList();

        //Normalize vertex normals
        WavefrontUtility.normalizeNormals(normalsArray);

        setVertices(verticesArray);
        setVertexNormals(normalsArray);
        setTextureCoordinates(textureCoordinates.toList());
        setMaterialFaces(faces);
        setBoundingFaces(boundingFaces);
    }

    public void deleteFaces(String orientation){
        //Get the materials and list of their faces that face the direction
        HashMap<String, ArrayList<Integer>> faceMaterials = facing.get(orientation);

        //Loop through materials
        for (String material : faceMaterials.keySet()) {
            //Get the face indexes of faces that the material uses that face the direction
            ArrayList<Integer> faceIndexes = faceMaterials.get(material);
            if(f.containsKey(material)) {
                //Get all the faces the material uses
                ArrayList<ArrayList<Integer[]>> faces = f.get(material);


                //Loop  through the indexes of faces and add them to the removal list
                for (int faceIndex : faceIndexes) {
                    faces.set(faceIndex, null);
                }

                if (ArrayUtility.arrayContainsOnlyNullElement(faces)) {
                    f.remove(material);
                }
            }
        }
    }

    @Override
    public IWavefrontObject clone() {
        WavefrontObject wavefrontObject = new WavefrontObject();
        wavefrontObject.copy(this);

        return wavefrontObject;
    }

    @Override
    public void copy(IWavefrontObject clone) {
        WavefrontObject cloneObject = (WavefrontObject) clone;
        name = cloneObject.name;
        v = new ArrayList<>(cloneObject.v);
        vt = new ArrayList<>(cloneObject.vt);
        vn = new ArrayList<>(cloneObject.vn);
        f = new HashMap<>(cloneObject.f.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x -> new ArrayList<>(x.getValue()))));
        facing = new HashMap<>(cloneObject.facing.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                x -> new HashMap<>(x.getValue().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        y -> new ArrayList<>(y.getValue())))))));

    }


}
