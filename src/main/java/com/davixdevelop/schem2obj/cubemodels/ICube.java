package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.Orientation;
import com.davixdevelop.schem2obj.cubemodels.model.CubeFace;

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

    void setCubeFace(int index, CubeFace cubeFace);

    /**
     * Get marked faces to be exported
     * @return A 6 length array of all cube faces marked to be exported (See Orientation.DIRECTIONS for order of faces)
     */
    Boolean[] getGeneratedFaces();

    /**
     * Get a list of material indexes per face
     * @return A 6 length array of material indexes per face (See Orientation.DIRECTIONS for order of faces)
     */
    Integer[] getMaterialFaces();

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
    ICube clone();

    /**
     * Create deep copy from clone
     *
     * @param clone The cloned Cube object
     */
    void copy(ICube clone);
}
