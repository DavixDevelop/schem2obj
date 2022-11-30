package com.davixdevelop.schem2obj.blockmodels;

import java.util.HashMap;

/**
 * Represents a instance of a element, inside the elements array in a BlockModel JSON
 *
 * @author DavixDevelop
 */
public class CubeElement {
    private Double[] from;
    private Double[] to;
    private Boolean shade;
    private CubeRotation rotation;
    private HashMap<String, CubeFace> faces;

    public CubeElement(Double[] from, Double[] to, Boolean shade, CubeRotation rotation, HashMap<String, CubeFace> faces){
        this.from = from;
        this.to = to;
        this.shade = shade;
        this.rotation = rotation;
        this.faces = faces;
    }

    public void setFrom(Double[] from) {
        this.from = from;
    }

    public Double[] getFrom() {
        return from;
    }

    public void setTo(Double[] to) {
        this.to = to;
    }

    public Double[] getTo() {
        return to;
    }

    public void setShade(Boolean shade) {
        this.shade = shade;
    }

    public Boolean getShade() {
        return shade;
    }

    public void setRotation(CubeRotation rotation) {
        this.rotation = rotation;
    }

    public CubeRotation getRotation() {
        return rotation;
    }

    public void setFaces(HashMap<String, CubeFace> faces) {
        this.faces = faces;
    }

    public HashMap<String, CubeFace> getFaces() {
        return faces;
    }

    public static class CubeRotation{
        private Double[] origin;
        private String[] axis;
        private Double[] angle;
        private Boolean rescale;

        public CubeRotation(Double[] origin, String axis[], Double angle[], Boolean rescale){
            this.origin = origin;
            this.axis = axis;
            this.angle = angle;
            this.rescale = rescale;
        }

        public void setOrigin(Double[] origin) {
            this.origin = origin;
        }

        public Double[] getOrigin() {
            return origin;
        }

        public void setAxis(String[] axis) {
            this.axis = axis;
        }

        public String[] getAxis() {
            return axis;
        }

        public void setAngle(Double[] angle) {
            this.angle = angle;
        }

        public Double[] getAngle() {
            return angle;
        }

        public void setRescale(Boolean rescale) {
            this.rescale = rescale;
        }

        public Boolean getRescale() {
            return rescale;
        }
    }

    public static class CubeFace {
        private Double[] uv;
        private String texture;
        private String cullface;
        private Double rotation;
        private Double tintindex;

        public CubeFace(Double[] uv, String texture, String cullface, Double rotation, Double tintindex){
            this.uv = uv;
            this.texture = texture;
            this.cullface = cullface;
            this.rotation = rotation;
            this.tintindex = tintindex;
        }

        public void setUv(Double[] uv) {
            this.uv = uv;
        }

        public Double[] getUv() {
            return uv;
        }

        public void setTexture(String texture) {
            this.texture = texture;
        }

        public String getTexture() {
            return texture;
        }

        public void setCullface(String cullface) {
            this.cullface = cullface;
        }

        public String getCullface() {
            return cullface;
        }

        public void setRotation(Double rotation) {
            this.rotation = rotation;
        }

        public Double getRotation() {
            return rotation;
        }

        public void setTintindex(Double tintindex) {
            this.tintindex = tintindex;
        }

        public Double getTintindex() {
            return tintindex;
        }
    }
}
