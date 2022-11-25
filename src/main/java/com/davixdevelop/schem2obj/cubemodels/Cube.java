package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.Orientation;
import com.davixdevelop.schem2obj.cubemodels.model.CubeFace;

import java.util.Arrays;

/**
 * Represents a single cube in a CubeModel
 *
 * @author DavixDevelop
 */
public class Cube implements ICube {
    private Integer[] materialFaces;
    private Boolean[] generatedFaces;
    private CubeFace[] cubeFaces;

    /**
     * Create new cube
     * ___F___
     * C¯¯¯   |   ¯¯¯G
     * |¯¯¯___B___¯¯¯|
     * |      M      |
     * |___¯¯¯|¯¯¯___|
     * D___   |   ___H
     * ¯¯¯A¯¯¯
     *
     * @param materialFaces  A 6 length array of material indexes per face (See Orientation.DIRECTIONS for order of faces)
     * @param generatedFaces A 6 length array that shows which faces should be exported (See Orientation.DIRECTIONS for order of faces)
     * @param cubeFaces      A 6 length array of the cube faces
     */
    public Cube(Integer[] materialFaces, Boolean[] generatedFaces, CubeFace[] cubeFaces) {
        this.materialFaces = materialFaces;
        this.generatedFaces = generatedFaces;
        this.cubeFaces = cubeFaces;
    }

    public Cube() {
        materialFaces = new Integer[6];
        generatedFaces = new Boolean[6];
    }

    @Override
    public CubeFace[] getFaces() {
        return cubeFaces;
    }

    @Override
    public void setCubeFace(int index, CubeFace cubeFace) {
        cubeFaces[index] = cubeFace;
    }

    @Override
    public Boolean[] getGeneratedFaces() {
        return generatedFaces;
    }

    @Override
    public Integer[] getMaterialFaces() {
        return materialFaces;
    }

    public void deleteFace(Orientation orientation) {
        generatedFaces[orientation.getOrder()] = false;
    }

    @Override
    public ICube clone() {
        Cube cubeClone = new Cube();
        cubeClone.copy(this);

        return cubeClone;
    }

    @Override
    public void copy(ICube clone) {
        Cube cloneCube = (Cube) clone;
        materialFaces = new Integer[6];
        materialFaces = Arrays.copyOf(cloneCube.materialFaces, 6);
        generatedFaces = new Boolean[6];
        generatedFaces = Arrays.copyOf(cloneCube.generatedFaces, 6);

        cubeFaces = new CubeFace[6];

        for (int x = 0; x < 6; x++) {
            CubeFace face = cloneCube.cubeFaces[x];
            if (face != null)
                cubeFaces[x] = face.clone();
        }
    }
}
