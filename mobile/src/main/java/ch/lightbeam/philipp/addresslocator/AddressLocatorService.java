package ch.lightbeam.philipp.addresslocator;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ResultReceiver;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class AddressLocatorService extends Service {

    Intent mServiceIntent;
    private GPSCoordinatesResultReceiver mGPSResultReceiver;

    public AddressLocatorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Get Location");
        mServiceIntent=intent;
        mGPSResultReceiver = new GPSCoordinatesResultReceiver(new Handler());

        GetGPSCoordinates.startActionGetGPSCoordinates(this,AddressLocatorConstants.RECEIVER, mGPSResultReceiver);

        System.out.println("End Location");

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    class GPSCoordinatesResultReceiver extends ResultReceiver implements Parcelable {
        public GPSCoordinatesResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            System.out.println("GPS Coordinates Receive Result");
            // Display the address string
            // or an error message sent from the intent service.
            Location lLocation = resultData.getParcelable(AddressLocatorConstants.RESULT_DATA_KEY);

            System.out.println("ResultData" + lLocation.toString());
            resolveAdress(lLocation);

            // Show a toast message if an address was found.
            if (resultCode == AddressLocatorConstants.SUCCESS_RESULT) {
                System.out.println("Adress found" + lLocation.toString());
            }

        }
    }

    public  void resolveAdress(Location pLocation)
    {
        System.out.println("Start resolving address");


        String url = String
                .format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&language="
                        + Locale.getDefault().getCountry(), pLocation.getLatitude(), pLocation.getLongitude());
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("volley response");
                        displayAddressOutput(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        queue.add(jsObjRequest);


    }

    public void displayAddressOutput(JSONObject pLocation)
    {
        System.out.println("Display Notification"+pLocation);
        String pAdress = getCurrentLocationViaJSON(pLocation);
        Bitmap bg = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.osm);

        Notification notif = new Notification.Builder(this)
                .setPriority(1)
                .setSmallIcon(R.drawable.launchericon)
                .setContentTitle("Current Address")
                .setContentText(pAdress)
                .extend(new Notification.WearableExtender().setBackground(bg))
                .build();
        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(0, notif);
        stopSelf();
    }

    public  String getCurrentLocationViaJSON(JSONObject pAddress) {

        JSONObject jsonObj =pAddress;

        String Address1 = "";
        String Address2 = "";
        String City = "";
        String State = "";
        String Country = "";
        String County = "";
        String PIN = "";

        String currentLocation = "";

        try {
            String status = jsonObj.getString("status").toString();

            if (status.equalsIgnoreCase("OK")) {
                JSONArray Results = jsonObj.getJSONArray("results");
                JSONObject zero = Results.getJSONObject(0);
                JSONArray address_components = zero
                        .getJSONArray("address_components");

                for (int i = 0; i < address_components.length(); i++) {
                    JSONObject zero2 = address_components.getJSONObject(i);
                    String long_name = zero2.getString("long_name");
                    JSONArray mtypes = zero2.getJSONArray("types");
                    String Type = mtypes.getString(0);

                    if (Type.equalsIgnoreCase("street_number")) {
                        Address1 = long_name + " ";
                    } else if (Type.equalsIgnoreCase("route")) {
                        Address1 = Address1 + long_name;
                    } else if (Type.equalsIgnoreCase("sublocality")) {
                        Address2 = long_name;
                    } else if (Type.equalsIgnoreCase("locality")) {
                        // Address2 = Address2 + long_name + ", ";
                        City = long_name;
                    } else if (Type
                            .equalsIgnoreCase("administrative_area_level_2")) {
                        County = long_name;
                    } else if (Type
                            .equalsIgnoreCase("administrative_area_level_1")) {
                        State = long_name;
                    } else if (Type.equalsIgnoreCase("country")) {
                        Country = long_name;
                    } else if (Type.equalsIgnoreCase("postal_code")) {
                        PIN = long_name;
                    }

                }

                currentLocation = Address1 + "," + Address2 + "," + City + ","+PIN+",\n"
                        + State + "," + Country ;

            }
        } catch (Exception e) {

        }
        return currentLocation;

    }
}
