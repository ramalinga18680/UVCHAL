package com.droidlogic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsPref {
    private static final String FIRST_RUN           = "first_run";
    private static final String WIFI_SAVE_STATE     = "wifi_save_state";

    public static void setFirstRun(Context c, boolean firstRun) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(FIRST_RUN, firstRun);
        editor.commit();
    }

    public static boolean getFirstRun(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getBoolean(FIRST_RUN, true);
    }

    public static int getWiFiSaveState(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(WIFI_SAVE_STATE, 0);
    }

    public static void setWiFiSaveState(Context c, int state) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(WIFI_SAVE_STATE, state);
        editor.commit();
    }
}

