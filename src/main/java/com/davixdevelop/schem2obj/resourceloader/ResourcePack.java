package com.davixdevelop.schem2obj.resourceloader;

import java.io.InputStream;

public class ResourcePack implements IResourcePack {
    private Format format;

    @Override
    public InputStream getResource(String path) {
        return null;
    }

    @Override
    public boolean storeInMemory() {
        return false;
    }

    @Override
    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public enum Format{
        Vanilla,
        SEUS;

        public static Format fromName(String name){
            if(name.equals("SEUS"))
                return SEUS;

            return Vanilla;
        }
    }
}
