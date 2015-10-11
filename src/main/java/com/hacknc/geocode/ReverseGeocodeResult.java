package com.hacknc.geocode;

/**
 * Created by joeywatts on 10/10/15.
 */
public class ReverseGeocodeResult {
    private String state, county, tract, blockGroup;
    private String stateName, countyName, tractName, blockGroupName;
    public ReverseGeocodeResult(String state, String stateName, String county, String countyName, String tract, String tractName, String blockGroup, String blockGroupName) {
        this.state = state;
        this.county = county;
        this.tract = tract;
        this.blockGroup = blockGroup;
        this.stateName = stateName;
        this.countyName = countyName;
        this.tractName = tractName;
        this.blockGroupName = blockGroupName;
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
    public String getStateName() {
        return stateName;
    }
    public String getCountyName() {
        return countyName;
    }
    public String getTractName() {
        return tractName;
    }
    public String getBlockGroupName() {
        return blockGroupName;
    }
}
