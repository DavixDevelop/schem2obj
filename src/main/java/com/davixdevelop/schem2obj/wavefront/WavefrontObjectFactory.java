package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.cubemodels.ICube;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.cubemodels.model.CubeFace;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.models.HashedStringList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WavefrontObjectFactory {
    public static IWavefrontObject fromCubeModel(ICubeModel cubeModel){
        //Each item is an array with the following values [vx, vy, vz]
        HashedDoubleList vertices = new HashedDoubleList();
        ArrayList<Double[]> normalsArray = new ArrayList<>();
        HashedDoubleList textureCoordinates = new HashedDoubleList();
        //Map of materialName and It's faces, where each face consists of an list of array indices
        //Each indices consists of the vertex index, texture coordinate index and vertex normal index
        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = new HashMap<>();

        HashedStringList cubeModelMaterials = cubeModel.getMaterials();
        List<String> materialList = cubeModelMaterials.toList();
        //Add materials to faces
        for(String material : materialList){
            faces.put(material, new ArrayList<>());
        }

        List<ICube> cubes = cubeModel.getCubes();

        boolean hasFaces = false;

        //Loop through cube and convert them to obj
        for(ICube cube : cubes){
            //Get faces for cube
            CubeFace[] cubeFaces = cube.getFaces();

            //Get which faces should be exported
            Boolean[] generatedFaces = cube.getGeneratedFaces();
            for(int c = 0; c < 6; c++){
                //Check if face should be exported
                if(generatedFaces[c]){
                    CubeFace cubeFace = cubeFaces[c];

                    List<Double[]> faceUv = cubeFace.getUv();
                    List<Double[]> faceVertices = cubeFace.getCorners();

                    Integer[] uvIndex = new Integer[4];
                    Integer[] vertIndex = new Integer[4];

                    //Append the uv's to textureCoordinates
                    for(int u = 0; u < faceUv.size(); u++){
                        Double[] uv = faceUv.get(u);
                        if (textureCoordinates.containsKey(uv)) {
                            uvIndex[u] = textureCoordinates.getIndex(uv);
                        } else {
                            uvIndex[u] = textureCoordinates.put(uv);
                        }
                        uvIndex[u] = textureCoordinates.put(uv);
                    }

                    //Append the face vertices to vertices
                    for(int v = 0; v < faceVertices.size(); v++){
                        Double[] vert = faceVertices.get(v);
                        if (vertices.containsKey(vert)) {
                            vertIndex[v] = vertices.getIndex(vert);
                        } else {
                            vertIndex[v] = vertices.put(vert);
                        }
                    }

                    //Set the face indices
                    ArrayList<Integer[]> faceIndices = new ArrayList<>();
                    for(int i = 0; i < 4; i++){
                        Integer[] indices = new Integer[3];
                        indices[0] = vertIndex[i];
                        indices[1] = uvIndex[i];
                        indices[2] = vertIndex[i];

                        faceIndices.add(indices);
                    }

                    //Add the face indices to faces
                    ArrayList<ArrayList<Integer[]>> materialFaces = faces.get(cubeFace.getMaterial());
                    materialFaces.add(faceIndices);

                    //Mark that the cube has faces present
                    hasFaces = true;
                }
            }
        }

        //If object has no faces return null
        if(!hasFaces)
            return null;

        //Check if no faces are present in material faces, and delete the material face
        faces.keySet().removeIf(material -> faces.get(material).isEmpty());

        //Create normals for object
        WavefrontUtility.createNormals(normalsArray, vertices, faces);

        //Get vertex list
        ArrayList<Double[]> verticesArray = vertices.toList();

        //Normalize vertex normals
        WavefrontUtility.normalizeNormals(normalsArray);

        WavefrontObject wavefrontObject = new WavefrontObject();

        wavefrontObject.setName(cubeModel.getName());

        wavefrontObject.setVertices(verticesArray);
        wavefrontObject.setVertexNormals(normalsArray);
        wavefrontObject.setTextureCoordinates(textureCoordinates.toList());
        wavefrontObject.setMaterialFaces(faces);

        return wavefrontObject;

    }
}
