package com.shan.volumecontrol;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ViewVolumeFragment extends Fragment
{
    private static final String TAG = ViewVolumeFragment.class.getSimpleName(); 
    private ProgressBar         m_MediaProgressBar, m_RingerProgressBar, 
                                m_AlertProgressBar,m_AlarmProgressBar; 
                                
    private VolumeManager m_VolMgr;

    
    
    @Override
    public void onAttach(Activity activity)
    {
        m_VolMgr = new VolumeManager(activity);
        super.onAttach(activity);
    }
    
        
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle saved_instance)
    {
        View        root_view = inflater.inflate(R.layout.view_volume, container, false);
        View        view;
        TextView    title;
                
        //aet up media
        view = root_view.findViewById(R.id.media);
        title = (TextView)view.findViewById(R.id.vol_bar_media_name);
        title.setText(R.string.vol_view_media);
        m_MediaProgressBar = (ProgressBar)view.findViewById(R.id.vol_bar_progress_bar);
        m_MediaProgressBar.setMax(m_VolMgr.getMaxVol(AudioManager.STREAM_MUSIC));
        
        //set up ringer
        view = root_view.findViewById(R.id.ringer);
        title = (TextView)view.findViewById(R.id.vol_bar_media_name);
        title.setText(R.string.vol_view_ringer);
        m_RingerProgressBar = (ProgressBar)view.findViewById(R.id.vol_bar_progress_bar);
        m_RingerProgressBar.setMax(m_VolMgr.getMaxVol(AudioManager.STREAM_RING));
                
        //set up alert
        view = root_view.findViewById(R.id.alert);
        title = (TextView)view.findViewById(R.id.vol_bar_media_name);
        title.setText(R.string.vol_view_alert);
        m_AlertProgressBar = (ProgressBar)view.findViewById(R.id.vol_bar_progress_bar);
        m_AlertProgressBar.setMax(m_VolMgr.getMaxVol(AudioManager.STREAM_NOTIFICATION));
        
        //set up alarm
        view = root_view.findViewById(R.id.alarm);
        title = (TextView)view.findViewById(R.id.vol_bar_media_name);
        title.setText(R.string.vol_view_alarm);
        m_AlarmProgressBar = (ProgressBar)view.findViewById(R.id.vol_bar_progress_bar);
        m_AlarmProgressBar.setMax(m_VolMgr.getMaxVol(AudioManager.STREAM_ALARM));
                
        return root_view;
    }
    
    @Override
    public void onStart()
    {
        updateVolumeBar();
        super.onStart();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menu_inflater)
    {
        menu_inflater.inflate(R.menu.view_vol_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        case R.id.menu_refresh:
            updateVolumeBar();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onPrepareOptionMenu");
        
        MenuItem item =  menu.findItem(R.id.menu_refresh); 
    }    
    
    private void updateVolumeBar()
    {
        AudioManager audio_mgr = 
                (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        
        m_MediaProgressBar.setProgress
        (
            audio_mgr.getStreamVolume(AudioManager.STREAM_MUSIC)
        );
        m_RingerProgressBar.setProgress
        (
            audio_mgr.getStreamVolume(AudioManager.STREAM_RING)
        );
        m_AlertProgressBar.setProgress
        (
            audio_mgr.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        );
        m_AlarmProgressBar.setProgress
        (
            audio_mgr.getStreamVolume(AudioManager.STREAM_ALARM)
        );
    }
}
