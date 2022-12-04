package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockDisplay;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.BlockTextures;
import com.davixdevelop.schem2obj.cubemodels.blocks.*;
import com.davixdevelop.schem2obj.cubemodels.entity.*;
import com.davixdevelop.schem2obj.cubemodels.entitytile.*;
import com.davixdevelop.schem2obj.cubemodels.item.ItemCubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.schematic.EntityValues;

import java.util.*;

/**
 * Factory responsible for generating and storing CubeModel's from block namespaces, and or their entity values.
 *
 * @author DavixDevelop
 */
public class CubeModelFactory {

    public Map<Map<?, ?>, ICubeModel> cubeModels;
    public Map<Object, ICubeModel> itemModels;

    public CubeModelFactory(){
        cubeModels = new LinkedHashMap<>();
        itemModels = new LinkedHashMap<>();
    }

    public ICubeModel fromNamespace(Namespace namespace){
        ICubeModel block = getType(namespace);
        Map<?, ?> key = block.getKey(namespace);

        if(cubeModels.containsKey(key))
            return cubeModels.get(key).duplicate();
        else{
            //Only store object in memory if to does not have random variants (multiple variants in "variants" field)
            //If it does recreate it every time
            if(block.fromNamespace(namespace))
                cubeModels.put(key, block);

            return block.duplicate();
        }
    }

    public ICubeModel getItemModel(EntityValues item, Namespace itemHolderNamespace){
        boolean itemUsesBlockAsIcon = false;

        //Get namespace of item
        String itemResource = item.getString("id");
        String itemResourcePath = itemResource.substring(itemResource.indexOf(":") + 1);
        Namespace namespace = Constants.NAMESPACE_MAPPING.getNamespace(itemResource);
        //If namespace is null, the item uses the icon in textures/items as the item model
        if(namespace == null)
            itemUsesBlockAsIcon = true;
        else{
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


        ICubeModel itemCubeModel = null;
        Map<String, Object> key = new LinkedHashMap<>();

        if(namespace != null)
        {
            namespace.setDisplayMode(Namespace.DISPLAY_MODE.FIXED);
            itemCubeModel = getType(namespace);
            key = itemCubeModel.getKey(namespace);
        }

        if(itemModels.containsKey(!key.isEmpty() ? key : itemResource)){
            return itemModels.get(!key.isEmpty() ? key : itemResource).duplicate();
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
                //ToDo: Convert layer0 texture to cube model
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

            itemModels.put(!key.isEmpty() ? key : itemResource, itemCubeModel);

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
        }

        if (blockNamespace.getResource().contains("slab") && !blockNamespace.getResource().contains("double"))
            return new SlabCubeModel();

        if (blockNamespace.getResource().contains("fence") && !blockNamespace.getResource().contains("gate"))
            return new FenceCubeModel();

        switch (blockNamespace.getResource()) {
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
            case "leaves":
                return new LeavesCubeModel();
            case "glass_pane":
                return new GlassPaneCubeModel();
            case "glass":
                return new GlassCubeModel();
            case "stairs":
                return new StairsCubeModel();
            case "command_block":
                return new CommandCubeModel();
            case "door":
                return new DoorCubeModel();
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
                object.getName().equals("slime") ||
                object.getName().contains("rail") ||
                object.getName().contains("button") ||
                object.getName().equals("grass_path") ||
                object.getName().contains("pressure_plate") ||
                object.getName().contains("daylight_detector") ||
                object.getName().contains("piston_head") ||
                object.getName().contains("comparator") ||
                object.getName().contains("repeater") ||
                object.getName().equals("snow_layer") ||
                object instanceof TileEntityCubeModel ||
                object instanceof EntityCubeModel;
    }

}
