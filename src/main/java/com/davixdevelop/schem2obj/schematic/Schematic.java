package com.davixdevelop.schem2obj.schematic;

import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a schematic, with methods for reading.
 * Optimized for block ID's of Minecraft 1.12.2
 * @author DavixDevelop
 *
 */
public class Schematic implements java.io.Serializable {
    int[] blocks;
    int[] data;
    short width;
    short length;
    short height;

    int originX;
    int originY;
    int originZ;

    Map<String, EntityValues>  tileEntities;
    List<EntityValues> entities;

    public Schematic(int[] blocks, int[] data, short width, short length, short height, int originX, int originY, int originZ, Map<String, EntityValues>  tileEntities, List<EntityValues> entities) {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.length = length;
        this.height = height;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.tileEntities = tileEntities;
        this.entities = entities;
    }

    public int[] getBlocks() {
        return blocks;
    }

    public int getBlock(int index){
        return blocks[index];
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

    public int getOriginX() {
        return originX;
    }

    public int getOriginY() {
        return originY;
    }

    public int getOriginZ() {
        return originZ;
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
     * @throws IOException If root tag of schematic isn't a Compound tag
     */
    public static Schematic loadSchematic(InputStream stream) throws IOException {

        NBTInputStream nbtInputStream = new NBTInputStream(stream);

        Tag<?> rootTag = nbtInputStream.readTag();

        if(!rootTag.getType().equals(TagType.TAG_COMPOUND))
            throw new IOException("Doesn't start with Compound tag");

        CompoundMap nbtData = ((CompoundTag) rootTag).getValue();

        //NBTTagCompound nbtData = CompressedStreamTools.readCompressed(stream);

        short width = ((ShortTag)nbtData.get("Width")).getValue();
        short length = ((ShortTag)nbtData.get("Length")).getValue();
        short height = ((ShortTag)nbtData.get("Height")).getValue();

        int offsetX = getOrDefault(((IntTag)nbtData.get("WEOriginX")), 0);
        int offsetY = getOrDefault(((IntTag)nbtData.get("WEOriginY")), 0);
        int offsetZ = getOrDefault(((IntTag)nbtData.get("WEOriginZ")), 0);

        byte[] blockId = ((ByteArrayTag)nbtData.get("Blocks")).getValue();
        byte[] blockData = ((ByteArrayTag)nbtData.get("Data")).getValue();

        boolean extras = false;
        byte[] extraBlocks = null;
        byte[] extraBlocksNibble;
        int[] blocks = new int[blockId.length];
        int[] data = new int[blockData.length];

        if (nbtData.containsKey("AddBlocks")) {
            extras = true;
            extraBlocksNibble = ((ByteArrayTag)nbtData.get("AddBlocks")).getValue();
            extraBlocks = new byte[extraBlocksNibble.length * 2];
            for(int i = 0; i < extraBlocksNibble.length; i++) {
                extraBlocks[i * 2] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
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

        ListTag<?> rawTileEntities = (ListTag<?>)nbtData.get("TileEntities");

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

        ListTag<?> rawEntities = (ListTag<?>)nbtData.get("Entities");

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

        return new Schematic(blocks, data, width, length, height, offsetX, offsetY, offsetZ, tileEntities, entities);
    }

    private static <VALUE, TAG extends Tag<VALUE>> VALUE getOrDefault(TAG tag, VALUE defaultValue) {
        if (tag == null) {
            return defaultValue;
        }
        return tag.getValue();
    }
}
