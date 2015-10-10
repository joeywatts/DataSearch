package com.hacknc.census;

import java.util.Map;

/**
 * Created by joeywatts on 10/10/15.
 */
public interface CensusResponseListener {
    void onResponse(Map<CensusVariable, String> response);
    void onError(Throwable t);
}
