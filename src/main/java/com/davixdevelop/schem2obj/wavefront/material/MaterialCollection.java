package com.davixdevelop.schem2obj.wavefront.material;

import com.davixdevelop.schem2obj.utilities.Utility;
import com.davixdevelop.schem2obj.wavefront.WavefrontCollection;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

public class MaterialCollection {
    private HashMap<String, Material> materials;
    private Set<String> usedMaterials;

    public MaterialCollection(){
        materials = new HashMap<>();
        usedMaterials = new HashSet<>();
    }

    public Material getMaterial(String name){
        return materials.get(name);
    }

    public void setMaterial(String name, Material material){
        materials.put(name, material);
        usedMaterials.add(name);
    }

    public boolean containsMaterial(String name){
        return materials.containsKey(name);
    }

    /**
     * Return the used materials names
     * @return A set of used materials
     */
    public Set<String> usedMaterials(){
        return usedMaterials;
    }

    public void registerTexturePack(String format, String resourcePack){
        //Get the path to the textures folder of the resource pack
        Path resourcePackTexturesFolder = Paths.get(resourcePack, "assets","minecraft","textures");
        //Get the texture folders inside the textures folder of the resource pack
        File[] textureFolders = resourcePackTexturesFolder.toFile().listFiles();


        Matcher matcher = null;
        //Loop through the textureFolders
        for(File textureFolder : textureFolders){
            //ToDo: For now only scan for textures in blocks
            if(!textureFolder.getName().equals("blocks"))
                continue;

            File[] textures = textureFolder.listFiles();

            String textureFolderName = textureFolder.getName();

            //Loop through the textures
            for(File texture : textures){

                //Pattern to get texture name from png texture (example grass_n.png or grass.png -> grass)
                matcher = Utility.TEXTURE_NAME_FROM_FILE.matcher(texture.getName());
                if(matcher.find()){
                    String textureName = matcher.group(1);


                    String materialName = String.format("%s/%s",textureFolderName,textureName);
                    //Check if material (texture folder + / + texture name, example blocks/grass)
                    //Is not already in memory
                    if(!containsMaterial(materialName)){
                        //Create material depending on the format (SEUS, Specular, Vanilla)
                        Material material = null;

                        switch (format){
                            case "SEUS":
                                material = new SEUSMaterial(resourcePack, materialName + ".png");
                        }

                        //Put material into memory, for later use
                        materials.put(materialName, material);
                    }
                }
            }
        }
    }
}
