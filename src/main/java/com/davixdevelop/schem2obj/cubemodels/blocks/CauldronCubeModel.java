package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for the Cauldron block
 *
 * @author DavixDevelop
 */
public class CauldronCubeModel extends BlockCubeModel {
    @Override
    public boolean fromNamespace(Namespace namespace) {
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getType());
        ArrayList<BlockState.Variant> variants = blockState.getVariants(namespace);
        BlockState.Variant variant = variants.get(0);

        VariantModels[] variantModels = new VariantModels[1];
        variantModels[0] = new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(variant.getModel()));

        if(!namespace.getDefaultBlockState().getData("level").equals("0")){
            VariantModels models = variantModels[0];

            ArrayList<BlockModel> blockModels = models.getModels();;

            BlockModel model = blockModels.get(0);

            ArrayList<CubeElement> cubeElements = model.getElements();

            CubeElement cubeElement = cubeElements.get(cubeElements.size() - 1);

            CubeElement.CubeFace cubeFace = cubeElement.getFaces().get("up");

            int selected_portion = new Float(CubeModelUtility.RANDOM.nextFloat() / (1.0 / 32)).intValue();

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

            variantModels[0] = new VariantModels(models.getVariant(), blockModels);
        }

        fromVariantModel(namespace.getType(), namespace, variantModels);

        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getType());
        key.put("level", namespace.getDefaultBlockState().getData("level"));

        return key;
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new CauldronCubeModel();
        clone.copy(this);

        return clone;
    }
}
