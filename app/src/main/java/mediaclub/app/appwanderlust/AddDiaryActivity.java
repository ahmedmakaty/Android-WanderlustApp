package mediaclub.app.appwanderlust;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;

public class AddDiaryActivity extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";

    Toolbar toolbar;
    TextView title, chooseButton;
    EditText diaryTitle, diaryDescription;
    Bitmap image;
    String id, postId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary);


        toolbar = (Toolbar) findViewById(R.id.app_bar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        chooseButton = (TextView) findViewById(R.id.choose_image);
        diaryTitle = (EditText) findViewById(R.id.title);
        diaryDescription = (EditText) findViewById(R.id.description);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        title.setText("Add diary");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        id = readFromPreferences(AddDiaryActivity.this, KEY_USER_ID, "id kda");

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 0);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_diary_menu, menu);
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
        } else if (item.getItemId() == R.id.save_diary) {

            long time1 = System.currentTimeMillis();
            String date = Long.toString(time1);

            String postTitle = diaryTitle.getText().toString();
            String postDescription = diaryDescription.getText().toString();

            RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

            String tag_json_obj = "json_obj_req";

            String url = "http://appwanderlust.com/appapi/add_post.php";

            final ProgressDialog pDialog = new ProgressDialog(AddDiaryActivity.this);
            pDialog.setMessage("Loading...");
            pDialog.show();

            Map<String, String> params = new HashMap<String, String>();
            params.put("title", postTitle);
            params.put("id", id);
            params.put("post", postDescription);
            params.put("date", date);

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
                                    new AlertDialog.Builder(AddDiaryActivity.this)
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
                                    postId = obj.getString("post_id");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //Toast.makeText(AddDiaryActivity.this, postId, Toast.LENGTH_SHORT).show();

                                if (image != null) {

                                    String imageString = getStringImage(image);
                                    long time = System.currentTimeMillis();
                                    String name = Long.toString(time);

                                    RequestQueue rq1 = Volley.newRequestQueue(getApplicationContext());

                                    String tag_json_obj1 = "json_obj_req";

                                    String url1 = "http://appwanderlust.com/appapi/add_post_image.php";

                                    final ProgressDialog pDialog1 = new ProgressDialog(AddDiaryActivity.this);
                                    pDialog1.setMessage("Uploading image ...");
                                    pDialog1.show();

                                    Map<String, String> params1 = new HashMap<String, String>();
                                    params1.put("image", imageString);
                                    params1.put("user_id", id);
                                    params1.put("post_id", postId);
                                    params1.put("img_name", name);

                                    CustomRequest jsonObjReq1 = new CustomRequest(Request.Method.POST,
                                            url1, params1,
                                            new Response.Listener<JSONObject>() {

                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    //Log.d(TAG, response.toString());
                                                    pDialog1.hide();
                                                    JSONObject obj = response;
                                                    boolean error = false;
                                                    try {
                                                        error = Boolean.valueOf(obj.getString("ecode"));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    if (error) {
                                                        try {
                                                            new AlertDialog.Builder(AddDiaryActivity.this)
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
                                                        Toast.makeText(AddDiaryActivity.this, "image uploaded", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                }
                                            }, new Response.ErrorListener() {

                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            //VolleyLog.d(TAG, "Error: " + error.getMessage());
                                            pDialog1.hide();
                                            //Toast.makeText(AddDiaryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }) {
                                    };

                                    rq1.add(jsonObjReq1);
                                } else {
                                    finish();
                                }

                                //finish();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyLog.d(TAG, "Error: " + error.getMessage());
                    pDialog.hide();
                    //Toast.makeText(AddDiaryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
            };

            rq.add(jsonObjReq);


        }
        return super.onOptionsItemSelected(item);
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

        if (resultCode == RESULT_OK) {
            Uri chosenImageUri = data.getData();

            Bitmap mBitmap = null;
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), chosenImageUri);
                image = mBitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
