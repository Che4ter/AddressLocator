package ch.lightbeam.philipp.addresslocator;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by philipp on 5/25/15.
 */
public class WearListener extends WearableListenerService {

    private static final String ADDRESSLOCATOR_WEAR_PATH = "/addresslocator-wear";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        System.out.println("Message from Wear");
        /*
         * Receive the message from wear
         */
        if (messageEvent.getPath().equals(ADDRESSLOCATOR_WEAR_PATH)) {
            System.out.println("Location Request received");
            Intent intent = new Intent(this, AddressLocatorService.class);

            this.startService(intent);
        }

    }
}
