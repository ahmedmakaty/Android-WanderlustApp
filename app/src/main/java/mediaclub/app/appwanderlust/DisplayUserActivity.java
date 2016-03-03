package mediaclub.app.appwanderlust;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.mikhaellopez.circularimageview.CircularImageView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.DataModels.Post;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;

public class DisplayUserActivity extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    Toolbar toolbar;
    TextView title;
    String id, nickname;
    RecyclerView imagesView;
    GridAdapter adapter;
    List<String> images = new ArrayList<String>();
    LinearLayout chat, viewDiary;
    JSONObject user;
    TextView name, genderAge, about, travel, looking, country, aboutTitle, travelTitle, lookingTitle, photosTitle;
    CircularImageView image;
    ImageView cover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_user);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        imagesView = (RecyclerView) findViewById(R.id.recyclerview);
        chat = (LinearLayout) findViewById(R.id.chat);
        viewDiary = (LinearLayout) findViewById(R.id.display_diary);
        name = (TextView) findViewById(R.id.name);
        aboutTitle = (TextView) findViewById(R.id.about_title);
        travelTitle = (TextView) findViewById(R.id.travel_title);
        lookingTitle = (TextView) findViewById(R.id.looking_title);
        photosTitle = (TextView) findViewById(R.id.photos_title);
        genderAge = (TextView) findViewById(R.id.gender_age);
        about = (TextView) findViewById(R.id.about);
        country = (TextView) findViewById(R.id.country);
        travel = (TextView) findViewById(R.id.travel);
        looking = (TextView) findViewById(R.id.looking);
        image = (CircularImageView) findViewById(R.id.image);
        cover = (ImageView) findViewById(R.id.cover);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT.ttf");
        Typeface helveticaBold = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT Bold.ttf");

        title.setTypeface(helveticaBold);
        name.setTypeface(helveticaBold);
        genderAge.setTypeface(helvetica);
        country.setTypeface(helvetica);
        about.setTypeface(helvetica);
        travel.setTypeface(helvetica);
        looking.setTypeface(helvetica);
        aboutTitle.setTypeface(helveticaBold);
        travelTitle.setTypeface(helveticaBold);
        lookingTitle.setTypeface(helveticaBold);
        photosTitle.setTypeface(helveticaBold);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            id = extras.getString("ID");
            nickname = extras.getString("NICKNAME");
        } else {
            id = savedInstanceState.getString("DISPLAYID");
            nickname = savedInstanceState.getString("DISPLAYNICKNAME");
        }

        loadUserData();

        getUserImages();

        //imagesView.setNestedScrollingEnabled(false);
        imagesView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new GridAdapter(this, images);
        imagesView.setAdapter(adapter);

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DisplayUserActivity.this, ChatActivity.class);
                i.putExtra("ID", id);
                i.putExtra("NICKNAME", nickname);
                startActivity(i);
            }
        });

        viewDiary.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(DisplayUserActivity.this, UserDiary.class);
                i.putExtra("ID", id);
                startActivity(i);

            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    String token = intent.getStringExtra("token");

                    //Toast.makeText(getApplicationContext(), "GCM registration token: " + token, Toast.LENGTH_LONG).show();

                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                    // gcm registration id is stored in our server's MySQL

                    //Toast.makeText(getApplicationContext(), "GCM registration token is stored in server!", Toast.LENGTH_LONG).show();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    NotificationUtils notificationUtils = new NotificationUtils();
                    notificationUtils.playNotificationSound();
                    String message = intent.getStringExtra("message");
                    String from = intent.getStringExtra("from");
                    //Toast.makeText(getApplicationContext(), from + ": " + message, Toast.LENGTH_LONG).show();
                    //saveToPreferences(MainActivity.this,KEY_NOTIFICATIONS,null);
                }
            }
        };
    }

    private void getUserImages() {

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_user_images.php";

        final ProgressDialog pDialog = new ProgressDialog(DisplayUserActivity.this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);


        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,
                url, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, response.toString());
                        pDialog.hide();
                        JSONObject obj = response;
                        boolean error = false;
                        try {
                            error = Boolean.valueOf(obj.getString("ecode"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (error) {
                            try {
                                new AlertDialog.Builder(DisplayUserActivity.this)
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
                                JSONArray imageUrls = obj.getJSONArray("images");
                                for (int i = 0; i < imageUrls.length(); i++) {
                                    JSONObject ob = imageUrls.getJSONObject(i);
                                    images.add(ob.getString("pic_url"));
                                }
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //Toast.makeText(DisplayUserActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
                //Toast.makeText(DisplayUserActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });

        rq.add(jsonObjReq);
    }

    private void loadUserData() {

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_user.php";

        final ProgressDialog pDialog = new ProgressDialog(DisplayUserActivity.this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,
                url, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, response.toString());
                        pDialog.hide();
                        JSONObject obj = response;
                        boolean error = false;
                        try {
                            error = Boolean.valueOf(obj.getString("ecode"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (error) {
                            try {
                                new AlertDialog.Builder(DisplayUserActivity.this)
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
                                user = obj.getJSONObject("user");
                                if (!user.getString("fname").matches("null") && !user.getString("lname").matches("null")) {
                                    name.setText(user.getString("fname") + " " + user.getString("lname"));
                                }
                                if (!user.getString("fname").matches("null") && !user.getString("lname").matches("null")) {
                                    title.setText(user.getString("fname") + " " + user.getString("lname"));
                                } else {
                                    title.setText(user.getString("nickname"));
                                }
                                country.setText(user.getString("country"));
                                if (!user.getString("about").matches("null")) {
                                    about.setText(user.getString("about"));
                                }
                                if (!user.getString("traveltext").matches("null")) {
                                    travel.setText(user.getString("traveltext"));
                                }
                                if (!user.getString("lookingfor").matches("null")) {
                                    looking.setText(user.getString("lookingfor"));
                                }


                                if (user.getString("age").matches("null")) {
                                    genderAge.setText("Not set");
                                } else {
                                    String gender = "Male";

                                    if (user.getString("gender").matches("0")) {
                                        gender = "Female";
                                    }

                                    gender = gender + " " + user.getString("age");

                                    genderAge.setText(gender);
                                }

                                String proP = user.getString("profile_pic");
                                String covP = user.getString("cover_pic");

                                loadImages(proP, covP);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(DisplayUserActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
                //Toast.makeText(DisplayUserActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });

        rq.add(jsonObjReq);

    }

    private void loadImages(String proP, String covP) {

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
        imageLoader.get("http://appwanderlust.com/appapi/" + proP, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "Image Load Error: " + error.getMessage());
                //Toast.makeText(DisplayUserActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    // load image into imageview
                    image.setImageBitmap(response.getBitmap());
                }
            }
        }, 2000, 1200);

        imageLoader.get("http://appwanderlust.com/appapi/" + covP, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "Image Load Error: " + error.getMessage());
                //Toast.makeText(DisplayUserActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    // load image into imageview
                    cover.setImageBitmap(response.getBitmap());
                }
            }
        }, 2000, 1200);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
//            Intent parentIntent = NavUtils.getParentActivityIntent(this);
//            parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            startActivity(parentIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("DISPLAYID", id);
        outState.putString("DISPLAYNICKNAME", nickname);
    }
}
