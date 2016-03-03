package mediaclub.app.appwanderlust;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.GcmIntentService;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_NOTIFICATIONS = "notifications";
    public static final String KEY_LOGGED = "logged";
    public static final String KEY_FLAG_BACK_NOT = "start_chat";

    private Toolbar toolbar;
    private NavigationView navDrawer;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int selectedItem;

    TextView title, logout;
    String type;
    TextView name, email;
    CircularImageView image;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        Typeface helveticaBold = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT Bold.ttf");
        Typeface helveticaOblique = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT Oblique.ttf");
        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT.ttf");
        Typeface myFont1 = Typeface.createFromAsset(getAssets(), "fonts/KaushanScript-Regular.otf");
        title.setTypeface(helveticaBold);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navDrawer = (NavigationView) findViewById(R.id.menu_drawer);
        View headerview = navDrawer.getHeaderView(0);
        logout = (TextView) headerview.findViewById(R.id.logout);
        name = (TextView) headerview.findViewById(R.id.name);
        email = (TextView) headerview.findViewById(R.id.email);
        image = (CircularImageView) headerview.findViewById(R.id.image);

        navDrawer.setItemIconTintList(null);

        navDrawer.setNavigationItemSelectedListener(this);

        name.setTypeface(helveticaOblique);
        email.setTypeface(helvetica);
        logout.setTypeface(helvetica);

        id = readFromPreferences(MainActivity.this, KEY_USER_ID, "id kda");

        String dirPath = getFilesDir().getAbsolutePath() + File.separator + id + File.separator + "profile.jpg";
        Uri uri = Uri.parse(dirPath);
        image.setImageURI(uri);

        getInfo();

//        String id = readFromPreferences(MainActivity.this,KEY_USER_ID,"id kda");
//        Toast.makeText(MainActivity.this, id, Toast.LENGTH_SHORT).show();

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        selectedItem = savedInstanceState == null ? R.id.nav_item_1 : savedInstanceState.getInt("selectedItem");
        //Toast.makeText(MainActivity.this, selectedItem+ "", Toast.LENGTH_SHORT).show();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            type = extras.getString("type");

            if (type.matches("notification")) {
                selectedItem = R.id.nav_item_2;
            }
        }

        switch (selectedItem) {
            case R.id.nav_item_1:
                Fragment dashboard = new Profile();
                fragmentTransaction.replace(R.id.containerView, dashboard);
                fragmentTransaction.commit();
                title.setText("Profile");
                // toolbar.inflateMenu(R.menu.dashboard_menu);
                break;
            case R.id.nav_item_2:
                Fragment templates = new Chat();
                fragmentTransaction.replace(R.id.containerView, templates);
                fragmentTransaction.commit();
                title.setText("Chat");
                // toolbar.inflateMenu(R.menu.menu_main);
                break;
            case R.id.nav_item_3:
                Fragment priceCalculator = new Nearby();
                fragmentTransaction.replace(R.id.containerView, priceCalculator);
                fragmentTransaction.commit();
                title.setText("Nearby");
                break;
            case R.id.nav_item_4:
                Fragment settings = new Diary();
                fragmentTransaction.replace(R.id.containerView, settings);
                fragmentTransaction.commit();
                title.setText("Diary");
                break;
        }

        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String id = readFromPreferences(MainActivity.this, KEY_USER_ID, "id");

                RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

                String url = "http://appwanderlust.com/appapi/set_user_offline.php";

                final ProgressDialog pDialog = new ProgressDialog(MainActivity.this);

                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);

                CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,
                        url, params,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                //Log.d(TAG, response.toString());
                                JSONObject obj = response;
                                boolean error = false;
                                try {
                                    error = Boolean.valueOf(obj.getString("ecode"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (error) {
                                    try {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Error")
                                                .setMessage(obj.getString("error"))
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // continue with delete
                                                    }
                                                })
                                                .show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {

                                    saveToPreferences(MainActivity.this, KEY_LOGGED, "false");
                                    saveToPreferences(MainActivity.this, KEY_NOTIFICATIONS, "");
                                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                    //Toast.makeText(MainActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //VolleyLog.d(TAG, "Error: " + error.getMessage());
                        //Toast.makeText(MainActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
                    }
                });
                rq.add(jsonObjReq);
            }
        });

        if (checkPlayServices()) {
            registerGCM();
        } else {
            //Toast.makeText(MainActivity.this, "Google is noogle", Toast.LENGTH_SHORT).show();
        }

    }

    private void getInfo() {

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_user.php";

//        final ProgressDialog pDialog = new ProgressDialog(MainActivity.this);
//        pDialog.setMessage("Loading...");
//        pDialog.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);


        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,
                url, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, response.toString());
                        //pDialog.hide();
                        JSONObject obj = response;
                        boolean error = false;
                        try {
                            error = Boolean.valueOf(obj.getString("ecode"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (error) {
                            try {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Error")
                                        .setMessage(obj.getString("error"))
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete
                                            }
                                        })
                                        .show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {

                                JSONObject user = obj.getJSONObject("user");
                                String nick = user.getString("nickname");
                                String eM = user.getString("email");
                                String picture = user.getString("profile_pic");

                                name.setText(nick);
                                email.setText(eM);

                                loadImage(picture);

                                //Toast.makeText(getContext(), user.toString(), Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(getContext(), obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                //pDialog.hide();
                Toast.makeText(MainActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.add(jsonObjReq);
    }

    private void loadImage(String picture) {

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
        imageLoader.get("http://appwanderlust.com/appapi/" + picture, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "Image Load Error: " + error.getMessage());
                //Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    // load image into imageview
                    image.setImageBitmap(response.getBitmap());
                }
            }
        }, 2000, 1200);
    }

    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                //Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean flag = Boolean.valueOf(readFromPreferences(MainActivity.this, KEY_FLAG_BACK_NOT, "false"));

        if (flag) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment templates = new Chat();
            fragmentTransaction.replace(R.id.containerView, templates);
            fragmentTransaction.commit();
            title.setText("Chat");
            saveToPreferences(MainActivity.this, KEY_FLAG_BACK_NOT, "false");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        item.setChecked(true);
        selectedItem = item.getItemId();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        switch (selectedItem) {
            case R.id.nav_item_1:
                Fragment dashboard = new Profile();
                fragmentTransaction.replace(R.id.containerView, dashboard);
                fragmentTransaction.commit();
                title.setText("Profile");
                // toolbar.inflateMenu(R.menu.dashboard_menu);
                break;
            case R.id.nav_item_2:
                Fragment templates = new Chat();
                fragmentTransaction.replace(R.id.containerView, templates);
                fragmentTransaction.commit();
                title.setText("Chat");
                // toolbar.inflateMenu(R.menu.menu_main);
                break;
            case R.id.nav_item_3:
                Fragment priceCalculator = new Nearby();
                fragmentTransaction.replace(R.id.containerView, priceCalculator);
                fragmentTransaction.commit();
                title.setText("Nearby");
                break;
            case R.id.nav_item_4:
                Fragment settings = new Diary();
                fragmentTransaction.replace(R.id.containerView, settings);
                fragmentTransaction.commit();
                title.setText("Diary");
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putInt("selectedItem", selectedItem);
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
}
