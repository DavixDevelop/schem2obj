package com.davixdevelop.schem2obj.wavefront;

import com.davixdevelop.schem2obj.blockmodels.BlockModelCollection;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.blockstates.BlockStateCollection;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.wavefront.custom.*;
import com.davixdevelop.schem2obj.wavefront.custom.entity.BedWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.custom.entity.TileEntity;
import com.davixdevelop.schem2obj.wavefront.material.MaterialCollection;

import java.util.HashMap;

public class WavefrontCollection {

    private HashMap<Namespace, IWavefrontObject> wavefrontObjecs;

    public WavefrontCollection(){
        wavefrontObjecs = new HashMap<>();
    }

    public IWavefrontObject fromNamespace(Namespace blockNamespace, EntityValues ...entityValues){
        if(wavefrontObjecs.containsKey(blockNamespace))
            return wavefrontObjecs.get(blockNamespace).clone();
        else{
            IWavefrontObject block = getType(blockNamespace);

            if(block instanceof TileEntity){
                //Only store tile entity object in memory if it specifies it
                //If it does recreate it every time
                if(((TileEntity) block).fromNamespace(blockNamespace, entityValues[0]));
                    wavefrontObjecs.put(blockNamespace, block);
            }else {
                //Only store object in memory if to does not have random variants (multiple variants in "variants" field)
                //If it does recreate it every time
                if(block.fromNamespace(blockNamespace))
                    wavefrontObjecs.put(blockNamespace, block);
            }

            return block.clone();
        }
    }

    public static IWavefrontObject getType(Namespace blockNamespace){
        if(blockNamespace.getName().contains("leaves"))
            return new LeavesBlockWavefrontObject();

        if(blockNamespace.getName().contains("glass_pane"))
            return new GlassPaneWavefrontObject();

        if(blockNamespace.getName().contains("glass"))
            return new GlassBlockWavefrontObject();

        if(blockNamespace.getName().contains("slab") && !blockNamespace.getName().contains("double"))
            return new SlabBlockWavefrontObject();

        if(blockNamespace.getName().contains("stairs"))
            return new StairsBlockWavefrontObject();

        if(blockNamespace.getName().contains("fence") && !blockNamespace.getName().contains("gate"))
            return new FenceWavefrontObject();

        if(blockNamespace.getName().contains("command_block"))
            return new CommandBlockWavefrontObject();

        if(blockNamespace.getName().contains("door"))
            return new DoorWavefrontObject();

        switch (blockNamespace.getName()){
            case "grass":
                return new GrassBlockWavefrontObject();
            case "magma":
                return new MagmaBlockWavefrontObject();
            case "sea_lantern":
                return new SeaLanternWavefrontObject();
            case "prismarine":
                return new PrismarineRoughWavefrontObject();
            case "lit_pumpkin":
                return new LitPumpkinWavefrontObject();
            case "iron_bars":
                return new IronBarsWavefrontObject();
            case "redstone_wire":
                return new RedstoneWireWavefrontObject();
            case "fire":
                return new FireWavefrontObject();
            case "lit_furnace":
                return new LitFurnaceWavefrontObject();
            case "cauldron":
                return new CauldronWavefrontObject();
            case "bed":
                return new BedWavefrontObject();
            default:
                return new BlockWavefrontObject();
        }
    }


    public static boolean isTranslucentOrNotFull(IWavefrontObject object){
        return (object instanceof GlassBlockWavefrontObject) ||
                (object instanceof  GlassPaneWavefrontObject) ||
                (object instanceof IronBarsWavefrontObject) ||
                (object instanceof LeavesBlockWavefrontObject) ||
                (object instanceof SlabBlockWavefrontObject) ||
                (object instanceof StairsBlockWavefrontObject) ||
                (object instanceof FenceWavefrontObject) ||
                (object instanceof RedstoneWireWavefrontObject) ||
                (object instanceof DoorWavefrontObject) ||
                (object instanceof FireWavefrontObject) ||
                (object instanceof CauldronWavefrontObject) ||
                object.getName().equals("slime") ||
                object.getName().contains("rail") ||
                object.getName().contains("button") ||
                object.getName().equals("grass_path") ||
                object.getName().contains("pressure_plate") ||
                object.getName().contains("daylight_detector") ||
                object.getName().contains("piston_head") ||
                object.getName().contains("comparator") ||
                object.getName().contains("repeater") ||
                object.getName().equals("snow_layer");
    }
}
