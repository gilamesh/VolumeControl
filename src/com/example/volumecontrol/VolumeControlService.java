package com.example.volumecontrol;
import com.example.volumecontrol.SetVolumeActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.provider.Settings.System;
import android.util.Log;


public class VolumeControlService extends Service
{
    private static final String TAG = VolumeControlService.class.getSimpleName();
    private VolumeControlReceiver m_VolReceiver = null;
    private int           m_CurrHeadSetVol = 1;
    private int           m_CurrSpeakerVol = 1;
    private boolean       m_IsSetAllStream = false; 
    private static final int[] m_AllOtherStream = 
                                    {
                                        AudioManager.STREAM_ALARM, 
                                        AudioManager.STREAM_NOTIFICATION, 
                                        AudioManager.STREAM_RING, 
                                        AudioManager.STREAM_SYSTEM, 
                                        AudioManager.STREAM_VOICE_CALL
                                    };
    
    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        
        m_CurrHeadSetVol = intent.getIntExtra
                            (
                                SetVolumeActivity.HEADSET_VOL, 
                                m_CurrHeadSetVol
                            );

        m_CurrSpeakerVol = intent.getIntExtra
                            (
                                SetVolumeActivity.SPEAKER_VOL, 
                                m_CurrSpeakerVol
                            );
        
        m_IsSetAllStream = intent.getBooleanExtra
                        (
                            SetVolumeActivity.IS_SET_ALL_STREAM, 
                            m_IsSetAllStream
                        );
        
        //Log.d(TAG, "onStartCommand id");

        AudioManager audio_mgr = 
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);        
        
        triggerVolumeChange(audio_mgr.isWiredHeadsetOn());
        return Service.START_REDELIVER_INTENT;
    }

    
    
    @Override
    public void onCreate()
    {
        //Log.d(TAG, "onCreate");
        //Set default values
        m_CurrHeadSetVol = 1;//m_AudioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        m_CurrSpeakerVol = 1;//m_CurrHeadSetVol;
        m_IsSetAllStream = false;
        
        IntentFilter receive_filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        m_VolReceiver = new VolumeControlReceiver();
        registerReceiver(m_VolReceiver, receive_filter);
    }



    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy()
    {
        if (null != m_VolReceiver)
            unregisterReceiver(m_VolReceiver);
        //Log.d(TAG, "onDestroy");
    }
    

    private void triggerVolumeChange(boolean is_headset_on)
    {
        AudioManager audio_mgr = 
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        int vol_to_set;
        
        if (is_headset_on)
            vol_to_set = m_CurrHeadSetVol; 
        else 
            vol_to_set = m_CurrSpeakerVol;
        
        // we will always set the music stream; the other streams 
        // will be set depending on the user's option
        Log.d(TAG, "is set all streams? " + Boolean.toString(m_IsSetAllStream));
        audio_mgr.setStreamVolume(AudioManager.STREAM_MUSIC, vol_to_set, 0);
        if (m_IsSetAllStream)
        {
            for (int stream_id : m_AllOtherStream)
                audio_mgr.setStreamVolume(stream_id, vol_to_set, 0);
        }
    }

    
    
    
    class VolumeControlReceiver extends BroadcastReceiver
    {
        /**
         * As specified in http://developer.android.com/reference/
         * android/content/Intent.html#ACTION_HEADSET_PLUG
         */
        private static final int HEADSET_UNPLUG = 0; 
        private static final int HEADSET_PLUG   = 1;
        
        
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int headset_state =  intent.getIntExtra("state", -1);

            
            //Log.d(TAG, "headset state: " + Integer.toString(headset_state));
            if (headset_state == -1)
            {
                Log.d(TAG, "Unknown headset state " + Integer.toString(headset_state));
                return;
            }
            
            boolean is_headset_on = (HEADSET_PLUG == headset_state) 
                                    ?
                                    true : false;
            
            triggerVolumeChange(is_headset_on);
        }
        
    }
}