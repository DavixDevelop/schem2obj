package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.*;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The CubeModel for all Bed blocks
 *
 * @author DavixDevelop
 */
public class BedCubeModel extends TileEntityCubeModel {

    @Override
    public boolean fromNamespace(Namespace namespace) {
        EntityValues entityValues = namespace.getCustomData();
        toCubeModel(entityValues.getInteger("color"), namespace.getDefaultBlockState().getData("part"), namespace.getDefaultBlockState().getData("facing"), namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        EntityValues entityValues = namespace.getCustomData();

        Map<String, Object> key = new LinkedHashMap<>();
        key.put("EntityTile", namespace.getType());
        key.put("color", entityValues.getInteger("color"));
        key.put("facing", namespace.getDefaultBlockState().getData("facing"));
        key.put("part", namespace.getDefaultBlockState().getData("part"));

        return key;
    }

    public void toCubeModel(int color, String part, String facing, Namespace namespace){

        String bedMaterial = String.format("entity/%s-bed", Constants.META_COLORS[color]);
        CubeModelUtility.generateOrGetMaterial(bedMaterial, namespace);

        ArrayVector.MatrixRotation rotationY = null;

        Double yAngle = Constants.FACING_ROTATION.get(facing);

        if(yAngle > 0.0)
            rotationY = new ArrayVector.MatrixRotation(yAngle, "Z");

        HashMap<String,String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("bed", bedMaterial);

        CubeElement[] bedElements;

        if(part.equals("head")){
            List<BlockModel> headModel = Constants.BLOCK_MODELS.getBlockModel("bed_head", "builtin");
            bedElements = new CubeElement[headModel.size()];
            bedElements = headModel.get(0).getElements().toArray(bedElements);
        }else{
            List<BlockModel> footModel = Constants.BLOCK_MODELS.getBlockModel("bed_foot", "builtin");
            bedElements = new CubeElement[footModel.size()];
            bedElements = footModel.get(0).getElements().toArray(bedElements);
        }

        //Convert cube to cube model
        fromCubes(String.format("%s-bed", Constants.META_COLORS[color]), false, null, rotationY, modelsMaterials, bedElements);

        if(namespace.getDisplayMode().equals(Namespace.DISPLAY_MODE.FIXED)){
            Double[] translate = new Double[]{0.0, -1.0, 0.0};
            //Only Shift the "foot" towards the south by -1.0
            if(namespace.getDefaultBlockState().getData("part").equals("foot"))
                CubeModelUtility.translateCubeModel(this, translate);


            Namespace namespace1 = namespace.duplicate();

            if(namespace1.getDefaultBlockState().getData("part").equals("foot")) {
                translate[1] = 0.0;
                namespace1.setDefaultBlockState(10);
            }else
                //Move the foot part to the south by 1.0
                namespace1.setDefaultBlockState(2);


            BedCubeModel secondPart = new BedCubeModel();
            namespace1.setDisplayMode(Namespace.DISPLAY_MODE.BLOCK);
            secondPart.fromNamespace(namespace1);

            CubeModelUtility.translateCubeModel(secondPart, translate);

            //Append the head part cubes to the item model
            appendCubeModel(secondPart);
        }

    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        //Check for collision if adjacent block is also a bed or a regular block
        if(adjacent instanceof BedCubeModel)
            return true;
        else return !CubeModelFactory.isTranslucentOrNotFull(adjacent);
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new BedCubeModel();
        clone.copy(this);

        return clone;
    }
}
