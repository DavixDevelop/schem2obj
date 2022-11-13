package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
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

public class GlassPaneWavefrontObject extends BlockWavefrontObject implements IAdjacentCheck {

    //Map<key: %glass_pane_name:north=true|false,south=true|false,east=true|false,west=true|false, value: Glass Pane Wavefront Object>
    public static HashMap<String, GlassPaneWavefrontObject> GLASS_PANE_VARIANTS = new HashMap<>();
    public static Set<String> MODIFIED_GLASS_MATERIALS = new HashSet<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        Namespace modifiedNamespace = blockNamespace.clone();

        WavefrontUtility.getAdjacentNamespace_NSWE(modifiedNamespace, this);

        String key =  WavefrontUtility.getKey_NSWE(modifiedNamespace);

        //Check if variant is already in memory
        //Else generate it and store it
        if(GLASS_PANE_VARIANTS.containsKey(key)){
            IWavefrontObject glass_pane_clone = GLASS_PANE_VARIANTS.get(key).clone();
            copy(glass_pane_clone);
        }else{
            //Convert modified namespace to OBJ
            super.baseConvert(modifiedNamespace);
            //Store it in memory for later use
            GLASS_PANE_VARIANTS.put(key, this);
        }

        return false;
    }

    @Override
    public boolean checkCollision(Namespace adjacentBlock, int y_index, String orientation){
        if(adjacentBlock.getName().contains("glass_pane"))
            return true;

        if(adjacentBlock.getName().equals("glowstone") || adjacentBlock.getName().equals("sea_lantern"))
            return false;

        if(adjacentBlock.getDomain().equals("builtin"))
            return false;

        return WavefrontCollection.getType(adjacentBlock) instanceof BlockWavefrontObject;
    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        //If the adjacent block is the same glass pane type, check for collision
        return getName().equals(adjacent.getName());
    }

    public static void modifyGlassMaterials(ArrayList<VariantModels> glassModels, Namespace blockNamespace){
        //Extract the textures the pane models use
        HashMap<String, HashMap<String, String>> glassMaterials = WavefrontUtility.texturesToMaterials(glassModels, blockNamespace);

        for(String rootModel : glassMaterials.keySet()) {
            HashMap<String, String> materials = glassMaterials.get(rootModel);

            //Loop through the glass materials and check if it was not yet modified (not in //Generate the OBJ for the glass pane variant)
            for (String textureVariable : materials.keySet()) {
                String texture = materials.get(textureVariable);
                if (!MODIFIED_GLASS_MATERIALS.contains(texture)) {
                    if (texture.endsWith("glass") || texture.endsWith("glass_pane_top")) {
                        //Texture is regular glass
                        IMaterial glass = Constants.BLOCK_MATERIALS.getMaterial(texture);

                        //Set the material options to regular glass
                        glass.setSpecularHighlights(242.25);
                        glass.setSpecularColor(0.03);
                        glass.setIlluminationModel(2);
                        glass.setTransparency(true);
                    } else {
                        //Texture is stained glass
                        IMaterial stained = Constants.BLOCK_MATERIALS.getMaterial(texture);

                        //Set the material options to stained glass
                        stained.setSpecularHighlights(242.25);
                        stained.setSpecularColor(0.03);
                        stained.setIlluminationModel(4);
                        stained.setTransmissionFilter(0.5);
                        stained.setTransparency(true);
                    }

                    MODIFIED_GLASS_MATERIALS.add(texture);
                }
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
