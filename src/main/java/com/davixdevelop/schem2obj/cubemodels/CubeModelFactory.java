package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockDisplay;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.BlockTextures;
import com.davixdevelop.schem2obj.cubemodels.blocks.*;
import com.davixdevelop.schem2obj.cubemodels.entity.*;
import com.davixdevelop.schem2obj.cubemodels.entitytile.*;
import com.davixdevelop.schem2obj.cubemodels.item.ItemCubeModel;
import com.davixdevelop.schem2obj.cubemodels.item.armor.ArmorBootsCubeModel;
import com.davixdevelop.schem2obj.cubemodels.item.armor.ArmorChestplateCubeModel;
import com.davixdevelop.schem2obj.cubemodels.item.armor.ArmorHelmetCubeModel;
import com.davixdevelop.schem2obj.cubemodels.item.armor.ArmorLeggingsCubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.schematic.EntityValues;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory responsible for generating and storing CubeModel's from block namespaces, and or their entity values.
 *
 * @author DavixDevelop
 */
public class CubeModelFactory {

    public ConcurrentMap<Map<?, ?>, ICubeModel> cubeModels;
    public ConcurrentMap<Object, ICubeModel> itemModels;

    public CubeModelFactory(){
        cubeModels = new ConcurrentHashMap<>();
        itemModels = new ConcurrentHashMap<>();

        //blockGenerationQueue = ConcurrentHashMap.newKeySet();
        //itemGenerationQueue = ConcurrentHashMap.newKeySet();
    }

    public Map<?, ?> getKey(Namespace namespace){
        ICubeModel block = getType(namespace);
        Map<?, ?> key = block.getKey(namespace);

        /*if(blockGenerationQueue.contains(key)){
            while (true){
                if(!blockGenerationQueue.contains(key))
                    break;
            }
        }else
            blockGenerationQueue.add(key);*/

        if (!cubeModels.containsKey(key)) {
            //Only store object in memory if to does not have random variants (multiple variants in "variants" field)
            //If it does recreate it every time
            if (block.fromNamespace(namespace))
                cubeModels.put(key, block);

        }
        //blockGenerationQueue.remove(key);

        return key;
    }

    public ICubeModel fromKey(Map<?, ?> key){
        if(cubeModels.containsKey(key))
            return cubeModels.get(key).duplicate();

        return null;
    }

    public ICubeModel fromNamespace(Namespace namespace){
        ICubeModel block = getType(namespace);
        Map<?, ?> key = block.getKey(namespace);

        /*if(blockGenerationQueue.contains(key)){
            while (true){
                if(!blockGenerationQueue.contains(key))
                    break;
            }
        }else
            blockGenerationQueue.add(key);*/

        if(cubeModels.containsKey(key)) {
            //blockGenerationQueue.remove(key);
            return cubeModels.get(key).duplicate();
        }else{
            //Only store object in memory if to does not have random variants (multiple variants in "variants" field)
            //If it does recreate it every time
            if(block.fromNamespace(namespace))
                cubeModels.put(key, block);

            //blockGenerationQueue.remove(key);
            return block.duplicate();
        }
    }

