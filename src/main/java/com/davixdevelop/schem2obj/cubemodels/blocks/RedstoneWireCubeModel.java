package com.davixdevelop.schem2obj.cubemodels.blocks;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.AdjacentBlockState;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.cubemodels.CubeModel;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.IAdjacentCheck;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.ImageUtility;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * The CubeModel for the Redstone Wire block
 *
 * @author DavixDevelop
 */
public class RedstoneWireCubeModel extends CubeModel implements IAdjacentCheck {
    static AdjacentBlockState ADJACENT_REDSTONE_WIRE_STATES = new AdjacentBlockState("assets/schem2obj/redstone_wire_states.json");
    static Set<String> MODIFIED_REDSTONE_WIRE_MATERIALS = new HashSet<>();

    private String power;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        //Convert modified namespace to cube model
        toCubeModel(namespace);

        return true;
    }


    private void toCubeModel(Namespace namespace) {
        setName(namespace.getDefaultBlockState().getName());

        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(namespace.getDefaultBlockState().getName());
        ArrayList<BlockState.Variant> variants = blockState.getVariants(namespace);

        VariantModels[] wireModels = new VariantModels[variants.size()];
        for(int c = 0; c < variants.size(); c++)
            wireModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));

        HashMap<String, HashMap<String, String>> modelsMaterials = CubeModelUtility.modelsToMaterials(wireModels, namespace);

        //Loop through the models
        for (String rootModel : modelsMaterials.keySet()) {
            HashMap<String, String> materials = modelsMaterials.get(rootModel);

            //Check of root model materials has an overlay texture variable
            if (materials.containsKey("overlay")) {

                //Loop through the materials for the rootModel
                for (String textureVariable : materials.keySet()) {
                    //Check if texture variable is not a overlay
                    if (!textureVariable.equals("overlay")) {
                        String textureName = materials.get(textureVariable);

                        //Check if modified redstone wire material is already generated,
                        //else, generate it
                        String newTextureName = String.format("%s_power_%s", textureName, power);
                        if (!MODIFIED_REDSTONE_WIRE_MATERIALS.contains(newTextureName)) {

                            Constants.BLOCK_MATERIALS.setMaterial(newTextureName, Constants.BLOCK_MATERIALS.getMaterial(textureName).duplicate());

                            IMaterial new_material = Constants.BLOCK_MATERIALS.getMaterial(newTextureName);

                            new_material.setName(String.format("%s_power_%s", new_material.getName(), power));

                            int p = Integer.parseInt(power);
                            new_material.setEmissionStrength((p == 0) ? 0.0 :  p / 16.0);

                            //Color the gray overlay with the redstone wire color
                            BufferedImage coloredRedstone = ImageUtility.colorImage(new_material.getDefaultDiffuseImage(), Constants.REDSTONE_COLORS.get(p));
                            //Set colored image as diffuseImage
                            new_material.setDiffuseImage(coloredRedstone);

                            Constants.BLOCK_MATERIALS.setMaterial(newTextureName, new_material);

                            MODIFIED_REDSTONE_WIRE_MATERIALS.add(newTextureName);
                        }

                        materials.put(textureVariable, newTextureName);
                        Constants.BLOCK_MATERIALS.unsetUsedMaterial(textureName);
                    }
                }

                //Unset the overlay from the used materials
                Constants.BLOCK_MATERIALS.unsetUsedMaterial(materials.get("overlay"));
            }
        }

        for (VariantModels variantModels : wireModels) {
            boolean generatedElements = false;

            for (BlockModel model : variantModels.getModels()) {
                ArrayList<CubeElement> elements = model.getElements();
                BlockState.Variant variant = variantModels.getVariant();

                if (!elements.isEmpty()) {

                    //Ignore the model (m) elements, if the model has elements and a parent and the elements were already set
                    if (generatedElements && model.getParent() != null)
                        continue;

                    //Only convert the first element (avoid the overlay element)
                    CubeElement element = elements.get(0);

                    boolean uvLock = false;
                    ArrayVector.MatrixRotation rotationX = null;
                    ArrayVector.MatrixRotation rotationY = null;

                    if (variant != null) {
                        uvLock = variantModels.getVariant().getUvlock();

                        //Check if variant model should be rotated
                        if (variant.getX() != null)
                            rotationX = new ArrayVector.MatrixRotation(variant.getX(), "X");

                        if (variant.getY() != null)
                            rotationY = new ArrayVector.MatrixRotation(variant.getY(), "Z");
                    }


                    //Convert the cube to obj
                    if(variant != null) {
                        CubeModelUtility.convertCubeElementToCubeModel(element, uvLock, rotationX, rotationY, modelsMaterials.get(variant.getModel()), this);

                        //Mark that the variant has generated elements
                        generatedElements = true;
                    }
                }
            }
        }
    }

    @Override
    public boolean checkCollision(Namespace adjacent, int y_index, String orientation) {
        if(y_index == 0){
            if(adjacent.getType().contains("repeater") || adjacent.getType().contains("comparator")){
                if(orientation.equals("west") || orientation.equals("east")){
                    return adjacent.getDefaultBlockState().getData().get("facing").equals("west") || adjacent.getDefaultBlockState().getData().get("facing").equals("east");
                }else {
                    return adjacent.getDefaultBlockState().getData().get("facing").equals("south") || adjacent.getDefaultBlockState().getData().get("facing").equals("north");
                }

            }

            return (adjacent.getType().contains("redstone") && (adjacent.getType().endsWith("_wire") || adjacent.getType().endsWith("_block") || adjacent.getType().endsWith("_torch"))) ||
                    adjacent.getType().equals("lever") ||
                    adjacent.getType().endsWith("pressure_plate") ||
                    adjacent.getType().contains("daylight_detector") ||
                    adjacent.getType().contains("button");
        }else {
            return adjacent.getType().equals("redstone_wire");
        }
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        return false;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();

        power = namespace.getDefaultBlockState().getData().get("power");
        //Get modified namespace depending on the adjacent block states
        CubeModelUtility.getAdjacentNamespace_AdjacentState(namespace, ADJACENT_REDSTONE_WIRE_STATES, this);


        key.put("BlockName", namespace.getType());
        key.put("power", namespace.getDefaultBlockState().getData("power"));
        key.put("north", namespace.getDefaultBlockState().getData("north"));
        key.put("south", namespace.getDefaultBlockState().getData("south"));
        key.put("west", namespace.getDefaultBlockState().getData("west"));
        key.put("east", namespace.getDefaultBlockState().getData("east"));

        return key;
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new RedstoneWireCubeModel();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(ICubeModel clone) {
        RedstoneWireCubeModel redstoneWireCopy = (RedstoneWireCubeModel) clone;
        power = redstoneWireCopy.power;

        super.copy(clone);
    }
}
