package mediaclub.app.appwanderlust.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.R;

/**
 * Created by Bloom on 28/1/2016.
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.GroupViewHolder> {

    List<String> images = new ArrayList<String>();
    LayoutInflater layoutInflater;
    Context context;
    boolean selected;
    int index;

    public GridAdapter(Context context, List<String> images) {
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

        String url = images.get(position);
        //holder.image.setImageResource(R.drawable.ic_pp);

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
        imageLoader.get("http://appwanderlust.com/appapi/" + url , new ImageLoader.ImageListener() {

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
    }

    @Override
    public int getItemCount() {
        return images.size();
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

