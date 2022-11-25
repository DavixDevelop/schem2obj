package com.davixdevelop.schem2obj.models;

public class IntegerString {
    String stringValue;
    Integer integerValue;

    public IntegerString(String stringValue, Integer integerValue){
        this.stringValue = stringValue;
        this.integerValue = integerValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
