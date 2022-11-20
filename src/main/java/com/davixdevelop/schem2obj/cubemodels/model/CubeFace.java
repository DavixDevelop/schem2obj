package com.davixdevelop.schem2obj.cubemodels.model;

import java.util.ArrayList;
import java.util.List;

public class CubeFace {
    private List<Double[]> corners;
    private List<Double[]> uv;
    private String material;
    private Boolean cullface;

    /**
     * Create new CubeFace
     * @param corners A 4 size list, containing the vertices of each corner
     * @param uv A 4 size list, containing the texture vertices of each corner
     * @param material The material of the face
     * @param isCullface If the face can be removed
     */
    public CubeFace(List<Double[]> corners, List<Double[]> uv, String material, Boolean isCullface){
        this.corners = corners;
        this.uv = uv;
        this.material = material;
        this.cullface = isCullface;
    }

    public CubeFace(){
        corners = new ArrayList<>();
        uv = new ArrayList<>();
        material = "";
        cullface = false;
    }


    public CubeFace clone(){
        CubeFace cloneFace = new CubeFace();
        cloneFace.copy(this);

        return cloneFace;
    }

    public void copy(CubeFace cubeFace){
        corners = new ArrayList<>(cubeFace.corners);
        uv = new ArrayList<>(cubeFace.uv);
        material = cubeFace.material;
        cullface = cubeFace.cullface;
    }

    /**
     * Get the corner vertices of the face
     * @return A 4 size list, containing the vertices of each corner
     */
    public List<Double[]> getCorners() {
        return corners;
    }

    public void setCorners(List<Double[]> corners) {
        this.corners = corners;
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
    public Boolean isCullface() {
        return cullface;
    }
}
