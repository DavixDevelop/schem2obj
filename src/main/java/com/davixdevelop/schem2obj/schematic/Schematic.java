package com.davixdevelop.schem2obj.schematic;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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
    private ListTag tileEntities;

    public Schematic(int[] blocks, int[] data, short width, short length, short height, ListTag tileEntities) {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.length = length;
        this.height = height;
        this.tileEntities = tileEntities;
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

    public ListTag getTileEntities(){
        return tileEntities;
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

        ListTag tileEntities = (ListTag)nbtData.get("TileEntities");

        stream.close();

        return new Schematic(blocks, data, width, length, height, tileEntities);
    }
}
