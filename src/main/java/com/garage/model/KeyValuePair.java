package com.garage.model;

public class KeyValuePair {
    private String key;
    private String value;

    public KeyValuePair() {
    }

    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "KeyValuePair {" + key + ": " + value + "}";
    }

}
