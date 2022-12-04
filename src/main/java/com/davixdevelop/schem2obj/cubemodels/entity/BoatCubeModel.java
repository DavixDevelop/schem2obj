package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.*;

public class BoatCubeModel extends EntityCubeModel {

    public static Set<String> GENERATED_MATERIALS = new HashSet<>();

    public boolean onGround = false;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Get entity values from custom data of namespace
        EntityValues entityValues = namespace.getCustomData();

        List<Float> rotation = entityValues.getFloatList("Rotation");
        String type = entityValues.getString("Type").replaceAll("_", "");

        onGround = entityValues.getByte("OnGround") == 1;

        toCubeModel(namespace, type, rotation.get(0), rotation.get(1));

        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        EntityValues entityValues = namespace.getCustomData();
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("EntityName", "boat");
        key.put("Type", entityValues.getString("Type"));
        key.put("Rotation", entityValues.getFloatList("Rotation"));


        return key;
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
    public ICubeModel duplicate() {
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
