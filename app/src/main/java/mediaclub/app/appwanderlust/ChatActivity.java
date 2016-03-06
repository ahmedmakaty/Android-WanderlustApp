package mediaclub.app.appwanderlust;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mediaclub.app.appwanderlust.Adapters.MessageAdapter;
import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.DataModels.Message;
import mediaclub.app.appwanderlust.app.Config;

public class ChatActivity extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";

    Toolbar toolbar;
    TextView title;
    ListView list;
    List<Message> messages = new ArrayList<Message>();
    RelativeLayout noMessage;
    MessageAdapter adapter;
    ImageButton send;
    EditText message;
    String id, otherId, otherNickname;
    Typeface helveticaBold, helveticaOblique, helvetica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        toolbar = (Toolbar) findViewById(R.id.app_bar);
        title = (TextView) findViewById(R.id.toolbar_title);
        list = (ListView) findViewById(R.id.message_list);
        noMessage = (RelativeLayout) findViewById(R.id.noMessage);
        send = (ImageButton) findViewById(R.id.send_button);
        message = (EditText) findViewById(R.id.send);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        helveticaBold = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT Bold.ttf");
        helveticaOblique = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT Oblique.ttf");
        helvetica = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT.ttf");

        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        title.setTypeface(helveticaBold);

        message.setTypeface(helvetica);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noMessage.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);

        //Toast.makeText(ChatActivity.this, "created", Toast.LENGTH_SHORT).show();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            otherId = extras.getString("ID");
            otherNickname = extras.getString("NICKNAME");
        } else {
            //Log.v("LoginActivity", savedInstanceState.toString());
            //Toast.makeText(ChatActivity.this, savedInstanceState.toString(), Toast.LENGTH_SHORT).show();
            otherId = savedInstanceState.getString("ID");
            otherNickname = savedInstanceState.getString("NICKNAME");
            //Toast.makeText(ChatActivity.this, otherId + " " + otherNickname, Toast.LENGTH_SHORT).show();
        }

        title.setText(otherNickname);

        //Toast.makeText(ChatActivity.this, otherId, Toast.LENGTH_SHORT).show();

        //Toast.makeText(ChatActivity.this, otherId + " " + id, Toast.LENGTH_SHORT).show();


        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                adapter = new MessageAdapter(ChatActivity.this, messages, ChatActivity.this);
                list.setAdapter(adapter);
                String msg = message.getText().toString();

                if (!msg.matches("")) {

                    long time = System.currentTimeMillis();
                    String date = Long.toString(time);


                    message.setText(null);
                    messages.add(new Message(msg, date, true));
                    adapter.notifyDataSetChanged();
                    //playSendSound();

                    if (messages.size() > 0) {
                        noMessage.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                    }

                    RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

                    String url = "http://appwanderlust.com/appapi/save_message.php";

                    final ProgressDialog pDialog = new ProgressDialog(ChatActivity.this);


                    Map<String, String> params = new HashMap<String, String>();
                    params.put("sender_id", id);
                    params.put("receiver_id", otherId);
                    params.put("message", msg);
                    params.put("time", date);


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
                                            new AlertDialog.Builder(ChatActivity.this)
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

                                        //Toast.makeText(ChatActivity.this, obj.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //VolleyLog.d(TAG, "Error: " + error.getMessage());
                            //Toast.makeText(ChatActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
                        }
                    });
                    rq.add(jsonObjReq);
                }
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
                    String message = intent.getStringExtra("message");
                    String from = intent.getStringExtra("from");
                    String time = intent.getStringExtra("time");
                    String other = intent.getStringExtra("otherId");

                    if (other.matches(otherId)) {
                        playReceiveSound();
                        messages.add(new Message(message, time, false));
                        adapter.notifyDataSetChanged();
                        clearUnread();
                    }

                    //Toast.makeText(getApplicationContext(), from + ": " + message + " " + other, Toast.LENGTH_LONG).show();
                    //saveToPreferences(MainActivity.this,KEY_NOTIFICATIONS,null);
                }
            }
        };

    }

    private void clearUnread() {
        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/clear_unread_messages.php";

//        final ProgressDialog pDialog = new ProgressDialog(getContext());
//        pDialog.setMessage("Loading...");
//        pDialog.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);
        params.put("otherId", otherId);

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
                                new AlertDialog.Builder(ChatActivity.this)
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

                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                //pDialog.hide();
                //Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.add(jsonObjReq);
    }

    private void getHistory() {

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_chat.php";

//        final ProgressDialog pDialog = new ProgressDialog(getContext());
//        pDialog.setMessage("Loading...");
//        pDialog.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", id);
        params.put("person_id", otherId);

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
                                new AlertDialog.Builder(ChatActivity.this)
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
                            Log.v("LoginActivity", obj.toString());
                            messages.clear();
                            try {
                                JSONArray around = obj.getJSONArray("history");
                                for (int i = 0; i < around.length(); i++) {
                                    JSONObject ob = around.getJSONObject(i);
                                    String text = ob.getString("message");
                                    String time = ob.getString("time");
                                    String typeId = ob.getString("sender");
                                    boolean type = false;
                                    if (typeId.matches(id)) {
                                        type = true;
                                    }
                                    messages.add(new Message(text, time, type));

                                    //Toast.makeText(getContext(), user.toString() + " " + lMessage.toString() + " " + counter, Toast.LENGTH_SHORT).show();
                                    //users.add(new User(name,id,image,nationality,distance,nickname));
                                }
                                adapter = new MessageAdapter(ChatActivity.this, messages, ChatActivity.this);
                                list.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                if (messages.size() > 0) {
                                    noMessage.setVisibility(View.GONE);
                                    list.setVisibility(View.VISIBLE);
                                }
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
                //Toast.makeText(ChatActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.add(jsonObjReq);
    }

    public void playSendSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + AppController.getInstance().getApplicationContext().getPackageName() + "/raw/send_1");
            Ringtone r = RingtoneManager.getRingtone(AppController.getInstance().getApplicationContext(), alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playReceiveSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + AppController.getInstance().getApplicationContext().getPackageName() + "/raw/receive_1");
            Ringtone r = RingtoneManager.getRingtone(AppController.getInstance().getApplicationContext(), alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
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


        //Toast.makeText(ChatActivity.this, otherId + " " + otherNickname, Toast.LENGTH_SHORT).show();
        id = readFromPreferences(ChatActivity.this, KEY_USER_ID, "id kda");

        clearUnread();
        getHistory();

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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Toast.makeText(ChatActivity.this, "save state", Toast.LENGTH_SHORT).show();

        outState.putString("ID", otherId);
        outState.putString("NICKNAME", otherNickname);

    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//
//        Toast.makeText(ChatActivity.this, "restore state", Toast.LENGTH_SHORT).show();
//        otherId = savedInstanceState.getString("ID");
//        otherNickname = savedInstanceState.getString("NICKNAME");
//        Toast.makeText(ChatActivity.this, otherId + " " + otherNickname, Toast.LENGTH_SHORT).show();
//    }
}