    public Namespace getNamespaceFromItem(EntityValues item){
        //Get namespace of item
        String itemResource = item.getString("id");
        Namespace namespace = Constants.NAMESPACE_MAPPING.getNamespace(itemResource);

        if(namespace != null){
            namespace.setPosition(new Integer[]{0,0,0});

            Set<String> tagKeys = new LinkedHashSet<>();
            boolean injectTagIntoNamespaceCustomData = false;

            //Check if item contains a tag and add all It's key to tagKey
            if(item.containsKey("tag")){
                injectTagIntoNamespaceCustomData = true;
                EntityValues tag = item.getEntityValues("tag");
                for(String key : tag.keySet()){
                    if(key.equals("BlockEntityTag")){
                        EntityValues blockEntityTag = tag.getEntityValues("BlockEntityTag");
                        tagKeys.addAll(blockEntityTag.keySet());
                    }else
                        tagKeys.add(key);
                }
            }

            boolean useDamageAsMetaID = false;
            EntityValues entityValues = new EntityValues();
            if(namespace.getDefaultEntityValues() != null)
                entityValues = namespace.getDefaultEntityValues().duplicate();

            if(!tagKeys.isEmpty() || !namespace.getValidEntityKeys().isEmpty()){
                List<String> namespaceValidKeys = namespace.getValidEntityKeys();
                for(String validKey : namespaceValidKeys){
                    if(tagKeys.contains(validKey)){
                        useDamageAsMetaID = true;
                        break;
                    }
                }
            }else
                useDamageAsMetaID = true;

            if(injectTagIntoNamespaceCustomData){
                //Check if item has additional tags
                //Loop through the "tag", add all It's values into entity values
                EntityValues tag = item.getEntityValues("tag");
                for(String key : tag.keySet()){
                    //If key names is BlockEntityTag, copy all It's key into entity values
                    if(key.equals("BlockEntityTag")){
                        EntityValues blockEntityTag = tag.getEntityValues("BlockEntityTag");
                        for(String blockEntityKey : blockEntityTag.keySet()){
                            entityValues.put(blockEntityKey, blockEntityTag.get(blockEntityKey));
                        }
                    }else{
                        entityValues.put(key, tag.get(key));
                    }
                }
            }

            if(useDamageAsMetaID){
                //Treat the "damage" value of the item as the meta ID
                namespace.setDefaultBlockState(item.getShort("Damage").intValue());
            }

            if(!useDamageAsMetaID) {
                //If the namespace uses custom data and it only has one default entity key, inject the "damage" value of the item
                //into the the first valid key value
                if(namespace.getValidEntityKeys().size() == 1)
                    entityValues.put(namespace.getValidEntityKeys().get(0), item.getShort("Damage"));
                else
                    namespace.setDefaultBlockState(item.getShort("Damage").intValue());
            }

            //Inject entity values into namespace custom data, if it contains any values
            if(entityValues != null && !entityValues.isEmpty())
                namespace.setCustomData(entityValues);
        }

        return namespace;
    }

