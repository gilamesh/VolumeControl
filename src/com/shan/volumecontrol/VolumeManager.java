package com.shan.volumecontrol;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class VolumeManager
{
    private static final String TAG = VolumeManager.class.getName();
    private int[]               m_MaxStreamVolArr   = {1, 1, 1, 1};
    private int[]               m_StreamVolArr      = {1, 1 ,1 ,1};
    private int             m_MaxStreamVol = -1;
    
    private static final int[] m_MediaType = { 
                    AudioManager.STREAM_MUSIC, AudioManager.STREAM_RING, 
                    AudioManager.STREAM_ALARM, AudioManager.STREAM_NOTIFICATION        
                    };

    
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
        float vol_percent = (float)vol_level / m_MaxStreamVol;
        
        vol_percent = (vol_percent > 1) ? 1 : vol_percent;

        int len = m_MediaType.length;
        for (int i = 0; i < len; ++i)
        {
            float   media_vol_f  = vol_percent * m_MaxStreamVolArr[i];
            int     media_vol;
            
            // make the volunme "sticky" - will not reach true 0% or 100% 
            // unless pushed.
            if (media_vol_f < 0.005)
            {
                media_vol = 0;
            }            
            else if (media_vol_f < 0.995)
            {
                media_vol = 1;
            }
            else if 
            (
                media_vol_f + 1.005 > m_MaxStreamVolArr[i] 
                && 
                media_vol_f + 0.005 < m_MaxStreamVolArr[i]
            )
            {
                media_vol = m_MaxStreamVolArr[i] - 1;
            }
            else
            {
                media_vol = (int)(media_vol_f + 0.5);
            }           
            
            m_StreamVolArr[i] = media_vol;
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
