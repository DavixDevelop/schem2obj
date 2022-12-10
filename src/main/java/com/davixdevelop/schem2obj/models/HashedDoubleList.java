package com.davixdevelop.schem2obj.models;

import com.davixdevelop.schem2obj.util.ArrayUtility;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

/**
 * An Array Double List that uses a Map to store the indexes to each element in the list
 *
 * @author DavixDevelop
 */
public class HashedDoubleList {
    private static final int DEFAULT_PRECISION = 6;

    private final ArrayList<Double[]> list;
    private final HashMap<CachedHashArray, Integer> index;

    private final MathContext context;

    public HashedDoubleList() {
        this(DEFAULT_PRECISION);
    }

    public HashedDoubleList(int hashPrecision) {
        this.context = new MathContext(hashPrecision);
        list = new ArrayList<>();
        index = new HashMap<>();
    }

    public boolean containsKey(Double... values) {
        return index.containsKey(new CachedHashArray(values));
    }

    public int put(Double... values) {
        Double[] clonedArray = ArrayUtility.cloneArray(values);
        list.add(clonedArray);
        index.put(new CachedHashArray(clonedArray), list.size() - 1);

        return list.size() - 1;
    }

    public Double[] get(Integer index) {
        return list.get(index);
    }

    public ArrayList<Double[]> toList() {
        return list;
    }

    public int size() {
        return list.size();
    }

    private class CachedHashArray {
        final Double[] internalArray;
        private final int cachedHash;

        public CachedHashArray(Double[] array) {
            this.internalArray = array;

            int result = 1;

            for (Double element : array) {
                result = 31 * result + (element == null ? 0 :
                        new BigDecimal(element).round(HashedDoubleList.this.context).hashCode());
            }

            this.cachedHash = result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CachedHashArray that = (CachedHashArray) o;
            return Arrays.equals(internalArray, that.internalArray);
        }

        @Override
        public int hashCode() {
            return this.cachedHash;
        }
    }
}
