package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.models.Hashed3KeyList;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.utilities.ArrayVector;
import com.davixdevelop.schem2obj.wavefront.material.IMaterial;
import com.davixdevelop.schem2obj.wavefront.material.Material;

import java.io.PrintWriter;
import java.util.*;

public class WavefrontUtility {

    public static final Double[] BLOCK_ORIGIN = new Double[] {0.5,0.5,0.5};

    /**
     * Extract default materials from models and return a map with texture variables
     * and names of the material
     * @param models An array of models
     * @return map of key: texture variable, value: name of default material
     */
    public static HashMap<String, String> texturesToMaterials(BlockModel[] models, Namespace blockNamespace) {
        HashMap<String, String> textureMaterials = new HashMap<>();

        for (BlockModel model : models) {
            HashMap<String, String> modelTextures = model.getTextures().getTextures();

            //Get textures from block
            for (String key : modelTextures.keySet()) {
                String value = modelTextures.get(key);

                //Check if texture value doesn't have a variable (raw value, ex. block/dirt)
                if (!value.startsWith("#")) {

                    String materialName = value;

                    if (WavefrontCollection.BLOCK_MATERIALS.containsMaterial(materialName)) {
                        if(!WavefrontCollection.BLOCK_MATERIALS.usedMaterials().contains(materialName)){
                            //If material isn't yet used, but It's in BLOCK_MATERIALS collection, it means It's a custom material, added from a resource pack
                            //Modify the material to include the lightValue of the block
                            IMaterial material = WavefrontCollection.BLOCK_MATERIALS.getMaterial(materialName);
                            material.setLightValue(blockNamespace.getLightValue());
                            WavefrontCollection.BLOCK_MATERIALS.setMaterial(materialName, material);
                        }
                        textureMaterials.put(key, materialName);
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

                        material.setTexture(value);
                        material.setTextureName(textureName(value));
                        material.setLightValue(blockNamespace.getLightValue());
                        material.setName(materialName);

                        WavefrontCollection.BLOCK_MATERIALS.setMaterial(materialName, material);

                        if(!textureMaterials.containsKey(key))
                            textureMaterials.put(key, materialName);
                    }

                } else {
                    //Check if texture materials already contains a key with the same variable name (removed # from the value)
                    if (textureMaterials.containsKey(value.substring(1))) {
                        //If it does, place the key with the actual value of the variable (ex. #all -> block/dirt)
                        textureMaterials.put(key, textureMaterials.get(value.substring(1)));
                    }
                }

            }
        }

        return textureMaterials;
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
    public static ArrayList<Double[]> setAndRotateUVFace(CubeElement.CubeFace face, Double[] from, Double[] to){
        ArrayList<Double[]> UVFace = new ArrayList<>();
        if(face.getUv() == null){
            UVFace.add(new Double[]{from[0], to[1]}); //3
            UVFace.add(new Double[]{from[0], from[1]}); //0
            UVFace.add(new Double[]{to[0], from[1]}); //1
            UVFace.add(new Double[]{to[0], to[1]}); //2

        }else{
            //MC face uv is a 4 item array [x1, y1, x2, y2]
            Double[] rawUV = face.getUv();
            UVFace.add(new Double[]{rawUV[0], rawUV[1]});
            UVFace.add(new Double[]{rawUV[2], rawUV[0]});
            UVFace.add(new Double[]{rawUV[2], rawUV[3]});
            UVFace.add(new Double[]{rawUV[0], rawUV[3]});
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
        ArrayVector.MatrixRotation uvRotation = new ArrayVector.MatrixRotation(Math.toRadians(angle),"Z");
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
        Double translateZ = position[2] - (spaceSize[2].doubleValue() / 2);
        Double[] translate = new Double[]{translateX, translateY, translateZ};

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
            point[c] = (double) Math.round(point[c]);

        return point;
    }

    public static ArrayList<Double[]> getFaceVertices(Hashed3KeyList<Double> vertices, ArrayList<Integer[]> face){
        ArrayList<Double[]> faceVertices = new ArrayList<>();
        for(Integer[] faceIndices : face){
             faceVertices.add(vertices.get(faceIndices[0]));
        }

        return faceVertices;
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
     * @param parentObject The parent object to check
     * @param face1 //The name of the face on the object to check
     * @param face2 //The name of the face on the parent object to check
     * @return True if the two faces are connected, else false
     */
    public static boolean checkFaceing(Set<String> objectBoundingBox, IWavefrontObject parentObject, String face1, String face2){
        if(parentObject == null)
            return false;

        if(objectBoundingBox.contains(face1)){
            Set<String> parentObjectBoundingBox = parentObject.getBoundingFaces().keySet();
            if(parentObjectBoundingBox.contains(face2))
                return true;
        }
        return false;
    }

    public static void writeObjectData(IWavefrontObject object, PrintWriter f){
        //Specify new object
        f.println(String.format("o %s", object.getName()));

        ArrayList<Double[]> vertices = object.getVertices();
        //Write all vertices
        for(Double[] v : vertices){
            f.println(String.format("v %f %f %f", v[0], v[2], -v[1]));
        }

        ArrayList<Double[]> uvs = object.getTextureCoordinates();
        //Write all texture coordinates
        for(Double[] vt : uvs){
            f.println(String.format("vt %f %f", vt[0], vt[1]));
        }

        ArrayList<Double[]> vertNormals = object.getVertexNormals();
        //Write all vertex normals
        for(Double[] vn : vertNormals){
            f.println(String.format("vn %f %f %f", vn[0], vn[1], vn[2]));
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
                    //Each index is negative, so it uses the the newest added vertices...
                    //Example, and index of -1 uses the last added vertex
                    //As the vertices/uv/normals list and faces are order in the same way (from 0 on forward), to get index to the last added
                    //vertex's, we need to decrease the indices index of v and vn by 8 (8 vertex in a cube),
                    // and we need to increase vt index by 1 and multiply it by -1
                    faceEntry += String.format(" %d/%d/%d", indices[0] - 8, (indices[1] + 1) * -1, indices[2] - 8);
                }
                f.println(faceEntry);
            }

        }
    }

}
