package mediaclub.app.appwanderlust;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import mediaclub.app.appwanderlust.Controller.GPSTracker;

public class WelcomeActivity extends AppCompatActivity {

    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_NATIONALITY = "nationality";
    public static final String KEY_REGISTER = "registered";
    public static final String KEY_LOGGED = "logged";
    public static final String KEY_USER_PP_URL = "pp_url";
    boolean registered, logged;

    //GPSTracker gps = new GPSTracker(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        registered = Boolean.valueOf(readFromPreferences(this, KEY_REGISTER, "false"));
        logged = Boolean.valueOf(readFromPreferences(this, KEY_LOGGED, "false"));

        LocationManager locationManager = (LocationManager) this
                .getSystemService(LOCATION_SERVICE);

        // getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            showSettingsAlert();
        }

        Runnable mRunnable;
        Handler mHandler = new Handler();

//        if (!gps.canGetLocation()) {
//            gps.showSettingsAlert();
//        }

        mRunnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                if (logged) {
                    Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        };

        mHandler.postDelayed(mRunnable, 2 * 1000);
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WelcomeActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
