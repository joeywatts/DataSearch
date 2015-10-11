package com.hacknc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.esri.android.map.*;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorReverseGeocodeResult;
import com.hacknc.census.*;
import com.hacknc.database.CrimeData;
import com.hacknc.geocode.GeocodeAPI;
import com.hacknc.geocode.GeocodeGeometryResultsListener;
import com.hacknc.geocode.ReverseGeocodeResult;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements OnSingleTapListener, SharedPreferences.OnSharedPreferenceChangeListener {

    MapView map;
    GraphicsLayer locationLayer;
    Point locationLayerPoint;
    String locationLayerPointString;
    Menu mMenu;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        /////////////// LOAD DB ///////////////
        CrimeData.loadDB(this);

        /////////////// NAV DRAWER ///////////////

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.pg_logo,  /* nav drawer icon to replace 'Up' caret */
                R.string.open_drawer,  /* "open drawer" description */
                R.string.close_drawer  /* "close drawer" description */
        ) {

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
                    String option = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("pref_mapOption", "STREETS");
                    final MapOptions top = new MapOptions(getMapType(option));
                    map.setMapOptions(top);
                }
            }
        });
        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
        }
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onSearchRequested() {
            MenuItem mi = mMenu.findItem(R.id.search);
            if(mi.isActionViewExpanded()){
                mi.collapseActionView();
            } else{
                mi.expandActionView();
            }

        return super.onSearchRequested();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        if (key.equals("pref_mapOption")) {
            String option = prefs.getString("pref_mapOption", "STREETS");

            final MapOptions top = new MapOptions(getMapType(option));
            map.setMapOptions(top);
            Log.d("NC", option);
            Log.d("NC", "---------- it happened ---------------");
        }

    }

    private MapOptions.MapType getMapType(String type) {
        if (type.equals("STREETS"))
            return MapOptions.MapType.STREETS;
        else if (type.equals("TOPO"))
            return MapOptions.MapType.TOPO;
        else if (type.equals("OCEAN"))
            return MapOptions.MapType.OCEANS;
        else if (type.equals("GREY"))
            return MapOptions.MapType.GRAY;
        else if (type.equals("SATELLITE"))
            return MapOptions.MapType.SATELLITE;
        else if (type.equals("NATIONAL_GEOGRAPHIC"))
            return MapOptions.MapType.NATIONAL_GEOGRAPHIC;
        else if (type.equals("OSM"))
            return MapOptions.MapType.OSM;
        else if (type.equals("HYBRID"))
            return MapOptions.MapType.HYBRID;
        return MapOptions.MapType.STREETS;
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
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        performGeocodeRequest();
    }

    private void performCensusRequest(ReverseGeocodeResult result) {
        CensusAPI api = new CensusAPI("***REMOVED***");
        CensusRequest request = new CensusRequest()
                .setState(result.getState())
                .setCounty("*")
                .setTract("*");
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

    private void performGeocodeRequest() {
        /**
         GeocodeAPI.reverseGeocode(38.6159530f, -76.6130150f, new ReverseGeocodeResultsListener() {
        @Override public void onSuccess(ReverseGeocodeResult result) {
        performCensusRequest(result);
        }

        @Override public void onError(Throwable t) {
        Log.d("Geocode", "Error", t);
        }
        });*/
        GeocodeAPI.geocodeGeometryQuery("tract", 38.6159530f, -76.6130150f, map, new SimpleFillSymbol(Color.BLUE), new GeocodeGeometryResultsListener() {
            @Override
            public void onSuccess(Layer layer) {
                map.addLayer(layer);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        map.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        map.unpause();
        final MapOptions top = new MapOptions(getMapType(prefs.getString("pref_mapOption", "STREETS")));
        map.setMapOptions(top);
    }

    @Override
    public void onSingleTap(final float x, final float y) {

        Runnable myRunnable = new Runnable() {
            public void run() {
                // Create the Locator if it hasn't been created yet
                Locator mLocator = Locator.createOnlineLocator();
                // Add the touch point to the MapView
                Point mapPoint = map.toMapPoint(x, y);
                Graphic g = new Graphic(mapPoint, new SimpleMarkerSymbol(Color.BLUE, 10, SimpleMarkerSymbol.STYLE.DIAMOND));
                locationLayer.addGraphic(g);

                try

                {
                    // Attempt to reverse geocode the touch event
                    LocatorReverseGeocodeResult result = mLocator.reverseGeocode(mapPoint, 100,
                            map.getSpatialReference(), map.getSpatialReference());

                    // Reverse geocoding results come back as [key,value] pairs, but you
                    // can create a formatted address by iterating through and delimiting with spaces
                    Map<String, String> addressFields = result.getAddressFields();
                    StringBuilder address = new StringBuilder();
                    for (Map.Entry<String, String> entry : addressFields.entrySet())
                        address.append(entry.getValue() != null ? "," + entry.getValue() + ")" : "");

                    // Show the address in the Map's callout
                    Log.d("NC", address.toString());
                } catch (Exception e) {
                    Log.e("Reverse Geocoding Error", e.getMessage(), e);
                }
            }
        };
        new Thread(myRunnable).start();

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
