package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.utilities.ArrayUtility;
import com.davixdevelop.schem2obj.wavefront.WavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class MagmaBlockWavefrontObject extends WavefrontObject {
    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        createObj(blockNamespace);
        return true;
    }

    public void createObj(Namespace blockNamespace){
        setName("magma");

        //Each item is an array with the following values [vx, vy, vz, vnx, vny, vnz]
        HashedDoubleList verticesAndNormals = new HashedDoubleList(3);
        HashedDoubleList textureCoordinates = new HashedDoubleList(2);
        //Map of materialName and It's faces, where each face consists of an list of array indices
        //Each indice consists of the vertex index, texture coordinate index and vertex normal index
        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = new HashMap<>();

        //A map that keeps track of what faces (indexes) bounds the block bounding box on that specific orientation
        //Map<Facing (Orientation):String, Map<MaterialName:String, List<FaceIndex:Integer>>>
        HashMap<String, HashMap<String, ArrayList<Integer>>> boundingFaces = new HashMap<>();

        HashMap<String,String> modelsMaterials = new HashMap<>();
        WavefrontUtility.generateOrGetMaterial("blocks/magma", blockNamespace);
        modelsMaterials.put("all", "blocks/magma");

        HashMap<String, CubeElement.CubeFace> cubeFaces = new HashMap<>();
        cubeFaces.put("down", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0 / 3}, "#all", "down", null, null));
        cubeFaces.put("up", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0 / 3}, "#all", "up", null, null));
        cubeFaces.put("north", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0 / 3}, "#all", "north", null, null));
        cubeFaces.put("south", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0 / 3}, "#all", "south", null, null));
        cubeFaces.put("west", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0 / 3}, "#all", "west", null, null));
        cubeFaces.put("east", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0 / 3}, "#all", "east", null, null));

        CubeElement cube = new CubeElement(
                new Double[]{0.0,0.0,0.0},
                new Double[]{1.0,1.0,1.0},
                false,
                null,
                cubeFaces);

        //Convert cube to obj
        WavefrontUtility.convertCubeToWavefront(cube, false, null, null, verticesAndNormals, textureCoordinates, faces, boundingFaces, modelsMaterials);

        //Split verticesAndNormals to two list's
        Object[] vertex_and_normals = ArrayUtility.splitArrayPairsToLists(verticesAndNormals.toList(), 3);

        //Get vertex list form vertex_and_normals
        ArrayList<Double[]> verticesArray = (ArrayList<Double[]>) vertex_and_normals[0];

        //Get normals list from vertex_and_normals
        ArrayList<Double[]> normalsArray = (ArrayList<Double[]>) vertex_and_normals[1];

        //Normalize vertex normals
        WavefrontUtility.normalizeNormals(normalsArray);

        setVertices(verticesArray);
        setVertexNormals(normalsArray);
        setTextureCoordinates(textureCoordinates.toList());
        setMaterialFaces(faces);
        setBoundingFaces(boundingFaces);
    }
}
