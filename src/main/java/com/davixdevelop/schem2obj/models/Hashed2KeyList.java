package com.davixdevelop.schem2obj.models;

import java.util.ArrayList;
import java.util.HashMap;

public class Hashed2KeyList<T> {
    private ArrayList<T[]> list;
    private HashMap<Object, HashMap<Object, Integer>> keys;

    public Hashed2KeyList(){
        list = new ArrayList<>();
        keys = new HashMap<>();
    }



    public T[] get(Integer index){
        return list.get(index);
    }

    public Integer getIndex(Object key1, Object key2){
        Integer index = keys.get(key1).get(key2);
        return index;
    }

    public void addAll(ArrayList<T[]> list){
        for(T[] item : list){
            add(item);
        }

    }

    public Integer add(T[] item){

        if(keys.containsKey(item[0])){
            HashMap<Object, Integer> keys2 = keys.get(item[0]);
            if(!keys2.containsKey(item[1])){
                list.add(item);
                keys2.put(item[1], list.size() - 1);
                keys.put(item[0], keys2);

                return list.size() - 1;
            }else{
                return getIndex(item[0], item[1]);
            }
        }

        list.add(item);
        HashMap<Object, Integer> keys2 = new HashMap<>();
        keys2.put(item[1], list.size() - 1);
        keys.put(item[0], keys2);

        return list.size() - 1;
    }

    public Boolean containsKey(Object key1, Object key2){
        if(keys.containsKey(key1))
            if(keys.get(key1).containsKey(key2))
                return true;

        return false;
    }


    public Integer size(){
        return list.size();
    }

    public ArrayList<T[]> toList(){
        return list;
    }
}
