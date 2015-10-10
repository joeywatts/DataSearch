package com.hacknc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.hacknc.census.CensusAPI;
import com.hacknc.census.CensusRequest;
import com.hacknc.census.CensusResponseListener;
import com.hacknc.census.CensusVariable;

import java.util.Map;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
    }

    @Override
    public void onStart() {
        super.onStart();
        TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setText("Hello world!");
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

}
