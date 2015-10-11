package com.hacknc.geocode;

/**
 * Created by joeywatts on 10/10/15.
 */
public interface ReverseGeocodeResultsListener {
    void onSuccess(ReverseGeocodeResult result);
    void onError(Throwable t);
}
