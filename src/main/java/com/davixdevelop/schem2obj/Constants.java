package com.davixdevelop.schem2obj;

import com.davixdevelop.schem2obj.blockmodels.BlockModelCollection;
import com.davixdevelop.schem2obj.blockstates.BlockStateCollection;
import com.davixdevelop.schem2obj.namespace.BlockMapping;
import com.davixdevelop.schem2obj.schematic.SchematicHolder;
import com.davixdevelop.schem2obj.wavefront.WavefrontCollection;
import com.davixdevelop.schem2obj.wavefront.material.MaterialCollection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Constants {

    public static void setConstants(){
        REDSTONE_COLORS.put(0, 4915200);
        REDSTONE_COLORS.put(1,7274496);
        REDSTONE_COLORS.put(2,7929856);
        REDSTONE_COLORS.put(3,8519680);
        REDSTONE_COLORS.put(4,9175040);
        REDSTONE_COLORS.put(5,9895936);
        REDSTONE_COLORS.put(6,10551296);
        REDSTONE_COLORS.put(7,11206656);
        REDSTONE_COLORS.put(8,11862016);
        REDSTONE_COLORS.put(9,12517376);
        REDSTONE_COLORS.put(10,13238272);
        REDSTONE_COLORS.put(11,13828096);
        REDSTONE_COLORS.put(12,14483456);
        REDSTONE_COLORS.put(13,15140352);
        REDSTONE_COLORS.put(14,15801088);
        REDSTONE_COLORS.put(15,16527616);

        FACING_ROTATION.put("north", 270.0);
        FACING_ROTATION.put("south", 90.0);
        FACING_ROTATION.put("west", 180.0);
        FACING_ROTATION.put("east", 0.0);

        EntityFilter.add("bed");
    }

    public static final SchematicHolder LOADED_SCHEMATIC = new SchematicHolder();

    public static final BlockModelCollection BLOCK_MODELS = new BlockModelCollection();
    public static final BlockStateCollection BLOCKS_STATES = new BlockStateCollection();
    public static final BlockMapping BLOCK_MAPPING = new BlockMapping();

    public static final MaterialCollection BLOCK_MATERIALS = new MaterialCollection();
    public static final WavefrontCollection WAVEFRONT_COLLECTION = new WavefrontCollection();

    public static int BIOME_GRASS_COLOR = 9551193;
    public static int BIOME_FOLIAGE_COLOR = 7842607;
    public static int BIOME_WATER_COLOR = 4159204;

    public static int SNOW_COLOR = 16316922;

    public static Map<Integer, Integer> REDSTONE_COLORS = new HashMap<>();

    public static boolean IS_SNOWY = false;

    public static Set<String> EntityFilter = new HashSet<>();

    public static final Double[] BLOCK_ORIGIN = new Double[] {0.5,0.5,0.5};

    public static Pattern TEXTURE_NAME_FROM_FILE = Pattern.compile("^(.*?)((?>_[a-z])|(?>))\\.png");

    //Factor to mix in "black" parts of the emission texture with the diffuse texture
    public static double EMISSION_MIX_FACTOR = 0.5;

    public static String[] META_COLORS = new String[]{
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black"
    };

    public static Map<String, Double> FACING_ROTATION = new HashMap<>();

}
