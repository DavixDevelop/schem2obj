package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.Orientation;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.AdjacentBlockState;
import com.davixdevelop.schem2obj.cubemodels.model.CubeFace;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.materials.Material;
import com.davixdevelop.schem2obj.materials.SEUSMaterial;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.BlockStateNamespace;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.resourceloader.ResourcePack;
import com.davixdevelop.schem2obj.util.ArrayUtility;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

/**
 * A utility class for working with CubeModels and various BlockModels
 *
 * @author DavixDevelop
 */
public class CubeModelUtility {
    public static Random RANDOM = new Random();

    /**
     * Extract default materials from models and return a map with texture variables and names of the material
     * for each variant model
     * @param models An array of models
     * @return map of key: texture variable, value: name of default material
     */
    public static HashMap<String, HashMap<String, String>> modelsToMaterials(VariantModels[] models, Namespace blockNamespace) {
        HashMap<String, HashMap<String, String>> textureMaterialsPerRootModel = new HashMap<>();

        for(VariantModels variantModels : models) {

            for (BlockModel model : variantModels.getModels()) {
                HashMap<String, String> modelTextures = model.getTextures().getTextures();

                //Get textures from block
                for (String key : modelTextures.keySet()) {
                    String rootModelName = variantModels.getVariant().getModel();

                    if(!textureMaterialsPerRootModel.containsKey(rootModelName)){
                        HashMap<String, String> textureMaterials = new HashMap<>();
                        textureMaterialsPerRootModel.put(rootModelName, textureMaterials);
                    }

                    HashMap<String, String> textureMaterials = textureMaterialsPerRootModel.get(rootModelName);


                    String value = modelTextures.get(key);

                    //Check if texture value doesn't have a variable (raw value, ex. block/dirt)
                    if (!value.startsWith("#")) {

                        generateOrGetMaterial(value, blockNamespace);

                        if (!textureMaterials.containsKey(key))
                            textureMaterials.put(key, value);

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

        return textureMaterialsPerRootModel;
    }

    /**
     * Get material or generate it
     * @param materialPath The path of material, ex. blocks/fire or entity/black-bed
     * @param namespace The namespace of the block the material uses
     */
    public static void generateOrGetMaterial(String materialPath, Namespace namespace){
        if (!Constants.BLOCK_MATERIALS.containsMaterial(materialPath)) {
            //ToDo: Implement this in LitBlockWavefrontObject

            //If material is a block or is an entity and doesn't contain - in it's material path
            //use the material path for the diffuse texture path

            IMaterial material = null;

            String diffuseTexturePath = null;

            if(materialPath.startsWith("blocks") || materialPath.startsWith("painting") || materialPath.startsWith("items") || (materialPath.startsWith("entity") && !materialPath.contains("-"))) {
                diffuseTexturePath = materialPath;
            }else if(materialPath.startsWith("entity") && materialPath.contains("-")){
                String materialName = textureName(materialPath);
                //If material path contains a -, it means the texture for that material is in a subfolder with the name of the entity
                //Ex: black-bed -> diffuseTextureFile = entity/bed/black
                diffuseTexturePath = String.format("entity/%s/%s", materialName.substring(materialName.indexOf('-') + 1), materialName.substring(0, materialName.indexOf('-')));
            }

            String diffusePath = ResourceLoader.getResourcePath("textures", diffuseTexturePath,"png");

            ResourcePack.Format materialFormat = ResourceLoader.getFormat(diffusePath);

            switch (materialFormat){
                case Vanilla:
                    material = new Material(materialPath, diffuseTexturePath);
                    break;
                case SEUS:
                    material = new SEUSMaterial(materialPath, diffuseTexturePath);
            }

            material.setEmissionStrength((namespace.getMetaIDS().isEmpty()) ? 0.0 : namespace.getDefaultBlockState().getLightValue());
            Constants.BLOCK_MATERIALS.setMaterial(materialPath, material);
        }
    }

    /**
     * Rotate a uv by a any angle
     * @param uvs The original unmodified uv list
     * @param angle The angle to shift rotate the uv's
     * @param rotationOrigin The origin of rotation. A Double array [x, y]
     */
    public static void rotateUV(List<Double[]> uvs, Double angle, Double[] rotationOrigin){
        //Set the rotation matrix in the axis Z
        ArrayVector.MatrixRotation uvRotation = new ArrayVector.MatrixRotation(-angle,"Z");
        for(int c = 0; c < uvs.size(); c++){
            Double[] uv = uvs.get(c);
            //Rotate the uv with rotatePoint by construction in vector that has an z value of 0
            Double[] new_uv = rotatePoint(new Double[] {uv[0], uv[1], 0.0}, uvRotation, rotationOrigin);
            uv[0] = round(new_uv[0], 6);
            uv[1] = round(new_uv[1], 6);

            uvs.set(c, uv);
        }
    }

    /**
     * Rotate a uv, by shifting the positions of the uv.
     * @param uvs The original unmodified uv list
     * @param angle The angle to shift rotate the uv's
     */
    public static void shiftRotateUV(ArrayList<Double[]> uvs, Double angle){

        //Make sure the angle is is between
        if(angle > 360.0 || angle < -360.0)
            angle %= 360.0;

        ArrayList<Double[]> shifted = new ArrayList<>(uvs);

        //If angle can be divided by 90 shifting the uv's by a offset
        if(angle % 90 == 0) {
            double offset = Math.abs(angle) / 90;

            if(angle > 0)
                offset *= -1;


            for (int c = 0; c < shifted.size(); c++) {
                int shift = (int) offset;
                if (c + shift >= shifted.size() || c + shift < 0) {
                    if (shift > 0)
                        shift = c + shift - shifted.size();
                    else
                        shift = c + shift + shifted.size();

                } else
                    shift = c + shift;

                shifted.set(c, uvs.get(shift));
            }
        }else{
            double median = Math.abs(angle) / 90.0;
            //Get the indexes to the start and end point that the median lies between

            boolean even = Math.ceil(median) % 2 == 0;

            for(int c = 0; c < shifted.size(); c++){
                double shiftedMedian = c + median;

                double iA = Math.floor(shiftedMedian) % 4;
                double iC = Math.ceil(shiftedMedian) % 4;


                if(angle > 0) {
                    if (even) {
                        iA = iA - 2;
                        if (iA < 0.0)
                            iA += 4;
                    } else {
                        iC = iC - 2;
                        if(iC < 0.0)
                            iC += 4;
                    }
                }

                Double[] A = uvs.get((int) iA);
                Double[] C = uvs.get((int) iC);

                double length = Math.sqrt(Math.pow(C[0] - A[0] ,2) + Math.pow(C[1] - A[1], 2));
                Double move_distance = (length * (Math.abs(angle) % 90)) / 90;

                if(A[0].equals(C[0])) {
                    if(A[1] < C[1])
                        shifted.set(c, new Double[]{A[0], A[1] + move_distance});
                    else
                        shifted.set(c, new Double[]{A[0], C[1] + move_distance});
                }else {
                    if(A[0] < C[0])
                        shifted.set(c, new Double[]{A[0] + move_distance, A[1]});
                    else
                        shifted.set(c, new Double[]{C[0] + move_distance, A[1]});
                }
            }
        }

        for(int c = 0; c < shifted.size(); c++)
            uvs.set(c, shifted.get(c));


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

    public static Double[] getUVFaceOrigin(ArrayList<Double[]> UVFace){
        Double x1 = UVFace.stream().min(Comparator.comparing(v -> v[0])).get()[0]; //Min x
        Double x2 = UVFace.stream().max(Comparator.comparing(v -> v[0])).get()[0]; //Max x
        Double y1 = UVFace.stream().min(Comparator.comparing(v -> v[1])).get()[1]; //Min y
        Double y2 = UVFace.stream().max(Comparator.comparing(v -> v[1])).get()[1]; //Max y

        return new Double[] {(x1 + x2) / 2, (y1 + y2) / 2, 0.0};
    }

    public static Double[] getRandomUV(int stack_size){
        //Get the uv height of one stack
        double stack_height = 1.0 / stack_size;
        //Get random index of stack
        int y = new Float(RANDOM.nextFloat() / (float) stack_height).intValue();

        //Default is selected bottom stack
        Double[] uv = new Double[]{0.0, 0.0, 1.0, round(stack_height, 6)};
        if(y > 0){
            uv[1] = y * stack_height;
            uv[3] = (y + 1) * stack_height;
        }

        return uv;
    }

    /**
     * Get the order of corners per face orientation
     * @param orientation The orientation of the face, ex. NORTH
     * @return A array of Corners
     */
    public static String[] getCornerPerOrientation(Orientation orientation){
        String[] corners = new String[]{"A","B","C","D"};
        switch (orientation){
            case NORTH:
                corners = new String[]{"M","F","C","D"}; //Orientation M:7 F:4 C:2 D:3
                break;
            case SOUTH:
                corners = new String[]{"A","B","G","H"}; //Orientation A:0 B:1 G:5 H:6
                break;
            case UP:
                corners = new String[]{"B","C","F","G"}; //Orientation B:1 C:2 F:4 G:5
                break;
            case DOWN:
                corners = new String[]{"D","A","H","M"}; //Orientation D:3 A:0 H:6 M:7
                break;
            case WEST:
                corners = new String[]{"D","C","B","A"}; //Orientation D:3 C:2 B:1 A:0
                break;
            case EAST:
                corners = new String[]{"H","G","F","M"}; //Orientation H:6 G:5 F:4 M:7
                break;
        }

        return corners;
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
             markCorners(addCorners, new Integer[]{7,4,2,3});
        }

        if(faces.contains("south")){
            //South face uses A B G H corners
             markCorners(addCorners, new Integer[]{0,1,5,6});
        }

        if(faces.contains("up")){
            //Up face uses B C F G corners
             markCorners(addCorners, new Integer[]{1,2,4,5});
        }

        if(faces.contains("down")){
            //Down face uses D A H M corners
             markCorners(addCorners, new Integer[]{3,0,6,7});
        }

        if(faces.contains("east")){
            //East face uses H G F M corners
             markCorners(addCorners, new Integer[]{6,5,4,7});
        }

        if(faces.contains("west")){
            //West face uses D C B A corners
             markCorners(addCorners, new Integer[]{3,2,1,0});
        }




        //Check if height, width or length of cube is zero and increase it by 0.0009 to avoid overlapping face (Z-fighting)
        if((A[2].equals(F[2])) || (A[1].equals(F[1])) || (A[0].equals(F[0])))
        {
            //Check height
            increaseOverlappingCube(A, F, 2);
            //Check length
            increaseOverlappingCube(A, F, 1);
            //Check width
            increaseOverlappingCube(A, F, 0);
        }




        if(addCorners[0])//Corner A
            vertices.put("A", ArrayUtility.cloneArray(A));
        if(addCorners[1]) //Corner B
            vertices.put("B", new Double[]{A[0], A[1], F[2]});
        if(addCorners[2]) //Corner C
            vertices.put("C", new Double[]{A[0], F[1], F[2]});
        if(addCorners[3]) //Corner D
            vertices.put("D",new Double[]{A[0], F[1], A[2]});
        if(addCorners[4]) //Corner F
            vertices.put("F",ArrayUtility.cloneArray(F));
        if(addCorners[5]) //Corner G
            vertices.put("G",new Double[]{F[0], A[1], F[2]});
        if(addCorners[6]) //Corner H
            vertices.put("H", new Double[]{F[0], A[1], A[2]});
        if(addCorners[7]) //Corner M
            vertices.put("M", new Double[]{F[0], F[1], A[2]});

        return  vertices;
    }

    /**
     * //Check if point A and F lay on the same axis (axisIndex) and increase it by 0.0009 to avoid overlapping face (Z-fighting)
     * @param A The start point of the cube
     * @param F The end point of the cube
     * @param axisIndex The index of the axis (0 -> X, 1 -> Y, 2 -> Z)
     */
    private static void increaseOverlappingCube(Double[] A, Double[] F, int axisIndex){
        if(A[axisIndex].equals(F[axisIndex])){
            Double OVERLAP_SIZE = 0.0009;
            if(A[axisIndex].equals(0.5)) {
                A[axisIndex] = round(A[axisIndex] - OVERLAP_SIZE, 6);
                F[axisIndex] = round(F[axisIndex] + OVERLAP_SIZE, 6);
            }
            else if(A[axisIndex] < 0.5) {
                OVERLAP_SIZE /= 2.0;
                F[2] = round(F[2] + OVERLAP_SIZE, 6);
            }else if(A[axisIndex] > 0.5) {
                OVERLAP_SIZE /= 2.0;
                A[axisIndex] = round(A[axisIndex] - OVERLAP_SIZE, 6);
            }

        }
    }

    private static void markCorners(Boolean[] addCorners, Integer[] indexes){
        for(int index : indexes)
            addCorners[index] = true;
    }

    /**
     * Move the cube model in the space to the desired position
     * @param cubeModel The block cube model to translate
     * @param position The position of the block in the space [x, y, z]
     * @param spaceSize The size of the space [width, length, height]
     */
    public static void translateCubeModel(ICubeModel cubeModel, Double[] position, Integer[] spaceSize){
        //Value by how much to move each vert (vert + translate)
        double translateX = position[0] - (spaceSize[0].doubleValue() / 2);
        double translateY = (position[1] * -1) + ((spaceSize[1].doubleValue() / 2) - 1);
        //Double translateZ = position[2].doubleValue();// - (spaceSize[2].doubleValue());
        Double[] translate = new Double[]{translateX, translateY, position[2]};

        translateCubeModel(cubeModel, translate);

    }

    /**
     * Move the cube model by the translate vector (X, Y, Z)
     * @param cubeModel The block cube model to translate
     * @param translate A 3 length Double array representing a direction vector
     */
    public static void translateCubeModel(ICubeModel cubeModel, Double[] translate){
        List<ICube> cubes = cubeModel.getCubes();

        //Loop through cubes
        for(ICube cube : cubes){
            CubeFace[] cubeFaces = cube.getFaces();
            List<Double[]> corners = cube.getCorners();

            for (CubeFace cubeFace : cubeFaces) {
                if (cubeFace != null) {
                    List<Integer> verticesIndex = cubeFace.getCorners();

                    //Sum each vertex and translate
                    for (Integer index : verticesIndex)
                        corners.set(index, ArrayVector.add(corners.get(index), translate));
                }
            }
        }
    }

    /**
     * Rotate the cube model around the origin by on each axis, depending the xyzRot array
     * @param cubeModel The cube model to rotate
     * @param xyzRot A 3 length array, that contains the rotation value for each axis [x, y, z]
     * @param origin A length array, that contains the origin of rotation
     */
    public static void rotateCubeModel(ICubeModel cubeModel, Double[] xyzRot, Double[] origin){
        ArrayVector.MatrixRotation rotationX = null;
        ArrayVector.MatrixRotation rotationY = null;
        ArrayVector.MatrixRotation rotationZ = null;
        if(xyzRot[0] != 0.0)
            rotationX = new ArrayVector.MatrixRotation(xyzRot[0], "X");

        if(xyzRot[1] != 0.0)
            rotationY = new ArrayVector.MatrixRotation(xyzRot[1], "Y");

        if(xyzRot[2] != 0.0)
            rotationZ = new ArrayVector.MatrixRotation(xyzRot[2], "Z");

        if(rotationX != null || rotationY != null || rotationZ != null){
            List<ICube> cubes = cubeModel.getCubes();

            //Loop through cubes
            for(ICube cube : cubes){
                CubeFace[] cubeFaces = cube.getFaces();
                List<Double[]> corners = cube.getCorners();

                for (CubeFace cubeFace : cubeFaces) {
                    if (cubeFace != null) {
                        List<Integer> verticesIndex = cubeFace.getCorners();

                        //Sum each vertex on each axis
                        for (Integer index : verticesIndex) {
                            if (rotationX != null)
                                corners.set(index, rotatePoint(corners.get(index), rotationX, origin));
                            if (rotationY != null)
                                corners.set(index, rotatePoint(corners.get(index), rotationY, origin));
                            if (rotationZ != null)
                                corners.set(index, rotatePoint(corners.get(index), rotationZ, origin));
                        }
                    }
                }
            }
        }
    }

    /**
     * Rotate the cube model around the origin by on each axis, depending the xyzRot array
     * @param cubeModel The cube model to rotate
     * @param xyzScale A 3 length array, that contains the rotation value for each axis [x, y, z]
     * @param origin A length array, that contains the origin of rotation
     */
    public static void scaleCubeModel(ICubeModel cubeModel, Double[] xyzScale, Double[] origin){
        List<ICube> cubes = cubeModel.getCubes();

        //Loop through cubes
        for(ICube cube : cubes){
            CubeFace[] cubeFaces = cube.getFaces();
            List<Double[]> corners = cube.getCorners();

            for (CubeFace cubeFace : cubeFaces) {
                if (cubeFace != null) {
                    List<Integer> verticesIndex = cubeFace.getCorners();

                    //Sum each vertex and translate
                    for (Integer index : verticesIndex) {
                        corners.set(index, scalePoint(corners.get(index), xyzScale[0], xyzScale[1], xyzScale[2], origin));
                    }
                }
            }
        }
    }

    /**
     * Rotate a point with the given rotation matrix around the given origin of rotation
     * @param point A Double array representing a point in a XYZ space
     * @param rotation A rotation matrix for the X, Y or Z axis
     * @param origin The rotation origin in the XYZ space
     * @return The rotated point
     */
    public static Double[] rotatePoint(Double[] point, ArrayVector.MatrixRotation rotation, Double[] origin){
        //Subtract the point by block origin, so that the origin of the block becomes 0,0,0
        point = ArrayVector.subtract(point, origin);

        //Rotate the point with the rotation matrix
        point = rotation.rotate(point, 1.0);

        //Add the rotated point and rotation origin, so that the origin of the block becomes the rotation origin again
        point = ArrayVector.add(point, origin);

        //Round the values to 6 decimals
        for(int c = 0; c < point.length; c++)
            point[c] = round(point[c], 6);

        return point;
    }

    /**
     * Scale point on given axis from the origin by the scale
     * @param point A Double array representing a point in a XYZ space
     * @param scaleX A double value from 0.0 on forward (ex. 1.0 in 100%, meaning unchanged point)
     * @param scaleY A double value from 0.0 on forward (ex. 1.0 in 100%, meaning unchanged point)
     * @param scaleZ A double value from 0.0 on forward (ex. 1.0 in 100%, meaning unchanged point)
     * @param origin The scale origin in the XYZ space
     * @return The scale point
     */
    public static Double[] scalePoint(Double[] point, Double scaleX, Double scaleY, Double scaleZ, Double[] origin){
        //Subtract the point by block origin, so that the origin of the block becomes 0,0,0
        point = ArrayVector.subtract(point, origin);

        //Scale the point on the given axis
        point[0] *= scaleX;
        point[1] *= scaleY;
        point[2] *= scaleZ;

        //Add the scaled point and scale origin, so that the origin of the block becomes the scale origin again
        point = ArrayVector.add(point, origin);

        //Round the scaled point on the given axis to 6 decimals
        for(int c = 0; c < point.length; c++)
            point[c] = round(point[c], 6);

        return point;
    }

    public static Double round(Double value, int decimals){
        return new BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Check if two cube models bounding boxes are connected on faceOrientation and adjacentFaceOrientation.
     * @param cubeModel The original parent object
     * @param adjacentCubeModel The parent object to check
     * @param faceOrientation //The name of the face on the object to check
     * @param adjacentFaceOrientation //The name of the face on the parent object to check
     * @return True if the two faces are connected, else false
     */
    public static boolean checkFacing(ICubeModel cubeModel, ICubeModel adjacentCubeModel, Orientation faceOrientation, Orientation adjacentFaceOrientation){
        if(adjacentCubeModel == null)
            return false;

        if(cubeModel.checkCollision(adjacentCubeModel)){
            List<ICube> cubes = cubeModel.getCubes();
            List<ICube> adjacentCubes = adjacentCubeModel.getCubes();

            if(cubes.size() > 0 && adjacentCubes.size() > 0){
                Integer faceIndex = faceOrientation.getOrder();
                Integer adjacentFaceIndex = adjacentFaceOrientation.getOrder();

                for(ICube cube : cubes){
                    CubeFace cubeFace = cube.getFaces()[faceIndex];

                    if(cubeFace != null && cubeFace.isCullFace()){

                        for(ICube adjacentCube : adjacentCubes){
                            CubeFace adjacentCubeFace = adjacentCube.getFaces()[adjacentFaceIndex];

                            if(adjacentCubeFace != null && adjacentCubeFace.isCullFace()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check north, south, west and east block, and depending on the check in IAdjacentCheck,
     * either set the modified namespace blockstate property of the orientation to true (default behaviour),
     * or perform some action upon the modified namespace as specified by IModifyNamespace
     *
     * @param modified The namespace to modify
     * @param check An interface which checks if the adjacent blocks meets a certain condition
     * @param modifyNamespace Optional interface that modifies the namespace if the `check` returns true
     */
    public static void getAdjacentNamespace_NSWE(Namespace modified, IAdjacentCheck check, IModifyNamespace ...modifyNamespace){
        //Check north
        Namespace adjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                modified.getPosition("X"),
                modified.getPosition("Y"),
                modified.getPosition("Z") - 1
                );
        if(adjacentBlock != null){
            if(check.checkCollision(adjacentBlock, 0, "north")) {
                if(modifyNamespace.length != 0)
                    modifyNamespace[0].modifyNamespace(modified);
                else
                    modified.getDefaultBlockState().getData().put("north", "true");
            }
        }

        //Check south
        adjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                modified.getPosition("X"),
                modified.getPosition("Y"),
                modified.getPosition("Z") + 1);
        if(adjacentBlock != null){
            if(check.checkCollision(adjacentBlock, 0, "south")) {
                if (modifyNamespace.length != 0)
                    modifyNamespace[0].modifyNamespace(modified);
                else
                    modified.getDefaultBlockState().getData().put("south", "true");
            }
        }

        //Check west
        adjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                modified.getPosition("X") - 1,
                modified.getPosition("Y"),
                modified.getPosition("Z"));
        if(adjacentBlock != null){
            if(check.checkCollision(adjacentBlock, 0, "west")) {
                if(modifyNamespace.length != 0)
                    modifyNamespace[0].modifyNamespace(modified);
                else
                    modified.getDefaultBlockState().getData().put("west", "true");
            }
        }

        //Check east
        adjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                modified.getPosition("X") + 1,
                modified.getPosition("Y"),
                modified.getPosition("Z"));
        if(adjacentBlock != null){
            if(check.checkCollision(adjacentBlock, 0, "east")) {
                if(modifyNamespace.length != 0)
                    modifyNamespace[0].modifyNamespace(modified);
                else
                    modified.getDefaultBlockState().getData().put("east", "true");
            }
        }
    }

    public static void getAdjacentNamespace_AdjacentState(Namespace namespace, AdjacentBlockState adjacentBlockStates, IAdjacentCheck check){
        Namespace stockNamespace = namespace.duplicate();

        //Get list of order of orientations to check
        List<String> checkOrder = adjacentBlockStates.getCheckOrder();

        for(String orientation : checkOrder){
            int x = namespace.getPosition("X");
            int y = namespace.getPosition("Y");
            int z = namespace.getPosition("Z");


            String orientation_raw = (orientation.endsWith("-1") || orientation.endsWith("+1")) ? orientation.substring(0, orientation.length() - 2) : orientation;
            switch (orientation_raw){
                case "south":
                    z += 1;
                    break;
                case "east":
                    x += 1;
                    break;
                case "north":
                    z -= 1;
                    break;
                case "west":
                    x -= 1;
                    break;
            }

            if(orientation.endsWith("+1"))
                y += 1;
            else if(orientation.endsWith("-1"))
                y -= 1;

            Namespace adjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(x, y, z);
            if(adjacentBlock != null) {
                if(check.checkCollision(adjacentBlock,
                        (orientation.endsWith("+1")) ? 1 : (orientation.endsWith("-1") ? -1 : 0),
                        orientation_raw)){
                    Map<String, String> statesToApply = adjacentBlockStates.getStates(orientation, stockNamespace.getDefaultBlockState().getData(), adjacentBlock.getDefaultBlockState().getData());
                    if(statesToApply != null){
                        BlockStateNamespace.cloneData(namespace.getDefaultBlockState(), statesToApply);
                    }
                }
            }
        }

    }

    public static void getKey_NSWE(Map<String, Object> key, Namespace namespace){
        key.put("north", namespace.getDefaultBlockState().getData().get("north"));
        key.put("south", namespace.getDefaultBlockState().getData().get("south"));
        key.put("east", namespace.getDefaultBlockState().getData().get("east"));
        key.put("west",namespace.getDefaultBlockState().getData().get("west"));
    }

    /**
     * Get the plane (axis) of the face
     * @param plane A double array with the value of the plane (axis)
     * @param vertices Vertices of the face
     * @return The index of the axis (0->X, 1->Y->, 2->Z)
     */
    public static int getFacePlane(Double[] plane, List<Double[]> vertices){
        Boolean x = null;
        Boolean y = null;
        Boolean z = null;

        Double[] temp = new Double[3];

        Double[] v1 = vertices.get(0);
        for(int v = 1; v < vertices.size(); v++){
            Double[] v2 = vertices.get(v);

            if(v1[0].equals(v2[0])){
                temp[0] = v1[0];
                if(x == null)
                    x = true;
            } else
                x = false;

            if(v1[1].equals(v2[1])){
                temp[1] = v1[1];
                if(y == null)
                    y = true;
            } else
                y = false;

            if(v1[2].equals(v2[2])){
                temp[2] = v1[2];
                if(z == null)
                    z = true;
            } else
                z = false;

        }

        if(x != null && x) {
            plane[0] = temp[0];
            return 0;
        }

        if(y != null && y) {
            plane[1] = temp[1];
            return 1;
        }

        if(z != null && z) {
            plane[2] = temp[2];
            return 2;
        }

        //Face is rotated
        //Calculate the face normal
        Double[] a = vertices.get(0);
        Double[] b = vertices.get(1);
        Double[] c = vertices.get(2);
        Double[] face_normal = ArrayVector.normalize(ArrayVector.multiply(ArrayVector.subtract(b, a), ArrayVector.subtract(c, a)));

        //First 3 values is the face of the normal, while the others are the origin
        plane[0] = face_normal[0];
        plane[1] = face_normal[1];
        plane[2] = face_normal[2];

        Double x1 = vertices.stream().min(Comparator.comparing(v -> v[0])).get()[0]; //Min x
        Double x2 = vertices.stream().max(Comparator.comparing(v -> v[0])).get()[0]; //Max x
        Double y1 = vertices.stream().min(Comparator.comparing(v -> v[1])).get()[1]; //Min y
        Double y2 = vertices.stream().max(Comparator.comparing(v -> v[1])).get()[1]; //Max y
        Double z1 = vertices.stream().min(Comparator.comparing(v -> v[2])).get()[2]; //Min y
        Double z2 = vertices.stream().max(Comparator.comparing(v -> v[2])).get()[2]; //Max y

        plane[3] = (x1 + x2) / 2;
        plane[4] = (y1 + y2) / 2;
        plane[5] = (z1 + z2) / 2;



        return -1;

    }

    /**
     * Avoid overlapping of cube that has one face, by looping through others cube,
     * checking for collision, and increasing It's cubeCorners, one the face direction vector.
     * Ex, this is used on blocks like Iron Bars, Redstone Wire... that use face that lie on the same plane
     * @param orientation The orientation of the face
     * @param cubeCorners The corner of the cube
     * @param cubes Other cubes (faces) on the cube model, ex. other faces of the redstone wire
     */
    public static void avoidOverlapping(Orientation orientation, Map<String, Double[]> cubeCorners, List<ICube> cubes){

        double OVERLAP_SIZE = 0.00045;

        //Get the order of corners for the face
        String[] faceCornersOrder = getCornerPerOrientation(orientation);

        ArrayList<Double[]> faceVertices = new ArrayList<>();
        //Get the vertices for the face
        for(String corner : faceCornersOrder)
            faceVertices.add(cubeCorners.get(corner));

        //Get face plane
        Double[] facePlane = new Double[6];
        int planeIndex = getFacePlane(facePlane, faceVertices);

        boolean increased = false;

        double vector_distance = 0.0;

        //Loop thorough all faces in the cubes and check if they overlap
        for(ICube cube : cubes){

            //Get all faces of cube
            CubeFace[] cubeFaces = cube.getFaces();

            List<Double[]> corners = cube.getCorners();

            for(CubeFace face : cubeFaces){
                if(face == null)
                    continue;

                List<Integer> faceIndexVertices = face.getCorners();

                List<Double[]> faceVertices2 = new ArrayList<>();
                for(Integer v : faceIndexVertices)
                    faceVertices2.add(corners.get(v));

                Double[] facePlane2 = new Double[6];
                int planeIndex2 = getFacePlane(facePlane2, faceVertices2);

                if(planeIndex != -1 && planeIndex2 != -1) {
                    //If both faces are on the same plane, increase the first plane axis
                    if (planeIndex == planeIndex2 && facePlane[planeIndex].equals(facePlane2[planeIndex2])) {
                        increased = true;
                        facePlane[planeIndex] += OVERLAP_SIZE;
                    }
                }else if(planeIndex == -1 && planeIndex2 == -2){

                    //If both faces have the same normal, and origin
                    if(facePlane[0].equals(facePlane2[0]) && facePlane[1].equals(facePlane2[1]) && facePlane[2].equals(facePlane2[2]) &&
                            facePlane[3].equals(facePlane2[3]) && facePlane[4].equals(facePlane2[4]) && facePlane[5].equals(facePlane2[5])){
                        increased = true;
                        vector_distance += OVERLAP_SIZE;
                        //Move the plane origin on the normal vector by vector distance

                        Double[] OriginNormal = new Double[]{
                                facePlane[3] - facePlane[0],
                                facePlane[4] - facePlane[1],
                                facePlane[5] - facePlane[2]
                        };

                        Double[] movedOrigin = ArrayVector.add(
                                new Double[]{facePlane[3], facePlane[4], facePlane[5]},
                                ArrayVector.multiply(OriginNormal, vector_distance));

                        facePlane[3] = round(movedOrigin[0],6);
                        facePlane[4] = round(movedOrigin[1],6);
                        facePlane[5] = round(movedOrigin[2],6);
                    }
                }
            }
        }

        //Update the corner vertices with to the new plane
        if(increased){
            for(String corner : faceCornersOrder){
                Double[] vert = cubeCorners.get(corner);
                if(planeIndex != -1)
                    vert[planeIndex] = round(facePlane[planeIndex], 6);
                else{
                    Double[] VertexNormal = new Double[]{
                            vert[0] - facePlane[0],
                            vert[1] - facePlane[1],
                            vert[2] - facePlane[2]
                    };

                    vert = ArrayVector.add(vert, ArrayVector.multiply(VertexNormal, vector_distance));

                    vert[0] = round(vert[0], 6);
                    vert[1] = round(vert[1], 6);
                    vert[2] = round(vert[2], 6);
                }
                cubeCorners.put(corner, vert);
            }
        }


    }

    /**
     * Get the the starting and ending point of a line, bound to the block bound (0.0,0.0 -> 1.0, 1.0), if we would draw a line through A1 and B1
     *-------------          -B-----------
    *| B1        |           | .         |
    *|   .       |    ->     |   .       |
    *|    .      |    ->     |    .      |
    *|     A1    |           |     .     |
    *-------------           -------A-----
     *
     * @param A1 The starting point of the original line
     * @param B1 The ending point of the original line
     * @param A The starting point of the original line bound to the block bound
     * @param B The ending point of the original line bound to the block bound
     */
    public static void getBoundingLineSegment(Double[] A1, Double[] B1, Double[] A, Double[] B){
        //Calculate the linear function of this line
        double k = (B1[1] - A1[1]) / (B1[0] - A1[0]);
        double n = A1[1] - (k * A1[0]);

        //Calculate the A and B point of the line bound to the block bounds (0.0,0.0 to 1.0,1.0)
                                    /*-B-----------
                                      | .         |
                                      |   .       |
                                      |    .      |
                                      |     .     |
                                      -------A-----
                                     */
        B[0] = 0.0;
        B[1] = n;
        //If the line touches the ordinate out out the block bound, it means it touches the top of the block bound
        if(n > 1.0){
            B[0] = (n - 1.0) / -k;
            B[1] = 1.0;
        }
        A[0] = 1.0;
        A[1] = k + n;
        //If the line touches the right side of the block bound bellow the abscissa (z < 0.0), it means it touches the bottom of the block bound
        if(A[1] < 0.0){
            A[0] = n / -k;
            A[1] = 0.0;
        }
    }

    /**
     * Set the UV's of a face in a clockwise orientation
     * @param face The face of the cube element
     * @param from The starting point of the cube
     * @param to The ending point of the cube
     * @return A 4 size array of UV's oriented clockwise (see getCornerPerOrientation, to see the order of corners)
     */
    public static ArrayList<Double[]> setAndRotateUVFace(CubeElement.CubeFace face, Orientation orientation, Double[] from, Double[] to){
        ArrayList<Double[]> UVFace = new ArrayList<>();
        if(face.getUv() == null){

            switch (orientation){
                case UP:
                case DOWN:
                    UVFace.add(new Double[]{to[0], to[1]}); //4
                    UVFace.add(new Double[]{to[0], from[1]}); //3
                    UVFace.add(new Double[]{from[0], from[1]}); //2
                    UVFace.add(new Double[]{from[0], to[1]}); //1
                    break;
                case NORTH:
                case SOUTH:
                    UVFace.add(new Double[]{to[0], to[2]});
                    UVFace.add(new Double[]{to[0], from[2]});
                    UVFace.add(new Double[]{from[0], from[2]});
                    UVFace.add(new Double[]{from[0], to[2]});
                    break;
                case WEST:
                case EAST:
                    UVFace.add(new Double[]{to[1], to[2]});
                    UVFace.add(new Double[]{to[1], from[2]});
                    UVFace.add(new Double[]{from[1], from[2]});
                    UVFace.add(new Double[]{from[1], to[2]});
                    break;
            }



        }else{
            //MC face uv is a 4 item array [x1, y1, x2, y2]
            Double[] rawUV = face.getUv();

            UVFace.add(new Double[]{rawUV[0], rawUV[1]}); //x1,y1 //1
            UVFace.add(new Double[]{rawUV[0], rawUV[3]}); //x1,y2 //2
            UVFace.add(new Double[]{rawUV[2], rawUV[3]}); //x2,y2 //3
            UVFace.add(new Double[]{rawUV[2], rawUV[1]}); //x2,y1 //4
        }

        //Rotate uv, to simulate rotation of texture
        if(face.getRotation() != null){
            //Calculate the origin of the UV face
            Double[] faceOrigin = getUVFaceOrigin(UVFace);

            if(face.getUv() == null)
                rotateUV(UVFace, (orientation.equals(Orientation.SOUTH) ||  orientation.equals(Orientation.NORTH)) ? -face.getRotation() : face.getRotation(), faceOrigin);
            else if(face.getRotation() % 90 == 0)
                shiftRotateUV(UVFace, face.getRotation());

        }

        return UVFace;
    }

    public static void convertCubeElementToCubeModel(CubeElement element, boolean uvLock, ArrayVector.MatrixRotation rotationX, ArrayVector.MatrixRotation rotationY, HashMap<String, String> modelsMaterials, CubeModel cubeModel){
        //Variable to store cube corner and their vertices index
        //Key: corner (ex. A), Value: Index of vertex
       // Map<String, Integer> CornersIndex = new HashMap<>();
        //Variable to store index of UV's

        //Get starting point of cube
        Double[] from = element.getFrom();
        //Get end point of cube
        Double[] to = element.getTo();

        //Create vertices for each corner of a face that the cube uses and add them to the object vertices
        Map<String, Double[]> cubeCorners = createCubeVerticesFromPoints(from, to, element.getFaces().keySet());

        //Array to store index to material per face (See Orientation.DIRECTIONS for order of faces)
        Integer[] materialFaces = new Integer[6];
        //Array to store which faces should be exported (See Orientation.DIRECTIONS for order of faces)
        Boolean[] generatedFaces = new Boolean[]{false, false, false, false, false, false};
        //Array to store cube faces (See Orientation.DIRECTIONS for order of faces)
        CubeFace[] cubeFaces = new CubeFace[6];

        //Hashed double list to store all the corners the cube uses
        HashedDoubleList corners = new HashedDoubleList();
        //Hashed double list to store all texture coordinates the cube uses
        HashedDoubleList textureCoordinates = new HashedDoubleList();

        //Check if element has rotation
        if (element.getRotation() != null) {
            CubeElement.CubeRotation cubeRotation = element.getRotation();

            String[] axes = cubeRotation.getAxis();
            for(int a = 0; a < axes.length; a++) {
                Double angle = cubeRotation.getAngle()[a];
                String axis = axes[a];

                //Construct matrix rotation based on the axis and angle
                ArrayVector.MatrixRotation matrixRotation = new ArrayVector.MatrixRotation(-angle, axis);

                Double[] rotation_origin = Constants.BLOCK_ORIGIN;
                if (cubeRotation.getOrigin() != null)
                    rotation_origin = cubeRotation.getOrigin();

                //loop through the corners (vertices) and rotate each vertex
                for (String corner : cubeCorners.keySet())
                    cubeCorners.put(corner, rotatePoint(cubeCorners.get(corner), matrixRotation, rotation_origin));

                if(cubeRotation.getRescale()){

                    Double scale_x = null;
                    Double scale_y = null;
                    Double scale_z = null;

                    double absRot = Math.abs(angle);

                    //The min/max values (points) of axis
                    Double[] min_x = null;
                    Double[] max_x = null;
                    Double[] min_y = null;
                    Double[] max_y = null;

                    //Find min/max values of x and y axis
                    for(String corner : cubeCorners.keySet()){
                        Double[] vert = cubeCorners.get(corner);

                        if(min_x == null){
                            min_x = vert;
                            max_x = min_x;
                            min_y = vert;
                            max_y = min_y;
                        }else{
                            if(vert[0] > max_x[0])
                                max_x = vert;
                            if(vert[0] < min_x[0])
                                min_x = vert;

                            if(vert[1] > max_y[1])
                                max_y = vert;
                            if(vert[1] < min_y[1])
                                min_y = vert;
                        }
                    }

                    switch (axis){
                        case "X":
                        case "Z":
                            if(max_y != null) {
                                //Get length of cube
                                Double length = Math.abs(max_y[1] - min_y[1]);
                                /*  / |
                                   /  |
                                c /   |
                                 /    |
                                /_____|
                                 width
                                 */
                                //Calculate the hypotenuse of the rotated length
                                Double c = length / Math.cos(Math.toRadians(absRot));
                                if(axis.equals("X")) {
                                    scale_y = c / length;
                                    scale_z = scale_y;
                                    scale_x = 1.0;
                                }else {
                                    scale_x = c / length;
                                    scale_y = scale_x;
                                    scale_z = 1.0;
                                }

                            }
                            break;
                        case "Y":
                            if(max_x != null) {
                                //Get width of cube
                                Double width = Math.abs(max_x[0] - min_x[0]);
                                /*  / |
                                   /  |
                                c /   |
                                 /    |
                                /_____|
                                 width
                                 */
                                //Calculate the hypotenuse of the rotated width
                                Double c = width / Math.cos(Math.toRadians(absRot));
                                scale_x = c / width;
                                scale_z = scale_x;
                                scale_y = 1.0;
                            }

                    }

                    if(scale_x != null) {
                        for (String corner : cubeCorners.keySet()) {
                            Double[] point = cubeCorners.get(corner);

                            //Scale the point on each axis
                            point = scalePoint(point, scale_x, scale_y, scale_z, rotation_origin);

                            cubeCorners.put(corner, point);

                        }
                    }
                }
            }


        }


        //Loop through the cube corners and to rotate them if variant specifies that it should be rotated
        if(rotationX != null || rotationY != null) {
            for (String corner : cubeCorners.keySet()) {
                if (rotationX != null)
                    cubeCorners.put(corner, rotatePoint(cubeCorners.get(corner), rotationX, Constants.BLOCK_ORIGIN));

                if (rotationY != null)
                    cubeCorners.put(corner, rotatePoint(cubeCorners.get(corner), rotationY, Constants.BLOCK_ORIGIN));
            }
        }

        //Get element faces
        HashMap<String, CubeElement.CubeFace> elementFaces = element.getFaces();

        //Check if cube only has one face
        if(elementFaces.size() == 1 && cubeModel.getCubes().size() > 0){
            //If it has, check if it overlaps with any faces, and increase the distance between the overlapping faces
            avoidOverlapping(Orientation.getOrientation(elementFaces.keySet().stream().findFirst().get()), cubeCorners, cubeModel.getCubes());
        }

        //Map to store what material is used per face before any kind of rotation has been done on the faces
        //Key: Orientation, value: material name
        HashMap<Orientation, String> materialPerOrientation = new HashMap<>();

        for (String faceName : elementFaces.keySet()) {
            CubeElement.CubeFace face = elementFaces.get(faceName);

            Orientation faceOrientation = Orientation.getOrientation(faceName);

            //Get the variable of the face (ex. #all)
            String faceTextureVariable = face.getTexture().substring(1);
            //Get the actual value of the material variable (ex. blocks/dirt)
            String faceMaterial = modelsMaterials.get(faceTextureVariable);

            materialPerOrientation.put(faceOrientation, faceMaterial);
        }

        //If either x or y rotation are not square, disable rotating face orientation,
        // use the face name to get orientation and disable cull faces
        boolean oddAngles = false;

        if(rotationX != null)
            oddAngles = rotationX.getRot() % 90.0 != 0.0;

        if(rotationY != null){
            if(!oddAngles)
                oddAngles = rotationY.getRot() % 90.0 != 0.0;
        }



        for (String faceName : elementFaces.keySet()) {

            CubeElement.CubeFace face = elementFaces.get(faceName);

            Orientation faceOrientation = Orientation.getOrientation(faceName);

            //Get the face uv's, or set them, if It's not defined in the uv field
            List<Double[]> faceUV = setAndRotateUVFace(face, faceOrientation, to, from);

            if(!oddAngles) {
                if (uvLock) {
                    if (rotationX != null) {
                        //If face orientation is west or east rotate the uv cords by the rotation X
                        //on the origin 0.5 0.5
                        if (faceOrientation.equals(Orientation.WEST) || faceOrientation.equals(Orientation.EAST))
                            rotateUV(faceUV, rotationX.getRot(), new Double[]{0.5, 0.5, 0.0});
                        else {
                        /*//Else rotate orientationCoord by closest right angle (ex, 100 -> 90)
                        ArrayVector.MatrixRotation rotation = rotationX;

                        //If the rotation angle on x isn't a right angle, move the uv cords by the module 90 (ex. 100 % 90 = 10 / 90 -> 0.1111..)
                        if (rotationX.getRot() % 90 != 0) {
                            Double angle = rotation.getRot() / 90;
                            rotation = new ArrayVector.MatrixRotation(Math.floor(angle), "X");
                            Double offsetX = (rotationX.getRot() % 90) / 90;
                            faceUV = WavefrontUtility.offsetUV(faceUV, offsetX, 0.0);
                        }*/
                            Double[] newOrientationCoord = new Double[]{faceOrientation.getXOffset().doubleValue(), faceOrientation.getYOffset().doubleValue(), faceOrientation.getZOffset().doubleValue()};
                            newOrientationCoord = rotatePoint(newOrientationCoord, rotationX, new Double[]{0.0, 0.0, 0.0});
                            faceOrientation = Orientation.getOrientation(newOrientationCoord[0].intValue(), newOrientationCoord[1].intValue(), newOrientationCoord[2].intValue());
                        }
                    }

                    if (rotationY != null) {
                        //If face orientation is top or down, rotate by the rotation Y
                        //on the origin 0.5 0.5
                        if (faceOrientation.equals(Orientation.UP) || faceOrientation.equals(Orientation.DOWN))
                            rotateUV(faceUV, rotationY.getRot(), new Double[]{0.5, 0.5, 0.0});
                        else {
                        /*//Else rotate orientationCoord by closest right angle (ex, 100 -> 90)
                        ArrayVector.MatrixRotation rotation = rotationY;

                        //If the rotation angle on x isn't a right angle, move the uv cords by the module 90 (ex. 100 % 90 = 10 / 90 -> 0.1111..)
                        if (rotationY.getRot() % 90 != 0) {
                            Double angle = rotation.getRot() / 90;
                            rotation = new ArrayVector.MatrixRotation(Math.floor(angle), "Z");
                            Double offsetY = (rotationX.getRot() % 90) / 90;
                            faceUV = WavefrontUtility.offsetUV(faceUV, 0.0, offsetY);
                        }*/

                            Double[] newOrientationCoord = new Double[]{faceOrientation.getXOffset().doubleValue(), faceOrientation.getYOffset().doubleValue(), faceOrientation.getZOffset().doubleValue()};
                            newOrientationCoord = rotatePoint(newOrientationCoord, rotationY, new Double[]{0.0, 0.0, 0.0});
                            faceOrientation = Orientation.getOrientation(newOrientationCoord[0].intValue(), newOrientationCoord[1].intValue(), newOrientationCoord[2].intValue());
                        }
                    }
                } else {
                    if (rotationX != null) {
                        if (!faceOrientation.equals(Orientation.WEST) && !faceOrientation.equals(Orientation.EAST)) {
                            Double[] newOrientationCoord = new Double[]{faceOrientation.getXOffset().doubleValue(), faceOrientation.getYOffset().doubleValue(), faceOrientation.getZOffset().doubleValue()};
                            newOrientationCoord = rotatePoint(newOrientationCoord, rotationX, new Double[]{0.0, 0.0, 0.0});
                            faceOrientation = Orientation.getOrientation(newOrientationCoord[0].intValue(), newOrientationCoord[1].intValue(), newOrientationCoord[2].intValue());
                        }
                    }

                    if (rotationY != null) {
                        if (!faceOrientation.equals(Orientation.DOWN) && !faceOrientation.equals(Orientation.UP)) {
                            Double[] newOrientationCoord = new Double[]{faceOrientation.getXOffset().doubleValue(), faceOrientation.getYOffset().doubleValue(), faceOrientation.getZOffset().doubleValue()};
                            newOrientationCoord = rotatePoint(newOrientationCoord, rotationY, new Double[]{0.0, 0.0, 0.0});
                            faceOrientation = Orientation.getOrientation(newOrientationCoord[0].intValue(), newOrientationCoord[1].intValue(), newOrientationCoord[2].intValue());
                        }
                    }
                }
            }

            //Get the material of the the face
            String faceMaterial = materialPerOrientation.get(oddAngles ? faceOrientation : (!uvLock || materialPerOrientation.size() == 1) ? Orientation.getOrientation(faceName) : ((rotationX != null && !faceOrientation.equals(Orientation.UP) && !faceOrientation.equals(Orientation.DOWN)) || (rotationY != null && (faceOrientation.equals(Orientation.UP) || faceOrientation.equals(Orientation.DOWN))) ? faceOrientation : Orientation.getOrientation(faceName)));

            //If the face material is null, ignore the face
            //Ex, if uv lock is on, the rotation x is 270, and the cube doesn't have a south face (meaning there is no material on that orientation)
            //The original orientation is up, and after applying the rotation of x 270 on orientation, the face orientation becomes south.
            //And since uv lock is on and the rotation is set to x so the materials per orientation stay the same, even after rotating the face,
            //the new orientation of the uv face, should not have any material on it, so we ignore it
            if(faceMaterial == null)
                continue;

            //Check if added face has any transparent parts
            IMaterial material = Constants.BLOCK_MATERIALS.getMaterial(faceMaterial);
            boolean hasTransparency = ImageUtility.hasAlpha(material, faceUV);
            //If it has, change the material to the transparent variant of the material
            if(hasTransparency){
                //Get the transparent variant name of the material
                String transparentTextureName = String.format("%s_transparent", faceMaterial);
                //Check if the transparent variant doesn't exist yet, and create it by cloning the base material
                if(!Constants.BLOCK_MATERIALS.containsMaterial(transparentTextureName)){
                    Constants.BLOCK_MATERIALS.setMaterial(transparentTextureName, material.duplicate());

                    //Modify the clone of the base material
                    IMaterial transparent_material = Constants.BLOCK_MATERIALS.getMaterial(transparentTextureName);
                    transparent_material.setName(String.format("%s_transparent", transparent_material.getName()));
                    transparent_material.setTransparency(true);
                }

                //Check if cube model doesn't have the transparent variant of the material yet, and create it
                //Add transparent variant of the material to the cube model material, if it does not exist yet
                cubeModel.putMaterial(transparentTextureName);

                //Finally set the face material to the transparent variant of the material
                faceMaterial = transparentTextureName;
            }


            boolean isCullFace = !oddAngles && face.getCullface() != null;
            //Get the face index from the faceOrientation
            Integer faceIndex = faceOrientation.getOrder();

            //Get the corners names for the original face orientation
            String[] cornerNames = getCornerPerOrientation(Orientation.getOrientation(faceName));
            //Create Integer list to store the index vertices of the face
            List<Integer> indexCorners = new ArrayList<>();
            for(String cornerName : cornerNames){
                indexCorners.add(corners.put(cubeCorners.get(cornerName)));
            }

            //Create Integer list to store the index texture coordinates of the face
            List<Integer> indexTextureCoordinates = new ArrayList<>();
            for(Double[] u : faceUV){
                indexTextureCoordinates.add(textureCoordinates.put(u));
            }

            //Mark which material the face uses
            materialFaces[faceIndex] = cubeModel.getMaterials().getIndex(faceMaterial);
            //Mark that the face is generated
            generatedFaces[faceIndex] = true;

            //Create cube face and append it to the cube
            CubeFace cubeFace = new CubeFace(indexCorners, indexTextureCoordinates, faceMaterial, isCullFace);
            cubeFaces[faceIndex] = cubeFace;
        }

        //Create cube from cube faces
        Cube cube = new Cube(materialFaces, generatedFaces, cubeFaces, corners.toList(), textureCoordinates.toList());
        //Append the cube to the cube model
        cubeModel.addCube(cube);
    }

    public static void setUVForPixelCube(List<Double[]> faceUV, Orientation faceOrientation,Integer x, Integer flippedY, Integer imageWidth, Integer imageHeight){
        Double x1 = x.doubleValue() / imageWidth.doubleValue();
        Double x2 = (x.doubleValue() + 1.0) / imageWidth.doubleValue();
        Double y1 = flippedY.doubleValue() / imageHeight.doubleValue();
        Double y2 =  (flippedY.doubleValue() + 1.0) / imageHeight.doubleValue();

        faceUV.add(new Double[]{x1, y1});
        faceUV.add(new Double[]{x1, y2});
        faceUV.add(new Double[]{x2, y2});
        faceUV.add(new Double[]{x2, y1});

        /*if(faceOrientation.equals(Orientation.NORTH) || faceOrientation.equals(Orientation.SOUTH)){
            faceUV.add(new Double[]{x1, y1});
            faceUV.add(new Double[]{x1, y2});
            faceUV.add(new Double[]{x2, y2});
            faceUV.add(new Double[]{x2, y1});
        }else if(faceOrientation.equals(Orientation.WEST)){
            faceUV.add(new Double[]{x1, y1});
            faceUV.add(new Double[]{x1, y2});
            faceUV.add(new Double[]{x1, y2});
            faceUV.add(new Double[]{x1, y1});
        }else if(faceOrientation.equals(Orientation.EAST)){
            faceUV.add(new Double[]{x2, y1});
            faceUV.add(new Double[]{x2, y2});
            faceUV.add(new Double[]{x2, y2});
            faceUV.add(new Double[]{x2, y1});
        }else if(faceOrientation.equals(Orientation.UP)){
            faceUV.add(new Double[]{x1, y2});
            faceUV.add(new Double[]{x1, y2});
            faceUV.add(new Double[]{x2, y2});
            faceUV.add(new Double[]{x2, y2});
        }else {
            faceUV.add(new Double[]{x1, y1});
            faceUV.add(new Double[]{x1, y1});
            faceUV.add(new Double[]{x2, y1});
            faceUV.add(new Double[]{x2, y1});
        }*/

    }

    public static void convertItemIconToCubeModel(ICubeModel itemCubeModel, String iconPath, Namespace itemNamespace){
        try{
            //Get material for icon
            generateOrGetMaterial(iconPath, itemNamespace);
            IMaterial iconMaterial = Constants.BLOCK_MATERIALS.getMaterial(iconPath);

            //Set material of item
            itemCubeModel.putMaterial(iconPath);

            //Get diffuse image from icon material
            BufferedImage icon = iconMaterial.getDefaultDiffuseImage();

            //Calculate cube width & height per pixel
            Double cubeWidth = (16.0 / icon.getWidth()) / 16.0;
            Double cubeHeight = (16.0 / icon.getHeight()) / 16.0;

            //Loop through the image and convert each pixel to a cube
            for(int x = 0; x < icon.getWidth(); x++){
                int flippedX = icon.getWidth() - x - 1;
                for(int y = 0; y < icon.getHeight(); y++){
                    int flippedY = icon.getHeight() - y - 1;

                    int _color = icon.getRGB(x, y);
                    Color color = new Color(_color, true);
                    if(color.getAlpha() != 0){
                        boolean createUpFace = false;
                        boolean createDownFace = false;
                        boolean createWestFace = false;
                        boolean createEastFace = false;

                        //Check for transparent pixel above
                        if(y == 0)
                            createUpFace = true;
                        else
                            createUpFace = (icon.getRGB(x, y - 1) >> 24 & 255) == 0;

                        //Check for transparent pixel bellow
                        if(y + 1 == icon.getHeight())
                            createDownFace = true;
                        else
                            createDownFace = (icon.getRGB(x, y + 1) >> 24 & 255) == 0;

                        //Check for transparent pixel on the left
                        if(x == 0)
                            createEastFace = true;
                        else
                            createEastFace = (icon.getRGB(x - 1, y) >> 24 & 255) == 0;

                        //Check for transparent pixel on the right
                        if(x + 1 == icon.getWidth())
                            createWestFace = true;
                        else
                            createWestFace = (icon.getRGB(x + 1, y) >> 24 & 255) == 0;

                        //Array to store index to material per face (See Orientation.DIRECTIONS for order of faces)
                        Integer[] materialFaces = new Integer[6];
                        //Array to store which faces should be exported (See Orientation.DIRECTIONS for order of faces)
                        Boolean[] generatedFaces = new Boolean[]{createUpFace, createDownFace, true, true, createWestFace, createEastFace};
                        //Array to store cube faces (See Orientation.DIRECTIONS for order of faces)
                        CubeFace[] cubeFaces = new CubeFace[6];

                        //Hashed double list to store all the corners the cube uses
                        HashedDoubleList corners = new HashedDoubleList();
                        //Hashed double list to store all texture coordinates the cube uses
                        HashedDoubleList textureCoordinates = new HashedDoubleList();

                        Set<String> faces = new HashSet<>();
                        faces.add("south");
                        faces.add("north");

                        if(createUpFace)
                            faces.add("up");
                        if(createDownFace)
                            faces.add("down");
                        if(createWestFace)
                            faces.add("west");
                        if(createEastFace)
                            faces.add("east");

                        //Calculate cube position
                        Double[] from = new Double[]{flippedX * cubeWidth, 0.4375, (flippedY * cubeHeight)};
                        Double[] to = new Double[]{(flippedX + 1) * cubeWidth, 0.5, (flippedY + 1) * cubeHeight};

                        //Create vertices for each corner of a face that the cube uses
                        Map<String, Double[]> cubeCorners = createCubeVerticesFromPoints(from, to, faces);

                        for (String faceName : faces) {

                            Orientation faceOrientation = Orientation.getOrientation(faceName);

                            //Get the face uv's, or set them, if It's not defined in the uv field
                            List<Double[]> faceUV = new ArrayList<>();
                            setUVForPixelCube(faceUV, faceOrientation, x, flippedY, icon.getWidth(), icon.getHeight());

                            //Get the face index from the faceOrientation
                            Integer faceIndex = faceOrientation.getOrder();

                            //Get the corners names for the original face orientation
                            String[] cornerNames = getCornerPerOrientation(Orientation.getOrientation(faceName));
                            //Create Integer list to store the indexes of the vertices the face uses
                            List<Integer> indexCorners = new ArrayList<>();
                            for(String cornerName : cornerNames){
                                indexCorners.add(corners.put(ArrayUtility.cloneArray(cubeCorners.get(cornerName))));
                            }

                            //Create Integer list to store the indexes of the texture coordinates the face uses
                            List<Integer> indexTextureCoordinates = new ArrayList<>();
                            for(Double[] u : faceUV)
                                indexTextureCoordinates.add(textureCoordinates.put(u));

                            //Mark which material the face uses
                            materialFaces[faceIndex] = 0;

                            //Create cube face and append it to the cube
                            CubeFace cubeFace = new CubeFace(indexCorners, indexTextureCoordinates, iconPath, false);
                            cubeFaces[faceIndex] = cubeFace;
                        }

                        //Create cube from cube faces
                        Cube cube = new Cube(materialFaces, generatedFaces, cubeFaces, corners.toList(), textureCoordinates.toList());
                        //Append the cube to the cube model
                        itemCubeModel.addCube(cube);
                    }

                }
            }

        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
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
}
