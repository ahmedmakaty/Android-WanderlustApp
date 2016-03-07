package mediaclub.app.appwanderlust;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.facebook.*;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.Controller.CustomJsonArrayRequest;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.Controller.GPSTracker;

import mediaclub.app.appwanderlust.Controller.PostJsonArrayRequest;
import mediaclub.app.appwanderlust.RealmModels.lUser;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;

import static com.android.volley.Request.Method.*;

public class LoginActivity extends AppCompatActivity {

    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_PP_URL = "pp_url";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_NICKNAME = "nickname";
    public static final String KEY_LOGGED = "logged";

    TextView join, login,que,facebookText;
    double latitude = 0;
    double longitude = 0;
    EditText emailField, passwordField;
    String email, password;
    //GPSTracker gps;
    JSONObject user;
    LinearLayout facebook;
    CallbackManager callbackManager;
    String fbEmail, fname, lname, gender;
    Realm realm;
    String rId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        realm = Realm.getDefaultInstance();

        join = (TextView) findViewById(R.id.join);
        login = (TextView) findViewById(R.id.login);
        que = (TextView) findViewById(R.id.que);
        facebookText = (TextView) findViewById(R.id.facebook_text);
        emailField = (EditText) findViewById(R.id.email);
        passwordField = (EditText) findViewById(R.id.password);
        facebook = (LinearLayout) findViewById(R.id.facebook_button);

        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT.ttf");
        Typeface helveticaBold = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT Bold.ttf");

        join.setTypeface(helveticaBold);
        login.setTypeface(helveticaBold);
        facebookText.setTypeface(helveticaBold);
        que.setTypeface(helvetica);
        emailField.setTypeface(helvetica);
        passwordField.setTypeface(helvetica);

        FacebookSdk.sdkInitialize(getApplicationContext());

        //Toast.makeText(LoginActivity.this, "ayklam", Toast.LENGTH_SHORT).show();
//        BackgroundTask task = new BackgroundTask(this);
//        task.execute();

//        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
//        locationProvider.getLastKnownLocation()
//                .subscribe(new Action1<Location>() {
//                    @Override
//                    public void call(Location location) {
//                        Toast.makeText(LoginActivity.this, "lat" + location.getLatitude(), Toast.LENGTH_SHORT).show();
//                        latitude = location.getLatitude();
//                    }
//                });
//
//        if (latitude == 0) {
//            LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
//                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                    .setNumUpdates(5)
//                    .setInterval(100);
//
//            ReactiveLocationProvider locationProvider1 = new ReactiveLocationProvider(this);
//            Subscription subscription = locationProvider1.getUpdatedLocation(request)
//                    .subscribe(new Action1<Location>() {
//                        @Override
//                        public void call(Location location) {
//                            Toast.makeText(LoginActivity.this, "lat" + location.getLatitude(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                Log.v("LoginActivity", object.toString());

                                RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

                                String tag_json_obj = "json_obj_req";

