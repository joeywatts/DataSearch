package com.hacknc.geocode;

import com.esri.core.map.FeatureSet;

/**
 * Created by joeywatts on 10/10/15.
 */
public interface GeocodeGeometryResultsListener {
    void onSuccess(String geometry);
    void onError(Throwable t);
}
