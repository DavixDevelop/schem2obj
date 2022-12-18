package com.davixdevelop.schem2obj;

import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.cubemodels.entity.EntityCubeModel;
import com.davixdevelop.schem2obj.cubemodels.entitytile.LavaCubeModel;
import com.davixdevelop.schem2obj.cubemodels.entitytile.WaterCubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.resourceloader.ResourcePack;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.schematic.Schematic;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.LogUtility;
import com.davixdevelop.schem2obj.wavefront.*;
import com.davixdevelop.schem2obj.materials.IMaterial;
import org.apache.commons.cli.*;

import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class SchemeToObj {
    public static void main(String[] args) {
        Date start = new Date();

        Constants.setConstants();

        Options options = new Options();

        Option minecraftFolderOption = Option.builder("m").longOpt("minecraftFolder").argName("Minecraft Folder").desc("Your .minecraft folder, which must contain versions/1.12.2/1.12.2.jar.").required().hasArg().numberOfArgs(1).build();
        options.addOption(minecraftFolderOption);
        Option inputOption = Option.builder("i").longOpt("input").argName("Input Schematic File").desc("The schematic/nbt input file.").required().hasArg().numberOfArgs(1).build();
        options.addOption(inputOption);
        Option outputOption = Option.builder("o").longOpt("output").argName("Output Schematic File").desc("The object file to output to.").required().hasArg().numberOfArgs(1).build();
        options.addOption(outputOption);

        Option exportAllBlocksOption = Option.builder("e").longOpt("allBlocks").argName("Export All Blocks").desc("Exports all blocks, including hidden blocks").optionalArg(true).hasArg(false).build();
        options.addOption(exportAllBlocksOption);
        Option isSnowyOption = Option.builder("s").longOpt("snowy").argName("Snowy").desc("Only generates snowy grass for now").optionalArg(true).hasArg(false).build();
        options.addOption(isSnowyOption);
        Option christmasChestsOption = Option.builder("c").longOpt("christmasChests").argName("Christmas Chests").desc("Uses the Christmas texture for the chests (except Enders chest)").optionalArg(true).hasArg(false).build();
        options.addOption(christmasChestsOption);

        Option resourcePacksOption = Option.builder("t").longOpt("resourcePacks").argName("List of resource packs (SEUS:<path> or Vanilla:<path>)").optionalArg(true).hasArg().valueSeparator(' ').build();
        options.addOption(resourcePacksOption);

        if (sendUsageInfoIfRequested(args, options)) return;

        DefaultParser parser;

        parser = DefaultParser.builder().setAllowPartialMatching(false).setStripLeadingAndTrailingQuotes(true).build();
        CommandLine parsed;
        try {
            parsed = parser.parse(options, args);
        } catch (ParseException e) {
            LogUtility.Log(String.format("Failed to parse input arguments: %s\nRun with --help to display usage", e.getMessage()));
            return;
        }

        String minecraftFolder = parsed.getOptionValue(minecraftFolderOption);
        String input = parsed.getOptionValue(inputOption);
        String output = parsed.getOptionValue(outputOption);

        //Get the path to the minecraft folder from the arguments
        Path minecraftJarPath = Paths.get(minecraftFolder, "versions", "1.12.2", "1.12.2.jar");
        if (Files.exists(minecraftJarPath)) {
            //Register the material, blocks models and block states for the provided 1.12.2.jar
            String resourcePath = minecraftJarPath.toString();

            LogUtility.Log("Loading resources from 1.12.2.jar...");
            if (!ResourceLoader.registerResourcePack(resourcePath, ResourcePack.Format.Vanilla)) {
                LogUtility.Log("Failed to read versions/1.12.2/1.12.2.jar in minecraft folder");
                return;
            }
        } else {
            LogUtility.Log("Could not find versions/1.12.2/1.12.2.jar in minecraft folder");
            return;
        }

        //Get scheme file from arguments
        String scheme_path;
        if (input.endsWith(".schematic") || input.endsWith(".nbt")) {
            scheme_path = Paths.get(input).toAbsolutePath().toString();
        } else {
            LogUtility.Log("Input scheme doesn't use the .schematic extension");
            return;
        }

        //Get output Wavefront file from arguments
        String output_path;
        if(output.endsWith(".obj")) {
            output_path = Paths.get(output).toAbsolutePath().toString();
        } else {
            LogUtility.Log("Output Wavefront file doesn't end with .obj");
            return;
        }

        Constants.EXPORT_ALL_BLOCKS = parsed.hasOption(exportAllBlocksOption);
        Constants.IS_SNOWY = parsed.hasOption(isSnowyOption);
        Constants.CHRISTMAS_CHEST = parsed.hasOption(christmasChestsOption);

        if(parsed.hasOption(resourcePacksOption)) {
            String[] resourcePacks = parsed.getOptionValues(resourcePacksOption);
            //Read resource pack
            for(String resourcePack : resourcePacks){
                //Check if the user defined what format the resource pack is
                if (resourcePack.startsWith("SEUS:") || resourcePack.startsWith("Vanilla:") || resourcePack.startsWith("Specular:")) {
                    //Get resource pack path
                    String resourcePath = Paths.get(resourcePack.substring(resourcePack.indexOf(":") + 1)).toAbsolutePath().toString();
                    //Read the resource pack format (SEUS, Vanilla, Specular)
                    String format = resourcePack.substring(0, resourcePack.indexOf(":"));

                    if (format.equals("SEUS") || format.equals("Vanilla") || format.equals("Specular")) {

                        LogUtility.Log("Loading resources from: " + resourcePath + " .Please wait.");
                        //Register the material, blocks models and block states the resource pack uses
                        if (!ResourceLoader.registerResourcePack(resourcePath, ResourcePack.Format.fromName(format))) {
                            LogUtility.Log("Input resource pack isn't valid");
                            LogUtility.Log("Using default textures instead");
                        }
                    } else {
                        LogUtility.Log("Failed to register resource pack. Incorrect format provided: " + format);
                    }
                }
            }
        }

        SchemeToObj s = new SchemeToObj();

        //ArrayList<ICubeModel> objects = s.schemeToCubeModels(scheme_path, exportAllBlock);

        if(!s.exportScheme(scheme_path, output_path, Constants.EXPORT_ALL_BLOCKS)){
            LogUtility.Log("Failed to convert schematic to OBJ");
            return;
        }

        Date end = new Date();
        double eclipsed = ((end.getTime()  - start.getTime()) / 1000.0) / 60.0;
        double minutes = Math.floor(eclipsed);
        double seconds = Math.floor((eclipsed - minutes) * 60);

        LogUtility.Log(String.format("Success (Done in: %02d:%02d)", (int) minutes, (int) seconds));
    }

    public boolean exportScheme(String schemePath, String outPath, boolean exportAllBlocks){
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
                return false;
            }
        }catch (FileNotFoundException exception){
            LogUtility.Log("Could not find specified schematic");
            LogUtility.Log(exception.getMessage());
            return false;
        }

        Path output_path = Paths.get(outPath);
        String fileName = output_path.toFile().getName().replace(".obj","");
        PrintWriter f = null;

        Path obj_file = Paths.get(output_path.toFile().toString());

        if(!exportAllBlocks)
        {
            try {
                String tempFileName = obj_file.toFile().getName();
                tempFileName = tempFileName.replace(".obj","");
                tempFileName = String.format("%s_temp.obj", tempFileName);
                obj_file = Paths.get(output_path.toFile().getParent(), tempFileName);
            }catch (Exception ex){
                LogUtility.Log(ex.getMessage());
                return false;
            }
        }

        try{
            //Write wavefront objects to output file
            f = new PrintWriter(new BufferedWriter(new FileWriter(obj_file.toFile().getAbsolutePath())), false){
                @Override
                public void println() {
                    write('\n');
                }
            };
            //Specify which material library to use
            f.println(String.format("mtllib %s.mtl", fileName));
            f.flush();
        }catch (Exception ex){
            LogUtility.Log("Could not create output file:");
            LogUtility.Log(ex.getMessage());
            return false;
        }

        //Queue of processed cube models to be exported to obj
        ConcurrentLinkedQueue<ICubeModel> processedCubesModels = new ConcurrentLinkedQueue<>();

        boolean[] writerError = {false};

        PrintWriter finalF = f;
        final Boolean[] processingBlocks = new Boolean[]{true};
        Thread writerTask = new Thread(() -> {
            //Array variable to keep track of how many vertices, texture coordinates and vertex normals were written
            int[] countTracker = new int[]{0,0,0};

            int counter = 0;
            //int totalBlocks = (Constants.LOADED_SCHEMATIC.getLength() * Constants.LOADED_SCHEMATIC.getWidth() * Constants.LOADED_SCHEMATIC.getHeight()) + Constants.LOADED_SCHEMATIC.getEntitiesCount();

            while(processingBlocks[0] || !processedCubesModels.isEmpty()) { //|| !executorService.isTerminated()){
                if(!processedCubesModels.isEmpty()){
                    ICubeModel cubeModel = processedCubesModels.poll();
                    if(!exportToOBJ(countTracker, cubeModel, finalF)) {
                        writerError[0] = true;
                        return;
                    }
                    //LogUtility.Log(String.format("Written: %d", counter));
                    counter += 1;
                    LogUtility.InlineLog(String.format("Converted: %d blocks \r", counter));
                }
            }
            //Flush and close output stream
            finalF.flush();
            finalF.close();
        });

        int width = Constants.LOADED_SCHEMATIC.getWidth();
        int length = Constants.LOADED_SCHEMATIC.getLength();
        int height = Constants.LOADED_SCHEMATIC.getHeight();

        WaterCubeModel waterObject = null;
        LavaCubeModel lavaObject = null;

        Map<Integer, SoftReference<Map<?, ?>>> singletonBlockIndex = new ConcurrentHashMap<>();

        LogUtility.Log("Generating blocks");
        //Create singleton cube models
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    final int index = x + (y * length + z) * width;

                    if(!Constants.LOADED_SCHEMATIC.isAirOrLiquid(index)){
                        Namespace namespace = Constants.LOADED_SCHEMATIC.getNamespace(x, y, z);
                        //ToDo: Remove the builtin domain
                        if(namespace.getDomain().equals("builtin") && Constants.SupportedEntities.contains(namespace.getType())){
                            namespace.setCustomData(Constants.LOADED_SCHEMATIC.getEntityValues(x, y, z));
                        }else if(namespace.getDomain().equals("builtin"))
                            continue;

                        singletonBlockIndex.put(index, new SoftReference<>(Constants.CUBE_MODEL_FACTORY.getKey(namespace)));
                    }
                }
            }
        }

        LogUtility.Log("Converting blocks");
        LogUtility.Log("");

        writerTask.start();
        for (int x = 0; x < width; x++)  {
            for (int y = 0; y < height; y++) {
                for(int z = 0; z < length; z++) {
                    final int index = x + (y * length + z) * width;

                    if(Constants.LOADED_SCHEMATIC.isLiquid(index)){
                        Namespace namespace = Constants.LOADED_SCHEMATIC.getNamespace(x, y, z);

                        if(namespace != null && namespace.getDomain().equals("builtin")) {
                            switch (namespace.getType()) {
                                case "flowing_water":
                                case "water":
                                    if (waterObject == null)
                                        waterObject = new WaterCubeModel();

                                    if (z == 15 && y == 4) {
                                        String w = "2";
                                    }

                                    waterObject.addBlock(namespace);
                                    break;
                                case "flowing_lava":
                                case "lava":
                                    if (lavaObject == null)
                                        lavaObject = new LavaCubeModel();

                                    lavaObject.addBlock(namespace);
                                    break;
                            }
                        }
                    }

                    if(Constants.LOADED_SCHEMATIC.isAirOrLiquid(index))
                        continue;

                    try{
                        if(!singletonBlockIndex.containsKey(index))
                            continue;

                        Map<?, ?> key = singletonBlockIndex.get(index).get();

                        if(key != null){
                            //Get copy of singleton
                            ICubeModel singletonCubeModel = Constants.CUBE_MODEL_FACTORY.fromKey(key);

                            if(singletonCubeModel != null){
                                singletonCubeModel = singletonCubeModel.duplicate();

                                if(!exportAllBlocks){
                                    //Check each face for cull-faces, and delete hidden faces
                                    for(int o = 0; o < 6; o++){
                                        //Get the orientation of the face
                                        Orientation faceOrientation = Orientation.getOrientation(o);
                                        //Get the opposite direction
                                        Orientation oppositeOrientation = faceOrientation.getOpposite();

                                        //Calculate the key index to the adjacent block
                                        int adjacentX = x + faceOrientation.getXOffset();
                                        int adjacentZ = z - faceOrientation.getYOffset();
                                        int adjacentY = y + faceOrientation.getZOffset();

                                        //Check if adjacent block is withing bounds
                                        if (adjacentX >= 0 && adjacentX < width &&
                                                adjacentZ >= 0 && adjacentZ < length &&
                                                adjacentY >= 0 && adjacentY < height) {
                                            int adjacentKey = adjacentX + (adjacentY * length + adjacentZ) * width;

                                            //If adjacent block is air or liquid ignore it
                                            if(Constants.LOADED_SCHEMATIC.isAirOrLiquid(adjacentKey))
                                                continue;

                                            if(!singletonBlockIndex.containsKey(adjacentKey))
                                                continue;

                                            Map<?, ?> adjacentSingletonKey = singletonBlockIndex.get(adjacentKey).get();
                                            if(adjacentSingletonKey == null)
                                                continue;

                                            //Get reference to the singleton from the adjacent block
                                            ICubeModel adjacentSingletonCubeModel = Constants.CUBE_MODEL_FACTORY.fromKey(adjacentSingletonKey);

                                            if(adjacentSingletonCubeModel == null)
                                                continue;

                                            //Perform the check on the cube model
                                            if (CubeModelUtility.checkFacing(singletonCubeModel, adjacentSingletonCubeModel, faceOrientation, oppositeOrientation))
                                                singletonCubeModel.deleteFaces(faceOrientation);
                                        }
                                    }
                                }

                                //Translate the copy of the singleton block to the position of the block in the space
                                CubeModelUtility.translateCubeModel(singletonCubeModel, new Double[]{(double)x, (double) z, (double) y}, new Integer[]{(int) width, (int) length, (int) height});

                                processedCubesModels.add(singletonCubeModel);
                            }
                        }

                    }catch (Exception ex){
                        LogUtility.Log(ex.getMessage());
                    }
                }
            }
        }

        if(waterObject != null){
            waterObject.finalizeCubeModel();
            processedCubesModels.add(waterObject);
        }

        if(lavaObject != null){
            lavaObject.finalizeCubeModel();
            processedCubesModels.add(lavaObject);
        }

        if(Constants.LOADED_SCHEMATIC.getEntitiesCount() > 0){
            int entitiesCount = Constants.LOADED_SCHEMATIC.getEntitiesCount();
            for(int entityIndex = 0; entityIndex < entitiesCount; entityIndex++){
                Namespace namespace = Constants.LOADED_SCHEMATIC.getEntityNamespace(entityIndex);

                if(namespace == null)
                    continue;

                EntityValues entityValues = Constants.LOADED_SCHEMATIC.getEntityValues(entityIndex);

                if(!Constants.SupportedEntities.contains(namespace.getType()))
                    continue;

                //Inject entity values into custom data of namespace
                namespace.setCustomData(entityValues);

                //Get  singleton entity cube model from memory or create it anew every time
                ICubeModel entityCubeModel = Constants.CUBE_MODEL_FACTORY.fromNamespace(namespace);

                if(entityCubeModel != null){
                    //Translate the singleton entity cube model to the position of the entity in the space
                    List<Double> pos = entityValues.getDoubleList("Pos");

                    //Get origin of entity
                    Double[] entityOrigin = ((EntityCubeModel)entityCubeModel).getOrigin();

                    double x = Math.abs(pos.get(0) - Constants.LOADED_SCHEMATIC.getOriginX() - entityOrigin[0]);
                    double y = Math.abs(pos.get(2) - Constants.LOADED_SCHEMATIC.getOriginZ() - entityOrigin[1]);
                    double z = Math.abs(pos.get(1) - Constants.LOADED_SCHEMATIC.getOriginY() - entityOrigin[2]);

                    CubeModelUtility.translateCubeModel(entityCubeModel, new Double[]{x, y, z}, new Integer[]{width,length,height});

                    processedCubesModels.add(entityCubeModel);
                }
            }
        }
        processingBlocks[0] = false;

        while (true){
            if(!writerTask.isAlive())
                break;
        }


        LogUtility.Log("");
        if(!writerError[0]) {
            LogUtility.Log("Writing material file");
            if(!exportMaterialsToMTL(outPath)) {
                LogUtility.Log("Error while writing material file");
                return false;
            }
        }

        try {
            writerTask.join();
        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
            return  false;
        }

        if(!exportAllBlocks){
            LogUtility.Log("Merging blocks");
            Constants.CUBE_MODEL_FACTORY.clearData();
            ImageUtility.clearData();
            if(!WavefrontUtility.mergeOBJ(obj_file, output_path))
                return false;
        }

        return !writerError[0];
    }

    public boolean exportMaterialsToMTL(String outputPath){
        try{
            Path output_path = Paths.get(outputPath);

            String fileName = output_path.toFile().getName().replace(".obj","");

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

        return true;
    }

    public boolean exportToOBJ(int[] countTracker, ICubeModel cubeModel, PrintWriter f){
        try{
            IWavefrontObject object = WavefrontObjectFactory.fromCubeModel(cubeModel);

            if(object != null && !object.getMaterialFaces().isEmpty()){
                WavefrontUtility.writeObjectData(object, f, countTracker);
            }

        }catch (Exception ex){
            return false;
        }

        return true;
    }

    /**
     * @author NotStirred
     */
    private static boolean sendUsageInfoIfRequested(String[] args, Options options) {
        Options helpOptions = new Options();
        Option helpOption = Option.builder("h").longOpt("help").argName("Help").desc("Display usage information").optionalArg(true).build();
        helpOptions.addOption(helpOption);
        DefaultParser parser = DefaultParser.builder().setAllowPartialMatching(false).build();

        HelpFormatter helper = new HelpFormatter();
        if (args.length == 0) {
            helper.printHelp(120, " ", "", options, "");
            return true;
        }
        try {
            CommandLine parsed = parser.parse(helpOptions, args);

            if (parsed.hasOption(helpOption)) {
                helper.printHelp(120, " ", "", options, "");
                return true;
            }
        } catch (ParseException ignored) { }
        return false;
    }
}