                                try {
                                    fbEmail = object.getString("email");
                                    fname = object.getString("first_name");
                                    lname = object.getString("last_name");
                                    gender = object.getString("gender");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                String url = "http://appwanderlust.com/appapi/check_if_fb_user.php";

//        final ProgressDialog pDialog = new ProgressDialog(getContext());
//        pDialog.setMessage("Loading...");
//        pDialog.show();

                                Map<String, String> params = new HashMap<String, String>();
                                params.put("email", fbEmail);

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
                                                    Intent i = new Intent(LoginActivity.this, FacebookNicknameActivity.class);
                                                    i.putExtra("FNAME", fname);
                                                    i.putExtra("LNAME", lname);
                                                    i.putExtra("GENDER", gender);
                                                    i.putExtra("EMAIL", fbEmail);
                                                    startActivity(i);
                                                } else {
                                                    //Toast.makeText(LoginActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                                                    try {
                                                        user = obj.getJSONObject("user");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        saveToPreferences(LoginActivity.this, KEY_USER_ID, user.getString("id"));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    saveToPreferences(LoginActivity.this, KEY_LOGGED, "true");
                                                    try {
                                                        requestBindingNotifications();
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(i);
                                                }
                                            }
                                        }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //VolleyLog.d(TAG, "Error: " + error.getMessage());
                                        //pDialog.hide();
                                        //Toast.makeText(LoginActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                rq.add(jsonObjReq);
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,gender,location");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                //Toast.makeText(LoginActivity.this, "error in with facebook", Toast.LENGTH_SHORT).show();
            }
        });

        join.setOnClickListener(new View.OnClickListener()

                                {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                                        startActivity(i);
                                    }
                                }

        );

        login.setOnClickListener(new View.OnClickListener() {

                                     @Override
                                     public void onClick(View v) {

                                         //Toast.makeText(LoginActivity.this, email + password, Toast.LENGTH_SHORT).show();

                                         RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
                                         email = emailField.getText().toString();
                                         password = passwordField.getText().toString();

                                         // Tag used to cancel the request
                                         String tag_json_obj = "json_obj_req";

                                         String url = "http://appwanderlust.com/appapi/login.php";

                                         final ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);
                                         pDialog.setMessage("Loading...");
                                         pDialog.show();

                                         Map<String, String> params = new HashMap<String, String>();
                                         params.put("email", email);
                                         params.put("password", password);

//                                         CustomJsonArrayRequest jsonObjReq = new CustomJsonArrayRequest(Method.POST, url, params,
//                                                 new Response.Listener<JSONArray>() {
//                                                     @Override
//                                                     public void onResponse(JSONArray response) {
//                                                         pDialog.hide();
//                                                         Toast.makeText(LoginActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
//                                                     }
//                                                 }, new Response.ErrorListener() {
//                                             @Override
//                                             public void onErrorResponse(VolleyError error) {
//                                                 pDialog.hide();
//                                                 Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//                                             }
//                                         });

                                         CustomRequest jsonObjReq = new CustomRequest(Method.POST,
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
                                                                 new AlertDialog.Builder(LoginActivity.this)
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
                                                             } catch (JSONException e) {
                                                                 e.printStackTrace();
                                                             }
                                                             try {
                                                                 saveToPreferences(LoginActivity.this, KEY_USER_ID, user.getString("id"));
                                                                 saveToPreferences(LoginActivity.this, KEY_USER_PP_URL, user.getString("profile_pic"));
                                                                 saveToPreferences(LoginActivity.this, KEY_USER_EMAIL, user.getString("email"));
                                                             } catch (JSONException e) {
                                                                 e.printStackTrace();
                                                             }
                                                             saveToPreferences(LoginActivity.this, KEY_LOGGED, "true");
                                                             try {
                                                                 requestBindingNotifications();
                                                             } catch (JSONException e) {
                                                                 e.printStackTrace();
                                                             }

                                                             try {
                                                                 rId = user.getString("id");
                                                             } catch (JSONException e) {
                                                                 e.printStackTrace();
                                                             }

//                                                             RealmResults<lUser> rlUsers = realm.where(lUser.class).equalTo("id", rId).findAll();
//
//                                                             if(rlUsers.size() > 0){
//                                                                 //Toast.makeText(LoginActivity.this, "exists", Toast.LENGTH_SHORT).show();
//                                                             }else{
                                                             lUser rUser = new lUser();
                                                             try {
                                                                 rUser.setId(user.getString("id"));
                                                                 rUser.setFname(user.getString("fname"));
                                                                 rUser.setLname(user.getString("lname"));
                                                                 rUser.setEmail(user.getString("email"));
                                                                 rUser.setNickname(user.getString("nickname"));
                                                                 rUser.setGender(user.getString("gender"));
                                                                 rUser.setCountry(user.getString("country"));
                                                                 rUser.setAge(user.getString("age"));
                                                                 rUser.setAbout(user.getString("about"));
                                                                 rUser.setTravel(user.getString("traveltext"));
                                                                 rUser.setLookingfor(user.getString("lookingfor"));

                                                                 realm.beginTransaction();
                                                                 realm.copyToRealmOrUpdate(rUser);
                                                                 realm.commitTransaction();

                                                             } catch (JSONException e) {
                                                                 e.printStackTrace();
                                                             }
//                                                             }


                                                             Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                             startActivity(i);
                                                         }
                                                     }
                                                 }, new Response.ErrorListener() {

                                             @Override
                                             public void onErrorResponse(VolleyError error) {
                                                 //VolleyLog.d(TAG, "Error: " + error.getMessage());
                                                 pDialog.hide();
                                                 //Toast.makeText(LoginActivity.this, "error sa5ef0", Toast.LENGTH_SHORT).show();
                                             }
                                         });

// Adding request to request queue
                                         rq.add(jsonObjReq);

//                                         Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                                         startActivity(i);
                                     }
                                 }
        );

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> permissions = new ArrayList<String>();
                permissions.add("public_profile");
                permissions.add("email");
                permissions.add("user_location");
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, permissions);
            }
        });

//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LoginManager.getInstance().logOut();
//            }
//        });
    }

    private void requestBindingNotifications() throws JSONException {

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        String url = "http://appwanderlust.com/appapi/request_binding_notifications.php";

        final ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);

        String id = user.getString("id");

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
                                new AlertDialog.Builder(LoginActivity.this)
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

                            //Toast.makeText(LoginActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                //Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        rq.add(jsonObjReq);
    }

//    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
//        private ProgressDialog dialog;
//
//        public BackgroundTask(Activity activity) {
//            dialog = new ProgressDialog(activity);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            dialog.setMessage("Getting your location, please wait!");
//            dialog.show();
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            if (dialog.isShowing()) {
//                dialog.dismiss();
//            }
//            Toast.makeText(LoginActivity.this, "Hi " + latitude, Toast.LENGTH_SHORT).show();
//            if(latitude == 0){
//                BackgroundTask1 task1 = new BackgroundTask1(LoginActivity.this);
//                task1.execute();
//            }
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
////            while (true) {
////                gps = new GPSTracker(LoginActivity.this);
////                latitude = gps.getLatitude();
////                longitude = gps.getLongitude();
////                if (latitude > 0) {
////                    break;
////                }
////            }
//            Looper.prepare();
//            gps = new GPSTracker(LoginActivity.this);
//
//            latitude = gps.getLatitude();
//            longitude = gps.getLongitude();
//
//            // \n is for new line
//            return null;
//        }
//
//    }
//
//    private class BackgroundTask1 extends AsyncTask<Void, Void, Void> {
//        private ProgressDialog dialog;
//
//        public BackgroundTask1(Activity activity) {
//            dialog = new ProgressDialog(activity);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            dialog.setMessage("Getting your location, please wait!");
//            dialog.show();
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            if (dialog.isShowing()) {
//                dialog.dismiss();
//            }
//            Toast.makeText(LoginActivity.this, "Hi " + latitude, Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            latitude = gps.getLatitude();
//            longitude = gps.getLongitude();
//            return null;
//        }
//
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
