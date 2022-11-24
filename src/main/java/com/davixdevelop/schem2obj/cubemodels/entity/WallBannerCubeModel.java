package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;
import java.util.List;

public class WallBannerCubeModel extends BannerCubeModel {

    public static HashMap<String, WallBannerCubeModel> WALL_BANNER_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        super.fromNamespace(blockNamespace, entityValues);

        String facing = blockNamespace.getData("facing");

        String key = getKey(facing);

        if(WALL_BANNER_VARIANTS.containsKey(key)){
            ICubeModel variantObject = WALL_BANNER_VARIANTS.get(key);
            super.copy(variantObject);
        }else
        {
            toCubeModel(facing);
            WALL_BANNER_VARIANTS.put(key, this);
        }


        return false;
    }

    public String getKey(String facing){
        return String.format("%s:%s", getBannerPatternCode(), facing);
    }

    public void toCubeModel(String facing){
        String bannerMaterial = String.format("entity/banner-%s", getBannerPatternCode());

        ArrayVector.MatrixRotation rotationY = null;

        Double yAngle = Constants.FACING_ROTATION.get(facing);

        if(yAngle > 0.0)
            rotationY = new ArrayVector.MatrixRotation(yAngle, "Z");

        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("banner", bannerMaterial);


        BlockModel bannerModel = Constants.BLOCK_MODELS.getBlockModel("mounted_banner", "builtin").get(0);
        CubeElement[] bannerElements = bannerModel.getElements().toArray(new CubeElement[0]);

        //Convert cube elements to cube model
        fromCubes(String.format("wall_banner_%s", getBannerPatternCode()), false, null, rotationY, modelsMaterials, bannerElements);
    }


}
