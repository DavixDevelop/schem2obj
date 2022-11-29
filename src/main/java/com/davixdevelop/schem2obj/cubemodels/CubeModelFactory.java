package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.cubemodels.blocks.*;
import com.davixdevelop.schem2obj.cubemodels.entity.EntityCubeModel;
import com.davixdevelop.schem2obj.cubemodels.entity.MinecartEntityCubeModel;
import com.davixdevelop.schem2obj.cubemodels.entitytile.*;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;

import java.util.HashMap;

/**
 * Factory responsible for generating and storing CubeModel's from block namespaces, and or their entity values.
 *
 * @author DavixDevelop
 */
public class CubeModelFactory {

    public HashMap<Namespace, ICubeModel> cubeModels;

    public CubeModelFactory(){
        cubeModels = new HashMap<>();
    }

    public ICubeModel fromNamespace(Namespace blockNamespace, EntityValues ...entityValues){
        if(cubeModels.containsKey(blockNamespace))
            return cubeModels.get(blockNamespace).clone();
        else{
            ICubeModel block = getType(blockNamespace);

            if(block instanceof TileEntityCubeModel){
                //Only store tile entity object in memory if it specifies it
                //If it does recreate it every time
                if(((TileEntityCubeModel) block).fromNamespace(blockNamespace, entityValues[0]))
                    cubeModels.put(blockNamespace, block);
            }else if(block instanceof EntityCubeModel){
                //Only store  entity object in memory if it specifies it
                //If it does recreate it every time
                if(((EntityCubeModel) block).fromNamespace(blockNamespace, entityValues[0]))
                    cubeModels.put(blockNamespace, block);
            }
            else {
                //Only store object in memory if to does not have random variants (multiple variants in "variants" field)
                //If it does recreate it every time
                if(block.fromNamespace(blockNamespace))
                    cubeModels.put(blockNamespace, block);
            }

            return block.clone();
        }
    }

    public static ICubeModel getType(Namespace blockNamespace){
        if(blockNamespace.getName().contains("leaves"))
            return new LeavesCubeModel();

        if(blockNamespace.getName().contains("glass_pane"))
            return new GlassPaneCubeModel();

        if(blockNamespace.getName().contains("glass"))
            return new GlassCubeModel();

        if(blockNamespace.getName().contains("slab") && !blockNamespace.getName().contains("double"))
            return new SlabCubeModel();

        if(blockNamespace.getName().contains("stairs"))
            return new StairsCubeModel();

        if(blockNamespace.getName().contains("fence") && !blockNamespace.getName().contains("gate"))
            return new FenceCubeModel();

        if(blockNamespace.getName().contains("command_block"))
            return new CommandCubeModel();

        if(blockNamespace.getName().contains("door"))
            return new DoorCubeModel();

        if(blockNamespace.getType().equals("minecart")){
            return new MinecartEntityCubeModel();
        }

        switch (blockNamespace.getName()){
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
