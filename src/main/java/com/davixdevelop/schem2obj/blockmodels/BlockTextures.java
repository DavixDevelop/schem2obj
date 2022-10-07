package com.davixdevelop.schem2obj.blockmodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    public BlockTextures clone(){
        HashMap<String, String> newTextures = new HashMap<>();
        if(textures != null){
            newTextures = new HashMap<>(textures.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return new BlockTextures(particle, newTextures);
    }

}


