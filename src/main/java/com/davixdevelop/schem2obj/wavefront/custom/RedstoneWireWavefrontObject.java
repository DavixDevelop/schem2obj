package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.AdjacentBlockState;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.wavefront.BlockWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.IWavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;
import com.davixdevelop.schem2obj.wavefront.material.IMaterial;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RedstoneWireWavefrontObject extends BlockWavefrontObject implements IAdjacentCheck {

    private static AdjacentBlockState ADJACENT_REDSTONE_WIRE_STATES = new AdjacentBlockState("assets/minecraft/redstone_wire_states.json");

    //Map<key: %power:north=(side|up|none),west=(side|up|none),south=(side|up|none),east=(side|up|none) value: Stair Block Wavefront Object>
    private static HashMap<String, RedstoneWireWavefrontObject> REDSTONE_WIRE_VARIANTS = new HashMap<>();

    private static Set<String> MODIFIED_REDSTONE_WIRE_MATERIALS = new HashSet<>();

    private String power;

    @Override
    public boolean fromNamespace(Namespace blockNamespace) {
        power = blockNamespace.getData().get("power");

        Namespace modifiedNamespace = blockNamespace.clone();

        //Get modified namespace depending on the adjacent block states
        WavefrontUtility.getAdjacentNamespace_AdjacentState(blockNamespace, modifiedNamespace, ADJACENT_REDSTONE_WIRE_STATES, this);

        //Create key for variant
        String key = getKey(modifiedNamespace);

        //Check if variant of stairs is already in memory
        if (REDSTONE_WIRE_VARIANTS.containsKey(key)) {
            IWavefrontObject redstone_wire_copy = REDSTONE_WIRE_VARIANTS.get(key).clone();
            copy(redstone_wire_copy);
        } else {
            //Convert modified namespace to OBJ
            toObj(modifiedNamespace);
            REDSTONE_WIRE_VARIANTS.put(key, this);
        }

        return false;
    }

    private void toObj(Namespace wireNamespace) {
        setName(wireNamespace.getName());

        BlockState blockState = Constants.BLOCKS_STATES.getBlockState(wireNamespace);
        ArrayList<BlockState.Variant> variants = blockState.getVariants(wireNamespace);
        ArrayList<VariantModels> wireModels = new ArrayList<>();
        for (BlockState.Variant variant : variants)
            wireModels.add(new VariantModels(variant, Constants.BLOCK_MODELS.getBlockModel(wireNamespace, variant)));

        HashedDoubleList vertices = new HashedDoubleList();
        ArrayList<Double[]> normalsArray = new ArrayList<>();
        HashedDoubleList textureCoordinates = new HashedDoubleList();
        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = new HashMap<>();

        HashMap<String, HashMap<String, ArrayList<Integer>>> boundingFaces = new HashMap<>();

        HashMap<String, HashMap<String, String>> modelsMaterials = WavefrontUtility.texturesToMaterials(wireModels, wireNamespace);

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
                            BufferedImage coloredRedstone = ImageUtility.colorImage(new_material.getDiffuseImage(), Constants.REDSTONE_COLORS.get(p));
                            //Set colored image as diffuseImage
                            new_material.setDiffuseImage(coloredRedstone);

                            Constants.BLOCK_MATERIALS.setMaterial(newTextureName, new_material);

                            MODIFIED_REDSTONE_WIRE_MATERIALS.add(newTextureName);
                        }

                        materials.put(textureVariable, newTextureName);
                        Constants.BLOCK_MATERIALS.unsetMaterial(textureName);
                    }
                }

                //Unset the overlay from the used materials
                Constants.BLOCK_MATERIALS.unsetMaterial(materials.get("overlay"));
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
                    WavefrontUtility.convertCubeToWavefront(element, uvLock, rotationX, rotationY, vertices, textureCoordinates, faces, boundingFaces, modelsMaterials.get(variant.getModel()));

                    //Mark that the variant has generated elements
                    generatedElements = true;
                }
            }
        }

        //Create normals for object
        WavefrontUtility.createNormals(normalsArray, vertices, faces);

        //Get vertex list
        ArrayList<Double[]> verticesArray = vertices.toList();

        //Normalize vertex normals
        WavefrontUtility.normalizeNormals(normalsArray);

        setVertices(verticesArray);
        setVertexNormals(normalsArray);
        setTextureCoordinates(textureCoordinates.toList());
        setMaterialFaces(faces);
        setBoundingFaces(boundingFaces);
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
    public boolean checkCollision(IWavefrontObject adjacent) {
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
    public IWavefrontObject clone() {
        IWavefrontObject clone = new RedstoneWireWavefrontObject();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(IWavefrontObject clone) {
        RedstoneWireWavefrontObject redstoneWireCopy = (RedstoneWireWavefrontObject) clone;
        power = redstoneWireCopy.power;

        super.copy(clone);
    }
}
