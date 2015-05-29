package ch.lightbeam.philipp.addresslocator;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class StartMobileService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_STARTMOBILESERVICE = "ch.lightbeam.philipp.addresslocator.action.startmobileservice";
    private GoogleApiClient mGoogleApiClient;
    private Node mNode;
    private static final String ADDRESSLOCATOR_WEAR_PATH = "/addresslocator-wear";

    public static String getAction_StartMobilService()
    {
        return ACTION_STARTMOBILESERVICE;
    }
    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionStartMobileService(Context context) {
        Intent intent = new Intent(context, StartMobileService.class);
        intent.setAction(ACTION_STARTMOBILESERVICE);
        context.startService(intent);
    }



    public StartMobileService() {
        super("StartMobileService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_STARTMOBILESERVICE.equals(action)) {

                handleActionStartMobileService();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStartMobileService() {
        System.out.println("Handle Mobile Service Intent");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    private void resolveNode()
    {
        System.out.println("StartMobileService resolveNode");

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    mNode = node;
                }

                if (mNode != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    System.out.println("Try to start Mobile activity");
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, mNode.getId(), ADDRESSLOCATOR_WEAR_PATH, null).setResultCallback(

                            new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                                    if (!sendMessageResult.getStatus().isSuccess()) {
                                        Log.e("TAG", "Failed to send message with status code: "
                                                + sendMessageResult.getStatus().getStatusCode());
                                        System.out.println("Failed to send mesage");
                                    }
                                }
                            }
                    );
                }
                else
                {
                    System.out.println("StartMobileService resolveNode not Found");
                }
            }
        });
    }
    protected void onResume() {
        mGoogleApiClient.connect();
        System.out.println("StartMobileService onresume");

    }

    protected void onPause() {
        System.out.println("StartMobileService onpause");
        if (mGoogleApiClient.isConnected()) {

        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("StartMobileService onconnectionsuspended");
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("StartMobileService onconnected");
        resolveNode();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        System.out.println("StartMobileService Connection failed Result:" + connectionResult.toString());
    }
}
