package ch.lightbeam.philipp.addresslocator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartWearActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent serviceintent = new Intent(this, StartMobileService.class);
        serviceintent.setAction(StartMobileService.getAction_StartMobilService());
        this.startService(serviceintent);

        finish();
    }
}
