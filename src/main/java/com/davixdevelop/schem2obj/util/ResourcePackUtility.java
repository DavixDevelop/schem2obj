package com.davixdevelop.schem2obj.util;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModelCollection;
import com.davixdevelop.schem2obj.blockstates.BlockStateCollection;
import com.davixdevelop.schem2obj.materials.MaterialCollection;
import com.davixdevelop.schem2obj.materials.json.PackTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourcePackUtility {
    public static boolean registerResourcePack(String resourcePack, String format, MaterialCollection materialCollection, BlockModelCollection blockModelCollection, BlockStateCollection blockStateCollection){
        Path resourcePackPath = Paths.get(resourcePack);

        //Check if resource pack is a folder
        if(resourcePackPath.toFile().isDirectory()){
            //Get path to pack.mcmeta
            Path packPath = Paths.get(resourcePack,"pack.mcmeta");

            //Check if pack.mcmeta exists
            if(Files.exists(packPath)){
                try{
                    //Get input stream for pack.mcmeta
                    InputStream inputStream = new FileInputStream(packPath.toFile().toString());
                    //Get the contents of pack.mcmeta
                    PackTemplate packMetaJson = Constants.BLOCK_MATERIALS.getPackMeta(inputStream);

                    //Check that the format is in the correct format
                    if(packMetaJson.pack.pack_format.intValue() != 3){
                        LogUtility.Log(String.format("Incompatible resource pack (Pack format: %d)",packMetaJson.pack.pack_format.intValue()));
                        return false;
                    }
                }catch (Exception ex){
                    LogUtility.Log("Error while reading pack.mcmeta");
                    LogUtility.Log(ex.getMessage());
                    return false;
                }
            }else {
                LogUtility.Log("Resource pack doesn't contain pack.mcmeta");
                return false;
            }

            //Get the path to the textures folder
            Path texturesFolder = Paths.get(resourcePack, "assets","minecraft","textures");

            //Check if resource pack contains textures folder
            if(Files.exists(texturesFolder)) {

                //Get the texture folders inside the textures folder of the resource pack
                File[] textureFolders = texturesFolder.toFile().listFiles();

                //Loop through the textureFolders and generate materials from it
                if (textureFolders != null) {
                    for (File textureFolder : textureFolders) {
                        //Scan for textures in blocks and entities
                        if (!textureFolder.getName().equals("blocks") && !textureFolder.getName().equals("entity"))
                            continue;

                        File[] textures = textureFolder.listFiles();
                        String textureFolderName = textureFolder.getName();

                        if (textures != null) {
                            if (textureFolder.getName().equals("entity")) {
                                for (File file : textures) {

                                    if (file.isDirectory() && Constants.EntityFilter.contains(file.getName())) {
                                        File[] entityTextures = file.listFiles();

                                        //Parse all textures in the subfolder inside the entity folder
                                        if (entityTextures != null)
                                            materialCollection.parseTexturesFromPackFiles(entityTextures, textureFolderName, format, resourcePack, file.getName());
                                    }
                                    //ToDo: Here read the entity textures that are in the root entity folder

                                }
                            } else {
                                materialCollection.parseTexturesFromPackFiles(textures, textureFolderName, format, resourcePack);
                            }
                        }
                    }
                }
            }

            //Get the path to the models folder
            Path modelsFolder = Paths.get(resourcePack, "assets","minecraft","models");
            //Check if resource pack contains models folder
            if(Files.exists(modelsFolder)){
                File[] modelsSubfolder = modelsFolder.toFile().listFiles();

                if(modelsSubfolder != null){
                    for(File modelsSubFolder : modelsSubfolder){
                        if(modelsSubFolder.isDirectory() && modelsSubFolder.getName().equals("block")){
                            File[] blockModels = modelsSubFolder.listFiles();

                            if(blockModels != null)
                                for(File blockModel : blockModels){
                                    if(blockModel.isFile() && blockModel.getName().endsWith(".json")) {
                                        String modelName = String.format("%s/%s", modelsSubFolder.getName(), blockModel.getName().replace(".json", ""));
                                        try{

                                            InputStream modelStream = new FileInputStream(blockModel);
                                            blockModelCollection.putBlockModel(modelStream, modelName);
                                        }catch (Exception ex){
                                            LogUtility.Log("Failed to read BlockModel " + modelName);
                                            return false;
                                        }
                                    }
                                }

                        }

                    }
                }
            }

            //Get the path to the block states folder
            Path blockStatesFolder = Paths.get(resourcePack, "assets","minecraft","blockstates");
            //Check if resource pack contains block states folder
            if(Files.exists(blockStatesFolder)){
                File[] blockStates = blockStatesFolder.toFile().listFiles();

                if(blockStates != null){
                    for(File blockState : blockStates){
                        if(blockState.isFile() && blockState.getName().endsWith(".json")){
                            String blockStateName = blockState.getName().replace(".json", "");
                            try{
                                InputStream blockStateStream = new FileInputStream(blockState);
                                blockStateCollection.putBlockState(blockStateStream, blockStateName);
                            }catch (Exception ex){
                                LogUtility.Log("Failed to read BlockState " + blockStateName);
                                return false;
                            }


                        }

                    }
                }
            }

            return true;
        }else{
            try {

                ZipFile compressedFile = null;

                //Check if resource pack is a zip file, else treat it as a jar file
                if (resourcePack.endsWith(".zip")) {
                    compressedFile = new ZipFile(resourcePack);

                    //Get entry for pack.mcmeta inside zip
                    ZipEntry packEntry = compressedFile.getEntry("pack.mcmeta");
                    try{
                        //Get input stream for pack.mcmeta
                        InputStream inputStream = compressedFile.getInputStream(packEntry);
                        //Get the contents of pack.mcmeta
                        PackTemplate packMetaJson = Constants.BLOCK_MATERIALS.getPackMeta(inputStream);

                        //Check that the format is in the correct format
                        if(packMetaJson.pack.pack_format.intValue() != 3){
                            LogUtility.Log(String.format("Incompatible resource pack (Pack format: %d)",packMetaJson.pack.pack_format.intValue()));
                            return false;
                        }
                    }catch (Exception ex){
                        LogUtility.Log("Failed to read pack.mcmeta");
                        LogUtility.Log(ex.getMessage());
                        return false;
                    }

                }else
                    compressedFile = new JarFile(resourcePack);

                Enumeration<? extends ZipEntry> entries = compressedFile.entries();

                //Loop through the all entries is compressed file
                while (entries.hasMoreElements()){
                    ZipEntry zipEntry = entries.nextElement();

                    String fullPath = zipEntry.getName();

                    //Check if entry is in the texture folder and ends with .png
                    if(fullPath.startsWith("assets/minecraft/textures/") && fullPath.endsWith(".png")){
                        //Get the path to the texture within the texture folder
                        String assetPath = fullPath.substring(26);

                        //Get the type of the texture (ex. blocks/dirt.png -> blocks)
                        String textureType = assetPath.substring(0, assetPath.indexOf("/"));

                        //Scan only for texture types of blocks and entities
                        if(!textureType.equals("blocks") && !textureType.equals("entity"))
                            continue;



                        //Get the file name of texture (ex. entity/bed/blue.png - > blue.png)
                        String fileName = assetPath.substring(assetPath.lastIndexOf("/") + 1);
                        //Get the full path to the folder of the texture
                        //ex. assets/minecraft/textures/entity/bed/blue.png -> assets/minecraft/textures/entity/bed
                        String folderPath = fullPath.substring(0, fullPath.length() - fileName.length() - 1);

                        if(textureType.equals("entity")){
                            //Get the entity name (ex. entity/bed/blue.png -> bed )
                            String entityName = assetPath.substring(7);
                            //Skip entity textures that are not in a subfolder inside the entity folder
                            if(!entityName.equals(fileName)){
                                entityName = entityName.substring(0, entityName.length() - fileName.length() - 1);
                                if(Constants.EntityFilter.contains(entityName))
                                    materialCollection.parseMaterialFromEntry(compressedFile, fileName, format, textureType, folderPath, entityName);
                            }
                            //ToDo: Here read the entity texture that are in the root folder
                        }else {
                            materialCollection.parseMaterialFromEntry(compressedFile, fileName, format, textureType, folderPath);
                        }

                        continue;
                    }

                    //Check if entry is in the models block folder and ends with .json
                    if(fullPath.startsWith("assets/minecraft/models/") && fullPath.endsWith(".json")){
                        //Get the path to the block model within the models folder
                        String assetPath = fullPath.substring(24);

                        //Get the type of block model (block or item)
                        String modelType = assetPath.substring(0, assetPath.indexOf("/"));

                        String modelName = assetPath.substring(modelType.length() + 1, assetPath.length() - 5);

                        try{
                            InputStream modelStream = compressedFile.getInputStream(zipEntry);
                            blockModelCollection.putBlockModel(modelStream, modelType + "/" + modelName);
                        }catch (Exception ex){
                            LogUtility.Log("Failed to read BlockModel " + modelName);
                            return false;
                        }

                        continue;
                    }


                    //Check if entry is in the block states folder and ends with .json
                    if(fullPath.startsWith("assets/minecraft/blockstates/") && fullPath.endsWith(".json")){
                        //Get the path to the block state within the blockstates folder
                        String assetPath = fullPath.substring(29);

                        String blockStateName = assetPath.replace(".json", "");

                        try{
                            InputStream blockStateStream = compressedFile.getInputStream(zipEntry);
                            blockStateCollection.putBlockState(blockStateStream, blockStateName);
                        }catch (Exception ex){
                            LogUtility.Log("Failed to read BlockState " + blockStateName);
                            return false;
                        }
                    }

                }

                return true;
            }catch (Exception ex){
                LogUtility.Log(String.format("Failed to parse resource pack %s:", resourcePack));
                LogUtility.Log(ex.getMessage());
            }
        }

        return false;
    }
}
