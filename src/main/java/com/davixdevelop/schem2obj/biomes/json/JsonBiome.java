package com.davixdevelop.schem2obj.biomes.json;

public class JsonBiome {
    Integer ID;
    String name;
    String resourceLocation;
    Integer grassColor;
    Integer foliageColor;
    Integer waterColor;

    public JsonBiome(Integer ID, String name, String resourceLocation, Integer grassColor, Integer foliageColor, Integer waterColor){
        this.ID = ID;
        this.name = name;
        this.resourceLocation = resourceLocation;
        this.grassColor = grassColor;
        this.foliageColor = foliageColor;
        this.waterColor = waterColor;
    }

    public Integer getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getResourceLocation() {
        return resourceLocation;
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
