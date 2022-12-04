package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.models.IntegerString;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The CubeModel for the Banner entity
 * It's only responsible to create the material for the banner.
 * For the implementation of the Wall and Standing Banner,
 * see WallBannerCubeModel and StandingBannerCubeModel
 *
 * @author DavixDevelop
 */
public class BannerCubeModel extends TileEntityCubeModel {

    public static Map<String, BufferedImage> PATTERNS = new HashMap<>();
    public static Map<String, List<String>> PATTERN_CODES = new HashMap<>();

    private static boolean DEFAULT_MATERIAL_GENERATED = false;
    String bannerPatternCode;
    String bannerColor;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        EntityValues entityValues = namespace.getCustomData();

        //Get the base color index of the banner
        Integer baseColorIndex = entityValues.getInteger("Base");
        IntegerString baseColor = Constants.BANNER_COLORS.get(baseColorIndex);

        //Set the name of the color of the base banner
        bannerColor = baseColor.getStringValue();

        //Get the base pattern
        IntegerString basePattern = Constants.PATTERNS.get("b");

        List<String> patternCodes = new ArrayList<>();
        //Check if the colored pattern doesn't already exists, and create it
        patternCodes.add(generateColoredPattern(basePattern, baseColor, baseColorIndex));

        if(entityValues.containsKey("Patterns")){
            List<?> Patterns = entityValues.getList("Patterns");
            for(Object patternMap : Patterns){
                if(patternMap instanceof EntityValues){
                    EntityValues patternValues = (EntityValues) patternMap;
                    Integer patternColorIndex = patternValues.getInteger("Color");
                    IntegerString patternColor = Constants.BANNER_COLORS.get(patternColorIndex);
                    IntegerString pattern = Constants.PATTERNS.get(patternValues.getString("Pattern"));

                    patternCodes.add(generateColoredPattern(pattern, patternColor, patternColorIndex));
                }
            }
        }

        for(String pattern : PATTERN_CODES.keySet()){
            List<String> patterns = PATTERN_CODES.get(pattern);

            if(patterns.size() == patternCodes.size()){
                boolean matches = true;
                for(int p = 0; p < patterns.size(); p++){
                    if(!patterns.get(p).equals(patternCodes.get(p)))
                    {
                        matches = false;
                        break;
                    }
                }

                if(matches) {
                    bannerPatternCode = pattern;
                    return true;
                }
            }
        }

        //Create new material from patterns codes
        String baseMaterialPath = "entity/banner_base";
        CubeModelUtility.generateOrGetMaterial(baseMaterialPath, namespace);
        IMaterial banner_material = Constants.BLOCK_MATERIALS.getMaterial(baseMaterialPath).duplicate();

        try {

            BufferedImage diffuseImage = banner_material.getDefaultDiffuseImage();

            BufferedImage coloredBanner = null;

            StringBuilder patternCodesName = new StringBuilder();
            for (String patternCode : patternCodes) {
                if(patternCode != null){
                    patternCodesName.append(patternCode);

                    //Multiply the diffuse with the pattern
                    BufferedImage coloredPattern = ImageUtility.multiplyImage(diffuseImage, PATTERNS.get(patternCode));

                    //Get the portion of the colored base patter that the pattern covers
                    coloredPattern = ImageUtility.maskImage(coloredPattern, PATTERNS.get(patternCode));

                    //Overlay the portion with the diffuse image
                    if(coloredBanner == null){
                        coloredBanner = ImageUtility.overlayImage(diffuseImage, coloredPattern);
                    }else
                        coloredBanner = ImageUtility.overlayImage(coloredBanner, coloredPattern);
                }
            }

            banner_material.setName(String.format("banner-%s", patternCodesName));
            banner_material.setDiffuseImage(coloredBanner);

            //Put modified material into collection
            Constants.BLOCK_MATERIALS.setMaterial(String.format("entity/banner-%s", patternCodesName), banner_material);
            //Remove the base material from the used materials
            Constants.BLOCK_MATERIALS.unsetUsedMaterial(baseMaterialPath);

            bannerPatternCode = patternCodesName.toString();

            PATTERN_CODES.put(bannerPatternCode, patternCodes);

            return true;


        }catch (Exception ex){
            LogUtility.Log("Error while creating material for banner");
            LogUtility.Log(ex.getMessage());
        }

        //If an error occurred, set the default material as the banner material
        generateDefaultMaterial(namespace);

        return false;
    }

    public String getBannerPatternCode() {
        return bannerPatternCode;
    }

    /**
     * //Check if the colored pattern doesn't already exists, and create it
     * @param pattern The path and index to the pattern
     * @param color The name and int value of the color
     * @param colorIndex The index to the color
     */
    private String generateColoredPattern(IntegerString pattern, IntegerString color, Integer colorIndex){
        //<index of pattern>-<color index>
        String patternCode = String.format("%02d%02d",pattern.getIntegerValue(), colorIndex);
        if(!PATTERNS.containsKey(patternCode)){
            try{
                String patternPath = ResourceLoader.getResourcePath("textures", pattern.getStringValue(), "png");
                InputStream patternImageStream = ResourceLoader.getResource(patternPath);

                BufferedImage patternImage = ImageUtility.toBuffedImage(patternImageStream);

                patternImage = ImageUtility.colorImage(patternImage, color.getIntegerValue(), true);

                PATTERNS.put(patternCode, patternImage);

            }catch (Exception ex){
                LogUtility.Log(ex.getMessage());
                return null;
            }
        }

        return patternCode;
    }

    public void generateDefaultMaterial(Namespace blockNamespace){
        if(!DEFAULT_MATERIAL_GENERATED) {
            String baseMaterialPath = "entity/banner_base";
            CubeModelUtility.generateOrGetMaterial(baseMaterialPath, blockNamespace);
            IMaterial default_banner_material = Constants.BLOCK_MATERIALS.getMaterial(baseMaterialPath).duplicate();
            default_banner_material.setName("banner-default");
            Constants.BLOCK_MATERIALS.setMaterial("entity/banner-default", default_banner_material);
            Constants.BLOCK_MATERIALS.unsetUsedMaterial(baseMaterialPath);

            bannerPatternCode = "default";

            DEFAULT_MATERIAL_GENERATED = true;
        }
    }
}
