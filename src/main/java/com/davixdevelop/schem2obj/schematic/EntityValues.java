package com.davixdevelop.schem2obj.schematic;

import com.flowpowered.nbt.*;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityValues {
    private Map<String, Object> map;

    public EntityValues(){
        map = new HashMap<>();
    }

    public void parseCompoundMap(CompoundMap compoundMap){
        for(String key : compoundMap.keySet()){
            Tag tag = compoundMap.get(key);
            map.put(key, parseTag(tag));
        }

    }

    public static Object parseTag(Tag tag){
        switch (tag.getType()){
            case TAG_INT:
                return ((IntTag) tag).getValue();
            case TAG_STRING:
                return  ((StringTag) tag).getValue();
            case TAG_DOUBLE:
                return ((DoubleTag) tag).getValue();
            case TAG_FLOAT:
                return ((FloatTag) tag).getValue();
            case TAG_SHORT:
                return ((ShortTag) tag).getValue();
            case TAG_LONG:
                return ((LongTag) tag).getValue();
            case TAG_BYTE:
                return ((ByteTag) tag).getValue();
            case TAG_BYTE_ARRAY:
                return ((ByteArrayTag) tag).getValue();
            case TAG_INT_ARRAY:
                return ((IntArrayTag)tag).getValue();
            case TAG_SHORT_ARRAY:
                return ((ShortArrayTag)tag).getValue();
            case TAG_COMPOUND:
                EntityValues entityValues1 = new EntityValues();
                entityValues1.parseCompoundMap(((CompoundTag) tag).getValue());
                return entityValues1;
            case TAG_LIST:
                List<Object> entityValuesList = new ArrayList<>();

                List listTag = ((ListTag)tag).getValue();
                for(Object item : listTag){
                    Tag tag1 = (Tag) item;
                    entityValuesList.add(parseTag(tag1));
                }
                return entityValuesList;
        }
        //com.flowpowered.nbt.EndTag

        return null;
    }

    public Integer getInteger(String key){
        return getIntegerValue(map.getOrDefault(key, 0));
    }

    public static Integer getIntegerValue(Object value){
        if(value instanceof Byte)
            return ((Byte)value) & 0xFF;

        return (Integer)value;
    }

    public Integer[] getIntegerArray(String key){
        return getIntegerArrayValue(map.getOrDefault(key, new Integer[]{}));
    }

    public static Integer[] getIntegerArrayValue(Object value){
        return (Integer[]) value;
    }

    public String getString(String key){
        return getStringValue(map.getOrDefault(key, ""));
    }

    public static String getStringValue(Object value){
        return (String)value;
    }

    public Double getDouble(String key){
        return getDoubleValue(map.getOrDefault(key, 0.0));
    }

    public static Double getDoubleValue(Object value){
        return (Double)value;
    }

    public Float getFloat(String key){
        return getFloatValue(map.getOrDefault(key, 0.0));
    }

    public static Float getFloatValue(Object value){
        return (Float) value;
    }

    public Byte getByte(String key){
        return getByteValue(map.getOrDefault(key, 0x00));
    }

    public static Byte getByteValue(Object value){
        return (Byte) value;
    }

    public Byte[] getByteArray(String key){
        return getByteArrayValue(map.getOrDefault(key, new Byte[]{}));
    }

    public static Byte[] getByteArrayValue(Object value){
        return (Byte[]) value;
    }

    public Short getShort(String key){
        return getShortValue(map.getOrDefault(key, 0));
    }

    public static Short getShortValue(Object value){
        return (Short) value;
    }

    public Short[] getShortArray(String key){
        return getShortArrayValue(map.getOrDefault(key, new Short[]{}));
    }

    public static Short[] getShortArrayValue(Object value){
        return (Short[]) value;
    }

    public Long getLong(String key){
        return getLongValue(map.getOrDefault(key, 0));
    }

    public static Long getLongValue(Object value){
        return (Long) value;
    }

}
