package com.hacknc;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.hacknc.census.CensusAPI;
import com.hacknc.census.CensusRequest;
import com.hacknc.census.CensusResponseListener;
import com.hacknc.census.CensusVariable;

import java.util.Map;
import android.widget.Toast;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;

import java.util.List;

public class MainActivity extends Activity {

    MapView map;
    GraphicsLayer locationLayer;
    Point locationLayerPoint;
    String locationLayerPointString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // Get the Map View
        map = (MapView) findViewById(R.id.map);
        // Enable map to wrap around date line.
        map.enableWrapAround(true);

        // For location
        locationLayer = new GraphicsLayer();
        map.addLayer(locationLayer);

        // This will be called when the view is initialized.
        map.setOnStatusChangedListener(new OnStatusChangedListener() {
            public void onStatusChanged(Object source, STATUS status) {
                if ((source == map) && (status == STATUS.INITIALIZED)) {
                    executeLocatorTask("***REMOVED***");
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        performRequest();
    }

    private void performRequest() {
        CensusAPI api = new CensusAPI("***REMOVED***");
        County county = new County("*", "06");
        CensusRequest request = new CensusRequest(county);
        request.add(CensusVariable.POPULATION_BLACK_ALONE);
        Log.d("Hello", "World");
        api.request(request, new CensusResponseListener() {
            @Override
            public void onResponse(Map<CensusVariable, String> response) {
                for (Map.Entry<CensusVariable, String> entry : response.entrySet()) {
                    Log.d("HackNC", entry.getKey() + " - " + entry.getValue());
                }
                Log.d("HackNC", "done");
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
                SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(Color.RED, 16, SimpleMarkerSymbol.STYLE.CIRCLE);
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
