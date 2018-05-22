package co.edu.ingsw.udstrital.udbeacons;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.coresdk.service.BeaconManager;
import com.estimote.proximity_sdk.proximity.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.proximity.ProximityAttachment;
import com.estimote.proximity_sdk.proximity.ProximityObserver;
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder;
import com.estimote.proximity_sdk.proximity.ProximityZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import co.edu.ingsw.udstrital.udbeacons.activities.LoginActivity;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;


/**
 * Created by enriq on 25/3/2018.
 */

public class UDBeacons extends Application {
    private Context context;
    private BeaconManager beaconManager;
    private ProximityObserver.Handler proximityObserverHandler;

    public  EstimoteCloudCredentials cloudCredentials =
            new EstimoteCloudCredentials("udbeacons-9u6", "caa32fb735851cbe196d6906f689099a");

    private static final String WS_NOTIFICATIONS_URL = "http://35.231.239.50:8080/UDBeaconServices/services/notificationService/getNotifications";
    private static final String WS_REGISTERATTENDANCE_URL = "http://35.231.239.50:8080/UDBeaconServices/services/attendanceService/registerAttendance";
    private static final String WS_OCONSULTSCHEDULE_URL = "http://35.231.239.50:8080//UDBeaconServices/services/consultScheduleService/consultSchedule";
    private static String email;
    public boolean isActive;
    private SharedPreferences sharedPref;
    NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        this.context = getApplicationContext();
        this.sharedPref = this.context.getSharedPreferences(
                context.getString(R.string.shared_preference_file), Context.MODE_PRIVATE);
        this.email = this.sharedPref.getString(context.getString(R.string.user_pref_email), context.getString(R.string.default_email));

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setContentTitle("Escaneando beacons")
                .setContentText("Estamos escaneando los beacons")
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;

        ProximityObserver proximityObserver = new ProximityObserverBuilder(getApplicationContext(), cloudCredentials)
                .withScannerInForegroundService(notification)
                .withEstimoteSecureMonitoringDisabled()
                .withTelemetryReportingDisabled()
                .build();

