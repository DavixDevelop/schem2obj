package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.materials.SEUSMaterial;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CubeModel for the Lit Pumpkin block
 *
 * @author DavixDevelop
 */
public class LitPumpkinCubeModel extends CubeModel {
    private static boolean LIT_PUMPKIN_MATERIALS_GENERATED = false;
    private static int STACK_SIZE = -1;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Get BlockState for the lit_pumpkin
        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getType());

        //Get the variant for the lit pumpkin block
        BlockState.Variant variant = blockState.getVariants(namespace).get(0);

        ArrayVector.MatrixRotation rotationY = null;
        //Check if variant need's to be rotated
        if(variant.getY() != null)
            rotationY = new ArrayVector.MatrixRotation(variant.getY(), "Z");

        HashMap<String,String> modelsMaterials = new HashMap<>();
        modifyLitPumpkinMaterials(namespace);

        modelsMaterials.put("front", "blocks/pumpkin_face_on");
        modelsMaterials.put("top", "blocks/lit_pumpkin_top");
        modelsMaterials.put("side", "blocks/lit_pumpkin_side");

        IMaterial pumpkin_face_on = Constants.BLOCK_MATERIALS.getMaterial("blocks/pumpkin_face_on");

        if(pumpkin_face_on instanceof SEUSMaterial){
            SEUSMaterial seusMaterial = (SEUSMaterial) pumpkin_face_on.duplicate();
            String metaPath = ResourceLoader.getResourcePath("textures", "blocks/pumpkin_face_on", "png.mcmeta");
            if(ResourceLoader.resourceExists(metaPath)){
                if(STACK_SIZE == -1){
                    //Find out number of textures in the animation texture for pumpkin_face_on
                    try{
                        BufferedImage diffuseImage = seusMaterial.getDefaultDiffuseImage();
                        STACK_SIZE = diffuseImage.getHeight() / diffuseImage.getWidth();
                    }catch (Exception ex){
                        LogUtility.Log("Error while calculating count of textures in the animation texture for pumpkin_face_on");
                        LogUtility.Log(ex.getMessage());
                    }
                }
            }
        }

        Double[] northUV = new Double[]{0.0, 0.0, 1.0, 1.0};

        //If pumpkin_face_on texture has multiple textures use the one at the top
        if(STACK_SIZE != -1){
            northUV[1] = 1.0 / STACK_SIZE;
        }

        HashMap<String, CubeElement.CubeFace> cubeFaces = new HashMap<>();
        cubeFaces.put("down", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#top", "down", null, null));
        cubeFaces.put("up", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#top", "up", null, null));
        cubeFaces.put("north", new CubeElement.CubeFace(northUV, "#front", "north", null, null));
        cubeFaces.put("south", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#side", "south", null, null));
        cubeFaces.put("west", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#side", "west", null, null));
        cubeFaces.put("east", new CubeElement.CubeFace(new Double[]{0.0, 0.0, 1.0, 1.0}, "#side", "east", null, null));

        CubeElement cube = new CubeElement(
                new Double[]{0.0,0.0,0.0},
                new Double[]{1.0,1.0,1.0},
                false,
                null,
                cubeFaces);

        //Convert cube to obj
        fromCubes("lit_pumpkin", false, null, rotationY, modelsMaterials, cube);
        return true;
    }

    public void modifyLitPumpkinMaterials(Namespace blockNamespace){
        if(!LIT_PUMPKIN_MATERIALS_GENERATED){
            CubeModelUtility.generateOrGetMaterial("blocks/pumpkin_face_on", blockNamespace);
            CubeModelUtility.generateOrGetMaterial("blocks/pumpkin_side", blockNamespace);
            CubeModelUtility.generateOrGetMaterial("blocks/pumpkin_top", blockNamespace);

            IMaterial pumpkin_side = Constants.BLOCK_MATERIALS.getMaterial("blocks/pumpkin_side").duplicate();
            IMaterial pumpkin_top = Constants.BLOCK_MATERIALS.getMaterial("blocks/pumpkin_top").duplicate();
            IMaterial pumpkin_face_on = Constants.BLOCK_MATERIALS.getMaterial("blocks/pumpkin_face_on");

            pumpkin_side.setName("lit_pumpkin_side");
            pumpkin_top.setName("lit_pumpkin_top");

            Constants.BLOCK_MATERIALS.setMaterial("blocks/lit_pumpkin_side",pumpkin_side);
            Constants.BLOCK_MATERIALS.setMaterial("blocks/lit_pumpkin_top", pumpkin_top);

            IMaterial lit_pumpkin_side = Constants.BLOCK_MATERIALS.getMaterial("blocks/lit_pumpkin_side");
            IMaterial lit_pumpkin_top = Constants.BLOCK_MATERIALS.getMaterial("blocks/lit_pumpkin_top");

            if(lit_pumpkin_side instanceof SEUSMaterial)
                lit_pumpkin_side.setEmissionStrength(0.0);
            else
                lit_pumpkin_side.setEmissionStrength(15 / 16.0);

            if(lit_pumpkin_top instanceof SEUSMaterial)
                lit_pumpkin_top.setEmissionStrength(0.0);
            else
                lit_pumpkin_top.setEmissionStrength(15 / 16.0);

            if(pumpkin_face_on instanceof SEUSMaterial){
                ((SEUSMaterial)pumpkin_face_on).setEmissionMixFactor(0.0);
            }

            pumpkin_face_on.setEmissionStrength(15 / 16.0);



            LIT_PUMPKIN_MATERIALS_GENERATED = true;
        }
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("BlockName", namespace.getType());
        key.put("facing", namespace.getDefaultBlockState().getData("facing"));

        return key;
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new LitPumpkinCubeModel();
        clone.copy(this);

        return clone;
    }
}
