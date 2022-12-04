package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for the Sea Lantern block
 *
 * @author DavixDevelop
 */
public class SeaLanternCubeModel extends CubeModel {
    @Override
    public boolean fromNamespace(Namespace namespace) {

        HashMap<String,String> modelsMaterials = new HashMap<>();
        CubeModelUtility.generateOrGetMaterial("blocks/sea_lantern", namespace);
        modelsMaterials.put("all", "blocks/sea_lantern");

        HashMap<String, CubeElement.CubeFace> cubeFaces = new HashMap<>();

        //Get random portion of the sea lantern texture, that has 5 textures on it
        Double[] uv = CubeModelUtility.getRandomUV(5);

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
        fromCubes("sea_lantern", false, null, null, modelsMaterials, cube);

        modifySeaLanternMaterial(namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getType());

        return key;
    }

    public void modifySeaLanternMaterial(Namespace blockNamespace){
        CubeModelUtility.generateOrGetMaterial("blocks/sea_lantern", blockNamespace);
        IMaterial sea_lantern_material = Constants.BLOCK_MATERIALS.getMaterial("blocks/sea_lantern");

        sea_lantern_material.setSpecularHighlights(178.5);
        sea_lantern_material.setSpecularColor(0.14);
        sea_lantern_material.setIlluminationModel(2);
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new SeaLanternCubeModel();
        clone.copy(this);

        return clone;
    }
}
