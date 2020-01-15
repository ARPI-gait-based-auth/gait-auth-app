package c.arp.gaitauth;


import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Api {

    public static final String API_KEY = c.arp.gaitauth.BuildConfig.apikey;
    public static RequestQueue requstQueue;

    public static void sendRecord(final String name, final String key, final String data, final Context context) {
        String url = "https://gait.modri.si/" + API_KEY + "/record/" + name + "/" + key;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("csv", data);
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            Toast.makeText(context, "Record successfully uploaded.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null) {
                            Toast.makeText(context, "Record upload failed. Please notify developer.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
        };
        requstQueue.add(jsonobj);
    }

    /**
     * Tell if record belongs to given owner.
     * @param name - potential owner
     * @param data
     * @param context
     */
    public static void authenticateRecord(final String name, final String data, final Context context) {
        String url = "https://gait.modri.si/" + API_KEY + "/detect/" + name;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("csv", data);
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            Double authtrust = null;
                            try {
                                authtrust = response.getDouble("authTrust");
                                Toast.makeText(context, "Record auth successful. Auth trust is: " + authtrust, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Record auth failed. Please notify developer.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null) {
                            Toast.makeText(context, "Record auth failed. Please notify developer.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
        };
        requstQueue.add(jsonobj);
    }
}
