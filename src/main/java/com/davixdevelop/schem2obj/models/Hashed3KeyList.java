package com.davixdevelop.schem2obj.models;

import java.util.ArrayList;
import java.util.HashMap;

public class Hashed3KeyList<T> {
    private ArrayList<T[]> list;
    private HashMap<Object, HashMap<Object, HashMap<Object, Integer>>> keys;

    public Hashed3KeyList(){
        list = new ArrayList<>();
        keys = new HashMap<>();
    }



    public T[] get(Integer index){
        return list.get(index);
    }

    public Integer getIndex(Object key1, Object key2, Object key3){
        Integer index = keys.get(key1).get(key2).get(key3);
        return index;
    }

    /**
     * Add array list of T[] items to collection and return the indexes of each added item in array
     * If an T[] already exist return the existing index, else add it
     * @param list The list of T[] item to append
     * @return An list of indexes to each T[] item
     */
    public Integer[] addAll(ArrayList<T[]> list){
        Integer[] indexes = new Integer[list.size()];
        for(int c = 0; c < list.size(); c++){
            T[] item = list.get(c);
            indexes[c] = add(item);
        }

        return indexes;
    }

    public Integer add(T[] item){

        if(keys.containsKey(item[0])){
            HashMap<Object, HashMap<Object, Integer>> keys2 = keys.get(item[0]);
            if(!keys2.containsKey(item[1])){
                list.add(item);
                HashMap<Object, Integer> keys3 = new HashMap<>();
                keys3.put(item[2], list.size() - 1);
                keys2.put(item[1], keys3);
                keys.put(item[0], keys2);
                return list.size() - 1;
            }else{
                HashMap<Object, Integer> keys3 = keys2.get(item[1]);
                if(!keys3.containsKey(item[2])){
                    list.add(item);
                    keys3.put(item[2], list.size() - 1);
                    keys2.put(item[1], keys3);
                    keys.put(item[0], keys2);
                    return  list.size() - 1;
                }else
                    return getIndex(item[0], item[1], item[2]);
            }
        }

        list.add(item);
        HashMap<Object, HashMap<Object, Integer>> keys2 = new HashMap<>();
        HashMap<Object, Integer> keys3 = new HashMap<>();
        keys3.put(item[2], list.size() - 1);
        keys2.put(item[1], keys3);
        keys.put(item[0], keys2);

        return list.size() - 1;
    }

    public Boolean containsKey(Object key1, Object key2, Object key3){
        if(keys.containsKey(key1))
            if(keys.get(key1).containsKey(key2))
                if(keys.get(key1).get(key2).containsKey(key3))
                    return true;

        return false;
    }


    public Integer size(){
        return list.size();
    }

    public ArrayList<T[]> toList(){
        return list;
    }

    /**
     * Update an T[] item in the internal list at the specified index. Make sure to not update the first tree keys
     * @param index The index of the T[] item
     * @param item The updated T[] item
     */
    public void update(Integer index, T[] item){
        list.set(index, item);
    }

    public boolean isEmpty(){
        return list.isEmpty();
    }
}
