package com.davixdevelop.schem2obj.schematic;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.biomes.Biome;
import com.davixdevelop.schem2obj.namespace.Namespace;

import java.util.List;

public class SchematicHolder {
    private Schematic schematic;

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

    public int getOriginX(){return schematic.originX;}
    public int getOriginY(){return schematic.originY;}
    public int getOriginZ(){return schematic.originZ;}

    public Namespace getNamespace(String id, Integer meta){
        return Constants.NAMESPACE_MAPPING.getBlockNamespace(id, meta);
    }

    public Namespace getNamespace(int x, int y, int z){
        if(x < schematic.getWidth() && x >= 0 && y < schematic.getHeight() && y >= 0 && z < schematic.getLength() && z >= 0){

            final int index = x + (y * schematic.getLength() + z) * schematic.getWidth();

            int blockID = schematic.getBlock(index);

            int meta = schematic.getData()[index];

            //If blockID is negative add 256 to the id to get the actual id of the block
            if(blockID < 0)
                blockID += 256;

            Namespace namespace = getNamespace(Integer.toString(blockID), meta);
            namespace.setPosition(new Integer[]{x, y, z});

            return namespace;
        }

        return null;
    }

    public Biome getBiome(int x, int z){
        return schematic.getBiome(x, z);
    }

    public boolean isAirOrLiquid(int blockIndex){
        int ID = schematic.getBlock(blockIndex);
        return ID == 0 || (ID >= 8 && ID <= 11);
    }

    public boolean isLiquid(int blockIndex){
        int ID = schematic.getBlock(blockIndex);
        return (ID >= 8 && ID <= 11);
    }

    public int getEntitiesCount(){
        return schematic.getEntities().size();
    }

    public Namespace getEntityNamespace(int entityIndex){
        Namespace namespace = Constants.NAMESPACE_MAPPING.getNamespace(getEntityValues(entityIndex).getString("id"));
        if(namespace == null)
            return null;

        namespace.setPosition(new Integer[]{entityIndex});
        return namespace;
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