    /**
     * Get the item model from the Item nbt tag
     * @param item The nbt tag of the Item
     * @param itemHolderNamespace The namespace of the object that holds the Item
     * @return The model of the Item
     */
    public ICubeModel getItemModel(EntityValues item, Namespace itemHolderNamespace){
        boolean itemUsesBlockAsIcon = false;

        //Get namespace of item
        String itemResource = item.getString("id");
        String itemResourcePath = itemResource.substring(itemResource.indexOf(":") + 1);
        Namespace namespace = getNamespaceFromItem(item);

        //If namespace is null, the item uses the icon in textures/items as the item model
        if(namespace == null)
            itemUsesBlockAsIcon = true;


        ICubeModel itemCubeModel = null;
        Map<String, Object> key = new LinkedHashMap<>();

        if(namespace != null)
        {
            namespace.setDisplayMode(Namespace.DISPLAY_MODE.FIXED);
            itemCubeModel = getType(namespace);
            key = itemCubeModel.getKey(namespace);
        }

        Object itemKey = !key.isEmpty() ? key : itemResource;


        if(itemModels.containsKey(itemKey)){
            return itemModels.get(itemKey).duplicate();
        }else{
            String item_resource = null;

            if(namespace != null){
                item_resource = ResourceLoader.getResourcePath("models", String.format("item/%s", namespace.getResource()), "json");
                if(ResourceLoader.resourceExists(item_resource))
                    item_resource = namespace.getResource();
                else
                    item_resource = itemResourcePath;
            }else{
                item_resource = itemResourcePath;
            }

            //Get item block models
            ArrayList<BlockModel> itemBlockModels = Constants.BLOCK_MODELS.getBlockModel(item_resource, "item");
            //Get first block model (item model)
            BlockModel itemModel = itemBlockModels.get(0);
            //Check if the item model contains textures
            BlockTextures itemTextures = itemModel.getTextures();
            //If it does, use the provided texture (layer0) as the icon, and create a cube model from it
            if(!itemUsesBlockAsIcon) {
                itemUsesBlockAsIcon = !itemTextures.getTextures().isEmpty();
            }

            BlockDisplay.DisplayItem fixedDisplay = null;
            //Loop through the models, and find the display -> fixed object
            for(BlockModel model : itemBlockModels){
                if(model.getFixedDisplay() != null){
                    fixedDisplay = model.getFixedDisplay();
                    break;
                }
            }

            if(itemUsesBlockAsIcon){
                String itemIcon = null;
                if(itemTextures.getTextures().containsKey("layer0"))
                    itemIcon = itemTextures.getTextures().get("layer0");
                else
                {
                    Optional<String> optional = itemTextures.getTextures().values().stream().findFirst();
                    if(optional.isPresent())
                        itemIcon = optional.get();
                }

                itemCubeModel = new ItemCubeModel();
                CubeModelUtility.convertItemIconToCubeModel(itemCubeModel, itemIcon, itemHolderNamespace);
            }else{
                //Else get cube model of the default variant of the block
                itemCubeModel.fromNamespace(namespace);
            }

            if(fixedDisplay != null) {
                Double[] origin = Constants.BLOCK_ORIGIN;

                if(itemCubeModel instanceof EntityCubeModel)
                    origin = ((EntityCubeModel)itemCubeModel).getOrigin();

                //Scale, rotate and translate itemCube model, depending on the fixed display

                if (fixedDisplay.scale != null) {
                    if(fixedDisplay.scale[0] != 1.0 || fixedDisplay.scale[1] != 1.0 || fixedDisplay.scale[2] != 1.0)
                        CubeModelUtility.scaleCubeModel(itemCubeModel, fixedDisplay.scale, origin);
                }
                if(fixedDisplay.rotation != null)
                    CubeModelUtility.rotateCubeModel(itemCubeModel, fixedDisplay.rotation, origin);

                if(fixedDisplay.translation != null)
                    CubeModelUtility.translateCubeModel(itemCubeModel, fixedDisplay.translation);
            }

            itemModels.put(itemKey, itemCubeModel);
            //itemGenerationQueue.remove(itemKey);

            return itemCubeModel.duplicate();
        }
    }

