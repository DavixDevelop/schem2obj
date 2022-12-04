package com.davixdevelop.schem2obj.resourceloader;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.materials.json.PackTemplate;
import com.davixdevelop.schem2obj.util.LogUtility;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceLoader {

    public static List<IResourcePack> RESOURCE_PACKS = new ArrayList<>();
    public static Map<String, Integer> RESOURCES = new HashMap<>();

    /**
     * Get the InputStream of the resource based on the path to the resource
     * @param path Path to the resource, ex. textures/blocks/dirt.png
     * @return The InputStream of the resource
     */
    public static InputStream getResource(String path){
        if(RESOURCES.containsKey(path)){
            Integer resourcePackIndex = RESOURCES.get(path);
            return RESOURCE_PACKS.get(resourcePackIndex).getResource(path);
        }else{
            return ResourceLoader.class.getClassLoader().getResourceAsStream("assets/minecraft/" + path);
        }
    }

    /**
     * Return if resource can be kept in memory
     * @param path The path to the resource
     * @return True if it can be kept in memory, else not
     */
    public static boolean storeInMemory(String path){
        if(RESOURCES.containsKey(path)){
            Integer resourcePackIndex = RESOURCES.get(path);
            return RESOURCE_PACKS.get(resourcePackIndex).storeInMemory();
        }
        return true;
    }



    /**
     * Return the format of the resource pack
     * @param path The path to the resource
     * @return The format of the resource pack (Vanilla, SEUS...)
     */
    public static ResourcePack.Format getFormat(String path){
        if(RESOURCES.containsKey(path)){
            Integer resourcePackIndex = RESOURCES.get(path);
            return RESOURCE_PACKS.get(resourcePackIndex).getFormat();
        }else{
            return ResourcePack.Format.Vanilla;
        }
    }

    /**
     * Get the relative path to the texture in a resource pack
     * @param resourceType The path to the resource pack
     * @param resourceName The name of the resource, ex. blocks/dirt, entity/bed/blue, magma.json...
     * @param resourceFormat The format of the resource, ex. png, json...
     * @return The path to the resource, ex. textures/blocks/magma.png
     */
    public static String getResourcePath(String resourceType, String resourceName, String resourceFormat){
        return String.format("%s/%s.%s",resourceType, resourceName, resourceFormat);
    }

    /**
     * Check if resource exists
     * @param path The path to the resource
     * @return True if the resource exists, else false
     */
    public static boolean resourceExists(String path){
        return RESOURCES.containsKey(path);
    }

    /**
     * Register a new resource pack
     * @param resourcePack The path to the folder or zip/jar of the resource pack
     * @param format The format of the resource pack
     * @return True if resource pack was registered or not
     */
    public static boolean registerResourcePack(String resourcePack, ResourcePack.Format format){

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
                    PackTemplate packMetaJson = getPackMeta(inputStream);

                    //Check that the format is in the correct format
                    if(packMetaJson != null) {
                        if (packMetaJson.pack.pack_format.intValue() != 3) {
                            LogUtility.Log(String.format("Incompatible resource pack (Pack format: %d)", packMetaJson.pack.pack_format.intValue()));
                            return false;
                        }
                    }else
                        return false;
                }catch (Exception ex){
                    LogUtility.Log("Error while reading pack.mcmeta");
                    LogUtility.Log(ex.getMessage());
                    return false;
                }
            }else {
                LogUtility.Log("Resource pack doesn't contain pack.mcmeta");
                return false;
            }

            //Create new Resource Pack
            IResourcePack folderResourcePack = new FolderResourcePack(resourcePack, format);
            //Add it to the list of resource packs
            RESOURCE_PACKS.add(folderResourcePack);

            //Get the index to the resource pack
            Integer resourcePackIndex = RESOURCE_PACKS.size() - 1;

            //Get the path to the textures folder
            Path texturesFolder = Paths.get(resourcePack, "assets","minecraft","textures");

            //Check if resource pack contains textures folder
            if(Files.exists(texturesFolder)) {

                //Get the texture folders inside the textures folder of the resource pack
                File[] textureFolders = texturesFolder.toFile().listFiles();

                //Loop through the textureFolders and generate materials from it
                if (textureFolders != null) {

                    String textureFolderPath = Paths.get(resourcePack, "assets","minecraft").toString();

                    for (File textureFolder : textureFolders) {
                        //Scan for textures in blocks and entities
                        if (!textureFolder.getName().equals("blocks") && !textureFolder.getName().equals("entity") && !textureFolder.getName().equals("font") && !textureFolder.getName().equals("painting") && !textureFolder.getName().equals("items"))
                            continue;

                        File[] textures = textureFolder.listFiles();
                        //String textureFolderName = textureFolder.getName();

                        if (textures != null) {
                            parseTexturesFromPackFiles(textures, textureFolderPath, resourcePackIndex);
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
                        if(modelsSubFolder.isDirectory() && (modelsSubFolder.getName().equals("block") || modelsSubFolder.getName().equals("item"))){
                            File[] blockModels = modelsSubFolder.listFiles();

                            if(blockModels != null)
                                for(File blockModel : blockModels){
                                    if(blockModel.isFile() && blockModel.getName().endsWith(".json")) {
                                        String modelPath = String.format("models/%s/%s", modelsSubFolder.getName(), blockModel.getName());
                                        RESOURCES.put(modelPath, resourcePackIndex);
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
                            String blockStatePath = String.format("blockstates/%s", blockState.getName());
                            RESOURCES.put(blockStatePath, resourcePackIndex);
                        }

                    }
                }
            }

            //Get the path to the glyph_sizes.bin file
            Path glyphSizes = Paths.get(resourcePack, "assets","minecraft","font","glyph_sizes.bin");
            //Check if it exist, and add it to resource
            if(Files.exists(glyphSizes)){
                RESOURCES.put("font/glyph_sizes.bin", resourcePackIndex);
            }

            return true;
        }else{
            try {

                ZipFile compressedFile;

                //Check if resource pack is a zip file, else treat it as a jar file
                if (resourcePack.endsWith(".zip")) {
                    compressedFile = new ZipFile(resourcePack);

                    //Get entry for pack.mcmeta inside zip
                    ZipEntry packEntry = compressedFile.getEntry("pack.mcmeta");
                    try{
                        //Get input stream for pack.mcmeta
                        InputStream inputStream = compressedFile.getInputStream(packEntry);
                        //Get the contents of pack.mcmeta
                        PackTemplate packMetaJson = getPackMeta(inputStream);

                        //Check that the format is in the correct format
                        if(packMetaJson != null) {
                            if (packMetaJson.pack.pack_format.intValue() != 3) {
                                LogUtility.Log(String.format("Incompatible resource pack (Pack format: %d)", packMetaJson.pack.pack_format.intValue()));
                                return false;
                            }
                        }else
                            return false;
                    }catch (Exception ex){
                        LogUtility.Log("Failed to read pack.mcmeta");
                        LogUtility.Log(ex.getMessage());
                        return false;
                    }

                }else
                    compressedFile = new JarFile(resourcePack);

                Enumeration<? extends ZipEntry> entries = compressedFile.entries();

                //Create new resource pack
                IResourcePack compressedResourcePack = new ZippedResourcePack(compressedFile, format);
                //Add it to resource pack list
                RESOURCE_PACKS.add(compressedResourcePack);
                //Get the index to the resource pack
                Integer resourcePackIndex = RESOURCE_PACKS.size() - 1;

                //Loop through the all entries is compressed file
                while (entries.hasMoreElements()){
                    ZipEntry zipEntry = entries.nextElement();

                    String fullPath = zipEntry.getName();

                    //Check if entry is in the texture folder and ends with .png or .mcmeta
                    if(fullPath.startsWith("assets/minecraft/textures/") && (fullPath.endsWith(".png") || fullPath.endsWith(".mcmeta"))){
                        //Get the path to the texture within the texture folder  (ex. blocks/dirt.png)
                        String assetPath = fullPath.substring(26);

                        //Get the type of the texture (ex. blocks/dirt.png -> blocks)
                        String textureType = assetPath.substring(0, assetPath.indexOf("/"));

                        //Scan only for texture types of blocks and entities and font
                        if(!textureType.equals("blocks") && !textureType.equals("entity") && !textureType.equals("font") && !textureType.equals("painting") && !textureType.equals("items"))
                            continue;

                        //Get the path to the texture (ex. textures/entity/bed/blue.png - > blue.png)
                        String textureFilePath = fullPath.substring(17);

                        if(textureType.equals("entity")){
                            //Get the path to the entity texture within the entity folder (ex. entity/bed/blue.json -> bed/blue.json)
                            String entityPath = assetPath.substring(7);
                            //If the entity path contains /, the entity uses one or more textures
                            if(entityPath.contains("/")){
                                //Get the name of the entity (name of folder)
                                String entityName = entityPath.substring(0, entityPath.indexOf("/"));
                                //If the entity isn't supported skip it
                                if(!Constants.EntityFolderFilter.contains(entityName))
                                    continue;
                            }
                        }

                        //Add it to resources
                        RESOURCES.put(textureFilePath, resourcePackIndex);

                        continue;
                    }

                    //Check if entry is in the models block folder and ends with .json
                    if(fullPath.startsWith("assets/minecraft/models/") && fullPath.endsWith(".json")){
                        //Get the path to the block model within the minecraft folder, ex. models/block/magma.json
                        String modelPath = fullPath.substring(17);
                        //Add the block model to the resources
                        RESOURCES.put(modelPath, resourcePackIndex);
                        continue;
                    }


                    //Check if entry is in the block states folder and ends with .json
                    if(fullPath.startsWith("assets/minecraft/blockstates/") && fullPath.endsWith(".json")){
                        //Get the path to the block state within the minecraft folder, ex. blockstates/magma.json
                        String blockStatePath = fullPath.substring(17);
                        //Add the block state to the resources
                        RESOURCES.put(blockStatePath, resourcePackIndex);
                        continue;
                    }

                    if(fullPath.equals("assets/minecraft/font/glyph_sizes.bin")){
                        RESOURCES.put("font/glyph_sizes.bin", resourcePackIndex);
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

    private static void parseTexturesFromPackFiles(File[] textures, String textureFolderPath, Integer resourcePackIndex){
        //Loop through the textures and add them to resources
        for(File texture : textures){
            if(texture.isDirectory()){
                //Check if parent directory is entity
                if(texture.getParentFile().getName().equals("entity")) {
                    //Skip entity textures that the program doesn't support
                    if (!Constants.EntityFolderFilter.contains(texture.getName()))
                        continue;
                }

                File[] subTextures = texture.listFiles();
                if(subTextures != null)
                    parseTexturesFromPackFiles(subTextures, textureFolderPath, resourcePackIndex);
            }
            if(texture.getName().endsWith(".png") || texture.getName().endsWith(".mcmeta")) {

                String textureFilePath = texture.getPath().substring(textureFolderPath.length() + 1).replace("\\","/");

                RESOURCES.put(textureFilePath, resourcePackIndex);
            }
        }
    }

    public static PackTemplate getPackMeta(InputStream packMetaInputStream){
        try {
            //Get reader for pack.mcmeta
            Reader reader = new InputStreamReader(packMetaInputStream);
            //Deserialize json
            PackTemplate packMetaJson = new Gson().fromJson(reader, PackTemplate.class);

            reader.close();

            return packMetaJson;
        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
        }

        return null;
    }
}
