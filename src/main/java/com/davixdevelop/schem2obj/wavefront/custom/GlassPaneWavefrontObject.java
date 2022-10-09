package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontCollection;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;
import com.davixdevelop.schem2obj.wavefront.material.IMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GlassPaneWavefrontObject extends BlockWavefrontObject {

    public static HashMap<String, GlassPaneWavefrontObject> GLASS_PANE_VARIANTS = new HashMap<>();
    public static Set<String> MODIFIED_GLASS_MATERIALS = new HashSet<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        Namespace modifiedNamespace = blockNamespace.clone();

        //Check north
        Namespace adjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                Constants.LOADED_SCHEMATIC.getPosX(),
                Constants.LOADED_SCHEMATIC.getPosY(),
                Constants.LOADED_SCHEMATIC.getPosZ() - 1);
        if(adjacentBlock != null){
            if(checkCollision(adjacentBlock))
                modifiedNamespace.getData().put("north", "true");
        }

        //Check south
        adjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                Constants.LOADED_SCHEMATIC.getPosX(),
                Constants.LOADED_SCHEMATIC.getPosY(),
                Constants.LOADED_SCHEMATIC.getPosZ() + 1);
        if(adjacentBlock != null){
            if(checkCollision(adjacentBlock))
                modifiedNamespace.getData().put("south", "true");
        }

        //Check west
        adjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                Constants.LOADED_SCHEMATIC.getPosX() - 1,
                Constants.LOADED_SCHEMATIC.getPosY(),
                Constants.LOADED_SCHEMATIC.getPosZ());
        if(adjacentBlock != null){
            if(checkCollision(adjacentBlock))
                modifiedNamespace.getData().put("west", "true");
        }

        //Check east
        adjacentBlock = Constants.LOADED_SCHEMATIC.getNamespace(
                Constants.LOADED_SCHEMATIC.getPosX() + 1,
                Constants.LOADED_SCHEMATIC.getPosY(),
                Constants.LOADED_SCHEMATIC.getPosZ());
        if(adjacentBlock != null){
            if(checkCollision(adjacentBlock))
                modifiedNamespace.getData().put("east", "true");
        }

        String key = getKey(modifiedNamespace);

        //Check if variant is already in memory
        //Else generate it and store it
        if(GLASS_PANE_VARIANTS.containsKey(key)){
            IWavefrontObject glass_pane_clone = GLASS_PANE_VARIANTS.get(key).clone();
            copy(glass_pane_clone);

        }else{

            //Get BlockState for the glass pane
            BlockState blockState = Constants.BLOCKS_STATES.getBlockState(modifiedNamespace);

            //Get the individual multipart variants from the modified namespace
            ArrayList<BlockState.Variant> variants = blockState.getVariants(modifiedNamespace);

            ArrayList<VariantModels> paneModels = new ArrayList<>();

            //Get the models for each multipart of the pane
            for(BlockState.Variant variant : variants)
                paneModels.add(new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(modifiedNamespace, variant)));


            //Modify the glass materials if they were not yet modified
            modifyGlassMaterials(paneModels, modifiedNamespace);

            //Generate the OBJ for the glass pane variant
            super.toObj(paneModels, variants, modifiedNamespace);


            //Store it in memory for later use
            GLASS_PANE_VARIANTS.put(key, this);
        }

        return false;
    }

    private boolean checkCollision(Namespace adjacentBlock){
        if(adjacentBlock.getName().contains("glass_pane"))
            return true;

        if(adjacentBlock.getName().equals("glowstone") || adjacentBlock.getName().equals("sea_lantern"))
            return false;

        if(adjacentBlock.getDomain().equals("builtin"))
            return false;

        if(WavefrontCollection.getType(adjacentBlock) instanceof BlockWavefrontObject)
            return true;

        return false;
    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        //If the adjacent block is the same glass pane type, check for collision
        return getName().equals(adjacent.getName());
    }

    private String getKey(Namespace blockNamespace){
        return String.format("%s:north=%s,south=%s,east=%s,west=%s",
                blockNamespace.getName(),
                blockNamespace.getData().get("north"),
                blockNamespace.getData().get("south"),
                blockNamespace.getData().get("east"),
                blockNamespace.getData().get("west"));
    }

    public static void modifyGlassMaterials(ArrayList<VariantModels> glassModels, Namespace blockNamespace){
        //Extract the textures the pane models use
        HashMap<String, String> glassMaterials = WavefrontUtility.texturesToMaterials(glassModels, blockNamespace);

        //Loop through the glass materials and check if it was not yet modified (not in //Generate the OBJ for the glass pane variant)
        for(String textureVariable : glassMaterials.keySet()){
            String texture = glassMaterials.get(textureVariable);
            if(!MODIFIED_GLASS_MATERIALS.contains(texture)){
                if(texture.endsWith("glass") || texture.endsWith("glass_pane_top")){
                    //Texture is regular glass
                    IMaterial glass = Constants.BLOCK_MATERIALS.getMaterial(texture);

                    //Set the material options to regular glass
                    glass.setSpecularHighlights(242.25);
                    glass.setAmbientColor(0.2);
                    glass.setSpecularColor(0.03);
                    glass.setIlluminationModel(2);
                }else{
                    //Texture is stained glass
                    IMaterial stained = Constants.BLOCK_MATERIALS.getMaterial(texture);

                    //Set the material options to stained glass
                    stained.setSpecularHighlights(242.25);
                    stained.setAmbientColor(0.2);
                    stained.setSpecularColor(0.03);
                    stained.setIlluminationModel(4);
                    stained.setTransmissionFilter(0.5);
                }

                MODIFIED_GLASS_MATERIALS.add(texture);
            }
        }
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new GlassPaneWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
