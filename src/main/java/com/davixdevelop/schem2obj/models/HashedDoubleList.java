package com.davixdevelop.schem2obj.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class HashedDoubleList {
    private ArrayList<Double[]> list;
    private HashMap<String, Integer> index;
    private Integer keySize = 0;


    public HashedDoubleList(Integer keySize){
        list = new ArrayList<>();
        index = new HashMap<>();
        this.keySize = keySize;
    }

    public boolean containsKey(Double... values){
        return index.containsKey(createKey(false, values));
    }

    public int put(Double... values){
        list.add(values.clone());
        index.put(createKey(false, values), list.size() - 1);

        return list.size() - 1;
    }

    public Double[] get(Integer index){
        return list.get(index);
    }

    public Integer getIndex(Double... values){
        return index.get(createKey(false, values));
    }

    public void update(Integer index, Double... values){
        String originalKey = getKey(index);
        this.index.remove(originalKey);

        list.set(index, values.clone());

        this.index.put(createKey(true, values), index);
    }

    public ArrayList<Double[]> toList(){
        return list;
    }

    private String createKey(Boolean ignorekeySize, Double... values){
        String key = "";
        boolean first = true;
        Integer c = 0;
        for(Double val : values){
            key += String.format("%s%f",(first) ? "" : ":",val);
            first = false;
            if(!ignorekeySize){
                c++;
                if(c == keySize)
                    break;
            }
        }

        return key;
    }

    private String getKey(Integer index){
        for(String key : this.index.keySet()){
            if(index.equals(this.index.get(key)))
                return key;
        }

        return null;
    }
}
