package com.davixdevelop.schem2obj;

import com.davixdevelop.schem2obj.blockmodels.BlockModelCollection;
import com.davixdevelop.schem2obj.blockstates.BlockStateCollection;
import com.davixdevelop.schem2obj.namespace.BlockMapping;
import com.davixdevelop.schem2obj.schematic.SchematicHolder;
import com.davixdevelop.schem2obj.wavefront.WavefrontCollection;
import com.davixdevelop.schem2obj.wavefront.material.MaterialCollection;

public class Constants {

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

    public static final Double[] BLOCK_ORIGIN = new Double[] {0.5,0.5,0.5};
}
