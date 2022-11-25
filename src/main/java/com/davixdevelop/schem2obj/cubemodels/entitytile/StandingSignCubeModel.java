package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;

/**
 * The CubeModel for the Standing Sign block
 *
 * @author DavixDevelop
 */
public class StandingSignCubeModel extends SignCubeModel{

    public static HashMap<String, StandingSignCubeModel> STANDING_SIGN_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        super.fromNamespace(blockNamespace, entityValues);

        String rotationIndex = blockNamespace.getData("rotation");

        String key = getKey(rotationIndex);

        if(STANDING_SIGN_VARIANTS.containsKey(key)){
            ICubeModel variantObject = STANDING_SIGN_VARIANTS.get(key);
            super.copy(variantObject);
        }else {
            toCubeModel(rotationIndex);
            STANDING_SIGN_VARIANTS.put(key, this);
        }

        return false;
    }

    public String getKey(String rotation){
        return String.format("%s:%s", getSignText(), rotation);
    }

    public void toCubeModel(String rotation){
        String signTextMaterial = String.format("entity/sign-%s",getSignText());

        ArrayVector.MatrixRotation rotationY = null;

        int rot = Integer.parseInt(rotation);

        double yAngle = (360 / 16.0) * rot;

        if(yAngle > 0.0)
            rotationY = new ArrayVector.MatrixRotation(yAngle, "Z");

        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("sign", "entity/sign");
        modelsMaterials.put("front", signTextMaterial);


        BlockModel standingSingModel = Constants.BLOCK_MODELS.getBlockModel("standing_sign", "builtin").get(0);
        CubeElement[] signElements = standingSingModel.getElements().toArray(new CubeElement[0]);

        //Convert cube elements to cube model
        fromCubes(String.format("standing_sign_%s", getSignText()), false, null, rotationY, modelsMaterials, signElements);
    }
}
