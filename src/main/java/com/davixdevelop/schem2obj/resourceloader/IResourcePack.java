package com.davixdevelop.schem2obj.resourceloader;

import java.io.InputStream;

public interface IResourcePack {
    /**
     * Get the InputStream of the resource based on the path to the resource
     * @param path Path to the resource, ex. textures/blocks/dirt.png
     * @return The InputStream of the resource
     */
    InputStream getResource(String path);

    /**
     * Return if resource can be kept in memory
     * @return True if it can be kept in memory, else not
     */
    boolean storeInMemory();

    /**
     * Return the format of the resource pack
     * @return The format of the resource pack (Vanilla, SEUS...)
     */
    ResourcePack.Format getFormat();
}
