package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class LitFurnaceWavefrontObject extends BlockWavefrontObject {
    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        toObj(blockNamespace);
        return true;
    }

    public void toObj(Namespace blockNamespace){
        setName(blockNamespace.getName());

        //Get the BlockState for the lit furnace
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace);

        //Get the variant of the lit furnace
        BlockState.Variant variant = blockState.getVariants(blockNamespace).get(0);

        ArrayVector.MatrixRotation rotationY = null;

        if (variant != null) {

            //Check if lit furnace model should be rotated
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

        WavefrontUtility.generateOrGetMaterial("blocks/furnace_top", blockNamespace);
        WavefrontUtility.generateOrGetMaterial("blocks/furnace_front_on", blockNamespace);
        WavefrontUtility.generateOrGetMaterial("blocks/furnace_side", blockNamespace);

        modelsMaterials.put("top","blocks/furnace_top");
        modelsMaterials.put("front","blocks/furnace_front_on");
        modelsMaterials.put("side","blocks/furnace_side");

        HashMap<String, CubeElement.CubeFace> cubeOrientable = new HashMap<>();

        //Get random portion of furnace_front_on that contains 6 textures
        Double[] uv = WavefrontUtility.getRandomUV(6);

        cubeOrientable.put("down", new CubeElement.CubeFace(null, "#top", "down", null, null));
        cubeOrientable.put("up", new CubeElement.CubeFace(null, "#top", "up", null, null));
        cubeOrientable.put("north", new CubeElement.CubeFace(uv, "#front", "north", null, null));
        cubeOrientable.put("south", new CubeElement.CubeFace(null, "#side", "south", null, null));
        cubeOrientable.put("west", new CubeElement.CubeFace(null, "#side", "west", null, null));
        cubeOrientable.put("east", new CubeElement.CubeFace(null, "#top", "east", null, null));

        CubeElement cube = new CubeElement(
                new Double[]{0.0,0.0,0.0},
                new Double[]{1.0,1.0,1.0},
                false,
                null,
                cubeOrientable);

        //Convert cube to obj
        WavefrontUtility.convertCubeToWavefront(cube, false, null, rotationY, vertices, textureCoordinates, faces, boundingFaces, modelsMaterials);

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

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new LitFurnaceWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
