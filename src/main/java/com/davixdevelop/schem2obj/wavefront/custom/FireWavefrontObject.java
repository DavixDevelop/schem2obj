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

public class FireWavefrontObject extends BlockWavefrontObject {
    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        //Get the BlockState for the fire
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(blockNamespace);

        //Get the multipart variants of fire
        ArrayList<BlockState.Variant> variants = blockState.getVariants(blockNamespace);

        ArrayList<VariantModels> fireModels = new ArrayList<>();

        //Get the model/models the multipart variants use
        for(BlockState.Variant variant : variants)
            fireModels.add(new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(blockNamespace, variant)));

        //Modify the uv's of faces to use a random portion of the texture for fire, that consists of 32 textures
        Double[] uv = WavefrontUtility.getRandomUV(32);
        for(int v = 0; v < fireModels.size(); v++) {
            VariantModels models = fireModels.get(v);
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

            fireModels.set(v, new VariantModels(models.getVariant(), blockModels));
        }

        super.toObj(fireModels, blockNamespace);

        return false;
    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        return false;
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new FireWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
