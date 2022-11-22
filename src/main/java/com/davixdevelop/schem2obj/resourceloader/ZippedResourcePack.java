package com.davixdevelop.schem2obj.resourceloader;

import com.davixdevelop.schem2obj.util.LogUtility;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZippedResourcePack extends ResourcePack {

    ZipFile zipFile;

    public ZippedResourcePack(ZipFile zipFile, Format format){
        this.zipFile = zipFile;
        setFormat(format);
    }

    @Override
    public InputStream getResource(String path) {
        String entryName = Paths.get("assets","minecraft",path).toString().replace("\\", "/");
        ZipEntry zipEntry = zipFile.getEntry(entryName);
        if(zipEntry != null){
            try {
                return zipFile.getInputStream(zipEntry);
            }catch (Exception ex){
                LogUtility.Log(String.format("Error while reading %s in resource pack: %s", entryName, zipFile.getName()));
            }
        }

        LogUtility.Log(String.format("Could not find %s in resource pack: %s", entryName, zipFile.getName()));
        return null;
    }

    @Override
    public boolean storeInMemory() {
        return true;
    }
}
