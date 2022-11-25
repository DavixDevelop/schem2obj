package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;

/**
 * The CubeModel for the Wall Sign block
 *
 * @author DavixDevelop
 */
public class WallSignCubeModel extends SignCubeModel{

    public static HashMap<String, WallSignCubeModel> WALL_SIGN_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        super.fromNamespace(blockNamespace, entityValues);

        String facing = blockNamespace.getData("facing");
        String key = getKey(facing);

        if(WALL_SIGN_VARIANTS.containsKey(key)){
            ICubeModel variantObject = WALL_SIGN_VARIANTS.get(key);
            super.copy(variantObject);
        }else {
            toCubeModel(facing);
            WALL_SIGN_VARIANTS.put(key, this);
        }

        return false;
    }

    public String getKey(String facing){
        return String.format("%s:%s", getSignText(), facing);
    }

    public void toCubeModel(String facing){
        String signTextMaterial = String.format("entity/sign-%s", getSignText());

        ArrayVector.MatrixRotation rotationY = null;

        Double yAngle = Constants.FACING_ROTATION.get(facing);

        if(yAngle > 0.0)
            rotationY = new ArrayVector.MatrixRotation(yAngle, "Z");

        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("sign", "entity/sign");
        modelsMaterials.put("front", signTextMaterial);


        BlockModel wallSignModel = Constants.BLOCK_MODELS.getBlockModel("wall_sign", "builtin").get(0);
        CubeElement[] signElements = wallSignModel.getElements().toArray(new CubeElement[0]);

        //Convert cube elements to cube model
        fromCubes(String.format("wall_sign_%s", getSignText()), false, null, rotationY, modelsMaterials, signElements);
    }
}
