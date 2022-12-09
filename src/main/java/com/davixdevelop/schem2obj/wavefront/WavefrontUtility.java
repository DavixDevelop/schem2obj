package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.models.HashedStringList;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;

public class WavefrontUtility {


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
                //ex: A = vertex of face (vertices.get(materialFace.get(0 <- first indic)[0 <- first element in indic is the vertex index]))
                //↑A = first 3 items of A (splitArray(A))
                //Get 3 vertices
                Double[] av = vertices.get(materialFace.get(0)[0]);
                Double[] bv = vertices.get(materialFace.get(1)[0]);
                Double[] cv = vertices.get(materialFace.get(2)[0]);
                Double[] face_normal = ArrayVector.multiply(ArrayVector.subtract(bv, av), ArrayVector.subtract(cv, av));

                for (int x = 0; x < 4; x++) {
                    //Get vertex and vertex normal pair for each vert in face
                    Integer vertexIndex = materialFace.get(x)[0];

                    Double[] vn = normalsArray.get(vertexIndex);
                    //If the vn is null, set it to face_normal
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
     * Appends the object vertex normals to all normals, which is a map of vertices and It's vertex normals
     * @param allNormals Map<key: vertex, value:vertex normals>
     * @param object The object of which vertex normals to add to all normals
     */
    public static void resetNormals(HashMap<Double[], Double[]> allNormals, IWavefrontObject object){
        ArrayList<Double[]> normals = object.getVertexNormals();
        ArrayList<Double[]> vertices = object.getVertices();
        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = object.getMaterialFaces();

        //Normalize the cube/cubes
        for (String materialName : faces.keySet()) {
            //Get the faces that the texture uses
            ArrayList<ArrayList<Integer[]>> materialFaces = faces.get(materialName);
            for (ArrayList<Integer[]> materialFace : materialFaces) {

                if(materialFace == null)
                    continue;

                //Calculate face normal (↑B - ↑A) × (↑C - ↑A)
                //ex: A = vertex of face (vertices.get(materialFace.get(0 <- first indic)[0 <- first element in indic is the vertex index]))
                //↑A = first 3 items of A (splitArray(A))
                //Get 3 vertices
                Double[] av = vertices.get(materialFace.get(0)[0]);
                Double[] bv = vertices.get(materialFace.get(1)[0]);
                Double[] cv = vertices.get(materialFace.get(2)[0]);
                Double[] face_normal = ArrayVector.multiply(ArrayVector.subtract(bv, av), ArrayVector.subtract(cv, av));

                for (int x = 0; x < 4; x++) {
                    //Get vertex and vertex normal pair for each vert in face
                    Integer vertexIndex = materialFace.get(x)[0];

                    Double[] vn = null;
                    Double[] v = vertices.get(vertexIndex);

                    //If the vn is null, set it to face_normal
                    if (!allNormals.containsKey(v)) {
                        vn = face_normal;
                    } else {
                        vn = allNormals.get(v);
                        //Add face_normal to allNormal
                        vn = ArrayVector.add(vn, face_normal);
                    }

                    //Set vn to allNormals
                    allNormals.put(v, vn);
                }
            }
        }
    }

    /**
     * Normalize all the normals of all vertices
     * @param allNormals Map<key: vertex, value:vertex normals>
     */
    public static void normalizeNormals(HashMap<Double[], Double[]> allNormals){
        //Normalize the normals
        //Loop through all vertex normals for all vertices and normalize it
        for(Double[] vert : allNormals.keySet()){
            Double[] vn = allNormals.get(vert);
            vn = ArrayVector.normalize(vn);
            allNormals.put(vert, vn);
        }
    }

    public static void copyAllNormalsToObject(HashMap<Double[], Double[]> allNormals, IWavefrontObject object){
        ArrayList<Double[]> normals = object.getVertexNormals();
        ArrayList<Double[]> vertices = object.getVertices();

        for(int vertIndex = 0; vertIndex < vertices.size(); vertIndex++){
            Double[] vert = vertices.get(vertIndex);
            if(allNormals.containsKey(vert))
                normals.set(vertIndex,allNormals.get(vert));
        }
    }

    /**
     * Write the Wavefront OBJ to the print writer, and return an array that keeps count of all written
     * vertices/uv's/vertex normals
     * @param object The wavefront object to write the data
     * @param f The PrintWriter to write the data to
     * @param countTracker A 3 length integer array to keep count of all written vertices/uv's/vertex normals
     */
    public static void writeObjectData(IWavefrontObject object, PrintWriter f, int[] countTracker){
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
        /*for(Double[] vn : vertNormals){
            f.println(String.format(Locale.ROOT, "vn %f %f %f", vn[0], vn[1], vn[2]));
        }*/




        //key: materialName (ex. texture:blocks/dirt), value: list of faces
        HashMap<String, ArrayList<ArrayList<Integer[]>>>  materialFaces =  object.getMaterialFaces();
        for(String materialName : materialFaces.keySet()){
            //Specify which material to use
            f.println(String.format("usemtl %s", CubeModelUtility.textureName(materialName)));

            ArrayList<ArrayList<Integer[]>> faces = materialFaces.get(materialName);
            //Write all faces
            for (ArrayList<Integer[]> face : faces) {
                if(face == null)
                    continue;

                StringBuilder faceEntry = new StringBuilder("f");
                for (int x = face.size() - 1; x >= 0; x--) {
                    Integer[] indices = face.get(x);
                    //Format: vert index/texture coordinate/vert normal index
                    //Each index is calculated based on the sum of written vertices/uv/vertex normals + 1 (as in the Wavefront OBJ format indexes start with 1) + local index (ex 0)
                    faceEntry.append(String.format(" %d/%d/%d", countTracker[0] + 1 + indices[0], countTracker[1] + 1 + indices[1], countTracker[2] + 1 + indices[2]));
                }
                f.println(faceEntry);
            }

        }

        //Update size of written vertices on countTracker
        countTracker[0] += vertices.size();

        //Update size of written vertex normals on countTracker
        //countTracker[2] += vertices.size();

        //Update size of written texture coordinates on countTracker
       countTracker[1] += uvs.size();

    }

    /*//Create normals for object
        WavefrontUtility.createNormals(normalsArray, vertices, faces);

    //Get vertex list
    ArrayList<Double[]> verticesArray = vertices.toList();

    //Normalize vertex normals
        WavefrontUtility.normalizeNormals(normalsArray);

    setVertices(verticesArray);
    setVertexNormals(normalsArray);
    setTextureCoordinates(textureCoordinates.toList());
    setMaterialFaces(faces);
    setBoundingFaces(boundingFaces);*/

    public static boolean mergeOBJ(Path obj_file, Path out_file){
        try{
            Scanner scanner = new Scanner(obj_file.toFile());

            //Create hashed double list for vertices
            HashedStringList vertices = new HashedStringList();
            //Create hashed double list for texture coordinates
            HashedStringList uvs = new HashedStringList();

            //Read through the obj file, and get all vertices and textures coordinates
            while (scanner.hasNext()){
                String line = scanner.nextLine();
                if(line.startsWith("v ")){
                    vertices.put(line.substring(2));
                }else if(line.startsWith("vt ")){
                    uvs.put(line.substring(3));
                }
            }

            scanner.close();

            //Create print writer for outputfile
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(out_file.toFile().getAbsolutePath())), false){
                @Override
                public void println() {
                    write('\n');
                }
            };
            String file_name = out_file.toFile().getName().replace(".obj","");
            writer.println(String.format("mtllib %s.mtl", file_name));
            writer.println(String.format("o %s", file_name));

            //Write all vertices to output file
            List<String> rawVertices = vertices.toList();
            for(String vertex : rawVertices){
                writer.println(String.format("v %s", vertex));
            }

            //Write all texture coordinates to output file
            List<String> rawUVS = uvs.toList();
            for(String uv : rawUVS){
                writer.println(String.format("vt %s", uv));
            }

            //Loop through the obj file, and write It's faces to the output file

            //Keep count of read vertices and texture coordinates
            int[] countTracker = new int[]{0, 0};

            List<String> objectVertices = new ArrayList<>();
            List<String> objectTextureCoordinates = new ArrayList<>();

            Scanner scanner2 = new Scanner(obj_file.toFile());

            while(scanner2.hasNext()){
                String line = scanner2.nextLine();
                if(line.startsWith("o ")) {
                    countTracker[0] += objectVertices.size();
                    countTracker[1] += objectTextureCoordinates.size();

                    objectVertices.clear();
                    objectTextureCoordinates.clear();

                }
                else if(line.startsWith("v ")){
                    objectVertices.add(line.substring(2));
                }else if(line.startsWith("vt ")){
                    objectTextureCoordinates.add(line.substring(3));
                }else if(line.startsWith("usemtl "))
                    writer.println(line);
                else if(line.startsWith("f ")){
                    line = line.substring(2);
                    //Split line by space to get the face indices
                    String[] indices = line.split(" ");
                    StringBuilder newFace = new StringBuilder();
                    newFace.append("f");
                    for(String ind : indices){
                        //Split the ind by / to get the indexes
                        String[] indicesIndex = ind.split("/");
                        //Get the vertex
                        String vert = objectVertices.get(Integer.parseInt(indicesIndex[0]) - countTracker[0] - 1);
                        //Get the texture coordinate
                        String uv = objectTextureCoordinates.get(Integer.parseInt(indicesIndex[1]) - countTracker[1] - 1);
                        //Get index to vertex in output file
                        Integer newVertexIndex = vertices.getIndex(vert) + 1;
                        //Get index to texture coordinate in output file
                        Integer newUVIndex = uvs.getIndex(uv) + 1;

                        //Append new indices to face
                        newFace.append(String.format(" %d/%d", newVertexIndex, newUVIndex));
                    }

                    //Write face to output file
                    writer.println(newFace);
                }
            }

            scanner2.close();
            writer.flush();
            writer.close();

            obj_file.toFile().delete();

            return true;

        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
            obj_file.toFile().delete();
            return false;
        }
    }

}
