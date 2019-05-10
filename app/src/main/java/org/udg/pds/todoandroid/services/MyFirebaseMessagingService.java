package org.udg.pds.todoandroid.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.udg.pds.todoandroid.Constants;
import org.udg.pds.todoandroid.MyNotificationManager;
import org.udg.pds.todoandroid.TodoApp;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//class extending FirebaseMessagingService
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String TAG = "Firebase: ";

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "Title", body = "Body";

        Log.d(TAG, "Mesage receiverd!!!!!");

        //if the message contains data payload
        //It is a map of custom keyvalues
        //we can read it easily
        if(remoteMessage.getData().size() > 0){
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }


        //then here we can use the title and body to build a notification
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, importance);
            mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

        MyNotificationManager.getInstance(this).displayNotification(title, body);
    }
}