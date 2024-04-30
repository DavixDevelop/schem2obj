package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModelFactory;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.cubemodels.entitytile.SkullCubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayUtility;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.BlockModelUtility;

import java.util.*;

public class ArmorStandModel extends EntityCubeModel {

    private static Map<Object, ICubeModel> ARMORS = new LinkedHashMap<>();
    private static Double SKULL_SCALE = 1.125;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        toCubeModel(namespace);
        return true;
    }

    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        EntityValues entityValues = namespace.getCustomData();

        List<Float> rotation = entityValues.getFloatList("Rotation");

        Map<String, Object> key = new LinkedHashMap<>();
        key.put("EntityName", "armor_stand");
        key.put("Rotation", rotation.get(0));

        EntityValues pose = entityValues.getEntityValues("Pose");
        appendPoseToKey("Body", key, pose);
        appendPoseToKey("Head", key, pose);
        appendPoseToKey("LeftArm", key, pose);
        appendPoseToKey("LeftLeg", key, pose);
        appendPoseToKey("RightArm", key, pose);
        appendPoseToKey("RightLeg", key, pose);

        key.put("ShowArms", entityValues.getByte("ShowArms"));
        key.put("ArmorItems", entityValues.getList("ArmorItems"));

        key.put("StandInvisible", entityValues.getByte("Invisible"));

        return key;
    }

    public void appendPoseToKey(String poseName, Map<String, Object> key, EntityValues pose){
        if(pose.containsKey(poseName)){
            key.put(poseName, pose.getFloatList(poseName));
        }
    }

    public void toCubeModel(Namespace namespace){
        EntityValues entityValues = namespace.getCustomData();

        List<Float> rotation = entityValues.getFloatList("Rotation");

        Double rotationAngle = null;


        if(rotation.get(0) > 0.0f)
            rotationAngle = rotation.get(0).doubleValue();

        HashMap<String, String> modelMaterials = new HashMap<>();
        modelMaterials.put("armorstand", "entity/armorstand/wood");

        CubeModelUtility.generateOrGetMaterial(modelMaterials.get("armorstand"), namespace);

        BlockModel armorStandModel = Constants.BLOCK_MODELS.getBlockModel("armor_stand", "builtin").get(0);



        EntityValues pose = entityValues.getEntityValues("Pose");
        if(!pose.isEmpty()){
            ArrayList<CubeElement> cubeElements = armorStandModel.getElements();
            poseStand(pose, cubeElements);
        }

        CubeElement leftArmElement = armorStandModel.getElements().get(7);
        CubeElement rightArmElement = armorStandModel.getElements().get(8);

        byte showArms = entityValues.getByte("ShowArms");
        if(showArms == 0){
            ArrayList<CubeElement> cubeElements = armorStandModel.getElements();
            cubeElements.remove(cubeElements.size() - 1);
            cubeElements.remove(cubeElements.size() - 1);

            armorStandModel.setElements(cubeElements);
        }

        CubeElement[] armorStandElements = armorStandModel.getElements().toArray(new CubeElement[0]);

        if(entityValues.getByte("Invisible") == 0)
            fromCubes("armor-stand", false, null, null, modelMaterials, armorStandElements);
        else
            fromName("armor-stand", modelMaterials);

        //Add stand to model
        BlockModel standModel = Constants.BLOCK_MODELS.getBlockModel("armor_stand_plate", "builtin").get(0);
        CubeElement standElement = standModel.getElements().get(0);
        CubeModelUtility.convertCubeElementToCubeModel(standElement, false, null, null, modelMaterials, this);


        List<?> armorItems =  entityValues.getList("ArmorItems");
        addHelmet(armorItems, pose, armorStandElements[6]);
        addChestplate(armorItems, armorStandElements[2], leftArmElement, rightArmElement);
        addLeggings(armorItems, armorStandElements[2], armorStandElements[0], armorStandElements[1]);
        addBoots(armorItems, armorStandElements[0], armorStandElements[1]);

        if(rotationAngle != null)
            CubeModelUtility.rotateCubeModel(this, new Double[]{0.0, 0.0, rotationAngle}, getOrigin());
    }


    public void addHelmet(List<?> armorItems, EntityValues pose, CubeElement headElement){
        EntityValues helmetItem = (EntityValues) armorItems.get(3);
        if(!helmetItem.isEmpty()){
            List<Float> rotation = null;
            if(pose.containsKey("Head"))
                rotation = pose.getFloatList("Head");

            List<CubeElement> armorHeadElement =new ArrayList<>();
            armorHeadElement.add(headElement);

            ICubeModel helmetCubeModel = getArmorCubeModel(helmetItem, armorHeadElement);

            CubeElement.CubeRotation cubeRotation = headElement.getRotation();
            if(helmetCubeModel instanceof SkullCubeModel){
                if(rotation != null)
                    CubeModelUtility.rotateCubeModel(helmetCubeModel, new Double[]{-rotation.get(0).doubleValue(), -rotation.get(2).doubleValue(), rotation.get(1).doubleValue()}, new Double[]{0.5, 0.5, 0.0});

                Double[] translate = ArrayVector.subtract(cubeRotation.getOrigin(), new Double[]{0.5, 0.5, 0.0});
                //Move skull up to the head
                CubeModelUtility.translateCubeModel(helmetCubeModel, translate);
            }



            appendCubeModel(helmetCubeModel);
        }
    }

    public void addChestplate(List<?> armorItems, CubeElement hipElement, CubeElement leftArm, CubeElement rightArm){
        EntityValues chestplateItem = (EntityValues) armorItems.get(2);
        if(!chestplateItem.isEmpty()){
            List<CubeElement> armorBodyElements =new ArrayList<>();
            armorBodyElements.add(hipElement);
            armorBodyElements.add(leftArm);
            armorBodyElements.add(rightArm);

            ICubeModel chestplateCubeModel = getArmorCubeModel(chestplateItem, armorBodyElements);

            appendCubeModel(chestplateCubeModel);
        }
    }

    public void addLeggings(List<?> armorItems, CubeElement hipElement, CubeElement leftLeg, CubeElement rightLeg){
        EntityValues leggingsItem = (EntityValues) armorItems.get(1);
        if(!leggingsItem.isEmpty()){
            List<CubeElement> armorLegElements =new ArrayList<>();
            armorLegElements.add(hipElement);
            armorLegElements.add(leftLeg);
            armorLegElements.add(rightLeg);

            ICubeModel leggingsCubeModel = getArmorCubeModel(leggingsItem, armorLegElements);

            appendCubeModel(leggingsCubeModel);
        }
    }

    public void addBoots(List<?> armorItems, CubeElement leftLeg, CubeElement rightLeg){
        EntityValues bootsItem = (EntityValues) armorItems.get(0);
        if(!bootsItem.isEmpty()){
            List<CubeElement> armorLegElements =new ArrayList<>();
            armorLegElements.add(leftLeg);
            armorLegElements.add(rightLeg);

            ICubeModel bootsCubeModel = getArmorCubeModel(bootsItem, armorLegElements);

            appendCubeModel(bootsCubeModel);
        }
    }

    public ICubeModel getArmorCubeModel(EntityValues armorItem, List<CubeElement> armorStandsElements){
        Namespace armorNamespace = Constants.CUBE_MODEL_FACTORY.getNamespaceFromItem(armorItem);

        if(armorNamespace == null)
            return null;

        ICubeModel armorCubeModel = CubeModelFactory.getType(armorNamespace);

        EntityValues entityValues = armorNamespace.getCustomData();
        if(entityValues == null)
            entityValues = new EntityValues();

        entityValues.put("ArmorBones", armorStandsElements);
        armorNamespace.setCustomData(entityValues);

        Map<String, Object> key = armorCubeModel.getKey(armorNamespace);

        if(ARMORS.containsKey(key)){
            armorCubeModel = ARMORS.get(key).duplicate();
        }else {
            if(armorCubeModel instanceof SkullCubeModel) {
                armorNamespace.setDefaultBlockState(1);
                armorNamespace.getCustomData().put("Rot", (byte)8);
            }

            armorCubeModel.fromNamespace(armorNamespace);

            //Scale cube model by 1.18625, if helmet armor is a skull
            if(armorCubeModel instanceof SkullCubeModel)
                CubeModelUtility.scaleCubeModel(armorCubeModel, new Double[]{SKULL_SCALE, SKULL_SCALE, SKULL_SCALE}, new Double[]{0.5, 0.5, 0.0});

            ARMORS.put(key, armorCubeModel.duplicate());
        }

        return armorCubeModel;
    }

    public void poseStand(EntityValues pose, ArrayList<CubeElement> standElements){
        if(pose.containsKey("Body")){
            Double[] bodyRot = ArrayUtility.floatListToArray(pose.getFloatList("Body"));
            BlockModelUtility.setElementRotation(bodyRot, standElements, 2, 3, 4, 5);

            //Get body rotation origin
            Double[] bodyRotationOrigin = standElements.get(2).getRotation().getOrigin();

            //Shift the head, arms and legs element to reflect body rotation
            BlockModelUtility.shiftElementFromParentElement(bodyRot, bodyRotationOrigin, standElements, 0, 1, 6, 7, 8);
        }

        poseElement(pose, "Head", 6, standElements);
        poseElement(pose, "LeftArm", 7, standElements);

        //If pose has no left arm, rotate it to default angle
        if(!pose.containsKey("LeftArm")){
            Double[] leftArmRot = new Double[]{-16.0, 0.0, 12.0};
            BlockModelUtility.setElementRotation(leftArmRot, standElements, 7);
        }

        poseElement(pose, "RightArm", 8, standElements);

        //If pose has no right arm, rotate it to default angle
        if(!pose.containsKey("RightArm")){
            Double[] rightArmRot = new Double[]{-16.0, 0.0, -12.0};
            BlockModelUtility.setElementRotation(rightArmRot, standElements, 8);
        }

        poseElement(pose, "LeftLeg", 0, standElements);
        poseElement(pose, "RightLeg", 1, standElements);
    }

    public void poseElement(EntityValues pose, String elementName, Integer elementIndex, ArrayList<CubeElement> standElements){
        if(pose.containsKey(elementName)){
            Double[] rot = ArrayUtility.floatListToArray(pose.getFloatList(elementName));
            rot[1] = -rot[1];
            BlockModelUtility.setElementRotation(rot, standElements, elementIndex);
        }
    }

    @Override
    public Double[] getOrigin() {
        return new Double[]{0.5, 0.5, 0.0};
    }

    @Override
    public ICubeModel duplicate() {
        ICubeModel clone = new ArmorStandModel();
        clone.copy(this);

        return clone;
    }
}
