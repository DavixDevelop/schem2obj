package com.davixdevelop.schem2obj;

import com.davixdevelop.schem2obj.biomes.BiomeCollection;
import com.davixdevelop.schem2obj.blockmodels.BlockModelCollection;
import com.davixdevelop.schem2obj.blockstates.BlockStateCollection;
import com.davixdevelop.schem2obj.cubemodels.CubeModelFactory;
import com.davixdevelop.schem2obj.materials.MaterialCollection;
import com.davixdevelop.schem2obj.models.IntegerString;
import com.davixdevelop.schem2obj.namespace.NamespaceMapping;
import com.davixdevelop.schem2obj.schematic.SchematicHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

        EntityFolderFilter.add("bed");
        EntityFolderFilter.add("banner");
        EntityFolderFilter.add("enderdragon");
        EntityFolderFilter.add("skeleton");
        EntityFolderFilter.add("creeper");
        EntityFolderFilter.add("zombie");
        EntityFolderFilter.add("chest");
        EntityFolderFilter.add("boat");
        EntityFolderFilter.add("armorstand");

        SupportedEntities.add("minecart");
        SupportedEntities.add("bed");
        SupportedEntities.add("standing_banner");
        SupportedEntities.add("wall_banner");
        SupportedEntities.add("standing_sign");
        SupportedEntities.add("wall_sign");
        SupportedEntities.add("skull");
        SupportedEntities.add("chest");
        SupportedEntities.add("trapped_chest");
        SupportedEntities.add("ender_chest");
        SupportedEntities.add("painting");
        SupportedEntities.add("boat");
        SupportedEntities.add("item_frame");
        SupportedEntities.add("armor_stand");

        BANNER_COLORS.put(0, new IntegerString("black", 1644825));
        BANNER_COLORS.put(1, new IntegerString("red",10040115));
        BANNER_COLORS.put(2, new IntegerString("green",6717235));
        BANNER_COLORS.put(3, new IntegerString("brown",6704179));
        BANNER_COLORS.put(4, new IntegerString("blue",3361970));
        BANNER_COLORS.put(5, new IntegerString("purple",8339378));
        BANNER_COLORS.put(6, new IntegerString("cyan",5013401));
        BANNER_COLORS.put(7, new IntegerString("silver",10066329));
        BANNER_COLORS.put(8, new IntegerString("gray",5000268));
        BANNER_COLORS.put(9, new IntegerString("pink",15892389));
        BANNER_COLORS.put(10, new IntegerString("lime",8375321));
        BANNER_COLORS.put(11, new IntegerString("yellow",15066419));
        BANNER_COLORS.put(12, new IntegerString("light_blue",6724056));
        BANNER_COLORS.put(13, new IntegerString("magenta",11685080));
        BANNER_COLORS.put(14, new IntegerString("orange",14188339));
        BANNER_COLORS.put(15, new IntegerString("white",16777215));

        PATTERNS.put("b", new IntegerString("entity/banner/base", 0));
        PATTERNS.put("bs", new IntegerString("entity/banner/stripe_bottom", 1));
        PATTERNS.put("ts", new IntegerString("entity/banner/stripe_top", 2));
        PATTERNS.put("ls", new IntegerString("entity/banner/stripe_left", 3));
        PATTERNS.put("rs", new IntegerString("entity/banner/stripe_right", 4));
        PATTERNS.put("cs", new IntegerString("entity/banner/stripe_center", 5));
        PATTERNS.put("ms", new IntegerString("entity/banner/stripe_middle", 6));
        PATTERNS.put("drs", new IntegerString("entity/banner/stripe_downright", 7));
        PATTERNS.put("dls", new IntegerString("entity/banner/stripe_downleft", 8));
        PATTERNS.put("ss", new IntegerString("entity/banner/small_stripes", 9));
        PATTERNS.put("cr", new IntegerString("entity/banner/cross", 10));
        PATTERNS.put("sc", new IntegerString("entity/banner/straight_cross", 11));
        PATTERNS.put("ld", new IntegerString("entity/banner/diagonal_left", 12));
        PATTERNS.put("rud", new IntegerString("entity/banner/diagonal_up_right", 13));
        PATTERNS.put("lud", new IntegerString("entity/banner/diagonal_up_left", 14));
        PATTERNS.put("rd", new IntegerString("entity/banner/diagonal_right", 15));
        PATTERNS.put("vh", new IntegerString("entity/banner/half_vertical", 16));
        PATTERNS.put("vhr", new IntegerString("entity/banner/half_vertical_right", 17));
        PATTERNS.put("hh", new IntegerString("entity/banner/half_horizontal", 18));
        PATTERNS.put("hhb", new IntegerString("entity/banner/half_horizontal_bottom", 19));
        PATTERNS.put("bl", new IntegerString("entity/banner/square_bottom_left", 20));
        PATTERNS.put("br", new IntegerString("entity/banner/square_bottom_right", 21));
        PATTERNS.put("tl", new IntegerString("entity/banner/square_top_left", 22));
        PATTERNS.put("tr", new IntegerString("entity/banner/square_top_right", 23));
        PATTERNS.put("bt", new IntegerString("entity/banner/triangle_bottom", 24));
        PATTERNS.put("tt", new IntegerString("entity/banner/triangle_top", 25));
        PATTERNS.put("bts", new IntegerString("entity/banner/triangles_bottom", 26));
        PATTERNS.put("tts", new IntegerString("entity/banner/triangles_top", 27));
        PATTERNS.put("mc", new IntegerString("entity/banner/circle", 28));
        PATTERNS.put("mr", new IntegerString("entity/banner/rhombus", 29));
        PATTERNS.put("bo", new IntegerString("entity/banner/border", 30));
        PATTERNS.put("cbo", new IntegerString("entity/banner/curly_border", 31));
        PATTERNS.put("bri", new IntegerString("entity/banner/bricks", 32));
        PATTERNS.put("gra", new IntegerString("entity/banner/gradient", 33));
        PATTERNS.put("gru", new IntegerString("entity/banner/gradient_up", 34));
        PATTERNS.put("cre", new IntegerString("entity/banner/creeper", 35));
        PATTERNS.put("sku", new IntegerString("entity/banner/skull", 36));
        PATTERNS.put("flo", new IntegerString("entity/banner/flower", 37));
        PATTERNS.put("moj", new IntegerString("entity/banner/mojang", 37));

        TEXT_COLORS.put("red_color", new IntegerString("4", 11141120));
        TEXT_COLORS.put("red", new IntegerString("c", 16733525));
        TEXT_COLORS.put("gold", new IntegerString("6", 16755200));
        TEXT_COLORS.put("yellow", new IntegerString("e", 16777045));
        TEXT_COLORS.put("dark_green", new IntegerString("2", 43520));
        TEXT_COLORS.put("green", new IntegerString("a", 5635925));
        TEXT_COLORS.put("aqua", new IntegerString("b", 5636095));
        TEXT_COLORS.put("dark_aqua", new IntegerString("3", 43690));
        TEXT_COLORS.put("dark_blue", new IntegerString("1", 170));
        TEXT_COLORS.put("blue", new IntegerString("9", 5592575));
        TEXT_COLORS.put("light_purple", new IntegerString("d", 16733695));
        TEXT_COLORS.put("dark_purple", new IntegerString("5", 11141290));
        TEXT_COLORS.put("white", new IntegerString("f", 16777215));
        TEXT_COLORS.put("gray", new IntegerString("7", 11184810));
        TEXT_COLORS.put("dark_grey", new IntegerString("8", 5592405));
        TEXT_COLORS.put("black", new IntegerString("0", 0));
    }

    public static boolean EXPORT_ALL_BLOCKS = false;

    public static final SchematicHolder LOADED_SCHEMATIC = new SchematicHolder();

    public static final BlockModelCollection BLOCK_MODELS = new BlockModelCollection();
    public static final BlockStateCollection BLOCKS_STATES = new BlockStateCollection();
    public static final NamespaceMapping NAMESPACE_MAPPING = new NamespaceMapping();
    public static final BiomeCollection BIOME_COLLECTION = new BiomeCollection();

    public static final MaterialCollection BLOCK_MATERIALS = new MaterialCollection();
    public static final CubeModelFactory CUBE_MODEL_FACTORY = new CubeModelFactory();

    public static int CONSTANT_BIOME_ID = 1;

    public static int SNOW_COLOR = 16316922;
    public static int DEFAULT_LEATHER_COLOR = 10511680;

    public static int SWAMPLAND_PURPLE_OVERLAY = 10716067;

    public static int SPRUCE_LEAVES_COLOR = 6396257;

    public static int BIRCH_LEAVES_COLOR = 8431445;

    public static Map<Integer, Integer> REDSTONE_COLORS = new HashMap<>();

    public static Map<Integer, IntegerString> BANNER_COLORS = new HashMap<>();
    public static Map<String, IntegerString> PATTERNS = new HashMap<>();

    public static Map<String, IntegerString> TEXT_COLORS = new HashMap<>();

    public static boolean IS_SNOWY = false;

    public static Set<String> EntityFolderFilter = new HashSet<>();
    public static Set<String> SupportedEntities = new HashSet<>();

    public static final Double[] BLOCK_ORIGIN = new Double[] {0.5,0.5,0.5};

    //Factor to mix in "black" parts of the emission texture with the diffuse texture
    public static double EMISSION_MIX_FACTOR = 0.5;

    public static String[] META_COLORS = new String[]{
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black"
    };

    public static Map<String, Double> FACING_ROTATION = new HashMap<>();

    public static boolean CHRISTMAS_CHEST = false;

}
