package com.skronawi.askthedice;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public class MobileMainActivity extends PreferenceActivity {

    private static final String TAG = "askTheDice";

    private static final String COLOR_BACKGROUND_KEY = "colorBackgroundHexRGB";
    private static final String COLOR_DIE_KEY = "colorDieHexRGB";
    private static final String COLOR_TEXT_KEY = "colorTextHexRGB";
    private static final String VIBRATE_ACTIVE_KEY = "vibrateActive";

    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new
            SharedPreferences.OnSharedPreferenceChangeListener() {

                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {

                    if (mGoogleApiClient == null) {
                        return;
                    }

                    updateWear();
                }
            };

    private void updateWear() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final PutDataMapRequest putRequest = PutDataMapRequest.create("/" + getPackageName());
        final DataMap map = putRequest.getDataMap();

        map.putBoolean(VIBRATE_ACTIVE_KEY, sharedPreferences.getBoolean("vibrate_active", true));

        int colorInt = sharedPreferences.getInt("color_background", -1);
        if (colorInt == -1) {
            colorInt = Color.parseColor("#3fadff");
        }
        String backgroundColor = "#" + Integer.toHexString(colorInt).substring(2);
        map.putString(COLOR_BACKGROUND_KEY, backgroundColor);

        colorInt = sharedPreferences.getInt("color_die", -1);
        if (colorInt == -1) {
            colorInt = Color.parseColor("#c5c6c8");
        }
        String dieColor = "#" + Integer.toHexString(colorInt).substring(2);
        map.putString(COLOR_DIE_KEY, dieColor);

        colorInt = sharedPreferences.getInt("color_text", -1);
        if (colorInt == -1) {
            colorInt = Color.parseColor("#000000");
        }
        String textColor = "#" + Integer.toHexString(colorInt).substring(2);
        map.putString(COLOR_TEXT_KEY, textColor);

        //TODO    die sides values with DIE_SIDES_KEY

        Log.d(TAG, "sending new colors: background=" + backgroundColor + ", die=" + dieColor + ", text=" + textColor);

        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "connected");
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "connection suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "connection failed");
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}
