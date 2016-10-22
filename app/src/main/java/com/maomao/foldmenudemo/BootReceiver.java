package com.maomao.foldmenudemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;


public class BootReceiver extends BroadcastReceiver {

    private final static int ERROR_NOTIFICATION_ID = 2;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) Logger.log("booted");

        SharedPreferences prefs =
                context.getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);

        Database db = Database.getInstance(context);

        if (!prefs.getBoolean("correctShutdown", false)) {
            if (BuildConfig.DEBUG) Logger.log("Incorrect shutdown");
            // DEVICE_SHUTDOWN was not sent on shutdown -> display error message
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(ERROR_NOTIFICATION_ID,
                            new Notification.Builder(context).setContentTitle("Incorrect shutdown")
                                    .setContentText(
                                            "Use the power button to shutdown the device, otherwise the app might not be able to save your steps!")
                                    .setShowWhen(false).setSmallIcon(R.drawable.ic_notification)
                                    .build());
            // can we at least recover some steps?
            int steps = db.getCurrentSteps();
            long date = db.getLastDay();
            if (BuildConfig.DEBUG)
                Logger.log("Trying to recover " + (db.getSteps(date) + steps) + " steps");
            db.updateSteps(date, steps);
        }
        // last entry might still have a negative step value, so remove that
        // row if that's the case
        db.removeNegativeEntries();
        db.saveCurrentSteps(0);
        db.close();
        prefs.edit().remove("correctShutdown").apply();

        context.startService(new Intent(context, SensorListener.class));
    }
}
