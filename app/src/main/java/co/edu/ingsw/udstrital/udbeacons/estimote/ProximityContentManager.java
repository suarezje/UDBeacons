package co.edu.ingsw.udstrital.udbeacons.estimote;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.proximity_sdk.proximity.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.proximity.ProximityAttachment;
import com.estimote.proximity_sdk.proximity.ProximityObserver;
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder;
import com.estimote.proximity_sdk.proximity.ProximityZone;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.edu.ingsw.udstrital.udbeacons.R;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProximityContentManager {
    private Context context;
    private EstimoteCloudCredentials cloudCredentials;
    private ProximityObserver.Handler proximityObserverHandler;
    private static final String WS_NOTIFICATIONS_URL = "http://192.168.0.5:8080/UDBeaconServices/services/notificationsService/getNotifications";
    private static String email;
    public boolean isActive;
    private SharedPreferences sharedPref;

    public ProximityContentManager(Context context, EstimoteCloudCredentials cloudCredentials) {
        this.context = context;
        this.cloudCredentials = cloudCredentials;
        this.sharedPref = this.context.getSharedPreferences(
                context.getString(R.string.shared_preference_file), Context.MODE_PRIVATE);
        this.email = this.sharedPref.getString(context.getString(R.string.user_pref_email), context.getString(R.string.default_email));
    }

    public void start() {
        try{
            isActive = true;
            ProximityObserver proximityObserver = new ProximityObserverBuilder(context, cloudCredentials)
                    .withOnErrorAction(new Function1<Throwable, Unit>() {
                        @Override
                        public Unit invoke(Throwable throwable) {
                            Log.e(context.getString(R.string.app_name), "Proximity observer error: " + throwable);
                            return null;
                        }
                    })
                    .withBalancedPowerMode()
                    .build();

            ProximityZone zone = proximityObserver.zoneBuilder()
                    .forAttachmentKeyAndValue(context.getString(R.string.key_zone_classroom),
                            context.getString(R.string.value_zone_classroom))
                    .inNearRange()
                    .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
                        @Override
                        public Unit invoke(ProximityAttachment attachment) {
                            Log.d(context.getString(R.string.app_name),
                                    "Welcome to classroom "+
                                            attachment.getPayload().get(context.getString(R.string.key_zone_classroom)));
                            Log.d(context.getString(R.string.app_name),
                                    "BeaconUID: "+attachment.getDeviceId());
                            sendInfoToServer(attachment.getDeviceId());
                            return null;
                        }
                    })
                    .withOnExitAction(new Function1<ProximityAttachment, Unit>() {
                        @Override
                        public Unit invoke(ProximityAttachment attachment) {
                            Log.d(context.getString(R.string.app_name),
                                    "Bye bye, come visit us again on the classroom "+
                                            attachment.getPayload().get(context.getString(R.string.key_zone_classroom)));
                            return null;
                        }
                    })
                    .create();

            proximityObserver.addProximityZone(zone);

            proximityObserverHandler = proximityObserver.start();
        }catch (Exception e){
            Log.e(context.getString(R.string.app_name),
                    "Ha ocurrido un error iniciando el observador de proximidad");
            this.isActive = false;
        }

    }

    public void stop() {
        proximityObserverHandler.stop();
        isActive = false;
    }

    private void sendInfoToServer(final String beaconID){
        if(email != null && !email.isEmpty()) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, WS_NOTIFICATIONS_URL, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(context.getString(R.string.app_name),"Notification Service Response: "+response.toString());
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
                    params.put("email", email);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(jsonObjectRequest);
        }
    }
}
