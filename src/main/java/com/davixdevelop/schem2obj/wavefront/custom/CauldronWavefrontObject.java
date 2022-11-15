package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.util.ArrayList;

public class CauldronWavefrontObject extends BlockWavefrontObject {

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        toObj(blockNamespace);
        return false;
    }

    public void toObj(Namespace blockNamespace){
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace);
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);
        BlockState.Variant variant = variants.get(0);

        ArrayList<VariantModels> variantModels = new ArrayList<>();
        variantModels.add(new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(blockNamespace, variant)));

        if(!blockNamespace.getData().get("level").equals("0")){
            VariantModels models = variantModels.get(0);

            ArrayList<BlockModel> blockModels = models.getModels();;

            BlockModel model = blockModels.get(0);

            ArrayList<CubeElement> cubeElements = model.getElements();

            CubeElement cubeElement = cubeElements.get(cubeElements.size() - 1);

            CubeElement.CubeFace cubeFace = cubeElement.getFaces().get("up");

            int selected_portion = new Float(WavefrontUtility.RANDOM.nextFloat() / (1.0 / 32)).intValue();

            Double[] uv = new Double[]{
                    2 / 16.0,
                    (2 / 16.0) * (1 / 32.0),
                    14 / 16.0,
                    (14 / 16.0) * (1 / 32.0)
            };

            if(selected_portion > 0){
                uv[1] += selected_portion * (1 / 32.0);
                uv[3] += selected_portion * (1 / 32.0);
            }

            cubeFace.setUv(uv);

            cubeElements.set(cubeElements.size() - 1, cubeElement);

            model.setElements(cubeElements);

            blockModels.set(0, model);

            variantModels.set(0, new VariantModels(models.getVariant(), blockModels));
        }

        super.toObj(variantModels, blockNamespace);
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new CauldronWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
