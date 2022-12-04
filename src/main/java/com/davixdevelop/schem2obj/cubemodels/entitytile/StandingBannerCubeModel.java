package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for the Standing Banner Block
 *
 * @author DavixDevelop
 */
public class StandingBannerCubeModel extends BannerCubeModel {
    @Override
    public boolean fromNamespace(Namespace namespace) {
        toCubeModel(namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        super.fromNamespace(namespace);

        Map<String, Object> key = new LinkedHashMap<>();
        key.put("EntityTile", namespace.getType());
        key.put("bannerCode", getBannerPatternCode());
        key.put("rotation", namespace.getDefaultBlockState().getData("rotation"));

        return key;
    }

    public void toCubeModel(Namespace namespace){
        String bannerMaterial = String.format("entity/banner-%s", getBannerPatternCode());

        ArrayVector.MatrixRotation rotationY = null;

        if(namespace.getDisplayMode().equals(Namespace.DISPLAY_MODE.BLOCK)) {
            int rot = Integer.parseInt(namespace.getDefaultBlockState().getData("rotation"));

            double yAngle = (360 / 16.0) * rot;

            if (yAngle > 0.0)
                rotationY = new ArrayVector.MatrixRotation(yAngle, "Z");
        }
        else
            rotationY = new ArrayVector.MatrixRotation(180.0, "Z");

        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("banner", bannerMaterial);


        BlockModel bannerModel = Constants.BLOCK_MODELS.getBlockModel("standing_banner", "builtin").get(0);
        CubeElement[] bannerElements = bannerModel.getElements().toArray(new CubeElement[0]);

        //Convert cube elements to cube model
        fromCubes(String.format("standing_banner_%s", getBannerPatternCode()), false, null, rotationY, modelsMaterials, bannerElements);
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new StandingBannerCubeModel();
        clone.copy(this);

        return clone;
    }
}
