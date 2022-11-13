package com.davixdevelop.schem2obj.schematic;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.namespace.Namespace;

public class SchematicHolder {
    private Schematic schematic;

    private int posX;
    private int posY;
    private int posZ;

    public void setSchematic(Schematic schematic) {
        this.schematic = schematic;
    }

    public short getWidth(){
        return schematic.getWidth();
    }

    public short getHeight(){
        return schematic.getHeight();
    }

    public short getLength(){
        return schematic.getLength();
    }

    public void setCurrentBlockPosition(int x, int y, int z){
        posX = x;
        posY = y;
        posZ = z;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getPosZ() {
        return posZ;
    }

    public Namespace getNamespace(String id){
        return Constants.BLOCK_MAPPING.getBlockNamespace(id);
    }

    public Namespace getNamespace(int x, int y, int z){
        if(x < schematic.getWidth() && x >= 0 && y < schematic.getHeight() && y >= 0 && z < schematic.getLength() && z >= 0){

            final int index = x + (y * schematic.getLength() + z) * schematic.getWidth();

            int blockID = schematic.getBlocks()[index];

            int meta = schematic.getData()[index];

            if(meta == 2)
            {
                String w = "2";
            }

            return getNamespace(blockID + ":" + meta);
        }

        return null;
    }
}
