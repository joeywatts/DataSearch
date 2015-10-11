package com.hacknc.geocode;

/**
 * Created by joeywatts on 10/10/15.
 */
public interface GeocodeResultsListener {
    void onSuccess(GeocodeResult result);
    void onError(Throwable t);
}
