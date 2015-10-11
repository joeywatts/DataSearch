package com.hacknc.census;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joeywatts on 10/10/15.
 */
public class CensusResultRow {
    private String state, county, tract, blockGroup;
    private Map<CensusVariable, String> data;

    public CensusResultRow() {
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getTract() {
        return tract;
    }

    public void setTract(String tract) {
        this.tract = tract;
    }

    public String getBlockGroup() {
        return blockGroup;
    }

    public void setBlockGroup(String blockGroup) {
        this.blockGroup = blockGroup;
    }

    public CensusResultRow addData(CensusVariable var, String value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(var, value);
        return this;
    }

    public Map<CensusVariable, String> getData() {
        return data;
    }
}
