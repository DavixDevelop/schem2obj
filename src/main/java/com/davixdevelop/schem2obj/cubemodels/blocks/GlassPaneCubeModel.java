package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.cubemodels.CubeModelFactory;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GlassPaneCubeModel extends BlockCubeModel implements IAdjacentCheck {
    //Map<key: %glass_pane_name:north=true|false,south=true|false,east=true|false,west=true|false, value: Glass Pane Cube Model>
    public static HashMap<String, GlassPaneCubeModel> GLASS_PANE_VARIANTS = new HashMap<>();
    public static Set<String> MODIFIED_GLASS_MATERIALS = new HashSet<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        Namespace modifiedNamespace = blockNamespace.clone();

        CubeModelUtility.getAdjacentNamespace_NSWE(modifiedNamespace, this);

        String key =  CubeModelUtility.getKey_NSWE(modifiedNamespace);

        //Check if variant is already in memory
        //Else generate it and store it
        if(GLASS_PANE_VARIANTS.containsKey(key)){
            ICubeModel glass_pane_clone = GLASS_PANE_VARIANTS.get(key).clone();
            copy(glass_pane_clone);
        }else{
            //Convert modified namespace to cube model
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

        return CubeModelFactory.getType(adjacentBlock) instanceof BlockCubeModel;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        //If the adjacent block is the same glass pane type, check for collision
        return getName().equals(adjacent.getName());
    }

    public static void modifyGlassMaterials(VariantModels[] glassModels, Namespace blockNamespace){
        //Extract the textures the pane models use
        HashMap<String, HashMap<String, String>> glassMaterials = CubeModelUtility.modelsToMaterials(glassModels, blockNamespace);

        for(String rootModel : glassMaterials.keySet()) {
            HashMap<String, String> materials = glassMaterials.get(rootModel);

            //Loop through the glass materials and check if it was not yet modified (not in //Generate the cube model for the glass pane variant)
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
    public ICubeModel clone() {
        ICubeModel clone = new GlassPaneCubeModel();
        clone.copy(this);

        return clone;
    }
}
