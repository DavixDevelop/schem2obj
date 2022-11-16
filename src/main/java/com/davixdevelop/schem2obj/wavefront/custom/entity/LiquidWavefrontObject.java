package com.davixdevelop.schem2obj.wavefront.custom.entity;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.models.HashedDoubleList;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.util.ArrayVector;
import com.davixdevelop.schem2obj.wavefront.WavefrontObject;
import com.davixdevelop.schem2obj.wavefront.WavefrontUtility;

import java.util.*;

public class LiquidWavefrontObject extends WavefrontObject {

    private String flowableMaterial;
    private String stillMaterial;

    public static double ZERO_LIQUID_LEVEL = 11.0; //Level 0
    public static double MAX_LIQUID_LEVEL = 3.1; //Level 1
    public static double MIN_LIQUID_LEVEL = .4; //Level 7

    public static double DEFAULT_LIQUID_COEFFICIENT = 1.61291488892;
    //public static double DEFAULT_LIQUID_COEFFICIENT = 0.41875;
    public static double DEFAULT_LIQUID_VERTICAL_INTERCEPT = 14;

    public static double NON_BLOCK_LIQUID_COEFFICIENT = 0.63419845853;
    public static double NON_BLOCK_LIQUID_VERTICAL_INTERCEPT = 5.50024722391;

    public static Integer[][] NORTH_SOUTH_WEST_EAST_DIRECTION_VECTORS = new Integer[][]{{0,1}, {0,-1},{-1,0},{1,0}};

    //A collection to store all the CornerHeights for each liquid block
    //Map<key: y axis, value: Map<key: x axis, value: Map<key: z axis, value: CornerHeights>>>
    private Map<Integer, Map<Integer, Map<Integer, LiquidBlock>>> LiquidBlocksCollection = new HashMap<>();

    private double still_texture_rows = 0.0;
    private double flowing_texture_rows = 0.0;

    public LiquidWavefrontObject(){}

    /**
     * @param flowableMaterial The name of the flowable material
     * @param stillMaterial The name of the still material
     * @param still_texture_rows Number of rows in the still texture
     * @param flowing_texture_rows Number of rows in the flowing texture
     */
    public LiquidWavefrontObject(String flowableMaterial, String stillMaterial, double still_texture_rows, double flowing_texture_rows){
        this.flowableMaterial = flowableMaterial;
        this.stillMaterial = stillMaterial;
        this.still_texture_rows = still_texture_rows;
        this.flowing_texture_rows = flowing_texture_rows;
    }

    enum FLOW_DIRECTION {
        STILL,
        SOUTH,
        NORTH,
        EAST,
        WEST,
        SOUTH_WEST,
        SOUTH_EAST,
        NORTH_EAST,
        NORTH_WEST
    }

    public static double MAX_FLOWABLE_LEVEL = 15 / 16.0;

    HashedDoubleList vertices = new HashedDoubleList();
    ArrayList<Double[]> normalsArray = new ArrayList<>();
    HashedDoubleList textureCoordinates = new HashedDoubleList();

