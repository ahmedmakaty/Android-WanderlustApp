package mediaclub.app.appwanderlust.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Collections;
import java.util.List;

import mediaclub.app.appwanderlust.ChatActivity;
import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.DataModels.ChatItem;
import mediaclub.app.appwanderlust.R;

/**
 * Created by Bloom on 15/1/2016.
 */
public class ChatFragmentAdapter extends RecyclerView.Adapter<ChatFragmentAdapter.GroupViewHolder> {

    List<ChatItem> messages = Collections.emptyList();
    LayoutInflater layoutInflater;
    Context context;

    public ChatFragmentAdapter(Context context, List<ChatItem> messages) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.messages = messages;
    }


    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View row = layoutInflater.inflate(R.layout.messages_item, parent, false);
        GroupViewHolder holder = new GroupViewHolder(row);

        return holder;
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, final int position) {
        final ChatItem individual = messages.get(position);
        //holder.name.setText(individual.getName());
//        holder.status.setText("status");
//        holder.date.setText("status");
//        holder.message.setText("status");
//        holder.image.setImageResource(R.drawable.oval);

        holder.name.setText(individual.getNickname());
        holder.message.setText(individual.getLast_message());

        String counter = individual.getCounter();
        int cnt = Integer.parseInt(counter);
        if(cnt >0){
            holder.counter.setVisibility(View.VISIBLE);
            holder.counter.setText(individual.getCounter());
        }else{
            holder.counter.setVisibility(View.GONE);
        }

        Long time = Long.parseLong(individual.getDate());
        holder.date.setText(DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS,
                DateUtils.DAY_IN_MILLIS, 0));

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
        imageLoader.get("http://appwanderlust.com/appapi/" + individual.getImage(), new ImageLoader.ImageListener() {

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
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra("ID",individual.getId());
                i.putExtra("NICKNAME",individual.getNickname());
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {

        private CircularImageView image;
        private TextView name;
        private TextView date;
        private TextView message;
        private TextView counter;

        public GroupViewHolder(View itemView) {
            super(itemView);

            Typeface helveticaBold = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica LT Bold.ttf");
            Typeface helveticaOblique = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica LT Oblique.ttf");
            Typeface helvetica = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica LT.ttf");

//            image = (CircularImageView) itemView.findViewById(R.id.image);
            image = (CircularImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.my_name);
            message = (TextView) itemView.findViewById(R.id.message);
            date = (TextView) itemView.findViewById(R.id.date);
            counter = (TextView) itemView.findViewById(R.id.counter);

            Typeface myFont = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
            name.setTypeface(helveticaBold);
            message.setTypeface(helvetica);
            date.setTypeface(helvetica);
            counter.setTypeface(helvetica);
            //date = (TextView) itemView.findViewById(R.id.date);
//            message = (TextView) itemView.findViewById(R.id.message);
//            status = (TextView) itemView.findViewById(R.id.status);
        }
    }
}
