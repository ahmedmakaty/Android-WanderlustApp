package mediaclub.app.appwanderlust;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.PersistableBundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import mediaclub.app.appwanderlust.DataModels.Post;
import mediaclub.app.appwanderlust.app.Config;
import mediaclub.app.appwanderlust.gcm.NotificationUtils;

public class ViewUserPost extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private LinearLayout pager_indicator;
    private int dotsCount;
    private ImageView[] dots;

    Toolbar toolbar;
    TextView title;
    Post post;
    List<String> images = new ArrayList<String>();
    ViewPager imagesPager;
    SliderAdapter adapter;
    TextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_post);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        description = (TextView) findViewById(R.id.post);

        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            post = (Post) extras.getSerializable("POST");
        } else {
            post = (Post) savedInstanceState.getSerializable("POST");
        }

        imagesPager = (ViewPager) findViewById(R.id.pager);
        pager_indicator = (LinearLayout) findViewById(R.id.viewPagerCountDots);

        imagesPager.setOffscreenPageLimit(0);

        getPostImages();

        getSupportActionBar().setTitle("");

        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT.ttf");
        Typeface helveticaBold = Typeface.createFromAsset(getAssets(), "fonts/Helvetica LT Bold.ttf");
        title.setTypeface(helveticaBold);

        description.setTypeface(helvetica);

        title.setText(post.getTitle());
        description.setText(post.getDescription());

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    private void setSlider() {

        dotsCount = adapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(7, 0, 7, 0);

            pager_indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
    }

    private void getPostImages() {

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        String tag_json_obj = "json_obj_req";

        String url = "http://appwanderlust.com/appapi/get_post_images.php";

        final ProgressDialog pDialog = new ProgressDialog(ViewUserPost.this);
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
                                new AlertDialog.Builder(ViewUserPost.this)
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
                                    images.add(ob.getString("picture"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (images.size() > 0) {
                                //Toast.makeText(ViewUserPost.this, images.size() + images.toString(), Toast.LENGTH_SHORT).show();
                                adapter = new SliderAdapter(getSupportFragmentManager(), images);
                                imagesPager.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                imagesPager.destroyDrawingCache();

                                setSlider();

                                imagesPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                                    @Override
                                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                    }

                                    @Override
                                    public void onPageSelected(int position) {
                                        for (int i = 0; i < dotsCount; i++) {
                                            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
                                        }

                                        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));

//                if (position + 1 == dotsCount) {
//                    btnNext.setVisibility(View.GONE);
//                    btnFinish.setVisibility(View.VISIBLE);
//                } else {
//                    btnNext.setVisibility(View.VISIBLE);
//                    btnFinish.setVisibility(View.GONE);
//                }
                                    }

                                    @Override
                                    public void onPageScrollStateChanged(int state) {

                                    }
                                });
                            }

                            //Toast.makeText(ViewUserPost.this, obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.hide();
                //Toast.makeText(ViewUserPost.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
            }
        });

        rq.add(jsonObjReq);
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

        outState.putSerializable("POST", post);
    }
}