    /**
     * Add a liquid to the object
     * @param liquidNamespace The block namespace of the liquid
     */
    public void addBlock(Namespace liquidNamespace){

        int liquidLevel = getLiquidLevel(liquidNamespace);

        Namespace south = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX(), Constants.LOADED_SCHEMATIC.getPosY(), Constants.LOADED_SCHEMATIC.getPosZ() + 1);
        Namespace north = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX(), Constants.LOADED_SCHEMATIC.getPosY(), Constants.LOADED_SCHEMATIC.getPosZ() - 1);
        Namespace east = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX() + 1, Constants.LOADED_SCHEMATIC.getPosY(), Constants.LOADED_SCHEMATIC.getPosZ());
        Namespace west = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX() - 1, Constants.LOADED_SCHEMATIC.getPosY(), Constants.LOADED_SCHEMATIC.getPosZ());

        Namespace up = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX(), Constants.LOADED_SCHEMATIC.getPosY() + 1, Constants.LOADED_SCHEMATIC.getPosZ());
        Namespace down = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX(), Constants.LOADED_SCHEMATIC.getPosY() - 1, Constants.LOADED_SCHEMATIC.getPosZ());

        boolean hasLiquidUp = up != null && isLiquidAdjacent(up);
        boolean hasLiquidDown = down != null && isLiquidAdjacent(down);

        int x_pos = Constants.LOADED_SCHEMATIC.getPosX();
        int y_pos = Constants.LOADED_SCHEMATIC.getPosY();
        int z_pos = Constants.LOADED_SCHEMATIC.getPosZ();

        //If adjacent blocks are all liquid, don't add any face
        if((south != null && isLiquidAdjacent(south)) && (north != null && isLiquidAdjacent(north)) && (west != null && isLiquidAdjacent(west)) && (east != null && isLiquidAdjacent(east)) && (up != null && isLiquidAdjacent(up)) && (down != null && isLiquidAdjacent(down))) {
            //appendLiquidBlock(new LiquidBlock(liquidNamespace, new Double[]{}, hasLiquidUp, hasLiquidDown, north != null && isLiquidAdjacent(north), south != null && isLiquidAdjacent(south), west != null && isLiquidAdjacent(west), east != null && isLiquidAdjacent(east), FLOW_DIRECTION.STILL, true), y_pos, x_pos, z_pos);
            return;
        }


        Namespace north_east = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX() + 1, Constants.LOADED_SCHEMATIC.getPosY(), Constants.LOADED_SCHEMATIC.getPosZ() - 1);
        Namespace north_west = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX() - 1, Constants.LOADED_SCHEMATIC.getPosY(), Constants.LOADED_SCHEMATIC.getPosZ() - 1);
        Namespace south_east = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX() + 1, Constants.LOADED_SCHEMATIC.getPosY(), Constants.LOADED_SCHEMATIC.getPosZ() + 1);
        Namespace south_west = Constants.LOADED_SCHEMATIC.getNamespace(Constants.LOADED_SCHEMATIC.getPosX() - 1, Constants.LOADED_SCHEMATIC.getPosY(), Constants.LOADED_SCHEMATIC.getPosZ() + 1);

        Double[] cornerHeights = new Double[]{null, null, null, null};

        boolean createFullBlock = false;
        if(hasLiquidUp && hasLiquidDown)
            createFullBlock = true;
        else if(hasLiquidUp)
            createFullBlock = true;


        if(!hasLiquidDown && !hasLiquidUp){
            int y = Constants.LOADED_SCHEMATIC.getPosY();
            if(LiquidBlocksCollection.containsKey(y - 1)){
                Boolean[] checkCorner = new Boolean[]{false, false, false, false};
                hasLiquidAdjacent(y - 1, Constants.LOADED_SCHEMATIC.getPosX(), Constants.LOADED_SCHEMATIC.getPosZ(), checkCorner);
                hasLiquidDown = checkCorner[0] || checkCorner[1] || checkCorner[2] || checkCorner[3];
            }
        }

        boolean isLiquidAdjacent = (east != null && isLiquidAdjacent(east)) ||
                (north != null && isLiquidAdjacent(north)) ||
                (west != null && isLiquidAdjacent(west)) ||
                (south != null && isLiquidAdjacent(south));

        FLOW_DIRECTION flow_direction = FLOW_DIRECTION.STILL;

        if(isLiquidAdjacent)
            flow_direction = getFlowDirection(liquidLevel, north, south, west, east, north_east, north_west, south_east, south_west);

        if(!createFullBlock) {

            //2D Array to store what adjacent blocks are a liquid
            /*
                north_west  north   north_east
                west        center  east
                south_west  south   south_east
            */
            boolean[][] liquidMatrix = new boolean[][]{
                    {(north_west != null && isLiquidAdjacent(north_west)), (north != null && isLiquidAdjacent(north)), (north_east != null && isLiquidAdjacent(north_east))},
                    {(west != null && isLiquidAdjacent(west)), true, (east != null && isLiquidAdjacent(east))},
                    {(south_west != null && isLiquidAdjacent(south_west)), (south != null && isLiquidAdjacent(south)), (south_east != null && isLiquidAdjacent(south_east))}
            };

            //Set north east corner
            //Check east block
            setCornersFromAdjacentBlock(liquidLevel, cornerHeights, east, "east", isLiquidAdjacent, hasLiquidDown, flow_direction, liquidMatrix);
            //Check north east block
            setCornersFromAdjacentBlock(liquidLevel, cornerHeights, north_east, "north_east", isLiquidAdjacent, hasLiquidDown, flow_direction, liquidMatrix);
            //Check north block
            setCornersFromAdjacentBlock(liquidLevel, cornerHeights, north, "north", isLiquidAdjacent, hasLiquidDown, flow_direction, liquidMatrix);

            //Set north west corner
            //Check north west block
            setCornersFromAdjacentBlock(liquidLevel, cornerHeights, north_west, "north_west", isLiquidAdjacent, hasLiquidDown, flow_direction, liquidMatrix);
            //Check west block
            setCornersFromAdjacentBlock(liquidLevel, cornerHeights, west, "west", isLiquidAdjacent, hasLiquidDown, flow_direction, liquidMatrix);

            //Set south west corner
            //Check south west block
            setCornersFromAdjacentBlock(liquidLevel, cornerHeights, south_west, "south_west", isLiquidAdjacent, hasLiquidDown, flow_direction, liquidMatrix);
            //Check south block
            setCornersFromAdjacentBlock(liquidLevel, cornerHeights, south, "south", isLiquidAdjacent, hasLiquidDown, flow_direction, liquidMatrix);

            //Set south east corner
            //Check south east block
            setCornersFromAdjacentBlock(liquidLevel, cornerHeights, south_east, "south_east", isLiquidAdjacent, hasLiquidDown, flow_direction, liquidMatrix);


            for (int c = 0; c < cornerHeights.length; c++) {
                cornerHeights[c] = cornerHeights[c] / 3.0;
            }
        }else{
            for (int x = 0; x < 4; x++)
                cornerHeights[x] = 16.0;
        }

        /*int fullCounter = 0;

        for(int c = 0; c < cornerHeights.length; c++){
            if(cornerHeights[c].equals(16.0))
                fullCounter += 1;
        }

        if(fullCounter == 1){
            String w = "2";
        }*/


        boolean hasLiquidNorth = north != null && isLiquidAdjacent(north);
        boolean hasLiquidSouth = south != null && isLiquidAdjacent(south);
        boolean hasLiquidEast = east != null && isLiquidAdjacent(east);
        boolean hasLiquidWest = west != null && isLiquidAdjacent(west);

       appendLiquidBlock(new LiquidBlock(liquidNamespace, cornerHeights, hasLiquidUp, hasLiquidDown, hasLiquidNorth, hasLiquidSouth, hasLiquidWest, hasLiquidEast, flow_direction, !isLiquidAdjacent || createFullBlock), y_pos, x_pos, z_pos);

        if(!isLiquidAdjacent || createFullBlock)
            createBlock(liquidNamespace, cornerHeights, hasLiquidUp, hasLiquidDown, hasLiquidNorth, hasLiquidSouth, hasLiquidEast, hasLiquidWest, flow_direction, x_pos, y_pos, z_pos);




    }

    private void appendLiquidBlock(LiquidBlock liquidBlock, int y_pos, int x_pos, int z_pos){
        if(!LiquidBlocksCollection.containsKey(y_pos))
            LiquidBlocksCollection.put(y_pos, new HashMap<>());

        Map<Integer, Map<Integer, LiquidBlock>> row = LiquidBlocksCollection.get(y_pos);

        if(!row.containsKey(x_pos))
            row.put(x_pos, new HashMap<>());

        Map<Integer, LiquidBlock> column = row.get(x_pos);

        column.put(z_pos, liquidBlock);
    }

    public void createBlock(Namespace liquidNamespace, Double[] cornerHeights, boolean hasLiquidUp, boolean hasLiquidDown, boolean hasLiquidNorth, boolean hasLiquidSouth, boolean hasLiquidEast, boolean hasLiquidWest, FLOW_DIRECTION flowDirection, int x, int y, int z){
        //Value by how much to move each vert (vert + translate)
        Double translateX = x - (Constants.LOADED_SCHEMATIC.getWidth() / 2.0);
        Double translateY = (z * -1.0) + ((Constants.LOADED_SCHEMATIC.getLength() / 2) - 1);
        Double[] translate = new Double[]{translateX, translateY, y * 1.0};

        HashMap<String, Integer> CornersIndex = new HashMap<>();

        Double[] A = new Double[]{0.0, 0.0, 0.0};
        Double[] B = new Double[]{0.0, 0.0, WavefrontUtility.round(cornerHeights[3] / 16.0,  6)};
        Double[] C = new Double[]{0.0, 1.0, WavefrontUtility.round(cornerHeights[0] / 16.0, 6)};
        Double[] D = new Double[]{0.0, 1.0, 0.0};
        Double[] F = new Double[]{1.0, 1.0, WavefrontUtility.round(cornerHeights[1] / 16.0, 6)};
        Double[] G = new Double[]{1.0, 0.0, WavefrontUtility.round(cornerHeights[2] / 16.0, 6)};
        Double[] H = new Double[]{1.0, 0.0, 0.0};
        Double[] M = new Double[]{1.0, 1.0, 0.0};

        HashMap<String, Double[]> CubeCorners = new HashMap<>();
        Set<String> CubeFaces = new HashSet<>();

        //Create up face
        if(!hasLiquidUp) {
            CubeFaces.add("up");

            CubeCorners.put("B", B);
            CubeCorners.put("C", C);
            CubeCorners.put("F", F);
            CubeCorners.put("G", G);
        }

        if(!hasLiquidDown) {
            CubeFaces.add("down");

            CubeCorners.put("D", D);
            CubeCorners.put("A", A);
            CubeCorners.put("H", H);
            CubeCorners.put("M", M);
        }

        //Check to create north face
        if(!hasLiquidNorth && cornerHeights[0] != null && cornerHeights[1] != null){
            CubeFaces.add("north");

            CubeCorners.put("M", M);
            CubeCorners.put("F", F);
            CubeCorners.put("C", C);
            CubeCorners.put("D", D);
        }

        //Check to create west face
        if(!hasLiquidWest && cornerHeights[0] != null && cornerHeights[3] != null){
            CubeFaces.add("west");

            CubeCorners.put("D", D);
            CubeCorners.put("C", C);
            CubeCorners.put("B", B);
            CubeCorners.put("A", A);
        }

        //Check to create south face
        if(!hasLiquidSouth && cornerHeights[3] != null && cornerHeights[2] != null){
            CubeFaces.add("south");

            CubeCorners.put("A", A);
            CubeCorners.put("B", B);
            CubeCorners.put("G", G);
            CubeCorners.put("H", H);
        }

        //Check to create east face
        if(!hasLiquidEast && cornerHeights[1] != null && cornerHeights[2] != null){
            CubeFaces.add("east");

            CubeCorners.put("H", H);
            CubeCorners.put("G", G);
            CubeCorners.put("F", F);
            CubeCorners.put("M", M);
        }

        //Add the vertices to the object
        for(String corner : CubeCorners.keySet()){
            int VertexIndex = 0;

            Double[] c = CubeCorners.get(corner);
            c = ArrayVector.add(c, translate);

            if(vertices.containsKey(c))
                VertexIndex = vertices.getIndex(c);
            else
                VertexIndex = vertices.put(c);

            CornersIndex.put(corner, VertexIndex);
        }



        HashMap<String, ArrayList<ArrayList<Integer[]>>> faces = getMaterialFaces();
        if(faces == null)
        {
            WavefrontUtility.generateOrGetMaterial(stillMaterial, liquidNamespace);
            WavefrontUtility.generateOrGetMaterial(flowableMaterial, liquidNamespace);

            faces = new HashMap<>();
            faces.put(stillMaterial,new ArrayList<>());
            faces.put(flowableMaterial, new ArrayList<>());
        }

        //Loop through faces and add them to the material faces
        for(String face : CubeFaces){
            String[] faceCorners = null;
            ArrayList<Double[]> faceUV = new ArrayList<>();
            String faceMaterial = flowableMaterial;

            //Set the UV up coordinates to use the bottom portion of the material, and
            //and use the left portion of the flowable texture, as the flowable texture uses 2 identical columns
            double tup = (1 / flowing_texture_rows);
            double tlp = 0.0;

            double tl = 0.0;
            double tr = 0.5;

            switch (face){
                case "north":
                    faceCorners = new String[]{"M", "F", "C", "D"};
                    faceUV.add(new Double[]{tl, tlp});
                    faceUV.add(new Double[]{tl, (F[2] * tup) + (tlp * F[2])});
                    faceUV.add(new Double[]{tr, (C[2] * tup) + (tlp * C[2])});
                    faceUV.add(new Double[]{tr, tlp});
                    break;
                case "west":
                    faceCorners = new String[]{"D", "C", "B", "A"};
                    faceUV.add(new Double[]{tl, tlp});
                    faceUV.add(new Double[]{tl, (C[2] * tup) + (tlp * C[2])});
                    faceUV.add(new Double[]{tr, (B[2] * tup) + (tlp * B[2])});
                    faceUV.add(new Double[]{tr, tlp});
                    break;
                case "south":
                    faceCorners = new String[]{"A", "B", "G", "H"};
                    faceUV.add(new Double[]{tl, tlp});
                    faceUV.add(new Double[]{tl, (B[2] * tup) + (tlp * B[2])});
                    faceUV.add(new Double[]{tr, (G[2] * tup) + (tlp * G[2])});
                    faceUV.add(new Double[]{tr, tlp});
                    break;
                case "east":
                    faceCorners = new String[]{"H", "G", "F", "M"};
                    faceUV.add(new Double[]{tl, tlp});
                    faceUV.add(new Double[]{tl, (G[2] * tup) + (tlp * G[2])});
                    faceUV.add(new Double[]{tr, (F[2] * tup) + (tlp * F[2])});
                    faceUV.add(new Double[]{tr, tlp});
                    break;
                case "down":
                    faceCorners = new String[]{"D", "A", "H", "M"};
                    faceMaterial = stillMaterial;

                    tup = (1 / still_texture_rows);

                    faceUV.add(new Double[]{0.0, tlp});
                    faceUV.add(new Double[]{0.0, tup});
                    faceUV.add(new Double[]{1.0, tup});
                    faceUV.add(new Double[]{1.0, tlp});
                    break;
                case "up":
                    faceCorners = new String[]{"B", "C", "F", "G"};
                    faceMaterial = (flowDirection == FLOW_DIRECTION.STILL) ? stillMaterial : flowableMaterial;

                    if(flowDirection == FLOW_DIRECTION.STILL) {
                        tup = (1 / still_texture_rows);
                        faceUV.add(new Double[]{0.0, tlp});
                        faceUV.add(new Double[]{0.0, tup});
                        faceUV.add(new Double[]{1.0, tup});
                        faceUV.add(new Double[]{1.0, tlp});
                    }else{
                        //Use the left portion of the flowable texture, as the flowable texture uses 2 identical columns
                        faceUV.add(new Double[]{tl, tlp});
                        faceUV.add(new Double[]{tl, tup});
                        faceUV.add(new Double[]{tr, tup});
                        faceUV.add(new Double[]{tr, tlp});
                    }

                    //Rotate the UV up coordinates depending on the flow direction
                    switch (flowDirection) {
                        case SOUTH:
                            WavefrontUtility.shiftRotateUV(faceUV, 180.0);
                            break;
                        case EAST:
                            WavefrontUtility.shiftRotateUV(faceUV, 90.0);
                            break;
                        case WEST:
                            WavefrontUtility.shiftRotateUV(faceUV, 270.0);
                            break;
                        case NORTH_EAST:
                            WavefrontUtility.shiftRotateUV(faceUV,45.0);
                            break;
                        case SOUTH_EAST:
                            WavefrontUtility.shiftRotateUV(faceUV, 135.0);
                            break;
                        case SOUTH_WEST:
                            WavefrontUtility.shiftRotateUV(faceUV, 225.0);
                            break;
                        case NORTH_WEST:
                            WavefrontUtility.shiftRotateUV(faceUV, 315.0);
                            break;
                    }
                    break;
            }

            ArrayList<ArrayList<Integer[]>> materialFaces = faces.get(faceMaterial);

            ArrayList<Integer[]> indices = new ArrayList<>();
            for(int x1 = 0; x1 < 4; x1++){
                Integer cornerIndex = CornersIndex.get(faceCorners[x1]);

                Double[] uv = faceUV.get(x1);

                Integer uvIndex = 0;
                if(!textureCoordinates.containsKey(uv))
                    uvIndex = textureCoordinates.put(uv);
                else
                    uvIndex = textureCoordinates.getIndex(uv);


                indices.add(new Integer[]{cornerIndex, uvIndex, cornerIndex});
            }

            materialFaces.add(indices);

            faces.put(faceMaterial,materialFaces);

        }

        setMaterialFaces(faces);
    }

    /**
     * Check if the adjacent block is a non block (ex, air, sapling...)
     * @param adjacent The adjacent block
     * @return True if adjacent block is a non block, else false
     */
    public boolean isNonBlockAdjacent(Namespace adjacent){
        return (adjacent.getName().equals("air")) ||
                (adjacent.getType().equals("carpet")) ||
                (adjacent.getName().equals("end_rod")) ||
                (adjacent.getType().equals("sapling")) ||
                (adjacent.getType().equals("double_plant")) ||
                (adjacent.getType().contains("_flower")) ||
                (adjacent.getType().contains("flower_")) ||
                (adjacent.getType().equals("lever")) ||
                (adjacent.getType().contains("_comparator")) ||
                (adjacent.getType().contains("_repeater")) ||
                (adjacent.getType().contains("button")) ||
                (adjacent.getType().equals("skull")) ||
                (adjacent.getType().contains("redstone_torch")) ||
                (adjacent.getType().equals("snow_layer"));
    }

    /**
     * Check if the adjacent block is liquid
     * @param adjacent The adjacent block
     * @return True if adjacent block is liquid, else false
     */
    public boolean isLiquidAdjacent(Namespace adjacent) {
        return false;
    }

    /**
     * Set the cube corner heights, depending on the adjacent block
     * @param liquidLevel The level of the liquid 0-7
     * @param cornerHeights A 4 size Double array, where each element is represents a corner (0: north west, 1: north east, 2: south east, 3: south west)
     * @param adjacent The adjacent block namespace
     * @param orientation The orientation of the adjacent block (ex, north, north_east...)
     * @param hasLiquidAdjacent True if block has any liquid adjacent
     * @param hasLiquidDown True if block has liquid bellow
     * @param liquidMatrix 2D Boolean array Array which shows if adjacent block has a liquid
     */
    public void setCornersFromAdjacentBlock(int liquidLevel, Double[] cornerHeights, Namespace adjacent, String orientation, boolean hasLiquidAdjacent, boolean hasLiquidDown, FLOW_DIRECTION flowDirection, boolean[][] liquidMatrix){
        Double cornerHeight = null;

        if (!hasLiquidAdjacent) {
            //Check if adjacent block is a non block and set the corner height to default liquid level
            if (adjacent == null || isNonBlockAdjacent(adjacent)) {
                switch (liquidLevel) {
                    case 0:
                        cornerHeight = ZERO_LIQUID_LEVEL;
                        break;
                    case 1:
                        cornerHeight = MAX_LIQUID_LEVEL;
                        break;
                    case 7:
                        cornerHeight = MIN_LIQUID_LEVEL;
                        break;
                    default:
                        cornerHeight = ((liquidLevel - 1) * (MAX_LIQUID_LEVEL - MIN_LIQUID_LEVEL)) / 6;
                        break;
                }

                Integer[] cornerIndex = getCornerIndex(orientation);
                for (Integer index : cornerIndex)
                    setLiquidCornerHeight(cornerHeights, cornerHeight, index);
                //setDefaultCornerHeight(cornerHeights, cornerHeight, index);

            } else { //Else adjacent block is a regular block, and set corner height to default liquid level + 1
                /*switch (liquidLevel) {
                    case 0:
                        cornerHeight = ZERO_LIQUID_LEVEL + 1.0;
                        break;
                    case 1:
                        cornerHeight = MAX_LIQUID_LEVEL + 1.0;
                        break;
                    case 7:
                        cornerHeight = MIN_LIQUID_LEVEL + 1.0;
                        break;
                    default:
                        cornerHeight = (((liquidLevel - 1) * (MAX_LIQUID_LEVEL - MIN_LIQUID_LEVEL)) / 6) + 1.0;
                        break;
                }*/

                double abscissa = liquidLevel > 0 ? -1 * liquidLevel : 0;

                cornerHeight = DEFAULT_LIQUID_COEFFICIENT * abscissa + DEFAULT_LIQUID_VERTICAL_INTERCEPT;

                Integer[] cornerIndex = getCornerIndex(orientation);
                for (Integer index : cornerIndex)
                  setLiquidCornerHeight(cornerHeights, cornerHeight, index);
                //setCornerHeight(cornerHeights, cornerHeight, index);
            }
        } else {


            boolean isOrientationCorner = orientation.equals("north_east") || orientation.equals("north_west") || orientation.equals("south_east") || orientation.equals("south_west");

            if(!hasLiquidDown) {
                double abscissa = liquidLevel * -1;
                if (isOrientationCorner)
                    abscissa += abscissa != 0 ? 0.5 : -0.5;

                if (adjacent != null && isLiquidAdjacent(adjacent)) { //Check if adjacent block is liquid

                    int adjacentLiquidLevel = getLiquidLevel(adjacent);
                    double intersect_abscissa = 0.0;

                    if (liquidLevel == adjacentLiquidLevel)
                        intersect_abscissa = liquidLevel != 0 ? liquidLevel * -1.0 : 0.0;
                    else if (liquidLevel > adjacentLiquidLevel)
                        intersect_abscissa = (liquidLevel + adjacentLiquidLevel) / -2.0;
                    else
                        intersect_abscissa = ((liquidLevel + adjacentLiquidLevel) / -2.0) - 0.5;

                    cornerHeight = DEFAULT_LIQUID_COEFFICIENT * intersect_abscissa + DEFAULT_LIQUID_VERTICAL_INTERCEPT;

                } else if (adjacent == null || isNonBlockAdjacent(adjacent)) { //Check if adjacent block in a non block
                    cornerHeight = NON_BLOCK_LIQUID_COEFFICIENT * abscissa + NON_BLOCK_LIQUID_VERTICAL_INTERCEPT;
                } else { //Else adjacent block is a regular block
                    cornerHeight = DEFAULT_LIQUID_COEFFICIENT * abscissa + DEFAULT_LIQUID_VERTICAL_INTERCEPT;
                }
            }else{
                //double abscissa = (liquidLevel + 1) * -1;
                double abscissa = (liquidLevel + 8) / -2.0;
                if (isOrientationCorner)
                    abscissa += abscissa != 0 ? 0.5 : -0.5;

                if (adjacent != null && isLiquidAdjacent(adjacent)) { //Check if adjacent block is liquid

                    int adjacentLiquidLevel = getLiquidLevel(adjacent);
                    double intersect_abscissa = 0.0;

                    if (liquidLevel == adjacentLiquidLevel) {
                        if (liquidLevel != 0)
                            intersect_abscissa = (liquidLevel - 1) * -1;
                        else
                            intersect_abscissa = liquidLevel * -1;
                    } else {
                        intersect_abscissa = Math.floor((liquidLevel + adjacentLiquidLevel) / 2.0) * -1;
                    }

                    cornerHeight = DEFAULT_LIQUID_COEFFICIENT * intersect_abscissa + DEFAULT_LIQUID_VERTICAL_INTERCEPT;

                } else if (adjacent == null || isNonBlockAdjacent(adjacent)) { //Check if adjacent block in a non block

                    //Check if the direction the liquidMatrix faces and the flow direction and orientation all face the same way. Ex:
                    /*
                      false true false
                      true true true   => facing north
                      true true true

                      or

                      false false false
                      false true true   => facing north_west
                      false true true

                      If it does negate the cornerHeight, instead of adding it to the corner
                     */
                    if((flowDirection.equals(FLOW_DIRECTION.WEST) && (orientation.equals("west") || orientation.equals("north_west") || orientation.equals("south_west"))) ||
                            (flowDirection.equals(FLOW_DIRECTION.EAST) && (orientation.equals("east") || orientation.equals("north_east") || orientation.equals("south_east"))) ||
                            (flowDirection.equals(FLOW_DIRECTION.NORTH) && (orientation.equals("north") || orientation.equals("north_west") || orientation.equals("north_east"))) ||
                            (flowDirection.equals(FLOW_DIRECTION.SOUTH) && (orientation.equals("south") || orientation.equals("south_west") || orientation.equals("south_east"))) ||
                            (flowDirection.equals(FLOW_DIRECTION.NORTH_EAST) && !liquidMatrix[0][1] && !liquidMatrix[1][2] && liquidMatrix[1][0] && (orientation.equals("north") || orientation.equals("north_east"))) ||
                            (flowDirection.equals(FLOW_DIRECTION.NORTH_WEST) && !liquidMatrix[0][1] && !liquidMatrix[1][0] && liquidMatrix[1][2] && (orientation.equals("north") || orientation.equals("north_west")) ||
                            (flowDirection.equals(FLOW_DIRECTION.SOUTH_EAST) && !liquidMatrix[2][1] && !liquidMatrix[1][2] && liquidMatrix[1][0] && (orientation.equals("south") || orientation.equals("south_east"))) ||
                            (flowDirection.equals(FLOW_DIRECTION.SOUTH_WEST) && !liquidMatrix[2][1] && !liquidMatrix[1][0] && liquidMatrix[1][2] && (orientation.equals("south") || orientation.equals("south_west"))))) {
                        abscissa += -0.5;
                        cornerHeight = NON_BLOCK_LIQUID_COEFFICIENT * abscissa + NON_BLOCK_LIQUID_VERTICAL_INTERCEPT;
                        cornerHeight *= -1;
                    }
                    else{
                        if(liquidLevel == 0)
                            abscissa = 0;
                        else{
                            abscissa = (liquidLevel - 1) * -1;
                        }

                        cornerHeight = DEFAULT_LIQUID_COEFFICIENT * abscissa + DEFAULT_LIQUID_VERTICAL_INTERCEPT;
                    }


                } else { //Else adjacent block is a regular block
                    cornerHeight = DEFAULT_LIQUID_COEFFICIENT * abscissa + DEFAULT_LIQUID_VERTICAL_INTERCEPT;
                }
            }

            Integer[] cornerIndex = getCornerIndex(orientation);
            for (Integer index : cornerIndex)
                setLiquidCornerHeight(cornerHeights, cornerHeight, index);
        }
    }

    /**
     * Return the indexes to the corner that each orientation faces
     * Example, north faces the corner 0 and 1
     * @param orientation The orientation of the adjacent
     * @return A Integer array of corner indexes
     */
    private Integer[] getCornerIndex(String orientation){
        switch (orientation) {
            case "north":
                return new Integer[]{0, 1};
            case "west":
                return new Integer[]{0, 3};
            case "south":
                return new Integer[]{3, 2};
            case "east":
                return new Integer[]{2, 1};
            case "north_east":
                return new Integer[]{1};
            case "north_west":
                return new Integer[]{0};
            case "south_east":
                return new Integer[]{2};
            case "south_west":
                return new Integer[]{3};
        }

        return new Integer[]{};
    }

    private void setLiquidCornerHeight(Double[] cornerHeights, Double height, int index){
        if(cornerHeights[index] == null)
            cornerHeights[index] = height;
        else
            cornerHeights[index] += height;
    }

    /**
     * Set the corner height if the adjacent block is a regular block
     * @param cornerHeights A 4 size Double array, where each element is represents a corner (0: north west, 1: north east, 2: south east, 3: south west)
     * @param height The height of the corner (Max 16)
     * @param index The index of the corner in the cornerHeights (0 -> 3)
     */
    private void setCornerHeight(Double[] cornerHeights, Double height, int index){
        if(cornerHeights[index] == null)
            cornerHeights[index] = height;
        else if(cornerHeights[index] < height)
            cornerHeights[index] = height;
        else if(height.equals(cornerHeights[index])){
            cornerHeights[index] *= 2;
        }
    }

    /**
     * Set the corner height if the adjacent block is a non-block (air, snow layer, saplings...)
     * @param cornerHeights A 4 size Double array, where each element is represents a corner (0: north west, 1: north east, 2: south east, 3: south west)
     * @param height The height of the corner (Max 16)
     * @param index The index of the corner in the cornerHeights (0 -> 3)
     */
    private void setDefaultCornerHeight(Double[] cornerHeights, Double height, int index){
        if(cornerHeights[index] == null)
            cornerHeights[index] = height;
        else if(cornerHeights[index] < height)
            cornerHeights[index] = height;
    }

    private void setWeightedCornerHeight(Double[] cornerHeights, int index, Double ...heights){

        int counter = 0;
        Double sum = 0.0;

        for(Double height : heights){
            if(height != null){
                counter += 1;
                sum += height;
            }
        }

        cornerHeights[index] =  sum / counter;
    }

    /**
     * Get the direction of the flow
     * @param l Level of liquid at center
     * @param n The north adjacent block
     * @param s The south adjacent block
     * @param w The west adjacent blokc
     * @param e The east adjacent block
     * @param ne The north east adjacent block
     * @param nw The north west adjacent block
     * @param se The south east adjacent block
     * @param sw The south west adjacent block
     * @return The direction of the flow
     */
    private FLOW_DIRECTION getFlowDirection(int l, Namespace n, Namespace s, Namespace w, Namespace e, Namespace ne, Namespace nw, Namespace se, Namespace sw){
        /*int nwc = getLiquidLevel(nw);
        int nc = getLiquidLevel(n);
        int nec = getLiquidLevel(ne);
        int ec = getLiquidLevel(e);
        int wc = getLiquidLevel(w);
        int sec = getLiquidLevel(se);
        int swc = getLiquidLevel(sw);
        int sc = getLiquidLevel(s);*/


        //liquidMatrix of adjacent liquid levels. Ex:
        /*
        -1 -1  7
        -1  7  6
        -1  6  5
         */
        int[][] liquidMatrix = new int[][]{
                {getLiquidLevel(nw), getLiquidLevel(n), getLiquidLevel(ne)},
                {getLiquidLevel(w), l, getLiquidLevel(e)},
                {getLiquidLevel(sw), getLiquidLevel(s), getLiquidLevel(se)}
        };

        if(Constants.LOADED_SCHEMATIC.getPosY() == 6 && liquidMatrix[0][1] == -1 && liquidMatrix[2][1] == 0 && liquidMatrix[1][0] == -1){ //&& liquidMatrix[1][2] == 1){
            String ww = "2";
        }

        //Loop through the matrix and inverse the liquid levels (highest value is 7 and lowest is 0)
        inverseLiquidMatrix(liquidMatrix);

        //Sum the center liquid level and the 4 adjacent corner (north, south, west, east)
        int[] levelSums = new int[]{0,0,0,0};

        levelSums[0] = liquidMatrix[1][1] + liquidMatrix[0][1];
        levelSums[1] = liquidMatrix[1][1] + liquidMatrix[2][1];
        levelSums[2] = liquidMatrix[1][1] + liquidMatrix[1][0];
        levelSums[3] = liquidMatrix[1][1] + liquidMatrix[1][2];

        //Get the minimum sum
        Integer minimumSum = null;
        for(int x : levelSums){
            if(minimumSum == null)
                minimumSum = x;
            else if(minimumSum > x)
                minimumSum = x;
        }

        Integer[] directionVector = new Integer[]{0,0};

        //Loop through the level sums
        for(int d = 0; d < 4; d++){
            int levelSum = levelSums[d];
            //If levelSum equals minimum sum the direction vector with the direction vector of the adjacent corner
            if(minimumSum.equals(levelSum)){
                directionVector[0] += NORTH_SOUTH_WEST_EAST_DIRECTION_VECTORS[d][0];
                directionVector[1] += +NORTH_SOUTH_WEST_EAST_DIRECTION_VECTORS[d][1];
            }
        }

        switch (String.format("%d:%d", directionVector[0],directionVector[1])){
            case "-1:1":
                return FLOW_DIRECTION.NORTH_WEST;
            case "0:1":
                return FLOW_DIRECTION.NORTH;
            case "1:1":
                return FLOW_DIRECTION.NORTH_EAST;
            case "-1:0":
                return FLOW_DIRECTION.WEST;
            case "0:0":
                return FLOW_DIRECTION.STILL;
            case "1:0":
                return FLOW_DIRECTION.EAST;
            case "-1:-1":
                return FLOW_DIRECTION.SOUTH_WEST;
            case "0:-1":
                return FLOW_DIRECTION.SOUTH;
            case "1:-1":
                return FLOW_DIRECTION.SOUTH_EAST;
        }

        /*LinkedHashMap<FLOW_DIRECTION, Integer> directions = new LinkedHashMap<>();

        //North check
        checkAndCountDirection(directions, l, nc, FLOW_DIRECTION.SOUTH_NORTH, FLOW_DIRECTION.NORTH_SOUTH);

        //North east check
        checkAndCountDirection(directions, l, nec, FLOW_DIRECTION.SW_NE, FLOW_DIRECTION.NE_SW);

        //East check
        checkAndCountDirection(directions, l, ec, FLOW_DIRECTION.WEST_EAST, FLOW_DIRECTION.EAST_WEST);

        //South east check
        checkAndCountDirection(directions, l, sec, FLOW_DIRECTION.NW_SE, FLOW_DIRECTION.SE_NW);

        //South check
        checkAndCountDirection(directions, l, sc, FLOW_DIRECTION.NORTH_SOUTH, FLOW_DIRECTION.SOUTH_NORTH);

        //South west check
        checkAndCountDirection(directions, l, swc, FLOW_DIRECTION.NE_SW, FLOW_DIRECTION.SW_NE);

        //West check
        checkAndCountDirection(directions, l, wc, FLOW_DIRECTION.EAST_WEST, FLOW_DIRECTION.WEST_EAST);

        //North west check
        checkAndCountDirection(directions, l, nwc, FLOW_DIRECTION.SE_NW, FLOW_DIRECTION.NW_SE);


        Integer sum = 0;
        //Sum the count of directions
        for(FLOW_DIRECTION direction : directions.keySet()){
            sum += directions.get(direction);
        }

        //Get the mean and ceil it to the largest number
        Double mean = Math.ceil(sum.doubleValue() / directions.size());

        //Check if all directions counts are the mean
        boolean equal_counts = false;

        for(FLOW_DIRECTION direction : directions.keySet()){
            if(directions.get(direction).equals(mean.intValue()))
                equal_counts = true;
            else{
                equal_counts = false;
                break;
            }
        }

        if(!equal_counts) {
            for (FLOW_DIRECTION direction : directions.keySet()) {
                if (directions.get(direction).equals(mean.intValue()))
                    return direction;
            }
        }else{
            if(l == 0 && mean.intValue() == 1)
                return FLOW_DIRECTION.STILL;

            Double middle = Math.ceil(directions.size() / 2.0) - 1;

            int count = 0;
            for(FLOW_DIRECTION direction : directions.keySet()){
                if(count == middle.intValue())
                    return direction;

                count += 1;
            }
        }*/

        return FLOW_DIRECTION.STILL;

    }

    /**
     * Inverse the liquid level matrix.
     * Ex: level 1 -> level 7
     * @param liquidLevelMatrix Ad 2D array representing a matrix with adjacent liquid levels
     * Ex:
     * -1 -1  7
     * -1  7  6
     * -1  6  5
     * @return inverse of liquid level matrix
     */
    private void inverseLiquidMatrix(int[][] liquidLevelMatrix){

        //Get the centerLiquid and inverse it
        int centerLiquid = liquidLevelMatrix[1][1];
        centerLiquid = 8 - centerLiquid;

        int[][] originalLiquidMatrix = new int[liquidLevelMatrix.length][];
        for(int c = 0; c < liquidLevelMatrix.length; c++)
            originalLiquidMatrix[c] = Arrays.copyOf(liquidLevelMatrix[c], liquidLevelMatrix[c].length);

        for(int y = 0; y < liquidLevelMatrix.length; y++){
            int[] row = originalLiquidMatrix[y];
            for(int x = 0; x < row.length; x++){

                int liquidLevel = row[x];

                if(liquidLevel == -1){
                    int[] adjacentLiquidLevels = new int[]{};

                    if(x == 0 && y == 0)
                        adjacentLiquidLevels = new int[]{
                                originalLiquidMatrix[2][2],
                                originalLiquidMatrix[2][1],
                                originalLiquidMatrix[1][2]};
                    else if(x == 1 && y == 0)
                        adjacentLiquidLevels = new int[]{
                                originalLiquidMatrix[2][0],
                                originalLiquidMatrix[2][0],
                                originalLiquidMatrix[2][2]};
                    else if(x == 2 && y == 0)
                        adjacentLiquidLevels = new int[]{
                                originalLiquidMatrix[2][0],
                                originalLiquidMatrix[2][1],
                                originalLiquidMatrix[1][0]};
                    else if(x == 0 && y == 1)
                        adjacentLiquidLevels = new int[]{
                                originalLiquidMatrix[1][2],
                                originalLiquidMatrix[0][2],
                                originalLiquidMatrix[2][2]};
                    else if(x == 2 && y == 1)
                        adjacentLiquidLevels = new int[]{
                                originalLiquidMatrix[1][0],
                                originalLiquidMatrix[0][0],
                                originalLiquidMatrix[2][0]};
                    else if(x == 0 && y == 2)
                        adjacentLiquidLevels = new int[]{
                                originalLiquidMatrix[0][2],
                                originalLiquidMatrix[0][1],
                                originalLiquidMatrix[1][2]};
                    else if(x == 1 && y == 2)
                        adjacentLiquidLevels = new int[]{
                                originalLiquidMatrix[0][1],
                                originalLiquidMatrix[0][0],
                                originalLiquidMatrix[0][2]};
                    else if(x == 2 && y == 2)
                        adjacentLiquidLevels = new int[]{
                                originalLiquidMatrix[0][0],
                                originalLiquidMatrix[0][1],
                                originalLiquidMatrix[1][0]};

                    for(int adjacentLevel : adjacentLiquidLevels){
                        if(adjacentLevel != -1){
                            adjacentLevel = 8 - adjacentLevel;
                            if(centerLiquid > adjacentLevel)
                                liquidLevel = centerLiquid + 1;
                            else
                                liquidLevel = centerLiquid - 1;
                            break;
                        }
                    }

                    if(liquidLevel == -1)
                        liquidLevel = centerLiquid - 1;

                }else{
                    liquidLevel = 8 - liquidLevel;
                }

                liquidLevelMatrix[y][x] = liquidLevel;
            }


        }


    }

    /**
     * Check adjacent liquid level to the original liquid and apply the default or opposite direction to the direction counter
     * @param directions SortedMap <key: the direction, value: count of appearances>
     * @param l The original liquid level
     * @param al The adjacent liquid level
     * @param d The default direction
     * @param o The opposite direction
     */
    private void checkAndCountDirection(LinkedHashMap<FLOW_DIRECTION, Integer> directions, int l, int al, FLOW_DIRECTION d, FLOW_DIRECTION o){
        if(al == -1)
            countDirection(directions, d);
        else if(l > al)
            countDirection(directions, o);
        else if(l < al)
            countDirection(directions, d);
    }

    /**
     * Count the number of times a direction gets added
     * @param directions SortedMap <key: the direction, value: count of appearances>
     * @param direction The added direction
     */
    private void countDirection(LinkedHashMap<FLOW_DIRECTION, Integer> directions, FLOW_DIRECTION direction){
        if(directions.containsKey(direction)){
            directions.put(direction, directions.get(direction) + 1);
        }else
            directions.put(direction, 1);
    }

    /**
     * Get liquid level of adjacent block is a liquid. If the adjacent is null or a non liquid, return -1
     * @param adjacent
     * @return The liquid level
     */
    private int getLiquidLevel(Namespace adjacent){
        if(adjacent == null)
            return -1;

        if(isLiquidAdjacent(adjacent)) {
            Integer l = Integer.parseInt(adjacent.getData().get("level"));
            if(l > 7)
                l = 0;
            return l;
        }
        else
            return -1;
    }

    /**
     * Dump all the added blocks data to obj
     * This should be called after all blocks were added,
     * before the object is written to a file
     */
    public void finalizeObject(){

        //Loop through the liquid blocks and create them
        if(!LiquidBlocksCollection.isEmpty()){
            for(int y : LiquidBlocksCollection.keySet()){
                Map<Integer, Map<Integer, LiquidBlock>> layer = LiquidBlocksCollection.get(y);

                for(int x : layer.keySet()){
                    Map<Integer, LiquidBlock> collumn = layer.get(x);

                    for(int z : collumn.keySet()){
                        LiquidBlock liquidBlock = collumn.get(z);

                        if(liquidBlock.isGenerated())
                            continue;

                        //Construct the corner heights by getting the average of the sum of each corner and It's adjacent corners
                        Double[] cornerHeights = new Double[]{null, null, null, null};

                        LiquidBlock north = collumn.getOrDefault(z - 1, null);
                        LiquidBlock south = collumn.getOrDefault(z + 1, null);

                        Map<Integer, LiquidBlock> westColumn = layer.getOrDefault(x - 1, new HashMap<>());
                        LiquidBlock west = westColumn.getOrDefault(z, null);
                        LiquidBlock north_west = westColumn.getOrDefault(z - 1, null);
                        LiquidBlock south_west = westColumn.getOrDefault(z + 1, null);

                        Map<Integer, LiquidBlock> eastColumn = layer.getOrDefault(x + 1, new HashMap<>());
                        LiquidBlock east = eastColumn.getOrDefault(z, null);
                        LiquidBlock north_east = eastColumn.getOrDefault(z - 1, null);
                        LiquidBlock south_east = eastColumn.getOrDefault(z + 1, null);

                        boolean full_north_west_corner = false;
                        boolean full_north_east_corner = false;
                        boolean full_south_east_corner = false;
                        boolean full_south_west_corner = false;

                        if(LiquidBlocksCollection.containsKey(y + 1)){
                            Boolean[] checkCorner = new Boolean[]{false, false, false, false};
                            hasLiquidAdjacent(y + 1, x, z, checkCorner);

                            full_north_west_corner = checkCorner[0];
                            full_north_east_corner = checkCorner[1];
                            full_south_east_corner = checkCorner[2];
                            full_south_west_corner = checkCorner[3];

                        }

                        //Construct north west corner
                        if(!full_north_west_corner)
                            setWeightedCornerHeight(cornerHeights, 0,
                                    liquidBlock.getNorthWestCorner(),
                                    west != null ? west.getNorthEastCorner() : null,
                                    north_west != null ? north_west.getSouthEastCorner() : null,
                                    north != null ? north.getSouthWestCorner(): null);
                        else
                            cornerHeights[0] = 16.0;

                        //Construct north east corner
                        if(!full_north_east_corner)
                            setWeightedCornerHeight(cornerHeights, 1,
                                    liquidBlock.getNorthEastCorner(),
                                    east != null ? east.getNorthWestCorner() : null,
                                    north_east != null ? north_east.getSouthWestCorner() : null,
                                    north != null ? north.getSouthEastCorner(): null);
                        else
                            cornerHeights[1] = 16.0;

                        //Construct south east corner
                        if(!full_south_east_corner)
                            setWeightedCornerHeight(cornerHeights, 2,
                                    liquidBlock.getSouthEastCorner(),
                                    east != null ? east.getSouthWestCorner() : null,
                                    south_east != null ? south_east.getNorthWestCorner() : null,
                                    south != null ? south.getNorthEastCorner(): null);
                        else
                            cornerHeights[2] = 16.0;

                        //Construct south west corner
                        if(!full_south_west_corner)
                            setWeightedCornerHeight(cornerHeights, 3,
                                    liquidBlock.getSouthWestCorner(),
                                    west != null ? west.getSouthEastCorner() : null,
                                    south_west != null ? south_west.getNorthEastCorner() : null,
                                    south != null ? south.getNorthWestCorner(): null);
                        else
                            cornerHeights[3] = 16.0;

                        //Finally, create a obj from the corner heights
                        createBlock(liquidBlock.getLiquidNamespace(), cornerHeights, liquidBlock.isLiquidUp(), liquidBlock.isLiquidDown(), liquidBlock.isLiquidNorth(), liquidBlock.isLiquidSouth(), liquidBlock.isLiquidEast(), liquidBlock.isLiquidWest(), liquidBlock.getFlowDirection(), x, y, z);
                    }

                }
            }
        }


        //Create normals for object
        WavefrontUtility.createNormals(normalsArray, vertices, getMaterialFaces());

        //Get vertex list
        ArrayList<Double[]> verticesArray = vertices.toList();

        //Normalize vertex normals
        WavefrontUtility.normalizeNormals(normalsArray);

        setVertices(verticesArray);
        setVertexNormals(normalsArray);
        setTextureCoordinates(textureCoordinates.toList());
    }

    /**
     *
     * @param layerIndex They y index of the layer
     * @param x The x index of the block
     * @param z The z index of the block
     * @param checkCorners A 4 length Boolean array [north_west_corner, north_east_corner, south_east_corner, south_west_corner]
     */
    private void hasLiquidAdjacent(int layerIndex, int x, int z, Boolean[] checkCorners){
        Map<Integer, Map<Integer, LiquidBlock>> layer = LiquidBlocksCollection.get(layerIndex);


        Map<Integer, LiquidBlock> collumn = layer.getOrDefault(x, new HashMap<>());
        LiquidBlock north = collumn.getOrDefault(z - 1, null);
        LiquidBlock south = collumn.getOrDefault(z + 1, null);

        Map<Integer, LiquidBlock> westernColumn = layer.getOrDefault(x - 1, new HashMap<>());
        LiquidBlock west = westernColumn.getOrDefault(z, null);
        LiquidBlock northWest = westernColumn.getOrDefault(z - 1, null);
        LiquidBlock southWest = westernColumn.getOrDefault(z + 1, null);

        Map<Integer, LiquidBlock> easternColumn = layer.getOrDefault(x + 1, new HashMap<>());
        LiquidBlock east = easternColumn.getOrDefault(z, null);
        LiquidBlock northEast = easternColumn.getOrDefault(z - 1, null);
        LiquidBlock southEast = easternColumn.getOrDefault(z + 1, null);

        checkCorners[0] = north != null || northWest != null || west != null; //north_west_corner
        checkCorners[1] = north != null || northEast != null || east != null; //north_east_corner
        checkCorners[2] = east != null || southEast != null || south != null; //south_east_corner
        checkCorners[3] = south != null || southWest != null || west != null; //south_west_corner
    }

    public class LiquidBlock {
        private Namespace liquidNamespace;

        private double nw;
        private double ne;
        private double sw;
        private double se;

        private boolean liquidUp;
        private boolean liquidDown;
        private boolean liquidNorth;
        private boolean liquidSouth;
        private boolean liquidWest;
        private boolean liquidEast;

        private FLOW_DIRECTION flowDirection;

        private boolean generated;

        public LiquidBlock(Namespace liquidNamespace, Double[] cornerHeights, boolean hasLiquidUp, boolean hasLiquidDown, boolean hasLiquidNorth, boolean hasLiquidSouth, boolean hasLiquidWest, boolean hasLiquidEast, FLOW_DIRECTION flowDirection, boolean generated){
            this.liquidNamespace = liquidNamespace;

            nw = cornerHeights[0];
            ne = cornerHeights[1];
            sw = cornerHeights[3];
            se = cornerHeights[2];

            liquidUp = hasLiquidUp;
            liquidDown = hasLiquidDown;
            liquidNorth = hasLiquidNorth;
            liquidSouth = hasLiquidSouth;
            liquidEast = hasLiquidEast;
            liquidWest = hasLiquidWest;

            this.flowDirection = flowDirection;

            this.generated = generated;
        }

        public Namespace getLiquidNamespace() {
            return liquidNamespace;
        }

        public double getNorthEastCorner() {
            return ne;
        }

        public double getNorthWestCorner() {
            return nw;
        }

        public double getSouthEastCorner() {
            return se;
        }

        public double getSouthWestCorner() {
            return sw;
        }

        public boolean isLiquidDown() {
            return liquidDown;
        }

        public boolean isLiquidUp() {
            return liquidUp;
        }

        public boolean isLiquidNorth() {
            return liquidNorth;
        }

        public boolean isLiquidSouth() {
            return liquidSouth;
        }

        public boolean isLiquidWest() {
            return liquidWest;
        }

        public boolean isLiquidEast() {
            return liquidEast;
        }

        public FLOW_DIRECTION getFlowDirection() {
            return flowDirection;
        }

        public boolean isGenerated() {
            return generated;
        }
    }

}