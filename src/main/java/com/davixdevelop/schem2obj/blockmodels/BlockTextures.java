package com.davixdevelop.schem2obj.blockmodels;

import java.util.ArrayList;
import java.util.HashMap;

public class BlockTextures {
    private String particle;
    private HashMap<String, String> textures;

    public BlockTextures(String particle, HashMap<String, String>  textures){
        this.particle = particle;
        this.textures = textures;
    }

    public void setParticle(String particle) {
        this.particle = particle;
    }

    public String getParticle() {
        return particle;
    }

    public HashMap<String, String>  getTextures() {
        return textures;
    }

    public void setTextures(HashMap<String, String>  textures) {
        this.textures = textures;
    }

}


