package co.edu.ingsw.udstrital.udbeacons;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.estimote.proximity_sdk.proximity.EstimoteCloudCredentials;

import java.util.List;
import java.util.UUID;

import co.edu.ingsw.udstrital.udbeacons.activities.LoginActivity;

/**
 * Created by enriq on 25/3/2018.
 */

public class UDBeacons extends Application {
    private BeaconManager beaconManager;
    public  EstimoteCloudCredentials cloudCredentials =
            new EstimoteCloudCredentials("udbeacons-9u6", "caa32fb735851cbe196d6906f689099a");

    @Override
    public void onCreate() {
        super.onCreate();



        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new BeaconRegion(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        2350, 10872));
            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion region, List<Beacon> beacons) {
                showNotification(
                        "Prueba de ingreso a la región",
                        "Esa es una prueba del ingreso a la región"
                                + "usando los beacons de estimote"
                                + "Vamos avanzando!");
            }
            @Override
            public void onExitedRegion(BeaconRegion region) {
                showNotification(
                        "Saliendo de la región",
                        "Esa es una prueba de salida de la región"
                                + "usando los beacons de estimote"
                                + "Vamos avanzando!");
            }
        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, LoginActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

}
