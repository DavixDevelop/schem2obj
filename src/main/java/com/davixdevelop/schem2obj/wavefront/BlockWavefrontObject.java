package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.Hashed2KeyList;
import com.davixdevelop.schem2obj.models.Hashed3KeyList;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.utilities.ArrayUtility;
import com.davixdevelop.schem2obj.utilities.ArrayVector;

import java.util.*;

public class BlockWavefrontObject extends WavefrontObject {
    @Override
    public void fromNamespace(Namespace blockNamespace) {
        //Get the BlockState for the block
        BlockState blockState = WavefrontCollection.BLOCKS_STATES.getBlockState(blockNamespace);

        //Get the model/models the block uses based on the BlockState
        BlockModel[] blockModels = WavefrontCollection.BLOCK_MODELS.getBlockModel(blockNamespace, blockState);

        toObj(blockModels, blockState.getVariants(blockNamespace),blockNamespace);

    }

    /**
     * Convert a block models to wavefront object.
     * The indexes in the object are treated as if the object is the first one in the file
     * @param model The block model to convert
     * @param variants The variants of the block
     * @param blockNamespace The namespace of the block
     * @return the block wavefront object
     */
    public void toObj(BlockModel[] blockModels, ArrayList<BlockState.Variant> variants, Namespace blockNamespace){
        //Set the name of the object
        setName(blockNamespace.getName());

        //Each item is an array with the following values [vx, vy, vz, vnx, vny, vnz]
        Hashed3KeyList<Double> verticesAndNormals = new Hashed3KeyList<>();
        //Hashed3KeyList<Double> normals = new Hashed3KeyList<>();
        Hashed2KeyList<Double> textureCoordinates = new Hashed2KeyList<>();
        //Map of materialName and It's faces, where each face consists of an list of array indices
        //Each indice consists of the vertex index, texture coordinate index and vertex normal index
        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = new HashMap<>();

        //Extract the default materials from the textures that the models use
        HashMap<String, String> modelsMaterials = WavefrontUtility.texturesToMaterials(blockModels, blockNamespace);

        //Variable to store cube corner and their vertices index
        //Key: corner (ex. A), Value: Index of vertex
        Map<String, Integer> CornersIndex = new HashMap<>();
        //Variable to store index of UV's
        Integer[] UVIndexes = new Integer[]{0,1,2,3};

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

        for(BlockModel model : blockModels){
            ArrayList<CubeElement> elements = model.getElements();

            if(!elements.isEmpty()){

                //Ignore the model (m) elements, if the model has elements and a parent and the elements were already set
                if(generatedElements.contains(model.getRootParent()) && !elements.isEmpty() && model.getParent() != null)
                    continue;

                //Each element represents a cube
                for(CubeElement element: elements){

                    //Get starting point of cube

                    Double[] from = element.getFrom();
                    //Get end point of cube
                    Double[] to = element.getTo();

                    //Create vertices for each corner of a face that the cube uses and add them to the object vertices
                    Map<String, Double[]> cubeCorners = WavefrontUtility.createCubeVerticesFromPoints(from, to, element.getFaces().keySet());
                    //Check if element has rotation
                    if(element.getRotation() != null){
                        CubeElement.CubeRotation cubeRotation = element.getRotation();

                        //Construct matrix rotation based on the axis and angle
                        ArrayVector.MatrixRotation matrixRotation = new ArrayVector.MatrixRotation(Math.toRadians(cubeRotation.getAngle()), cubeRotation.getAxis());

                        //loop through the corners (vertices) and rotate each vertex
                        for(String corner : cubeCorners.keySet())
                            cubeCorners.put(corner, WavefrontUtility.rotatePoint(cubeCorners.get(corner), matrixRotation, (cubeRotation.getOrigin() != null) ? cubeRotation.getOrigin() : WavefrontUtility.BLOCK_ORIGIN));
                    }


                    //Loop through the variants
                    for(BlockState.Variant variant : variants){
                        //Check if the sub/main model's root parent model-name and the variant model-name is the same
                        if(model.getRootParent().equals(variant.getModel())){
                            //Check if variant model should be rotated
                            if(variant.getX() != null || variant.getY() != null){
                                ArrayVector.MatrixRotation rotationX = null;
                                ArrayVector.MatrixRotation rotationY = null;

                                if(variant.getX() != null)
                                    rotationX = new ArrayVector.MatrixRotation(Math.toRadians(variant.getX()), "X");

                                if(variant.getY() != null)
                                    rotationY = new ArrayVector.MatrixRotation(Math.toRadians(variant.getY()), "Z");

                                //Loop through the cube corners and to rotate them
                                for(String corner : cubeCorners.keySet()) {
                                    if(rotationX != null)
                                        cubeCorners.put(corner, WavefrontUtility.rotatePoint(cubeCorners.get(corner), rotationX, WavefrontUtility.BLOCK_ORIGIN));

                                    if(rotationY != null)
                                        cubeCorners.put(corner, WavefrontUtility.rotatePoint(cubeCorners.get(corner), rotationY, WavefrontUtility.BLOCK_ORIGIN));
                                }
                            }
                        }


                    }

                    //Append cube corner to object vertices and get the indexes to vertex's and normals in verticesAndNormals
                    for(String corner : cubeCorners.keySet())
                        CornersIndex.put(corner, verticesAndNormals.add(cubeCorners.get(corner)));

                    //Get element faces
                    HashMap<String, CubeElement.CubeFace> elementFaces = element.getFaces();
                    for(String key : elementFaces.keySet() ){

                        CubeElement.CubeFace face = elementFaces.get(key);
                        //Get the face uv's, or set them, if It's not defined in the uv field
                        ArrayList<Double[]> faceUV = WavefrontUtility.setAndRotateUVFace(face, to, from);

                        ///Append custom uv's cords to texture coordinates
                        UVIndexes = new Integer[faceUV.size()];
                        for(int c = 0; c < faceUV.size(); c++){
                            Double[] uv = faceUV.get(c);
                            if(textureCoordinates.containsKey(uv[0], uv[1])){
                                UVIndexes[c] = textureCoordinates.getIndex(uv[0], uv[1]);
                            }else{
                                UVIndexes[c] = textureCoordinates.add(uv);
                            }
                        }



                        //Create the wavefront face out of the cube face
                        ArrayList<Integer[]> wvFace = WavefrontUtility.createWavefrontFace(face, CornersIndex, UVIndexes, key);

                        //Get the variable of the face (ex. #all)
                        String faceTextureVariable = face.getTexture().substring(1);
                        //Get the actual value of the material variable (ex. blocks/dirt)
                        String faceMaterial = modelsMaterials.get(faceTextureVariable);

                        //Append the wavefront face to the collection faces of the material, that the new face uses
                        if(faces.containsKey(faceMaterial)){
                            ArrayList<ArrayList<Integer[]>> textureFaces = faces.get(faceMaterial);
                            textureFaces.add(wvFace);
                            faces.put(faceMaterial,textureFaces);
                        }else{
                            //Else crete the collection which It's key is the face material
                            ArrayList<ArrayList<Integer[]>> textureFaces = new ArrayList<>();
                            textureFaces.add(wvFace);
                            faces.put(faceMaterial, textureFaces);
                        }

                    }

                    //Map<Integer, Integer> indexOccourance = new HashMap<>();

                    //Normalize the cube/cubes
                    for(String materialName: faces.keySet()){
                        //Get the faces that the texture uses
                        ArrayList<ArrayList<Integer[]>> materialFaces = faces.get(materialName);
                        for(ArrayList<Integer[]> materialFace : materialFaces){

                            //Calculate face normal (↑B - ↑A) × (↑C - ↑A)
                            //ex: A = first pair of vertex and vertex normal of face (verticesAndNormals.get(wvf.get(0 <- first indic)[0 <- first element in indic is the vertex index]))
                            //↑A = first 3 items of A (splitArray(A))
                            //Get 3 vertices
                            Double[] av = (Double[]) ArrayUtility.splitArray(verticesAndNormals.get(materialFace.get(0)[0]), 3)[0];
                            Double[] bv = (Double[]) ArrayUtility.splitArray(verticesAndNormals.get(materialFace.get(1)[0]), 3)[0];
                            Double[] cv = (Double[]) ArrayUtility.splitArray(verticesAndNormals.get(materialFace.get(2)[0]), 3)[0];
                            Double[] face_normal = ArrayVector.multiply(ArrayVector.subtract(bv, av), ArrayVector.subtract(cv, av));

                            for(int x = 0; x < 4; x++){
                                //Get vertex and vertex normal pair for each vert in face
                                Integer vertexIndex = materialFace.get(x)[0];

                                Double[] v = verticesAndNormals.get(vertexIndex);
                                //If the length of the pair is 4, the vertex normal hasn't been set yet
                                if(v.length == 3){
                                    //Append face_normal to the pair, to complete it it
                                    v = (Double[]) ArrayUtility.combineArray(v, face_normal);
                                }else{
                                    //Split the v pair
                                    Object[] v_and_n = ArrayUtility.splitArray(v, 3);
                                    //Get the normal from the pair
                                    Double[] vn = (Double[]) v_and_n[1];
                                    //Add face_normal to vertex normal
                                    vn = ArrayVector.add(vn, face_normal);

                                    //Create new pair from vertex and normal
                                    v = (Double[]) ArrayUtility.combineArray((Double[]) v_and_n[0], vn);
                                }

                                //Set v pair back to verticesAndNormals (to set the new normal)
                                verticesAndNormals.update(vertexIndex, v);;
                            }

                        }
                    }
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

        //Loop through vertex normals and normalize it
        for(int c = 0; c < normalsArray.size(); c++){
            Double[] vn = normalsArray.get(c);

            if(vn[0] != null){
                vn = ArrayVector.normalize(vn);
                normalsArray.set(c, vn);
            }else{
                normalsArray.remove(c);
                c--;
            }
        }

        setVertices(verticesArray);
        setVertexNormals(normalsArray);
        setTextureCoordinates(textureCoordinates.toList());
        setMaterialFaces(faces);
    }
}
