package mediaclub.app.appwanderlust;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mediaclub.app.appwanderlust.Adapters.NearbyGridAdapter;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.DataModels.User;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;


/**
 * A simple {@link Fragment} subclass.
 */
public class Nearby extends Fragment {

    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";

    RecyclerView grid;
    View view;
    List<User> users = new ArrayList<User>();
    NearbyGridAdapter adapter;
    double latitude;
    double longitude;
    String id;
    LinearLayout noNear;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public Nearby() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fragment3, container, false);
        grid = (RecyclerView) view.findViewById(R.id.recyclerview);
        noNear = (LinearLayout) view.findViewById(R.id.no_near);

        grid.setLayoutManager(new GridLayoutManager(getContext(), 3));
        grid.addItemDecoration(new ItemDecorationAlbumColumns(5, 3));


        id = readFromPreferences(getContext(),KEY_USER_ID,"id");

//        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getContext());
//        locationProvider.getLastKnownLocation()
//                .subscribe(new Action1<Location>() {
//                    @Override
//                    public void call(Location location) {
//                        //Toast.makeText(getContext(), "lat" + location.getLatitude() + "long " + location.getLongitude(), Toast.LENGTH_SHORT).show();
//                        latitude = location.getLatitude();
//
//                        String lat = String.valueOf(location.getLatitude());
//                        String longi = String.valueOf(location.getLongitude());
//
//
//                        RequestQueue rq = Volley.newRequestQueue(getContext());
//
//                        String tag_json_obj = "json_obj_req";
//
//                        String url = "http://appwanderlust.com/appapi/update_user_location.php";
//
////                        final ProgressDialog pDialog = new ProgressDialog(getContext());
////                        pDialog.setMessage("Loading...");
////                        pDialog.show();
//
//                        Map<String, String> params = new HashMap<String, String>();
//                        params.put("id", id);
//                        params.put("lat", lat);
//                        params.put("long", longi);
//
//                        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,
//                                url, params,
//                                new Response.Listener<JSONObject>() {
//
//                                    @Override
//                                    public void onResponse(JSONObject response) {
//                                        //Log.d(TAG, response.toString());
//                                        //pDialog.hide();
//                                        JSONObject obj = response;
//                                        boolean error = false;
//                                        try {
//                                            error = Boolean.valueOf(obj.getString("ecode"));
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                        if (error) {
//                                            try {
//                                                new AlertDialog.Builder(getContext())
//                                                        .setTitle("Error")
//                                                        .setMessage(obj.getString("error"))
//                                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                            public void onClick(DialogInterface dialog, int which) {
//                                                                // continue with delete
//                                                            }
//                                                        })
//                                                        .show();
//                                            } catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }
//                                        } else {
//                                            Toast.makeText(getContext(), "Location updated", Toast.LENGTH_SHORT).show();
//
//                                            RequestQueue rq = Volley.newRequestQueue(getContext().getApplicationContext());
//
//                                            String tag_json_obj = "json_obj_req";
//
//                                            String url = "http://appwanderlust.com/appapi/get_around.php";
//
//                                            final ProgressDialog pDialog = new ProgressDialog(getContext());
//                                            pDialog.setMessage("Loading...");
//                                            pDialog.show();
//
//                                            Map<String, String> params = new HashMap<String, String>();
//                                            params.put("id", id);
//
//                                            CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,
//                                                    url, params,
//                                                    new Response.Listener<JSONObject>() {
//
//                                                        @Override
//                                                        public void onResponse(JSONObject response) {
//                                                            //Log.d(TAG, response.toString());
//                                                            pDialog.hide();
//                                                            JSONObject obj = response;
//                                                            boolean error = false;
//                                                            try {
//                                                                error = Boolean.valueOf(obj.getString("ecode"));
//                                                            } catch (JSONException e) {
//                                                                e.printStackTrace();
//                                                            }
//                                                            if (error) {
//                                                                try {
//                                                                    new AlertDialog.Builder(getContext())
//                                                                            .setTitle("Error")
//                                                                            .setMessage(obj.getString("error"))
//                                                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                                                public void onClick(DialogInterface dialog, int which) {
//                                                                                    // continue with delete
//                                                                                }
//                                                                            })
//                                                                            .show();
//                                                                } catch (JSONException e) {
//                                                                    e.printStackTrace();
//                                                                }
//                                                            } else {
//
//                                                                try {
//                                                                    JSONArray around = obj.getJSONArray("around");
//                                                                    for(int i =0;i<around.length();i++){
//                                                                        JSONObject ob = around.getJSONObject(i);
//                                                                        String id = ob.getString("id");
//                                                                        String name = ob.getString("fname");
//                                                                        String nationality = ob.getString("country");
//                                                                        String distance = ob.getString("distance");
//                                                                        String image = ob.getString("profile_pic");
//                                                                        String nickname = ob.getString("nickname");
//                                                                        users.add(new User(name,id,image,nationality,distance,nickname));
//                                                                    }
//                                                                    adapter.notifyDataSetChanged();
//                                                                    if(users.size() > 0){
//                                                                        noNear.setVisibility(View.GONE);
//                                                                        grid.setVisibility(View.VISIBLE);
//                                                                    }
//                                                                } catch (JSONException e) {
//                                                                    e.printStackTrace();
//                                                                }
//
//                                                                //Toast.makeText(getContext(), obj.toString(), Toast.LENGTH_SHORT).show();
//                                                            }
//                                                        }
//                                                    }, new Response.ErrorListener() {
//
//                                                @Override
//                                                public void onErrorResponse(VolleyError error) {
//                                                    //VolleyLog.d(TAG, "Error: " + error.getMessage());
//                                                    pDialog.hide();
//                                                    Toast.makeText(getContext(), error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//                                            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                                            rq.add(jsonObjReq);
//                                        }
//                                    }
//                                }, new Response.ErrorListener() {
//
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                //VolleyLog.d(TAG, "Error: " + error.getMessage());
//                                //pDialog.hide();
//                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        }) {
//                        };
//
//                        rq.add(jsonObjReq);
//                    }
//                });
//
//        if (latitude == 0) {
//            LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
//                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                    .setNumUpdates(1)
//                    .setInterval(100);
//
//            ReactiveLocationProvider locationProvider1 = new ReactiveLocationProvider(getContext());
//            Subscription subscription = locationProvider1.getUpdatedLocation(request)
//                    .subscribe(new Action1<Location>() {
//                        @Override
//                        public void call(Location location) {
//                            //Toast.makeText(getContext(), "lat" + location.getLatitude() + "long " + location.getLongitude(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
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

    private void getNearby() {

        String lat = String.valueOf(latitude);
        String longi = String.valueOf(longitude);


        RequestQueue rq = Volley.newRequestQueue(getContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/update_user_location.php";

//                        final ProgressDialog pDialog = new ProgressDialog(getContext());
//                        pDialog.setMessage("Loading...");
//                        pDialog.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);
        params.put("lat", lat);
        params.put("long", longi);

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
                            //Toast.makeText(getContext(), "Location updated", Toast.LENGTH_SHORT).show();

                            RequestQueue rq = Volley.newRequestQueue(getContext().getApplicationContext());

                            String tag_json_obj = "json_obj_req";

                            String url = "http://appwanderlust.com/appapi/get_around.php";

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
                                                users.clear();
                                                //Toast.makeText(getContext(),users.toString()+ " hi ", Toast.LENGTH_SHORT).show();
                                                try {
                                                    JSONArray around = obj.getJSONArray("around");
                                                    for(int i =0;i<around.length();i++){
                                                        JSONObject ob = around.getJSONObject(i);
                                                        String id = ob.getString("id");
                                                        String name = ob.getString("fname");
                                                        String nationality = ob.getString("country");
                                                        String distance = ob.getString("distance");
                                                        String image = ob.getString("profile_pic");
                                                        String nickname = ob.getString("nickname");
                                                        users.add(new User(name,id,image,nationality,distance,nickname));
                                                    }
                                                    adapter = new NearbyGridAdapter(getContext(), users);
                                                    grid.setAdapter(adapter);
                                                    adapter.notifyDataSetChanged();
                                                    if(users.size() > 0){
                                                        noNear.setVisibility(View.GONE);
                                                        grid.setVisibility(View.VISIBLE);
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                //Toast.makeText(getContext(), obj.toString(), Toast.LENGTH_SHORT).show();
                                                //Log.v("Near", obj.toString());
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
                            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            rq.add(jsonObjReq);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                //pDialog.hide();
                //Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
        };

        rq.add(jsonObjReq);
    }

    private void getNear() {

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getContext());
        locationProvider.getLastKnownLocation()
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        //Toast.makeText(getContext(), latitude + " " + longitude, Toast.LENGTH_SHORT).show();

                        Toast.makeText(getContext(), "last known", Toast.LENGTH_SHORT).show();
                    }
                });
        if (latitude == 0 && longitude == 0) {
            LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(1)
                    .setInterval(100);

            ReactiveLocationProvider locationProvider1 = new ReactiveLocationProvider(getContext());
            Subscription subscription = locationProvider1.getUpdatedLocation(request)
                    .subscribe(new Action1<Location>() {
                        @Override
                        public void call(Location location) {
                            //Toast.makeText(getContext(), "lat" + location.getLatitude() + "long " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            //Toast.makeText(getContext(), latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                            Toast.makeText(getContext(), "request", Toast.LENGTH_SHORT).show();
                            getNearby();
                        }
                    });
        }else{
            getNearby();
        }

        //Toast.makeText(getContext(), latitude + " " + longitude, Toast.LENGTH_SHORT).show();

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


    @Override
    public void onResume() {
        super.onResume();

        getNear();

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
