package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.Orientation;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.blockstates.BlockState;
import com.davixdevelop.schem2obj.models.HashedStringList;
import com.davixdevelop.schem2obj.models.VariantModels;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.*;

public class CubeModel implements ICubeModel {
    private String name;
    private HashedStringList materials;
    private List<ICube> cubes;

    public CubeModel(){
        materials = new HashedStringList();
        cubes = new ArrayList<>();
    }

    @Override
    public boolean fromNamespace(Namespace namespace) {
        return false;
    }

    @Override
    public boolean checkCollision(ICubeModel adjacent) {
        //If the adjacent cube model a translucent or not full cube model, don't check for collision
        return !CubeModelFactory.isTranslucentOrNotFull(adjacent);
    }

    @Override
    public void deleteFaces(Orientation orientation) {
        for(ICube cube : cubes){
            cube.deleteFace(orientation);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCube(ICube cube) {
        cubes.add(cube);
    }

    @Override
    public List<ICube> getCubes() {
        return cubes;
    }

    @Override
    public HashedStringList getMaterials() {
        return materials;
    }

    public Integer putMaterial(String material) {
        return  materials.put(material);
    }

    @Override
    public ICubeModel clone() {
        CubeModel cubeModel = new CubeModel();
        cubeModel.copy(this);

        return cubeModel;
    }

    @Override
    public void copy(ICubeModel clone) {
        CubeModel cubeClone = (CubeModel) clone;
        name = cubeClone.name;
        materials = cubeClone.materials.clone();
        cubes = new ArrayList<>();
        for(ICube cube : cubeClone.cubes){
            cubes.add(cube.clone());
        }

    }

    public void fromVariantModel(String name, Namespace namespace, VariantModels... blockModels){
        //Set the name to the cube model
        setName(name);

        //Extract the materials from the textures that the models use
        HashMap<String, HashMap<String, String>> modelsMaterials = CubeModelUtility.modelsToMaterials(blockModels, namespace);

        //Set the the materials to the cube
        materials = new HashedStringList();
        for(String modelName : modelsMaterials.keySet()){
            HashMap<String,String> modelMaterials = modelsMaterials.get(modelName);
            for(String textureVariable : modelMaterials.keySet())
                materials.put(modelMaterials.get(textureVariable));
        }

        for(VariantModels variantModels : blockModels){
            //Minecraft usually parses the block models from bottom to top (ex. from block -> dirt)
            //This approach overrides the generated elements, if the current model has a parents and elements
            //In other words if the current model has a parent and elements, ignore the previous generated elements and
            //Use the current model elements as the block elements
            //To emulate this approach when parsing the models from top to bottom (ex. from dirt -> block),
            //to skip generating elements that would be discarded (written over) later, we need to
            //check if the variant elements were already generated (mark it), and the current model has an parent and elements
            //If it does, skip the current model, else generate the elements.
            boolean generatedElements = false;

            for (BlockModel model : variantModels.getModels()) {
                ArrayList<CubeElement> elements = model.getElements();
                BlockState.Variant variant = variantModels.getVariant();

                if (!elements.isEmpty()) {

                    //Ignore the model (m) elements, if the model has elements and a parent and the elements were already set
                    if (generatedElements && model.getParent() != null)
                        continue;

                    //Each element represents a cube
                    for (CubeElement element : elements) {
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
                    }

                    //Mark that the variant has generated elements
                    generatedElements = true;
                }
            }
        }
    }

    /**
     * Create the cube model from one or more cubes
     * @param name The name of the cube model
     * @param uvLock Set to true, to keep the uv's in place, and just rotate the cube
     * @param rotationX RotationMatrix around the X axis
     * @param rotationY RotationMatrix around the Z axis
     * @param modelsMaterials A map of texture variables and the material it's set to, ex key: #north, value: block/dirt
     * @param cubeElements Cube element/element's to create a obj from
     */
    public void fromCubes(String name, Boolean uvLock, ArrayVector.MatrixRotation rotationX, ArrayVector.MatrixRotation rotationY,HashMap<String,String> modelsMaterials, CubeElement ...cubeElements){
        setName(name);

        //Set the materials the cubes to the cube model
        materials = new HashedStringList();
        for(String textureVariable : modelsMaterials.keySet())
            materials.put(modelsMaterials.get(textureVariable));

        //Convert the cubes to wavefront
        for(CubeElement cube : cubeElements) {
            CubeModelUtility.convertCubeElementToCubeModel(cube, uvLock, rotationX, rotationY, modelsMaterials, this);
        }
    }

}
