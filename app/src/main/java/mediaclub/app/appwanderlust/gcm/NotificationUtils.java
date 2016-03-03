package mediaclub.app.appwanderlust.gcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.R;
import mediaclub.app.appwanderlust.app.Config;

/**
 * Created by Bloom on 11/2/2016.
 */
public class NotificationUtils {


    private static String TAG = NotificationUtils.class.getSimpleName();

    private Context mContext;

    public static final String PREF_FILE_NAME = "mediaclub.app.appwanderlust.preferences";
    public static final String KEY_NOTIFICATIONS = "notifications";
    final static String GROUP_KEY_NOT = "group_key_nots";


    public NotificationUtils() {
    }

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
    }

    public void showNotificationMessage(String title, String message, String timeStamp, Intent intent, String user) {
        showNotificationMessage(title, message, timeStamp, intent, user, null);
    }

    public void showNotificationMessage(final String title, final String message, final String timeStamp, Intent intent, String user, String imageUrl) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;


        // notification icon
        final int icon = R.mipmap.ic_launcher_new_bevel;

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT
                );

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext);

        final Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + mContext.getPackageName() + "/raw/iphone_notification");


        showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, user, alarmSound);
        playNotificationSound();

    }


    private void showSmallNotification(NotificationCompat.Builder mBuilder, int icon, String title, String message, String timeStamp, PendingIntent resultPendingIntent, String user, Uri alarmSound) {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        if (Config.appendNotificationMessages) {
            // store the notification in shared pref first

            String oldNotifications = readFromPreferences(mContext, KEY_NOTIFICATIONS, null);

            if (oldNotifications != null) {
                oldNotifications += "|" + user + ": " + message;
            } else {
                oldNotifications = user + ": " + message;
            }

            saveToPreferences(mContext, KEY_NOTIFICATIONS, oldNotifications);

            // get the notifications from shared preferences
            String oldNotification = readFromPreferences(mContext, KEY_NOTIFICATIONS, null);

            List<String> messages = Arrays.asList(oldNotification.split("\\|"));

            for (int i = messages.size() - 1; i >= 0; i--) {
                inboxStyle.addLine(messages.get(i));
            }
        } else {
            inboxStyle.addLine(user + ": " +message);
        }

        long date = System.currentTimeMillis();

        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setContentText(user + ": " + message)
                .setStyle(inboxStyle)
                .setWhen(date)
                .setVibrate(new long[]{100, 200, 100, 500})
                .setSmallIcon(R.mipmap.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .build();

//        Random random = new Random();
//        int m = random.nextInt(9999 - 1000) + 1000;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID, notification);
    }

    // Playing notification sound
    public void playNotificationSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + AppController.getInstance().getApplicationContext().getPackageName() + "/raw/iphone_notification");
            Ringtone r = RingtoneManager.getRingtone(AppController.getInstance().getApplicationContext(), alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method checks if the app is in background or not
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    // Clears notification tray messages
    public static void clearNotifications() {
        NotificationManager notificationManager = (NotificationManager) AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
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
