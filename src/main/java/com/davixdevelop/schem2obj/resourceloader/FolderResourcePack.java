package com.davixdevelop.schem2obj.resourceloader;

import com.davixdevelop.schem2obj.util.LogUtility;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FolderResourcePack extends ResourcePack {

    private String resourcePack;

    public FolderResourcePack(String resourcePack, Format format){
        this.resourcePack = resourcePack;
        setFormat(format);
    }

    @Override
    public InputStream getResource(String path) {
        Path resourcePath = Paths.get(resourcePack, "assets","minecraft", path);
        if(Files.exists(resourcePath)){
            try{
                return new FileInputStream(resourcePath.toFile());
            }catch (Exception ex){
                LogUtility.Log(String.format("Error while reading %s:", resourcePath));
                LogUtility.Log(ex.getMessage());
            }
        }

        LogUtility.Log("Could not find: " + resourcePath);
        return null;

    }

    @Override
    public boolean storeInMemory() {
        return false;
    }
}
