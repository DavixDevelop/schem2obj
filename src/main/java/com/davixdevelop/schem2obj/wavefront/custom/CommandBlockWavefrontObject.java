package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandBlockWavefrontObject extends BlockWavefrontObject {
    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        toObj(blockNamespace);
        return true;
    }

    public void toObj(Namespace blockNamespace){
        setName(blockNamespace.getName());

        //Get the BlockState for the block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace);

        //Get the variant/variants of the block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);


        ArrayList<VariantModels> blockModels = new ArrayList<>();

        //Get the model for the variant of the command block, only to get the textures
        BlockState.Variant variant = variants.get(0);
        blockModels.add(new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(blockNamespace, variant)));

        HashMap<String, HashMap<String, String>> commandBlockMaterials = WavefrontUtility.texturesToMaterials(blockModels, blockNamespace);

        ArrayVector.MatrixRotation rotationX = null;
        ArrayVector.MatrixRotation rotationY = null;


        boolean uvLock = false;

        if (variant != null) {
            uvLock = variant.getUvlock();

            //Check if variant model should be rotated
            if (variant.getX() != null)
                rotationX = new ArrayVector.MatrixRotation(variant.getX(), "X");

            if (variant.getY() != null)
                rotationY = new ArrayVector.MatrixRotation(variant.getY(), "Z");
        }


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

        //Copy the command block texture variables to modelMaterials
        for(String rootModel : commandBlockMaterials.keySet()){
            HashMap<String, String> materials = commandBlockMaterials.get(rootModel);
            modelsMaterials.putAll(materials);
        }

        HashMap<String, CubeElement.CubeFace> cubeDirectional = new HashMap<>();

        //Get top portion of the texture, that has 4 textures on it
        Double[] uv = new Double[]{0.0,(1.0 / 4) * 3 ,1.0, 1.0};

        cubeDirectional.put("down", new CubeElement.CubeFace(uv, "#down", "down", 180.0, null));
        cubeDirectional.put("up", new CubeElement.CubeFace(uv, "#up", "up", null, null));
        cubeDirectional.put("north", new CubeElement.CubeFace(uv, "#north", "north", null, null));
        cubeDirectional.put("south", new CubeElement.CubeFace(uv, "#south", "south", null, null));
        cubeDirectional.put("west", new CubeElement.CubeFace(uv, "#west", "west", 270.0, null));
        cubeDirectional.put("east", new CubeElement.CubeFace(uv, "#east", "east", 90.0, null));

        CubeElement cube = new CubeElement(
                new Double[]{0.0,0.0,0.0},
                new Double[]{1.0,1.0,1.0},
                false,
                null,
                cubeDirectional);

        //Convert cube to obj
        WavefrontUtility.convertCubeToWavefront(cube, uvLock, rotationX, rotationY, vertices, textureCoordinates, faces, boundingFaces, modelsMaterials);

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
