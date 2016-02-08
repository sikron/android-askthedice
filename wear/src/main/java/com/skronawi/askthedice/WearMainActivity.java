package com.skronawi.askthedice;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Random;

public class WearMainActivity extends Activity {

    private static final String TAG = "askTheDice";

    private static final String COLOR_BACKGROUND_KEY = "colorBackgroundHexRGB";
    private static final String COLOR_DIE_KEY = "colorDieHexRGB";
    private static final String COLOR_TEXT_KEY = "colorTextHexRGB";
    private static final String VIBRATE_ACTIVE_KEY = "vibrateActive";

    private Random random = new Random();

    private TextView mRollResult;
    private RelativeLayout layout;
    private ImageButton dieButton;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {

                        Log.d(TAG, "onConnected()");

                        Wearable.DataApi.addListener(mGoogleApiClient, new DataApi.DataListener() {
                            @Override
                            public void onDataChanged(DataEventBuffer dataEvents) {

                                Log.d(TAG, "onDataChanged " + dataEvents);

                                final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
                                for (DataEvent event : events) {

                                    final Uri uri = event.getDataItem().getUri();
                                    final String path = uri != null ? uri.getPath() : null;

                                    if (("/" + getPackageName()).equals(path)) {
                                        update(event.getDataItem());
                                    }
                                }
                            }
                        });
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

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                final SharedPreferences defaultSharedPreferences = PreferenceManager.
                        getDefaultSharedPreferences(WearMainActivity.this);

                layout = (RelativeLayout) findViewById(R.id.layout);
                layout.setBackgroundColor(Color.parseColor(defaultSharedPreferences.getString(
                        COLOR_BACKGROUND_KEY, "#3fadff")));

                Drawable drawable = getResources().getDrawable(R.drawable.die_white_shadows);
                drawable.setColorFilter(Color.parseColor(defaultSharedPreferences.getString(
                        COLOR_DIE_KEY, "#c5c6c8")), PorterDuff.Mode.MULTIPLY);
                dieButton = (ImageButton) findViewById(R.id.dieButton);
                dieButton.setBackground(drawable);

                mRollResult = (TextView) stub.findViewById(R.id.rollResult);
                mRollResult.setTextColor(Color.parseColor(defaultSharedPreferences.getString(
                        COLOR_TEXT_KEY, "#000000")));
                mRollResult.setText("");

                ImageButton mRollButton = (ImageButton) stub.findViewById(R.id.dieButton);
                mRollButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        int i = random.nextInt(DefaultDiceSideProvider.numberOfSides());
                        mRollResult.setText(getResources().getString(
                                DefaultDiceSideProvider.getSides().get(i)));

                        if (defaultSharedPreferences.getBoolean(VIBRATE_ACTIVE_KEY, true)) {
                            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            long[] vibrationPattern = {0, 500};
                            vibrator.vibrate(vibrationPattern, -1);//-1 - don't repeat
                        }

                        mRollResult.invalidate();
                        dieButton.invalidate();
                        layout.invalidate();
                    }
                });
            }
        });
    }

    private void update(DataItem item) {

        Log.d(TAG, "update()");
        final DataMap map = DataMapItem.fromDataItem(item).getDataMap();

        // read your values from map:
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                this).edit();

        boolean shouldVibrate = map.getBoolean(VIBRATE_ACTIVE_KEY, true);
        editor.putBoolean(VIBRATE_ACTIVE_KEY, shouldVibrate);

        final String colorBackground = map.getString(COLOR_BACKGROUND_KEY, "#3fadff");
        editor.putString(COLOR_BACKGROUND_KEY, colorBackground);

        final String colorDie = map.getString(COLOR_DIE_KEY, "#c5c6c8");
        editor.putString(COLOR_DIE_KEY, colorDie);

        final String colorText = map.getString(COLOR_TEXT_KEY, "#000000");
        editor.putString(COLOR_TEXT_KEY, colorText);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "updating colors: background=" + colorBackground + ", die=" + colorDie + ", text=" + colorText);
                layout.setBackgroundColor(Color.parseColor(colorBackground));

                Drawable drawable = getResources().getDrawable(R.drawable.die_white_shadows);
                drawable.setColorFilter(Color.parseColor(colorDie), PorterDuff.Mode.MULTIPLY);
                dieButton.setBackground(drawable);

                mRollResult.setTextColor(Color.parseColor(colorText));
            }
        });

        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}
