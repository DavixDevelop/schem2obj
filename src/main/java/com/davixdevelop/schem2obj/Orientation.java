package com.davixdevelop.schem2obj;

public enum Orientation {
    UP(0, 0, 1),
    DOWN(0, 0, -1),
    NORTH(0, 1, 0),
    SOUTH(0, -1, 0),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    private final int x;
    private final int y;
    private final int z;

    Orientation(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private static final Orientation[] DIRECTIONS = new Orientation[]{UP, DOWN, NORTH, SOUTH, WEST, EAST};
    public static final Integer[] OPPOSITE_DIRECTIONS = new Integer[]{1,0,3,2,5,4};

    public Integer getOrder(){
        return ordinal();
    }

    /**
     * Return a orientation from a index (ex 1 - > DOWN), or from coordinates (ex. 1, 0, 0 - > WEST)
     * @param indexOrCords Orientation index (ex 1 - > DOWN), or coordinates (ex. 1, 0, 0 - > WEST)
     * @return The Orientation
     */
    public static Orientation getOrientation(Integer ...indexOrCords){
        if(indexOrCords.length == 1)
            return DIRECTIONS[indexOrCords[0]];
        else if(indexOrCords.length == 3){
            for(Orientation orientation : DIRECTIONS){
                if(orientation.x == indexOrCords[0] &&
                        orientation.y == indexOrCords[1] &&
                        orientation.z == indexOrCords[2])
                    return orientation;
            }
        }
        return Orientation.UP;
    }

    public static Orientation getOrientation(String orientationName){
        switch (orientationName){
            case "up":
                return  UP;
            case "down":
                return DOWN;
            case "north":
                return NORTH;
            case "south":
                return SOUTH;
            case "east":
                return EAST;
            case "west":
                return WEST;
        }

        return UP;
    }

    public Integer getXOffset() {
        return x;
    }

    public Integer getYOffset() {
        return y;
    }

    public Integer getZOffset() {
        return z;
    }

    public Orientation getOpposite(){
        return DIRECTIONS[OPPOSITE_DIRECTIONS[ordinal()]];
    }
}
