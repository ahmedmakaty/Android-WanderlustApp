package mediaclub.app.appwanderlust;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.soundcloud.android.crop.Crop;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.RealmModels.lUser;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;

public class UserActivity extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";

    Spinner spinner;
    String[] countries;
    RadioGroup radioGroup;
    RadioButton genderBtn;
    Toolbar toolbar;
    TextView title,aboutTitle,travelTitle,lookingTitle,photosTitle,nationality;
    RecyclerView imagesView;
    EditGridAdapter imagesAdapter;
    List<String> images = new ArrayList<String>();
    CircularImageView pp_image;
    ImageView cover;
    EditText fnameField,lnameField,ageField,aboutField,travelField,lookingField;
    boolean genderType;
    String country;
    String id;
    JSONObject user = new JSONObject();
    RadioButton male,female;
    Bitmap pP,coverP;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        realm = Realm.getDefaultInstance();

        spinner = (Spinner) findViewById(R.id.spinner);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        aboutTitle = (TextView) findViewById(R.id.about_title);
        travelTitle = (TextView) findViewById(R.id.travel_title);
        lookingTitle = (TextView) findViewById(R.id.looking_title);
        photosTitle = (TextView) findViewById(R.id.photos_title);
        nationality = (TextView) findViewById(R.id.nationality);

        imagesView = (RecyclerView) findViewById(R.id.recyclerview);
        pp_image = (CircularImageView) findViewById(R.id.image);
        cover = (ImageView) findViewById(R.id.cover);
        fnameField = (EditText) findViewById(R.id.fname);
        lnameField = (EditText) findViewById(R.id.lname);
        ageField = (EditText) findViewById(R.id.age);
        aboutField = (EditText) findViewById(R.id.about);
        travelField = (EditText) findViewById(R.id.travel);
        lookingField = (EditText) findViewById(R.id.looking);
        male = (RadioButton) findViewById(R.id.radioButton);
        female = (RadioButton) findViewById(R.id.radioButton2);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        title.setText("Edit Profile");

        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT.ttf");
        Typeface helveticaBold = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT Bold.ttf");
        title.setTypeface(helveticaBold);
        fnameField.setTypeface(helvetica);
        lnameField.setTypeface(helvetica);
        ageField.setTypeface(helvetica);
        aboutField.setTypeface(helvetica);
        travelField.setTypeface(helvetica);
        lookingField.setTypeface(helvetica);
        aboutTitle.setTypeface(helveticaBold);
        travelTitle.setTypeface(helveticaBold);
        lookingTitle.setTypeface(helveticaBold);
        photosTitle.setTypeface(helveticaBold);
        male.setTypeface(helvetica);
        female.setTypeface(helvetica);
        nationality.setTypeface(helvetica);

        id = readFromPreferences(UserActivity.this, KEY_USER_ID, "id kda");

        RealmResults<lUser> users = realm.where(lUser.class).findAll();
        Toast.makeText(UserActivity.this, users.size()+"", Toast.LENGTH_SHORT).show();

        //Toast.makeText(UserActivity.this, rUser.toString(), Toast.LENGTH_SHORT).show();

        getUserData();

        countries = new String[]{"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla",

                "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria",

                "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",

                "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana",

                "Brazil", "British Indian Ocean Territory", "British Virgin Islands", "Brunei", "Bulgaria",

                "Burkina Faso", "Burma (Myanmar)", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde",

                "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island",

                "Cocos (Keeling) Islands", "Colombia", "Comoros", "Cook Islands", "Costa Rica",

                "Croatia", "Cuba", "Cyprus", "Czech Republic", "Democratic Republic of the Congo",

                "Denmark", "Djibouti", "Dominica", "Dominican Republic",

                "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia",

                "Ethiopia", "Falkland Islands", "Faroe Islands", "Fiji", "Finland", "France", "French Polynesia",

                "Gabon", "Gambia", "Gaza Strip", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece",

                "Greenland", "Grenada", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana",

                "Haiti", "Holy See (Vatican City)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India",

                "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Ivory Coast", "Jamaica",

                "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kosovo", "Kuwait",

                "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein",

                "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", "Malaysia",

                "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mayotte", "Mexico",

                "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco",

                "Mozambique", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia",

                "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Korea",

                "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama",

                "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn Islands", "Poland",

                "Portugal", "Puerto Rico", "Qatar", "Republic of the Congo", "Romania", "Russia", "Rwanda",

                "Saint Barthelemy", "Saint Helena", "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin",

                "Saint Pierre and Miquelon", "Saint Vincent and the Grenadines", "Samoa", "San Marino",

                "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone",

                "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Korea",

                "Spain", "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland",

                "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tokelau",

                "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands",

                "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "US Virgin Islands", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam",

                "Wallis and Futuna", "West Bank", "Yemen", "Zambia", "Zimbabwe"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countries);
        spinner.setAdapter(adapter);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //imagesView.setNestedScrollingEnabled(false);
        imagesView.setLayoutManager(new GridLayoutManager(this, 4));
        imagesAdapter = new EditGridAdapter(this, images);
        imagesView.setAdapter(imagesAdapter);

        getUserImages();

        //Toast.makeText(UserActivity.this, id, Toast.LENGTH_SHORT).show();

        pp_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 2);
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

        final ProgressDialog pDialog = new ProgressDialog(UserActivity.this);
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
                                new AlertDialog.Builder(UserActivity.this)
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
                                imagesAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //Toast.makeText(UserActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
                //Toast.makeText(UserActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });

        rq.add(jsonObjReq);

    }

    private void getUserData() {

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_user.php";

        final ProgressDialog pDialog = new ProgressDialog(UserActivity.this);
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
                                new AlertDialog.Builder(UserActivity.this)
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
                                if (!user.getString("fname").matches("null")) {
                                    fnameField.setText(user.getString("fname"));
                                }
                                if (!user.getString("lname").matches("null")) {
                                    lnameField.setText(user.getString("lname"));
                                }
                                if (!user.getString("age").matches("null")) {
                                    ageField.setText(user.getString("age"));
                                }
                                if (!user.getString("about").matches("null")) {
                                    aboutField.setText(user.getString("about"));
                                }
                                if (!user.getString("traveltext").matches("null")) {
                                    travelField.setText(user.getString("traveltext"));
                                }
                                if (!user.getString("lookingfor").matches("null")) {
                                    lookingField.setText(user.getString("lookingfor"));
                                }

                                String proP = user.getString("profile_pic");
                                String covP = user.getString("cover_pic");

                                loadImages(proP, covP);

                                String gender = user.getString("gender");
                                if (gender.matches("1")) {
                                    radioGroup.check(R.id.radioButton);
                                } else {
                                    radioGroup.check(R.id.radioButton2);
                                }

                                String country = user.getString("country");
                                int index = Arrays.asList(countries).indexOf(country);
                                spinner.setSelection(index);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(UserActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
                //Toast.makeText(UserActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(UserActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    // load image into imageview
                    pp_image.setImageBitmap(response.getBitmap());
                }
            }
        }, 2000, 1200);

        imageLoader.get("http://appwanderlust.com/appapi/" + covP, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "Image Load Error: " + error.getMessage());
                //Toast.makeText(UserActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            Intent parentIntent = NavUtils.getParentActivityIntent(this);
            parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(parentIntent);
            finish();
        } else if (item.getItemId() == R.id.save) {

            int selectedId = radioGroup.getCheckedRadioButtonId();
            genderBtn = (RadioButton) findViewById(selectedId);
            String gender = genderBtn.getText().toString();

            if (gender.matches("Male")) {
                gender = "1";
            } else {
                gender = "0";
            }

            String fname = fnameField.getText().toString();
            String lname = lnameField.getText().toString();
            String age = ageField.getText().toString();
            String about = aboutField.getText().toString();
            String travel = travelField.getText().toString();
            String looking = lookingField.getText().toString();
            country = spinner.getSelectedItem().toString();

            RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

            String tag_json_obj = "json_obj_req";

            String url = "http://appwanderlust.com/appapi/edit_user.php";

            final ProgressDialog pDialog = new ProgressDialog(UserActivity.this);
            pDialog.setMessage("Loading...");
            pDialog.show();

            Map<String, String> params = new HashMap<String, String>();
            params.put("first_name", fname);
            params.put("id", id);
            params.put("last_name", lname);
            params.put("age", age);
            params.put("country", country);
            params.put("about", about);
            params.put("looking_for", looking);
            params.put("travel", travel);
            params.put("gender", gender);

            if (pP != null) {
                String profilePicture = getStringImage(pP);
                params.put("profile_pic", profilePicture);
            }
            if (coverP != null) {
                String coverPicture = getStringImage(coverP);
                params.put("cover_pic", coverPicture);
            }

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
                                    new AlertDialog.Builder(UserActivity.this)
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
                                //Toast.makeText(UserActivity.this, "hi", Toast.LENGTH_SHORT).show();
                                String dirPath = getFilesDir().getAbsolutePath() + File.separator + id;
                                //Toast.makeText(UserActivity.this, dirPath, Toast.LENGTH_SHORT).show();
                                Log.v("Path", dirPath);
                                File projDir = new File(dirPath);
                                if (!projDir.exists()) {
                                    projDir.mkdirs();
                                }

                                String pp_name = "profile.jpg";
                                File profilePicture = new File(projDir, pp_name);
                                Log.v("Path", profilePicture.getAbsolutePath());

                                String cover_name = "cover.jpg";
                                File coverPicture = new File(projDir, cover_name);

                                if (pP != null) {
                                    if (profilePicture.exists()) {
                                        profilePicture.delete();
                                    }
                                    try {
                                        FileOutputStream out = new FileOutputStream(profilePicture);
                                        pP.compress(Bitmap.CompressFormat.JPEG, 70, out);
                                        out.flush();
                                        out.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }

                                if (coverP != null) {
                                    if (coverPicture.exists()) {
                                        coverPicture.delete();
                                    }
                                    try {
                                        FileOutputStream out = new FileOutputStream(coverPicture);
                                        coverP.compress(Bitmap.CompressFormat.JPEG, 70, out);
                                        out.flush();
                                        out.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }

                                finish();
                                startActivity(getIntent());
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyLog.d(TAG, "Error: " + error.getMessage());
                    pDialog.hide();
                    //Toast.makeText(UserActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
            };
            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            rq.add(jsonObjReq);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 0: {
                if (resultCode == RESULT_OK) {

                    Uri chosenImageUri = data.getData();

                    Bitmap mBitmap = null;

                    try {
                        mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), chosenImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String image = getStringImage(mBitmap);
                    long time = System.currentTimeMillis();
                    String name = Long.toString(time);

                    RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

                    String tag_json_obj = "json_obj_req";

                    String url = "http://appwanderlust.com/appapi/add_user_image.php";

                    final ProgressDialog pDialog = new ProgressDialog(UserActivity.this);
                    pDialog.setMessage("Loading...");
                    pDialog.show();

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("image", image);
                    params.put("user_id", id);
                    params.put("img_name", name);

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
                                            new AlertDialog.Builder(UserActivity.this)
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
                                        //Toast.makeText(UserActivity.this, "hi", Toast.LENGTH_SHORT).show();
                                        try {
                                            images.add(obj.getString("url"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        imagesAdapter.notifyDataSetChanged();
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //VolleyLog.d(TAG, "Error: " + error.getMessage());
                            pDialog.hide();
                            //Toast.makeText(UserActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                    };
                    jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    rq.add(jsonObjReq);

                }
                break;
            }
            case 1: {
                if (resultCode == RESULT_OK) {
                    Uri chosenImageUri = data.getData();
                    String oldUri = chosenImageUri.toString();
                    String newUri = oldUri + "_1";
                    Uri modifiedUri = Uri.parse(newUri);
                    Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
                    //Toast.makeText(UserActivity.this, oldUri + modifiedUri.toString(), Toast.LENGTH_SHORT).show();
                    Crop.of(chosenImageUri, destination).asSquare().start(UserActivity.this);
//                    Bitmap mBitmap = null;
//                    try {
//                        mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), chosenImageUri);
//                        pP = mBitmap;
//
//                        //pp_image.setImageBitmap(mBitmap);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
                break;
            }
            case 2: {
                if (resultCode == RESULT_OK) {
                    Uri chosenImageUri = data.getData();

                    Bitmap mBitmap = null;
                    try {
                        mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), chosenImageUri);
                        coverP = mBitmap;
                        cover.setImageBitmap(mBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case Crop.REQUEST_CROP: {
                if (resultCode == RESULT_OK) {
                    Uri image = Crop.getOutput(data);
                    Bitmap mBitmap = null;
                    try {
                        mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                        pP = mBitmap;
                        pp_image.setImageBitmap(mBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
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
}
