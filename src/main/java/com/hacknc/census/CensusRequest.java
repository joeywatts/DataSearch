package com.hacknc.census;

import com.hacknc.County;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by joeywatts on 10/10/15.
 */
public class CensusRequest {
    private County county;
    private Set<CensusVariable> variables;

    public CensusRequest(County county) {
        this.county = county;
        variables = new HashSet<>();
    }

    public CensusRequest add(CensusVariable variable) {
        variables.add(variable);
        return this;
    }

    public County getCounty() {
        return county;
    }

    public Set<CensusVariable> getVariables() {
        return variables;
    }

}
