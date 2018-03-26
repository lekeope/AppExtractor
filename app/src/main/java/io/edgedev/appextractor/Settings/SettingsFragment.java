package io.edgedev.appextractor.Settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import io.edgedev.appextractor.R;

/**
 * Created by OPEYEMI OLORUNLEKE on 9/5/2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
    }
}
