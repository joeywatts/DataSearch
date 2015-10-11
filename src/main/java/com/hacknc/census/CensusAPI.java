package com.hacknc.census;

import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

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

    private String getStateID(String state) {
        return stateIdMap.get(state);
    }

    public void request(final CensusRequest request, final CensusResponseListener listener) {
        String county = request.getCounty();
        String state = request.getState();
        String tract = request.getTract();
        String blockGroup = request.getBlockGroup();
        String combinator = "&in=";

        String forString = "&for=";
        if (blockGroup != null) {
            forString += "block+group:" + blockGroup + combinator;
            combinator = "+";
        }
        if (tract != null) {
            forString += "tract:" + tract + combinator;
            combinator = "+";
        }
        if (county != null) {
            forString += "county:" + county + combinator;
        }
        if (state != null) {
            forString += "state:" + state;
        }
        String variablesString = createVariablesString(request.getVariables());
        String url = "http://api.census.gov/data/2013/acs5?key=" + apiKey + "&get=NAME," + variablesString + forString;
        client.get(url, new RequestParams(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = new String(responseBody, DEFAULT_CHARSET);
                    JSONArray array = new JSONArray(response);
                    Map<CensusVariable, String> data = new HashMap<>();
                    CensusResultRow result[] = new CensusResultRow[array.length()-1];
                    for (int i = 1; i < array.length(); i++) {
                        String name = array.getJSONArray(i).getString(0);
                        String split[] = name.split(", ");
                        result[i - 1] = new CensusResultRow();
                        if (split.length >= 2) {
                            result[i - 1].setState(split[split.length - 1]);
                            result[i - 1].setCounty(split[split.length - 2]);
                        } if (split.length >= 3) {
                            result[i - 1].setTract(split[split.length - 3]);
                        } if (split.length >= 4) {
                            result[i - 1].setBlockGroup(split[split.length - 4]);
                        }
                    }
                    int column = 1;
                    for (CensusVariable variable : request.getVariables()) {
                        for (int i = 1; i < array.length(); i++) {
                            JSONArray row = array.getJSONArray(i);
                            String value = row.getString(column);
                            result[i - 1].addData(variable, value);
                        }
                        column++;
                    }
                    listener.onResponse(result);
                } catch (Throwable t) {
                    listener.onError(t);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    Log.d("Census", new String(responseBody, DEFAULT_CHARSET));
                } catch (Exception e) {}
                listener.onError(error);
            }
        });
    }

    private String createVariablesString(LinkedHashSet<CensusVariable> variables) {
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

    private LinkedHashSet<CensusVariable> normalizeIfNecessary(LinkedHashSet<CensusVariable> variables) {
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

    /**
     * A mapping from state abbreviation to state id.
     */
    private static Map<String, String> stateIdMap = new HashMap<>();
    static {
        stateIdMap.put("AL", "01");
        stateIdMap.put("CA", "06");
        stateIdMap.put("AK", "02");
        stateIdMap.put("AR", "03");
        stateIdMap.put("CO", "08");
        stateIdMap.put("AZ", "04");
        stateIdMap.put("CT", "09");
        stateIdMap.put("DE", "10");
        stateIdMap.put("DC", "11");
        stateIdMap.put("HI", "15");
        stateIdMap.put("GA", "13");
        stateIdMap.put("FL", "12");
        stateIdMap.put("ID", "16");
        stateIdMap.put("IL", "17");
        stateIdMap.put("IN", "18");
        stateIdMap.put("KS", "20");
        stateIdMap.put("IA", "19");
        stateIdMap.put("KY", "21");
        stateIdMap.put("LA", "22");
        stateIdMap.put("MD", "24");
        stateIdMap.put("MI", "26");
        stateIdMap.put("ME", "23");
        stateIdMap.put("MA", "25");
        stateIdMap.put("MN", "27");
        stateIdMap.put("MS", "28");
        stateIdMap.put("MO", "29");
        stateIdMap.put("MT", "30");
        stateIdMap.put("NE", "31");
        stateIdMap.put("NV", "32");
        stateIdMap.put("NH", "33");
        stateIdMap.put("NJ", "34");
        stateIdMap.put("NC", "37");
        stateIdMap.put("NM", "35");
        stateIdMap.put("NY", "36");
        stateIdMap.put("ND", "38");
        stateIdMap.put("OH", "39");
        stateIdMap.put("PA", "42");
        stateIdMap.put("OK", "40");
        stateIdMap.put("OR", "41");
        stateIdMap.put("RI", "44");
        stateIdMap.put("SC", "45");
        stateIdMap.put("SD", "46");
        stateIdMap.put("TX", "48");
        stateIdMap.put("UT", "49");
        stateIdMap.put("VA", "51");
        stateIdMap.put("TN", "47");
        stateIdMap.put("VT", "50");
        stateIdMap.put("WA", "53");
        stateIdMap.put("WY", "56");
        stateIdMap.put("WV", "54");
        stateIdMap.put("WI", "55");
    }
}
