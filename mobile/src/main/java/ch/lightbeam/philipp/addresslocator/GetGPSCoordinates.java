package ch.lightbeam.philipp.addresslocator;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetGPSCoordinates extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GETGPSCOORDINATES = "ch.lightbeam.philipp.addresslocator.action.getgpscoordinates";
    private GoogleApiClient mGoogleApiClient;
    protected ResultReceiver mReceiver;
    private int mAccuracyCount=10;
    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionGetGPSCoordinates(Context context,String pExtraName, ResultReceiver pReceiver) {
        Intent intent = new Intent(context, GetGPSCoordinates.class);
        intent.setAction(ACTION_GETGPSCOORDINATES);
        intent.putExtra(pExtraName,pReceiver);
        context.startService(intent);
    }


    public GetGPSCoordinates() {
        super("GetGPSCoordinates");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GETGPSCOORDINATES.equals(action)) {
                mReceiver = intent.getParcelableExtra(AddressLocatorConstants.RECEIVER);
                if (mReceiver == null) {
                    System.out.println("Error: no Receiver found");
                    return;
                }
                handleActionGetGPSCoordinates();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetGPSCoordinates() {
        System.out.println("Handle Action Get GPS Coordinates ===========");
        mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    protected void onResume() {
        mGoogleApiClient.connect();
        System.out.println("onresume");

    }

    protected void onPause() {
        System.out.println("onpause");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("connectionsuspended");
        /*if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "connection to location client suspended");
        }*/
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("Location onConnect");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(700);
        System.out.println("FusedLocatoin");
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                .setResultCallback(new ResultCallback() {
                    @Override
                    public void onResult(Result result) {
                        Status status = result.getStatus();
                        if (status.getStatus().isSuccess()) {

                            /*if (Log.isLoggable(TAG, Log.DEBUG)) {
                                Log.d(TAG, "Successfully requested location updates");
                            }
                        } else {
                            System.out.println("status filed");

                            Log.e(TAG,
                                    "Failed in requesting location updates, "
                                            + "status code: "
                                            + status.getStatusCode()
                                            + ", message: "
                                            + status.getStatusMessage());*/
                        }
                    }
                });
    }

    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        System.out.println("Connection failed Result:" + connectionResult.toString());
    }
    public void onLocationChanged(Location newLocation)
    {
        if(newLocation!=null)
        {
            if(newLocation.getAccuracy()<15 || mAccuracyCount<=0)
            {
                System.out.println("Accuracy"+newLocation.getAccuracy());
                System.out.println("Location Changed : " + newLocation.toString());

                deliverResultToReceiver(0, newLocation);
                onPause();
            }
            else
            {
                mAccuracyCount--;
                System.out.println("Warning: not Accurat enough "+newLocation.getAccuracy());
            }


        }



    }

    private void deliverResultToReceiver(int resultCode, Location pLocation) {
        System.out.println("deliverResultToReceiver");
        Bundle bundle = new Bundle();
        bundle.putParcelable(AddressLocatorConstants.RESULT_DATA_KEY,pLocation);
        mReceiver.send(resultCode, bundle);
    }


}
