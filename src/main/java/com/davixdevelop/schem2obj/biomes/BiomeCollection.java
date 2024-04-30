package com.davixdevelop.schem2obj.biomes;

import com.davixdevelop.schem2obj.biomes.json.JsonBiome;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;

public class BiomeCollection {
    LinkedHashMap<Integer, Biome> biomes;

    public BiomeCollection(){
        biomes = new LinkedHashMap<>();

        Gson gson = new Gson();

        //Get tge input stream of biomes.json
        //biomes.json was generated using ExtractBlocks (https://github.com/DavixDevelop/ExtractBlocks)
        InputStream biomesStream = this.getClass().getClassLoader().getResourceAsStream("assets/schem2obj/biomes.json");

        if(biomesStream != null){
            //Get reader for input stream
            Reader reader = new InputStreamReader(biomesStream);
            //Deserialize the JSON to Map
            List<JsonBiome> jsonBiomes = gson.fromJson(reader, new TypeToken<List<JsonBiome>>(){}.getType());

            for(JsonBiome jsonBiome : jsonBiomes){
                biomes.put(jsonBiome.getID(), new Biome(jsonBiome.getName(), jsonBiome.getResourceLocation(), jsonBiome.getGrassColor(), jsonBiome.getFoliageColor(), jsonBiome.getWaterColor()));
            }
        }
    }

    public Biome getBiomeForId(Integer id){
        if(biomes.containsKey(id))
            return biomes.get(id);

        return null;
    }
}
