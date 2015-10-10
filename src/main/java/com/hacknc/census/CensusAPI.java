package com.hacknc.census;

import com.hacknc.County;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Created by joeywatts on 10/10/15.
 */
public class CensusAPI {

    private String apiKey;
    private AsyncHttpClient client;

    public CensusAPI(String apiKey) {
        this.apiKey = apiKey;
        client = new AsyncHttpClient();
    }

    public void request(final CensusRequest request, final CensusResponseListener listener) {
        String countyName = request.getCounty().getName();
        String stateName = request.getCounty().getState();
        String variablesString = createVariablesString(request.getVariables());
        String url = "http://api.census.gov/data/2013/acs5?key=" + apiKey + "&get=NAME," + variablesString + "&for=county:" + countyName + "+state:" + stateName;
        client.get(url, new RequestParams(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = new String(responseBody, DEFAULT_CHARSET);
                    JSONArray array = new JSONArray(response);
                    JSONObject responseData = array.getJSONObject(1);
                    Map<CensusVariable, String> data = new HashMap<>();
                    for (CensusVariable variable : request.getVariables()) {
                        if (responseData.has(variable.get())) {
                            data.put(variable, responseData.get(variable.get()).toString());
                        }
                    }
                    listener.onResponse(data);
                } catch (Throwable t) {
                    listener.onError(t);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                listener.onError(error);
            }
        });
    }

    private String createVariablesString(Set<CensusVariable> variables) {
        variables = normalizeIfNecessary(variables);
        StringBuilder builder = new StringBuilder();
        Iterator<CensusVariable> iter = variables.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next().get());
            if (iter.hasNext()) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private Set<CensusVariable> normalizeIfNecessary(Set<CensusVariable> variables) {
        if (variables.contains(CensusVariable.POPULATION))
            return variables;
        for (CensusVariable variable : variables) {
            if (variable.isNormalizable()) {
                variables.add(CensusVariable.POPULATION);
                break;
            }
        }
        return variables;
    }

}
