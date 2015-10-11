package com.hacknc.geocode;

import android.util.Log;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.Symbol;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.codehaus.jackson.JsonFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by joeywatts on 10/10/15.
 */
public class GeocodeAPI {
    private static AsyncHttpClient client = new AsyncHttpClient();
    public static void geocodeGeometryQuery(String type, String geometry, String geometryType, String spatialRel, final GeocodeGeometryResultsListener listener) {

        int mapserver = 84;
        if (type.equals("county")) {
            mapserver = 86;
        } else if (type.equals("tract")) {
            mapserver = 8;
        } else if (type.equals("blockGroup")) {
            mapserver = 10;
        }
        String url = "http://tigerweb.geo.census.gov/arcgis/rest/services/TIGERweb/tigerWMS_Current/MapServer/" + mapserver + "/query";
        RequestParams params = new RequestParams();
        String spatialReferenceCode = "4326";
        params.add("geometry", geometry);
        params.add("geometryType", geometryType);
        params.add("spatialRel", spatialRel);
        params.add("f", "json");
        params.add("where", "");
        params.add("outFields", "*");
        params.add("outSR", spatialReferenceCode);
        params.add("inSR", spatialReferenceCode);
        client.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                    /*Graphic[] graphics = set.getGraphics();
                    SpatialReference in = SpatialReference.create(4326);
                    SpatialReference out = mapView.getSpatialReference();
                    GraphicsLayer layer = new GraphicsLayer();
                    for (int i = 0; i < graphics.length; i++) {
                        Geometry geom = GeometryEngine.project(graphics[i].getGeometry(), in, out);
                        layer.addGraphic(new Graphic(geom, symbol));
                    }*/
                Log.d("Debug", response);
                    listener.onSuccess(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onError(throwable);
            }
        });
    }

    public static void geocodeGeometryQuery(String type, String polygon, final GeocodeGeometryResultsListener listener) {
        geocodeGeometryQuery(type, polygon, "esriGeometryPolygon", "esriSpatialRelContains", listener);
    }
    public static void geocodeGeometryQuery(String type, float lat, float lon, final GeocodeGeometryResultsListener listener) {
        geocodeGeometryQuery(type, lon + "," + lat, "esriGeometryPoint", "esriSpatialRelIntersects", listener);
    }

    public static void reverseGeocode(final float lat, final float lon, final ReverseGeocodeResultsListener listener) {
        doReverseGeocode(0, lat, lon, listener);
    }
    private static void doReverseGeocode(final int tries, final float lat, final float lon, final ReverseGeocodeResultsListener listener) {
        String url = "http://geocoding.geo.census.gov/geocoder/geographies/coordinates?x=" + lon + "&y=" + lat + "&benchmark=Public_AR_Current&vintage=Current_Current&format=json";
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Census", response.toString());
                try {
                    JSONObject geographies = response.getJSONObject("result").getJSONObject("geographies");
                    JSONObject fipsData = geographies.getJSONArray("2010 Census Blocks").getJSONObject(0);
                    listener.onSuccess(new ReverseGeocodeResult(
                            fipsData.getString("STATE"),
                            geographies.getJSONArray("States").getJSONObject(0).getString("NAME"),
                            fipsData.getString("COUNTY"),
                            geographies.getJSONArray("Counties").getJSONObject(0).getString("NAME"),
                            fipsData.getString("TRACT"),
                            geographies.getJSONArray("Census Tracts").getJSONObject(0).getString("NAME"),
                            fipsData.getString("BLKGRP"),
                            fipsData.getString("NAME")
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
