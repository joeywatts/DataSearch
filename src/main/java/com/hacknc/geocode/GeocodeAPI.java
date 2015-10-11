package com.hacknc.geocode;

import android.graphics.Color;
import android.util.Log;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.*;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.Symbol;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.codehaus.jackson.JsonFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;

/**
 * Created by joeywatts on 10/10/15.
 */
public class GeocodeAPI {
    private static AsyncHttpClient client = new AsyncHttpClient();
    public static void geocodeGeometryQuery(String type, float lat, float lon, final MapView mapView, final Symbol symbol, final GeocodeGeometryResultsListener listener) {

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
        params.add("geometry", lon + "," + lat);
        params.add("geometryType", "esriGeometryPoint");
        params.add("spatialRel", "esriSpatialRelIntersects");
        params.add("f", "json");
        params.add("where", "");
        params.add("outFields", "*");
        params.add("outSR", spatialReferenceCode);
        params.add("inSR", spatialReferenceCode);
        client.post(url, params, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                try {
                    JsonFactory factory = new JsonFactory();
                    FeatureSet set = FeatureSet.fromJson(factory.createJsonParser(response));
                    Graphic[] graphics = set.getGraphics();
                    SpatialReference in = SpatialReference.create(4326);
                    SpatialReference out = mapView.getSpatialReference();
                    GraphicsLayer layer = new GraphicsLayer();
                    for (int i = 0; i < graphics.length; i++) {
                        Geometry geom = GeometryEngine.project(graphics[i].getGeometry(), in, out);
                        layer.addGraphic(new Graphic(geom, symbol));
                    }
                    listener.onSuccess(layer);
                } catch (Exception e) {
                    listener.onError(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onError(throwable);
            }
        });
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
                    JSONObject fipsData = response.getJSONObject("result").getJSONObject("geographies").getJSONArray("2010 Census Blocks").getJSONObject(0);
                    listener.onSuccess(new ReverseGeocodeResult(
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
