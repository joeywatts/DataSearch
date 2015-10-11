package com.hacknc.geocode;

import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by joeywatts on 10/10/15.
 */
public class GeocodeAPI {
    private static AsyncHttpClient client = new AsyncHttpClient();
    public static void reverseGeocode(final float lat, final float lon, final GeocodeResultsListener listener) {
        doReverseGeocode(0, lat, lon, listener);
    }
    private static void doReverseGeocode(final int tries, final float lat, final float lon, final GeocodeResultsListener listener) {
        String url = "http://geocoding.geo.census.gov/geocoder/geographies/coordinates?x=" + lon + "&y=" + lat + "&benchmark=Public_AR_Current&vintage=Current_Current&format=json";
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Census", response.toString());
                try {
                    JSONObject fipsData = response.getJSONObject("result").getJSONObject("geographies").getJSONArray("2010 Census Blocks").getJSONObject(0);
                    listener.onSuccess(new GeocodeResult(
                            fipsData.getString("STATE"),
                            fipsData.getString("COUNTY"),
                            fipsData.getString("TRACT"),
                            fipsData.getString("BLKGRP")
                    ));
                } catch (JSONException e) {
                    if (tries > 2) {
                        listener.onError(e);
                    } else {
                        doReverseGeocode(tries + 1, lat, lon, listener);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onError(throwable);
            }
        });
    }
}
