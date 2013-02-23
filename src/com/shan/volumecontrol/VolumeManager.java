package com.shan.volumecontrol;

import android.content.Context;
import android.media.AudioManager;

public class VolumeManager
{
    private static final String TAG = VolumeManager.class.getName();
    private int[]      m_MaxStreamVolArr = {1, 1, 1, 1, 1, 1};
    private int[]      m_StreamVolArr = {1, 1 ,1 ,1 ,1, 1};
    private static final int[] m_MediaType = { 
                    AudioManager.STREAM_MUSIC, AudioManager.STREAM_RING, 
                    AudioManager.STREAM_VOICE_CALL, AudioManager.STREAM_ALARM, 
                    AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_SYSTEM        
                    };
   
    
    private int         m_MaxStreamVol = -1;
    
    public VolumeManager(Context context)
    {
        AudioManager audio_mgr = 
                (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        
        
        
        
        int len = m_MaxStreamVolArr.length;
        for (int i = 0; i < len; ++i)
        {
            m_MaxStreamVolArr[i] = audio_mgr.getStreamMaxVolume(m_MediaType[i]);
            m_MaxStreamVol =    (m_MaxStreamVol > m_MaxStreamVolArr[i]) 
                                ? 
                                m_MaxStreamVol : m_MaxStreamVolArr[i];     
        }        
        audio_mgr = null;
    }
    
    public int getMaxStreamVol()
    {
        return m_MaxStreamVol;
    }
    
    public void setCurrentStreamVol(int vol_level)
    {
        float vol_percentile = (float)vol_level / m_MaxStreamVol;
        
        vol_percentile = (vol_percentile > 1) ? 1 : vol_percentile;

        int len = m_MediaType.length;
        for (int i = 0; i < len; ++i)
        {
            m_StreamVolArr[i] = (int)(vol_percentile * m_MaxStreamVolArr[i] + 0.5);
        }
    }
    
    public int getCurrVol(int audio_mgr_stream_id)
    {
        for (int i = 0; i < m_MediaType.length; ++i)
        {
            if (m_MediaType[i] == audio_mgr_stream_id)
                return m_StreamVolArr[i];
        }
        
        return -1;
    }

    public int getMaxVol(int audio_mgr_stream_id)
    {
        for (int i = 0; i < m_MediaType.length; ++i)
        {
            if (m_MediaType[i] == audio_mgr_stream_id)
                return m_MaxStreamVolArr[i];
        }
        
        return -1;
    }
    
    
    
}
