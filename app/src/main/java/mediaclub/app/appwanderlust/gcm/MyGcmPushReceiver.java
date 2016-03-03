package mediaclub.app.appwanderlust.gcm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import mediaclub.app.appwanderlust.ChatActivity;
import mediaclub.app.appwanderlust.MainActivity;
import mediaclub.app.appwanderlust.app.Config;

/**
 * Created by Bloom on 12/2/2016.
 */
public class MyGcmPushReceiver extends GcmListenerService {

    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_FLAG_BACK_NOT = "start_chat";

    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();

    private NotificationUtils notificationUtils;

    /**
     * Called when message is received.
     *
     * @param from   SenderID of the sender.
     * @param bundle Data bundle containing message data as key/value pairs.
     *               For Set of keys use data.keySet().
     */

    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        String title = bundle.getString("title");
        String message = bundle.getString("message");
        String image = bundle.getString("image");
        String timestamp = bundle.getString("created_at");
        String user = bundle.getString("user");
        String id = bundle.getString("user_id");
        Log.e(TAG, "From: " + from);
        Log.e(TAG, "Title: " + title);
        Log.e(TAG, "message: " + message);
        Log.e(TAG, "image: " + image);
        Log.e(TAG, "timestamp: " + timestamp);

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            pushNotification.putExtra("from", user);
            pushNotification.putExtra("time", timestamp);
            pushNotification.putExtra("otherId", id);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
//            NotificationUtils notificationUtils = new NotificationUtils();
//            notificationUtils.playNotificationSound();
        } else {

            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            resultIntent.putExtra("message", message);
            resultIntent.putExtra("type", "notification");

            saveToPreferences(getApplicationContext(),KEY_FLAG_BACK_NOT,"true");

            showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent, user);

        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent, String user) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, user);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
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
