package com.davixdevelop.schem2obj.cubemodels.item.armor;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.item.ItemCubeModel;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.BlockModelUtility;
import com.davixdevelop.schem2obj.util.ImageUtility;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArmorCubeModel extends ItemCubeModel {
    @Override
    public Map<String, Object> getKey(Namespace namespace) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("ArmorType", namespace.getType());

        EntityValues entityValues = namespace.getCustomData();
        key.put("ArmorBones", entityValues.getList("ArmorBones"));

        if(namespace.getType().contains("leather") && entityValues.containsKey("display"))
            if(entityValues.getEntityValues("display").containsKey("color"))
                key.put("ArmorColor", entityValues.getEntityValues("display").getInteger("color"));

        return key;
    }

    /**
     * Create a cube model of the armor type
     * @param namespace The namespace of the armor
     * @param armorType Type of armor (helmet, chestplate, leggings, boots)
     * @param layer The layer the armor uses (layer_1, layer_2)
     */
    public void toCubeModel(Namespace namespace, String armorType, String layer){
        String armorMaterialType = namespace.getType().replace("_" + armorType, "");

        if(armorMaterialType.equals("golden"))
            armorMaterialType = "gold";

        EntityValues entityValues = namespace.getCustomData();

        List<CubeElement> armorBones = null;

        try {
            armorBones = (List<CubeElement>) entityValues.getList("ArmorBones");
        }catch (Exception ex) {
            return;
        }

        HashMap<String, String> modelMaterials = getMaterials(namespace, armorMaterialType, layer, armorType);

        BlockModel helmetModel = Constants.BLOCK_MODELS.getBlockModel("armor_" + armorType, "builtin").get(0);
        List<CubeElement> elements = helmetModel.getElements();

        for(int c = 0; c < armorBones.size(); c++) {
            Double[] boneOrigin = BlockModelUtility.getElementRotationOrigin(armorBones.get(c));
            Double[] boneRotation = BlockModelUtility.getElementRotation(armorBones.get(c));
            Double[] elementOrigin = BlockModelUtility.getElementRotationOrigin(elements.get(c));

            //Move element to first bone
            elements.set(c, BlockModelUtility.moveElement(elements.get(c), elementOrigin, boneOrigin));
            //Rotate element to the bone rotation
            BlockModelUtility.setElementRotation(boneRotation, elements, c);
        }

        fromCubes(namespace.getType(), false, null, null, modelMaterials, elements.toArray(new CubeElement[0]));
    }

    /**
     * Get the materials the armor uses
     * @param namespace The namespace of the armor
     * @param materialType Type of material the armor uses (leather, diamond...)
     * @param layer The layer the armor material uses (layer_1, layer_2)
     * @param armorType The type of armor (helmet, chestplate, boots, leggings)
     * @return A map of materials used
     */
    public HashMap<String, String> getMaterials(Namespace namespace, String materialType, String layer, String armorType){
        HashMap<String, String> materials = new HashMap<>();
        EntityValues entityValues = namespace.getCustomData();

        String defaultMaterialPath = String.format("models/armor/%s_%s", materialType, layer);

        if(materialType.equals("leather")){
            Integer armorColor = Constants.DEFAULT_LEATHER_COLOR;
            String armorMaterialName = String.format("%s_%s_armor", materialType, layer);


            if(entityValues.containsKey("display")) {
                armorColor = entityValues.getEntityValues("display").getInteger("color");
                armorMaterialName += "_" + armorColor.toString();
            }

            String armorMaterialPath = "models/armor/" + armorMaterialName;

            materials.put(armorType, armorMaterialPath);

            if(Constants.BLOCK_MATERIALS.containsMaterial(armorMaterialPath))
                return materials;

            String overlayMaterialPath = String.format("models/armor/leather_%s_overlay", layer);

            CubeModelUtility.generateOrGetMaterial(defaultMaterialPath, namespace);
            CubeModelUtility.generateOrGetMaterial(overlayMaterialPath, namespace);

            IMaterial leatherMaterial = Constants.BLOCK_MATERIALS.getMaterial(defaultMaterialPath).duplicate();
            IMaterial overlayMaterial = Constants.BLOCK_MATERIALS.getMaterial(overlayMaterialPath);

            leatherMaterial.setName(armorMaterialName);

            //Color the leather grayscale image
            BufferedImage layerImage = ImageUtility.colorImage(leatherMaterial.getDefaultDiffuseImage(), armorColor);

            //Overlay the leather overlay image on top of the layer image
            layerImage = ImageUtility.overlayImage(layerImage, overlayMaterial.getDefaultDiffuseImage());

            //Set the colored image to the diffuse image of the material
            leatherMaterial.setDiffuseImage(layerImage);

            Constants.BLOCK_MATERIALS.setMaterial(armorMaterialPath, leatherMaterial);
            Constants.BLOCK_MATERIALS.unsetUsedMaterial(defaultMaterialPath);
            Constants.BLOCK_MATERIALS.unsetUsedMaterial(overlayMaterialPath);
        }else {
            CubeModelUtility.generateOrGetMaterial(defaultMaterialPath, namespace);
            materials.put(armorType, defaultMaterialPath);
        }

        return materials;
    }
}
