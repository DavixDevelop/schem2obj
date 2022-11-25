package com.davixdevelop.schem2obj;

import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.cubemodels.entity.LavaCubeModel;
import com.davixdevelop.schem2obj.cubemodels.entity.WaterCubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.resourceloader.ResourcePack;
import com.davixdevelop.schem2obj.schematic.Schematic;
import com.davixdevelop.schem2obj.util.LogUtility;
import com.davixdevelop.schem2obj.wavefront.*;
import com.davixdevelop.schem2obj.materials.IMaterial;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SchemeToObj {
    public static void main(String[] arg) {

        Date start = new Date();

        String scheme_path = null;
        String output_path = null;

        boolean exportAllBlock = false;

        String rootFolder = Paths.get(".").toAbsolutePath().normalize().toString();

        Constants.setConstants();

        if(arg.length >= 6) {
            //Get the path to the minecraft folder from the arguments
            if(arg[0].startsWith("-minecraftFolder")){
                String minecraftFolder = arg[1];
                Path minecraftJarPath = Paths.get(minecraftFolder, "versions", "1.12.2", "1.12.2.jar");
                if(Files.exists(minecraftJarPath)){
                    //Register the material, blocks models and block states for the provided 1.12.2.jar
                    String resourcePath = minecraftJarPath.toString();

                    LogUtility.Log("Loading resources from 1.12.2.jar...");
                    if(!ResourceLoader.registerResourcePack(resourcePath, ResourcePack.Format.Vanilla)){
                        LogUtility.Log("Failed to read versions/1.12.2/1.12.2.jar in minecraft folder");
                        return;
                    }
                }
                else {
                    LogUtility.Log("Could not find versions/1.12.2/1.12.2.jar in minecraft folder");
                    return;
                }
            }

            //Get scheme file from arguments
            if(arg[2].startsWith("-i")){
                if(arg[3].endsWith(".schematic") || arg[1].endsWith(".nbt")) {
                    if(arg[3].startsWith(".")) //If filename starts with . It's a relative path -> convert it to absolute
                        scheme_path = Paths.get(rootFolder, arg[3].substring(1)).toString();
                    else
                        scheme_path = arg[3];
                }else{
                    LogUtility.Log("Input scheme doesn't use the .schematic extension");
                    return;
                }
            }else
                return;

            //Get output Wavefront file from arguments
            if(arg[4].startsWith("-o")){
                if(arg[5].endsWith(".obj")){
                    if(arg[5].startsWith(".")) //If filename starts with . It's a relative path -> convert it to absolute
                        output_path = Paths.get(rootFolder, arg[5].substring(1)).toString();
                    else
                        output_path = arg[5];

                }else {
                    LogUtility.Log("Output Wavefront file doesn't end with .obj");
                    return;
                }
            }else
                return;

            //Read additional parameters (ex -allBlocks)
            if(6 < arg.length){
                int nextArgIndex = 6;
                while (nextArgIndex < arg.length){
                    if(arg[nextArgIndex].startsWith("-t")){
                        nextArgIndex += 1;

                        //Check if the user defined what format the resource pack is
                        while(arg[nextArgIndex].startsWith("SEUS:") || arg[nextArgIndex].startsWith("Vanilla:") || arg[nextArgIndex].startsWith("Specular:")){
                            //Get resource pack path
                            String resourcePath = arg[nextArgIndex].substring(arg[nextArgIndex].indexOf(":") + 1);
                            if(resourcePath.startsWith(".")) //Relative path -> convert to absolute
                                resourcePath = Paths.get(rootFolder, resourcePath.substring(1)).toString();
                            //Read the resource pack format (SEUS, Vanilla, Specular)
                            String format = arg[nextArgIndex].substring(0, arg[nextArgIndex].indexOf(":"));

                            if(format.equals("SEUS") || format.equals("Vanilla") || format.equals("Specular")) {

                                LogUtility.Log("Loading resources from: " + resourcePath + " .Please wait.");
                                //Register the material, blocks models and block states the resource pack uses
                                if (!ResourceLoader.registerResourcePack(resourcePath, ResourcePack.Format.fromName(format))) {
                                    LogUtility.Log("Input resource pack isn't valid");
                                    LogUtility.Log("Using default textures instead");
                                }
                            }else
                            {
                                LogUtility.Log("Failed to register resource pack. Incorrect format provided: " + format);
                            }

                            nextArgIndex += 1;

                            if(nextArgIndex >= arg.length)
                                break;
                        }
                    }
                    else if(arg[nextArgIndex].equals("-allBlocks"))
                        exportAllBlock = true;
                    else if(arg[nextArgIndex].equals("-snowy"))
                        Constants.IS_SNOWY = true;
                    nextArgIndex += 1;
                }
            }
        }else
            System.console().writer().println("Add arguments (-i <input schematic file> -t <path to resource pack> -o <output OBJ file>)");

        SchemeToObj s = new SchemeToObj();

        ArrayList<ICubeModel> objects = s.schemeToCubeModels(scheme_path, exportAllBlock);

        if(objects == null || objects.isEmpty()){
            LogUtility.Log("Failed to convert schematic to OBJ");
            return;
        }

        //Write wavefront objects and materials to file
        if(!s.exportToOBJ(objects, output_path)){
            LogUtility.Log("Failed to write wavefront file");
            return;
        }

        Date end = new Date();
        double eclipsed = ((end.getTime()  - start.getTime()) / 1000.0) / 60.0;
        double minutes = Math.floor(eclipsed);
        double seconds = Math.floor((eclipsed - minutes) * 60);

        LogUtility.Log(String.format("Success (Done in: %02d:%02d)", (int) minutes, (int) seconds));


    }

    public ArrayList<ICubeModel> schemeToCubeModels(String schemePath, boolean exportAllBlocks){


        try {
            InputStream schemeInput = new FileInputStream(schemePath);

            try{
                //Read schematic
                Schematic schematic;
                schematic = Schematic.loadSchematic(schemeInput);
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

        int width = Constants.LOADED_SCHEMATIC.getWidth();
        int length = Constants.LOADED_SCHEMATIC.getLength();
        int height = Constants.LOADED_SCHEMATIC.getHeight();

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
                                case "standing_banner":
                                case "wall_banner":
                                case "standing_sign":
                                case "wall_sign":
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

                                int adjacentX = x + faceOrientation.getXOffset();
                                int adjacentZ = z - faceOrientation.getYOffset();
                                int adjacentY = y + faceOrientation.getZOffset();

                                if(adjacentX >= 0 && adjacentX < width &&
                                adjacentZ >= 0 && adjacentZ < length &&
                                adjacentY >= 0 && adjacentY < height){
                                    Integer adjacentIndex = adjacentX + (adjacentY * length + adjacentZ) * width;

                                    ICubeModel adjacentCubeModel = allBlocks.get(adjacentIndex);

                                    if(CubeModelUtility.checkFacing(object, adjacentCubeModel, faceOrientation, oppositeOrientation))
                                        object.deleteFaces(faceOrientation);
                                }
                            }

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

            //Array variable to keep track of how many vertices, texture coordinates and vertex normals were written
            int[] countTracker = new int[]{0,0,0};

            for(ICubeModel cubeModel : cubeModels){
                IWavefrontObject object = WavefrontObjectFactory.fromCubeModel(cubeModel);

                if(object != null && !object.getMaterialFaces().isEmpty()){
                    WavefrontUtility.writeObjectData(object, f, countTracker);
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
                if(textureFiles != null) {
                    for (File textureFile : textureFiles) {
                        textureFile.delete();
                    }
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
