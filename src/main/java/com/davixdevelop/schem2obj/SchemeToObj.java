package com.davixdevelop.schem2obj;

import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.cubemodels.entity.LavaCubeModel;
import com.davixdevelop.schem2obj.cubemodels.entity.WaterCubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.Schematic;
import com.davixdevelop.schem2obj.util.LogUtility;
import com.davixdevelop.schem2obj.wavefront.*;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.materials.json.PackTemplate;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SchemeToObj {
    public static void main(String[] arg) {

        String schem_path = null;
        String output_path = null;

        boolean exportAllBlock = false;

        String rootFolder = Paths.get(".").toAbsolutePath().normalize().toString();

        Constants.setConstants();

        if(arg.length >= 4) {
            //Get scheme file from arguments
            if(arg[0].startsWith("-i")){
                if(arg[1].endsWith(".schematic") || arg[1].endsWith(".nbt")) {
                    if(arg[1].startsWith(".")) //If filename starts with . It's a relative path -> convert it to absolute
                        schem_path = Paths.get(rootFolder, arg[1].substring(1)).toString();
                    else
                        schem_path = arg[1];
                }else{
                    LogUtility.Log("Input scheme doesn't use the .schematic extension");
                    return;
                }
            }else
                return;

            Integer nextArgIndex = 3;

            //Get texture pack path from arguments
            if(arg[2].startsWith("-t")){
                //Repeat until next argument is -o
                while(!arg[nextArgIndex].equals("-o")){

                    //Check if the user defined what format the resource pack is
                    if(arg[nextArgIndex].startsWith("SEUS:") || arg[nextArgIndex].startsWith("Vanilla:") || arg[nextArgIndex].startsWith("Specular:")){
                        //Get resource pack path
                        String resourcePath = arg[nextArgIndex].substring(arg[nextArgIndex].indexOf(":") + 1);
                        if(resourcePath.startsWith(".")) //Relative path -> convert to absolute
                            resourcePath = Paths.get(rootFolder, resourcePath.substring(1)).toString();
                        //Read the resource pack format (SEUS, Vanilla, Specular)
                        String format = arg[nextArgIndex].substring(0, arg[nextArgIndex].indexOf(":"));

                        //Get path to pack.mcmeta
                        Path packPath = Paths.get(resourcePath,"pack.mcmeta");
                        //Check if pack.mcmeta exists
                        if(Files.exists(packPath)){
                            try{
                                //Get input stream for pack.mcmeta
                                InputStream inputStream = new FileInputStream(packPath.toFile().toString());
                                //Get reader for pack.mcmeta
                                Reader reader = new InputStreamReader(inputStream);
                                //Deserialize json
                                PackTemplate packMetaJson = new Gson().fromJson(reader, PackTemplate.class);

                                reader.close();

                                //Check that the format is in the correct format
                                if(packMetaJson.pack.pack_format.intValue() == 3) {

                                    //Register the material, blocks models and block states the resource pack uses
                                    Constants.BLOCK_MATERIALS.registerTexturePack(format, resourcePath);
                                    Constants.BLOCK_MODELS.parseResourcePack(resourcePath);
                                    Constants.BLOCKS_STATES.parseResourcePack(resourcePath);

                                }else
                                    LogUtility.Log(String.format("Incompatible resource pack (Pack format: %d). Using built resource pack instead",packMetaJson.pack.pack_format.intValue()));
                            }catch (Exception ex){
                                LogUtility.Log("Error while reading pack.mcmeta");
                                LogUtility.Log(ex.getMessage());
                                LogUtility.Log("Using built resource pack instead");
                            }

                        }else{
                            LogUtility.Log("Input resource pack isn't valid");
                            LogUtility.Log("Using built resource pack instead");
                        }
                    }

                    nextArgIndex += 1;
                }

            }else
                nextArgIndex = 2;

            //Get output Wavefront file from arguments
            if(arg[nextArgIndex].startsWith("-o")){
                if(arg[nextArgIndex + 1].endsWith(".obj")){
                    if(arg[nextArgIndex + 1].startsWith(".")) //If filename starts with . It's a relative path -> convert it to absolute
                        output_path = Paths.get(rootFolder, arg[nextArgIndex + 1].substring(1)).toString();
                    else
                        output_path = arg[nextArgIndex + 1];

                }else {
                    LogUtility.Log("Output Wavefront file doesn't end with .obj");
                    return;
                }
            }else
                return;

            //Read additional parameters (ex -allBlocks)
            if(nextArgIndex + 2 < arg.length){
                nextArgIndex += 2;
                while (nextArgIndex < arg.length){
                    if(arg[nextArgIndex].equals("-allBlocks"))
                        exportAllBlock = true;
                    else if(arg[nextArgIndex].equals("-snowy"))
                        Constants.IS_SNOWY = true;
                    nextArgIndex += 1;
                }
            }
        }else
            System.console().writer().println("Add arguments (-i <input schematic file> -t <path to resource pack> -o <output OBJ file>)");

        SchemeToObj s = new SchemeToObj();

        ArrayList<ICubeModel> objects = s.schemToCubeModels(schem_path, exportAllBlock);

        if(objects == null || objects.isEmpty()){
            LogUtility.Log("Failed to convert schematic to OBJ");
            return;
        }

        //Write wavefront objects and materials to file
        if(!s.exportToOBJ(objects, output_path)){
            LogUtility.Log("Failed to write wavefront file");
            return;
        }

        LogUtility.Log("Success");


    }

    public ArrayList<ICubeModel> schemToCubeModels(String schemPath, boolean exportAllBlocks){


        try {
            InputStream schemInput = new FileInputStream(schemPath);

            try{
                //Read schematic
                Schematic schematic = null; schematic = Schematic.loadSchematic(schemInput);
                //Load schematic into LOADED_SCHEMATIC
                Constants.LOADED_SCHEMATIC.setSchematic(schematic);
            }
            catch(IOException exception){
                LogUtility.Log("Error while reading schematic");
                LogUtility.Log(exception.getMessage());
                return null;
            }
        }catch (FileNotFoundException exception){
            LogUtility.Log("Could not find specified schematic");
            LogUtility.Log(exception.getMessage());
            return null;
        }

        //The final blocks to export
        ArrayList<ICubeModel> blocks = new ArrayList<>();
        //All blocks (including air -> null)
        HashMap<Integer,ICubeModel> allBlocks = new HashMap<>();

        Integer width = (int) Constants.LOADED_SCHEMATIC.getWidth();
        Integer length = (int) Constants.LOADED_SCHEMATIC.getLength();
        Integer height = (int) Constants.LOADED_SCHEMATIC.getHeight();

        WaterCubeModel waterObject = null;
        LavaCubeModel lavaObject = null;

        for (int x = 0; x < width; x++)  {
            for (int y = 0; y < height; y++) {
                for(int z = 0; z < length; z++) {
                    final int index = x + (y * length + z) * width;

                    //Set the current position of the read block, so other WavefrontObject can check the adjacent blocks
                    Constants.LOADED_SCHEMATIC.setCurrentBlockPosition(x, y, z);

                    Namespace blockNamespace = Constants.LOADED_SCHEMATIC.getNamespace(x, y, z);

                    //ToDo: Write custom blocks (ex, Chest, Sign, Wall Sign...). Until then, ignore these blocks
                    if (blockNamespace == null || blockNamespace.getDomain().equals("builtin")){
                        if(blockNamespace != null) {
                            switch (blockNamespace.getType()){
                                case "flowing_water":
                                case "water":
                                    if (waterObject == null)
                                        waterObject = new WaterCubeModel();

                                    waterObject.addBlock(blockNamespace);
                                    break;
                                case "flowing_lava":
                                case "lava":
                                    if(lavaObject == null)
                                        lavaObject = new LavaCubeModel();

                                    lavaObject.addBlock(blockNamespace);
                                    break;
                                case "bed":
                                    if(Constants.LOADED_SCHEMATIC.getPosZ() == 5 && Constants.LOADED_SCHEMATIC.getPosX() == 7){
                                        String www = "2";
                                    }

                                    //Get  singleton tile entity cube model from memory or create it anew every time
                                    ICubeModel entityCubeModel = Constants.CUBE_MODEL_FACTORY.fromNamespace(
                                            blockNamespace,
                                            Constants.LOADED_SCHEMATIC.getEntityValues(Constants.LOADED_SCHEMATIC.getPosX(), Constants.LOADED_SCHEMATIC.getPosY(), Constants.LOADED_SCHEMATIC.getPosZ()));

                                    //Translate the singleton block to the position of the block in the space
                                    CubeModelUtility.translateCubeModel(entityCubeModel, new Integer[]{x, z, y}, new Integer[]{width,length,height});

                                    //Add it to blocks
                                    if (exportAllBlocks)
                                        blocks.add(entityCubeModel);
                                    else
                                        allBlocks.put(index, entityCubeModel);

                                    continue;
                            }
                        }

                        if (!exportAllBlocks)
                            allBlocks.put(index, null);

                        continue;
                    }

                    //Get  singleton cube model from memory
                    ICubeModel cubeModel = Constants.CUBE_MODEL_FACTORY.fromNamespace(blockNamespace);

                    //Translate the singleton block to the position of the block in the space
                    CubeModelUtility.translateCubeModel(cubeModel, new Integer[]{x, z, y}, new Integer[]{width,length,height});

                    if (exportAllBlocks)
                        blocks.add(cubeModel);
                    else
                        allBlocks.put(index, cubeModel);

                }
            }
        }

        if(!exportAllBlocks){
            HashMap<Double[], Double[]> allNormals = new HashMap<>();

            //If exportAllBlocks all blocks is false, loop through allBlocks from bottom to top and delete hidden faces
            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    for(int z = 0; z < length; z++) {
                        final int index = x + (y * length + z) * width;

                        Constants.LOADED_SCHEMATIC.setCurrentBlockPosition(x, y, z);

                        ICubeModel object = allBlocks.get(index);
                        if(object != null){

                            for(int o = 0; o < 6; o++){
                                Orientation faceOrientation = Orientation.getOrientation(o);

                                Orientation oppositeOrientation = faceOrientation.getOpposite();

                                Integer adjacentX = x + faceOrientation.getXOffset();
                                Integer adjacentZ = z - faceOrientation.getYOffset();
                                Integer adjacentY = y + faceOrientation.getZOffset();

                                if(adjacentX >= 0 && adjacentX < width &&
                                adjacentZ >= 0 && adjacentZ < length &&
                                adjacentY >= 0 && adjacentY < height){
                                    Integer adjacentIndex = adjacentX + (adjacentY * length + adjacentZ) * width;

                                    ICubeModel adjacentCubeModel = allBlocks.get(adjacentIndex);

                                    if(CubeModelUtility.checkFacing(object, adjacentCubeModel, faceOrientation, oppositeOrientation))
                                        object.deleteFaces(faceOrientation);
                                }
                            }
                            /*
                            final Set<String> objectBoundingFaces = object.getBoundingFaces().keySet();

                            if(objectBoundingFaces.isEmpty()) {
                                blocks.add(object);
                                continue;
                            }

                            //West block check
                            if(x > 0)
                                if(CubeModelUtility.checkFacing(objectBoundingFaces, object, allBlocks.get((x - 1) + (y * length + z) * width), "west", "east"))
                                    object.deleteFaces("west");


                            //East block check
                            if(x + 1 < width)
                                if(CubeModelUtility.checkFacing(objectBoundingFaces, object, allBlocks.get((x + 1) + (y* length + z) * width), "east", "west"))
                                    object.deleteFaces("east");


                            //North block check
                            if(z > 0)
                                if(CubeModelUtility.checkFacing(objectBoundingFaces, object, allBlocks.get(x + (y * length + (z - 1)) * width), "north", "south"))
                                    object.deleteFaces("north");


                            //South block check
                            if(z + 1 < length)
                                if(CubeModelUtility.checkFacing(objectBoundingFaces, object, allBlocks.get(x + (y * length + (z + 1)) * width), "south", "north"))
                                    object.deleteFaces("south");


                            //Up block check
                            if(y + 1 < height)
                                if(CubeModelUtility.checkFacing(objectBoundingFaces, object, allBlocks.get(x + ((y + 1) * length + z) * width), "up", "down"))
                                    object.deleteFaces("up");


                            //Down block check
                            if(y > 0)
                                if(CubeModelUtility.checkFacing(objectBoundingFaces, object, allBlocks.get(x + ((y - 1) * length + z) * width), "down", "up"))
                                    object.deleteFaces("down");

                            if(!object.getMaterialFaces().isEmpty()) {
                                //Reset the normals for the object to reflect the deleted faces and add them to all normals
                                WavefrontUtility.resetNormals(allNormals, object);
                            }*/

                            blocks.add(object);
                        }
                    }
                }
            }
            /*
            //Normalize allNormals
            WavefrontUtility.normalizeNormals(allNormals);

            //Copy allNormals back to each object
            for(int c = 0; c < blocks.size(); c++){
                IWavefrontObject object = blocks.get(c);
                WavefrontUtility.copyAllNormalsToObject(allNormals, object);
                blocks.set(c, object);
            }*/


        }

        if(waterObject != null){
            waterObject.finalizeCubeModel();
            blocks.add(waterObject);
        }

        if(lavaObject != null){
            lavaObject.finalizeCubeModel();
            blocks.add(lavaObject);
        }


        //ToDo: Convert items (mob heads, chests...) to wavefront

        return blocks;
    }

    public boolean exportToOBJ(ArrayList<ICubeModel> cubeModels, String outputPath){
        Path output_path = Paths.get(outputPath);

        String fileName = output_path.toFile().getName().replace(".obj","");
        try{
            OutputStream outputStream = new FileOutputStream(output_path.toFile().getAbsolutePath());

            //Write wavefront objects to output file
            PrintWriter f = new PrintWriter(outputStream){
                @Override
                public void println() {
                    write('\n');
                }
            };
            //Specify which material library to use
            f.println(String.format("mtllib %s.mtl", fileName));

            //Array variable to keep track of how many vertices, texture coordinates and vertex normals were writen
            int[] countTracker = new int[]{0,0,0};

            for(ICubeModel cubeModel : cubeModels){
                IWavefrontObject object = WavefrontObjectFactory.fromCubeModel(cubeModel);

                if(object != null && !object.getMaterialFaces().isEmpty()){
                    countTracker = WavefrontUtility.writeObjectData(object, f, countTracker);
                }
            }

            //Flush and close output stream
            f.flush();
            f.close();

        }catch (FileNotFoundException ex){
            LogUtility.Log("Could not create output file:");
            LogUtility.Log(ex.getMessage());
            return false;
        }

        try{
            //Set the folder for the textures
            Path textureFolderOutPath = Paths.get(output_path.toFile().getParent(), fileName);
            if(!textureFolderOutPath.toFile().exists())
                //Crete folder with name of output file if it doesn't exist yet
                textureFolderOutPath.toFile().mkdir();
            else{
                //Else delete the files inside the output texture folder
                File[] textureFiles = textureFolderOutPath.toFile().listFiles();
                for(File textureFile : textureFiles){
                    textureFile.delete();
                }
            }

            Path materialFile = Paths.get(output_path.toFile().getParent(), String.format("%s.mtl", fileName));
            OutputStream outputStream = new FileOutputStream(materialFile.toFile());

            PrintWriter f = new PrintWriter(outputStream){
                @Override
                public void println() {
                    write('\n');
                }
            };

            //Texture path is the same folder as the output path in the folder of the same name as the object file
            String textureFileOutPath = Paths.get(output_path.toFile().getParent(), fileName).toFile().toString();

            for(String materialName : Constants.BLOCK_MATERIALS.usedMaterials()){
                IMaterial material = Constants.BLOCK_MATERIALS.getMaterial(materialName);

                //Get the material lines
                ArrayList<String> materialLines = material.toMTL(textureFileOutPath);

                //Write material to file
                for(String line : materialLines)
                    f.println(line);

                //If material is lit and it emits light, set the material name to lit_ + material name
                /*if(material.isLit() || material.getLightValue() > 0.0)
                    materialName = "lit_" + materialName;*/
            }

            f.flush();
            f.close();

        }catch (Exception ex){
            LogUtility.Log("Could not create material file");
            LogUtility.Log(ex.getMessage());
            return false;
        }

        //ToDo: Download players head's textures if there are any custom mob heads

        return true;
    }
}
