package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.utilities.ArrayUtility;
import com.davixdevelop.schem2obj.utilities.ArrayVector;

import java.util.*;
import java.util.stream.Collectors;

public class BlockWavefrontObject extends WavefrontObject {

    private static HashMap<BlockState.Variant, IWavefrontObject> BLOCK_RANDOM_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        if(blockNamespace.getName().equals("oak_stairs")){
            String w = "2";
        }

        //Get the BlockState for the block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace);

        //Get the variant/variants of the block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);


        ArrayList<BlockModel> blockModels = new ArrayList<>();

        //Get the model/models the block uses based on the BlockState
        for(BlockState.Variant variant : variants){
            blockModels.addAll(Constants.BLOCK_MODELS.getBlockModel(blockNamespace, variant));
        }

        //If block state uses "variants" with random variants, don't store them in memory
        boolean storeInMemory = true;

        if(blockState.isRandomVariants()){
            //Check if random variant is not in BLOCK_RANDOM_VARIANTS, generate it and store it
            //Else get a copy of the singleton
            if(!BLOCK_RANDOM_VARIANTS.containsKey(variants.get(0))){
                toObj(blockModels, variants, blockNamespace);
                BLOCK_RANDOM_VARIANTS.put(variants.get(0), this);

            }else{
                IWavefrontObject copy = BLOCK_RANDOM_VARIANTS.get(variants.get(0)).clone();
                super.copy(copy);
            }

            return false;
        }

        toObj(blockModels, variants,blockNamespace);

        return true;

    }

    /**
     * Convert a block models to wavefront object.
     * The indexes in the object are treated as if the object is the first one in the file
     * @param blockModels The block models to convert
     * @param variants The variants of the block
     * @param blockNamespace The namespace of the block
     * @return the block wavefront object
     */
    public void toObj(ArrayList<BlockModel> blockModels, ArrayList<BlockState.Variant> variants, Namespace blockNamespace){
        //Set the name of the object
        setName(blockNamespace.getName());

        //Each item is an array with the following values [vx, vy, vz, vnx, vny, vnz]
        HashedDoubleList verticesAndNormals = new HashedDoubleList(3);
        HashedDoubleList textureCoordinates = new HashedDoubleList(2);
        //Map of materialName and It's faces, where each face consists of an list of array indices
        //Each indice consists of the vertex index, texture coordinate index and vertex normal index
        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = new HashMap<>();

        //A map that keeps track of what faces (indexes) bounds the block bounding box on that specific orientation
        //Map<Facing (Orientation):String, Map<MaterialName:String, List<FaceIndex:Integer>>>
        HashMap<String, HashMap<String, ArrayList<Integer>>> boundingFaces = new HashMap<>();

        //Extract the default materials from the textures that the models use
        HashMap<String, String> modelsMaterials = WavefrontUtility.texturesToMaterials(blockModels, blockNamespace);

        //Minecraft usually parses the block models from bottom to top (ex. from block -> dirt)
        //This approach overrides the generated elements, if the current model has a parents and elements
        //In other words if the current model has a parent and elements, ignore the previous generated elements and
        //Use the current model elements as the block elements
        //To emulate this approach when parsing the models from top to bottom (ex. from dirt -> block),
        //to skip generating elements that would be discarded (written over) later, we need to
        //check if the root model elements were already generated (mark it), and the current model has an parent and elements
        //If it does, skip the current model, else generate the elements.
        //As the array model can contain multiple root model, we need to store if the root model
        //already has generated elements in a Set (root model name is in set, if it already has elements)
        //Key: the name of the root model
        Set<String> generatedElements = new HashSet<>();

        for(BlockModel model : blockModels) {
            ArrayList<CubeElement> elements = model.getElements();

            if (!elements.isEmpty()) {

                //Ignore the model (m) elements, if the model has elements and a parent and the elements were already set
                if (generatedElements.contains(model.getRootParent()) && !elements.isEmpty() && model.getParent() != null)
                    continue;

                //Each element represents a cube
                for (CubeElement element : elements) {
                    boolean uvLock = false;
                    ArrayVector.MatrixRotation rotationX = null;
                    ArrayVector.MatrixRotation rotationY = null;

                    Optional<BlockState.Variant> variantOptional = variants.stream().filter(x -> x.getModel().equals(model.getRootParent())).findFirst();

                    if(variantOptional.isPresent()){
                        BlockState.Variant variant = variantOptional.get();

                        uvLock = variant.getUvlock();

                        //Check if variant model should be rotated
                        if (variant.getX() != null)
                            rotationX = new ArrayVector.MatrixRotation(Math.toRadians(-variant.getX()), "X");

                        if (variant.getY() != null)
                            rotationY = new ArrayVector.MatrixRotation(Math.toRadians(-variant.getY()), "Z");
                    }else {
                        String w = "2";
                    }





                    //Convert the cube to obj
                    WavefrontUtility.convertCubeToWavefront(element, uvLock, rotationX, rotationY, verticesAndNormals, textureCoordinates, faces, boundingFaces, modelsMaterials);
                }

                //Mark that the root model has generated elements
                generatedElements.add(model.getRootParent());
            }
        }

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
