package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.HashMap;

public class PrismarineCubeModel extends CubeModel {
    @Override
    public boolean fromNamespace(Namespace blockNamespace) {

        HashMap<String,String> modelsMaterials = new HashMap<>();
        CubeModelUtility.generateOrGetMaterial("blocks/prismarine_rough", blockNamespace);
        modelsMaterials.put("all", "blocks/prismarine_rough");

        HashMap<String, CubeElement.CubeFace> cubeFaces = new HashMap<>();

        //Get random portion of the prismarine_rough texture, that has 4 textures on it
        Double[] uv = CubeModelUtility.getRandomUV(4);

        cubeFaces.put("down", new CubeElement.CubeFace(uv, "#all", "down", null, null));
        cubeFaces.put("up", new CubeElement.CubeFace(uv, "#all", "up", null, null));
        cubeFaces.put("north", new CubeElement.CubeFace(uv, "#all", "north", null, null));
        cubeFaces.put("south", new CubeElement.CubeFace(uv, "#all", "south", null, null));
        cubeFaces.put("west", new CubeElement.CubeFace(uv, "#all", "west", null, null));
        cubeFaces.put("east", new CubeElement.CubeFace(uv, "#all", "east", null, null));

        CubeElement cube = new CubeElement(
                new Double[]{0.0,0.0,0.0},
                new Double[]{1.0,1.0,1.0},
                false,
                null,
                cubeFaces);

        //Convert cube to obj
        fromCubes("prismarine", false, null, null, modelsMaterials, cube);

        return true;
    }
}
