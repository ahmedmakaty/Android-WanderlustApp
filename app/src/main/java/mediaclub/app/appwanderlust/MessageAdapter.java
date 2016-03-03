package mediaclub.app.appwanderlust;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mediaclub.app.appwanderlust.DataModels.Message;

/**
 * Created by Bloom on 24/2/2016.
 */
public class MessageAdapter extends BaseAdapter {

    List<Message> messages = new ArrayList<Message>();
    Context context;
    Activity activity;

    public MessageAdapter(Context context, List<Message> messages,Activity activity){
        this.context = context;
        this.messages = messages;
        this.activity = activity;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return messages.size();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(final int pos, View v, ViewGroup arg2) {

        final Message m = getItem(pos);
        Typeface helvetica = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica LT.ttf");

        if (m.isType()) {
            v = activity.getLayoutInflater().inflate(R.layout.message_send_item, null);
            TextView message = (TextView) v.findViewById(R.id.message);
            TextView date = (TextView) v.findViewById(R.id.date);

            message.setTypeface(helvetica);
            date.setTypeface(helvetica);
            String dat = m.getDate();
            Long da = Long.parseLong(dat);
            message.setText(m.getMessage());
            date.setText(DateUtils.getRelativeDateTimeString(activity, da, DateUtils.SECOND_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS, 0));
        } else {
            v = activity.getLayoutInflater().inflate(R.layout.message_receive_item, null);
            TextView message = (TextView) v.findViewById(R.id.message);
            TextView date = (TextView) v.findViewById(R.id.date);
            message.setTypeface(helvetica);
            date.setTypeface(helvetica);
            String dat = m.getDate();
            Long da = Long.parseLong(dat);
            message.setText(m.getMessage());
            date.setText(DateUtils.getRelativeDateTimeString(activity, da, DateUtils.SECOND_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS, 0));
        }

        return v;
    }

}