package com.hacknc.census;

/**
 * Created by joeywatts on 10/10/15.
 */
public interface CensusResponseListener {
    void onResponse(CensusResultRow[] response);
    void onError(Throwable t);
}
