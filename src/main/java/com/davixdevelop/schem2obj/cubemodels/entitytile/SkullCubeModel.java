package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.Orientation;
import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.cubemodels.json.PlayerTextureTemplate;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.google.gson.Gson;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class SkullCubeModel extends TileEntityCubeModel {

    //key facing:Rot:SkullType(:Owner-ID), value Skull Cube Model
    public static HashMap<String, SkullCubeModel> SKULL_VARIANTS = new HashMap<>();
    //SkullType or Owner-ID
    public static Set<String> GENERATED_SKULLS = new HashSet<>();
    //Map<key: Owner-ID, value: 0 -> default player head | 1 -> hd player head
    public static Map<String, Integer> PLAYER_HEAD_TYPES = new HashMap<>();

    static Gson GSON = new Gson();

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {

        String key = getKey(blockNamespace, entityValues);

        if(SKULL_VARIANTS.containsKey(key)){
            ICubeModel variantObject = SKULL_VARIANTS.get(key);
            copy(variantObject);
        }else{
            toCubeModel(blockNamespace, entityValues);
            SKULL_VARIANTS.put(key, this);
        }

        return false;
    }

    public String getKey(Namespace namespace, EntityValues entityValues){
        String key = String.format("%s:%d:%d",namespace.getData("facing"), entityValues.getByte("Rot"),entityValues.getByte("SkullType"));
        if(entityValues.containsKey("Owner")){
            EntityValues owner = entityValues.getEntityValues("Owner");
            key = String.format("%s:%s",key, owner.getString("Id"));
        }
        return key;
    }

    public void toCubeModel(Namespace namespace, EntityValues entityValues){
        boolean isPlayerHead = entityValues.containsKey("Owner");
        byte skullType = entityValues.getByte("SkullType");

        String headModelName;
        String material;

        //Set the name of the head model and the material of the head, depending on the skull type
        if(skullType == 5) {
            headModelName = "enderdragon/head";
            material = "dragon-enderdragon";
        }
        else if(skullType == 3 || skullType == 2) {
            headModelName = "default_head";
            if(skullType == 2)
                material = "zombie-zombie";
            else
                material = "steve";
        }else {
            headModelName = "mob_head";
            material = (skullType == 0) ? "skeleton-skeleton" : (skullType == 1) ? "wither_skeleton-skeleton" : "creeper-creeper";
        }

        //If the skull is a player head, download the player's skin and set the material
        if(isPlayerHead){
            try{
                //Get the "Owner" tag from the tile entity
                EntityValues owner = entityValues.getEntityValues("Owner");
                //Check if material wasn't generated yet for the player head
                if(!GENERATED_SKULLS.contains(owner.getString("Id"))) {
                    //Get the properties of the owner
                    EntityValues properties = owner.getEntityValues("Properties");
                    //Get textures from properties
                    List<?> textures = properties.getList("textures");
                    EntityValues texture = (EntityValues) textures.get(0);
                    //Decode the value of the texture
                    String base64_Value = texture.getString("Value");
                    byte[] decodedByteValue = Base64.getDecoder().decode(base64_Value);
                    String value = new String(decodedByteValue);
                    //Deserialize the value
                    PlayerTextureTemplate textureTemplate = GSON.fromJson(value, PlayerTextureTemplate.class);
                    //Get url to player skin
                    String url = textureTemplate.textures.SKIN.url;

                    //Download the player skin
                    InputStream inputStream = new URL(url).openStream();
                    //Create buffered image from it
                    BufferedImage playerSkin = ImageUtility.toBuffedImage(inputStream);
                    //If ratio of image is not 1:1, use the hd variant of the player head
                    if(playerSkin != null) {
                        if (playerSkin.getWidth() / playerSkin.getHeight() != 1) {
                            headModelName = "hd_player_head";
                            PLAYER_HEAD_TYPES.put(owner.getString("Id"), 1);
                        }
                        else{
                            headModelName = "player_head";
                            PLAYER_HEAD_TYPES.put(owner.getString("Id"), 0);
                        }
                        material = owner.getString("Id");

                        //Ensure the default "steve" material was generated
                        if(!GENERATED_SKULLS.contains("steve"))
                            CubeModelUtility.generateOrGetMaterial("entity/steve", namespace);

                        //Get copy of "steve" material, set the id of the owner to It's name, as set the player skin diffuse image to it
                        IMaterial player_skin_material = Constants.BLOCK_MATERIALS.getMaterial("entity/steve").clone();
                        player_skin_material.setName(material);
                        player_skin_material.setDiffuseImage(playerSkin);

                        //Put the new material to the material collection
                        Constants.BLOCK_MATERIALS.setMaterial(String.format("entity/%s", material), player_skin_material);

                        if(!GENERATED_SKULLS.contains("steve"))
                            Constants.BLOCK_MATERIALS.unsetUsedMaterial("entity/steve");

                        GENERATED_SKULLS.add(material);

                    }
                }else{
                    material = owner.getString("Id");
                    Integer playerHeadType = PLAYER_HEAD_TYPES.get(material);
                    headModelName = playerHeadType == 0 ? "player_head" : "hd_player_head";
                }

            }catch (Exception ex){
                material = "steve";
                isPlayerHead = false;
            }

        }

        if(!isPlayerHead){
            if(!GENERATED_SKULLS.contains(material))
            {
                CubeModelUtility.generateOrGetMaterial(String.format("entity/%s", material), namespace);
                GENERATED_SKULLS.add(material);
            }
        }

        int Rot = entityValues.getByte("Rot");
        String facing = namespace.getData("facing");
        switch (facing){
            case "east":
                Rot = 4;
                break;
            case "north":
                Rot = 7;
                break;
            case "west":
                Rot = 12;
                break;
            case "south":
                Rot = 0;
                break;
        }

        ArrayVector.MatrixRotation rotationY = null;

        if(Rot > 0){
            rotationY = new ArrayVector.MatrixRotation((360 / 16.0) * Rot, "Z");
        }

        BlockModel headModel = Constants.BLOCK_MODELS.getBlockModel(headModelName, "builtin").get(0);
        CubeElement[] headElements = headModel.getElements().toArray(new CubeElement[0]);

        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("head", String.format("entity/%s", material));

        //Convert cube elements to cube model
        fromCubes(String.format("skull-%s", material), false, null, rotationY, modelsMaterials, headElements);

        Double[] translate = null;

        //If skull faces down, translate it up
        if(facing.equals("down")){
            if(skullType != 5){
                //Skull type is a regular cube
                translate = new Double[]{0.0, 0.0, 8 / 16.0};
            }else{
                //Skull type is ender dragon
                translate = new Double[]{0.0, 0.0, 4 / 16.0};
            }
        }else if(!facing.equals("up")){
            //If it doesn't face up, translate it in the direction of the facing

            Double offset = 0.0;

            if(skullType != 5){
                //Skull type is a regular cube
                offset = 4.17555614108 / 16;
                translate = new Double[]{0.0, 0.0, 4 / 16.0};
            }else{
                //Skull type is ender dragon
                offset = 3.55 / 16;
                translate = new Double[]{0.0, 0.0, 2 / 16.0};
            }

            Orientation orientation = Orientation.getOrientation(facing);
            Integer yOffset = orientation.getYOffset();
            if(yOffset != 0)
                translate[1] = yOffset * offset * -1;

            Integer xOffset = orientation.getXOffset();
            if(xOffset != 0)
                translate[0] = xOffset * offset * -1;
        }

        if(translate != null)
            CubeModelUtility.translateCubeModel(this, translate);
    }
}
