package mediaclub.app.appwanderlust;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.DataModels.IndividualMessage;
import mediaclub.app.appwanderlust.DataModels.Post;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class Diary extends Fragment {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";

    View view;
    RecyclerView list;
    DiaryAdapter adapter;
    List<IndividualMessage> messages = new ArrayList<IndividualMessage>();
    List<Post> diary = new ArrayList<Post>();
    TextView noDiary;
    LinearLayout addDiary;
    String id;
    TextView btnText;


    public Diary() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_fragment4, container, false);

        list = (RecyclerView) view.findViewById(R.id.recyclerview);
        noDiary = (TextView) view.findViewById(R.id.noDiary);
        addDiary = (LinearLayout) view.findViewById(R.id.add_diary);
        btnText = (TextView) view.findViewById(R.id.add_text);

        Typeface helvetica = Typeface.createFromAsset(getContext().getAssets(), "fonts/Helvetica LT.ttf");
        Typeface helveticaBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Helvetica LT Bold.ttf");

        btnText.setTypeface(helvetica);

        id = readFromPreferences(getContext(), KEY_USER_ID, "id kda");

        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        list.addItemDecoration(new SpaceItemDecoration(20));

        adapter = new DiaryAdapter(getContext(), diary);
        list.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView

                final int pos = viewHolder.getAdapterPosition();

                new AlertDialog.Builder(getContext())
                        .setTitle("Warning")
                        .setMessage("This will delete the post with all the attached images, Are you sure you wanna delete this item?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete


                                Post post = adapter.getItem(pos);
                                adapter.removeItem(pos);
                                adapter.notifyDataSetChanged();

                                RequestQueue rq = Volley.newRequestQueue(getContext().getApplicationContext());

                                String tag_json_obj = "json_obj_req";

                                String url = "http://appwanderlust.com/appapi/delete_post.php";

                                final ProgressDialog pDialog = new ProgressDialog(getContext());
                                pDialog.setMessage("Loading...");
                                pDialog.show();

                                Map<String, String> params = new HashMap<String, String>();
                                params.put("id", post.getId());

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
                                                    //Toast.makeText(getContext(), "item deleted from database", Toast.LENGTH_SHORT).show();
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
                        })
                        .setNegativeButton("Undo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .show();

                //Toast.makeText(getContext(), pos + "", Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(list);

        addDiary.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AddDiaryActivity.class);
                getContext().startActivity(i);
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

    private void getPosts() {


        RequestQueue rq = Volley.newRequestQueue(getContext().getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_diary.php";

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
                            diary.clear();
                            try {
                                JSONArray posts = obj.getJSONArray("posts");
                                for(int i =0;i<posts.length();i++){
                                    JSONObject ob = posts.getJSONObject(i);
                                    String id = ob.getString("post_id");
                                    String title = ob.getString("title");
                                    String description = ob.getString("post");
                                    String date = ob.getString("date");
                                    String image = ob.getString("pic_url");
                                    diary.add(new Post(id,date,title,description,image));
                                }
                                adapter = new DiaryAdapter(getContext(), diary);
                                list.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                if(diary.size() > 0){
                                    noDiary.setVisibility(View.GONE);
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
                pDialog.hide();
                //Toast.makeText(getContext(), error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });

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
        getPosts();

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
