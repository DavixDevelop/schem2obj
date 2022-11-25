package com.davixdevelop.schem2obj.schematic;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.List;

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

    public int getOriginX(){return schematic.originX;}
    public int getOriginY(){return schematic.originY;}
    public int getOriginZ(){return schematic.originZ;}

    public Namespace getNamespace(String id){
        return Constants.BLOCK_MAPPING.getBlockNamespace(id);
    }

    public Namespace getNamespace(int x, int y, int z){
        if(x < schematic.getWidth() && x >= 0 && y < schematic.getHeight() && y >= 0 && z < schematic.getLength() && z >= 0){

            final int index = x + (y * schematic.getLength() + z) * schematic.getWidth();

            int blockID = schematic.getBlocks()[index];

            int meta = schematic.getData()[index];

            //If blockID is negative add 256 to the id to get the actual id of the block
            if(blockID < 0)
                blockID += 256;

            return getNamespace(blockID + ":" + meta);
        }

        return null;
    }

    public int getEntitiesCount(){
        return schematic.getEntities().size();
    }

    public Namespace getNamespace(int entityIndex){
        return Constants.ENTITY_MAPPING.getEntityNamespace(getEntityValues(entityIndex).getString("id"));
    }

    public EntityValues getEntityValues(int entityIndex){
        return schematic.entities.get(entityIndex);
    }

    public EntityValues getEntityValues(int x, int y, int z){
        //Construct key:
        String key = String.format("%d:%d:%d", x, y, z);
        return schematic.getTileEntities().get(key);
    }

    public List<EntityValues> getEntities(){
        return schematic.getEntities();
    }
}
