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
    private List<Double[]> corners;
    private List<Double[]> uv;
    private String material;
    private Boolean cullFace;

    /**
     * Create new CubeFace
     * @param corners A 4 size list, containing the vertices of each corner
     * @param uv A 4 size list, containing the texture vertices of each corner
     * @param material The material of the face
     * @param isCullFace If the face can be removed
     */
    public CubeFace(List<Double[]> corners, List<Double[]> uv, String material, Boolean isCullFace){
        this.corners = corners;
        this.uv = uv;
        this.material = material;
        this.cullFace = isCullFace;
    }

    public CubeFace(){
        corners = new ArrayList<>();
        uv = new ArrayList<>();
        material = "";
        cullFace = false;
    }


    public CubeFace duplicate(){
        CubeFace cloneFace = new CubeFace();
        cloneFace.copy(this);

        return cloneFace;
    }

    public void copy(CubeFace cubeFace){
        corners = new ArrayList<>();
        for(Double[] v : cubeFace.corners) {
            Double[] newVertex = new Double[v.length];
            newVertex = Arrays.copyOf(v, v.length);
            corners.add(newVertex);
        }

        uv = new ArrayList<>();
        for(Double[] u : cubeFace.uv) {
            Double[] newUV = new Double[u.length];
            newUV = Arrays.copyOf(u, u.length);
            uv.add(newUV);
        }

        material = cubeFace.material;
        cullFace = cubeFace.cullFace;
    }

    /**
     * Get the corner vertices of the face
     * @return A 4 size list, containing the vertices of each corner
     */
    public List<Double[]> getCorners() {
        return corners;
    }

    /**
     * Get the texture vertices of each corner
     * @return A 4 size list, containing the texture vertices of each corner
     */
    public List<Double[]> getUv() {
        return uv;
    }

    /**
     * Get the material of the face
     * @return name of material
     */
    public String getMaterial() {
        return material;
    }

    /**
     * Check if face can be deleted
     * @return True if face can be deleted
     */
    public Boolean isCullFace() {
        return cullFace;
    }
}
