package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.material.Material;

import java.util.ArrayList;
import java.util.HashMap;

public class WavefrontObject implements IWavefrontObject {
    private String name;
    private ArrayList<Double[]> v;
    private ArrayList<Double[]> vt;
    private ArrayList<Double[]> vn;
    //Key: materials used Value: List of faces, and It's indices
    private HashMap<String, ArrayList<ArrayList<Integer[]>>> f;

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
    public void fromNamespace(Namespace blockNamespace) { }
}
