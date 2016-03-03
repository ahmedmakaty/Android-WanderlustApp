package mediaclub.app.appwanderlust;

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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mediaclub.app.appwanderlust.Adapters.EditPostImagesAdapter;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.DataModels.Post;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;

public class EditPost extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";

    Toolbar toolbar;
    TextView title, imagesTitle;
    EditText postTitle, postDescription;
    Post post;
    RecyclerView imagesView;
    EditPostImagesAdapter adapter;
    List<String> images = new ArrayList<String>();
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        postTitle = (EditText) findViewById(R.id.title);
        postDescription = (EditText) findViewById(R.id.description);
        imagesView = (RecyclerView) findViewById(R.id.recyclerview);
        imagesTitle = (TextView) findViewById(R.id.images_title);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        title.setText("Edit");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT.ttf");
        Typeface helveticaBold = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT Bold.ttf");
        title.setTypeface(helveticaBold);

        postTitle.setTypeface(helvetica);
        postDescription.setTypeface(helvetica);
        imagesTitle.setTypeface(helveticaBold);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            post = (Post) extras.getSerializable("POST");
        } else {
            post = (Post) savedInstanceState.getSerializable("POST");
        }

        postTitle.setText(post.getTitle());

        postDescription.setText(post.getDescription());

        imagesView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new EditPostImagesAdapter(this, images);
        imagesView.setAdapter(adapter);

        id = readFromPreferences(EditPost.this, KEY_USER_ID, "id kda");

        getPostImages();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    String token = intent.getStringExtra("token");

                    ///Toast.makeText(getApplicationContext(), "GCM registration token: " + token, Toast.LENGTH_LONG).show();

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

    private void getPostImages() {

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_post_images.php";

        final ProgressDialog pDialog = new ProgressDialog(EditPost.this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("post_id", post.getId());


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
                                new AlertDialog.Builder(EditPost.this)
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
                                    images.add(ob.getString("picture"));
                                }
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //Toast.makeText(EditPost.this, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
                //Toast.makeText(EditPost.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });

        rq.add(jsonObjReq);
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
//            Intent parentIntent = NavUtils.getParentActivityIntent(this);
//            parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            startActivity(parentIntent);
            finish();
        } else if (item.getItemId() == R.id.save) {

            String title = postTitle.getText().toString();
            String description = postDescription.getText().toString();

            RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

            String tag_json_obj = "json_obj_req";

            String url = "http://appwanderlust.com/appapi/edit_post.php";

            final ProgressDialog pDialog = new ProgressDialog(EditPost.this);
            pDialog.setMessage("Loading...");
            pDialog.show();

            Map<String, String> params = new HashMap<String, String>();
            params.put("id", post.getId());
            params.put("title", title);
            params.put("description", description);

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
                                    new AlertDialog.Builder(EditPost.this)
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
                                //Toast.makeText(EditPost.this, "edited", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyLog.d(TAG, "Error: " + error.getMessage());
                    pDialog.hide();
                    //Toast.makeText(EditPost.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
            };
            rq.add(jsonObjReq);
        }
        return super.onOptionsItemSelected(item);
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

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
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

                    String url = "http://appwanderlust.com/appapi/add_post_image.php";

                    final ProgressDialog pDialog = new ProgressDialog(EditPost.this);
                    pDialog.setMessage("Loading...");
                    pDialog.show();

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("image", image);
                    params.put("user_id", id);
                    params.put("post_id", post.getId());
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
                                            new AlertDialog.Builder(EditPost.this)
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
                                        //Toast.makeText(EditPost.this, "uploaded", Toast.LENGTH_SHORT).show();
                                        try {
                                            images.add(obj.getString("url"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //VolleyLog.d(TAG, "Error: " + error.getMessage());
                            pDialog.hide();
                            //Toast.makeText(EditPost.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                    };

                    jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    rq.add(jsonObjReq);

                }
                break;
            }
        }
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
