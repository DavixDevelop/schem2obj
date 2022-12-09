package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.Orientation;
import com.davixdevelop.schem2obj.cubemodels.model.CubeFace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a single cube in a CubeModel
 *
 * @author DavixDevelop
 */
public class Cube implements ICube {
    private Integer[] materialFaces;
    private Boolean[] generatedFaces;
    private CubeFace[] cubeFaces;

    private List<Double[]> corners;
    private List<Double[]> uvs;

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
    public Cube(Integer[] materialFaces, Boolean[] generatedFaces, CubeFace[] cubeFaces, List<Double[]> corners, List<Double[]> textureCoordinates) {
        this.materialFaces = materialFaces;
        this.generatedFaces = generatedFaces;
        this.cubeFaces = cubeFaces;
        this.corners = corners;
        uvs = textureCoordinates;
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
    public List<Double[]> getCorners() {
        return corners;
    }

    @Override
    public List<Double[]> getTextureCoordinates() {
        return uvs;
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

    @Override
    public void setMaterialFace(Integer faceIndex, Integer materialIndex) {
        materialFaces[faceIndex] = materialIndex;
    }

    public void deleteFace(Orientation orientation) {
        generatedFaces[orientation.getOrder()] = false;
    }

    @Override
    public ICube duplicate() {
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

        corners = new ArrayList<>();
        for(Double[] v : cloneCube.corners)
            corners.add(Arrays.copyOf(v, v.length));

        uvs = new ArrayList<>();
        for(Double[] u : cloneCube.uvs)
            uvs.add(Arrays.copyOf(u, u.length));

        for (int x = 0; x < 6; x++) {
            CubeFace face = cloneCube.cubeFaces[x];
            if (face != null)
                cubeFaces[x] = face.duplicate();
        }
    }
}
