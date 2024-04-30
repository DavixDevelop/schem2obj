package com.davixdevelop.schem2obj.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An utility class for manipulating with array and arraylists
 * @author DavixDevelop
 */
public class ArrayUtility {
    /**
     * This method flatten the values in a double array to the max_value (ex. 16 becomes 1.0 if max_value is 16)
     * @param in The input array to flatten
     * @param max_value The value with which to subdivide each array element
     * @return The flattened array
     */
    public static Double[] flattenArray(Double[] in, Integer max_value){
        for(int c = 0; c < in.length; c++)
            in[c] = in[c] / max_value.doubleValue();

        return in;
    }
    /**
     * This method combines two arrays of objects
     * @param first The first part of the combined array
     * @param second The second part of the combined array
     * @return The combined array
     */
    public static Double[] combineArray(Double[] first, Double[] second){
        Double[] combinedArray = new Double[first.length + second.length];
        for(int c = 0; c < combinedArray.length; c++){
            if(c < first.length)
                combinedArray[c] = first[c];
            else
                combinedArray[c] = second[c - first.length];
        }

        return combinedArray;
    }


    /**
     * Split an array in two
     * @param array The input array
     * @param size The size of the first part of the array
     * @return And two element Object array, where the two elements are the split array
     */
    public static Object[] splitArray(Double[] array, Integer size){
        Double[] partOne = new Double[size];
        Double[] partTwo = new Double[size];

        for(int c = 0; c < array.length; c++){
            if(c < size){
                partOne[c] = array[c];
            }else{
                partTwo[c - size] = array[c];
            }
        }

        return new Object[]{partOne, partTwo};
    }

    /**
     * Split's an array lists individual items by size to two array list's
     * @param input The input array to split
     * @param size The size of the first array's individual items count
     * @return And Object array consisting of the first and second array list
     */
    public static Object[] splitArrayPairsToLists(ArrayList<Double[]> input, Integer size){
        ArrayList<Double[]> pair1 = new ArrayList<>();
        ArrayList<Double[]> pair2 = new ArrayList<>();
        for (Double[] array : input) {
            Object[] pairs = splitArray(array, size);
            pair1.add((Double[]) pairs[0]);
            pair2.add((Double[]) pairs[1]);
        }

        return new Object[] {pair1, pair2};
    }

    public static boolean arrayContainsOnlyNullElement(Iterable<?> list){
        for(Object obj : list){
            if(obj != null)
                return false;
        }

        return true;
    }

    public static Double[] cloneArray(Double[] org){
        if(org == null)
            return null;

        return Arrays.copyOf(org, org.length);
    }

    public static Double[] floatListToArray(List<Float> floatList){
        Double[] array = new Double[floatList.size()];
        for(int c = 0; c < floatList.size(); c++)
            array[c] = floatList.get(c).doubleValue();

        return array;
    }
}
