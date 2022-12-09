package com.davixdevelop.schem2obj.cubemodels.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a single face of a cube
 *
 * @author DavixDevelop
 */
public class CubeFace {
    private List<Integer> corners;
    private List<Integer> uv;
    private Boolean cullFace;

    /**
     * Create new CubeFace
     * @param corners A 4 size list, containing the vertices of each corner
     * @param uv A 4 size list, containing the texture vertices of each corner
     * @param material The material of the face
     * @param isCullFace If the face can be removed
     */
    public CubeFace(List<Integer> corners, List<Integer> uv, String material, Boolean isCullFace){
        this.corners = corners;
        this.uv = uv;
        this.cullFace = isCullFace;
    }

    public CubeFace(){
        corners = new ArrayList<>();
        uv = new ArrayList<>();
        cullFace = false;
    }


    public CubeFace duplicate(){
        CubeFace cloneFace = new CubeFace();
        cloneFace.copy(this);

        return cloneFace;
    }

    public void copy(CubeFace cubeFace){
        corners = new ArrayList<>();
        corners.addAll(cubeFace.corners);

        uv = new ArrayList<>();
        uv.addAll(cubeFace.uv);

        cullFace = cubeFace.cullFace;
    }

    /**
     * Get the the index to corner vertices of the face
     * @return A 4 size list, containing the indexes to vertices of each corner
     */
    public List<Integer> getCorners() {
        return corners;
    }

    /**
     * Get the index to texture vertices of each corner
     * @return A 4 size list, containing the indexes to texture vertices of each corner
     */
    public List<Integer> getUv() {
        return uv;
    }



    /**
     * Check if face can be deleted
     * @return True if face can be deleted
     */
    public Boolean isCullFace() {
        return cullFace;
    }
}
