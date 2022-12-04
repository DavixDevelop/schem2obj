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
 * The CubeModel for the Magma block
 *
 * @author DavixDevelop
 */
public class MagmaCubeModel extends CubeModel {
    
    @Override
    public boolean fromNamespace(Namespace namespace) {
        HashMap<String,String> modelsMaterials = new HashMap<>();
        CubeModelUtility.generateOrGetMaterial("blocks/magma", namespace);
        modelsMaterials.put("all", "blocks/magma");

        HashMap<String, CubeElement.CubeFace> cubeFaces = new HashMap<>();

        //Get random portion of the magma block texture, that has 3 textures on it
        Double[] uv = CubeModelUtility.getRandomUV(3);

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
        fromCubes("magma", false,null,null,modelsMaterials,cube);

        modifyMagmaMaterial(namespace);
        return true;
    }

    public void modifyMagmaMaterial(Namespace blockNamespace){
        CubeModelUtility.generateOrGetMaterial("blocks/magma", blockNamespace);
        IMaterial magma = Constants.BLOCK_MATERIALS.getMaterial("blocks/magma");

        magma.setSpecularHighlights(0.0);
        magma.setSpecularColor(0.0);
        magma.setEmissionStrength(0.0894427);
        magma.setIlluminationModel(2);
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getType());

        return key;
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new MagmaCubeModel();
        clone.copy(this);

        return clone;
    }
}
