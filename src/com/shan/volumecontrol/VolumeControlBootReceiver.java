package com.shan.volumecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;


/**
 * Revive the service upon reboot if service was running before phone was 
 * shutdown.
 * @author Wen Shan Chang
 */
public class VolumeControlBootReceiver extends BroadcastReceiver
{
    private static final String TAG = 
            VolumeControlBootReceiver.class.getSimpleName();
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences pref = 
                context.getSharedPreferences
                (
                    VolumeControlService.VOL_CTRL_TAG, 
                    Context.MODE_PRIVATE
                );
        
        boolean is_service_running = 
                pref.getBoolean
                (
                    VolumeControlService.IS_VOLSERVICE_RUNNING, 
                    false
                );
        if (is_service_running)
        {
            Intent init_intent = new Intent();
            
            init_intent.setClassName
            (
                context.getPackageName(), 
                VolumeControlService.class.getName()
            );
            
            context.startService(init_intent);
        }

    }


}
