package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MinecartEntityCubeModel extends EntityCubeModel {

    public boolean MINECART_MATERIAL_GENERATED = false;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        EntityValues entityValues = namespace.getCustomData();

        List<Float> rotation = entityValues.getFloatList("Rotation");
        toCubeModel(namespace, rotation.get(1), rotation.get(0));


        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        EntityValues entityValues = namespace.getCustomData();
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("EntityName", "minecart");
        key.put("Rotation", entityValues.getFloatList("Rotation"));

        return key;
    }

    public void toCubeModel(Namespace namespace, Float x, Float y){
        ArrayVector.MatrixRotation rotationX = null;
        ArrayVector.MatrixRotation rotationY = null;

        if(x > 0.0f)
            rotationX = new ArrayVector.MatrixRotation(x.doubleValue(), "X");

        if(y > 0.0f)
            rotationY = new ArrayVector.MatrixRotation(y.doubleValue(), "Z");

        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("cart", "entity/minecart");

        if(!MINECART_MATERIAL_GENERATED) {
            CubeModelUtility.generateOrGetMaterial("entity/minecart", namespace);
            MINECART_MATERIAL_GENERATED = true;
        }

        BlockModel minecartModel = Constants.BLOCK_MODELS.getBlockModel("minecart", "builtin").get(0);
        CubeElement[] cartElements = minecartModel.getElements().toArray(new CubeElement[0]);

        //Convert cube element to cube model
        fromCubes("minecart", false, rotationX, rotationY, modelsMaterials, cartElements);
    }

    @Override
    public Double[] getOrigin() {
        return new Double[]{0.5, 0.5, 0.0};
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new MinecartEntityCubeModel();
        clone.copy(this);

        return clone;
    }
}
