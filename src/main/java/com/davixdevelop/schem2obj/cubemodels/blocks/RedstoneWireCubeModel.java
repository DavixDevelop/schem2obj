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
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.ImageUtility;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RedstoneWireCubeModel extends CubeModel implements IAdjacentCheck {
    private static AdjacentBlockState ADJACENT_REDSTONE_WIRE_STATES = new AdjacentBlockState("assets/minecraft/redstone_wire_states.json");

    //Map<key: %power:north=(side|up|none),west=(side|up|none),south=(side|up|none),east=(side|up|none) value: Stair Block Wavefront Object>
    private static HashMap<String, RedstoneWireCubeModel> REDSTONE_WIRE_VARIANTS = new HashMap<>();

    private static Set<String> MODIFIED_REDSTONE_WIRE_MATERIALS = new HashSet<>();

    private String power;

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        power = blockNamespace.getData().get("power");

        Namespace modifiedNamespace = blockNamespace.clone();

        //Get modified namespace depending on the adjacent block states
        CubeModelUtility.getAdjacentNamespace_AdjacentState(blockNamespace, modifiedNamespace, ADJACENT_REDSTONE_WIRE_STATES, this);

        //Create key for variant
        String key = getKey(modifiedNamespace);

        //Check if variant of stairs is already in memory
        if (REDSTONE_WIRE_VARIANTS.containsKey(key)) {
            ICubeModel redstone_wire_copy = REDSTONE_WIRE_VARIANTS.get(key).clone();
            copy(redstone_wire_copy);
        } else {
            //Convert modified namespace to cube model
            toCubeModel(modifiedNamespace);
            REDSTONE_WIRE_VARIANTS.put(key, this);
        }

        return false;
    }

    private void toCubeModel(Namespace wireNamespace) {
        setName(wireNamespace.getName());

        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(wireNamespace.getName());
        ArrayList<BlockState.Variant> variants = blockState.getVariants(wireNamespace);

        VariantModels[] wireModels = new VariantModels[variants.size()];
        for(int c = 0; c < variants.size(); c++)
            wireModels[c] = new VariantModels(variants.get(c), Constants.BLOCK_MODELS.getBlockModel(variants.get(c).getModel()));

        HashedDoubleList vertices = new HashedDoubleList();
        ArrayList<Double[]> normalsArray = new ArrayList<>();
        HashedDoubleList textureCoordinates = new HashedDoubleList();
        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = new HashMap<>();

        HashMap<String, HashMap<String, ArrayList<Integer>>> boundingFaces = new HashMap<>();

        HashMap<String, HashMap<String, String>> modelsMaterials = CubeModelUtility.modelsToMaterials(wireModels, wireNamespace);

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

                            Constants.BLOCK_MATERIALS.setMaterial(newTextureName, Constants.BLOCK_MATERIALS.getMaterial(textureName).clone());

                            IMaterial new_material = Constants.BLOCK_MATERIALS.getMaterial(newTextureName);

                            new_material.setName(String.format("%s_power_%s", new_material.getName(), power));

                            Integer p = Integer.valueOf(power);
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
                    if (generatedElements && !elements.isEmpty() && model.getParent() != null)
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
                    CubeModelUtility.convertCubeElementToCubeModel(element, uvLock, rotationX, rotationY, modelsMaterials.get(variant.getModel()), this);

                    //Mark that the variant has generated elements
                    generatedElements = true;
                }
            }
        }
    }

    @Override
    public boolean checkCollision(Namespace adjacent, int y_index, String orientation) {
        if(y_index == 0){
            if(adjacent.getName().contains("repeater") || adjacent.getName().contains("comparator")){
                if(orientation.equals("west") || orientation.equals("east")){
                    if(adjacent.getData().get("facing").equals("west") || adjacent.getData().get("facing").equals("east"))
                        return true;
                    else
                        return false;
                }else {
                    if(adjacent.getData().get("facing").equals("south") || adjacent.getData().get("facing").equals("north"))
                        return true;
                    else
                        return false;
                }

            }

            if ((adjacent.getName().contains("redstone") && (adjacent.getName().endsWith("_wire") || adjacent.getName().endsWith("_block") || adjacent.getName().equals("_torch"))) ||
                    adjacent.getName().equals("lever") ||
                    adjacent.getName().endsWith("pressure_plate") ||
                    adjacent.getName().contains("daylight_detector") ||
                    adjacent.getName().contains("button"))
                return true;
        }else {
            return adjacent.getName().equals("redstone_wire");
        }


        return false;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        return false;
    }

    private String getKey(Namespace wireNamespace) {
        return String.format("%s:north=%s,west=%s,south=%s,east=%s",
                power,
                wireNamespace.getData().get("north"),
                wireNamespace.getData().get("west"),
                wireNamespace.getData().get("south"),
                wireNamespace.getData().get("east"));
    }

    @Override
    public ICubeModel clone() {
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
