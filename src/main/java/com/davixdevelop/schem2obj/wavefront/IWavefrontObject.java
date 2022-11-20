package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.ArrayList;
import java.util.HashMap;

public interface IWavefrontObject {
    String getName();
    ArrayList<Double[]> getVertices();
    void setVertices(ArrayList<Double[]> list);
    ArrayList<Double[]> getVertexNormals();
    ArrayList<Double[]> getTextureCoordinates();
    HashMap<String, ArrayList<ArrayList<Integer[]>>> getMaterialFaces();
}
