package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for the Wall Banner block
 *
 * @author DavixDevelop
 */
public class WallBannerCubeModel extends BannerCubeModel {

    public static HashMap<String, WallBannerCubeModel> WALL_BANNER_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        toCubeModel(blockNamespace.getDefaultBlockState().getData("facing"));
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        super.fromNamespace(namespace);

        Map<String, Object> key = new LinkedHashMap<>();
        key.put("EntityTile", namespace.getType());
        key.put("bannerCode", getBannerPatternCode());
        key.put("facing", namespace.getDefaultBlockState().getData("facing"));

        return key;
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

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new WallBannerCubeModel();
        clone.copy(this);

        return clone;
    }


}
