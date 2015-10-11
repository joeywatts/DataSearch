package com.hacknc;

import com.hacknc.census.CensusVariable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joeywatts on 10/10/15.
 */
public class County {
    private String name;
    private String state;
    private Map<CensusVariable, String> data;

    public County(String name, String state) {
        this.name = name;
        this.state = state;
    }

    public String getState() {
        return state;
    }
    public String getName() {
        return name;
    }

    public County addData(CensusVariable var, String value) {
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
