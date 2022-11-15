package com.davixdevelop.schem2obj.schematic;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;

/**
 * Represents a schematic, with methods for reading.
 * Optimized for block ID's of Minecraft 1.12.2
 * @author DavixDevelop
 *
 */
public class Schematic implements java.io.Serializable {
    private int[] blocks;
    private int[] data;
    private short width;
    private short length;
    private short height;
    private Map<String, EntityValues>  tileEntities;
    private List<EntityValues> entities;

    public Schematic(int[] blocks, int[] data, short width, short length, short height, Map<String, EntityValues>  tileEntities, List<EntityValues> entities) {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.length = length;
        this.height = height;
        this.tileEntities = tileEntities;
        this.entities = entities;
    }

    public int[] getBlocks() {
        return blocks;
    }

    public int[] getData() {
        return data;
    }

    public short getWidth() {
        return width;
    }

    public short getLength() {
        return length;
    }

    public short getHeight() {
        return height;
    }

    public Map<String, EntityValues> getTileEntities(){
        return tileEntities;
    }

    public List<EntityValues> getEntities() {
        return entities;
    }

    /**
     * This method read's a schematic and initializes the class object, to be used later
     * @param stream A InputStream from a schematic resource
     * @return new instance of Schematic
     * @throws IOException
     */
    public static Schematic loadSchematic(InputStream stream) throws IOException {

        NBTInputStream nbtInputStream = new NBTInputStream(stream);

        Tag rootTag = nbtInputStream.readTag();

        if(!rootTag.getType().equals(TagType.TAG_COMPOUND))
            throw new IOException("Doesn't start with Compund tag");

        CompoundMap nbtData = ((CompoundTag) rootTag).getValue();

        //NBTTagCompound nbtData = CompressedStreamTools.readCompressed(stream);

        short width = ((ShortTag)nbtData.get("Width")).getValue();
        short length = ((ShortTag)nbtData.get("Length")).getValue();
        short height = ((ShortTag)nbtData.get("Height")).getValue();

        byte[] blockId = ((ByteArrayTag)nbtData.get("Blocks")).getValue();
        byte[] blockData = ((ByteArrayTag)nbtData.get("Data")).getValue();

        boolean extras = false;
        byte extraBlocks[] = null;
        byte extraBlocksNibble[] = null;
        int[] blocks = new int[blockId.length];
        int[] data = new int[blockData.length];

        if (nbtData.containsKey("AddBlocks")) {
            extras = true;
            extraBlocksNibble = ((ByteArrayTag)nbtData.get("AddBlocks")).getValue();
            extraBlocks = new byte[extraBlocksNibble.length * 2];
            for(int i = 0; i < extraBlocksNibble.length; i++) {
                extraBlocks[i * 2 + 0] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
                extraBlocks[i * 2 + 1] = (byte) (extraBlocksNibble[i] & 0xF);
            }
        }

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int z = 0; z < length; z++) {
                    final int index = x + (y * length + z) * width;
                    int blockID = (blockId[index] & 0xFF) | (extras ? ((extraBlocks[index] & 0xFF) << 8) : 0);
                    final int meta = blockData[index] & 0xFF;
                    blocks[index] = blockID;
                    data[index] = meta;
                }
            }
        }

        Map<String, EntityValues> tileEntities = new HashMap<>();

        ListTag rawTileEntities = (ListTag)nbtData.get("TileEntities");

        //Loop through the entities
        for(Object tag : rawTileEntities.getValue()){
            //Check if tag is of type TAG_COMPOUND
            if(tag instanceof CompoundTag){
                CompoundTag compoundTag = (CompoundTag) tag;
                //Get the CompoundMap
                CompoundMap compoundMap = compoundTag.getValue();

                //Get the position of the entity and remove it from the compoundMap
                IntTag xTag = (IntTag) compoundMap.get("x");
                IntTag yTag = (IntTag) compoundMap.get("y");
                IntTag zTag = (IntTag) compoundMap.get("z");
                compoundMap.remove("x");
                compoundMap.remove("y");
                compoundMap.remove("z");

                //Parse through the compoundMap
                EntityValues values = new EntityValues();
                values.parseCompoundMap(compoundMap);

                tileEntities.put(String.format("%d:%d:%d", xTag.getValue(), yTag.getValue(), zTag.getValue()), values);
            }
        }

        List<EntityValues> entities = new ArrayList<>();

        ListTag rawEntities = (ListTag)nbtData.get("Entities");

        for(Object tag : rawEntities.getValue()){
            if(tag instanceof CompoundTag){
                CompoundTag compoundTag = (CompoundTag) tag;
                //Get the CompoundMap
                CompoundMap compoundMap = compoundTag.getValue();

                //Parse through the compoundMap
                EntityValues values = new EntityValues();
                values.parseCompoundMap(compoundMap);

                entities.add(values);
            }
        }

        stream.close();

        return new Schematic(blocks, data, width, length, height, tileEntities, entities);
    }
}
