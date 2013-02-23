package com.shan.volumecontrol;
import com.shan.volumecontrol.SetVolumeFragment;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.util.Log;


public class VolumeControlService extends Service
{
    private static final String TAG = VolumeControlService.class.getSimpleName();
    private VolumeControlReceiver m_VolReceiver = null;
    private int           m_CurrHeadSetVol = 5;
    private int           m_CurrSpeakerVol = 5;
    private boolean       m_IsSetAllStream = false; 
    private VolumeManager m_VolMgr;
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
        if (null != intent)
        {
        m_CurrHeadSetVol = intent.getIntExtra
                            (
                                SetVolumeFragment.HEADSET_VOL, 
                                m_CurrHeadSetVol
                            );

        m_CurrSpeakerVol = intent.getIntExtra
                            (
                                SetVolumeFragment.SPEAKER_VOL, 
                                m_CurrSpeakerVol
                            );
        
        m_IsSetAllStream = intent.getBooleanExtra
                        (
                            SetVolumeFragment.IS_SET_ALL_STREAM, 
                            m_IsSetAllStream
                        );
        }
        else
        {
            Context c = getApplicationContext();
            SharedPreferences pref = getSharedPreferences(SetVolumeFragment.TAG_EXT, MODE_PRIVATE);
                       
            if (null != pref)
            {                
                //Log.d(TAG, "searching pref now.....");
                m_IsSetAllStream = pref.getBoolean(SetVolumeFragment.IS_SET_ALL_STREAM, m_IsSetAllStream);
                m_CurrHeadSetVol = pref.getInt(SetVolumeFragment.HEADSET_VOL, m_CurrHeadSetVol);
                m_CurrSpeakerVol = pref.getInt(SetVolumeFragment.SPEAKER_VOL, m_CurrSpeakerVol);
            }
            else
            {
                throw new NullPointerException(TAG + "Cannot find intent or prefernce");
            }
        }
        
        //Log.d(TAG, "ServiceonStartCommand id");

        AudioManager audio_mgr = 
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);        
        
        triggerVolumeChange(audio_mgr.isWiredHeadsetOn());
        return Service.START_STICKY;
    }

    
    
    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
    }



    @Override
    public void onCreate()
    {
        //Log.d(TAG, "onCreate");
        m_VolMgr = new VolumeManager(getApplicationContext());
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
        //Log.d(TAG, "ServiceonDestroy");
    }
    

    
    
    
    private void triggerVolumeChange(boolean is_headset_on)
    {
        //Log.d(TAG, "triggerVolumeChange");
        
        AudioManager audio_mgr = 
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        int vol_to_set = (is_headset_on) 
                         ? 
                         m_CurrHeadSetVol : m_CurrSpeakerVol;
        
        m_VolMgr.setCurrentStreamVol(vol_to_set);
        
        // we will always set the music stream; the other streams 
        // will be set depending on the user's option
        audio_mgr.setStreamVolume
        (
            AudioManager.STREAM_MUSIC, 
            m_VolMgr.getCurrVol(AudioManager.STREAM_MUSIC), 
            0
        );
        
        if (m_IsSetAllStream)
        {
            for (int stream_id : m_AllOtherStream)
            {
                audio_mgr.setStreamVolume
                (
                   stream_id, 
                   m_VolMgr.getCurrVol(stream_id),
                   0
               );
            }
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
