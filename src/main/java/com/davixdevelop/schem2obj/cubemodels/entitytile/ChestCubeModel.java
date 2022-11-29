package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.AdjacentBlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;

public class ChestCubeModel extends TileEntityCubeModel implements IAdjacentCheck {
    public static AdjacentBlockState ADJACENT_CHEST_STATES = new AdjacentBlockState("assets/minecraft/chest_states.json");

    //Map<key: variant:type:facing, value: Chest Cube Model>
    public static HashMap<String, ChestCubeModel> CHEST_VARIANTS = new HashMap<>();

    String variant;
    String name;
    String type;
    boolean adjacentCheck;

    public ChestCubeModel(String variant, String chest_name, boolean adjacentCheck){
        this.variant = variant;
        this.name = chest_name;
        this.adjacentCheck = adjacentCheck;

        if(!variant.equals("ender") && Constants.CHRISTMAS_CHEST)
            this.variant = "christmas";
    }

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {

        if(adjacentCheck){
            Namespace modifiedNamespace = blockNamespace.clone();
            //Get modified namespace depending on the adjacent block states
            CubeModelUtility.getAdjacentNamespace_AdjacentState(blockNamespace, modifiedNamespace, ADJACENT_CHEST_STATES, this);

            if(modifiedNamespace.getData().containsKey("type")){
                type = modifiedNamespace.getData("type");
            }else
                type = "single";

        }else
            type = "single";

        String key = getKey(blockNamespace);

        if(CHEST_VARIANTS.containsKey(key)){
            ICubeModel variantObject = CHEST_VARIANTS.get(key);
            copy(variantObject);
        }else{
            toCubeModel(blockNamespace);
            CHEST_VARIANTS.put(key, this);
        }

        return false;
    }

    public String getKey(Namespace blockNamespace){
        return String.format("%s:%s:%s", variant, type, blockNamespace.getData("facing"));
    }

    public void toCubeModel(Namespace namespace){
        ArrayVector.MatrixRotation rotationY = null;

        Double yAngle = Constants.FACING_ROTATION.get(namespace.getData("facing"));

        if(yAngle > 0.0){
            rotationY = new ArrayVector.MatrixRotation(yAngle, "Z");
        }

        BlockModel chestModel = Constants.BLOCK_MODELS.getBlockModel(String.format("%s_chest", type), "builtin").get(0);
        CubeElement[] chestElements = chestModel.getElements().toArray(new CubeElement[0]);

        HashMap<String, String> modelsMaterials = new HashMap<>();

        String materialName;

        if(type.equals("single"))
            materialName = String.format("%s-chest", variant);
        else
            materialName = String.format("%s_double-chest", variant);

        CubeModelUtility.generateOrGetMaterial(String.format("entity/%s", materialName), namespace);
        modelsMaterials.put("chest", String.format("entity/%s", materialName));

        //Convert cube elements to cube model
        fromCubes(String.format("%s-chest", variant), false, null, rotationY, modelsMaterials, chestElements);

    }


    @Override
    public boolean checkCollision(Namespace adjacent, int y_index, String orientation) {
        return adjacent.getName().equals(name);
    }

    @Override
    public void copy(ICubeModel clone) {
        super.copy(clone);

        ChestCubeModel chestClone = (ChestCubeModel) clone;
        variant = chestClone.variant;
        type = chestClone.type;
        name = chestClone.name;
        adjacentCheck = chestClone.adjacentCheck;
    }
}
