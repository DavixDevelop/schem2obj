package com.davixdevelop.schem2obj.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class HashedDoubleList {
    private ArrayList<Double[]> list;
    private HashMap<String, Integer> index;


    public HashedDoubleList(){
        list = new ArrayList<>();
        index = new HashMap<>();
    }

    public boolean containsKey(Double... values){
        return index.containsKey(createKey(values));
    }

    public int put(Double... values){
        list.add(values.clone());
        index.put(createKey(values), list.size() - 1);

        return list.size() - 1;
    }

    public Double[] get(Integer index){
        return list.get(index);
    }

    public Integer getIndex(Double... values){
        return index.get(createKey(values));
    }

    public ArrayList<Double[]> toList(){
        return list;
    }

    public int size(){
        return list.size();
    }

    private String createKey(Double... values){
        String key = "";
        boolean first = true;
        for(Double val : values){
            key += String.format(Locale.ROOT, "%s%.6f",(first) ? "" : ":",val);
            first = false;
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
