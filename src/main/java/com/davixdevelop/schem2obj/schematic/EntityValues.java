package com.davixdevelop.schem2obj.schematic;

import com.flowpowered.nbt.*;
import com.google.gson.Gson;

import java.util.*;

public class EntityValues {
    Map<String, Object> map;

    static Gson GSON = new Gson();

    public EntityValues(){
        map = new HashMap<>();
    }

    public void put(String key, Object value){
        map.put(key, value);
    }

    public Object get(String key){
        return map.get(key);
    }

    public Set<String> keySet(){
        return map.keySet();
    }

    public void parseMap(Map<String, Object> map1){
        for(String key : map1.keySet()){
            Object item = map1.get(key);
            if(item instanceof Map<?, ?>){
                Map<String, Object> map2 = (Map<String, Object>) item;
                EntityValues entityValues = new EntityValues();
                entityValues.parseMap(map2);
                map.put(key, entityValues);
            }else
                map.put(key, item);
        }
    }

    public void parseCompoundMap(CompoundMap compoundMap){
        for(String key : compoundMap.keySet()){
            Tag<?> tag = compoundMap.get(key);
            map.put(key, parseTag(tag));
        }

    }

    public boolean containsKey(String key){
        return map.containsKey(key);
    }

    public static Object parseTag(Tag<?> tag){
        switch (tag.getType()){
            case TAG_INT:
                return ((IntTag) tag).getValue();
            case TAG_STRING:
                String value = ((StringTag)tag).getValue();
                //If value starts with a { and end with }  It's JSON text
                if(value.startsWith("{") & value.endsWith("}")){
                    EntityValues entityValues = new EntityValues();
                    Map<?, ?> values = GSON.fromJson(value, Map.class);
                    parseJsonMap(values, entityValues);

                    return entityValues;
                }
                return value;
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

                List<?> listTag = ((ListTag<?>)tag).getValue();
                for(Object item : listTag){
                    Tag<?> tag1 = (Tag<?>) item;
                    entityValuesList.add(parseTag(tag1));
                }
                return entityValuesList;
        }
        //com.flowpowered.nbt.EndTag

        return null;
    }

    public static void parseJsonMap(Map<?, ?> map, EntityValues entityValues){
        for(Object key : map.keySet()){
            Object val = map.get(key);

            if(val instanceof String)
                entityValues.map.put((String)key, val);
            else if(val instanceof Map){
                EntityValues entityValues1 = new EntityValues();
                parseJsonMap((Map<?, ?>) val, entityValues1);
                entityValues.map.put((String)key, entityValues1);
            }else if(val instanceof List){
                List<?> list = (List<?>) val;
                List<Object> entityValuesList = new ArrayList<>(list);
                entityValues.map.put((String) key, entityValuesList);
            }
        }
    }

    public EntityValues getEntityValues(String key){
        return (EntityValues)map.getOrDefault(key, new EntityValues());
    }

    public Integer getInteger(String key){
        return getIntegerValue(map.getOrDefault(key, 0));
    }

    public static Integer getIntegerValue(Object value){
        if(value instanceof Byte)
            return ((Byte)value) & 0xFF;
        else if(value instanceof Short)
            return ((Short)value).intValue();

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
        if(value instanceof Short)
            return ((Short)value).toString();

        return value.toString();
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
        if(value instanceof Short)
            return ((Short)value).byteValue();
        if(value instanceof Double)
            return ((Double)value).byteValue();
        if(value instanceof Integer)
            return ((Integer)value).byteValue();

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
        if(value instanceof Short)
            return ((Short)value).longValue();
        return (Long) value;
    }

    public List<?> getList(String key) {
        Object list = map.get(key);
        if(list instanceof List<?>){
            return (List<?>) map.get(key);
        }
        return null;
    }

    public List<Float> getFloatList(String key){
        List<?> rawList = getList(key);
        List<Float> list = new ArrayList<>();
        for(Object rawValue : rawList)
            list.add((Float) rawValue);

        return list;
    }

    public List<Double> getDoubleList(String key){
        List<?> rawList = getList(key);
        List<Double> list = new ArrayList<>();
        for(Object rawValue : rawList)
            list.add((Double) rawValue);

        return list;
    }

    public EntityValues duplicate(){
        EntityValues entityValues = new EntityValues();
        Map<String, Object> cloneMap = new LinkedHashMap<>();
        for(String key : map.keySet()){
            Object item = map.get(key);
            if(item instanceof EntityValues){
                EntityValues entityValues1 = (EntityValues) item;
                cloneMap.put(key, entityValues1.duplicate());
            }else
                cloneMap.put(key, item);
        }

        entityValues.map = cloneMap;

        return entityValues;
    }

    public boolean isEmpty(){
        return map.isEmpty();
    }

}
