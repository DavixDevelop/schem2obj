package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.utilities.ArrayUtility;
import com.davixdevelop.schem2obj.utilities.ArrayVector;
import com.davixdevelop.schem2obj.wavefront.material.IMaterial;
import com.davixdevelop.schem2obj.wavefront.material.Material;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class WavefrontUtility {
    /**
     * Extract default materials from models and return a map with texture variables
     * and names of the material
     * @param models An array of models
     * @return map of key: texture variable, value: name of default material
     */
    public static HashMap<String, String> texturesToMaterials(List<VariantModels> models, Namespace blockNamespace) {
        HashMap<String, String> textureMaterials = new HashMap<>();

        for(VariantModels variantModels : models) {

            for (BlockModel model : variantModels.getModels()) {
                HashMap<String, String> modelTextures = model.getTextures().getTextures();

                //Get textures from block
                for (String key : modelTextures.keySet()) {
                    String value = modelTextures.get(key);

                    //Check if texture value doesn't have a variable (raw value, ex. block/dirt)
                    if (!value.startsWith("#")) {

                        String materialName = value;

                        generateOrGetMaterial(materialName, blockNamespace);

                        if (!textureMaterials.containsKey(key))
                            textureMaterials.put(key, materialName);

                    } else {
                        //Check if texture materials already contains a key with the same variable name (removed # from the value)
                        if (textureMaterials.containsKey(value.substring(1))) {
                            //If it does, place the key with the actual value of the variable (ex. #all -> block/dirt)
                            textureMaterials.put(key, textureMaterials.get(value.substring(1)));
                        }
                    }

                }
            }
        }

        return textureMaterials;
    }

    public static void generateOrGetMaterial(String materialName, Namespace blockNamespace){
        if (Constants.BLOCK_MATERIALS.containsMaterial(materialName)) {
            if(!Constants.BLOCK_MATERIALS.usedMaterials().contains(materialName)){
                //If material isn't yet used, but It's in BLOCK_MATERIALS collection, it means It's a custom material, added from a resource pack
                //Modify the material to include the lightValue of the block
                IMaterial material = Constants.BLOCK_MATERIALS.getMaterial(materialName);
                material.seEmissionStrength(blockNamespace.getLightValue());
                Constants.BLOCK_MATERIALS.setMaterial(materialName, material);
            }
        }else {
            Material material = new Material();
            //ToDo: Implement this in LitBlockWavefrontObject
                        /*boolean isLit = false;
                        //Loop through the variants and check if the model root parent name is the same same as the model name
                        //If it is, check if the variant model start's with lit_ and set isLit to true
                        //This approach enables a material to be named lit_ + texture name and still keep the texture name the same
                        for(BlockState.Variant variant : variants){
                            if(variant.getModel().equals(model.getRootParent())){
                                if(variant.getModel().startsWith("lit_")) {
                                    isLit = true;
                                    break;
                                }
                            }
                        }*/

            material.setDiffuseTexturePath(materialName);
            material.setDiffuseTextureName(textureName(materialName));
            material.seEmissionStrength(blockNamespace.getLightValue());
            material.setName(textureName(materialName));
            Constants.BLOCK_MATERIALS.setMaterial(materialName, material);
        }
    }

    /**
     * Return the name of the texture path (ex. blocks/dirt -> dirt)
     * @param texture The texture path
     * @return The name of the texture
     */
    public static String textureName(String texture){
        if(texture.contains("/")){
            return texture.substring(texture.lastIndexOf("/") + 1);
        }else if(texture.contains("\\"))
            return texture.substring(texture.lastIndexOf("\\") + 1);
        else
            return texture;
    }

    /**
     * Set the UV's of a face in a clockwise orientation
     * @param face The face of the
     * @param from
     * @param to
     * @return
     */
    public static ArrayList<Double[]> setAndRotateUVFace(CubeElement.CubeFace face, String orientation, Double[] from, Double[] to){
        ArrayList<Double[]> UVFace = new ArrayList<>();
        if(face.getUv() == null){
            /*
            UVFace.add(new Double[]{from[0], to[1]}); //1
            UVFace.add(new Double[]{from[0], from[1]}); //2
            UVFace.add(new Double[]{to[0], from[1]}); //3
            UVFace.add(new Double[]{to[0], to[1]}); //4
            */

            UVFace.add(new Double[]{to[0], to[1]}); //4
            UVFace.add(new Double[]{to[0], from[1]}); //3
            UVFace.add(new Double[]{from[0], from[1]}); //2
            UVFace.add(new Double[]{from[0], to[1]}); //1

        }else{
            //MC face uv is a 4 item array [x1, y1, x2, y2]
            Double[] rawUV = face.getUv();
            /*UVFace.add(new Double[]{rawUV[2], rawUV[1]}); x2,y1
            UVFace.add(new Double[]{rawUV[2], rawUV[3]}); x2,y2
            UVFace.add(new Double[]{rawUV[0], rawUV[3]}); x1,y2
            UVFace.add(new Double[]{rawUV[0], rawUV[1]}); x1,y1
            */

            /*UVFace.add(new Double[]{rawUV[0], rawUV[1]}); //x1,y1
            UVFace.add(new Double[]{rawUV[0], rawUV[3]}); //x1,y2
            UVFace.add(new Double[]{rawUV[2], rawUV[3]}); //x2,y2
            UVFace.add(new Double[]{rawUV[2], rawUV[1]}); //x2,y1*/

            ArrayList<Double[]> defaultUV = new ArrayList<>();
            defaultUV.add(new Double[]{rawUV[0], rawUV[1]}); //x1,y1 //1
            defaultUV.add(new Double[]{rawUV[0], rawUV[3]}); //x1,y2 //2
            defaultUV.add(new Double[]{rawUV[2], rawUV[3]}); //x2,y2 //3
            defaultUV.add(new Double[]{rawUV[2], rawUV[1]}); //x2,y1 //4



            switch (orientation){
                case "north":
                    UVFace.add(defaultUV.get(1)); //2
                    UVFace.add(defaultUV.get(0)); //1
                    UVFace.add(defaultUV.get(3)); //4
                    UVFace.add(defaultUV.get(2)); //3
                case "south":
                    UVFace.add(defaultUV.get(2)); //3
                    UVFace.add(defaultUV.get(3)); //4
                    UVFace.add(defaultUV.get(0)); //1
                    UVFace.add(defaultUV.get(1)); //2
                    break;
                case "east":
                    UVFace = defaultUV;
                    break;
                case "west":
                case "up":
                case "down":
                default:
                    UVFace = defaultUV;
                    break;
            }




        }

        //Rotate uv, to simulate rotation of texture
        if(face.getRotation() != null){
            //Calculate the origin of the UV face
            Double[] faceOrigin = getUVFaceOrigin(UVFace);

            UVFace = rotateUV(UVFace, face.getRotation(), faceOrigin);

        }

        return UVFace;
    }

    public static ArrayList<Double[]> rotateUV(ArrayList<Double[]> uvs, Double angle, Double[] rotationOrigin){
        //Set the rotation matrix in the axis Z
        ArrayVector.MatrixRotation uvRotation = new ArrayVector.MatrixRotation(angle,"Z");
        for(int c = 0; c < uvs.size(); c++){
            Double[] uv = uvs.get(c);
            //Rotate the uv with rotatePoint by construction in vector that has an z value of 0
            Double[] new_uv = rotatePoint(new Double[] {uv[0], uv[1], 0.0}, uvRotation, rotationOrigin);
            uv[0] = (double) Math.round(new_uv[0]);
            uv[1] = (double) Math.round(new_uv[1]);
            //Set back the uv to the UVFace
            uvs.set(c, uv);
        }

        return uvs;
    }

    public static ArrayList<Double[]> offsetUV(ArrayList<Double[]> uvs, Double x, Double y){
        for(int c = 0; c < uvs.size(); c++ ){
            Double[] uv = uvs.get(c);
            if(x > 0.0)
                uv[0] = uv[0] - x;
            if(y > 0.0)
                uv[1] = uv[1] - y;

            uvs.set(c, uv);
        }

        return uvs;
    }

    private static Double[] getUVFaceOrigin(ArrayList<Double[]> UVFace){
        Double x1 = UVFace.stream().min(Comparator.comparing(v -> v[0])).get()[0]; //Min x
        Double x2 = UVFace.stream().max(Comparator.comparing(v -> v[0])).get()[0]; //Max x
        Double y1 = UVFace.stream().min(Comparator.comparing(v -> v[1])).get()[1]; //Min y
        Double y2 = UVFace.stream().max(Comparator.comparing(v -> v[1])).get()[1]; //Max y

        /*
        for(Double[] uv : UVFace){
            if(x1 == null){
                x1 = uv[0];
                x2 = uv[0];
                y1 = uv[1];
                y2 = uv[1];
            }else{
                if(uv[0] < x1)
                    x1 = uv[0];
                if(uv[0] > x2)
                    x2 = uv[0];

                if(uv[1] < y1)
                    y1 = uv[1];
                if(uv[1] > y2)
                    y2 = uv[1];
            }
        }*/

        return new Double[] {(x1 + x2) / 2, (y1 + y2) / 2, 0.0};
    }

    public static ArrayList<Integer[]> createWavefrontFace(CubeElement.CubeFace face, Map<String, Integer> VerticesIndexes, Integer[] UVIndexes, String orientation) {
        String[] corners = new String[]{"A","B","C","D"};
        switch (orientation){
            case "north":
                corners = new String[]{"M","F","C","D"}; //Orientation M:7 F:4 C:2 D:3
                break;
            case "south":
                corners = new String[]{"A","B","G","H"}; //Orientation A:0 B:1 G:5 H:6
                break;
            case "up":
                corners = new String[]{"B","C","F","G"}; //Orientation B:1 C:2 F:4 G:5
                break;
            case "down":
                corners = new String[]{"D","A","H","M"}; //Orientation D:3 A:0 H:6 M:7
                break;
            case "west":
                corners = new String[]{"D","C","B","A"}; //Orientation D:3 C:2 B:1 A:0
                break;
            case "east":
                corners = new String[]{"H","G","F","M"}; //Orientation H:6 G:5 F:4 M:7
                break;
        }


        ArrayList<Integer[]> wvFace = new ArrayList<>();
        //Add the indices of each vertex to the face
        //Each Integer array represent the the index of the vertex, texture coordinate and vertex normal and
        //[v, vt, vn]
        for(int c = 0; c <4; c++)
            wvFace.add(new Integer[]{VerticesIndexes.get(corners[c]), UVIndexes[c], VerticesIndexes.get(corners[c])});

        return  wvFace;
    }



    /**
     * Create 8 or less vertices for the cube
     * @param A The start corner of the cube
     * @param F The end corner of the cube
     * @return An map of vertices of the cube, where the key (String) represents a corner
     */
    public static Map<String, Double[]> createCubeVerticesFromPoints(Double[] A, Double[] F, Set<String> faces){
        Map<String, Double[]> vertices = new HashMap<>();

        //Array to keep track of which corners to add to the cube
        //The indexes are the following: 0:A , 1:B, 2:C, 3:D, 4:F, 5:G, 6:H, 7:M
        Boolean[] addCorners = new Boolean[] {false,false,false,false,false,false,false,false};

        if(faces.contains("north"))
        {
            //North face uses M F C D corners
            addCorners = markCorners(addCorners, new Integer[]{7,4,2,3});
        }

        if(faces.contains("south")){
            //South face uses A B G H corners
            addCorners = markCorners(addCorners, new Integer[]{0,1,5,6});
        }

        if(faces.contains("up")){
            //Up face uses B C F G corners
            addCorners = markCorners(addCorners, new Integer[]{1,2,4,5});
        }

        if(faces.contains("down")){
            //Down face uses D A H M corners
            addCorners = markCorners(addCorners, new Integer[]{3,0,6,7});
        }

        if(faces.contains("east")){
            //East face uses H G F M corners
            addCorners = markCorners(addCorners, new Integer[]{6,5,4,7});
        }

        if(faces.contains("west")){
            //West face uses D C B A corners
            addCorners = markCorners(addCorners, new Integer[]{3,3,2,0});
        }

        if(addCorners[0])//Corner A
            vertices.put("A",A);
        if(addCorners[1]) //Corner B
            vertices.put("B", new Double[]{A[0], A[1], F[2]});
        if(addCorners[2]) //Corner C
            vertices.put("C", new Double[]{A[0], F[1], F[2]});
        if(addCorners[3]) //Corner D
            vertices.put("D",new Double[]{A[0], F[1], A[2]});
        if(addCorners[4]) //Corner F
            vertices.put("F",F);
        if(addCorners[5]) //Corner G
            vertices.put("G",new Double[]{F[0], A[1], F[2]});
        if(addCorners[6]) //Corner H
            vertices.put("H", new Double[]{F[0], A[1], A[2]});
        if(addCorners[7]) //Corner M
            vertices.put("M", new Double[]{F[0], F[1], A[2]});

        return  vertices;
    }

    private static Boolean[] markCorners(Boolean[] addCorners, Integer[] indexes){
        for(int index : indexes)
            addCorners[index] = true;

        return addCorners;
    }

    /**
     *
     * @param wavefrontBlock
     * @param position The position of the block in the space [x, y, z]
     * @param spaceSize The size of the space [width, length, height]
     * @return
     */
    public static IWavefrontObject translateWavefrontBlock(IWavefrontObject wavefrontBlock, Integer[] position, Integer[] spaceSize){
        //Value by how much to move each vert (vert + translate)
        Double translateX = position[0] - (spaceSize[0].doubleValue() / 2);
        Double translateY = (position[1] * -1) + ((spaceSize[1].doubleValue() / 2) - 1);
        //Double translateZ = position[2].doubleValue();// - (spaceSize[2].doubleValue());
        Double[] translate = new Double[]{translateX, translateY, position[2].doubleValue()};

        ArrayList<Double[]> verticesArray = wavefrontBlock.getVertices();

        //Sum each vertex and translate
        for(int c = 0; c < verticesArray.size(); c++)
            verticesArray.set(c, ArrayVector.add(verticesArray.get(c), translate));

        //Set new vertices back to object
        wavefrontBlock.setVertices(verticesArray);

        return wavefrontBlock;
    }

    public static Double[] rotatePoint(Double[] point, ArrayVector.MatrixRotation rotation, Double[] origin){
        //Subtract the point by block origin, so that the origin of the block become 0,0,0
        point = ArrayVector.subtract(point, origin);

        //Rotate the point with the rotation matrix
        point = rotation.rotate(point, 1.0);

        //Add the rotated point and block origin, so that the origin become 0.5, 0.5, 0.5 again
        point = ArrayVector.add(point, origin);

        for(int c = 0; c < point.length; c++)
            point[c] = round(point[c], 6);

        return point;
    }

    public static Double round(Double value, int decimals){
        return new BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }

    public static String coordOrientationToOrientation(List<Integer> coord){

        if(coord.get(2) == 1)
            return "up";
        else if(coord.get(2) == -1)
            return "down";
        else if(coord.get(1) == 1)
            return "north";
        else if(coord.get(1) == -1)
            return "south";
        else if(coord.get(0) == -1)
            return "west";
        else if(coord.get(0) == 1)
            return "east";

        return null;
    }

    public static List<Integer> orientationToCoords(String orientation){
        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(0);
        list.add(0);

        switch (orientation){
            case "up":
                list.set(2,1);
                break;
            case "down":
                list.set(2,-1);
                break;
            case "north":
                list.set(1,1);
                break;
            case "south":
                list.set(1,-1);
                break;
            case "east":
                list.set(0,1);
                break;
            case "west":
                list.set(0,-1);
        }

        return list;
    }

    /**
     * Check if two objects bounding boxes are connected on face1 and face2
     * @param objectBoundingBox The object bounding box faces
     * @param object The original parent object
     * @param adjacentObject The parent object to check
     * @param face1 //The name of the face on the object to check
     * @param face2 //The name of the face on the parent object to check
     * @return True if the two faces are connected, else false
     */
    public static boolean checkFacing(Set<String> objectBoundingBox, IWavefrontObject object, IWavefrontObject adjacentObject, String face1, String face2){
        if(adjacentObject == null)
            return false;

        if(object.checkCollision(adjacentObject)){
            if(objectBoundingBox.contains(face1)){
                Set<String> parentObjectBoundingBox = adjacentObject.getBoundingFaces().keySet();
                if(parentObjectBoundingBox.contains(face2))
                    return true;
            }
        }
        return false;
    }


    public static void convertCubeToWavefront(CubeElement element, boolean uvLock, ArrayVector.MatrixRotation rotationX, ArrayVector.MatrixRotation rotationY,  HashedDoubleList vertices, HashedDoubleList textureCoordinates, HashMap<String, ArrayList<ArrayList<Integer[]>>> faces, HashMap<String, HashMap<String, ArrayList<Integer>>> boundingFaces, HashMap<String, String> modelsMaterials){
        //Variable to store cube corner and their vertices index
        //Key: corner (ex. A), Value: Index of vertex
        Map<String, Integer> CornersIndex = new HashMap<>();
        //Variable to store index of UV's
        Integer[] UVIndexes = new Integer[]{0,1,2,3};

        //Get starting point of cube

        Double[] from = element.getFrom();
        //Get end point of cube
        Double[] to = element.getTo();

        //Create vertices for each corner of a face that the cube uses and add them to the object vertices
        Map<String, Double[]> cubeCorners = WavefrontUtility.createCubeVerticesFromPoints(from, to, element.getFaces().keySet());


        //Check if element has rotation
        if (element.getRotation() != null) {
            CubeElement.CubeRotation cubeRotation = element.getRotation();

            //Construct matrix rotation based on the axis and angle
            ArrayVector.MatrixRotation matrixRotation = new ArrayVector.MatrixRotation(cubeRotation.getAngle(), cubeRotation.getAxis());

            //loop through the corners (vertices) and rotate each vertex
            for (String corner : cubeCorners.keySet())
                cubeCorners.put(corner, WavefrontUtility.rotatePoint(cubeCorners.get(corner), matrixRotation, (cubeRotation.getOrigin() != null) ? cubeRotation.getOrigin() : Constants.BLOCK_ORIGIN));
        }


        //Loop through the cube corners and to rotate them if variant specifies that it should be rotated
        if(rotationX != null || rotationY != null) {
            for (String corner : cubeCorners.keySet()) {
                if (rotationX != null)
                    cubeCorners.put(corner, WavefrontUtility.rotatePoint(cubeCorners.get(corner), rotationX, Constants.BLOCK_ORIGIN));

                if (rotationY != null)
                    cubeCorners.put(corner, WavefrontUtility.rotatePoint(cubeCorners.get(corner), rotationY, Constants.BLOCK_ORIGIN));
            }
        }

        //Get element faces
        HashMap<String, CubeElement.CubeFace> elementFaces = element.getFaces();

        //Append cube corner to object vertices and get the indexes to vertex's and normals in verticesAndNormals
        for (String corner : cubeCorners.keySet()){
            int VertexIndex = 0;
            if(vertices.containsKey(cubeCorners.get(corner)))
                VertexIndex = vertices.getIndex(cubeCorners.get(corner));
            else
                VertexIndex = vertices.put(cubeCorners.get(corner));
            CornersIndex.put(corner, VertexIndex);
        }




        //Map to store what material is used per face before any kind of rotation has been done on the faces
        //Key: Orientation in coords (ex. up -> 0,0,1, east -> 1,0,0...)
        HashMap<List<Integer>, String> materialPerOrientation = new HashMap<>();

        for (String originalOrientation : elementFaces.keySet()) {
            CubeElement.CubeFace face = elementFaces.get(originalOrientation);

            //Get the coord of the orientation of the face
            List<Integer> orientationCoord = WavefrontUtility.orientationToCoords(originalOrientation);

            //Get the variable of the face (ex. #all)
            String faceTextureVariable = face.getTexture().substring(1);
            //Get the actual value of the material variable (ex. blocks/dirt)
            String faceMaterial = modelsMaterials.get(faceTextureVariable);

            materialPerOrientation.put(orientationCoord, faceMaterial);

            if (!faces.containsKey(faceMaterial)) {
                //Create an empty collection which It's key is the face material, if faces does not contain it yet
                ArrayList<ArrayList<Integer[]>> textureFaces = new ArrayList<>();
                faces.put(faceMaterial, textureFaces);
            }
        }

        for (String key : elementFaces.keySet()) {
            CubeElement.CubeFace face = elementFaces.get(key);

            //Get the coord of the orientation of the face
            List<Integer> orientationCoord = WavefrontUtility.orientationToCoords(key);

            //Get the face uv's, or set them, if It's not defined in the uv field
            ArrayList<Double[]> faceUV = WavefrontUtility.setAndRotateUVFace(face, key, to, from);

            if (uvLock) {
                if (rotationX != null) {
                    //If face orientation is west or east rotate the uv coords by the rotation X
                    //on the origin 0.5 0.5
                    if (key.equals("west") || key.equals("east"))
                        faceUV = WavefrontUtility.rotateUV(faceUV, rotationX.getRot(), new Double[]{0.5, 0.5, 0.0});
                    else {
                        //Else rotate orientationCoord by closest right angle (ex, 100 -> 90)
                        ArrayVector.MatrixRotation rotation = rotationX;

                        //If the rotation angle on x isn't a right angle, move the uv coords by the modulu 90 (ex. 100 % 90 = 10 / 90 -> 0.1111..)
                        if (rotationX.getRot() % 90 != 0) {
                            Double angle = rotation.getRot() / 90;
                            rotation = new ArrayVector.MatrixRotation(Math.floor(angle), "X");
                            Double offsetX = (rotationX.getRot() % 90) / 90;
                            faceUV = WavefrontUtility.offsetUV(faceUV, offsetX, 0.0);
                        }
                        Double[] newOrientationCoord = new Double[]{orientationCoord.get(0).doubleValue(), orientationCoord.get(1).doubleValue(), orientationCoord.get(2).doubleValue()};
                        newOrientationCoord = WavefrontUtility.rotatePoint(newOrientationCoord, rotation, new Double[]{0.0, 0.0, 0.0});
                        orientationCoord.set(0, newOrientationCoord[0].intValue());
                        orientationCoord.set(1, newOrientationCoord[1].intValue());
                        orientationCoord.set(2, newOrientationCoord[2].intValue());
                    }
                }

                if (rotationY != null) {
                    //If face orientation is top or down, rotate by the rotation Y
                    //on the origin 0.5 0.5
                    if (orientationCoord.get(2) == 1 || orientationCoord.get(2) == -1)
                        faceUV = WavefrontUtility.rotateUV(faceUV, rotationY.getRot(), new Double[]{0.5, 0.5, 0.0});
                    else {
                        //Else rotate orientationCoord by closest right angle (ex, 100 -> 90)
                        ArrayVector.MatrixRotation rotation = rotationY;

                        //If the rotation angle on x isn't a right angle, move the uv coords by the modulu 90 (ex. 100 % 90 = 10 / 90 -> 0.1111..)
                        if (rotationY.getRot() % 90 != 0) {
                            Double angle = rotation.getRot() / 90;
                            rotation = new ArrayVector.MatrixRotation(Math.floor(angle), "Z");
                            Double offsetY = (rotationX.getRot() % 90) / 90;
                            faceUV = WavefrontUtility.offsetUV(faceUV, 0.0, offsetY);
                        }
                        Double[] newOrientationCoord = new Double[]{orientationCoord.get(0).doubleValue(), orientationCoord.get(1).doubleValue(), orientationCoord.get(2).doubleValue()};
                        newOrientationCoord = WavefrontUtility.rotatePoint(newOrientationCoord, rotation, new Double[]{0.0, 0.0, 0.0});
                        orientationCoord.set(0, newOrientationCoord[0].intValue());
                        orientationCoord.set(1, newOrientationCoord[1].intValue());
                        orientationCoord.set(2, newOrientationCoord[2].intValue());
                    }
                }
            }else{
                if(rotationX != null){
                    if(!key.equals("west") && !key.equals("east")){
                        Double[] newOrientationCoord = new Double[]{orientationCoord.get(0).doubleValue(), orientationCoord.get(1).doubleValue(), orientationCoord.get(2).doubleValue()};
                        newOrientationCoord = WavefrontUtility.rotatePoint(newOrientationCoord, rotationX, new Double[]{0.0, 0.0, 0.0});
                        orientationCoord.set(0, newOrientationCoord[0].intValue());
                        orientationCoord.set(1, newOrientationCoord[1].intValue());
                        orientationCoord.set(2, newOrientationCoord[2].intValue());
                    }
                }

                if(rotationY != null){
                    if(orientationCoord.get(2) != -1 && orientationCoord.get(2) != 1) {
                        Double[] newOrientationCoord = new Double[]{orientationCoord.get(0).doubleValue(), orientationCoord.get(1).doubleValue(), orientationCoord.get(2).doubleValue()};
                        newOrientationCoord = WavefrontUtility.rotatePoint(newOrientationCoord, rotationY, new Double[]{0.0, 0.0, 0.0});
                        orientationCoord.set(0, newOrientationCoord[0].intValue());
                        orientationCoord.set(1, newOrientationCoord[1].intValue());
                        orientationCoord.set(2, newOrientationCoord[2].intValue());
                    }
                }
            }

            //Get the material of the the face
            String faceMaterial = materialPerOrientation.get((!uvLock) ? WavefrontUtility.orientationToCoords(key) : orientationCoord);

            ///Append custom uv's cords to texture coordinates
            UVIndexes = new Integer[faceUV.size()];
            for (int c = 0; c < faceUV.size(); c++) {
                Double[] uv = faceUV.get(c);
                if (textureCoordinates.containsKey(uv[0], uv[1])) {
                    UVIndexes[c] = textureCoordinates.getIndex(uv[0], uv[1]);
                } else {
                    UVIndexes[c] = textureCoordinates.put(uv);
                }
            }
            //Create the wavefront face out of the cube face
            ArrayList<Integer[]> wvFace = WavefrontUtility.createWavefrontFace(face, CornersIndex, UVIndexes, key);

            ArrayList<Integer> boundingMaterialFaceIndexes = null;
            String faceOrientation = null;

            //Face is a cullface, add it to the the boundingFace
            if (face.getCullface() != null) {
                //Get the orientation from the orientationCoord
                faceOrientation = WavefrontUtility.coordOrientationToOrientation(orientationCoord);


                if (faceOrientation != null) {
                    //Put orientation if It's not yet present
                    if (!boundingFaces.containsKey(faceOrientation))
                        boundingFaces.put(faceOrientation, new HashMap<>());

                    //Get the material faces of orientation
                    HashMap<String, ArrayList<Integer>> boundingMaterialFaces = boundingFaces.get(faceOrientation);

                    //Put material into bounding material faces if It's not yet present
                    if (!boundingMaterialFaces.containsKey(faceMaterial)){
                        boundingMaterialFaces.put(faceMaterial, new ArrayList<>());
                        boundingFaces.put(faceOrientation, boundingMaterialFaces);
                    }

                    boundingMaterialFaceIndexes = boundingMaterialFaces.get(faceMaterial);
                }
            }

            //Append the wavefront face to the collection faces of the material, that the new face uses
            ArrayList<ArrayList<Integer[]>> textureFaces = faces.get(faceMaterial);
            textureFaces.add(wvFace);//Add the index of the added face to the face indexes the material that faces the bounding box bounds
            if (faceOrientation != null) {
                boundingMaterialFaceIndexes.add(textureFaces.size() - 1);
            }

        }
    }


    public static void createNormals(ArrayList<Double[]> normalsArray, HashedDoubleList vertices, HashMap<String, ArrayList<ArrayList<Integer[]>>> faces){

        //Populate the normalsArray with null of count of size the vertices list
        normalsArray.clear();
        for(int c = 0; c < vertices.size(); c++){
            normalsArray.add(null);
        }

        //Normalize the cube/cubes
        for (String materialName : faces.keySet()) {
            //Get the faces that the texture uses
            ArrayList<ArrayList<Integer[]>> materialFaces = faces.get(materialName);
            for (ArrayList<Integer[]> materialFace : materialFaces) {

                //Calculate face normal (↑B - ↑A) × (↑C - ↑A)
                //ex: A = first pair of vertex and vertex normal of face (verticesAndNormals.get(wvf.get(0 <- first indic)[0 <- first element in indic is the vertex index]))
                //↑A = first 3 items of A (splitArray(A))
                //Get 3 vertices
                Double[] av = (Double[]) ArrayUtility.splitArray(vertices.get(materialFace.get(0)[0]), 3)[0];
                Double[] bv = (Double[]) ArrayUtility.splitArray(vertices.get(materialFace.get(1)[0]), 3)[0];
                Double[] cv = (Double[]) ArrayUtility.splitArray(vertices.get(materialFace.get(2)[0]), 3)[0];
                Double[] face_normal = ArrayVector.multiply(ArrayVector.subtract(bv, av), ArrayVector.subtract(cv, av));

                for (int x = 0; x < 4; x++) {
                    //Get vertex and vertex normal pair for each vert in face
                    Integer vertexIndex = materialFace.get(x)[0];

                    Double[] v = vertices.get(vertexIndex);

                    Double[] vn = normalsArray.get(vertexIndex);
                    //If the vn is null, set it to face_nomal
                    if (vn== null) {
                        vn = face_normal;
                    } else {
                        //Add face_normal to vertex normal
                        vn = ArrayVector.add(vn, face_normal);
                    }

                    //Set vn to normalsArray
                    normalsArray.set(vertexIndex, vn);
                }

            }
        }
    }

    public static void normalizeNormals(ArrayList<Double[]> normalsArray){
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
    }

    /**
     * Write the Wavefront OBJ to the print writer, and return an array that keeps count of all written
     * vertices/uv's/vertex normals
     * @param object The wavefront object to write the data
     * @param f The PrintWriter to write the data to
     * @param countTracker A 3 length integer array to keep count of all written vertices/uv's/vertex normals
     * @return A 3 length integer array that keeps count of all written vertices/uv's/vertex normals
     */
    public static int[] writeObjectData(IWavefrontObject object, PrintWriter f, int[] countTracker){
        //Specify new object
        f.println(String.format("o %s", object.getName()));

        ArrayList<Double[]> vertices = object.getVertices();

        //Write all vertices
        for(Double[] v : vertices){
            f.println(String.format(Locale.ROOT, "v %f %f %f", v[0], v[2], -v[1]));
        }

        ArrayList<Double[]> uvs = object.getTextureCoordinates();
        //Write all texture coordinates
        for(Double[] vt : uvs){
            f.println(String.format(Locale.ROOT, "vt %f %f", vt[0], vt[1]));
        }

        ArrayList<Double[]> vertNormals = object.getVertexNormals();
        //Write all vertex normals
        for(Double[] vn : vertNormals){
            f.println(String.format(Locale.ROOT, "vn %f %f %f", vn[0], vn[1], vn[2]));
        }




        //key: materialName (ex. texture:blocks/dirt), value: list of faces
        HashMap<String, ArrayList<ArrayList<Integer[]>>>  materialFaces =  object.getMaterialFaces();
        for(String materialName : materialFaces.keySet()){
            //Specify which material to use
            f.println(String.format("usemtl %s", WavefrontUtility.textureName(materialName)));

            ArrayList<ArrayList<Integer[]>> faces = materialFaces.get(materialName);
            //Write all faces
            for (ArrayList<Integer[]> face : faces) {
                if(face == null)
                    continue;

                String faceEntry = "f";
                for (int x = face.size() - 1; x >= 0; x--) {
                    Integer[] indices = face.get(x);
                    //Format: vert index/texture coordinate/vert normal index
                    //Each index is calculated based on the sum of written vertices/uv/vertex normals + 1 (as in the Wavefront OBJ format indexes start with 1) + local index (ex 0)
                    faceEntry += String.format(" %d/%d/%d", countTracker[0] + 1 + indices[0], countTracker[1] + 1 + indices[1], countTracker[2] + 1 + indices[2]);
                }
                f.println(faceEntry);
            }

        }

        //Update size of written vertices on countTracker
        countTracker[0] += vertices.size();

        //Update size of written vertex normals on countTracker
        countTracker[2] += vertices.size();

        //Update size of written texture coordinates on countTracker
       countTracker[1] += uvs.size();

        return countTracker;
    }

}
