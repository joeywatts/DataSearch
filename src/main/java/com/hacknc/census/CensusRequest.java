package com.hacknc.census;

import java.util.LinkedHashSet;

/**
 * Created by joeywatts on 10/10/15.
 */
public class CensusRequest {
    private float lat, lon;
    private LinkedHashSet<CensusVariable> variables;
    private String state, county, tract, blockGroup;

    public CensusRequest() {
        variables = new LinkedHashSet<>();
    }

    public CensusRequest setState(String state) {
        this.state = state;
        return this;
    }

    public CensusRequest setCounty(String county) {
        this.county = county;
        return this;
    }

    public CensusRequest setTract(String tract) {
        this.tract = tract;
        return this;
    }

    public CensusRequest setBlockGroup(String blockGroup) {
        this.blockGroup = blockGroup;
        return this;
    }

    public CensusRequest add(CensusVariable variable) {
        variables.add(variable);
        return this;
    }

    public LinkedHashSet<CensusVariable> getVariables() {
        return variables;
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
