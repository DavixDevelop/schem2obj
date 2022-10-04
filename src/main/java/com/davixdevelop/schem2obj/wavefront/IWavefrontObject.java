package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.namespace.Namespace;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public interface IWavefrontObject {
    String getName();
    ArrayList<Double[]> getVertices();
    void setVertices(ArrayList<Double[]> list);
    ArrayList<Double[]> getVertexNormals();
    ArrayList<Double[]> getTextureCoordinates();
    HashMap<String, ArrayList<ArrayList<Integer[]>>> getMaterialFaces();

    /**
     * Generate wavefront object from block namespace
     * @param blockNamespace
     */
    void fromNamespace(Namespace blockNamespace);

    /**
     * Get the bounding face of the object (the faces that are connected with other objects)
     * This is used to determine if the face of the object is hidden and need's to be deleted
     * Map<Facing (Orientation):String, Map<MaterialName:String, List<FaceIndex:Integer>>>
     * @return a map of orientations and their material faces
     */
    HashMap<String, HashMap<String, ArrayList<Integer>>> getBoundingFaces();

    /**
     * Nullify all object faces that face the specified orientation and remove material if it all It's face are null
     * @param orientation The orientation (ex. top, up, north...)
     */
    void deleteFaces(String orientation);
}
