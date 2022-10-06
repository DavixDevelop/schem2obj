package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.utilities.ArrayUtility;
import com.davixdevelop.schem2obj.utilities.Utility;
import com.davixdevelop.schem2obj.wavefront.material.Material;

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

    /**
     * Set the bounding faces of the object
     * @param facing Map<Facing (Orientation):String, Map<MaterialName:String, List<FaceIndex:Integer>>>
     */
    public void setBoundingFaces(HashMap<String, HashMap<String, ArrayList<Integer>>> facing) {
        this.facing = facing;
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

                if (ArrayUtility.arrayContainsOnlyNullElement(faces))
                    f.remove(material);
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
