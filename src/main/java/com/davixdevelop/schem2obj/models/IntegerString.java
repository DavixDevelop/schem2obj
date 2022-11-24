package com.davixdevelop.schem2obj.models;

public class IntegerString {
    private String stringValue;
    private Integer integerValue;

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
