package mediaclub.app.appwanderlust.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.Collections;
import java.util.List;

import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.DataModels.Post;
import mediaclub.app.appwanderlust.R;
import mediaclub.app.appwanderlust.ViewUserPost;

/**
 * Created by Bloom on 15/1/2016.
 */
public class UserDiaryAdapter extends RecyclerView.Adapter<UserDiaryAdapter.GroupViewHolder> {

    List<Post> diary = Collections.emptyList();
    LayoutInflater layoutInflater;
    Context context;

    public UserDiaryAdapter(Context context, List<Post> diary) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.diary = diary;
    }


    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View row = layoutInflater.inflate(R.layout.diary_item, parent, false);
        GroupViewHolder holder = new GroupViewHolder(row);

        return holder;
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, final int position) {

        final Post post = diary.get(position);
//        holder.itemView.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(context, ChatActivity.class);
//                context.startActivity(i);
//            }
//        });

        holder.title.setText(post.getTitle());
        holder.description.setText(post.getDescription());

        if(post.getImageUrl() != null){

            ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
            imageLoader.get("http://appwanderlust.com/appapi/" + post.getImageUrl() , new ImageLoader.ImageListener() {

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
            });
        }

        String date = post.getDate();
        //Toast.makeText(context, Long.parseLong(date) + "", Toast.LENGTH_SHORT).show();

        holder.day.setText((String) android.text.format.DateFormat.format("dd", Long.parseLong(date)));
        holder.month.setText((String) android.text.format.DateFormat.format("MMM", Long.parseLong(date)));
        holder.year.setText((String) android.text.format.DateFormat.format("yyyy", Long.parseLong(date)));

        holder.itemView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ViewUserPost.class);
                i.putExtra("POST",post);
                context.startActivity(i);
            }
        });

//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View v) {
//
//                //Toast.makeText(v.getContext(), "OnLongClick Version :" + position, Toast.LENGTH_SHORT).show();
//
//                holder.overlay.setVisibility(View.VISIBLE);
//
//                Runnable mRunnable;
//                Handler mHandler = new Handler();
//
//                mRunnable = new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//                        holder.overlay.setVisibility(View.GONE);
//                    }
//                };
//
//                mHandler.postDelayed(mRunnable, 5 * 1000);
//
//                return true;
//
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return diary.size();
    }

    public Post getItem(int position){
        return diary.get(position);
    }

    public void removeItem(int position){
        diary.remove(position);
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {


        private ImageView image;
        private TextView day;
        private TextView month;
        private TextView year;
        private TextView title;
        private TextView description;


        public GroupViewHolder(View itemView) {
            super(itemView);

            day = (TextView) itemView.findViewById(R.id.day);
            month = (TextView) itemView.findViewById(R.id.month);
            year = (TextView) itemView.findViewById(R.id.year);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);

            Typeface myFont = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
            Typeface helvetica = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica LT.ttf");
            Typeface helveticaBold = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica LT Bold.ttf");
            title.setTypeface(helveticaBold);
            day.setTypeface(helvetica);
            month.setTypeface(helvetica);
            year.setTypeface(helvetica);
            description.setTypeface(helvetica);


            image = (ImageView) itemView.findViewById(R.id.image);

        }
    }
}