        ProximityZone zoneClassRoom = proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue(getApplicationContext().getString(R.string.key_zone_classroom),
                        getApplicationContext().getString(R.string.value_zone_classroom))
                .inNearRange()
                .withOnExitAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment attachment) {
                        Log.d(getApplicationContext().getString(R.string.app_name),
                                "Bye bye from classroom "+
                                        attachment.getPayload().get(getApplicationContext().getString(R.string.key_zone_classroom)));
                        Log.d(getApplicationContext().getString(R.string.app_name),
                                "BeaconUID: "+attachment.getDeviceId());
                        consultSchedule(attachment.getPayload().get(getApplicationContext().getString(R.string.key_zone_classroom)));
                        return null;
                    }
                })
                .create();

        ProximityZone zoneUniversity = proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue(getApplicationContext().getString(R.string.key_zone_university),
                        getApplicationContext().getString(R.string.value_zone_university))
                .inNearRange()
                .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment attachment) {
                        Log.d(getApplicationContext().getString(R.string.app_name),
                                "Welcome to University");
                        Log.d(getApplicationContext().getString(R.string.app_name),
                                "BeaconUID: "+attachment.getDeviceId());
                        requestForNotifications(attachment.getDeviceId());
                        return null;
                    }
                })
                .create();

        proximityObserver.addProximityZone(zoneClassRoom);
        proximityObserver.addProximityZone(zoneUniversity);

        proximityObserverHandler = proximityObserver.start();

    }

    public void showNotification(String title, String message, int idNotification) {
        Intent notifyIntent = new Intent(this, LoginActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setContentTitle(title)
                .setStyle(new Notification.BigTextStyle().bigText(message))
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notificationManager.notify(idNotification
                , notification);
    }

    private String[] validateAttendance(JSONArray schedule, String classroomID){
        Date currentDateTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        SimpleDateFormat sdfJustDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDayOfWeek = sdfDayOfWeek.format(currentDateTime).toUpperCase();
        for(int i = 0; i < schedule.length(); i++){
            try {
                JSONObject subjectRow = schedule.getJSONObject(i);
                String dayOfWeekSchedule = subjectRow.getString("day").toUpperCase();
                String classroomIDSchedule = subjectRow.getString("classRoom").toUpperCase();
                String endHourSchedule = subjectRow.getString("timeEnd");
                String endDateTimeSubject = sdfJustDate.format(currentDateTime)+" "+endHourSchedule;
                Date dateEndDateTimeSubject = null;
                try {
                    dateEndDateTimeSubject = sdfDateTime.parse(endDateTimeSubject);
                } catch (ParseException e) {
                    Log.d(context.getString(R.string.app_name),"Error parsing date");
                    e.printStackTrace();
                    return null;
                }
                String subjectIDSchedule = subjectRow.getString("subjectCode");
                String subjectNameSchedule = subjectRow.getString("subjectName");

                if(classroomIDSchedule.equals(classroomID.toUpperCase()) && dayOfWeekSchedule.equals(currentDayOfWeek) && dateEndDateTimeSubject != null){
                    long diffDatesInMillies = Math.abs(dateEndDateTimeSubject.getTime()-currentDateTime.getTime());
                    long diffDatesInMinutes = TimeUnit.MINUTES.convert(diffDatesInMillies, TimeUnit.MILLISECONDS);
                    if(diffDatesInMinutes >= 0 && diffDatesInMinutes <= 30){
                        return new String[]{subjectIDSchedule, subjectNameSchedule};
                    }
                }

            } catch (JSONException e) {
                Log.d(context.getString(R.string.app_name),"Error parsing JSON Schedule");
                e.printStackTrace();
                return null;
            }

        }
        return null;
    }


    private JSONArray consultSchedule(final String classroomID){
        JSONArray[] jsonArraySchedule = new JSONArray[1];
        if(email != null && !email.isEmpty()) {
            JSONObject js = new JSONObject();
            try {
                js.put("user", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, WS_OCONSULTSCHEDULE_URL, js, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(context.getString(R.string.app_name),"Notification Service Response: "+response.toString());
                                if(response.get("code").equals("00")){
                                    JSONArray jsonArraySchedule = response.getJSONArray("classSchedule");
                                    if(jsonArraySchedule != null){
                                        String[] subject = validateAttendance(jsonArraySchedule, classroomID);
                                        if(subject != null){
                                            registerAttendance(subject[0], subject[1]);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();

                            } finally {
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(context.getString(R.string.app_name), "Error invoking notification service: " + error.getMessage());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("user", email);
                    return params;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(jsonObjectRequest);
        }
        return jsonArraySchedule[0];
    }

    private void registerAttendance(final String subjectID, final String subjectName){
        final JSONArray[] jsonArraySchedule = new JSONArray[1];
        if(email != null && !email.isEmpty()) {
            final JSONObject js = new JSONObject();
            try {
                js.put("subjectId", subjectID);
                js.put("user", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, WS_REGISTERATTENDANCE_URL, js, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(context.getString(R.string.app_name),"Notification Service Response: "+response.toString());
                                if(response.get("code").equals("00")){
                                    showNotification(context.getString(R.string.app_name), context.getString(R.string.attendance_ok)+" "+subjectName,23);
                                }else{
                                    showNotification(context.getString(R.string.app_name), context.getString(R.string.attendance_error)+" "+subjectName,23);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();

                            } finally {
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(context.getString(R.string.app_name), "Error invoking notification service: " + error.getMessage());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("subjectId", subjectID);
                    params.put("user", email);
                    return params;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(jsonObjectRequest);
        }
    }

    private void requestForNotifications(final String beaconID){
        if(email != null && !email.isEmpty()) {
            JSONObject js = new JSONObject();
            try {
                js.put("beacon_id", beaconID);
                js.put("user", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, WS_NOTIFICATIONS_URL, js, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(context.getString(R.string.app_name),"Notification Service Response: "+response.toString());
                                if(response.get("code").equals("00")){
                                    JSONArray notifications = response.getJSONArray("notifications");
                                    for(int i=0; i < notifications.length(); i++){
                                        showNotification(getApplicationContext().getString(R.string.app_name), notifications.getJSONObject(i).getString("textNotification")
                                                , notifications.getJSONObject(i).getInt("idNotification"));
                                    }
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();

                            } finally {
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(context.getString(R.string.app_name), "Error invoking notification service: " + error.getMessage());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("beacon_id", beaconID);
                    params.put("user", email);
                    return params;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(jsonObjectRequest);
        }
    }
}
