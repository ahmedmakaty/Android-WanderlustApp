package mediaclub.app.appwanderlust.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.Controller.CustomRequest;
import mediaclub.app.appwanderlust.R;

/**
 * Created by Bloom on 28/1/2016.
 */
public class EditGridAdapter extends RecyclerView.Adapter<EditGridAdapter.GroupViewHolder> {

    List<String> images = new ArrayList<String>();
    LayoutInflater layoutInflater;
    Context context;
    boolean selected;
    int index;

    public EditGridAdapter(Context context, List<String> images) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.images = images;
        //Toast.makeText(context, images.size()+"", Toast.LENGTH_SHORT).show();
    }


    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View row = layoutInflater.inflate(R.layout.grid_item, parent, false);
        GroupViewHolder holder = new GroupViewHolder(row);
        return holder;
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, final int position) {
        //String url = images.get(position);
        //holder.image.setImageResource(R.drawable.ic_pp);
        if(position == 0){
            holder.image.setImageResource(R.drawable.ic_add_photo);

            holder.image.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {

                    //Toast.makeText(context, "Add photo", Toast.LENGTH_SHORT).show();

                    Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    photoPickerIntent.setType("image/*");
                    ((Activity)context).startActivityForResult(photoPickerIntent, 0);
                }
            });
        }else{
            final String image = images.get(position - 1);

            ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
            imageLoader.get("http://appwanderlust.com/appapi/" + image , new ImageLoader.ImageListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    //Log.e(TAG, "Image Load Error: " + error.getMessage());
                    //Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                    if (response.getBitmap() != null) {
                        // load image into imageview
                        holder.image.setImageBitmap(response.getBitmap());
                    }
                }
            },2000,1200);

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View v) {

                    images.remove(position - 1);

                    RequestQueue rq = Volley.newRequestQueue(context.getApplicationContext());

                    String tag_json_obj = "json_obj_req";

                    String url = "http://appwanderlust.com/appapi/delete_user_image.php";

                    final ProgressDialog pDialog = new ProgressDialog(context);
                    pDialog.setMessage("Loading...");
                    pDialog.show();

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("url", image);

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
                                            new AlertDialog.Builder(context)
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
                                        //Toast.makeText(context, "deleted", Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //VolleyLog.d(TAG, "Error: " + error.getMessage());
                            pDialog.hide();
                            //Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                    };

                    rq.add(jsonObjReq);

                    return false;
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return images.size()+1;
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        TextView name;

        public GroupViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.image);


        }
    }
}