    public static ICubeModel getType(Namespace blockNamespace){

        switch (blockNamespace.getResource()){
            case "minecart":
                return new MinecartEntityCubeModel();
            case "painting":
                return new PaintingCubeModel();
            case "boat":
                return new BoatCubeModel();
            case "item_frame":
                return new ItemFrameCubeModel();
            case "armor_stand":
                return new ArmorStandModel();
        }

        if (blockNamespace.getResource().contains("slab") && !blockNamespace.getResource().contains("double"))
            return new SlabCubeModel();

        if (blockNamespace.getResource().contains("fence") && !blockNamespace.getResource().contains("gate"))
            return new FenceCubeModel();

        if(blockNamespace.getResource().contains("glass_pane"))
            return new GlassPaneCubeModel();

        if(blockNamespace.getResource().contains("glass"))
            return new GlassCubeModel();

        if(blockNamespace.getResource().contains("stairs"))
            return new StairsCubeModel();

        if(blockNamespace.getResource().contains("command_block"))
            return new CommandCubeModel();

        if(blockNamespace.getResource().contains("door"))
            return new DoorCubeModel();

        if(blockNamespace.getResource().contains("leaves"))
            return new LeavesCubeModel();

        if(blockNamespace.getResource().contains("fence_gate"))
            return new FenceGateCubeModel();

        if(blockNamespace.getType().contains("_helmet"))
            return new ArmorHelmetCubeModel();

        if(blockNamespace.getType().contains("_chestplate"))
            return new ArmorChestplateCubeModel();

        if(blockNamespace.getType().contains("_leggings"))
            return new ArmorLeggingsCubeModel();

        if(blockNamespace.getType().contains("_boots"))
            return new ArmorBootsCubeModel();


        switch (blockNamespace.getType()) {
            case "grass":
                return new GrassCubeModel();
            case "magma":
                return new MagmaCubeModel();
            case "sea_lantern":
                return new SeaLanternCubeModel();
            case "prismarine":
                return new PrismarineCubeModel();
            case "lit_pumpkin":
                return new LitPumpkinCubeModel();
            case "iron_bars":
                return new IronBarsCubeModel();
            case "redstone_wire":
                return new RedstoneWireCubeModel();
            case "fire":
                return new FireCubeModel();
            case "lit_furnace":
                return new LitFurnaceCubeModel();
            case "cauldron":
                return new CauldronCubeModel();
            case "bed":
                return new BedCubeModel();
            case "standing_banner":
                return new StandingBannerCubeModel();
            case "wall_banner":
                return new WallBannerCubeModel();
            case "standing_sign":
                return new StandingSignCubeModel();
            case "wall_sign":
                return new WallSignCubeModel();
            case "skull":
                return new SkullCubeModel();
            case "chest":
                return new NormalChestCubeModel();
            case "trapped_chest":
                return new TrappedChestCubeModel();
            case "ender_chest":
                return new EnderChestCubeMode();
            case "tallgrass":
                return new TallGrassCubeModel();
            case "double_plant":
                return new DoublePlantCubeModel();
            case "cobblestone_wall":
                return new CobblestoneWallCubeModel();
            default:
                return new BlockCubeModel();

        }

    }

    public static boolean isTranslucentOrNotFull(ICubeModel object){

        return (object instanceof GlassCubeModel) ||
                (object instanceof GlassPaneCubeModel) ||
                (object instanceof IronBarsCubeModel) ||
                (object instanceof LeavesCubeModel) ||
                (object instanceof SlabCubeModel) ||
                (object instanceof StairsCubeModel) ||
                (object instanceof FenceCubeModel) ||
                (object instanceof RedstoneWireCubeModel) ||
                (object instanceof DoorCubeModel) ||
                (object instanceof FireCubeModel) ||
                (object instanceof CauldronCubeModel) ||
                (object instanceof TallGrassCubeModel) ||
                (object instanceof DoublePlantCubeModel) ||
                (object instanceof CobblestoneWallCubeModel) ||
                (object instanceof FenceGateCubeModel) ||
                (object.getName() != null && isTranslucentOrNotFull(object.getName())) ||
                object instanceof TileEntityCubeModel ||
                object instanceof EntityCubeModel;
    }

    public static boolean isTranslucentOrNotFull(String blockType){
        return blockType.equals("slime") ||
                blockType.contains("rail") ||
                blockType.contains("button") ||
                blockType.equals("grass_path") ||
                blockType.contains("pressure_plate") ||
                blockType.contains("daylight_detector") ||
                blockType.contains("piston_head") ||
                blockType.contains("comparator") ||
                blockType.contains("repeater") ||
                blockType.equals("snow_layer") ||
                blockType.contains("carpet") ||
                blockType.contains("flower") ||
                blockType.equals("air") ||
                blockType.equals("end_rod") ||
                blockType.equals("sapling") ||
                blockType.equals("double_plant") ||
                blockType.equals("tallgrass") ||
                blockType.equals("lever") ||
                blockType.contains("_comparator") ||
                blockType.contains("_repeater") ||
                blockType.equals("skull") ||
                blockType.contains("redstone_torch") ||
                blockType.contains("door") ||
                blockType.equals("barrier");
    }

    public void clearData(){
        cubeModels.clear();
        itemModels.clear();
    }

}
