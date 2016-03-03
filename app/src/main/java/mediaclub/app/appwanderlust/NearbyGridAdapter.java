package mediaclub.app.appwanderlust;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mikhaellopez.circularimageview.CircularImageView;


import java.util.ArrayList;
import java.util.List;

import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.DataModels.User;

/**
 * Created by Bloom on 28/1/2016.
 */
public class NearbyGridAdapter extends RecyclerView.Adapter<NearbyGridAdapter.GroupViewHolder> {

    List<User> users = new ArrayList<User>();
    LayoutInflater layoutInflater;
    Context context;

    public NearbyGridAdapter(Context context, List<User> users) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.users = users;
    }


    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View row = layoutInflater.inflate(R.layout.nearby_grid_item, parent, false);
        GroupViewHolder holder = new GroupViewHolder(row);
        return holder;
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, final int position) {

        //String url = images.get(position);
        //holder.image.setImageResource(R.drawable.ic_pp);

        final User user = users.get(position);

        if(!user.getName().matches("")&& !user.getName().matches("null")) {
            holder.name.setText(user.getName());
        }
        holder.nationality.setText(user.getNationality());

        String distance = user.getDistance();
        float dist = Float.parseFloat(distance);
        holder.distance.setText((int)dist + " Km");

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
        imageLoader.get("http://appwanderlust.com/appapi/" + user.getImage(), new ImageLoader.ImageListener() {

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
        }, 2000, 1200);

        holder.itemView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DisplayUserActivity.class);
                i.putExtra("ID",user.getId());
                i.putExtra("NICKNAME",user.getNickname());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {

        private CircularImageView image;
        TextView name;
        TextView nationality;
        TextView distance;

        public GroupViewHolder(View itemView) {
            super(itemView);

            image = (CircularImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            nationality = (TextView) itemView.findViewById(R.id.country);
            distance = (TextView) itemView.findViewById(R.id.distance);

            Typeface helveticaBold = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica LT Bold.ttf");
            Typeface helveticaOblique = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica LT Oblique.ttf");
            Typeface helvetica = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica LT.ttf");

            Typeface myFont = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
            name.setTypeface(helveticaBold);
            nationality.setTypeface(helvetica);
            distance.setTypeface(helvetica);

        }
    }
}

