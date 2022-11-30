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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoatCubeModel extends EntityCubeModel {
    //Map<key: type:xRot:yRot, value: Boat Cube Model Object
    public static HashMap<String, BoatCubeModel> BOAT_VARIANTS = new HashMap<>();

    public static Set<String> GENERATED_MATERIALS = new HashSet<>();

    public boolean onGround = false;

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        List<Float> rotation = entityValues.getFloatList("Rotation");
        String type = entityValues.getString("Type").replaceAll("_", "");

        String key = getKey(type, rotation.get(0), rotation.get(1));

        onGround = entityValues.getByte("OnGround") == 1;

        if(BOAT_VARIANTS.containsKey(key)){
            ICubeModel variantObject = BOAT_VARIANTS.get(key);
            copy(variantObject);
        }else {
            toCubeModel(blockNamespace, type, rotation.get(0), rotation.get(1));
            BOAT_VARIANTS.put(key, this);
        }

        return false;
    }

    public String getKey(String type, Float y, Float x){
        return String.format("%s:%f:%f", type, x, y);
    }

    public void toCubeModel(Namespace namespace, String type, Float y, Float x){
        ArrayVector.MatrixRotation rotationX = null;
        ArrayVector.MatrixRotation rotationY = null;

        if(x > 0.0f)
            rotationX = new ArrayVector.MatrixRotation(x.doubleValue(), "X");

        if(y > 0.0f)
            rotationY = new ArrayVector.MatrixRotation(y.doubleValue(), "Z");

        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("boat", String.format("entity/boat/boat_%s", type));

        if(!GENERATED_MATERIALS.contains(type)){
            CubeModelUtility.generateOrGetMaterial(String.format("entity/boat/boat_%s", type), namespace);
            GENERATED_MATERIALS.add(type);
        }

        BlockModel boatModel = Constants.BLOCK_MODELS.getBlockModel("boat", "builtin").get(0);
        CubeElement[] boatElements = boatModel.getElements().toArray(new CubeElement[0]);

        //Convert cube element to cube model
        fromCubes(String.format("%s-boat", type), false, rotationX, rotationY, modelsMaterials, boatElements);
    }

    @Override
    public Double[] getOrigin() {
        return new Double[]{0.5, 0.5, onGround ? 0.0 : 0.08};
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new BoatCubeModel();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(ICubeModel clone) {
        BoatCubeModel boatClone = (BoatCubeModel) clone;
        onGround = boatClone.onGround;

        super.copy(clone);
    }
}
