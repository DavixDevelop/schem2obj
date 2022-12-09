package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.Orientation;
import com.davixdevelop.schem2obj.cubemodels.model.CubeFace;

import java.util.List;

/**
 * A interface for a Cube in a CubeModel
 *
 * @author DavixDevelop
 */
public interface ICube {
    /**
     * Get all faces of the cube
     *
     * @return A 6 length array of all cube faces (See Orientation.DIRECTIONS for order of faces)
     */
    CubeFace[] getFaces();

    /**
     * Get list of vertices the cube uses
     * @return Max 8 size list of vertices
     */
    List<Double[]> getCorners();

    /**
     * Get list of uv's the cube uses
     * @return List of uv's the cube uses
     */
    List<Double[]> getTextureCoordinates();


    void setCubeFace(int index, CubeFace cubeFace);

    /**
     * Get marked faces to be exported
     * @return A 6 length array of all cube faces marked to be exported (See Orientation.DIRECTIONS for order of faces)
     */
    Boolean[] getGeneratedFaces();

    /**
     * Get a array of material indexes per face
     * @return A 6 length array of material indexes per face (See Orientation.DIRECTIONS for order of faces)
     */
    Integer[] getMaterialFaces();

    /**
     * Set material index per face
     * @param faceIndex The index to the face (See Orientation.DIRECTIONS for order of faces)
     * @param materialIndex The index to the material in the cube
     */
    void setMaterialFace(Integer faceIndex, Integer materialIndex);

    /**
     * Delete the cube face that faces the orientation
     *
     * @param orientation The orientation of the face to delete
     */
    void deleteFace(Orientation orientation);

    /**
     * Return a deep copy of the Cube
     *
     * @return The deep copy of the Cube
     */
    ICube duplicate();

    /**
     * Create deep copy from clone
     *
     * @param clone The cloned Cube object
     */
    void copy(ICube clone);
}
