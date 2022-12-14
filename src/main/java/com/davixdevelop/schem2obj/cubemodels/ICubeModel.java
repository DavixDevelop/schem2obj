package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.Orientation;
import com.davixdevelop.schem2obj.models.HashedStringList;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.List;
import java.util.Map;

/**
 * A interface for the CubeModel
 *
 * @author DavixDevelop
 */
public interface ICubeModel {
    String getName();
    HashedStringList getMaterials();
    Integer putMaterial(String material);
    List<ICube> getCubes();
    void addCube(ICube cube);

    /**
     * Append another cube model to the cube model
     * @param model The cube model to append
     */
    void appendCubeModel(ICubeModel model);

    /**
     * Get the key to the cube model
     * Ex, regular blocks have a key consisting of a block name entry and the block states of the block
     * @param namespace The namespace of the cube model
     * @return A map representing a key
     */
    Map<String, Object> getKey(Namespace namespace);

    /**
     * Generate a Cube Model from block namespace
     * Return true to store cube model in memory or not
     * @param namespace The namespace of the block
     * @return true to store in memory, else not
     */
    boolean fromNamespace(Namespace namespace);

    /**
     * Return true if cube model should check for collision (facing) with the parent cube model
     * @param adjacent The Adjacent cube model
     * @return true if it should check, else false
     */
    boolean checkCollision(ICubeModel adjacent);

    /**
     * Delete all cube model faces that face the specified orientation and remove material if it all It's face are null
     * @param orientation The orientation (ex. TOP, UP, NORTH...)
     */
    void deleteFaces(Orientation orientation);

    /**
     * Return a deep copy of the CubeModel
     * @return The deep copy of the CubeModel
     */
    ICubeModel duplicate();

    /**
     * Create deep copy from clone
     * @param clone The cloned CubeModel object
     */
    void copy(ICubeModel clone);

}
