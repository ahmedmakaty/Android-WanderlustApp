package mediaclub.app.appwanderlust;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.DataModels.ChatItem;
import mediaclub.app.appwanderlust.RealmModels.ChatBuddy;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class Chat extends Fragment {

    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_NOTIFICATIONS = "notifications";
    public static final String KEY_USER_ID = "user_id";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    Realm realm;

    RecyclerView chatMessages;
    View view;
    ChatFragmentAdapter adapter;
    List<ChatItem> messages = new ArrayList<ChatItem>();
    RelativeLayout noMessage;
    String id;

    public Chat() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fragment2, container, false);

        realm = Realm.getDefaultInstance();

        chatMessages = (RecyclerView) view.findViewById(R.id.recyclerview);
        noMessage = (RelativeLayout) view.findViewById(R.id.noMessage);

        adapter = new ChatFragmentAdapter(getContext(), messages);
        chatMessages.setAdapter(adapter);

        chatMessages.setLayoutManager(new LinearLayoutManager(getActivity()));

        id = readFromPreferences(getContext(), KEY_USER_ID, "id kda");

        saveToPreferences(getContext(), KEY_NOTIFICATIONS, "");

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

                    getHistory();

                    //Toast.makeText(getApplicationContext(), from + ": " + message + " " + other, Toast.LENGTH_LONG).show();
                    //saveToPreferences(MainActivity.this,KEY_NOTIFICATIONS,null);
                }
            }
        };

        return view;
    }

    private void getHistory() {

        RequestQueue rq = Volley.newRequestQueue(getContext().getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_chat_users.php";

//        final ProgressDialog pDialog = new ProgressDialog(getContext());
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
                            messages.clear();
                            try {
                                JSONArray around = obj.getJSONArray("data");
                                for (int i = 0; i < around.length(); i++) {
                                    JSONObject ob = around.getJSONObject(i);
                                    JSONObject user = ob.getJSONObject("user");
                                    JSONObject lMessage = ob.getJSONObject("last_message");
                                    String counter = ob.getString("counter");
                                    String chat_id = user.getString("id");
                                    String nickname = user.getString("nickname");
                                    String image = user.getString("profile_pic");
                                    String text_msg = lMessage.getString("text");
                                    String date = lMessage.getString("time_sent");

                                    messages.add(new ChatItem(chat_id, nickname, image, text_msg, counter, date));

                                    //Toast.makeText(getContext(), user.toString() + " " + lMessage.toString() + " " + counter, Toast.LENGTH_SHORT).show();
                                    //users.add(new User(name,id,image,nationality,distance,nickname));
                                }
                                adapter.notifyDataSetChanged();
                                if (messages.size() > 0) {
                                    noMessage.setVisibility(View.GONE);
                                    chatMessages.setVisibility(View.VISIBLE);
                                }

                                for (ChatItem c : messages) {

                                    ChatBuddy buddy = new ChatBuddy();
                                    buddy.setKey(id + "_" + c.getId());
                                    buddy.setId(id);
                                    buddy.setOtherId(c.getId());
                                    buddy.setNickname(c.getNickname());
                                    buddy.setLast_message(c.getLast_message());
                                    buddy.setCounter(c.getCounter());
                                    buddy.setDate(c.getDate());
                                    //Toast.makeText(getContext(), buddy.getKey(), Toast.LENGTH_SHORT).show();

                                    realm.beginTransaction();
                                    realm.copyToRealmOrUpdate(buddy);
                                    realm.commitTransaction();
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
                //Toast.makeText(getContext(), error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.add(jsonObjReq);
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

        id = readFromPreferences(getContext(), KEY_USER_ID, "id kda");

        getLocalHistory();

        getHistory();

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
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void getLocalHistory() {

        RealmResults<ChatBuddy> buddies = realm.where(ChatBuddy.class).equalTo("id", id).findAll();

        for (ChatBuddy c : buddies) {
            messages.add(new ChatItem(c.getId(), c.getNickname(), "", c.getLast_message(), c.getCounter(), c.getDate()));
        }
        adapter.notifyDataSetChanged();
        if (messages.size() > 0) {
            noMessage.setVisibility(View.GONE);
            chatMessages.setVisibility(View.VISIBLE);
        }
    }

    public boolean hasInternetAccess(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                //Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            //Log.d(TAG, "No network available!");
        }
        return false;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
