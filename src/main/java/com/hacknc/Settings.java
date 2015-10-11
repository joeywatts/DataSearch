package com.hacknc;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Gunnar on 10/11/2015.
 */
public class Settings extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
