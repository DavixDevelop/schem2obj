package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.cubemodels.CubeModelFactory;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.*;

/**
 * The CubeModel for the Glass Pane block
 *
 * @author DavixDevelop
 */
public class GlassPaneCubeModel extends BlockCubeModel implements IAdjacentCheck {
    public static Set<String> MODIFIED_GLASS_MATERIALS = new HashSet<>();

    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Convert namespace to cube model
        super.baseConvert(namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getDefaultBlockState().getName());

        CubeModelUtility.getAdjacentNamespace_NSWE(namespace, this);
        CubeModelUtility.getKey_NSWE(key, namespace);

        return key;
    }

    @Override
    public boolean checkCollision(Namespace adjacentBlock, int y_index, String orientation){
        if(adjacentBlock.getType().contains("glass_pane"))
            return true;

        if(adjacentBlock.getType().equals("glowstone") || adjacentBlock.getType().equals("sea_lantern"))
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
    public ICubeModel duplicate() {
        ICubeModel clone = new GlassPaneCubeModel();
        clone.copy(this);

        return clone;
    }
}
