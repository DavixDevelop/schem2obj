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
 * The CubeModel for the Wall Sign block
 *
 * @author DavixDevelop
 */
public class WallSignCubeModel extends SignCubeModel{

    public static HashMap<String, WallSignCubeModel> WALL_SIGN_VARIANTS = new HashMap<>();

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
        key.put("signText", getSignText());
        key.put("facing", namespace.getDefaultBlockState().getData("facing"));

        return key;
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

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new WallSignCubeModel();
        clone.copy(this);

        return clone;
    }
}
