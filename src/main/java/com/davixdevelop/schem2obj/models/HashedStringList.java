package com.davixdevelop.schem2obj.models;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A String List that uses a Map to store the indexes to each element in the list
 * @author DavixDevelop
 */
public class HashedStringList {
    private List<String> list;
    private Map<String, Integer> index;

    public HashedStringList(){
        list = new ArrayList<>();
        index = new HashMap<>();
    }

    /**
     * Get element from the index
     * @param index Index to the String element
     * @return The element at the specified index
     */
    public String get(Integer index){
        return list.get(index);
    }

    /**
     * Get the index to the String element
     * @param value The String element
     * @return The index to the String element
     */
    public Integer getIndex(String value){
        return index.get(value);
    }

    /**
     * Put a new String element if it does not exist yet, and return the index to it
     * @param value A new String
     * @return The index to the new element
     */
    public Integer put(String value){
        if(index.containsKey(value))
            return index.get(value);

        list.add(value);
        index.put(value, list.size() -1);

        return list.size() - 1;
    }

    /**
     * Return the list of String
     * @return A list of Strings
     */
    public List<String> toList(){
        return new ArrayList<>(list);
    }



    /**
     * Clone the hashed string list
     * @return The clone of the object
     */
    public HashedStringList duplicate(){
        HashedStringList clone = new HashedStringList();
        clone.list = new ArrayList<>(list);
        clone.index = new HashMap<>(index.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return clone;
    }


}
