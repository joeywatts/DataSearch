package com.hacknc.census;

import com.hacknc.County;

import java.util.Map;

/**
 * Created by joeywatts on 10/10/15.
 */
public interface CensusResponseListener {
    void onResponse(County[] response);
    void onError(Throwable t);
}
