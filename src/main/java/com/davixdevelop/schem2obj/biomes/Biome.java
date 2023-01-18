package com.davixdevelop.schem2obj.biomes;

import java.awt.*;

public class Biome {
    String name;
    String resource;

    Integer grassColor;
    Integer foliageColor;
    Integer waterColor;

    public Biome(String name, String resource, Integer grassColor, Integer foliageColor, Integer waterColor){
        this.name = name;
        this.resource = resource;
        this.grassColor = grassColor;
        this.foliageColor = foliageColor;
        this.waterColor = waterColor;
    }

    public String getName() {
        return name;
    }

    public String getResource() {
        return resource;
    }

    public Integer getGrassColor() {
        return grassColor;
    }

    public Integer getFoliageColor() {
        return foliageColor;
    }

    public Integer getWaterColor() {
        return waterColor;
    }
}
