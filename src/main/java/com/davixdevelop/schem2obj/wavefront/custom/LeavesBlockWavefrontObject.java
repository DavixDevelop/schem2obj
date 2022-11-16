package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;
import com.davixdevelop.schem2obj.wavefront.material.IMaterial;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LeavesBlockWavefrontObject extends BlockWavefrontObject {

    public static Set<String> MODIFIED_LEAVES_MATERIALS = new HashSet<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        //Get BlockState for the leaves block
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace.getName());

        //Get the variant for the glass block
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);

        ArrayList<VariantModels> leavesModels = new ArrayList<>();

        //Get the models for the glass
        for(BlockState.Variant variant : variants)
            leavesModels.add(new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(variant.getModel())));


        //Modify the leaves materials if they were not yet modified
        modifyLeavesMaterials(leavesModels, blockNamespace);

        //Generate the OBJ for the leaves block variant
        super.toObj(leavesModels, blockNamespace);

        return true;
    }

    public static void modifyLeavesMaterials(ArrayList<VariantModels> leavesModel, Namespace blockNamespace){
        //Extract the texture the leaves use
        String leaves_texture = WavefrontUtility.texturesToMaterials(leavesModel, blockNamespace).entrySet().stream().findFirst().get().getValue().
                entrySet().stream().findFirst().get().getValue();
        if(!MODIFIED_LEAVES_MATERIALS.contains(leaves_texture)){

            IMaterial leaves_material = Constants.BLOCK_MATERIALS.getMaterial(leaves_texture);

            //Color the leaves with the foliage color
            leaves_material.setDiffuseImage(ImageUtility.colorImage(leaves_material.getDiffuseImage(), Constants.BIOME_FOLIAGE_COLOR));

            //Set the material options for leaves
            leaves_material.setSpecularHighlights(0.0);
            leaves_material.setSpecularColor(0.0);
            leaves_material.setIlluminationModel(2);

            leaves_material.setTransparency(true);

            MODIFIED_LEAVES_MATERIALS.add(leaves_texture);
        }
    }

    @Override
    public IWavefrontObject clone() {
        LeavesBlockWavefrontObject clone = new LeavesBlockWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
