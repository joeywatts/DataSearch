package com.hacknc.geocode;

import com.esri.android.map.Layer;

/**
 * Created by joeywatts on 10/10/15.
 */
public interface GeocodeGeometryResultsListener {
    void onSuccess(Layer fs);
    void onError(Throwable t);
}
