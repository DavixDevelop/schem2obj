package com.davixdevelop.schem2obj.wavefront.custom.entity;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.wavefront.*;
import com.davixdevelop.schem2obj.wavefront.custom.GlassBlockWavefrontObject;

import java.util.HashMap;
import java.util.List;

public class BedWavefrontObject extends TileEntity {

    //Map<key: %color:part:facing, value: Glass Pane Wavefront Object>
    public static HashMap<String, BedWavefrontObject> BED_VARIANTS = new HashMap<>();

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {
        int color = entityValues.getInteger("color");
        String facing = blockNamespace.getData("facing");
        String part = blockNamespace.getData("part");

        String key = getKey(color, part, facing);

        if(!BED_VARIANTS.containsKey(key)){
            toObj(color, part, facing, blockNamespace);
            BED_VARIANTS.put(key, this);
        }else{
            IWavefrontObject variantObject = BED_VARIANTS.get(key);
            super.copy(variantObject);
        }

        return false;
    }

    private String getKey(int color, String part, String facing){
        return String.format("%d:%s:%s",color,part,facing);
    }

    public void toObj(int color, String part, String facing, Namespace namespace){

        String bedMaterial = String.format("entity/%s-bed", Constants.META_COLORS[color]);
        WavefrontUtility.generateOrGetMaterial(bedMaterial, namespace);

        ArrayVector.MatrixRotation rotationY = null;

        Double yAngle = Constants.FACING_ROTATION.get(facing);

        if(yAngle > 0.0)
            rotationY = new ArrayVector.MatrixRotation(yAngle, "Z");

        HashMap<String,String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("bed", bedMaterial);

        CubeElement[] bedElements = new CubeElement[]{};

        if(part.equals("head")){
            List<BlockModel> headModel = Constants.BLOCK_MODELS.getBlockModel("bed_head", "builtin");
            bedElements = new CubeElement[headModel.size()];
            bedElements = headModel.get(0).getElements().toArray(bedElements);
        }else{
            List<BlockModel> footModel = Constants.BLOCK_MODELS.getBlockModel("bed_foot", "builtin");
            bedElements = new CubeElement[footModel.size()];
            bedElements = footModel.get(0).getElements().toArray(bedElements);
        }

        //Convert cube to obj
        createObjFromCube(String.format("%s-bed", Constants.META_COLORS[color]), false, null, rotationY, modelsMaterials, bedElements);

    }

    @Override
    public boolean checkCollision(IWavefrontObject adjacent) {
        //Check for collision if adjacent block is also a bed or a regular block
        if(adjacent instanceof BedWavefrontObject)
            return true;
        else return !WavefrontCollection.isTranslucentOrNotFull(adjacent);
    }

    @Override
    public IWavefrontObject clone() {
        IWavefrontObject clone = new BedWavefrontObject();
        clone.copy(this);

        return clone;
    }
}
