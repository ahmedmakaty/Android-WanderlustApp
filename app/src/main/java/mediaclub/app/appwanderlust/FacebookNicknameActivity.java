package mediaclub.app.appwanderlust;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.RealmModels.lUser;

public class FacebookNicknameActivity extends AppCompatActivity {

    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_LOGGED = "logged";

    TextView submit;
    EditText nickname;
    String fname, lname, gender, email, nick, id;
    String gend = "1";
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_nickname);

        realm = Realm.getDefaultInstance();

        submit = (TextView) findViewById(R.id.submit);
        nickname = (EditText) findViewById(R.id.nickname);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            fname = extras.getString("FNAME");
            lname = extras.getString("LNAME");
            gender = extras.getString("GENDER");
            email = extras.getString("EMAIL");
        } else {
            fname = savedInstanceState.getString("LNAME");
            lname = savedInstanceState.getString("LNAME");
            gender = savedInstanceState.getString("GENDER");
            email = savedInstanceState.getString("EMAIL");
        }

        //Toast.makeText(FacebookNicknameActivity.this, fname + " " + lname + " " + email + " " + gender, Toast.LENGTH_SHORT).show();

        if (gender.matches("male")) {
            gend = "1";
        } else {
            gend = "0";
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nick = nickname.getText().toString();

                if (!nick.matches("")) {

                    RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

                    String tag_json_obj = "json_obj_req";

                    String url = "http://appwanderlust.com/appapi/create_fb_user.php";

//        final ProgressDialog pDialog = new ProgressDialog(getContext());
//        pDialog.setMessage("Loading...");
//        pDialog.show();

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("email", email);
                    params.put("fname", fname);
                    params.put("lname", lname);
                    params.put("gender", gend);
                    params.put("nickname", nick);

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
                                            new AlertDialog.Builder(FacebookNicknameActivity.this)
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
                                            lUser rUser = new lUser();
                                            id = obj.getString("id");
                                            rUser.setId(id);
                                            rUser.setNickname(obj.getString("nickname"));
                                            rUser.setEmail(obj.getString("email"));

                                            realm.beginTransaction();
                                            realm.copyToRealm(rUser);
                                            realm.commitTransaction();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        saveToPreferences(FacebookNicknameActivity.this, KEY_USER_ID, id);
                                        saveToPreferences(FacebookNicknameActivity.this, KEY_LOGGED, "true");

                                        Intent i = new Intent(FacebookNicknameActivity.this, MainActivity.class);
                                        startActivity(i);
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //VolleyLog.d(TAG, "Error: " + error.getMessage());
                            //pDialog.hide();
                            //Toast.makeText(FacebookNicknameActivity.this, error.getMessage() + "hi", Toast.LENGTH_SHORT).show();
                        }
                    });
                    jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 7, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    rq.add(jsonObjReq);
                }
            }
        });
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
}
