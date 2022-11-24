package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;

public class StandingBannerCubeModel extends BannerCubeModel {
    public static HashMap<String, StandingBannerCubeModel> STANDING_BANNER_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        super.fromNamespace(blockNamespace, entityValues);

        String rotationIndex = blockNamespace.getData("rotation");

        String key = getKey(rotationIndex);

        if(STANDING_BANNER_VARIANTS.containsKey(key)){
            ICubeModel variantObject = STANDING_BANNER_VARIANTS.get(key);
            super.copy(variantObject);
        }else {
            toCubeModel(rotationIndex);
            STANDING_BANNER_VARIANTS.put(key, this);
        }


        return false;
    }

    public String getKey(String rotation){
        return String.format("%s:%s", getBannerPatternCode(), rotation);
    }

    public void toCubeModel(String rotation){
        String bannerMaterial = String.format("entity/banner-%s", getBannerPatternCode());

        ArrayVector.MatrixRotation rotationY = null;

        int rot = Integer.parseInt(rotation);

        double yAngle = (360 / 16.0) * rot;

        if(yAngle > 0.0)
            rotationY = new ArrayVector.MatrixRotation(yAngle, "Z");

        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("banner", bannerMaterial);


        BlockModel bannerModel = Constants.BLOCK_MODELS.getBlockModel("standing_banner", "builtin").get(0);
        CubeElement[] bannerElements = bannerModel.getElements().toArray(new CubeElement[0]);

        //Convert cube elements to cube model
        fromCubes(String.format("standing_banner_%s", getBannerPatternCode()), false, null, rotationY, modelsMaterials, bannerElements);
    }
}
