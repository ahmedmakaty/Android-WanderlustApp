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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class Profile extends Fragment {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";

    View view;
    Spinner spinner;
    String[] countries;
    RadioGroup radioGroup;
    RadioButton genderBtn;
    Toolbar toolbar;
    TextView title;
    RecyclerView imagesView;
    GridAdapter imagesAdapter;
    List<String> images = new ArrayList<String>();
    TextView edit;
    ImageView cover;
    CircularImageView image;
    String id;
    JSONObject user;
    TextView name, genderAge, country, about, travel, looking, aboutTitle, travelTitle, lookingTitle, photosTitle;

    public Profile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_fragment1, container, false);

        imagesView = (RecyclerView) view.findViewById(R.id.recyclerview);
        edit = (TextView) view.findViewById(R.id.edit);
        image = (CircularImageView) view.findViewById(R.id.image);
        cover = (ImageView) view.findViewById(R.id.cover);
        name = (TextView) view.findViewById(R.id.name);
        genderAge = (TextView) view.findViewById(R.id.gender_age);
        country = (TextView) view.findViewById(R.id.country);
        about = (TextView) view.findViewById(R.id.about);
        travel = (TextView) view.findViewById(R.id.travel);
        looking = (TextView) view.findViewById(R.id.looking);
        aboutTitle = (TextView) view.findViewById(R.id.about_title);
        travelTitle = (TextView) view.findViewById(R.id.travel_title);
        lookingTitle = (TextView) view.findViewById(R.id.looking_title);
        photosTitle = (TextView) view.findViewById(R.id.photos_title);

        Typeface myFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Helvetica LT.ttf");
        Typeface helveticaBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Helvetica LT Bold.ttf");

        name.setTypeface(helveticaBold);

        about.setTypeface(myFont);
        travel.setTypeface(myFont);
        looking.setTypeface(myFont);

        aboutTitle.setTypeface(helveticaBold);
        travelTitle.setTypeface(helveticaBold);
        lookingTitle.setTypeface(helveticaBold);
        photosTitle.setTypeface(helveticaBold);

        edit.setTypeface(myFont);
        genderAge.setTypeface(myFont);
        country.setTypeface(myFont);

//        loadUserData();
//        Toast.makeText(getContext(), images.size()+"", Toast.LENGTH_SHORT).show();
//
//        getUserImages();

        edit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), UserActivity.class);
                startActivity(i);
            }
        });

        //imagesView.setNestedScrollingEnabled(false);
        imagesView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        imagesAdapter = new GridAdapter(getActivity(), images);
        imagesView.setAdapter(imagesAdapter);

        id = readFromPreferences(getContext(), KEY_USER_ID, "id kda");


        String dirPath = getContext().getFilesDir().getAbsolutePath() + File.separator + id + File.separator + "profile.jpg";

        File imageDir = new File(dirPath);
        if (imageDir.exists()) {
            Log.v("PathPP", dirPath);
            Uri uri = Uri.parse(dirPath);
            image.setImageURI(uri);
        }

//        String coverPath = getContext().getFilesDir().getAbsolutePath() + File.separator + id + File.separator + "cover.jpg";
//
//        File coverDir = new File(coverPath);
//        if (coverDir.exists()) {
//            Log.v("PathCP", coverPath);
//            Uri cUri = Uri.parse(coverPath);
//            cover.setImageURI(cUri);
//        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    String token = intent.getStringExtra("token");

                    //Toast.makeText(getContext().getApplicationContext(), "GCM registration token: " + token, Toast.LENGTH_LONG).show();

                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                    // gcm registration id is stored in our server's MySQL

                    //Toast.makeText(getContext().getApplicationContext(), "GCM registration token is stored in server!", Toast.LENGTH_LONG).show();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    NotificationUtils notificationUtils = new NotificationUtils();
                    notificationUtils.playNotificationSound();
                    String message = intent.getStringExtra("message");
                    String from = intent.getStringExtra("from");
                    String time = intent.getStringExtra("time");
                    String other = intent.getStringExtra("otherId");
                    //Toast.makeText(getApplicationContext(), from + ": " + message + " " + other, Toast.LENGTH_LONG).show();
                    //saveToPreferences(MainActivity.this,KEY_NOTIFICATIONS,null);
                }
            }
        };

        return view;
    }

    private void getUserImages() {

        RequestQueue rq = Volley.newRequestQueue(getContext().getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_user_images.php";

        final ProgressDialog pDialog = new ProgressDialog(getContext());
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
                                new AlertDialog.Builder(getContext())
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

                            images.clear();
                            try {
                                JSONArray imageUrls = obj.getJSONArray("images");
                                for (int i = 0; i < imageUrls.length(); i++) {
                                    JSONObject ob = imageUrls.getJSONObject(i);
                                    images.add(ob.getString("pic_url"));
                                }
                                imagesAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(getContext(), images.size()+"", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getContext(), obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
                //Toast.makeText(getContext(), error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });

        rq.add(jsonObjReq);

    }

    private void loadUserData() {

        RequestQueue rq = Volley.newRequestQueue(getContext().getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_user.php";

        final ProgressDialog pDialog = new ProgressDialog(getContext());
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
                                new AlertDialog.Builder(getContext())
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

                                //Toast.makeText(getContext(), user.toString(), Toast.LENGTH_SHORT).show();

                                if (!user.getString("fname").matches("null") && !user.getString("lname").matches("null") && !user.getString("fname").matches("") && !user.getString("lname").matches("null")) {
                                    name.setText(user.getString("fname") + " " + user.getString("lname"));
                                }
                                country.setText(user.getString("country"));
                                if (!user.getString("about").matches("") && !user.getString("about").matches("")) {
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
                            //Toast.makeText(getContext(), obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
                Toast.makeText(getContext(), error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.add(jsonObjReq);

    }

    private void loadImages(String proP, String covP) {

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
        imageLoader.get("http://appwanderlust.com/appapi/" + proP, new ImageLoader.ImageListener() {

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

        imageLoader.get("http://appwanderlust.com/appapi/" + covP, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "Image Load Error: " + error.getMessage());
                //Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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


//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.profile_edit_menu, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        if (item.getItemId() == R.id.save) {
////            Intent i = new Intent(getActivity(), GetContacts.class);
////            i.putExtra("REQUESTTYPE","new");
////            startActivity(i);
//        }
//        return super.onOptionsItemSelected(item);
//    }

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

    @Override
    public void onResume() {
        super.onResume();

        id = readFromPreferences(getContext(), KEY_USER_ID, "id kda");
        loadUserData();
        getUserImages();
        //Toast.makeText(getContext(), "Inside resume", Toast.LENGTH_SHORT).show();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRegistrationBroadcastReceiver);
    }
}
