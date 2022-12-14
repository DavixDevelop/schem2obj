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
 * The CubeModel for the Fire block
 *
 * @author DavixDevelop
 */
public class FireCubeModel extends BlockCubeModel{
    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Get the BlockState for the fire
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getType());

        //Get the multipart variants of fire
        ArrayList<BlockState.Variant> variants = blockState.getVariants(namespace);

        VariantModels[] fireModels = new VariantModels[variants.size()];

        //Get the model/models the multipart variants use
        for(int c = 0; c < variants.size(); c++)
            fireModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));

        //Modify the uv's of faces to use a random portion of the texture for fire, that consists of 32 textures
        Double[] uv = CubeModelUtility.getRandomUV(32);
        for(int v = 0; v < fireModels.length; v++) {
            VariantModels models = fireModels[v];
            ArrayList<BlockModel> blockModels = models.getModels();

            for(int c = 0; c <blockModels.size(); c++){
                BlockModel model = blockModels.get(c);

                ArrayList<CubeElement> cubeElements = model.getElements();

                for(int d = 0; d < cubeElements.size(); d++){
                    CubeElement cubeElement = cubeElements.get(d);

                    for(String side : cubeElement.getFaces().keySet()){
                        CubeElement.CubeFace cubeFace = cubeElement.getFaces().get(side);
                        cubeFace.setUv(uv);
                    }

                    cubeElements.set(d, cubeElement);
                }

                model.setElements(cubeElements);

                blockModels.set(c, model);
            }

            fireModels[v] = new VariantModels(models.getVariant(), blockModels);
        }

        fromVariantModel(namespace.getType(), namespace, fireModels);

        return false;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getType());

        return key;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        return false;
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new FireCubeModel();
        clone.copy(this);

        return clone;
    }
}
