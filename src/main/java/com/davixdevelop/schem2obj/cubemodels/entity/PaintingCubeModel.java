package com.davixdevelop.schem2obj.cubemodels.entity;

import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.cubemodels.ICubeModel;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ArrayVector;

import java.util.HashMap;

public class PaintingCubeModel extends EntityCubeModel{
    //Map<key: motive:direction, value: Painting cube model>
    public static HashMap<String, PaintingCubeModel> PAINTING_VARIANTS = new HashMap<>();

    Double[] origin = new Double[3];

    @Override
    public boolean fromNamespace(Namespace blockNamespace, EntityValues entityValues) {

        String key = getKey(entityValues);

        if(PAINTING_VARIANTS.containsKey(key)){
            ICubeModel variantObject = PAINTING_VARIANTS.get(key);
            copy(variantObject);
        }else {
            toCubeModel(blockNamespace, entityValues);
            PAINTING_VARIANTS.put(key, this);
        }

        return false;
    }

    public String getKey(EntityValues entityValues){
        return String.format("%s:%d", entityValues.getString("Motive"), entityValues.getByte("Direction"));
    }

    public void toCubeModel(Namespace namespace, EntityValues entityValues){
        String motive = entityValues.getString("Motive");

        Integer xIndex = 0;
        Integer yIndex = 15;
        Integer width = 1;
        Integer height = 1;

        //Get the coordinate and size of to the frame (portion on image), where the Motive lies of
        switch (motive){
            case "Alban":
                xIndex = 2;
                break;
            case "Aztec":
                xIndex = 1;
                break;
            case "Aztec2":
                xIndex = 3;
                break;
            case "Bomb":
                xIndex = 4;
                break;
            case "Plant":
                xIndex = 5;
                break;
            case "Wasteland":
                xIndex = 6;
                break;
            case "Courbet":
                xIndex = 2;
                yIndex = 13;
                width = 2;
                break;
            case "Pool":
                yIndex = 13;
                width = 2;
                break;
            case "Sea":
                xIndex = 4;
                yIndex = 13;
                width = 2;
                break;
            case "Creebet":
                xIndex = 8;
                yIndex = 13;
                width = 2;
                break;
            case "Sunset":
                xIndex = 6;
                yIndex = 13;
                width = 2;
                break;
            case "Graham":
                xIndex = 1;
                yIndex = 10;
                height = 2;
                break;
            case "Wanderer":
                yIndex = 10;
                height = 2;
                break;
            case "Bust":
                xIndex = 2;
                yIndex = 6;
                height = 2;
                height = 2;
                break;
            case "Match":
                yIndex = 6;
                height = 2;
                height = 2;
                break;
            case "Skull and Roses":
                xIndex = 8;
                yIndex = 6;
                height = 2;
                height = 2;
                break;
            case "Stage":
                xIndex = 4;
                yIndex = 6;
                height = 2;
                height = 2;
                break;
            case "Void":
                xIndex = 6;
                yIndex = 6;
                height = 2;
                height = 2;
                break;
            case "Wither":
                xIndex = 10;
                yIndex = 6;
                height = 2;
                height = 2;
                break;
            case "Fighters":
                yIndex = 8;
                height = 4;
                height = 2;
                break;
            case "Kong":
                xIndex = 12;
                yIndex = 6;
                height = 4;
                height = 3;
                break;
            case "Skeleton":
                xIndex = 12;
                yIndex = 9;
                width = 4;
                height = 3;
                break;
            case "Burning Skull":
                xIndex = 8;
                yIndex = 0;
                width = 4;
                height = 4;
                break;
            case "RGB":
                xIndex = 4;
                yIndex = 0;
                width = 4;
                height = 4;
                break;
            case "Pointer":
                yIndex = 0;
                width = 4;
                height = 4;
                break;
        }

        //Set the frame bounds of the south face (left, bottom, right, top)
        Double[] southBounds = new Double[4];
        setFrameBounds(southBounds, xIndex, yIndex, width, height);
        //Set the frame bound of the north face (left, bottom, right, top)
        Double[] northBounds = new Double[4];
        setFrameBounds(northBounds, 12, 12, width, height);
        //Set the frame bound of the down face (left, bottom, right, top)
        Double[] downBounds = new Double[]{northBounds[0], northBounds[1], northBounds[2], northBounds[1] + 1};
        //Set the frame bound of the up face (left, bottom, right, top)
        Double[] upBound = new Double[]{northBounds[0], northBounds[3] - 1, northBounds[2], northBounds[3]};
        //Set the frame bound of the west face (left, bottom, right, top)
        Double[] westBound = new Double[]{northBounds[0], northBounds[1], northBounds[0] + 1, northBounds[3]};
        //Set the frame bound of the east face (left, bottom, right, top)
        Double[] eastBound = new Double[]{northBounds[2] - 1, northBounds[1], northBounds[2], northBounds[3]};

        //Create the faces for the cube
        HashMap<String, CubeElement.CubeFace> cubeFaces = new HashMap<>();
        cubeFaces.put("south", new CubeElement.CubeFace(frameBoundsToUV(southBounds), "#painting", null, null, null));
        cubeFaces.put("north", new CubeElement.CubeFace(frameBoundsToUV(northBounds), "#painting", null, null, null));
        cubeFaces.put("up", new CubeElement.CubeFace(frameBoundsToUV(upBound), "#painting", null, null, null));
        cubeFaces.put("down", new CubeElement.CubeFace(frameBoundsToUV(downBounds), "#painting", null, null, null));
        cubeFaces.put("west", new CubeElement.CubeFace(frameBoundsToUV(westBound), "#painting", null, null, null));
        cubeFaces.put("east", new CubeElement.CubeFace(frameBoundsToUV(eastBound), "#painting", null, null, null));

        //Create cube for the painting
        CubeElement cube = new CubeElement(
                new Double[]{0.0,7.5 / 16, 0.0},
                new Double[]{width.doubleValue(), 8.5 / 16, height.doubleValue()},
                false,
                null,
                cubeFaces);

        //Set the material the cube uses
        CubeModelUtility.generateOrGetMaterial("painting/paintings_kristoffer_zetterstrand", namespace);
        HashMap<String, String> modelsMaterials = new HashMap<>();
        modelsMaterials.put("painting", "painting/paintings_kristoffer_zetterstrand");

        Float yAngle = entityValues.getFloatList("Rotation").get(0);
        ArrayVector.MatrixRotation rotationY = null;

        if(yAngle > 0.0f)
            rotationY = new ArrayVector.MatrixRotation(yAngle.doubleValue(), "Z");

        //Convert cube element to cube model
        fromCubes(String.format("Painting-%s", motive.replace(" ", "_")), false, null, rotationY, modelsMaterials, cube);



        origin[2] = height / 2.0;
        origin[0] = (yAngle == 0.0f || yAngle == 180.0f) ? width / 2.0 : 0.5;
        origin[1] = (yAngle == 90.0f || yAngle == 270.0f) ? width / 2.0 : 0.5;

    }

    /**
     * Set the bounds of the frame depending on where the frame lies on the 256x256 image
     * @param uvs 4 length Double array [left, bottom, right, top]
     * @param x The abscissa of the frame on the image (from left to right)
     * @param y The ordinate of the frame on the image (from bottom to top)
     * @param width The width of the frame
     * @param height The height of the frame
     */
    public void setFrameBounds(Double[] uvs, Integer x, Integer y, Integer width, Integer height){
        uvs[0] = x * 16.0;
        uvs[1] = y * 16.0;
        uvs[2] = uvs[0] + (width * 16.0);
        uvs[3] = uvs[1] + (height * 16.0);
    }

    public Double[] frameBoundsToUV(Double[] bounds){
        Double[] uvs = new Double[4];
        for(int c = 0; c < 4; c++)
            uvs[c] = bounds[c] / 256.0;

        return uvs;
    }

    @Override
    public Double[] getOrigin() {
        return origin;
    }

    @Override
    public ICubeModel clone() {
        ICubeModel clone = new PaintingCubeModel();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(ICubeModel clone) {
        super.copy(clone);
        PaintingCubeModel paintClone = (PaintingCubeModel) clone;

        origin = paintClone.origin;
    }
}
