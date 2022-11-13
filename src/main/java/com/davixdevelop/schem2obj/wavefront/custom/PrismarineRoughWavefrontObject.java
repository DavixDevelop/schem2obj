package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.WavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class PrismarineRoughWavefrontObject extends WavefrontObject {
    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        toObj(blockNamespace);
        return true;
    }

    public void toObj(Namespace blockNamespace){
        setName("prismarine");

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

        HashMap<String,String> modelsMaterials = new HashMap<>();
        WavefrontUtility.generateOrGetMaterial("blocks/prismarine_rough", blockNamespace);
        modelsMaterials.put("all", "blocks/prismarine_rough");

        HashMap<String, CubeElement.CubeFace> cubeFaces = new HashMap<>();

        //Get random portion of the prismarine_rough texture, that has 4 textures on it
        Double[] uv = WavefrontUtility.getRandomUV(4);

        cubeFaces.put("down", new CubeElement.CubeFace(uv, "#all", "down", null, null));
        cubeFaces.put("up", new CubeElement.CubeFace(uv, "#all", "up", null, null));
        cubeFaces.put("north", new CubeElement.CubeFace(uv, "#all", "north", null, null));
        cubeFaces.put("south", new CubeElement.CubeFace(uv, "#all", "south", null, null));
        cubeFaces.put("west", new CubeElement.CubeFace(uv, "#all", "west", null, null));
        cubeFaces.put("east", new CubeElement.CubeFace(uv, "#all", "east", null, null));

        CubeElement cube = new CubeElement(
                new Double[]{0.0,0.0,0.0},
                new Double[]{1.0,1.0,1.0},
                false,
                null,
                cubeFaces);

        //Convert cube to obj
        WavefrontUtility.convertCubeToWavefront(cube, false, null, null, vertices, textureCoordinates, faces, boundingFaces, modelsMaterials);

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
}
