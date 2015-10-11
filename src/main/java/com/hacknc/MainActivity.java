package com.hacknc;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.*;
import com.esri.core.map.FeatureSet;
import com.hacknc.census.CensusAPI;
import com.hacknc.census.CensusRequest;
import com.hacknc.census.CensusResponseListener;
import com.hacknc.census.CensusVariable;

import android.widget.Toast;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;

import com.hacknc.database.CrimeData;

import com.hacknc.census.*;
import static com.hacknc.census.CensusVariable.*;
import com.hacknc.geocode.GeocodeAPI;
import com.hacknc.geocode.GeocodeGeometryResultsListener;
import com.hacknc.geocode.ReverseGeocodeResult;
import com.hacknc.geocode.ReverseGeocodeResultsListener;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements OnSingleTapListener {

    static class TractData {
        Graphic graphic;
        CensusResultRow censusData;
        int graphicId;
    }

    Map<String, TractData> tractIdDataMap;
    CensusVariable selectedVariable = POPULATION;
    MapView map;
    GraphicsLayer locationLayer;
    GraphicsLayer polygons;
    ReverseGeocodeResult lastTap;
    Point locationLayerPoint;
    String locationLayerPointString;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CensusAPI censusApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        censusApi = new CensusAPI("***REMOVED***");

        /////////////// LOAD DB ///////////////
        CrimeData.loadDB(this);

        /////////////// NAV DRAWER ///////////////

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.pg_logo,  /* nav drawer icon to replace 'Up' caret */
                R.string.open_drawer,  /* "open drawer" description */
                R.string.close_drawer  /* "close drawer" description */) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle("HackNC");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle("Options");
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        /////////////// MAP ///////////////

        // Get the Map View
        map = (MapView) findViewById(R.id.map);
        // Enable map to wrap around date line.
        map.enableWrapAround(true);

        // For location
        locationLayer = new GraphicsLayer();
        map.addLayer(locationLayer);
        map.setOnSingleTapListener(this);

        // This will be called when the view is initialized.
        map.setOnStatusChangedListener(new OnStatusChangedListener() {
            public void onStatusChanged(Object source, STATUS status) {
                if ((source == map) && (status == STATUS.INITIALIZED)) {
                    //executeLocatorTask("***REMOVED***");
                    LocationDisplayManager ldm = map.getLocationDisplayManager();
                    ldm.setAutoPanMode(LocationDisplayManager.AutoPanMode.NAVIGATION);
                    ldm.start();
                    map.centerAt(ldm.getLocation().getLatitude(), ldm.getLocation().getLongitude(), true);
                }
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void performCensusRequest(ReverseGeocodeResult result) {
        CensusAPI api = new CensusAPI("***REMOVED***");
        CensusRequest request = new CensusRequest().setState(result.getState()).setCounty("*").setTract("*");
        request.add(CensusVariable.POVERTY).add(CensusVariable.POPULATION);
        api.request(request, new CensusResponseListener() {
            @Override
            public void onResponse(CensusResultRow[] result) {
                for (int i = 0; i < result.length; i++) {
                    CensusResultRow c = result[i];
                    String loc = "";
                    if (c.getBlockGroup() != null) {
                        loc += c.getBlockGroup() + ", ";
                    }
                    if (c.getTract() != null) {
                        loc += c.getTract() + ", ";
                    }
                    if (c.getCounty() != null) {
                        loc += c.getCounty() + ", ";
                    }
                    if (c.getState() != null) {
                        loc += c.getState();
                    }
                    String s = "['" + loc + "'";
                    Iterator<Map.Entry<CensusVariable, String>> iter = c.getData().entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<CensusVariable, String> entry = iter.next();
                        s += ", " + entry.getKey() + " -> '" + entry.getValue() + "'";
                    }
                    s += "]";
                    Log.d("Census", s);
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.d("HackNC", "Error", t);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.unpause();
    }

    @Override
    public void onSingleTap(final float x, final float y) {

        // Create the Locator if it hasn't been created yet
        Locator mLocator = Locator.createOnlineLocator();
        // Add the touch point to the MapView
        Point mapPoint = map.toMapPoint(x, y);
        Point longLat = (Point) GeometryEngine.project(mapPoint, map.getSpatialReference(), SpatialReference.create
                (4326));
        final float lat = (float) longLat.getY(), lng = (float) longLat.getX();
        GeocodeAPI.reverseGeocode(lat, lng, new ReverseGeocodeResultsListener() {
            @Override
            public void onSuccess(ReverseGeocodeResult result) {
                lastTap = result;
                showEachCountyOnMap(result, lat, lng);
            }

            @Override
            public void onError(Throwable t) {
                Log.d("HackNC", "Error", t);

            }
        });

    }

    private void showEachCountyOnMap(final ReverseGeocodeResult geocodeResult, float lat, float lng) {
        GeocodeAPI.geocodeGeometryQuery("county", lat, lng, new GeocodeGeometryResultsListener() {
            @Override
            public void onSuccess(String countyJson) {
                try {
                    JSONObject countyObj = new JSONObject(countyJson);
                    String countyGeom = countyObj.getJSONArray("features").getJSONObject(0).get("geometry").toString();
                    GeocodeAPI.geocodeGeometryQuery("tract", countyGeom, new GeocodeGeometryResultsListener() {
                        @Override
                        public void onSuccess(String geometry) {
                            try {
                                JsonFactory factory = new JsonFactory();
                                JsonParser parser = factory.createJsonParser(geometry);
                                FeatureSet featureSet = FeatureSet.fromJson(parser);
                                Graphic[] graphics = featureSet.getGraphics();
                                if (graphics == null) {
                                    return;
                                }
                                if (polygons != null) {
                                    map.removeLayer(polygons);
                                }
                                polygons = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);
                                tractIdDataMap = new HashMap<>();
                                for (int i = 0; i < graphics.length; i++) {
                                    Geometry geom = graphics[i].getGeometry();
                                    geom = GeometryEngine.project(geom, SpatialReference.create(4326), map
                                            .getSpatialReference());

                                    Graphic g = new Graphic(geom, new SimpleFillSymbol(Color.argb(125, 255, 0, 0)),
                                            graphics[i].getAttributes());
                                    TractData tractData = new TractData();
                                    tractData.graphic = g;
                                    tractIdDataMap.put(graphics[i].getAttributeValue("TRACT").toString(), tractData);
                                    int graphicId = polygons.addGraphic(g);
                                    tractData.graphicId = graphicId;
                                }
                                map.addLayer(polygons);
                                loadDataForTracts(geocodeResult, tractIdDataMap);
                            } catch (Exception e) {
                                Log.d("HackNC", "Error", e);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            Log.d("HackNC", "Error", t);
                        }
                    });
                } catch (Exception e) {
                    Log.d("HackNC", "Error", e);
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.d("HackNC", "Error", t);
            }
        });
    }

    private void loadDataForTracts (ReverseGeocodeResult geocodeResult, final Map<String, TractData> map) {
        CensusRequest request = new CensusRequest();
        request.setState(geocodeResult.getState()).setCounty(geocodeResult.getCounty()).setTract("*");
        request.add(POPULATION);
        censusApi.request(request, new CensusResponseListener() {
            @Override
            public void onResponse(CensusResultRow[] response) {
                for (CensusResultRow row : response) {
                    String tractsplit[] = row.getTract().split(" ");
                    String tract = "";
                    for (char c : tractsplit[tractsplit.length - 1].toCharArray()) {
                        if (Character.isDigit(c)) {
                            tract += c;
                        }
                    }
                    if (tract.length() == 3) {
                        tract += "00";
                    }
                    while (tract.length() < 6) {
                        tract = "0" + tract;
                    }
                    TractData data = map.get(tract);
                    if (data == null) {
                        continue;
                    }
                    data.censusData = row;
                }
                setTractColorsWithData(selectedVariable);
                // Do something with the census data.
            }

            @Override
            public void onError(Throwable t) {
                Log.d("HackNC", "Error", t);
            }
        });
    }

    private void setTractColorsWithData(CensusVariable var) {
        if (tractIdDataMap == null) {
            return;
        }
        double max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        for (TractData data : tractIdDataMap.values()) {
            if (data.censusData == null) continue;
            double d = Double.parseDouble(data.censusData.getData().get(var));
            max = Math.max(max, d);
            min = Math.min(min, d);
        }
        for (TractData data : tractIdDataMap.values()) {
            if (data.censusData == null) continue;
            double score = Double.parseDouble(data.censusData.getData().get(var));
            score = (score - min) / (max - min);
            Log.d("Color", "" + score * 360.0f * 0.4f);
            float[] hsv = {360.0f * (float) score * 0.4f, 0.9f, 0.9f};
            int color = Color.HSVToColor(125, hsv);
            polygons.updateGraphic(data.graphicId, new SimpleFillSymbol(color));
        }
    }

    private void executeLocatorTask(String address) {
        // Create Locator parameters from single line address string
        LocatorFindParameters findParams = new LocatorFindParameters(address);

        // Use the centre of the current map extent as the find location point
        findParams.setLocation(map.getCenter(), map.getSpatialReference());

        // Calculate distance for find operation
        Envelope mapExtent = new Envelope();
        map.getExtent().queryEnvelope(mapExtent);
        // assume map is in metres, other units wont work, double current envelope
        double distance = (mapExtent != null && mapExtent.getWidth() > 0) ? mapExtent.getWidth() * 2 : 10000;
        findParams.setDistance(distance);
        findParams.setMaxLocations(2);

        // Set address spatial reference to match map
        findParams.setOutSR(map.getSpatialReference());

        // Execute async task to find the address
        new LocatorAsyncTask().execute(findParams);
        locationLayerPointString = address;
    }

    private class LocatorAsyncTask extends AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {
        private Exception mException;

        @Override
        protected List<LocatorGeocodeResult> doInBackground(LocatorFindParameters... params) {
            mException = null;
            List<LocatorGeocodeResult> results = null;
            Locator locator = Locator.createOnlineLocator();
            try {
                results = locator.find(params[0]);
            } catch (Exception e) {
                mException = e;
            }
            return results;
        }

        protected void onPostExecute(List<LocatorGeocodeResult> result) {
            if (mException != null) {
                Log.w("PlaceSearch", "LocatorSyncTask failed with:");
                mException.printStackTrace();
                Toast.makeText(MainActivity.this, "Address search failed", Toast.LENGTH_LONG).show();
                return;
            }

            if (result.size() == 0) {
                Toast.makeText(MainActivity.this, "No results found", Toast.LENGTH_LONG).show();
            } else {
                // Use first result in the list
                LocatorGeocodeResult geocodeResult = result.get(0);

                // get return geometry from geocode result
                Point resultPoint = geocodeResult.getLocation();
                // create marker symbol to represent location
                SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.RED, 16, SimpleMarkerSymbol.STYLE
                        .CIRCLE);
                // create graphic object for resulting location
                Graphic resultLocGraphic = new Graphic(resultPoint, resultSymbol);
                // add graphic to location layer
                locationLayer.addGraphic(resultLocGraphic);

                // create text symbol for return address
                String address = geocodeResult.getAddress();
                TextSymbol resultAddress = new TextSymbol(20, address, Color.BLACK);
                // create offset for text
                resultAddress.setOffsetX(-4 * address.length());
                resultAddress.setOffsetY(10);
                // create a graphic object for address text
                Graphic resultText = new Graphic(resultPoint, resultAddress);
                // add address text graphic to location graphics layer
                locationLayer.addGraphic(resultText);

                locationLayerPoint = resultPoint;

                // Zoom map to geocode result location
                map.zoomToResolution(geocodeResult.getLocation(), 2);
            }
        }
    }

}
