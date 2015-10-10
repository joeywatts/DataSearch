package com.hacknc;

/**
 * Created by joeywatts on 10/10/15.
 */
public class County {
    private String name;
    private String state;

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
}
