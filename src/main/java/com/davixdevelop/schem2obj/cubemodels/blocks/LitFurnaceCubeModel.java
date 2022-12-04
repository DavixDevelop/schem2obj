package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for the Lit Furnace block
 *
 * @author DavixDevelop
 */
public class LitFurnaceCubeModel extends BlockCubeModel {
    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Get the BlockState for the lit furnace
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getType());

        //Get the variant of the lit furnace
        BlockState.Variant variant = blockState.getVariants(namespace).get(0);

        ArrayVector.MatrixRotation rotationY = null;

        if (variant != null) {

            //Check if lit furnace model should be rotated
            if (variant.getY() != null)
                rotationY = new ArrayVector.MatrixRotation(variant.getY(), "Z");
        }

        HashMap<String,String> modelsMaterials = new HashMap<>();

        CubeModelUtility.generateOrGetMaterial("blocks/furnace_top", namespace);
        CubeModelUtility.generateOrGetMaterial("blocks/furnace_front_on", namespace);
        CubeModelUtility.generateOrGetMaterial("blocks/furnace_side", namespace);

        modelsMaterials.put("top","blocks/furnace_top");
        modelsMaterials.put("front","blocks/furnace_front_on");
        modelsMaterials.put("side","blocks/furnace_side");

        HashMap<String, CubeElement.CubeFace> cubeOrientable = new HashMap<>();

        //Get random portion of furnace_front_on that contains 6 textures
        Double[] uv = CubeModelUtility.getRandomUV(6);

        cubeOrientable.put("down", new CubeElement.CubeFace(null, "#top", "down", null, null));
        cubeOrientable.put("up", new CubeElement.CubeFace(null, "#top", "up", null, null));
        cubeOrientable.put("north", new CubeElement.CubeFace(uv, "#front", "north", null, null));
        cubeOrientable.put("south", new CubeElement.CubeFace(null, "#side", "south", null, null));
        cubeOrientable.put("west", new CubeElement.CubeFace(null, "#side", "west", null, null));
        cubeOrientable.put("east", new CubeElement.CubeFace(null, "#top", "east", null, null));

        CubeElement cube = new CubeElement(
                new Double[]{0.0,0.0,0.0},
                new Double[]{1.0,1.0,1.0},
                false,
                null,
                cubeOrientable);

        //Convert cube to obj
        fromCubes(namespace.getType(), false, null, rotationY, modelsMaterials, cube);

        return true;
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new LitFurnaceCubeModel();
        clone.copy(this);

        return clone;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getType());
        key.put("facing", namespace.getDefaultBlockState().getData("facing"));

        return key;
    }
}
