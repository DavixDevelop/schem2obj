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
import java.util.List;

public class MinecartEntityCubeModel extends EntityCubeModel {
    public static HashMap<String, MinecartEntityCubeModel> MINECART_ROTATIONS = new HashMap<>();

    public boolean MINECART_MATERIAL_GENERATED = false;

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {

        List<Float> rotation = entityValues.getFloatList("Rotation");

        String key = getKey(rotation.get(0), rotation.get(1));

        if(MINECART_ROTATIONS.containsKey(key)){
            ICubeModel variantObject = MINECART_ROTATIONS.get(key);
            super.copy(variantObject);
        }else {
            toCubeModel(blockNamespace, rotation.get(1), rotation.get(0));
            MINECART_ROTATIONS.put(key, this);
        }


        return false;
    }

    public String getKey(Float x, Float y){
        return String.format("%f:%f", x, y);
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
    public ICubeModel clone() {
        ICubeModel clone = new MinecartEntityCubeModel();
        clone.copy(this);

        return clone;
    }
}
