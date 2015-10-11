package com.hacknc.geocode;

/**
 * Created by joeywatts on 10/10/15.
 */
public class ReverseGeocodeResult {
    private String state, county, tract, blockGroup;
    public ReverseGeocodeResult(String state, String county, String tract, String blockGroup) {
        this.state = state;
        this.county = county;
        this.tract = tract;
        this.blockGroup = blockGroup;
    }
    public String getState() {
        return state;
    }
    public String getCounty() {
        return county;
    }
    public String getTract() {
        return tract;
    }
    public String getBlockGroup() {
        return blockGroup;
    }
}
